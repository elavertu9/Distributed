package cs455.overlay.wireformats;

// Java imports
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

// Custom imports
import cs455.overlay.routing.RoutingEntry;

public class NodeManifest implements Event
{
    private int type;
    ArrayList<RoutingEntry> neighbors;
    int[] nodeIdentifiers;

    public NodeManifest(ArrayList<RoutingEntry> neighbors, int[] nodeIdentifiers)
    {
        this.type = 6;
        this.neighbors = neighbors;
        this.nodeIdentifiers = nodeIdentifiers;
    }

    @Override
    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));
        dataOutputStream.writeInt(getType());
        dataOutputStream.writeInt(getNeighbors().size());
        for(RoutingEntry entry : getNeighbors()) {
            dataOutputStream.writeInt(entry.getIdentifier());
            byte[] ip = entry.getIp().getBytes();
            dataOutputStream.writeInt(ip.length);
            dataOutputStream.write(ip);
            dataOutputStream.writeInt(entry.getPort());
        }
        dataOutputStream.writeInt(getNodeIdentifiers().length);
        for(int identifier : getNodeIdentifiers()) {
            dataOutputStream.writeInt(identifier);
        }
        dataOutputStream.flush();
        marshalledBytes = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        dataOutputStream.close();
        return marshalledBytes;
    }

    @Override
    public int getType()
    {
        return this.type;
    }
    public ArrayList<RoutingEntry> getNeighbors() { return this.neighbors; }
    public int[] getNodeIdentifiers() { return this.nodeIdentifiers; }
}
