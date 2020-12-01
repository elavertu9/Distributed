package cs455.overlay.util;

// Java imports
import java.util.Scanner;

// Custom imports
import cs455.overlay.node.MessagingNode;

public class MessagingNodeCommandParser
{
    private Scanner parser;
    private MessagingNode messagingNode;

    public MessagingNodeCommandParser(MessagingNode messagingNode)
    {
        this.parser = new Scanner(System.in);
        this.messagingNode = messagingNode;
    }

    public void listen()
    {
        String input;
        while(true)
        {
            input = getParser().next();
            handleInput(input);
        }
    }

    private void handleInput(String input)
    {
        switch(input) {
            case "print-counters-and-diagnostics":
                getMessagingNode().printStats();
                break;
            case "exit-overlay":
                getMessagingNode().sendDeregistration();
                break;
            default:
                System.out.println("Usage: \n\tprint-counters-and-diagnostics\n\texit-overlay");
        }
    }

    private void closeParser()
    {
        getParser().close();
    }

    public Scanner getParser() { return this.parser; }
    public MessagingNode getMessagingNode() { return this.messagingNode; }
}
