package cs455.scaling.task;

// Java imports
import java.util.Timer;
import java.util.TimerTask;

// Custom imports
import cs455.scaling.Client.Client;

public class ClientStatPrinter implements Runnable
{
    private Client client;

    public ClientStatPrinter(Client client)
    {
        this.client = client;
    }

    @Override
    public void run()
    {
        Timer timer = new Timer();
        PrintTask printTask = new PrintTask(client);
        timer.scheduleAtFixedRate(printTask, 0L, 20000L);
    }
}

class PrintTask extends TimerTask
{
    private Client client;

    public PrintTask(Client client)
    {
        this.client = client;
    }

    @Override
    public void run()
    {
        System.out.print("[" + System.currentTimeMillis() + "] Total Sent Count: " + client.getSentCount() + ", Total Received Count: " + client.getReceivedCount() + "\n");
        client.resetStats();
    }
}
