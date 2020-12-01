package cs455.overlay.util;

// Java imports
import java.util.Scanner;

// Custom imports
import cs455.overlay.node.Registry;

public class RegistryCommandParser
{
    private Scanner parser;
    private Registry registry;

    public RegistryCommandParser(Registry registry)
    {
        this.parser = new Scanner(System.in);
        this.registry = registry;
    }

    public void listen()
    {
        String input;
        while(true)
        {
            input = getParser().nextLine();
            handleInput(input);
        }
    }

    private void handleInput(String input)
    {
        String[] split = input.split(" ");
        switch(split[0]) {
            case "list-messaging-nodes":
                getRegistry().listMessagingNodes();
                break;
            case "setup-overlay":
                if(split.length == 2) {
                    try {
                        int N = Integer.parseInt(split[1]);
                        getRegistry().setupOverlay(N);
                    }catch(NumberFormatException e) {
                        System.out.println("Routing table size requested was not a number...");
                    }
                } else if (split.length == 1) {
                    int N = 3;
                    getRegistry().setupOverlay(N);
                } else {
                    System.out.println("Please enter a routing table size with the setup-overlay command...");
                }
                break;
            case "list-routing-tables":
                getRegistry().listRoutingTables();
                break;
            case "start":
                if(split.length == 2) {
                    try {
                        int numMessages = Integer.parseInt(split[1]);
                        getRegistry().sendTaskInitiate(numMessages);
                    } catch(NumberFormatException e) {
                        System.out.println("Number of messages requested was not a number...");
                    }
                } else {
                    System.out.println("Please enter number of messages with the start command...");
                }
                break;
            default:
                System.out.println("Usage: \n\tlist-messaging-nodes\n\tsetup-overlay number-of-routing-table-entries\n\tlist-routing-tables\n\tstart number-of-messages");
        }
    }

    public Scanner getParser() { return this.parser; }
    public Registry getRegistry() { return this.registry; }
}
