package cs455.overlay.wireformats;

// Java imports
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Deregister implements Event
{
    private int type;
    private String senderIp;
    private int senderPort;
    private int senderIdentifier;

    public Deregister(String senderIp, int senderPort, int senderIdentifier)
    {
        this.type = 4;
        this.senderIp = senderIp;
        this.senderPort = senderPort;
        this.senderIdentifier = senderIdentifier;
    }

    @Override
    public byte[] getBytes() throws IOException {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));
        dataOutputStream.writeInt(getType());
        byte[] byteSenderHostName = getSenderIp().getBytes();
        int senderHostNameLength = byteSenderHostName.length;
        dataOutputStream.writeInt(senderHostNameLength);
        dataOutputStream.write(byteSenderHostName);
        dataOutputStream.writeInt(getSenderPort());
        dataOutputStream.writeInt(getSenderIdentifier());
        dataOutputStream.flush();
        marshalledBytes = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        dataOutputStream.close();
        return marshalledBytes;
    }

    @Override
    public int getType() {
        return this.type;
    }

    public String getSenderIp() { return this.senderIp; }
    public int getSenderPort() { return this.senderPort; }
    public int getSenderIdentifier() { return this.senderIdentifier; }
}
