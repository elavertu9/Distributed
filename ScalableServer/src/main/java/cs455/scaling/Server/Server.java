package cs455.scaling.Server;

// Java imports
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

// Custom imports
import cs455.scaling.task.*;
import cs455.scaling.task.ServerStatPrinter;

public class Server implements Runnable
{
    private static Logger logger = LogManager.getLogger(Server.class);
    private int serverPort;
    private ThreadPool threadPool;
    private Selector selector;
    private ServerSocketChannel serverSocket;
    private ServerStatPrinter statPrinter;

    public Server(int serverPort, int threadPoolSize, int batchSize, long batchTime) throws IOException
    {
        this.serverPort = serverPort;
        this.threadPool = new ThreadPool(threadPoolSize, batchSize, batchTime);
        this.statPrinter = new ServerStatPrinter(this);
    }

    public void render() throws IOException
    {
        initializeServerSocket();

        startThreadPool();
    }

    private void startStatPrinter()
    {
        (new Thread(statPrinter, "Server Stat Printer")).start();
    }

    private void initializeServerSocket() throws IOException
    {
        // Open the selector
        selector = Selector.open();

        // Create input channels
        serverSocket = ServerSocketChannel.open();

        // Bind server socket to listen for incoming connections
        logger.info("Hostname: " + InetAddress.getLocalHost().getCanonicalHostName());
        serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost().getCanonicalHostName(), serverPort));
        serverSocket.configureBlocking(false);

        // Register the channel with the selector
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    private void startThreadPool()
    {
        (new Thread(threadPool, "Thread Pool Manager")).start();
    }

    private void createTask(SelectionKey selectionKey, String type)
    {
        threadPool.addPendingTask(new Task(type, selector, selectionKey));
    }

    public int getNumClients()
    {
        return threadPool.getClientMap().amount();
    }

    public int getTotalProcessed()
    {
        return threadPool.getClientMap().countProcessed();
    }

    public void clearClientStats()
    {
        threadPool.getClientMap().resetClientStats();
    }

    public double getMean()
    {
        return threadPool.getClientMap().calculateMean();
    }

    public double getStdDev()
    {
        return threadPool.getClientMap().calculateStdDev();
    }

    @Override
    public void run()
    {
        startStatPrinter();

        while(true) {
            try {
                selector.selectNow();

                // Key(s) ready
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                // Loop over ready keys
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

                while(keyIterator.hasNext()) {
                    // Grab current key
                    SelectionKey key = keyIterator.next();

                    // Optional
                    if(!key.isValid()) {
                        continue;
                    }

                    // New Connection on serverSocket
                    if(key.isAcceptable()) {
                        logger.info("Current key is acceptable...");
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_ACCEPT);
                        createTask(key, "accept");
                    }

                    // Previous Connection has data to read
                    if(key.isReadable()) {
                        logger.info("Current key is readable...");
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                        createTask(key, "read");
                    }

                    // Remove it from our set
                    keyIterator.remove();
                }

            } catch(IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    public static void main(String[] args)
    {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(Level.DEBUG);
        ctx.updateLoggers();

        logger.info("Main in Server...");

        // java cs455.scaling.server.Server <portnum> <thread-pool-size> <batch-size> <batch-time>
        if(args.length != 4) {
            System.out.println("Usage: Server <portnum> <thread-pool-size> <batch-size> <batch-time>");
            System.exit(0);
        } else {
            try {
                // Read from command line
                int serverPort = Integer.parseInt(args[0]);
                int threadPoolSize = Integer.parseInt(args[1]);
                int batchSize = Integer.parseInt(args[2]);
                long batchTime = Long.parseLong(args[3]);

                // Create server
                Server server = new Server(serverPort, threadPoolSize, batchSize, batchTime);
                server.render();

                // Run Server Thread
                (new Thread(server, "Server Thread")).start();

            } catch(NumberFormatException e) {
                System.out.println("Invalid arguments\n\tUsage: Server <portnum> <thread-pool-size> <batch-size> <batch-time>");
                System.exit(0);
            } catch(IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
