package cs455.overlay.node;

// Java imports
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

// Custom imports;
import cs455.overlay.util.*;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.routing.*;

public class MessagingNode implements Node
{
    private int registryConnectionPort;
    private Protocol protocol;
    private String ip;
    private TCPConnection connectionToRegistry;
    private int identifier;
    private TCPServerThread serverThread;
    private int serverPort;
    private ArrayList<RoutingEntry> subTable;
    private ArrayList<WrapperNode> connectedNodes;
    private StatCollector statCollector;
    private Random random;
    private int[] nodeIdentifiers;
    private TCPConnectionsCache connectionsCache;

    public MessagingNode()
    {
        this.protocol = new Protocol();
        this.subTable = new ArrayList<>();
        this.connectedNodes = new ArrayList<>();
        this.statCollector = new StatCollector();
        this.random = new Random();
        this.connectionsCache = new TCPConnectionsCache();
    }

    public static void init(String[] args)
    {
        if (args.length == 2) {
            try {
                MessagingNode messagingNode = new MessagingNode();
                messagingNode.connectToRegistry(args[0], Integer.parseInt(args[1]));
                messagingNode.startReceiverThread();
                messagingNode.sendRegistration();

                MessagingNodeCommandParser commandParser = new MessagingNodeCommandParser(messagingNode);
                commandParser.listen();
            }catch(Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            System.out.println("Usage: java cs455.overlay.node.MessagingNode registry-host registry-port");
            System.exit(0);
        }
    }

    /* =============================================================================================================================
       =                                                      CLASS METHODS                                                        =
       =============================================================================================================================
     */
    private void connectToRegistry(String registryIP, int registryPort)
    {
        try {
            Socket registryConnection = new Socket(registryIP, registryPort);
            setConnectionToRegistry(new TCPConnection(registryConnection, this));
            setIp(InetAddress.getLocalHost().getCanonicalHostName());
            setRegistryConnectionPort(registryConnection.getLocalPort());

            Thread registryReceiverThread = new Thread(new TCPReceiverThread(registryConnection, this));
            registryReceiverThread.start();
        }catch(IOException e){
             e.printStackTrace();
             System.exit(0);
         }
    }

    private void startReceiverThread()
    {
        try {
            this.serverThread = new TCPServerThread(this);
            setServerPort(getServerThread().getTCPServerSocket().getLocalPort());
            (new Thread(this.serverThread)).start();
        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    private void sendRegistration()
    {
        try {
            Register message = new Register(getIp(), getServerPort());
            byte[] sendBytes = message.getBytes();
            getRegistryTCPSender().sendData(sendBytes);
        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void onEvent(Event event, Socket socket)
    {
        //logger.info("Attempting to identify event...");
        switch(event.getType()) {
            case 3:
                readSuccessfulRegistrationResponse((RegisterSuccess) event);
                break;
            case 5:
                readSuccessfulDeregistration((DeregisterSuccess) event);
                break;
            case 6:
                readRegistrySendsNodeManifest((NodeManifest) event);
                break;
            case 8:
                readTaskInitiate((TaskInitiate) event);
                break;
            case 9:
                readSendData((SendData) event, socket);
                break;
            case 11:
                sendTrafficSummary();
        }
    }

    private void readSuccessfulRegistrationResponse(RegisterSuccess registerSuccess)
    {
        if(registerSuccess.getStatus() == -1) {
            System.out.println("Registration unsuccessful.");
        } else {
            setIdentifier(registerSuccess.getStatus());
            System.out.println("Registration successful.");
        }
    }

    private void readSuccessfulDeregistration(DeregisterSuccess deregisterSuccess)
    {
        if(deregisterSuccess.getStatus() == -1) {
            System.out.println("Deregistration unsuccessful.");
        } else {
            System.out.println("Deregistration successful.");
        }
    }

    private void readRegistrySendsNodeManifest(NodeManifest nodeManifest)
    {
        for(RoutingEntry entry : nodeManifest.getNeighbors()) {
            subTable.add(entry);
        }
        connectToNeighbors();
        setNodeIdentifiers(nodeManifest.getNodeIdentifiers());
        sendSuccessManifest();
    }

    private void connectToNeighbors()
    {
        for(RoutingEntry entry : getSubTable()) {
            WrapperNode node = createConnection(entry.getIp(), entry.getPort());
            if(node != null) {
                node.setIdentifier(entry.getIdentifier());
                addConnectedNode(node);
            } else {
                sendUnsuccessfulManifest();
                break;
            }
        }
    }

    private synchronized void addConnectedNode(WrapperNode node)
    {
        try {
            getConnectedNodes().add(node);
            TCPConnection connection = new TCPConnection(node.getConnectionSocket(), this);
            getConnectionsCache().addConnection(connection);
        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private WrapperNode createConnection(String ip, int port)
    {
        try {
            Socket neighborConnection = new Socket(ip, port);
            Thread receiverThread = new Thread(new TCPReceiverThread(neighborConnection, this));
            receiverThread.start();
            return new WrapperNode(ip, port, neighborConnection);
        }catch(IOException e) {
            return null;
        }
    }

    private void sendSuccessManifest()
    {
        try {
            Integer id = getIdentifier();
            byte status = id.byteValue();
            String info = "Successfully connected to " + getConnectedNodes().size() + " neighbors. Overlay setup successful.";
            SuccessManifest successManifest = new SuccessManifest(status, info);
            getRegistryTCPSender().sendData(successManifest.getBytes());
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void sendUnsuccessfulManifest()
    {
        try {
            Integer id = -1;
            byte status = id.byteValue();
            String info = "Unsuccessful connected to at least one neighbor. Overlay setup unsuccessful.";
            SuccessManifest successManifest = new SuccessManifest(status, info);
            getRegistryTCPSender().sendData(successManifest.getBytes());
        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void readTaskInitiate(TaskInitiate taskInitiate)
    {
        int randomDestIndex;

        for(int i = 0; i < taskInitiate.getNumMessages(); i++) {
            randomDestIndex = getRandomIndex();
            SendData sendData = new SendData(getNodeIdentifiers()[randomDestIndex], getIdentifier(), getRandom().nextInt(),  new int[getNodeIdentifiers().length]);
            if(subTableContainsIdentifier(sendData.getDestId())) {
                // If destination already exists in sub table then go directly
                try {
                    //logger.info("SPAWN: I have the destination in my table, sending data... " + sendData.getDestId());
                    getStatCollector().incrementSendTracker();
                    getStatCollector().addSendSum(sendData.getPayload());
                    getTCPSender(sendData.getDestId()).sendData(sendData.getBytes());
                }catch(IOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            } else {
                // destination not in sub table
                try {
                    //logger.info("SPAWN: Destination not in my table, forwarding to new node... " + sendData.getDestId());
                    getStatCollector().incrementSendTracker();
                    getStatCollector().addSendSum(sendData.getPayload());
                    getTCPSender(getNextIdentifier(sendData)).sendData(sendData.getBytes());
                }catch(IOException e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        }

        try {
            Thread.sleep(5000);
            sendTaskFinished();
        }catch(InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private int getRandomIndex()
    {
        int randomIndex = 0;
        while(true) {
            randomIndex = getRandom().nextInt(getNodeIdentifiers().length);
            if(getNodeIdentifiers()[randomIndex] != getIdentifier()) {
                break;
            }
        }
        return randomIndex;
    }

    private int getNextIdentifier(SendData sendData)
    {
        int returned = -1;
        if(getIdentifier() > sendData.getDestId())
        {
            // Wrap Around
            // Find Closest
            int indexOfDestination = findDestinationNode(sendData.getDestId());
            int check = indexOfDestination - 1;
            while(check >= 0) {
                if(subTableContainsIdentifier(getNodeIdentifiers()[check])) {
                    returned = getNodeIdentifiers()[check];
                    break;
                } else {
                    check--;
                }
            }
            if(check == -1) {
                int check2 = getNodeIdentifiers().length - 1;
                while(check2 >= 0) {
                    if(subTableContainsIdentifier(getNodeIdentifiers()[check2])) {
                        returned = getNodeIdentifiers()[check2];
                        break;
                    } else {
                        check2--;
                    }
                }
            }
        } else {
            // No Wrap Around
            // Find largest not exceeding destination
            for(int i = 0; i < getConnectedNodes().size(); i++) {
                WrapperNode node = getConnectedNodes().get(i);
                if((node.getIdentifier() > returned) && (node.getIdentifier() < sendData.getDestId())) {
                    returned = node.getIdentifier();
                }
            }
        }
        return returned;
    }

    private int findDestinationNode(int dest)
    {
        int returned = -1;
        for(int i = 0; i < getNodeIdentifiers().length; i++) {
            if(getNodeIdentifiers()[i] == dest) {
                returned =  i;
                break;
            }
        }
        return returned;
    }

    private void readSendData(SendData sendData, Socket socket)
    {
        sendData.incrementHops();
        int[] newTraversedIds = new int[sendData.getHops()];
        newTraversedIds[newTraversedIds.length - 1] = getIdentifier();
        sendData.setTraversedIds(newTraversedIds);
        if(sendData.getDestId() == getIdentifier()) {
            // If you are the destination
            //logger.info("READ: I am the destination id! " + getIdentifier());
            getStatCollector().incrementReceiveTracker();
            getStatCollector().addReceiveSum(sendData.getPayload());
        } else if(subTableContainsIdentifier(sendData.getDestId())) {
            // If you can directly access destination
            try {
                //logger.info("READ: I am not the destination but I know who is! Sending data... " + sendData.getDestId());
                getStatCollector().incrementRelayTracker();
                getTCPSender(sendData.getDestId()).sendData(sendData.getBytes());
            }catch(IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        } else {
            // if not in subTable, send to > ID that does not exceed destId
            try {
                //logger.info("READ: Destination not in my table, forwarding data... " + sendData.getDestId());
                getStatCollector().incrementRelayTracker();
                getTCPSender(getNextIdentifier(sendData)).sendData(sendData.getBytes());
            }catch(IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    private void sendTaskFinished()
    {
        try {
            getRegistryTCPSender().sendData(new TaskFinished(getIp(), getServerPort(), getIdentifier()).getBytes());
        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void sendTrafficSummary()
    {
        try {
            StatCollector stats = getStatCollector();
            getRegistryTCPSender().sendData(new TrafficSummary(getIdentifier(), stats.getSendTracker(), stats.getRelayTracker(), stats.getSendSum(), stats.getReceiveTracker(), stats.getReceiveSum()).getBytes());
        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void printStats()
    {
        getStatCollector().displayStats(getIdentifier());
    }

    private boolean subTableContainsIdentifier(int id) {
        boolean contains = false;
        for (RoutingEntry entry : getSubTable()) {
            if (entry.getIdentifier() == id) {
                contains = true;
            }
        }
        return contains;
    }

    @Override
    public String toString() {
        return "MessagingNode with id " + getIdentifier() + ": " + getIp() + ":" + getRegistryConnectionPort();
    }

    public void sendDeregistration()
    {
        try {
            Deregister deregister = new Deregister(getIp(), getRegistryConnectionPort(), getIdentifier());
            getRegistryTCPSender().sendData(deregister.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /* =============================================================================================================================
       =                                                      GETTERS                                                              =
       =============================================================================================================================
     */
    public int getRegistryConnectionPort()
    {
        return this.registryConnectionPort;
    }
    public Protocol getProtocol()
    {
        return this.protocol;
    }
    public TCPServerThread getServerThread()
    {
        return this.serverThread;
    }
    public int[] getNodeIdentifiers() { return this.nodeIdentifiers; }
    public TCPConnection getConnectionToRegistry() { return this.connectionToRegistry; }
    public String getIp() { return this.ip; }
    public TCPSender getRegistryTCPSender() { return getConnectionToRegistry().getTcpSender(); }
    public int getIdentifier() { return this.identifier; }
    public int getServerPort() { return this.serverPort; }
    public synchronized ArrayList<RoutingEntry> getSubTable() { return this.subTable; }
    public synchronized ArrayList<WrapperNode> getConnectedNodes() { return this.connectedNodes; }
    public synchronized StatCollector getStatCollector() { return this.statCollector; }
    public Random getRandom() { return this.random; }
    public synchronized TCPSender getTCPSender(int id)
    {
        TCPSender returnedSender = null;
        for(WrapperNode node : getConnectedNodes()) {
            if(node.getIdentifier() == id) {
                returnedSender = getConnectionsCache().getConnection(node.getConnectionSocket()).getTcpSender();
            }
        }
        return returnedSender;
    }
    public synchronized TCPConnectionsCache getConnectionsCache() { return this.connectionsCache; }
    public synchronized TCPSender getTCPSender(Socket socket) { return getServerThread().getConnectionsCache().getConnection(socket).getTcpSender(); }


    /* =============================================================================================================================
       =                                                      SETTERS                                                              =
       =============================================================================================================================
     */
    public void setRegistryConnectionPort(int port) { this.registryConnectionPort = port; }
    public void setIdentifier(int identifier) { this.identifier = identifier; }
    public void setIp(String ip)
    {
        this.ip = ip;
    }
    public void setConnectionToRegistry(TCPConnection connection) { this.connectionToRegistry = connection; }
    public void setServerPort(int port) { this.serverPort = port; }
    public void setNodeIdentifiers(int[] nodeIdentifiers) { this.nodeIdentifiers = nodeIdentifiers; }

    public static void main(String[] args)
    {
        init(args);
    }
}
