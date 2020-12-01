package cs455.scaling.Server;

// Java imports
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Custom imports
import cs455.scaling.task.Batch;
import cs455.scaling.task.Task;
import cs455.scaling.util.ClientTracker;

public class ThreadPool implements Runnable
{
    private static Logger logger = LogManager.getLogger(ThreadPool.class);
    private int batchSize = 0;
    private long batchTime = 0;
    private final LinkedList<WorkerThread> threadPool;
    private final LinkedBlockingQueue<Batch> batchQueue;
    private final LinkedBlockingQueue<Task> pendingTasks;
    private Batch batch;
    private int workerId = 0;
    private ClientTracker clientMap;

    public ThreadPool(int threadPoolSize, int batchSize, long batchTime) throws IOException
    {
        this.batchSize = batchSize;
        this.batchTime = batchTime;
        this.threadPool = new LinkedList<>();
        this.batchQueue = new LinkedBlockingQueue<>();
        this.pendingTasks = new LinkedBlockingQueue<>();
        this.clientMap = new ClientTracker();

        // Add working threads to thread pool
        for(int i = 0; i < threadPoolSize; i++) {
            threadPool.add(new WorkerThread(this));
        }
    }

    private void startThreadPool()
    {
        // Start all the threads in the thread pool
        for(WorkerThread thread : threadPool) {
            (new Thread(thread, "Worker Thread " + workerId++)).start();
        }
    }

    // For the server to add pending tasks to the queue
    public void addPendingTask(Task task)
    {
        pendingTasks.add(task);
    }

    // Add completed batch to batch queue
    private void addBatch(Batch batch)
    {
        logger.info("Adding batch to queue: " + batch.batchID);
        batchQueue.add(batch);
    }

    // Add individual tasks to batch queue until batch size is reached
    // or batch time expires
    private void addTaskToBatch(Task task)
    {
        logger.info("Adding task to pending batch: " + batch.batchID);

        // Add tasks to  until batch size is met
        if((batch.getTaskQueue().size() + 1) == batchSize) {
            batch.addTask(task);
            addBatch(batch);
            batch = new Batch();
        } else if (batch.getTaskQueue().size() + 1 < batchSize) {
            batch.addTask(task);
        } else {
            logger.info("ERROR: addTaskToBatch():ThreadPool.java - Task did not get added to batch");
        }
    }

    // For the worker threads to check the batch queue
    public LinkedBlockingQueue<Batch> getBatchQueue()
    {
        // Maybe better to return copy?
        return batchQueue;
    }

    public ClientTracker getClientMap()
    {
        return clientMap;
    }

    public synchronized void addClientConnection(SocketChannel socketChannel)
    {
        clientMap.addConnection(socketChannel);
    }

    public synchronized void incrementClient(SocketChannel socketChannel)
    {
        clientMap.incrementClient(socketChannel);
    }

    public synchronized void removeClient(SocketChannel socketChannel)
    {
        clientMap.removeClient(socketChannel);
    }

    @Override
    public void run()
    {
        // Server filling pending tasks as requests arrive

        // ThreadPool organize pending tasks into batches for threads in the pool to handle
        startThreadPool();
        batch = new Batch();

        while(true) {
            while(((System.currentTimeMillis() / 1000) - batch.startTime) < (batchTime)) {
                Task toDo = pendingTasks.poll();
                if(toDo != null) {
                    switch(toDo.getType()) {
                        case "accept":
                            logger.info("Accept message in task queue");
                            Batch acceptBatch = new Batch();
                            acceptBatch.addTask(toDo);
                            addBatch(acceptBatch);
                            break;
                        case "read":
                            logger.info("Read message in task queue");
                            addTaskToBatch(toDo);
                            break;
                    }
                }
            }

            // batch time expired
            // sanity check to make sure we are not adding batches with no tasks
            if (batch.getTaskQueue().size() != 0) {
                logger.info("Batch time expired for batch " + batch.batchID + " - adding batch to work queue");
                addBatch(batch);
            }
                batch = new Batch();
        }
    }
}
