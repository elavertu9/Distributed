# CS455: Distributed Systems Assignment 1
## About
This code base is a peer-to-peer networking overlay. It uses a single Registry to create a network overlay
of OverlayNodes (MessagingNodes). The Registry achieves this functionality through TCP connections.

## Tools
* Java
    * Project and all files written in Java
    * Uses Java 13
* Gradle
    * Gradle used for jar file generation and dependency management

## Build
* This project is built using gradle. Gradle generates a jar file and with one of the two following options it 
  can act as either a Registry or MessagingNode
  * cs455.overlay.node.Registry <port>
  * cs455.overlay.node.MessagingNode <registry-hostname> <registry-port>

## Run
* This project must be built and ran on all computers acting as a Registry Node and a MessagingNode
* The Registry must be started before any MessagingNode's
* This code base contains the gradle wrapper which can be used to generate the jar file
* From the root of the directory the first thing you need to do is run ./gradlew build
    * This utilizes the gradle wrapper in the root of the directory
    * This generates the build directory
* Navigate to the build/libs/ directory and in there you will see the jar file needed to execute the program
* From that directory run java -cp cs455-1.0-SNAPSHOT.jar
    * Note: Java version required is 13, on the CSB120 lab machines I had to point it to /usr/lib/jvm/java-13/bin/java
    * You will need to append the node type and command line arguments to this command the options are:
        * cs455.overlay.node.Registry <port>
        * cs455.overlay.node.MessagingNode <registry-hostname> <registry-port>
    * Registry Example: java -cp cs455-1.0-SNAPSHOT.jar cs455.overlay.node.Registry 8099
    * MessagingNode Example: java -cp cs455-1.0-SNAPSHOT.jar cs455.overlay.node.MessagingNode denver.cs.colostate.edu 8099
        * If Registry is running on Denver
* Once the program has finished executing, you must restart it to run it again.



## Package cs455.overlay.node
This package is responsible for all overlay node types. It handles the creation and communication between Registry and all Messaging Nodes.

#### Node.java
* This class defines an interface for overlay nodes. It includes the onEvent() method which is used to identify message types so that they can be properly handled.

#### MessagingNode.java
* Implements Event
* This class is responsible for the creation of MessagingNodes and communication amongst itself and the Registry. As well as communication
  amongst MessagingNodes in its sub routing table

#### Registry.java
* Implements Event
* This class is responsible for the creation of a Registry overlay node and is the controller for all MessagingNode's in the overlay.
* It handles things such as overlay node registration, deregistration, routing table creation, and task initiation.

#### WrapperNode.java
* This class is used in data structures within overlay nodes so that the information being stored is limited to only the data needed to create and communicate
  within the overlay.

#### WrapperNodeComparator.java
* This class is used to compare different WrapperNodes so that data structures containing WrapperNode objects can be ordered.




## Package cs455.overlay.routing
This package is responsible for defining routing tables within the network overlay.

#### RoutingEntry.java
* This class contains data about a MessagingNode within the network overlay. Gets added to RoutingTable.

#### RoutingTable.java
* This class contains RoutingEntry objects. The routing table is used to decide where a message should be sent and defines who is allowed to talk to who.





## Package cs455.overlay.transport
This package is responsible for handling TCP socket connections and the sending/receiving of data within the network overlay.

#### TCPConnection.java
* This class is used to store data about a connection between either two MessagingNodes or a MessagingNode and the Registry

#### TCPConnectionsCache.java
* This class is used to store all TCPConnections for a ServerThread.

#### TCPReceiverThread.java
* This class is responsible for receiving data from a TCPConnection.

#### TCPSender.java
* This class is responsible for sending data through a TCPConnection.

#### TCPServerThread.java
* This class is responsible for initiating socket connections when a connection is requested to the server socket port.






## Package cs455.overlay.util
This package is responsible for providing small utilities/functionality needed to communicate and operate the network overlay.

#### MessagingNodeCommandParser.java
* This class handles user input while the MessagingNode is running. It allows for the following commands:
    * print-counters-and-diagnostics
    * exit-overlay

#### RegistryCommandParser.java
* This class handles user input while the Registry is running. It allows for the following commands:
    * list-messaging-nodes
    * setup-overlay number-of-routing-table-entries
        * Default is 3
    * list-routing-tables
    * start number-of-messages

#### StatCollector.java
* This class is responsible for collecting stats within the overlay and displaying them.





## Package cs455.overlay.wireformats
This package is responsible for defining and creating different message types that are allowed to be sent within the network overlay.

#### EventFactory.java
* This is a singleton instance used to create and identify message types that are being received within the network overlay.

#### Event.java
* This class defines an interface for all message types to implement. It contains methods getBytes() and getType().
    * getBytes() is used to convert data within the message to a byte array in order to send it across the network.
    * getType() is used to identify the message being sent within the overlay.
    
#### Deregister.java
* Implements Event
* This class defines a message type for when a MessagingNode wants to deregister from the Registry.

#### DeregisterSuccess.java
* Implements Event
* This class defines a message type for when the Registry reports back the MessagingNode about its deregistration request.
* Status field assigned node id on success, and -1 on fail.

#### NodeManifest.java
* Implements Event
* This class defines a message type for when the Registry send routing tables to all the MessagingNode's in the network overlay.
* This request is sent out to every registered MessagingNode

#### Protocol.java
* This class defines a method that returns the string name of a message type integer. Mainly used for testing.

#### Register.java
* Implements Event
* This class defines a message type for when a MessagingNode want to register with the Registry

#### RegisterSuccess.java
* Implements Event
* This class defines a message type for when the Registry reports back to the MessagingNode about its registration request.
* Status field assigned node id on success, and -1 on fail.

#### SendData.java
* Implements Event
* This class is defines a message type for when MessagingNodes route data throughout the overlay.

#### SuccessManifest.java
* Implements Event
* This class defines a message type for when the MessagingNode responds to the Registry about whether or not it was successful connecting
  to all its neighbors defined in the NodeManifest.
* Status field assigned node id on success, and -1 on fail.

#### TaskFinished.java
* Implements Event
* This class defines a message type for when a MessagingNode reports back to the Registry that is has finished sending all the messages throughout the network overlay.

#### TaskInitiate.java
* Implements Event
* This class defines a message type for when the Registry is ready for the MessagingNodes to send data throughout the network overlay.
* This class contains a field to let the MessagingNode's know how many messages each one of them should send.
* This request is sent out to every registered MessagingNode

#### TrafficSummary.java
* Implements Event
* This class defines a message type for when the MessagingNode reports back the Registry about all the stats it collected while sending data throughout the network overlay

#### TrafficSummaryRequest.java
* Implements Event
* This class defines a message type for when the Registry wants to receive all the stats collected within the network overlay
* This request is sent out to every registered MessagingNode
