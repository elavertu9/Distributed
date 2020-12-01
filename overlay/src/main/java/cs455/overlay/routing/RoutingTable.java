package cs455.overlay.routing;

// Java imports
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;

public class RoutingTable
{
    private TreeMap<Integer, ArrayList<RoutingEntry>> routingTable;

    public RoutingTable()
    {
        this.routingTable = new TreeMap<>();
    }

    public void addEntry(Integer identifier, ArrayList<RoutingEntry> entry)
    {
        getRoutingTable().put(identifier, entry);
    }

    @Override
    public String toString()
    {
        String toString = "Master Routing Table: \n";
        for(Map.Entry<Integer, ArrayList<RoutingEntry>> entry : getRoutingTable().entrySet()) {
            toString += entry.getKey() + ": " + entry.getValue().toString() + "\n\n";
        }
        return toString;
    }

    /* =============================================================================================================================
       =                                                      GETTERS                                                              =
       =============================================================================================================================
     */
    public TreeMap<Integer, ArrayList<RoutingEntry>> getRoutingTable()
    {
        return this.routingTable;
    }
}
