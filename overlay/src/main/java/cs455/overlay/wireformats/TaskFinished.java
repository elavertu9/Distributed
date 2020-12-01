package cs455.overlay.wireformats;

// Java imports
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TaskFinished implements Event {
    private int type;
    private String ip;
    private int port;
    private int identifier;

    public TaskFinished(String ip, int port, int identifier)
    {
        this.type = 10;
        this.ip = ip;
        this.port = port;
        this.identifier = identifier;
    }

    @Override
    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));
        dataOutputStream.writeInt(getType());
        dataOutputStream.writeInt(getIp().length());
        byte[] ipAddress = getIp().getBytes();
        dataOutputStream.write(ipAddress);
        dataOutputStream.writeInt(getPort());
        dataOutputStream.writeInt(getIdentifier());
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

    public String getIp()
    {
        return this.ip;
    }

    public int getPort()
    {
        return this.port;
    }

    public int getIdentifier()
    {
        return this.identifier;
    }
}
