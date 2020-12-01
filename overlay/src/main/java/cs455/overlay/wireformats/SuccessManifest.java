package cs455.overlay.wireformats;

// Java imports
import java.io.*;

public class SuccessManifest implements Event
{
    private int type;
    private byte status;
    private String info;

    public SuccessManifest(byte status, String info)
    {
        this.type = 7;
        this.status = status;
        this.info = info;
    }

    @Override
    public byte[] getBytes() throws IOException
    {
        byte[] marshalledBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(byteArrayOutputStream));
        dataOutputStream.writeInt(getType());
        dataOutputStream.writeByte(getStatus());
        byte[] moreInfo = getInfo().getBytes();
        int moreInfoLength = moreInfo.length;
        dataOutputStream.writeInt(moreInfoLength);
        dataOutputStream.write(moreInfo);
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

    public byte getStatus()
    {
        return this.status;
    }

    public String getInfo()
    {
        return this.info;
    }
}
