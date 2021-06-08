# Akka Actors for Basic Data Processing demo project

This project is a Scala application which uses Akka to create a few actors that are used for a basic data processing application (in this case, reading lines from a file, counting the number of words in each line, and then sending this information to an aggregator actor which totals up the values). 

- - -

## Versions of software used: 
- Scala 2.13.1
- Akka 2.6.14
- SBT 1.4.7

- - -

## To run the project: 
```bash
sbt run
```

