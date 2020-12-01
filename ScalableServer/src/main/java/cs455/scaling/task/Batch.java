package cs455.scaling.task;

// Java imports
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Random;

public class Batch
{
    public int batchID;
    public long startTime;
    private LinkedBlockingQueue<Task> taskQueue;

    public Batch()
    {
        this.startTime = System.currentTimeMillis() / 1000;
        this.taskQueue = new LinkedBlockingQueue<>();
        this.batchID = new Random().nextInt(50);
    }

    public void addTask(Task task)
    {
        taskQueue.add(task);
    }

    public Task getTask() throws InterruptedException
    {
        return taskQueue.take();
    }

    public LinkedBlockingQueue<Task> getTaskQueue()
    {
        return taskQueue;
    }
}
