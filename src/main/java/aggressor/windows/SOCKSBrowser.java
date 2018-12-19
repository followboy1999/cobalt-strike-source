package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.TabManager;
import beacon.BeaconPivot;
import common.*;
import cortana.Cortana;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import ui.ATable;
import ui.GenericTableModel;
import ui.TablePopup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SOCKSBrowser
        extends AObject implements AdjustData,
        ActionListener,
        TablePopup {
    protected TeamQueue conn;
    protected Cortana engine;
    protected DataManager data;
    protected TabManager manager;
    protected AggressorClient client;
    protected ActivityPanel dialog = null;
    protected GenericTableModel model = null;
    protected ATable table = null;
    protected String[] cols = new String[]{"user", "computer", "pid", "type", "port", "fhost", "fport"};

    public SOCKSBrowser(AggressorClient client) {
        this.client = client;
        this.engine = client.getScriptEngine();
        this.conn = client.getConnection();
        this.data = client.getData();
        this.manager = client.getTabManager();
    }

    @Override
    public Map<String, Object> format(String key, Object o) {
        Map<String, Object> temp = (Map) o;
        String bid = temp.get("bid") + "";
        BeaconEntry entry = DataUtils.getBeacon(this.data, bid);
        if (entry != null) {
            temp.put("user", entry.getUser());
            temp.put("computer", entry.getComputer());
            temp.put("pid", entry.getPid());
        }
        return temp;
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("socks", this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        block3:
        {
            BeaconPivot[] targets;
            block2:
            {
                targets = BeaconPivot.resolve(this.client, this.model.getSelectedRows(this.table));
                if (!"Stop".equals(ev.getActionCommand())) break block2;
                for (BeaconPivot target : targets) {
                    target.die();
                }
                break block3;
            }
            if (!"Tunnel".equals(ev.getActionCommand())) break block3;
            for (BeaconPivot target : targets) {
                target.tunnel();
            }
        }
    }

    @Override
    public void showPopup(MouseEvent ev) {
        DialogUtils.showSessionPopup(this.client, ev, this.model.getSelectedValues(this.table));
    }

    public JComponent getContent() {
        LinkedList<Map<String, Object>> temp = this.data.populateListAndSubscribe("socks", this);
        this.model = DialogUtils.setupModel("bid", this.cols, temp);
        this.dialog = new ActivityPanel();
        this.dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        this.table.setPopupMenu(this);
        JButton stop = new JButton("Stop");
        JButton tunnel = new JButton("Tunnel");
        JButton help = new JButton("Help");
        stop.addActionListener(this);
        tunnel.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-socks-proxy-pivoting"));
        this.dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        this.dialog.add(DialogUtils.center(stop, tunnel, help), "South");
        return this.dialog;
    }

    @Override
    public void result(String key, Object o) {
        LinkedList<Map<String, Object>> results = CommonUtils.apply(key, (List) o, this);
        DialogUtils.setTable(this.table, this.model, results);
        this.dialog.touch();
    }
}

