package aggressor.windows;

import aggressor.DataManager;
import aggressor.dialogs.InterfaceDialog;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import common.TeamQueue;
import cortana.Cortana;
import dialog.DialogUtils;
import ui.ATable;
import ui.GenericTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

public class InterfaceManager
        extends AObject implements Callback,
        ActionListener {
    protected TeamQueue conn;
    protected Cortana engine;
    protected DataManager data;
    protected GenericTableModel model;
    protected ATable table = null;
    protected String[] cols = new String[]{"interface", "channel", "port", "mac", "client", "tx", "rx"};

    public InterfaceManager(DataManager data, Cortana engine, TeamQueue conn) {
        this.engine = engine;
        this.conn = conn;
        this.data = data;
        this.model = DialogUtils.setupModel("interface", this.cols, new LinkedList());
        data.subscribe("interfaces", this);
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("interfaces", this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Add".equals(ev.getActionCommand())) {
            new InterfaceDialog(this.conn, this.data).show();
        } else if ("Remove".equals(ev.getActionCommand())) {
            String intf = this.model.getSelectedValue(this.table) + "";
            String channel = this.model.getSelectedValueFromColumn(this.table, "channel") + "";
            String port = this.model.getSelectedValueFromColumn(this.table, "port") + "";
            this.conn.call("cloudstrike.stop_tap", CommonUtils.args(intf));
            if ("TCP (Bind)".equals(channel)) {
                this.conn.call("beacons.pivot_stop_port", CommonUtils.args(port));
            }
        }
    }

    public JComponent getContent() {
        JPanel dialog = new JPanel();
        dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        JButton add = new JButton("Add");
        JButton del = new JButton("Remove");
        JButton help = new JButton("Help");
        add.addActionListener(this);
        del.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-covert-vpn"));
        dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        dialog.add(DialogUtils.center(add, del, help), "South");
        return dialog;
    }

    @Override
    public void result(String key, Object o) {
        DialogUtils.setTable(this.table, this.model, (List) o);
    }
}

