package cs455.overlay.wireformats;

public class Protocol
{
    public String getMessageTypeString(int messageTypeInt)
    {
        switch(messageTypeInt)
        {
            case 2:
                return "OVERLAY_NODE_SENDS_REGISTRATION";
            case 3:
                return "REGISTRY_REPORTS_REGISTRATION_STATUS";
            case 4:
                return "OVERLAY_NODE_SENDS_DEREGISTRATION";
            case 5:
                return "REGISTRY_REPORTS_DEREGISTRATION_STATUS";
            case 6:
                return "REGISTRY_SENDS_NODE_MANIFEST";
            case 7:
                return "NODE_REPORTS_OVERLAY_SETUP_STATUS";
            case 8:
                return "REGISTRY_REQUESTS_TASK_INITIATE";
            case 9:
                return "OVERLAY_NODE_SENDS_DATA";
            case 10:
                return "OVERLAY_NODE_REPORTS_TASK_FINISHED";
            case 11:
                return "REGISTRY_REQUESTS_TRAFFIC_SUMMARY";
            case 12:
                return "OVERLAY_NODE_REPORTS_TRAFFIC_SUMMARY";
            default:
                return "INVALID";
        }
    }
}
