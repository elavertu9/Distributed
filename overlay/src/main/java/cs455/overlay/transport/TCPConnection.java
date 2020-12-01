package cs455.overlay.transport;

// Java imports
import java.net.*;
import java.io.IOException;

// Custom imports
import cs455.overlay.node.*;

public class TCPConnection
{
    private TCPSender tcpSender;
    private Socket connectionSocket;
    private Node node;

    public TCPConnection(Socket socket, Node node) throws IOException
    {
        this.tcpSender = new TCPSender(socket);
        this.connectionSocket = socket;
        this.node = node;
    }

    public void startReceiverThread()
    {
        try {
            new Thread(new TCPReceiverThread(this.connectionSocket, this.node)).start();
        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /* =============================================================================================================================
       =                                                      GETTERS                                                              =
       =============================================================================================================================
     */
    public TCPSender getTcpSender()
    {
        return this.tcpSender;
    }
    public Socket getConnectionSocket()
    {
        return this.connectionSocket;
    }
    public Node getNode() { return this.node; }
}
