package aggressor;

import aggressor.bridges.*;
import beacon.BeaconCommands;
import beacon.BeaconExploits;
import beacon.SecureShellCommands;
import common.*;
import console.Activity;
import cortana.Cortana;
import cortana.gui.ScriptableApplication;
import report.ReportEngine;
import ui.KeyBindings;
import ui.KeyHandler;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Stack;

public class AggressorClient
        extends JComponent implements ScriptableApplication,
        DisconnectListener,
        Callback {
    protected KeyBindings keys = new KeyBindings();
    protected JMenuBar menu = new JMenuBar();
    protected TabManager tabs = null;
    protected Cortana engine = null;
    protected JSplitPane split = null;
    protected String title = "Cobalt Strike";
    protected MultiFrame window = null;
    protected JSplitPane split2 = null;
    protected JToolBar tool = new JToolBar();
    protected TeamQueue conn = null;
    protected DataManager data = null;
    protected ReportEngine reports = null;
    protected AliasManager aliases = null;
    protected boolean connected = true;
    protected SecureShellAliasManager ssh_aliases = null;
    protected JPanel viz = new JPanel();
    protected CardLayout viz_c = new CardLayout();

    public String getTitle() {
        return this.title;
    }

    public MultiFrame getWindow() {
        return this.window;
    }

    public ReportEngine getReportEngine() {
        return this.reports;
    }

    public void showViz(String name) {
        this.viz_c.show(this.viz, name);
        this.viz.validate();
    }

    public void addViz(String name, JComponent component) {
        this.viz.add(component, name);
    }

    public void setTitle(String title) {
        this.window.setTitle(this, title);
    }

    public Cortana getScriptEngine() {
        return this.engine;
    }

    public TabManager getTabManager() {
        return this.tabs;
    }

    public TeamQueue getConnection() {
        return this.conn;
    }

    public DataManager getData() {
        return this.data;
    }

    public KeyBindings getBindings() {
        return this.keys;
    }

    public SecureShellAliasManager getSSHAliases() {
        return this.ssh_aliases;
    }

    public AliasManager getAliases() {
        return this.aliases;
    }

    @Override
    public void bindKey(String combination, KeyHandler action) {
        this.keys.bind(combination, action);
    }

    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public JMenuBar getJMenuBar() {
        return this.menu;
    }

    public void touch() {
        Component c = this.tabs.getTabbedPane().getSelectedComponent();
        if (c == null) {
            return;
        }
        if (c instanceof Activity) {
            ((Activity) c).resetNotification();
        }
        c.requestFocusInWindow();
    }

    public void kill() {
        CommonUtils.print_info("shutting down client");
        this.engine.getEventManager().stop();
        this.engine.stop();
        this.conn.close();
    }

    @Override
    public void disconnected(TeamSocket s) {
        this.disconnected();
    }

    public void disconnected() {
        JButton close = new JButton("Close");
        close.addActionListener(ev -> AggressorClient.this.window.quit());
        JPanel announce = new JPanel();
        announce.setLayout(new BorderLayout());
        announce.setBackground(Color.RED);
        announce.add(new JLabel("<html><body><strong>Disconnected from server</strong></body></html>"), "Center");
        announce.add(close, "East");
        announce.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        this.add(announce, "South");
        this.revalidate();
        this.getData().dead();
        this.connected = false;
    }

    public void dock(JComponent component, Dimension size) {
        this.split2.setBottomComponent(component);
        this.split2.setDividerSize(10);
        this.split2.setResizeWeight(1.0);
        component.setPreferredSize(size);
        component.setSize(size);
        this.validate();
    }

    public void noDock() {
        this.split2.setBottomComponent(null);
        this.split2.setDividerSize(0);
        this.split2.setResizeWeight(1.0);
        this.validate();
    }

    @Override
    public void result(String key, Object o) {
        if ("server_error".equals(key)) {
            CommonUtils.print_error("Server error: " + o);
        }
    }

    public void loadScripts() {
        try {
            this.engine.loadScript("scripts/default.cna", CommonUtils.resource("scripts/default.cna"));
        } catch (Exception ex) {
            MudgeSanity.logException("Loading scripts/default.cna", ex, false);
        }
        for (Object o : Prefs.getPreferences().getList("cortana.scripts")) {
            String scriptf = (String) o;
            try {
                this.engine.loadScript(scriptf);
            } catch (Exception ex) {
                MudgeSanity.logException("Loading " + scriptf, ex, true);
            }
        }
    }

    public AggressorClient(MultiFrame window, TeamQueue conn, Map metadata, Map options) {
        this.setup(window, conn, metadata, options);
    }

    @Override
    public boolean isHeadless() {
        return false;
    }

    public AggressorClient() {
    }

    public void setup(MultiFrame window, TeamQueue conn, Map metadata, Map options) {
        this.window = window;
        this.conn = conn;
        conn.addDisconnectListener(this);
        this.tabs = new TabManager(this);
        this.engine = new Cortana(this);
        this.reports = new ReportEngine(this);
        this.aliases = new AliasManager();
        this.ssh_aliases = new SecureShellAliasManager();
        this.engine.register(new TabBridge(this.engine, this.tabs));
        this.engine.register(new GraphBridge(this.engine, this.tabs));
        this.engine.register(new AggressorBridge(this, this.engine, this.tabs, window, conn));
        this.engine.register(new ToolBarBridge(this.tool));
        this.engine.register(new TeamServerBridge(this.engine, conn));
        this.engine.register(new DataBridge(this, this.engine, conn));
        this.engine.register(new BeaconBridge(this, this.engine, conn));
        this.engine.register(new BeaconTaskBridge(this));
        this.engine.register(new ElevateBridge(this));
        this.engine.register(new UtilityBridge(this));
        this.engine.register(new ReportingBridge(this));
        this.engine.register(new EventLogBridge(this));
        this.engine.register(new SafeDialogBridge(this));
        this.engine.register(new PreferencesBridge(this));
        this.engine.register(new ListenerBridge(this));
        this.engine.register(new CovertVPNBridge(this));
        this.engine.register(new ArtifactBridge(this));
        this.engine.register(new DialogBridge(this));
        this.engine.register(new SiteBridge(this));
        this.engine.register(new AttackBridge());
        this.engine.register(this.aliases.getBridge());
        this.engine.register(this.ssh_aliases.getBridge());
        this.reports.registerInternal("scripts/default.rpt");
        this.data = new DataManager(this.engine);
        this.data.put("metadata", metadata);
        this.data.put("options", options);
        this.data.put("beacon_commands", new BeaconCommands());
        this.data.put("beacon_exploits", new BeaconExploits());
        DataUtils.getBeaconExploits(this.data).registerDefaults(this);
        this.data.put("ssh_commands", new SecureShellCommands());
        conn.setSubscriber(this.data);
        this.data.subscribe("server_error", this);
        this.loadScripts();
        this.viz.setLayout(this.viz_c);
        this.split2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.tabs.getTabbedPane(), null);
        this.split2.setDividerSize(0);
        this.split2.setOneTouchExpandable(true);
        this.split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.viz, this.split2);
        this.split.setOneTouchExpandable(true);
        this.tool.setFloatable(false);
        this.tool.add(Box.createHorizontalGlue());
        this.add(this.split, "Center");
        if (Prefs.getPreferences().isSet("client.toolbar.boolean", true)) {
            JPanel top = new JPanel();
            top.setLayout(new BorderLayout());
            top.add(this.menu, "North");
            top.add(this.tool, "Center");
            this.setLayout(new BorderLayout());
            this.add(top, "North");
            this.add(this.split, "Center");
        } else {
            this.setLayout(new BorderLayout());
            this.add(this.menu, "North");
            this.add(this.split, "Center");
        }
        if (!this.isHeadless()) {
            new SyncMonitor(this);
        }
        this.getData().subscribe("playback.status", (key, value) -> {
            PlaybackStatus status = (PlaybackStatus) value;
            if (status.isDone()) {
                if (AggressorClient.this.isHeadless()) {
                    GlobalDataManager.getGlobalDataManager().wait(AggressorClient.this.getData());
                }
                AggressorClient.this.engine.getEventManager().fireEvent("ready", new Stack());
                AggressorClient.this.engine.go();
            }
        });
        conn.call("aggressor.ready");
    }

    public void showTime() {
        CommonUtils.Guard();
        this.split.setDividerLocation(0.5);
    }

}

