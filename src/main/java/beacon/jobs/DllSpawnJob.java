package beacon.jobs;

import beacon.JobSimple;
import beacon.TaskBeacon;
import common.CommonUtils;

import java.io.File;

public class DllSpawnJob
        extends JobSimple {
    protected String file;
    protected String arg;
    protected String desc;
    protected int waittime;

    public DllSpawnJob(TaskBeacon tasker, String file, String arg, String desc, int waittime) {
        super(tasker);
        this.file = file;
        this.arg = arg;
        this.desc = desc;
        this.waittime = waittime;
        if (desc == null || desc.length() == 0) {
            this.desc = CommonUtils.stripRight(new File(file).getName(), ".dll");
        }
        if (desc.length() > 48) {
        }
    }

    @Override
    public String getDescription() {
        return "Tasked beacon to spawn " + this.desc;
    }

    @Override
    public String getShortDescription() {
        return this.desc;
    }

    @Override
    public String getDLLName() {
        return this.file;
    }

    @Override
    public int getWaitTime() {
        return this.waittime;
    }

    @Override
    public byte[] getArgument() {
        return CommonUtils.toBytes(this.arg + '\u0000');
    }

    @Override
    public byte[] getDLLContent() {
        return CommonUtils.readFile(this.getDLLName());
    }
}

