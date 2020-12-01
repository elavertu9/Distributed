package cs455.overlay.transport;

// Java imports
import cs455.overlay.wireformats.Event;
import cs455.overlay.wireformats.EventFactory;
import java.io.IOException;
import java.net.Socket;
import java.io.DataInputStream;
import java.net.SocketException;

// Custom imports
import cs455.overlay.node.*;

public class TCPReceiverThread implements Runnable
{
    private EventFactory eventFactory = EventFactory.getEventFactory();
    private Socket socket;
    private DataInputStream dataInputStream;
    private Node node;

    public TCPReceiverThread(Socket socket, Node node) throws IOException
    {
        this.socket = socket;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.node = node;
    }

    @Override
    public synchronized void run()
    {
        int dataLength;
        while(this.socket != null) {
            try {
                //logger.info("Receiving Message...");
                dataLength = this.dataInputStream.readInt();
                byte[] data = new byte[dataLength];
                this.dataInputStream.readFully(data, 0, dataLength);
                Event newEvent = eventFactory.createEvent(data);
                node.onEvent(newEvent, this.socket);
            }catch(SocketException socketException) {
                System.out.println(socketException.getMessage());
                break;
            }catch(IOException ioException) {
                System.out.println(ioException.getMessage());
                break;
            }
        }
    }
}
