package cs455.overlay.transport;

// Java imports
import java.io.IOException;
import java.net.*;

// Custom imports
import cs455.overlay.node.*;

public class TCPServerThread implements Runnable
{
    private ServerSocket serverSocket;
    private TCPConnectionsCache connectionsCache;
    private Node node;

    public TCPServerThread(Node node) throws IOException
    {
        this(0, node);
    }

    public TCPServerThread(int port, Node node) throws IOException
    {
        this.connectionsCache = new TCPConnectionsCache();
        this.serverSocket = new ServerSocket(port);
        this.node = node;
    }

    @Override
    public void run()
    {
        Socket socket;
        while(true) {
            try {
                //logger.info("Waiting for Connection");

                socket = getTCPServerSocket().accept();
                if(socket != null) {
                    TCPConnection tcpConnection = new TCPConnection(socket, this.node);
                    getConnectionsCache().addConnection(tcpConnection);
                    tcpConnection.startReceiverThread();
                }
            }catch(Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    /* =============================================================================================================================
       =                                                      GETTERS                                                              =
       =============================================================================================================================
     */
    public synchronized TCPConnectionsCache getConnectionsCache() {
        return this.connectionsCache;
    }
    public ServerSocket getTCPServerSocket()
    {
        return this.serverSocket;
    }
}
