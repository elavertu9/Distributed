package cs455.overlay.wireformats;

// Java imports
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TrafficSummary implements Event
{
    private int type;
    private int identifier;
    private int packetsSent;
    private int packetsRelayed;
    private long sumOfPacketDataSent;
    private int packetsReceived;
    private long sumOfPacketDataReceived;

    public TrafficSummary(int identifier, int packetsSent, int packetsRelayed, long sumOfPacketDataSent, int packetsReceived, long sumOfPacketDataReceived)
    {
        this.type = 12;
        this.identifier = identifier;
        this.packetsSent = packetsSent;
        this.packetsRelayed = packetsRelayed;
        this.sumOfPacketDataSent = sumOfPacketDataSent;
        this.packetsReceived = packetsReceived;
        this.sumOfPacketDataReceived = sumOfPacketDataReceived;
    }

    @Override
    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));
        dataOutputStream.writeInt(getType());
        dataOutputStream.writeInt(getIdentifier());
        dataOutputStream.writeInt(getPacketsSent());
        dataOutputStream.writeInt(getPacketsRelayed());
        dataOutputStream.writeLong(getSumOfPacketDataSent());
        dataOutputStream.writeInt(getPacketsReceived());
        dataOutputStream.writeLong(getSumOfPacketDataReceived());
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

    public int getIdentifier() { return this.identifier; }
    public int getPacketsSent() { return this.packetsSent; }
    public int getPacketsRelayed() { return this.packetsRelayed; }
    public long getSumOfPacketDataSent() { return this.sumOfPacketDataSent; }
    public int getPacketsReceived() { return this.packetsReceived; }
    public long getSumOfPacketDataReceived() { return this.sumOfPacketDataReceived; }
}
