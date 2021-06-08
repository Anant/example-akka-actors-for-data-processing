package com.dataproc

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

//import scala.concurrent._
//import scala.concurrent.duration._
//import ExecutionContext.Implicits.global

import scala.util.Random;
import scala.io.Source


//#counteraggregator-actor
object CounterAggregator {
  final case class CountMessage(value: Int)
  final case class LineMessage(line: String, hub: ActorRef[CountMessage]) 
  private var count = 0;

  def apply(): Behavior[CountMessage] = {
    Behaviors.setup { context =>
      context.log.info("CounterAggregator has been spawned")

      Behaviors.receiveMessage { message =>
        count += message.value
        context.log.info("The current count is: " + count)
        Behaviors.same
      }
    }
  }
}


object CounterBot {

  def apply(): Behavior[CounterAggregator.LineMessage] = {
    Behaviors.setup { context =>
      context.log.info("CounterBot has been spawned")

      Behaviors.receiveMessage { message =>
        //Take .line of the message. Count the words. Send a CountMessage with the result to message.hub
        var countResult = message.line.toLowerCase.split("[ ,!.]+")
        //for(word <- countResult)
            //context.log.info(word)
        message.hub ! CounterAggregator.CountMessage(countResult.length)
        Behaviors.stopped
      }
    }
  }
}


object CounterMain {

  final case class InitializeMessage(line: String)
  var rand = new Random()

  def apply(): Behavior[InitializeMessage] =
    Behaviors.setup { context =>
      context.log.info("Counter main has just been created")

      //Spawn in a CounterAggregator here
      val aggregator = context.spawn(CounterAggregator(), "AggregatorBot")

      Behaviors.receiveMessage { message =>
        //Spawn in a CounterBot, send it message to go do work.
        //context.log.info(message.line)
        val countingBot = context.spawn(CounterBot(), "CountingBot" + rand.nextInt())
        countingBot ! CounterAggregator.LineMessage(message.line, aggregator)

        Behaviors.same
      }
    }
}


object DataProc extends App {

  val counterMain: ActorSystem[CounterMain.InitializeMessage] =
    ActorSystem(CounterMain(), "AkkaDataProc")

  val bufferedSource = Source.fromResource("sample.txt")
  for (line <- bufferedSource.getLines) {
     counterMain ! CounterMain.InitializeMessage(line)
  }

  //Close the file after we are done with it. 
  bufferedSource.close
  
}
