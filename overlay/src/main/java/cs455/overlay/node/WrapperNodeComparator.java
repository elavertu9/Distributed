package cs455.overlay.node;

// Java imports
import java.util.Comparator;

public class WrapperNodeComparator implements Comparator<WrapperNode>
{

    @Override
    public int compare(WrapperNode node1, WrapperNode node2) {
        return node1.getIdentifier() - node2.getIdentifier();
    }
}
