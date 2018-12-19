package aggressor.browsers;

import aggressor.AggressorClient;
import common.AObject;
import common.AdjustData;
import common.CommonUtils;
import dialog.DialogUtils;
import sleep.runtime.Scalar;
import ui.ATable;
import ui.GenericTableModel;
import ui.TablePopup;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.*;

public class Services
        extends AObject implements AdjustData,
        TablePopup {
    protected AggressorClient client;
    protected GenericTableModel model = null;
    protected ATable table = null;
    protected String[] cols = new String[]{"address", "port", "banner", "note"};
    protected Set targets;

    public Services(AggressorClient client, String[] targets) {
        this.client = client;
        this.targets = CommonUtils.toSet(targets);
    }

    public ActionListener cleanup() {
        return this.client.getData().unsubOnClose("services", this);
    }

    @Override
    public Map<String, Object> format(String key, Object value) {
        Map<String, Object> temp = (Map) value;
        if (this.targets.contains(temp.get("address"))) {
            return temp;
        }
        return null;
    }

    public JComponent getContent() {
        LinkedList<Map<String, Object>> targets = this.client.getData().populateListAndSubscribe("services", this);
        this.model = DialogUtils.setupModel("address", this.cols, targets);
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.sortby(this.table, 1);
        this.table.setPopupMenu(this);
        Map<String, String> widths = DialogUtils.toMap("address: 125, port: 60, banner: 250, note: 250");
        DialogUtils.setTableColumnWidths(this.table, widths);
        return DialogUtils.FilterAndScroll(this.table);
    }

    public Map[] getSelectedRows() {
        return this.model.getSelectedRows(this.table);
    }

    @Override
    public void showPopup(MouseEvent ev) {
        Set temp = CommonUtils.toSet(this.model.getSelectedValues(this.table));
        Object[] unique = CommonUtils.toArray(temp);
        Stack<Scalar> args = new Stack<>();
        args.push(CommonUtils.toSleepArray(unique));
        this.client.getScriptEngine().getMenuBuilder().installMenu(ev, "targets", args);
    }

    @Override
    public void result(String key, Object o) {
        DialogUtils.setTable(this.table, this.model, CommonUtils.apply(key, (List) o, this));
    }
}

