package aggressor.browsers;

import aggressor.AggressorClient;
import common.AObject;
import common.AdjustData;
import common.CommonUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import dialog.SafeDialogs;
import filter.DataFilter;
import ui.ATable;
import ui.GenericTableModel;
import ui.TablePopup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.Map;

public class Credentials
        extends AObject implements ActionListener,
        AdjustData,
        TablePopup {
    protected AggressorClient client;
    protected ActivityPanel dialog = null;
    protected GenericTableModel model = null;
    protected ATable table = null;
    protected String[] cols = new String[]{"user", "password", "realm", "note", "source", "host"};
    protected DataFilter filter = new DataFilter();

    public void setColumns(String colz) {
        this.cols = CommonUtils.toArray(colz);
    }

    public void noHashes() {
        this.filter.checkNTLMHash("password", true);
    }

    public DataFilter getFilter() {
        return this.filter;
    }

    public Credentials(AggressorClient client) {
        this.client = client;
    }

    public ActionListener cleanup() {
        return this.client.getData().unsubOnClose("credentials", this);
    }

    public WindowListener onclose() {
        return this.client.getData().unsubOnClose("credentials", this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Set Realm...".equals(ev.getActionCommand())) {
            SafeDialogs.ask("Set Domain to:", "", r -> {
                Map[] options = Credentials.this.model.getSelectedRows(Credentials.this.table);
                for (Map option : options) {
                    Credentials.this.client.getConnection().call("credentials.remove", CommonUtils.args(CommonUtils.CredKey(option)));
                    option.put("realm", r);
                    Credentials.this.client.getConnection().call("credentials.add", CommonUtils.args(CommonUtils.CredKey(option), option));
                }
                Credentials.this.client.getConnection().call("credentials.push");
            });
        } else if ("Set Note...".equals(ev.getActionCommand())) {
            SafeDialogs.ask("Set Note to:", "", r -> {
                Map[] options = Credentials.this.model.getSelectedRows(Credentials.this.table);
                for (Map option : options) {
                    option.put("note", r);
                    Credentials.this.client.getConnection().call("credentials.add", CommonUtils.args(CommonUtils.CredKey(option), option));
                }
                Credentials.this.client.getConnection().call("credentials.push");
            });
        }
    }

    @Override
    public Map<String, Object> format(String key, Object value) {
        if (!this.filter.test((Map) value)) {
            return null;
        }
        return (Map) value;
    }

    public JComponent getContent() {
        LinkedList creds = this.client.getData().populateListAndSubscribe("credentials", this);
        this.model = DialogUtils.setupModel("user", this.cols, creds);
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        this.table.setPopupMenu(this);
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
        JMenuItem a = new JMenuItem("Set Realm...");
        JMenuItem b = new JMenuItem("Set Note...");
        menu.add(a);
        menu.add(b);
        a.addActionListener(this);
        b.addActionListener(this);
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

