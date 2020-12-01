# CS455: Distributed Systems Assignment 2
## About
* This code base uses Java NIO and contains code for both client and server classes. It uses the server to handle network traffic through a thread pool. This thread pool
  has a configurable number of worker threads that are used to perform tasks relating to network communications. The thread pool manages:
  * Management of incoming network connections
  * Receiving of data over these network connections
  * Organizing data into batches to improve performance
  * Sending data over any of the links

## Tools
* Java
    * Project and all files are written in Java
    * Uses Java 13
* Gradle
    * Gradle used for jar file generation and dependency management

## Build


## Run


## Package cs455.scaling.client
This package connects to server and sends data. It also maintains the hashes of the data being sent and compares the hashes with the ones the server got.
#### Client.java
* This class establishes a connection to the server and reads the responses returned for the messages sent by the sender threads

#### ClientSenderThread.java
* This class sends randomly computed byte arrays to the server at a user defined interval (message rate)
* Stores hashes of these byte arrays to be compared upon response from the server

## Package cs455.scaling.server
This package establishes client connections and processes client requests
#### Server.java
* This class accepts connections from clients and creates tasks that it hands off to the thread pool

#### ThreadPool.java
* This class contains a pool of worker threads that handle tasks being maintained by the thread pool
* Creates batches of work to be performed by worker threads and places them on a queue to be handled by the worker threads

#### WorkerThread.java
* This class is created as a part of a thread pool
* Worker Threads look at the queue being maintained by the thread pool manager
    * If a batch is available on the queue, the worker thread takes it and processes the requests
    * If a batch is not available, it waits until one is ready

## Package cs455.scaling.task
This package provides utilities to maintain the different types of tasks to be handled by the thread pool. It also contains some console print tools for the client and server to print their statistics.
#### Batch.java
* This class maintains a list of tasks in a batch to be completed by the worker threads in the thread pool
* The thread pool will place a batch on the work queue as soon as either the batch time has expired or the batch size was met

#### ClientStatPrinter.java
* This class prints statistics for the client every 20 seconds

#### ServerStatPrinter
* This class prints statistics for the server every 20 seconds

#### Task.java
* This class is used to abstract different types of requests sent to the server by the client. Tasks are then combined by the thread pool manager into batches to be processed by the worker threads. 

## Package cs455.scaling.util
* This package provides tools needed throughout the code base. It is responsible for timers and hashing
#### Hasher.java
* This class provides a function to convert a byte array into a hash code

#### AtomicStats.java
* This class is used by the clients to track and maintain information about the number of packets sent and received

#### ClientTracker.java
* This class is used by the server as a way to track client connections and individual client throughput.

