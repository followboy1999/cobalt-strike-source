package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.MultiFrame;
import aggressor.TabManager;
import aggressor.browsers.Beacons;
import aggressor.browsers.Sessions;
import aggressor.browsers.Targets;
import aggressor.dialogs.*;
import aggressor.viz.PivotGraph;
import aggressor.windows.*;
import common.BeaconEntry;
import common.CommonUtils;
import common.TeamQueue;
import console.Console;
import cortana.Cortana;
import dialog.DialogUtils;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.util.Stack;

public class AggressorBridge implements Function,
        Loadable {
    protected TabManager manager;
    protected Cortana engine;
    protected MultiFrame window;
    protected AggressorClient client;
    protected TeamQueue conn;

    public AggressorBridge(AggressorClient c, Cortana e, TabManager m, MultiFrame w, TeamQueue q) {
        this.client = c;
        this.engine = e;
        this.manager = m;
        this.window = w;
        this.conn = q;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&openScriptConsole", this);
        Cortana.put(si, "&openEventLog", this);
        Cortana.put(si, "&openConnectDialog", this);
        Cortana.put(si, "&closeClient", this);
        Cortana.put(si, "&openHostFileDialog", this);
        Cortana.put(si, "&openWebLog", this);
        Cortana.put(si, "&openSiteManager", this);
        Cortana.put(si, "&openListenerManager", this);
        Cortana.put(si, "&openBeaconBrowser", this);
        Cortana.put(si, "&openWindowsExecutableStageDialog", this);
        Cortana.put(si, "&openAutoRunDialog", this);
        Cortana.put(si, "&openPayloadHelper", this);
        Cortana.put(si, "&openWindowsExecutableDialog", this);
        Cortana.put(si, "&openPayloadGeneratorDialog", this);
        Cortana.put(si, "&openOfficeMacroDialog", this);
        Cortana.put(si, "&openJavaSignedAppletDialog", this);
        Cortana.put(si, "&openJavaSmartAppletDialog", this);
        Cortana.put(si, "&openHTMLApplicationDialog", this);
        Cortana.put(si, "&openWindowsDropperDialog", this);
        Cortana.put(si, "&openPowerShellWebDialog", this);
        Cortana.put(si, "&openScriptedWebDialog", this);
        Cortana.put(si, "&openBeaconConsole", this);
        Cortana.put(si, "&openProcessBrowser", this);
        Cortana.put(si, "&openFileBrowser", this);
        Cortana.put(si, "&openCloneSiteDialog", this);
        Cortana.put(si, "&openSystemProfilerDialog", this);
        Cortana.put(si, "&openSpearPhishDialog", this);
        Cortana.put(si, "&openPreferencesDialog", this);
        Cortana.put(si, "&openScriptManager", this);
        Cortana.put(si, "&openAboutDialog", this);
        Cortana.put(si, "&openInterfaceManager", this);
        Cortana.put(si, "&openScreenshotBrowser", this);
        Cortana.put(si, "&openKeystrokeBrowser", this);
        Cortana.put(si, "&openDownloadBrowser", this);
        Cortana.put(si, "&openBrowserPivotSetup", this);
        Cortana.put(si, "&openCovertVPNSetup", this);
        Cortana.put(si, "&openSOCKSSetup", this);
        Cortana.put(si, "&openPivotListenerSetup", this);
        Cortana.put(si, "&openSOCKSBrowser", this);
        Cortana.put(si, "&openGoldenTicketDialog", this);
        Cortana.put(si, "&openMakeTokenDialog", this);
        Cortana.put(si, "&openSpawnAsDialog", this);
        Cortana.put(si, "&openCredentialManager", this);
        Cortana.put(si, "&openApplicationManager", this);
        Cortana.put(si, "&openJumpDialog", this);
        Cortana.put(si, "&openTargetBrowser", this);
        Cortana.put(si, "&openServiceBrowser", this);
        Cortana.put(si, "&openPortScanner", this);
        Cortana.put(si, "&openPortScannerLocal", this);
        Cortana.put(si, "&openSystemInformationDialog", this);
        Cortana.put(si, "&addVisualization", this);
        Cortana.put(si, "&showVisualization", this);
        Cortana.put(si, "&pgraph", this);
        Cortana.put(si, "&tbrowser", this);
        Cortana.put(si, "&bbrowser", this);
        Cortana.put(si, "&sbrowser", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&openScriptConsole")) {
            Console c = new CortanaConsole(this.engine).getConsole();
            this.manager.addTab("Script Console", c, null, "Cortana script console");
            return SleepUtils.getScalar(c);
        }
        if (name.equals("&openEventLog")) {
            EventLog ev = new EventLog(this.client.getData(), this.engine, this.client.getConnection());
            Console c = ev.getConsole();
            this.manager.addTab("Event Log", c, ev.cleanup(), "Log of events/chat messages");
            return SleepUtils.getScalar(c);
        }
        if (name.equals("&openWebLog")) {
            WebLog ev = new WebLog(this.client.getData(), this.engine, this.client.getConnection());
            Console c = ev.getConsole();
            this.manager.addTab("Web Log", c, ev.cleanup(), "Log of web server activity");
            return SleepUtils.getScalar(c);
        }
        if (name.equals("&openSiteManager")) {
            SiteManager ev = new SiteManager(this.client.getData(), this.engine, this.client.getConnection());
            this.manager.addTab("Sites", ev.getContent(), ev.cleanup(), "Manage Cobalt Strike's web server");
            return SleepUtils.getEmptyScalar();
        }
        if (name.equals("&openListenerManager")) {
            ListenerManager ev = new ListenerManager(this.client.getData(), this.engine, this.client.getConnection());
            this.manager.addTab("Listeners", ev.getContent(), ev.cleanup(), "Manage Cobalt Strike's listeners");
            return SleepUtils.getEmptyScalar();
        }
        if (name.equals("&openCredentialManager")) {
            CredentialManager ev = new CredentialManager(this.client);
            this.manager.addTab("Credentials", ev.getContent(), ev.cleanup(), "Manage credentials");
            return SleepUtils.getEmptyScalar();
        }
        if (name.equals("&openApplicationManager")) {
            ApplicationManager ev = new ApplicationManager(this.client);
            this.manager.addTab("Applications", ev.getContent(), ev.cleanup(), "View system profiler results");
            return SleepUtils.getEmptyScalar();
        }
        if (name.equals("&openBeaconBrowser")) {
            BeaconBrowser ev = new BeaconBrowser(this.client);
            this.manager.addTab("Beacons", ev.getContent(), ev.cleanup(), "Haters gonna hate, beacons gonna beacon");
            return SleepUtils.getEmptyScalar();
        }
        if (name.equals("&openTargetBrowser")) {
            TargetBrowser ev = new TargetBrowser(this.client);
            this.manager.addTab("Targets", ev.getContent(), ev.cleanup(), "Hosts that Cobalt Strike knows about");
            return SleepUtils.getEmptyScalar();
        }
        if (name.equals("&openServiceBrowser")) {
            String[] targets = CommonUtils.toStringArray(BridgeUtilities.getArray(args));
            ServiceBrowser ev = new ServiceBrowser(this.client, targets);
            this.manager.addTab("Services", ev.getContent(), ev.cleanup(), "Services known by Cobalt Strike");
            return SleepUtils.getEmptyScalar();
        }
        if (name.equals("&openPortScanner")) {
            String[] targets = CommonUtils.toStringArray(BridgeUtilities.getArray(args));
            new PortScanDialog(this.client, targets).show();
            return SleepUtils.getEmptyScalar();
        }
        if (name.equals("&openPortScannerLocal")) {
            String bid = BridgeUtilities.getString(args, "");
            new PortScanLocalDialog(this.client, bid).show();
            return SleepUtils.getEmptyScalar();
        }
        switch (name) {
            case "&openBeaconConsole": {
                String bid = BridgeUtilities.getString(args, "");
                BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), bid);
                if (entry == null) {
                    throw new RuntimeException("No beacon entry for: '" + bid + "'");
                }
                if (entry.isBeacon()) {
                    BeaconConsole c = new BeaconConsole(bid, this.client);
                    this.manager.addTab(entry.title(), c.getConsole(), c.cleanup(), "Beacon console");
                } else if (entry.isSSH()) {
                    SecureShellConsole c = new SecureShellConsole(bid, this.client);
                    this.manager.addTab(entry.title(), c.getConsole(), c.cleanup(), "SSH console");
                }
                break;
            }
            case "&openProcessBrowser": {
                String[] bids = BeaconBridge.bids(args);
                if (bids.length == 1) {
                    BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), bids[0]);
                    ProcessBrowser c = new ProcessBrowser(this.client, bids[0]);
                    this.manager.addTab(entry.title("Processes"), c.getContent(), null, "Process Browser");
                } else {
                    ProcessBrowserMulti c = new ProcessBrowserMulti(this.client, bids);
                    this.manager.addTab("Processes", c.getContent(), null, "Process Browser");
                }
                break;
            }
            case "&openFileBrowser": {
                String[] bids = BeaconBridge.bids(args);
                if (bids.length == 1) {
                    BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), bids[0]);
                    FileBrowser c = new FileBrowser(this.client, bids[0]);
                    this.manager.addTab(entry.title("Files"), c.getContent(), null, "File Browser");
                }
                break;
            }
            case "&openBrowserPivotSetup": {
                String bid = BridgeUtilities.getString(args, "");
                new BrowserPivotSetup(this.client, bid).show();
                break;
            }
            case "&openGoldenTicketDialog": {
                String bid = BridgeUtilities.getString(args, "");
                new GoldenTicketDialog(this.client, bid).show();
                break;
            }
            case "&openMakeTokenDialog": {
                String bid = BridgeUtilities.getString(args, "");
                new MakeTokenDialog(this.client, bid).show();
                break;
            }
            case "&openSpawnAsDialog": {
                String bid = BridgeUtilities.getString(args, "");
                new SpawnAsDialog(this.client, bid).show();
                break;
            }
            case "&openJumpDialog":
                String type = BridgeUtilities.getString(args, "");
                String[] targets = CommonUtils.toStringArray(BridgeUtilities.getArray(args));
                switch (type) {
                    case "psexec":
                        JumpDialogAlt.PsExec(this.client, targets).show();
                        break;
                    case "psexec_psh":
                        JumpDialogAlt.PsExecPSH(this.client, targets).show();
                        break;
                    case "winrm":
                        JumpDialogAlt.WinRM(this.client, targets).show();
                        break;
                    case "wmi":
                        JumpDialogAlt.WMI(this.client, targets).show();
                        break;
                    case "ssh":
                        new SecureShellDialog(this.client, targets).show();
                        break;
                    case "ssh-key":
                        new SecureShellPubKeyDialog(this.client, targets).show();
                        break;
                }
                break;
            case "&openSOCKSSetup": {
                String bid = BridgeUtilities.getString(args, "");
                new SOCKSSetup(this.client, bid).show();
                break;
            }
            case "&openPivotListenerSetup": {
                String bid = BridgeUtilities.getString(args, "");
                new PivotListenerSetup(this.client, bid).show();
                break;
            }
            case "&openCovertVPNSetup": {
                String bid = BridgeUtilities.getString(args, "");
                new CovertVPNSetup(this.client, bid).show();
                break;
            }
            case "&openScreenshotBrowser": {
                ScreenshotBrowser c = new ScreenshotBrowser(this.client.getData(), this.engine, this.client.getConnection());
                this.manager.addTab("Screenshots", c.getContent(), c.cleanup(), "Screenshot browser");
                break;
            }
            case "&openSOCKSBrowser": {
                SOCKSBrowser c = new SOCKSBrowser(this.client);
                this.manager.addTab("Proxy Pivots", c.getContent(), c.cleanup(), "Beacon SOCKS Servers, port forwards, and reverse port forwards.");
                break;
            }
            case "&openKeystrokeBrowser": {
                KeystrokeBrowser c = new KeystrokeBrowser(this.client.getData(), this.engine, this.client.getConnection());
                this.manager.addTab("Keystrokes", c.getContent(), c.cleanup(), "Keystroke browser");
                break;
            }
            case "&openDownloadBrowser": {
                DownloadBrowser c = new DownloadBrowser(this.client.getData(), this.engine, this.client.getConnection());
                this.manager.addTab("Downloads", c.getContent(), c.cleanup(), "Downloads browser");
                break;
            }
            case "&openConnectDialog":
                new ConnectDialog(this.window).show();
                break;
            case "&openHostFileDialog":
                new HostFileDialog(this.window, this.conn, this.client.getData()).show();
                break;
            case "&openCloneSiteDialog":
                new CloneSiteDialog(this.window, this.conn, this.client.getData()).show();
                break;
            case "&openSystemProfilerDialog":
                new SystemProfilerDialog(this.window, this.conn, this.client.getData()).show();
                break;
            case "&openSpearPhishDialog":
                new SpearPhishDialog(this.client, this.window, this.conn, this.client.getData()).show();
                break;
            case "&closeClient":
                this.client.kill();
                break;
            case "&openWindowsExecutableStageDialog":
                new WindowsExecutableStageDialog(this.client).show();
                break;
            case "&openAutoRunDialog":
                new AutoRunDialog(this.window, this.conn).show();
                break;
            case "&openPayloadHelper":
                final SleepClosure f = BridgeUtilities.getFunction(args, script);
                new ListenerChooser(this.client.getConnection(), this.client.getData(), r -> {
                    Stack<Scalar> args1 = new Stack<>();
                    args1.push(SleepUtils.getScalar(r));
                    SleepUtils.runCode(f, "dialogResult", null, args1);
                }).show();
                break;
            case "&openWindowsExecutableDialog":
                new WindowsExecutableDialog(this.client).show();
                break;
            case "&openPayloadGeneratorDialog":
                new PayloadGeneratorDialog(this.client).show();
                break;
            case "&openOfficeMacroDialog":
                new OfficeMacroDialog(this.client).show();
                break;
            case "&openJavaSignedAppletDialog":
                new JavaSignedAppletDialog(this.client).show();
                break;
            case "&openJavaSmartAppletDialog":
                new JavaSmartAppletDialog(this.client).show();
                break;
            case "&openHTMLApplicationDialog":
                new HTMLApplicationDialog(this.client).show();
                break;
            case "&openWindowsDropperDialog":
                new WindowsDropperDialog(this.client).show();
                break;
            case "&openPowerShellWebDialog":
                new ScriptedWebDialog(this.client).show();
                break;
            case "&openScriptedWebDialog":
                new ScriptedWebDialog(this.client).show();
                break;
            case "&openPreferencesDialog":
                new PreferencesDialog().show();
                break;
            case "&openAboutDialog":
                new AboutDialog().show();
                break;
            case "&openScriptManager":
                ScriptManager m = new ScriptManager(this.client);
                this.manager.addTab("Scripts", m.getContent(), null, "Manage your Aggressor scripts.");
                break;
            case "&openInterfaceManager":
                InterfaceManager ev = new InterfaceManager(this.client.getData(), this.engine, this.client.getConnection());
                this.manager.addTab("Interfaces", ev.getContent(), ev.cleanup(), "Manage Covert VPN Interfaces");
                break;
            case "&openSystemInformationDialog":
                new SystemInformationDialog(this.client).show();
                break;
            case "&addVisualization": {
                String title = BridgeUtilities.getString(args, "");
                JComponent comp = (JComponent) BridgeUtilities.getObject(args);
                this.client.addViz(title, comp);
                break;
            }
            case "&showVisualization": {
                String title = BridgeUtilities.getString(args, "");
                this.client.showViz(title);
                break;
            }
            case "&pgraph": {
                PivotGraph graph = new PivotGraph(this.client);
                graph.ready();
                return SleepUtils.getScalar(graph.getContent());
            }
            case "&tbrowser": {
                Targets table = new Targets(this.client);
                JComponent content = table.getContent();
                DialogUtils.setupScreenshotShortcut(this.client, table.getTable(), "Targets");
                return SleepUtils.getScalar(content);
            }
            case "&bbrowser": {
                Beacons beacons
                        = new Beacons(this.client, true);
                JComponent content = beacons.getContent();
                DialogUtils.setupScreenshotShortcut(this.client, beacons.getTable(), "Beacons");
                return SleepUtils.getScalar(content);
            }
            case "&sbrowser": {
                Sessions sessions = new Sessions(this.client, true);
                JComponent content = sessions.getContent();
                DialogUtils.setupScreenshotShortcut(this.client, sessions.getTable(), "Sessions");
                return SleepUtils.getScalar(content);
            }
            default:
                break;
        }
        return SleepUtils.getEmptyScalar();
    }

}

