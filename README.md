# Streaming Service


## Summary

**streaming-service** is a Java 8 and Akka Stream library based service.
 The service essentially processes incoming **Transaction** messages, asynchronously, and generates **TransactionResult** messages which are output to **std out**.


## Technologies

Java 8, Akka Stream and Test-Kit library, JUnit, Maven exec plugin. 
It was developed using IntelliJ and it builds and runs fine from both the command line and IntelliJ.


## Build

Project is built using  Maven (see pom.xml in the project folder). 

To build the project, use standard **mvn install** or IntelliJ Maven Plugin. 

## Run

To run the service, you can use IntelliJ with the main method in the class file StreamingService.java.
It can also be run from the command line using **mvn exec:java** in the streaming-service project folder (after building it!).

## Design/Code

Akka Streams library is used to demonstrate processing of input stream (from a text file) and "streaming" output to std out.

NOTE: StreamingService class which implements the **Service** interface could have just implemented the interface and rest of the akka related code
would probably benefit from being in a separate "TransactionProcessor" class file. However, for simplicity and brevity, most of the code 
is kept in StreamingService.java class file - which also implements the **Service** interface.
The input-data.txt file was generated randomly. I have not included this random generator class in the project though.

NOTE: If you are running in either MacOS/Linux/Windows environment, the input-data.txt files **may** need to be adjusted for CR/LF differences between environments.
This is because of the way text files are handled by Git between Windows and Nix platforms.

The main class method uses **input_data.txt** file in the ../src/main/resources folder. 

## Tests 

Basic Unit tests are provided and they use akka-stream-testkit library. These test do not represent
fully comprehensive test suite and do not necessarily test all the public methods and edge cases.
These tests use test input data files which reside in the ../src/main/test/resources folder.

## Assumptions/Exclusions/Other Considerations

- To keep things simple, each input message/event is essentially a line (integer) from the input text file. It represents a hypothetical "transaction id" in the Transaction Object. And for it to be valid, it must be a positive integer. Negative integer is regarded as business logic failure, and any other text is regarded as a java exception throwing input. 

- Project does not use application config/properties file. However, this must be provided to extract out properties like parallel execution threads, input stream names etc.

- This project does not used logging framework like slf4j or logback. Neither does it use any other helpers like "Lombok" for model objects etc.