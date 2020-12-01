package cs455.overlay.transport;

// Java imports
import java.io.IOException;
import java.net.Socket;
import java.io.DataOutputStream;

public class TCPSender
{
    private Socket socket;
    private DataOutputStream dataOutputStream;

    public TCPSender(Socket socket) throws IOException
    {
        this.socket = socket;
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void sendData(byte[] dataToSend) throws IOException
    {
        synchronized (this.socket) {
            int dataLength = dataToSend.length;
            dataOutputStream.writeInt(dataLength);
            dataOutputStream.write(dataToSend, 0, dataLength);
            dataOutputStream.flush();
        }
    }

}
