package cs455.overlay.node;

// Java imports
import java.net.*;

// Custom imports
import cs455.overlay.wireformats.*;

public interface Node
{
    public void onEvent(Event event, Socket socket);
}
