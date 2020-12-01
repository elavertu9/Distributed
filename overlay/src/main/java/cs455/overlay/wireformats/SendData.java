package cs455.overlay.wireformats;

// Java imports
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SendData implements Event
{
    private int type;
    private int destId;
    private int srcId;
    int payload;
    int hops;
    int[] traversedIds;

    public SendData(int destId, int srcId, int payload, int[] traversedIds)
    {
        this.type = 9;
        this.destId = destId;
        this.srcId = srcId;
        this.payload = payload;
        this.traversedIds = traversedIds;
        this.hops = traversedIds.length;
    }

    @Override
    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));
        dataOutputStream.writeInt(getType());
        dataOutputStream.writeInt(getDestId());
        dataOutputStream.writeInt(getSrcId());
        dataOutputStream.writeInt(getPayload());
        dataOutputStream.writeInt(getHops());
        for(int id : getTraversedIds()) {
            dataOutputStream.writeInt(id);
        }
        dataOutputStream.flush();
        marshalledBytes = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        dataOutputStream.close();
        return marshalledBytes;
    }

    public void setTraversedIds(int[] traversedIds)
    {
        this.traversedIds = traversedIds;
    }

    @Override
    public int getType()
    {
        return this.type;
    }

    public int getDestId()
    {
        return this.destId;
    }

    public int getSrcId()
    {
        return this.srcId;
    }

    public int getPayload()
    {
        return this.payload;
    }

    public synchronized int getHops()
    {
        return this.hops;
    }

    public int[] getTraversedIds()
    {
        return this.traversedIds;
    }

    public synchronized void incrementHops()
    {
        this.hops++;
    }
}
