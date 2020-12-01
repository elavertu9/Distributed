package cs455.scaling.task;

// Java imports
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class Task
{
    private String type;
    private Selector selector;
    private SelectionKey selectionKey;

    public Task(String type, Selector selector, SelectionKey selectionKey)
    {
        this.type = type;
        this.selector = selector;
        this.selectionKey = selectionKey;
    }

    public String getType()
    {
        return type;
    }

    public SelectionKey getSelectionKey()
    {
        return selectionKey;
    }
}
