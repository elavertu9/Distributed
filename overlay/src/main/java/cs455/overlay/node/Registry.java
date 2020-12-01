package cs455.overlay.node;

// Java imports
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

// Custom imports
import cs455.overlay.util.*;
import cs455.overlay.transport.*;
import cs455.overlay.wireformats.*;
import cs455.overlay.routing.*;

// Singleton
public class Registry implements Node
{
    private ArrayList<WrapperNode> registeredNodes;
    private Protocol protocol;
    private int port;
    private ArrayList<Integer> validIdentifiers;
    private TCPServerThread serverThread;
    private RoutingTable masterRoutingTable;
    private ArrayList<Integer> finishedNodes;
    private ArrayList<TrafficSummary> totalStats;
    private boolean statsPrinted = false;

    public Registry(int port)
    {
        this.registeredNodes = new ArrayList<>();
        this.protocol = new Protocol();
        this.port = port;
        this.validIdentifiers = populateIdentifiers();
        this.masterRoutingTable = new RoutingTable();
        this.finishedNodes = new ArrayList<>();
        this.totalStats = new ArrayList<>();
    }

    public static void init(String[] args)
    {
        if (args.length == 1) {
            try {
                Registry registry = new Registry(Integer.parseInt(args[0]));
                registry.startReceiverThread();

                RegistryCommandParser commandParser = new RegistryCommandParser(registry);
                commandParser.listen();
            }catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Usage: java cs455.overlay.node.Registry portnum");
            System.exit(0);
        }
    }

    /* =============================================================================================================================
       =                                                      CLASS METHODS                                                        =
       =============================================================================================================================
     */
    private ArrayList<Integer> populateIdentifiers()
    {
        ArrayList<Integer> identifiers = new ArrayList<>();
        for(int i = 0; i < 128; i++) {
            identifiers.add(i);
        }
        Collections.shuffle(identifiers);
        return identifiers;
    }

    private void startReceiverThread()
    {
        try {
            this.serverThread = new TCPServerThread(getPort(), this);
            (new Thread(this.serverThread)).start();
        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void onEvent(Event event, Socket socket)
    {
        switch (event.getType())
        {
            case 2:
                registerNode((Register) event, socket);
                break;
            case 4:
                deregisterNode((Deregister) event, socket);
                break;
            case 7:
                readSuccessManifest((SuccessManifest) event, socket);
                break;
            case 10:
                readTaskFinished((TaskFinished) event, socket);
                break;
            case 12:
                readTrafficSummaryResponse((TrafficSummary) event, socket);
                break;
        }
    }

    private synchronized void registerNode(Register node, Socket socket)
    {
        WrapperNode wrapperNode = new WrapperNode(node.getSenderIp(), node.getSenderPort(), socket);
        if (isValidRegistration(node, socket)) {
            addRegisteredNode(wrapperNode);
            sendSuccessfulRegistration(wrapperNode, socket);
        } else {
            sendUnsuccessfulRegistration(wrapperNode, socket);
        }
    }

    private boolean isValidRegistration(Register node, Socket socket)
    {
    	// The senders ip matches the ip in the message
        if ((socket.getInetAddress().getCanonicalHostName().equals(node.getSenderIp())) && (!nodeAlreadyRegistered(node.getSenderIp(), node.getSenderPort()))) {
        	return true;
        } else {
            return false;
        }
    }

    private boolean nodeAlreadyRegistered(String ip, int port)
    {
        for(WrapperNode node : this.registeredNodes) {
            if (node.getIp().equals(ip) && node.getPort() == port) {
                return true;
            }
        }
        return false;
    }

    private synchronized void addRegisteredNode(WrapperNode node)
    {
        node.setIdentifier(getUniqueIdentifier());
        registeredNodes.add(node);
    }

    private void sendSuccessfulRegistration(WrapperNode node, Socket socket)
    {
        try {
            Integer identifier = node.getIdentifier();
            byte status = identifier.byteValue();
            String info = "Registration successful. The number of messaging nodes currently constituting the overlay is " + getRegisteredNodes().size();
            RegisterSuccess registerSuccess = new RegisterSuccess(status, info);
            getTCPSender(socket).sendData(registerSuccess.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void sendUnsuccessfulRegistration(WrapperNode node, Socket socket)
    {
        try {
            Integer identifier = -1;
            byte status = identifier.byteValue();
            String info = "Registration unsuccessful. The number of messaging nodes currently constituting the overlay is unchanged " + getRegisteredNodes().size();
            RegisterSuccess registerSuccess = new RegisterSuccess(status, info);
            getTCPSender(socket).sendData(registerSuccess.getBytes());
        }catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private synchronized void deregisterNode(Deregister node, Socket socket)
    {
        WrapperNode wrapperNode = new WrapperNode(node.getSenderIp(), node.getSenderPort(), socket);
        wrapperNode.setIdentifier(node.getSenderIdentifier());
        if(isValidDeregistration(node, socket)) {
            removeRegisteredNode(node.getSenderIp(), node.getSenderPort());
            sendSuccessfulDeregistration(wrapperNode, socket);
        } else {
            sendUnsuccessfulDeregistration(wrapperNode, socket);
        }
    }

    private boolean isValidDeregistration(Deregister node, Socket socket)
    {
        // If the ip sent matches the ip of the sender and it is already in the overlay
        if((node.getSenderIp().equals(socket.getInetAddress().getHostName())) && (nodeAlreadyRegistered(node.getSenderIp(), node.getSenderPort()))) {
            return true;
        } else {
            return false;
        }
    }

    private synchronized void removeRegisteredNode(String ip, int port)
    {
        for(int i = 0; i < getRegisteredNodes().size(); i++) {
            if((getRegisteredNodes().get(i).getIp().equals(ip)) && (getRegisteredNodes().get(i).getPort() == port)) {
                getRegisteredNodes().remove(i);
                break;
            }
        }
    }

    private void sendSuccessfulDeregistration(WrapperNode node, Socket socket)
    {
        try {
            Integer identifier = node.getIdentifier();
            byte status = identifier.byteValue();
            String info = "Deregistration successful. The number of messaging nodes currently constituting the overlay is " + getRegisteredNodes().size();
            DeregisterSuccess deregisterSuccess = new DeregisterSuccess(status, info);
            getTCPSender(socket).sendData(deregisterSuccess.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void sendUnsuccessfulDeregistration(WrapperNode node, Socket socket)
    {
        try {
            Integer identifier = -1;
            byte status = identifier.byteValue();
            String info = "Deregistration unsuccessful. The number of messaging nodes currently constituting the overlay is unchanged " + getRegisteredNodes().size();
            DeregisterSuccess deregisterSuccess = new DeregisterSuccess(status, info);
            getTCPSender(socket).sendData(deregisterSuccess.getBytes());
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private Integer getUniqueIdentifier()
    {
        return getValidIdentifiers().remove(0);
    }

    public void listMessagingNodes()
    {
        if (getRegisteredNodes().size() == 0) {
            System.out.println("There are currently no MessagingNodes in the overlay...");
        } else {
            for (WrapperNode node : getRegisteredNodes()) {
                System.out.println(node.toString());
            }
        }
    }

    public void setupOverlay(int N)
    {
        if(isValidOverlayRequest(N)) {
            sortRegisteredNodes();
            createOverlay(N);
            sendNodeManifest();
        } else {
            System.out.println("Not a valid setup-overlay request...");
        }
    }

    private boolean isValidOverlayRequest(int N)
    {
        return N < ((int) Math.pow(2, N - 1) + 1) && getRegisteredNodes().size() >= (Math.pow(2, N-1) + 1);
    }

    private void sortRegisteredNodes()
    {
        Collections.sort(getRegisteredNodes(), new WrapperNodeComparator());
    }

    private void createOverlay(int N)
    {
        int[] hops = getHops(N);
        int size = getRegisteredNodes().size();

        for(int i = 0; i < size; i++) {
            ArrayList<RoutingEntry> subTable = new ArrayList<>();
            for(int j = 0; j < N; j++) {
                WrapperNode node = getRegisteredNodes().get((i + hops[j]) % size);
                subTable.add(new RoutingEntry(node.getIdentifier(), node.getIp(), node.getPort()));
            }
            getMasterRoutingTable().addEntry(getRegisteredNodes().get(i).getIdentifier(), subTable);
        }
    }

    private int[] getHops(int N)
    {
        int[] hops = new int[N];
        for(int i = 0; i < N; i++) {
            hops[i] = getNextNeighborIndex(i);
        }
        return hops;
    }

    private int getNextNeighborIndex(int i)
    {
        return (int) Math.pow(2, ((i + 1) - 1));
    }

    private void sendNodeManifest()
    {
        try {
            int[] nodeIdentifiers = getIdentifiers();
            for(Map.Entry<Integer, ArrayList<RoutingEntry>> entry : getMasterRoutingTable().getRoutingTable().entrySet()) {
                WrapperNode node = getRegisteredNode(entry.getKey());
                NodeManifest manifest = new NodeManifest(entry.getValue(), nodeIdentifiers);
                getTCPSender(node.getConnectionSocket()).sendData(manifest.getBytes());
            }
        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void readSuccessManifest(SuccessManifest event, Socket socket)
    {
        System.out.println(event.getStatus() + ": " + event.getInfo());
    }

    public void listRoutingTables()
    {
        if(getMasterRoutingTable().getRoutingTable().size() > 0) {
            System.out.println(getMasterRoutingTable().toString());
        } else {
            System.out.println("Routing tables have not yet been established...");
        }
    }

    public void sendTaskInitiate(int numMessages)
    {
        try {
            for(WrapperNode node : getRegisteredNodes()) {
                getTCPSender(node.getConnectionSocket()).sendData(new TaskInitiate(numMessages).getBytes());
            }
        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void readTaskFinished(TaskFinished taskFinished, Socket socket)
    {
        getFinishedNodes().add(taskFinished.getIdentifier());
        if(getFinishedNodes().size() == getRegisteredNodes().size()) {
            sendTrafficSummaryRequest();
        }
    }

    private void sendTrafficSummaryRequest()
    {
        try {
            for(WrapperNode node : getRegisteredNodes()) {
                getTCPSender(node.getConnectionSocket()).sendData(new TrafficSummaryRequest().getBytes());
            }
        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void readTrafficSummaryResponse(TrafficSummary trafficSummary, Socket socket)
    {
    	addTotalStats(trafficSummary);
    	checkForAllTrafficResponses();
    }
    
    private synchronized void addTotalStats(TrafficSummary summary)
    {
    	if(!statsAlreadyAdded(summary)) {
        	this.totalStats.add(summary);
    	}
    }
    
    private synchronized boolean statsAlreadyAdded(TrafficSummary summary)
    {
    	for(TrafficSummary check : this.totalStats) {
    		if(check.getIdentifier() == summary.getIdentifier()) {
    			return true;
    		}
    	}
    	return false;
    }
    
    private synchronized void checkForAllTrafficResponses()
    {
    	if(this.totalStats.size() == getRegisteredNodes().size()) {
    		printTotalStats();
    	}
    }
    
    private void printTotalStats()
    {
    	if(!this.statsPrinted) {        	
        	System.out.printf("%19s", "Node ID");
        	System.out.printf("%20s", "Packets Sent");
            System.out.printf("%20s", "Packets Received");
            System.out.printf("%20s", "Packets Relayed");
            System.out.printf("%22s", "Sum Values Sent");
            System.out.printf("%22s%n", "Sum Values Received");
            
            int totalPacketsSent = 0;
            int totalPacketsReceived = 0;
            int totalPacketsRelayed = 0;
            long totalSumSent = 0;
            long totalSumReceived = 0;
            
            for(TrafficSummary summary : this.totalStats) {
            	System.out.printf("%19s", summary.getIdentifier());
            	System.out.printf("%20s", summary.getPacketsSent());
            	totalPacketsSent += summary.getPacketsSent();
                System.out.printf("%20s", summary.getPacketsReceived());
                totalPacketsReceived += summary.getPacketsReceived();
                System.out.printf("%20s", summary.getPacketsRelayed());
                totalPacketsRelayed += summary.getPacketsRelayed();
                System.out.printf("%22s", summary.getSumOfPacketDataSent());
                totalSumSent += summary.getSumOfPacketDataSent();
                System.out.printf("%22s%n", summary.getSumOfPacketDataReceived());
                totalSumReceived += summary.getSumOfPacketDataReceived();
            }
            System.out.printf("%19s", "Sum");
            System.out.printf("%20s", totalPacketsSent);
            System.out.printf("%20s", totalPacketsReceived);
            System.out.printf("%20s", totalPacketsRelayed);
            System.out.printf("%22s", totalSumSent);
            System.out.printf("%22s%n", totalSumReceived);
            this.statsPrinted = true;
    	}
    	
    }

    private WrapperNode getRegisteredNode(int identifier)
    {
        WrapperNode returnNode = null;
        for(WrapperNode node : getRegisteredNodes()) {
            if(node.getIdentifier() == identifier) {
                returnNode = node;
            }
        }
        return returnNode;
    }

    private int[] getIdentifiers()
    {
        int[] nodeIdentifiers = new int[getRegisteredNodes().size()];
        for(int i = 0; i < getRegisteredNodes().size(); i++) {
            nodeIdentifiers[i] = getRegisteredNodes().get(i).getIdentifier();
        }
        return nodeIdentifiers;
    }

    /* =============================================================================================================================
       =                                                      GETTERS                                                              =
       =============================================================================================================================
     */
    public Protocol getProtocol() {
        return this.protocol;
    }
    public int getPort() { return this.port; }
    public TCPSender getTCPSender(Socket socket) { return getServerThread().getConnectionsCache().getConnection(socket).getTcpSender(); }
    public TCPServerThread getServerThread() { return this.serverThread; }
    public ArrayList<Integer> getValidIdentifiers() { return this.validIdentifiers; }
    public ArrayList<WrapperNode> getRegisteredNodes() { return this.registeredNodes; }
    public RoutingTable getMasterRoutingTable() { return this.masterRoutingTable; }
    private ArrayList<Integer> getFinishedNodes() { return this.finishedNodes; }

    /* =============================================================================================================================
       =                                                      SETTERS                                                              =
       =============================================================================================================================
     */
    public static void main(String[] args)
    {
        init(args);
    }
}
