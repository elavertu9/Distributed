package cs455.overlay.node;

// Java imports
import java.net.*;

public class WrapperNode
{
    private String ip;
    private int port;
    private Socket connectionSocket;
    private int identifier;

    public WrapperNode(String ip, int port, Socket connectionSocket)
    {
        this.connectionSocket = connectionSocket;
        this.ip = ip;
        this.port = port;
    }

    /* =============================================================================================================================
       =                                                      CLASS METHODS                                                        =
       =============================================================================================================================
     */
    @Override
    public String toString()
    {
        return "MessagingNode with id " + getIdentifier() + ": " + getIp() + ":" + getPort();
    }

    /* =============================================================================================================================
       =                                                      GETTERS                                                              =
       =============================================================================================================================
     */
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
    public Socket getConnectionSocket() { return this.connectionSocket; }

    /* =============================================================================================================================
       =                                                      SETTERS                                                              =
       =============================================================================================================================
     */
    public void setIdentifier(int identifier) { this.identifier = identifier; }
}
