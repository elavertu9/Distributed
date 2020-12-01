package cs455.overlay.wireformats;

// Java imports
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

// Custom imports
import cs455.overlay.routing.*;

// Singleton
public class EventFactory
{
    // Singleton Instance
    private static EventFactory eventFactory;

    private EventFactory() {}

    public static EventFactory getEventFactory()
    {
        if(eventFactory == null) {
            synchronized (EventFactory.class) {
                if(eventFactory == null) {
                    eventFactory = new EventFactory();
                }
            }
        }
        return eventFactory;
    }

    public Event createEvent(byte[] message) throws IOException
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message);
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(byteArrayInputStream));
        int type = dataInputStream.readInt();
        Event unmarshalledMessage = unmarshall(type, dataInputStream);
        byteArrayInputStream.close();
        dataInputStream.close();
        return unmarshalledMessage;
    }

    private Event unmarshall(int type, DataInputStream dataInputStream) throws IOException
    {
        switch(type)
        {
            case 2:
                return registerEvent(dataInputStream);
            case 3:
                return registerSuccessEvent(dataInputStream);
            case 4:
                return deregisterEvent(dataInputStream);
            case 5:
                return deregisterSuccessEvent(dataInputStream);
            case 6:
                return nodeManifestEvent(dataInputStream);
            case 7:
                return successManifestEvent(dataInputStream);
            case 8:
                return taskInitiateEvent(dataInputStream);
            case 9:
                return sendDataEvent(dataInputStream);
            case 10:
                return taskFinishedEvent(dataInputStream);
            case 11:
                return trafficSummaryRequestEvent();
            case 12:
                return trafficSummaryEvent(dataInputStream);
            default:
                return null;
        }
    }

    private Register registerEvent(DataInputStream dataInputStream) throws IOException
    {
        int ipLength = dataInputStream.readInt();
        byte[] ipByte = new byte[ipLength];
        dataInputStream.readFully(ipByte);
        String ip = new String(ipByte);
        int port = dataInputStream.readInt();
        return new Register(ip, port);
    }

    private RegisterSuccess registerSuccessEvent(DataInputStream dataInputStream) throws IOException
    {
        byte status = dataInputStream.readByte();
        int infoLength = dataInputStream.readInt();
        byte[] moreInfo = new byte[infoLength];
        dataInputStream.readFully(moreInfo);
        String info = new String(moreInfo);
        return new RegisterSuccess(status, info);
    }

    private Deregister deregisterEvent(DataInputStream dataInputStream) throws IOException
    {
        int ipLength = dataInputStream.readInt();
        byte[] ipByte = new byte[ipLength];
        dataInputStream.readFully(ipByte);
        String ip = new String(ipByte);
        int port = dataInputStream.readInt();
        int identifier = dataInputStream.readInt();
        return new Deregister(ip, port, identifier);
    }

    private DeregisterSuccess deregisterSuccessEvent(DataInputStream dataInputStream) throws IOException
    {
        byte status = dataInputStream.readByte();
        int infoLength = dataInputStream.readInt();
        byte[] moreInfo = new byte[infoLength];
        dataInputStream.readFully(moreInfo);
        String info = new String(moreInfo);
        return new DeregisterSuccess(status, info);
    }

    private NodeManifest nodeManifestEvent(DataInputStream dataInputStream) throws IOException
    {
        int numberOfNeighbors = dataInputStream.readInt();
        ArrayList<RoutingEntry> neighbors = new ArrayList<>();
        for(int i = 0; i < numberOfNeighbors; i++) {
            int neighborIdentifier = dataInputStream.readInt();
            int neighborIpLength = dataInputStream.readInt();
            byte[] neighborIpBytes = new byte[neighborIpLength];
            dataInputStream.readFully(neighborIpBytes);
            String neighborIp = new String(neighborIpBytes);
            int neighborPort = dataInputStream.readInt();
            RoutingEntry entry = new RoutingEntry(neighborIdentifier, neighborIp, neighborPort);
            neighbors.add(entry);
        }
        int numTotalNodes = dataInputStream.readInt();
        int[] nodeIdentifiers = new int[numTotalNodes];
        for(int i = 0; i < numTotalNodes; i++) {
            int identifier = dataInputStream.readInt();
            nodeIdentifiers[i] = identifier;
        }
        return new NodeManifest(neighbors, nodeIdentifiers);
    }

    private SuccessManifest successManifestEvent(DataInputStream dataInputStream) throws IOException
    {
        byte status = dataInputStream.readByte();
        int infoLength = dataInputStream.readInt();
        byte[] moreInfo = new byte[infoLength];
        dataInputStream.readFully(moreInfo);
        String info = new String(moreInfo);
        return new SuccessManifest(status, info);
    }

    private TaskInitiate taskInitiateEvent(DataInputStream dataInputStream) throws IOException
    {
        int numMessages = dataInputStream.readInt();
        return new TaskInitiate(numMessages);
    }

    private SendData sendDataEvent(DataInputStream dataInputStream) throws IOException
    {
        int destId = dataInputStream.readInt();
        int srcId = dataInputStream.readInt();
        int payload = dataInputStream.readInt();
        int hops = dataInputStream.readInt();
        int[] traversedIds = new int[hops];
        for(int i = 0; i < hops; i++) {
            traversedIds[i] = dataInputStream.readInt();
        }
        return new SendData(destId, srcId, payload, traversedIds);
    }

    private TaskFinished taskFinishedEvent(DataInputStream dataInputStream) throws IOException
    {
        int ipLength = dataInputStream.readInt();
        byte[] ipBytes = new byte[ipLength];
        dataInputStream.readFully(ipBytes);
        String ip = new String(ipBytes);
        int port = dataInputStream.readInt();
        int identifier = dataInputStream.readInt();
        return new TaskFinished(ip, port, identifier);
    }

    private TrafficSummaryRequest trafficSummaryRequestEvent()
    {
        return new TrafficSummaryRequest();
    }

    private TrafficSummary trafficSummaryEvent(DataInputStream dataInputStream) throws IOException
    {
        int identifier = dataInputStream.readInt();
        int packetsSent = dataInputStream.readInt();
        int packetsRelayed = dataInputStream.readInt();
        long sumOfPacketDataSent = dataInputStream.readLong();
        int packetsReceived = dataInputStream.readInt();
        long sumOfPacketDataReceived = dataInputStream.readLong();
        return new TrafficSummary(identifier, packetsSent, packetsRelayed, sumOfPacketDataSent, packetsReceived, sumOfPacketDataReceived);
    }
}
