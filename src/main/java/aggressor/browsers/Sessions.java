package aggressor.browsers;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.AObject;
import common.BeaconEntry;
import common.Callback;
import common.CommonUtils;
import dialog.DialogUtils;
import ui.ATable;
import ui.GenericTableModel;
import ui.TablePopup;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.Map;

public class Sessions extends AObject implements Callback, TablePopup {
    protected AggressorClient client;
    protected GenericTableModel model = null;
    protected ATable table = null;
    protected String[] cols = new String[]{" ", "external", "internal", "user", "computer", "note", "pid", "last"};
    protected boolean multipleSelect;

    public ATable getTable() {
        return this.table;
    }

    public void setColumns(String colz) {
        this.cols = CommonUtils.toArray(colz);
    }

    public Sessions(AggressorClient client, boolean multipleSelect) {
        this.client = client;
        this.multipleSelect = multipleSelect;
    }

    public ActionListener cleanup() {
        return this.client.getData().unsubOnClose("beacons", this);
    }

    public WindowListener onclose() {
        return this.client.getData().unsubOnClose("beacons", this);
    }

    public Object[] getSelectedValues() {
        return this.model.getSelectedValues(this.table);
    }

    public Object getSelectedValue() {
        return this.model.getSelectedValue(this.table) + "";
    }

    @Override
    public void showPopup(MouseEvent ev) {
        DialogUtils.showSessionPopup(this.client, ev, this.model.getSelectedValues(this.table));
    }

    public JComponent getContent() {
        this.model = this.cols.length == 8 ? DialogUtils.setupModel("id", this.cols, new LinkedList<>()) : DialogUtils.setupModel("id", this.cols, DataUtils.getBeaconModel(this.client.getData()));
        this.table = DialogUtils.setupTable(this.model, this.cols, this.multipleSelect);
        if (this.cols.length == 8) {
            DialogUtils.sortby(this.table, 2, 6);
            this.table.getColumn(" ").setPreferredWidth(32);
            this.table.getColumn(" ").setMaxWidth(32);
            DialogUtils.setupImageRenderer(this.table, this.model, " ", "image");
        } else {
            DialogUtils.sortby(this.table, 1);
        }
        DialogUtils.setupTimeRenderer(this.table, "last");
        this.table.setPopupMenu(this);
        this.client.getData().subscribe("beacons", this);
        return DialogUtils.FilterAndScroll(this.table);
    }

    @Override
    public void result(String key, Object o) {
        Map<String, BeaconEntry> resultz = (Map<String, BeaconEntry>) o;
        if (!this.table.isShowing()) {
            return;
        }
        DialogUtils.setTable(this.table, this.model, DataUtils.getBeaconModelFromResult(resultz));
    }
}

