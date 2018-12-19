package aggressor.viz;

import aggressor.AggressorClient;
import common.*;
import dialog.DialogUtils;
import graph.GraphPopup;
import graph.NetworkGraph;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class PivotGraph extends AObject implements Callback, GraphPopup {
    protected AggressorClient client;
    protected NetworkGraph graph = new NetworkGraph();
    protected long last = 0L;
    protected Map external = new HashMap();

    public PivotGraph(AggressorClient _client) {
        this.client = _client;
        this.graph.setGraphPopup(this);
        this.graph.addActionForKey("ctrl pressed P", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                Image phear = PivotGraph.this.graph.getScreenshot();
                byte[] data = DialogUtils.toImage((BufferedImage) phear, "png");
                PivotGraph.this.client.getConnection().call("aggressor.screenshot", CommonUtils.args(new TabScreenshot("Pivot Graph", data)));
                DialogUtils.showInfo("Pushed screenshot to team server");
            }
        });
    }

    public void ready() {
        this.client.getData().subscribe("beacons", this);
    }

    @Override
    public void showGraphPopup(String[] nodes, MouseEvent ev) {
        if (nodes.length > 0) {
            DialogUtils.showSessionPopup(this.client, ev, nodes);
        } else {
            Stack<Scalar> args = new Stack<>();
            args.push(SleepUtils.getScalar(this.getContent()));
            this.client.getScriptEngine().getMenuBuilder().installMenu(ev, "pgraph", args);
        }
    }

    public void showPopup(MouseEvent ev) {
        Stack args = new Stack();
        this.client.getScriptEngine().getMenuBuilder().installMenu(ev, "beacon", args);
    }

    public JComponent getContent() {
        return this.graph;
    }

    public String getExternal(BeaconEntry entry) {
        String bid = entry.getId();
        if (this.external.containsKey(bid)) {
            return (String) this.external.get(bid);
        }
        if ("".equals(entry.getExternal())) {
            return "unknown";
        }
        this.external.put(bid, entry.getExternal());
        return entry.getExternal();
    }

    @Override
    public void result(String key, Object o) {
        if (!this.graph.isShowing()) {
            return;
        }
        Map beacons = (Map) o;
        long sessid = CommonUtils.dataIdentity(beacons);

        if (sessid == this.last) {
            return;
        }
        this.last = sessid;

        this.graph.start();

        Iterator i = beacons.values().iterator();
        while (i.hasNext()) {
            BeaconEntry entry = (BeaconEntry) i.next();
            if (!entry.isEmpty()) {
                Image myimage = DialogUtils.TargetVisualization(entry.getOperatingSystem().toLowerCase(), entry.getVersion(), entry.isAdmin(), !entry.isAlive());

                if (entry.isSSH()) {
                    if ("".equals(entry.getNote())) {
                        this.graph.addNode(entry.getId(), entry.getComputer(), entry.getUser(), myimage, entry.getInternal());
                    } else {
                        this.graph.addNode(entry.getId(), entry.getComputer() + "\n" + entry.getNote(), entry.getUser(), myimage, entry.getInternal());
                    }

                } else if ("".equals(entry.getNote())) {
                    this.graph.addNode(entry.getId(), entry.getComputer() + " @ " + entry.getPid(), entry.getUser(), myimage, entry.getInternal());
                } else {
                    this.graph.addNode(entry.getId(), entry.getComputer() + " @ " + entry.getPid() + "\n" + entry.getNote(), entry.getUser(), myimage, entry.getInternal());
                }

                if ((!entry.getInternal().equals(getExternal(entry))) && (entry.getParentId().length() == 0)) {
                    this.graph.addNode(getExternal(entry), getExternal(entry), "", DialogUtils.TargetVisualization("firewall", 0.0D, false, false), "");
                    this.graph.addEdge(getExternal(entry), entry.getId(), "#00FF00", "4", "true", "");
                }
            }
        }

        i = beacons.values().iterator();
        while (i.hasNext()) {
            BeaconEntry entry = (BeaconEntry) i.next();

            if (entry.getParentId().length() > 0) {
                if (entry.getLinkState() == 1) {
                    this.graph.addEdge(entry.getParentId(), entry.getId(), "#FFA500", "4", "false", "");
                } else {
                    this.graph.addEdge(entry.getParentId(), entry.getId(), "#FF0000", "4", "false", "DISCONNECTED");
                }
            }
        }

        this.graph.deleteNodes();
        this.graph.end();
    }


}

