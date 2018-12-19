package aggressor.windows;

import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.dialogs.ListenerDialog;
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
import java.util.HashMap;
import java.util.Map;

public class ListenerManager
        extends AObject implements Callback,
        ActionListener {
    protected TeamQueue conn;
    protected Cortana engine;
    protected DataManager data;
    protected GenericTableModel model;
    protected ATable table = null;
    protected String[] cols = new String[]{"name", "payload", "host", "port", "beacons"};

    public ListenerManager(DataManager data, Cortana engine, TeamQueue conn) {
        this.engine = engine;
        this.conn = conn;
        this.data = data;
        this.model = DialogUtils.setupModel("name", this.cols, DataUtils.getListenerModel(data));
        data.subscribe("listeners", this);
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("listeners", this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        block3:
        {
            block5:
            {
                block4:
                {
                    block2:
                    {
                        if (!"Add".equals(ev.getActionCommand())) break block2;
                        new ListenerDialog(this.conn, this.data).show();
                        break block3;
                    }
                    if (!"Edit".equals(ev.getActionCommand())) break block4;
                    Map listeners = (Map) this.data.get("listeners", new HashMap());
                    String name = this.model.getSelectedValue(this.table) + "";
                    new ListenerDialog(this.conn, this.data, (HashMap<String,Object>) listeners.get(name)).show();
                    break block3;
                }
                if (!"Remove".equals(ev.getActionCommand())) break block5;
                Object[] names = this.model.getSelectedValues(this.table);
                for (Object name : names) {
                    this.conn.call("listeners.remove", CommonUtils.args(name));
                }
                break block3;
            }
            if (!"Restart".equals(ev.getActionCommand())) break block3;
            Object[] names = this.model.getSelectedValues(this.table);
            for (Object name : names) {
                this.conn.call("listeners.restart", CommonUtils.args(name), (key, o) -> DialogUtils.showInfo("Updated and restarted listener: " + o));
            }
        }
    }

    public JComponent getContent() {
        JPanel dialog = new JPanel();
        dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.setupListenerStatusRenderer(this.table, this.model, "name");
        DialogUtils.setTableColumnWidths(this.table, DialogUtils.toMap("name: 125, payload: 250, host: 125, port: 60, beacons: 250"));
        JButton add = new JButton("Add");
        JButton edit = new JButton("Edit");
        JButton del = new JButton("Remove");
        JButton restart = new JButton("Restart");
        JButton help = new JButton("Help");
        add.addActionListener(this);
        edit.addActionListener(this);
        del.addActionListener(this);
        restart.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-listener-management"));
        dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        dialog.add(DialogUtils.center(add, edit, del, restart, help), "South");
        return dialog;
    }

    @Override
    public void result(String key, Object o) {
        DialogUtils.setTable(this.table, this.model, ((Map) o).values());
    }

}

