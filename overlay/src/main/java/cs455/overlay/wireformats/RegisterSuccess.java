package cs455.overlay.wireformats;

// Java imports
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterSuccess implements Event
{
    private int type;
    private byte status;
    private String info;

    public RegisterSuccess(byte status, String info)
    {
        this.type = 3;
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
    public int getType()
    {
        return this.type;
    }

    // GETTERS
    public byte getStatus()
    {
        return this.status;
    }
    public String getInfo()
    {
        return this.info;
    }
}
