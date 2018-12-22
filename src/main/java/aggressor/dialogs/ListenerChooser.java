package aggressor.dialogs;

import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.GenericDataManager;
import aggressor.GlobalDataManager;
import common.AObject;
import common.Callback;
import common.TeamQueue;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import ui.ATable;
import ui.GenericTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ListenerChooser
        extends AObject implements Callback,
        ActionListener {
    protected JFrame dialog = null;
    protected TeamQueue conn;
    protected SafeDialogCallback callback;
    protected GenericDataManager data = GlobalDataManager.getGlobalDataManager();
    protected DataManager datal;
    protected GenericTableModel model;
    protected ATable table = null;
    protected String[] cols = new String[]{"name", "payload", "host", "port"};

    public ListenerChooser(TeamQueue conn, DataManager datal, SafeDialogCallback callback) {
        this.conn = conn;
        this.callback = callback;
        this.datal = datal;
        this.model = DialogUtils.setupModel("name", this.cols, DataUtils.getUsableListenerModel(this.data, datal));
        this.data.subscribe("listeners", this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Add".equals(ev.getActionCommand())) {
            new ListenerDialog(this.conn, this.datal).show();
        } else if ("Choose".equals(ev.getActionCommand())) {
            String name = (String) this.model.getSelectedValue(this.table);
            this.data.unsub("listeners", this);
            this.dialog.setVisible(false);
            this.dialog.dispose();
            if (name != null) {
                this.callback.dialogResult(name);
            }
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Choose a listener", 640, 240);
        this.dialog.setLayout(new BorderLayout());
        this.dialog.addWindowListener(this.data.unsubOnClose("listeners", this));
        this.table = DialogUtils.setupTable(this.model, this.cols, false);
        DialogUtils.setTableColumnWidths(this.table, DialogUtils.toMap("name: 125, payload: 250, host: 125, port: 60"));
        JButton choose = new JButton("Choose");
        JButton add = new JButton("Add");
        JButton help = new JButton("Help");
        choose.addActionListener(this);
        add.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-listener-management"));
        this.dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        this.dialog.add(DialogUtils.center(choose, add, help), "South");
        this.dialog.setVisible(true);
    }

    @Override
    public void result(String key, Object o) {
        DialogUtils.setTable(this.table, this.model, DataUtils.getUsableListenerModel(this.data, this.datal));
    }
}

