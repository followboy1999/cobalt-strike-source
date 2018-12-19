package beacon;

public class TaskBeaconCallback {
    protected CommandBuilder builder = new CommandBuilder();

    protected byte[] taskNoArgsCallback(int command, int reqid) {
        this.builder.setCommand(command);
        this.builder.addInteger(reqid);
        return this.builder.build();
    }

    public byte[] IPConfig(int reqid) {
        return this.taskNoArgsCallback(48, reqid);
    }

    public byte[] Ps(int reqid) {
        return this.taskNoArgsCallback(32, reqid);
    }

    public byte[] Ls(int reqid, String folder) {
        this.builder.setCommand(53);
        this.builder.addInteger(reqid);
        if (folder.endsWith("\\")) {
            this.builder.addLengthAndString(folder + "*");
        } else {
            this.builder.addLengthAndString(folder + "\\*");
        }
        return this.builder.build();
    }

    public byte[] Drives(int reqid) {
        return this.taskNoArgsCallback(55, reqid);
    }
}

