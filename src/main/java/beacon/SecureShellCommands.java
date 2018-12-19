package beacon;

public class SecureShellCommands
        extends BeaconCommands {
    @Override
    public String getCommandFile() {
        return "resources/ssh_help.txt";
    }

    @Override
    public String getDetailFile() {
        return "resources/ssh_details.txt";
    }
}

