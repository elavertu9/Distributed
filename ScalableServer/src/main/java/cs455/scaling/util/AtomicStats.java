package cs455.scaling.util;

// Java imports
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicStats
{
    private AtomicInteger sent = new AtomicInteger(0);
    private AtomicInteger received = new AtomicInteger(0);

    public void incrementSent()
    {
        sent.incrementAndGet();
    }

    public void incrementReceived()
    {
        received.incrementAndGet();
    }

    public void resetStats()
    {
        sent.set(0);
        received.set(0);
    }

    public int getSent()
    {
        return sent.get();
    }

    public int getReceived()
    {
        return received.get();
    }
}
