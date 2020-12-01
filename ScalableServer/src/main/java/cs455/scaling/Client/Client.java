package cs455.scaling.Client;

// Java imports
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

// Custom imports
import cs455.scaling.util.AtomicStats;
import cs455.scaling.task.*;

public class Client implements Runnable
{
    private static Logger logger = LogManager.getLogger(Client.class);
    private String serverHostname;
    private int serverPort;
    private int messageRate;
    private final LinkedList<String> hashCodes;
    private SocketChannel serverConnection;
    private ClientSenderThread senderThread;
    private AtomicStats stats;
    private ClientStatPrinter statPrinter;

    public Client(String serverHostname, int serverPort, int messageRate)
    {
        this.serverHostname = serverHostname;
        this.serverPort = serverPort;
        this.messageRate = messageRate;
        this.hashCodes = new LinkedList<>();
        this.stats = new AtomicStats();
        this.statPrinter = new ClientStatPrinter(this);
    }

    public void render() throws IOException
    {
        connectToServer();
    }

    private void startStatPrinter()
    {
        (new Thread(statPrinter, "Client Stat Printer")).start();
    }

    private void connectToServer() throws IOException
    {
        // Connect to server
        serverConnection = SocketChannel.open(new InetSocketAddress(serverHostname, serverPort));

        // Loop to ensure method exits once connection is established
        while(!serverConnection.finishConnect()) {
            serverConnection.finishConnect();
        }

        senderThread = new ClientSenderThread(serverConnection, messageRate, this);
    }

    private void startSenderThread()
    {
        (new Thread(senderThread, "Client Sender Thread")).start();
    }

    public void appendHashCode(String hashCode)
    {
        // Add hash code to list of sent hash codes
        synchronized (hashCodes) {
            hashCodes.add(hashCode);
        }
    }


    private void confirmMatchingHashCodes(String hashCode)
    {
        synchronized (hashCodes) {
            if(hashCodes.contains(hashCode)) {
                hashCodes.remove(hashCode);
            }
        }
    }

    public AtomicStats getStats()
    {
        return stats;
    }

    public int getSentCount()
    {
        return stats.getSent();
    }

    public int getReceivedCount()
    {
        return stats.getReceived();
    }

    public void resetStats()
    {
        stats.resetStats();
    }

    @Override
    public void run()
    {
        startSenderThread();

        startStatPrinter();

        try {
            ByteBuffer buffer = ByteBuffer.allocate(40);
            byte[] received = new byte[buffer.capacity()];

            while(true) {
                int bytesRead = 0;
                buffer.clear();
                while(buffer.hasRemaining()) {
                    bytesRead = serverConnection.read(buffer);
                }
                if (bytesRead == -1) {
                    logger.info("Error reading response");
                } else {
                    buffer.rewind();

                    buffer.get(received);

                    String hash = new String(received);

                    confirmMatchingHashCodes(hash);

                    stats.incrementReceived();
                }
            }


        }catch(IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static void main(String[] args)
    {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.DEBUG);
        ctx.updateLoggers();

        logger.info("Main in Client...");

        // java cs455.scaling.client.Client <server-host> <server-port> <message-rate>
        if(args.length != 3) {
            System.out.println("Usage: Client <server-host> <server-port> <message-rate>");
            System.exit(0);
        } else {
            try {
                // Read from command line
                String serverHostname = args[0];
                int serverPort = Integer.parseInt(args[1]);
                int messageRate = Integer.parseInt(args[2]);

                // Create Client
                Client client = new Client(serverHostname, serverPort, messageRate);
                client.render();

                // Run Client Thread
                (new Thread(client, "Client Thread")).start();

            }catch(NumberFormatException e) {
                System.out.println("Invalid arguments\n\tUsage: Client <server-host> <server-port> <message-rate>");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
