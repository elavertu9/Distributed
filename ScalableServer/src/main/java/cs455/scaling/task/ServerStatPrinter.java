package cs455.scaling.task;

// Java imports
import java.util.Timer;
import java.util.TimerTask;

// Custom imports
import cs455.scaling.Server.Server;

public class ServerStatPrinter implements Runnable
{
    private Server server;

    public ServerStatPrinter(Server server)
    {
        this.server = server;
    }

    @Override
    public void run()
    {
        Timer timer = new Timer();
        ServerTask serverTask = new ServerTask(server);
        timer.scheduleAtFixedRate(serverTask, 0L, 20000L);
    }
}

class ServerTask extends TimerTask
{
    private Server server;

    public ServerTask(Server server)
    {
        this.server = server;
    }

    @Override
    public void run()
    {
        System.out.print("\n\n[" + System.currentTimeMillis() + "] Server Throughput: " + server.getTotalProcessed()  + " messages, Active Client Connections: " + server.getNumClients() + ", \nMean Per-client Throughput: " + server.getMean() + " messages, Std. Dev. Of Per-client Throughput: " + server.getStdDev() + " messages\n");
        server.clearClientStats();
    }
}
