package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;

public class NetViewJob
        extends Job {
    protected String command;
    protected String target;
    protected String param;

    public NetViewJob(TaskBeacon tasker, String command, String target, String param) {
        super(tasker);
        this.command = command;
        this.target = target;
        this.param = param;
    }

    @Override
    public String getTactic() {
        if (CommonUtils.toSet("computers, dclist, domain_trusts, view").contains(this.command)) {
            return "T1018";
        }
        if (CommonUtils.toSet("group, localgroup, user").contains(this.command)) {
            return "T1087";
        }
        if (CommonUtils.toSet("logons, sessions").contains(this.command)) {
            return "T1033";
        }
        if ("share".equals(this.command)) {
            return "T1135";
        }
        if ("time".equals(this.command)) {
            return "T1124";
        }
        return "";
    }

    @Override
    public String getDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("Tasked beacon to run net ").append(this.command);
        if (this.param != null) {
            desc.append(" ").append(this.param);
        }
        if (this.target != null) {
            desc.append(" on ").append(this.target);
        }
        return desc.toString();
    }

    @Override
    public String getShortDescription() {
        return "net " + this.command;
    }

    @Override
    public String getDLLName() {
        return "resources/netview.dll";
    }

    @Override
    public String getPipeName() {
        return "netview";
    }

    @Override
    public int getCallbackType() {
        return 24;
    }

    @Override
    public int getWaitTime() {
        return 30000;
    }

    @Override
    public byte[] fix(byte[] mydll) {
        Packer packit = new Packer();
        packit.little();
        packit.addWideString(this.command, 2048);
        if (this.target != null) {
            packit.addWideString(this.target, 2048);
        } else {
            packit.pad('\u0000', 2048);
        }
        if (this.param != null) {
            packit.addWideString(this.param, 2048);
        } else {
            packit.pad('\u0000', 2048);
        }
        String patch = CommonUtils.bString(packit.getBytes());
        mydll = CommonUtils.patch(mydll, "PATCHME!12345", patch);
        return mydll;
    }
}

