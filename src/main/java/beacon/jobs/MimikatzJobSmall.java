package beacon.jobs;

import beacon.TaskBeacon;

public class MimikatzJobSmall
        extends MimikatzJob {
    public MimikatzJobSmall(TaskBeacon tasker, String commandz) {
        super(tasker, commandz);
    }

    @Override
    public String getDLLName() {
        if (this.arch.equals("x64")) {
            return "resources/mimikatz-min.x64.dll";
        }
        return "resources/mimikatz-min.x86.dll";
    }
}

