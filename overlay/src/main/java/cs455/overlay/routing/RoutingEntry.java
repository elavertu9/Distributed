package cs455.overlay.routing;

public class RoutingEntry
{
    private int identifier;
    private String ip;
    private int port;
    private boolean isFinished;

    public RoutingEntry(int identifier, String ip, int port)
    {
        this.identifier = identifier;
        this.ip = ip;
        this.port = port;
        this.isFinished = false;
    }

    @Override
    public String toString()
    {
        return "ID: " + getIdentifier() + " - Address: " + getIp() + ":" + getPort();
    }

    /* =============================================================================================================================
       =                                                      GETTERS                                                              =
       =============================================================================================================================
     */
    public int getIdentifier() { return this.identifier; }
    public String getIp() { return this.ip; }
    public int getPort() { return this.port; }
    public boolean getIsFinished() { return this.isFinished; }
}
