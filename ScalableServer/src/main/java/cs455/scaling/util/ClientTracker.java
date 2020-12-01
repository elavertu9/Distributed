package cs455.scaling.util;

// Java imports
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.Math;

public class ClientTracker
{
    private HashMap<SocketChannel, AtomicInteger> connectedClients;

    public ClientTracker()
    {
        this.connectedClients = new HashMap<>();
    }

    public synchronized void addConnection(SocketChannel socketChannel)
    {
        connectedClients.put(socketChannel, new AtomicInteger(0));
    }

    public synchronized void incrementClient(SocketChannel socketChannel)
    {
        connectedClients.get(socketChannel).incrementAndGet();
    }

    public synchronized int amount()
    {
        return connectedClients.size();
    }

    public synchronized int countProcessed()
    {
        int total = 0;
        for(AtomicInteger count : connectedClients.values()) {
            total += count.get();
        }
        return total;
    }

    public synchronized void resetClientStats()
    {
        for(AtomicInteger count : connectedClients.values()) {
            count.set(0);
        }
    }

    public synchronized void removeClient(SocketChannel socketChannel)
    {
        connectedClients.remove(socketChannel);
    }

    public synchronized double calculateMean()
    {
        double mean = 0.0;
        for(AtomicInteger count : connectedClients.values()) {
            mean += count.get();
        }
        return mean/amount();
    }

    public double calculateMean(double[] array)
    {
        double mean = 0;
        for(double i : array) {
            mean += i;
        }
        return mean/array.length;
    }

    public synchronized double calculateStdDev()
    {
        double[] middleMan = new double[amount()];

        // Calculate the mean
        double mean = calculateMean();

        // Subtract the mean from each point
        int i = 0;
        for(AtomicInteger count : connectedClients.values()) {
            middleMan[i] = count.get();
            i++;
        }

        for(int j = 0; j < middleMan.length; j++) {
            middleMan[j] = middleMan[j] - mean;
        }

        // Square each difference
        for(int j = 0; j < middleMan.length; j++) {
            middleMan[j] = middleMan[j] * middleMan[j];
        }

        // Calculate the mean of the squared differences
        double squaredMean = calculateMean(middleMan);

        // Take the square root
        return (int) Math.sqrt(squaredMean);
    }
}
