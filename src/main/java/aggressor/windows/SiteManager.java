package aggressor.windows;

import aggressor.DataManager;
import aggressor.DataUtils;
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

public class SiteManager
        extends AObject implements Callback,
        ActionListener {
    protected TeamQueue conn;
    protected Cortana engine;
    protected DataManager data;
    protected GenericTableModel model;
    protected ATable table = null;
    protected String[] cols = new String[]{"URI", "Host", "Port", "Type", "Description"};

    public SiteManager(DataManager data, Cortana engine, TeamQueue conn) {
        this.engine = engine;
        this.conn = conn;
        this.data = data;
        this.model = DialogUtils.setupModel("URI", this.cols, DataUtils.getSites(data));
        data.subscribe("sites", this);
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("sites", this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Kill".equals(ev.getActionCommand())) {
            Object[][] all = this.model.getSelectedValuesFromColumns(this.table, CommonUtils.toArray("URI, Port"));
            for (Object[] anAll : all) {
                String URI2 = anAll[0] + "";
                String port = anAll[1] + "";
                this.conn.call("cloudstrike.kill_site", CommonUtils.args(port, URI2));
            }
        } else if ("Copy URL".equals(ev.getActionCommand())) {
            String uri = this.model.getSelectedValue(this.table) + "";
            String host = this.model.getSelectedValueFromColumn(this.table, "Host") + "";
            String port = this.model.getSelectedValueFromColumn(this.table, "Port") + "";
            String proto = this.model.getSelectedValueFromColumn(this.table, "Proto") + "";
            String url = proto + host + ":" + port + uri;
            String desc = this.model.getSelectedValueFromColumn(this.table, "Description") + "";
            if ("PowerShell Web Delivery".equals(desc)) {
                DialogUtils.addToClipboard(CommonUtils.PowerShellOneLiner(url));
            } else if (desc.startsWith("Scripted Web Delivery (") && desc.endsWith(")")) {
                String type = desc;
                type = CommonUtils.strrep(type, "Scripted Web Delivery (", "");
                type = CommonUtils.strrep(type, ")", "");
                DialogUtils.addToClipboard(CommonUtils.OneLiner(url, type));
            } else {
                DialogUtils.addToClipboard(url);
            }
        }
    }

    public JComponent getContent() {
        JPanel dialog = new JPanel();
        dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.setTableColumnWidths(this.table, DialogUtils.toMap("URI: 125, Host: 125, Port: 60, Type: 60, Description: 250"));
        JButton kill = new JButton("Kill");
        JButton copy2 = new JButton("Copy URL");
        JButton help = new JButton("Help");
        kill.addActionListener(this);
        copy2.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-manage-sites"));
        dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        dialog.add(DialogUtils.center(copy2, kill, help), "South");
        return dialog;
    }

    @Override
    public void result(String key, Object o) {
        DialogUtils.setTable(this.table, this.model, (LinkedList) o);
    }
}

