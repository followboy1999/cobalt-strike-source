package beacon;

public class Tasks {
    public static final int COMMAND_SPAWN = 1;
    public static final int COMMAND_SHELL = 2;
    public static final int COMMAND_DIE = 3;
    public static final int COMMAND_SLEEP = 4;
    public static final int COMMAND_CD = 5;
    public static final int COMMAND_KEYLOG_START = 6;
    public static final int COMMAND_KEYLOG_STOP = 7;
    public static final int COMMAND_INJECT_PID = 9;
    public static final int COMMAND_UPLOAD = 10;
    public static final int COMMAND_SPAWN_PROC_X86 = 13;
    public static final int COMMAND_JOB_REGISTER = 40;
    public static final int COMMAND_INJECTX64_PID = 43;
    public static final int COMMAND_SPAWNX64 = 44;
    public static final int COMMAND_JOB_REGISTER_IMPERSONATE = 62;
    public static final int COMMAND_SPAWN_POWERSHELLX86 = 63;
    public static final int COMMAND_SPAWN_POWERSHELLX64 = 64;
    public static final int COMMAND_INJECT_POWERSHELLX86_PID = 65;
    public static final int COMMAND_INJECT_POWERSHELLX64_PID = 66;
    public static final int COMMAND_UPLOAD_CONTINUE = 67;
    public static final int COMMAND_PIPE_OPEN_EXPLICIT = 68;
    public static final int COMMAND_SPAWN_PROC_X64 = 69;
    public static final int COMMAND_JOB_SPAWN_X86 = 70;
    public static final int COMMAND_JOB_SPAWN_X64 = 71;
    public static final int COMMAND_SETENV = 72;
    public static final int COMMAND_FILE_COPY = 73;
    public static final int COMMAND_FILE_MOVE = 74;
    public static final int COMMAND_PPID = 75;
    public static final int COMMAND_RUN_UNDER_PID = 76;
    public static final int COMMAND_GETPRIVS = 77;
    public static final int COMMAND_EXECUTE_JOB = 78;
    public static final int COMMAND_PSH_HOST_TCP = 79;
    public static final int COMMAND_DLL_LOAD = 80;
    public static final int COMMAND_REG_QUERY = 81;

    public static long max() {
        return 0x100000L;
    }
}

