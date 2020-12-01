package cs455.overlay.util;

public class StatCollector
{
    private int sendTracker; /* Number of data packets sent by node */
    private int receiveTracker; /* Number of packets received by node */
    private int relayTracker; /* Number of packets that a node relays (i.e., packets for which it was neither the source nor the sink) */
    private long sendSum;
    private long receiveSum;

    public StatCollector()
    {
        this.sendTracker = 0;
        this.receiveTracker = 0;
        this.relayTracker = 0;
        this.sendSum = 0;
        this.receiveSum = 0;
    }

    public synchronized void incrementSendTracker()
    {
        this.sendTracker++;
    }

    public synchronized void incrementReceiveTracker()
    {
        this.receiveTracker++;
    }

    public synchronized void incrementRelayTracker()
    {
        this.relayTracker++;
    }

    public synchronized void addSendSum(long sum) { this.sendSum += sum; }

    public synchronized void addReceiveSum(long sum) { this.receiveSum += sum; }

    public void displayStats(int identifier)
    {
    	System.out.printf("%19s", "Node ID");
    	System.out.printf("%19s", "Packets Sent");
        System.out.printf("%19s", "Packets Received");
        System.out.printf("%19s", "Packets Relayed");
        System.out.printf("%19s", "Sum Sent");
        System.out.printf("%19s%n", "Sum Recieved");
        
        System.out.printf("%19s", identifier);
    	System.out.printf("%19s", getSendTracker());
        System.out.printf("%19s", getReceiveTracker());
        System.out.printf("%19s", getRelayTracker());
        System.out.printf("%19s", getSendSum());
        System.out.printf("%19s%n", getReceiveSum());
    }

    public synchronized int getSendTracker() { return this.sendTracker; }
    public synchronized int getReceiveTracker() { return this.receiveTracker; }
    public synchronized int getRelayTracker() { return this.relayTracker; }
    public synchronized long getSendSum() { return this.sendSum; }
    public synchronized long getReceiveSum() { return this.receiveSum; }
}
