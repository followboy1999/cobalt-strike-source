package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.AddressList;
import common.CommonUtils;
import common.Packer;
import common.PortFlipper;

public class PortScannerJob
        extends Job {
    protected String targets;
    protected String ports;
    protected String discovery;
    protected int maxsockets;

    public PortScannerJob(TaskBeacon tasker, String targets, String ports, String discovery, int maxsockets) {
        super(tasker);
        this.targets = targets;
        this.ports = ports;
        this.discovery = discovery;
        this.maxsockets = maxsockets;
    }

    @Override
    public String getDescription() {
        return "Tasked beacon to scan ports " + this.ports + " on " + this.targets;
    }

    @Override
    public String getShortDescription() {
        return "port scanner";
    }

    @Override
    public String getDLLName() {
        return "resources/portscan.dll";
    }

    @Override
    public String getPipeName() {
        return "portscan";
    }

    @Override
    public String getTactic() {
        return "T1046";
    }

    @Override
    public int getCallbackType() {
        return 25;
    }

    @Override
    public int getWaitTime() {
        return 1;
    }

    @Override
    public byte[] fix(byte[] mydll) {
        String tg = CommonUtils.pad(CommonUtils.bString(new AddressList(this.targets).export()), '\u0000', 2048);
        mydll = CommonUtils.patch(mydll, "TARGETS!12345", tg);
        mydll = CommonUtils.patch(mydll, "PORTS!12345", CommonUtils.bString(new PortFlipper(this.ports).getMask()));
        Packer patch = new Packer();
        patch.little();
        patch.addInt(this.maxsockets);
        switch (this.discovery) {
            case "none":
                patch.addInt(0);
                break;
            case "icmp":
                patch.addInt(1);
                break;
            case "arp":
                patch.addInt(2);
                break;
        }
        String ds = CommonUtils.pad(CommonUtils.bString(patch.getBytes()), '\u0000', 32);
        mydll = CommonUtils.patch(mydll, "PREFERENCES!12345", ds);
        return mydll;
    }
}

