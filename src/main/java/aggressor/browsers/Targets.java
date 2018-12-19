package aggressor.browsers;

import aggressor.AggressorClient;
import common.AObject;
import common.AdjustData;
import common.BeaconEntry;
import common.CommonUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import sleep.runtime.Scalar;
import ui.ATable;
import ui.GenericTableModel;
import ui.TablePopup;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.*;

public class Targets
        extends AObject implements AdjustData,
        TablePopup {
    protected AggressorClient client;
    protected ActivityPanel dialog = null;
    protected GenericTableModel model = null;
    protected ATable table = null;
    protected String[] cols = new String[]{" ", "address", "name", "note"};
    protected LinkedList targets = new LinkedList();
    protected Set compromised = new HashSet();

    public Targets(AggressorClient client) {
        this.client = client;
    }

    public ATable getTable() {
        return this.table;
    }

    public ActionListener cleanup() {
        return this.client.getData().unsubOnClose("targets, beacons", this);
    }

    @Override
    public Map<String, Object> format(String key, Object value) {
        HashMap<String, Object> temp = new HashMap<String, Object>((Map) value);
        boolean hacked = this.compromised.contains(temp.get("address"));
        ImageIcon viz = DialogUtils.TargetVisualizationSmall(temp.get("os") + "", CommonUtils.toDoubleNumber(temp.get("version") + "", 0.0), hacked, false);
        temp.put("image", viz);
        temp.put("owned", hacked ? Boolean.TRUE : Boolean.FALSE);
        return temp;
    }

    public JComponent getContent() {
        this.client.getData().subscribe("beacons", this);
        this.targets = this.client.getData().populateListAndSubscribe("targets", this);
        this.model = DialogUtils.setupModel("address", this.cols, this.targets);
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        this.table.setPopupMenu(this);
        DialogUtils.sortby(this.table, 1);
        Map widths = DialogUtils.toMap("address: 125, name: 125, note: 625");
        DialogUtils.setTableColumnWidths(this.table, widths);
        this.table.getColumn(" ").setPreferredWidth(32);
        this.table.getColumn(" ").setMaxWidth(32);
        DialogUtils.setupImageRenderer(this.table, this.model, " ", "image");
        DialogUtils.setupBoldOnKeyRenderer(this.table, this.model, "address", "owned");
        DialogUtils.setupBoldOnKeyRenderer(this.table, this.model, "name", "owned");
        DialogUtils.setupBoldOnKeyRenderer(this.table, this.model, "note", "owned");
        return DialogUtils.FilterAndScroll(this.table);
    }

    public Map[] getSelectedRows() {
        return this.model.getSelectedRows(this.table);
    }

    @Override
    public void showPopup(MouseEvent ev) {
        Stack<Scalar> args = new Stack<>();
        args.push(CommonUtils.toSleepArray(this.model.getSelectedValues(this.table)));
        this.client.getScriptEngine().getMenuBuilder().installMenu(ev, "targets", args);
    }

    public void refresh() {
        this.targets = CommonUtils.apply("targets", this.targets, this);
        DialogUtils.setTable(this.table, this.model, this.targets);
    }

    @Override
    public void result(String key, Object o) {
        if ("targets".equals(key)) {
            this.targets = new LinkedList((LinkedList) o);
            this.refresh();
            if (this.dialog != null) {
                this.dialog.touch();
            }
        } else if ("beacons".equals(key)) {
            HashSet updates = new HashSet();

            for (Object o1 : ((Map) o).values()) {
                BeaconEntry entry = (BeaconEntry) o1;
                if (entry.isAlive()) {
                    updates.add(entry.getInternal());
                }
            }

            if (!updates.equals(this.compromised)) {
                this.compromised = updates;
                this.refresh();
            }
        }

    }

    public void notifyOnResult(ActivityPanel p) {
        this.dialog = p;
    }
}

