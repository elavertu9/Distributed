package cs455.scaling.Server;

// Java imports
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;

// Custom imports
import cs455.scaling.util.Hasher;
import cs455.scaling.task.Task;
import cs455.scaling.task.Batch;

public class WorkerThread implements Runnable
{
    private static Logger logger = LogManager.getLogger(WorkerThread.class);
    private ThreadPool myPool;
    private Hasher hasher = Hasher.getInstance();

    public WorkerThread(ThreadPool myPool)
    {
        this.myPool = myPool;
    }

    private void processBatch(Batch batch) throws IOException, InterruptedException
    {
        logger.info("Worker thread processing batch " + batch.batchID);
        while(batch.getTaskQueue().size() > 0) {
            Task task = batch.getTask();
            switch(task.getType()) {
                case "accept":
                    handleAcceptTask(task);
                    break;
                case "read":
                    handleReadTask(task);
                    break;
            }
        }
    }

    private void handleAcceptTask(Task task) throws IOException
    {
        logger.info("Thread worker currently processing accept task");

        SelectionKey channelKey = task.getSelectionKey();
        ServerSocketChannel serverChannel = (ServerSocketChannel) channelKey.channel();

        if(channelKey.isAcceptable()) {
            SocketChannel client = serverChannel.accept();
            if(client != null) {
                client.configureBlocking(false);
                client.register(channelKey.selector(), SelectionKey.OP_READ);
                myPool.addClientConnection(client);
                logger.info("New Client Registered...");
            }
        }
        channelKey.interestOps(channelKey.interestOps() | SelectionKey.OP_ACCEPT);
    }

    private void handleReadTask(Task task) throws IOException
    {
        logger.info("Thread worker currently processing read task");

        SelectionKey channelKey = task.getSelectionKey();

        // Create a buffer to read into
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        byte[] received = new byte[buffer.capacity()];

        // Grab the socket from the key
        SocketChannel client = (SocketChannel) channelKey.channel();

        // Read from it
        int bytesRead = 0;
        while(buffer.hasRemaining()) {
            bytesRead = client.read(buffer);
        }

        // Error check
        if(bytesRead != -1) {
            // Rewind buffer iterator
            buffer.rewind();

            // Fill byte[] w/ ByteBuffer contents
            buffer.get(received);

            // Make key readable again for more messages
            channelKey.interestOps(SelectionKey.OP_READ);

            // Increment client processed count
            myPool.incrementClient(client);

            sendHashResponse(client, received);

        } else {
            myPool.removeClient(client);
            logger.info("Error on Read");
        }
    }

    private void sendHashResponse(SocketChannel client, byte[] message) throws IOException
    {
        try {
            byte[] response = computeHash(message).getBytes();
            ByteBuffer writeBuffer = ByteBuffer.wrap(response);

            logger.info("Writing to client");
            while(writeBuffer.hasRemaining()) {
                client.write(writeBuffer);
            }
            writeBuffer.clear();
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private String computeHash(byte[] message) throws NoSuchAlgorithmException
    {
        return hasher.SHA1FromBytes(message);
    }

    @Override
    public void run()
    {
        try {
            while(true) {
                if(myPool.getBatchQueue().size() != 0) {
                    Batch myBatch = myPool.getBatchQueue().take();
                    processBatch(myBatch);
                }
            }
        } catch(InterruptedException | IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
