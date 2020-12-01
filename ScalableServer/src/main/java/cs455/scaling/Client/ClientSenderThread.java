package cs455.scaling.Client;

// Java imports
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

// Custom imports
import cs455.scaling.util.Hasher;

public class ClientSenderThread implements Runnable
{
    private static Logger logger = LogManager.getLogger(ClientSenderThread.class);
    private final SocketChannel channel;
    private int messageRate;
    private Client client;
    private Random random = new Random();
    private Hasher hasher = Hasher.getInstance();

    public ClientSenderThread(SocketChannel channel, int messageRate, Client client)
    {
        this.channel = channel;
        this.messageRate = messageRate;
        this.client = client;

    }

    private void sendMessage() throws IOException
    {

        byte[] randomBuffer = new byte[8192];

        // Fill buffer with garb
        random.nextBytes(randomBuffer);

        computeAndStoreHash(randomBuffer);

        // Send data to server
        ByteBuffer writeBuffer = ByteBuffer.wrap(randomBuffer);
        while(writeBuffer.hasRemaining()) {
            channel.write(writeBuffer);
        }

        logger.info("Message Sent!");

        // Increment sent count
        client.getStats().incrementSent();
    }

    private void computeAndStoreHash(byte[] buffer)
    {
        try {
            String hashCode = hasher.SHA1FromBytes(buffer);

            client.appendHashCode(hashCode);

        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    @Override
    public void run()
    {
        try {
            // wait desired interval
            // Thread.sleep(1000/R) => Typical R values are between 2-4
            while(true) {
                Thread.sleep(1000 / messageRate);
                sendMessage();
            }

        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
