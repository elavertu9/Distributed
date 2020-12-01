package cs455.overlay.transport;

// Java imports
import java.util.ArrayList;
import java.net.*;

public class TCPConnectionsCache
{
    private ArrayList<TCPConnection> connectionsCache;

    public TCPConnectionsCache()
    {
        this.connectionsCache = new ArrayList<>();
    }

    public synchronized void addConnection(TCPConnection newTCPConnection)
    {
        getConnectionsCache().add(newTCPConnection);
    }

    public TCPConnection getConnection(Socket socket)
    {
        for(TCPConnection connection : getConnectionsCache()) {
            if(connection.getConnectionSocket() == socket) {
                return connection;
            }
        }
        return null;
    }

    public void printConnectionsCache()
    {
        for(TCPConnection connection : this.connectionsCache) {
            System.out.println(("Connection in Cache: " + connection.getConnectionSocket().toString()));
        }
    }

    /* =============================================================================================================================
       =                                                      GETTERS                                                              =
       =============================================================================================================================
     */
    public ArrayList<TCPConnection> getConnectionsCache() { return this.connectionsCache; }
}
