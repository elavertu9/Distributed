package cs455.overlay.wireformats;

// Java imports
import java.io.IOException;

public interface Event {
    public byte[] getBytes() throws IOException;
    public int getType();
}