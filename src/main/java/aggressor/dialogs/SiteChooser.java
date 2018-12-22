package aggressor.dialogs;

import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.GenericDataManager;
import aggressor.GlobalDataManager;
import common.AObject;
import common.AdjustData;
import common.CommonUtils;
import common.TeamQueue;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import ui.ATable;
import ui.GenericTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class SiteChooser
        extends AObject implements AdjustData,
        ActionListener {
    protected JFrame dialog = null;
    protected TeamQueue conn;
    protected SafeDialogCallback callback;
    protected GenericDataManager data = GlobalDataManager.getGlobalDataManager();
    protected DataManager datal;
    protected GenericTableModel model;
    protected ATable table = null;
    protected String[] cols = new String[]{"Host", "URI", "Port", "Type", "Description"};

    public SiteChooser(TeamQueue conn, DataManager datal, SafeDialogCallback callback) {
        this.conn = conn;
        this.callback = callback;
        this.datal = datal;
        this.model = DialogUtils.setupModel("URI", this.cols, CommonUtils.apply("sites", DataUtils.getSites(this.data), this));
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        String uri = (String) this.model.getSelectedValue(this.table);
        String port = (String) this.model.getSelectedValueFromColumn(this.table, "Port");
        String host = (String) this.model.getSelectedValueFromColumn(this.table, "Host");
        String proto = (String) this.model.getSelectedValueFromColumn(this.table, "Proto");
        String url = proto + host + ":" + port + uri;
        this.dialog.setVisible(false);
        this.dialog.dispose();
        this.callback.dialogResult(url);
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Choose a site", 640, 240);
        this.dialog.setLayout(new BorderLayout());
        this.dialog.addWindowListener(this.data.unsubOnClose("sites", this));
        this.table = DialogUtils.setupTable(this.model, this.cols, false);
        DialogUtils.setTableColumnWidths(this.table, DialogUtils.toMap("Host: 125, URI: 125, Port: 60, Type: 60, Description: 250"));
        JButton choose = new JButton("Choose");
        choose.addActionListener(this);
        this.dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        this.dialog.add(DialogUtils.center(choose), "South");
        this.dialog.setVisible(true);
    }

    @Override
    public Map<String, Object> format(String key, Object value) {
        Map next = (Map) value;
        if ("".equals(next.get("Host"))) {
            return null;
        }
        if ("beacon".equals(next.get("Type"))) {
            return null;
        }
        return next;
    }

    @Override
    public void result(String key, Object o) {
    }
}

