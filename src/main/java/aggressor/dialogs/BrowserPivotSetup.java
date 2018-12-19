package aggressor.dialogs;

import aggressor.AggressorClient;
import beacon.TaskBeacon;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import ui.ATable;
import ui.GenericTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class BrowserPivotSetup
        extends AObject implements Callback,
        DialogListener {
    protected String bid;
    protected AggressorClient client;
    protected JFrame dialog = null;
    protected GenericTableModel model;
    protected ATable table = null;
    protected String[] cols = new String[]{"PID", "PPID", "Arch", "Name", "User", " "};

    public BrowserPivotSetup(AggressorClient client, String bid) {
        this.client = client;
        this.bid = bid;
        this.model = DialogUtils.setupModel("PID", this.cols, new LinkedList<>());
    }

    public void refresh() {
        this.client.getConnection().call("beacons.task_ps", CommonUtils.args(this.bid), this);
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        int pid = Integer.parseInt(this.model.getSelectedValueFromColumn(this.table, "PID") + "");
        String arch = this.model.getSelectedValueFromColumn(this.table, "Arch") + "";
        int port = DialogUtils.number(options, "ProxyPort");
        TaskBeacon session = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
        DialogUtils.openOrActivate(this.client, this.bid);
        session.input("browserpivot " + pid + " " + arch);
        session.BrowserPivot(this.bid, pid, arch, port);
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Browser Pivot", 680, 240);
        this.dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        Map<String,String> colz = DialogUtils.toMap("PID: 60, PPID: 60, Arch: 60, Name: 120, User: 240");
        colz.put(" ", "20");
        DialogUtils.setTableColumnWidths(this.table, colz);
        JScrollPane scroll = new JScrollPane(this.table);
        scroll.setPreferredSize(new Dimension(scroll.getWidth(), 100));
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("ProxyPort", CommonUtils.randomPort() + "");
        controller.text("ProxyPort", "Proxy Server Port:");
        JButton chooser = controller.action("Launch");
        JButton help = controller.help("https://www.cobaltstrike.com/help-browser-pivoting");
        JPanel middle = new JPanel();
        middle.setLayout(new BorderLayout());
        middle.add(controller.row(), "North");
        middle.add(DialogUtils.center(chooser, help), "South");
        this.dialog.add(scroll, "Center");
        this.dialog.add(middle, "South");
        this.refresh();
        this.dialog.setVisible(true);
    }

    @Override
    public void result(String key, Object o) {
        LinkedList<Map<String, Object>> results = new LinkedList<>();
        HashMap<String, String> pidToName = new HashMap<>();
        String[] rows = o.toString().trim().split("\n");
        for (String row : rows) {
            String name;
            String[] cols = row.split("\t");
            HashMap<String, Object> next = new HashMap<>();
            if (cols.length >= 1) {
                next.put("Name", cols[0]);
            }
            if (cols.length >= 2) {
                next.put("PPID", cols[1]);
            }
            if (cols.length >= 3) {
                next.put("PID", cols[2]);
            }
            if (cols.length >= 4) {
                next.put("Arch", cols[3]);
            }
            if (cols.length >= 5) {
                next.put("User", cols[4]);
            }
            if (cols.length >= 6) {
                next.put("Session", cols[5]);
            }
            if (cols.length >= 3) {
                pidToName.put(cols[2], cols[0].toLowerCase());
            }
            if (!(name = (next.get("Name") + "").toLowerCase()).equals("explorer.exe") && !name.equals("iexplore.exe"))
                continue;
            results.add(next);
        }
        for (Map<String, Object> result : results) {
            String ppid = result.get("PPID") + "";
            String pname = pidToName.get(ppid) + "";
            if (!"iexplore.exe".equals(pname)) continue;
            result.put(" ", '\u2713');
        }
        DialogUtils.setTable(this.table, this.model, results);
    }
}

