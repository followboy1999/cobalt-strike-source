package aggressor.windows;

import aggressor.DataManager;
import common.*;
import cortana.Cortana;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;
import ui.ATable;
import ui.GenericTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.Map;

public class DownloadBrowser extends AObject implements AdjustData, ActionListener, SafeDialogCallback {
    protected TeamQueue conn;
    protected Cortana engine;
    protected DataManager data;
    protected ActivityPanel dialog = null;
    protected GenericTableModel model = null;
    protected ATable table = null;
    protected String[] cols = new String[]{"host", "name", "path", "size", "date"};

    public DownloadBrowser(DataManager data, Cortana engine, TeamQueue conn) {
        this.engine = engine;
        this.conn = conn;
        this.data = data;
    }

    public ActionListener cleanup() {
        return this.data.unsubOnClose("downloads", this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        SafeDialogs.openFile("Sync downloads to?", null, null, false, true, this);
    }

    @Override
    public void dialogResult(String r) {
        if (r == null) {
            return;
        }
        new DownloadFiles(this.conn, this.model.getSelectedRows(this.table), new File(r)).startNextDownload();
    }

    public JComponent getContent() {
        LinkedList<Map<String,Object>> history = this.data.populateAndSubscribe("downloads", this);
        this.model = DialogUtils.setupModel("lpath", this.cols, history);
        this.dialog = new ActivityPanel();
        this.dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.setupDateRenderer(this.table, "date");
        DialogUtils.setupSizeRenderer(this.table, "size");
        JButton sync = new JButton("Sync Files");
        JButton help = new JButton("Help");
        sync.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-manage-downloads"));
        this.dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        this.dialog.add(DialogUtils.center(sync, help), "South");
        return this.dialog;
    }

    @Override
    public Map<String, Object> format(String key, Object o) {
        Download temp = (Download) o;
        return temp.toMap();
    }

    @Override
    public void result(String key, Object o) {
        DialogUtils.addToTable(this.table, this.model, this.format(key, o));
        this.dialog.touch();
    }
}

