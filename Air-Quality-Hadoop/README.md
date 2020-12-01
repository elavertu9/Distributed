# Air-Quality-Hadoop
* Evan Lavertu
* CS455 Assignment 3

## About
Data analysis using the EPA's Air Quality System dataset and hadoop

## Tools
* Java
    * Project and files are written in Java
* Gradle
    * Used for jar file creation and dependency management
* Hadoop
    * Environment used to run MapReduce application

## Build
* Project is built using Gradle. Gradle generates a jar file in the build/libs directory
* To create the jar file, execute gradle build
* The jar file can run using
    * cs455.hadoop.JobCoordinator

## Run
* To run this application you must be using the shared hdfs or have a local hdfs configuration setup with test files.
* After building the jar:
    * $HADOOP_HOME/bin/hadoop jar /build/libs/Air-Quality-Hadoop.jar cs455.hadoop.JobCoordinator /input/path /output/path 1-6
    * The 1-6 at the end is which question you want to run
* Appended output folder is labeled /output/path/final

## Package cs455.hadoop
This package contains classes to perform MapReduce jobs in hadoop.
#### JobCoordinator.java
* This class contains the main method. It sets up jobs using the classes below to be executed using hadoop

#### SiteMapper.java
* Mapper1 for Question 1
    * Input: File Chunk
    * Output: Text, Text
* Creates a UID for individual sites by combining state code, county code, and site number
* Writes pair \<UID, state-name>

#### SiteReducer.java
* Reducer1 for Querstion 1
    * Input: Text, Text
    * Output: Text, Text
* Reads in <UID, state-name> and flips key and val in output 
* Writes pair \<state-name, UID>
* Job controller places output of this reducer in temp directory

#### SwapMapper.java
* Mapper2 for Question 1
    * Input: Text, Text
    * Output: Text, IntWritable
* Reads in from temp directory once SiteReducer finishes
* Simply swaps key val
* Writes pair \<state-name, 1>

#### SwapReducer.java
* Reducer2 for Question 1
    * Input: Text, IntWritable
    * Output: Text, IntWritable
* Counts all the occurrences for each state name
* overrides cleanup method to order output
    * This does not work if the class being used for reducer and combiner is the same
* Writes final output \<state-name, count>


#### CoastMapper.java
* Mapper for Question 2
    * Input: File Chunk
    * Output: Text, DoubleWritable
* Uses an enum to identify coastal states by their state code
* Writes pair \<coast - measurement>

#### CoastReducer.java
* Reducer for Question 2
    * Input: Text, DoubleWritable
    * Output: Text, DoubleWritable
* Counts all the entries while summing them up in a separate variable
* Writes final output \<coast - mean>

#### TimeMapper.java
* Mapper for Question 3
    * Input: File Chunk
    * Output: Text, DoubleWritable
* Finds measurements for dates within bounds during each hour
* Writes pair \<time - measurement>

#### TimeReducer.java
* Reducer for Question 3
    * Input: Text, DoubleWritable
    * Output: Text, DoubleWritable
* Counts all the entries while summing them up in a separate variable
* Writes final output \<time - mean>

#### FortyMapper.java
* Mapper for Question 4
    * Input: File Chunk
    * Output: Text, DoubleWritable
* Finds SO2 levels over the past 40 years
* Writes pair \<year - measurement>    
    
#### FortyReducer.java
* Reducer for Question 4
    * Input: Text, DoubleWritable
    * Output: Text, DoubleWritable
* Counts all the entries while summing them up in a separate variable
* Writes final output \<year - mean>

#### HotMapper.java
* Mapper for Question 5
    * Input: File Chunk
    * Output: Text, DoubleWritable
* Finds measurements during the months of June, July, and August for each state
* Writes pair \<state - measurement>

#### HotReducer.java
* Reducer for Question 5
    * Input: Text, DoubleWritable
    * Output: Text, DoubleWritable
* Counts all the entries while summing them up in a separate variable
* Writes final output \<state - mean>

#### StateMapper.java
* Mapper for Question 6
    * Input: File Chunk
    * Output: Text, DoubleWritable
* Searches for SO2 measurements associated with the top 10 hottest states from Question 5
    * Arizona, Puerto Rico, Virgin Islands, Texas, Nevada, Mississippi, Florida, Louisiana, Arkansas, Oklahoma
* Writes pair \<state - measurement>

#### StateReducer.java
* Reducer for Question 6
    * Input: Text, DoubleWritable
    * Output: Text, DoubleWritable
* Counts all the entries while summing them up in a separate variable
* Writes final output \<state - mean>