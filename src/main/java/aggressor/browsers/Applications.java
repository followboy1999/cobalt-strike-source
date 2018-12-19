package aggressor.browsers;

import aggressor.AggressorClient;
import common.AObject;
import common.AdjustData;
import common.CommonUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import dialog.SafeDialogs;
import ui.ATable;
import ui.GenericTableModel;
import ui.TablePopup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Applications
        extends AObject implements ActionListener,
        AdjustData,
        TablePopup {
    protected AggressorClient client;
    protected ActivityPanel dialog = null;
    protected GenericTableModel model = null;
    protected ATable table = null;
    protected String[] cols = new String[]{" ", "external", "internal", "application", "version", "note", "date"};
    protected boolean nohashes = false;

    public Applications(AggressorClient client) {
        this.client = client;
    }

    public ActionListener cleanup() {
        return this.client.getData().unsubOnClose("applications", this);
    }

    public WindowListener onclose() {
        return this.client.getData().unsubOnClose("applications", this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Set Note...".equals(ev.getActionCommand())) {
            SafeDialogs.ask("Set Note to:", "", r -> {
                Map[] options = Applications.this.model.getSelectedRows(Applications.this.table);
                for (Map option : options) {
                    option.put("note", r);
                    Applications.this.client.getConnection().call("applications.update", CommonUtils.args(CommonUtils.ApplicationKey(option), option));
                }
                Applications.this.client.getConnection().call("applications.push");
            });
        }
    }

    @Override
    public Map<String, Object> format(String key, Object value) {
        HashMap<String, Object> temp = new HashMap<String, Object>((Map) value);
        String os = temp.get("os") + "";
        String osver = temp.get("osver") + "";
        ImageIcon viz = DialogUtils.TargetVisualizationSmall(os, CommonUtils.toDoubleNumber(osver, 0.0), false, false);
        temp.put("image", viz);
        return temp;
    }

    public JComponent getContent() {
        LinkedList creds = this.client.getData().populateListAndSubscribe("applications", this);
        this.model = DialogUtils.setupModel("nonce", this.cols, creds);
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        this.table.setPopupMenu(this);
        this.table.getColumn(" ").setPreferredWidth(32);
        this.table.getColumn(" ").setMaxWidth(32);
        DialogUtils.setupImageRenderer(this.table, this.model, " ", "image");
        DialogUtils.setupDateRenderer(this.table, "date");
        DialogUtils.sortby(this.table, 6);
        return DialogUtils.FilterAndScroll(this.table);
    }

    public JTable getTable() {
        return this.table;
    }

    public Object getSelectedValueFromColumn(String col) {
        return this.model.getSelectedValueFromColumn(this.table, col);
    }

    public Map[] getSelectedRows() {
        return this.model.getSelectedRows(this.table);
    }

    @Override
    public void showPopup(MouseEvent ev) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem b = new JMenuItem("Set Note...");
        b.addActionListener(this);
        menu.add(b);
        menu.show((Component) ev.getSource(), ev.getX(), ev.getY());
    }

    public void notifyOnResult(ActivityPanel p) {
        this.dialog = p;
    }

    @Override
    public void result(String key, Object o) {
        LinkedList values = CommonUtils.apply(key, (LinkedList) o, this);
        DialogUtils.setTable(this.table, this.model, values);
        if (this.dialog != null) {
            this.dialog.touch();
        }
    }

}

