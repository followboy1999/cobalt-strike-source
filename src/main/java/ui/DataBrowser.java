package ui;

import aggressor.Prefs;
import common.CommonUtils;
import console.Activity;
import cortana.Cortana;
import dialog.DialogUtils;
import sleep.runtime.Scalar;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

public class DataBrowser
        extends JComponent implements ListSelectionListener,
        Activity,
        TablePopup {
    protected JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    protected GenericTableModel model;
    protected ATable table;
    protected LinkedList listeners = new LinkedList();
    protected String hook;
    protected Cortana engine;
    protected String col;
    protected JLabel label;
    protected Color original;

    @Override
    public void registerLabel(JLabel label) {
        this.original = label.getForeground();
        this.label = label;
    }

    @Override
    public void resetNotification() {
        this.label.setForeground(this.original);
    }

    public static DataBrowser getBeaconDataBrowser(Cortana engine, String primary, JComponent other, LinkedList initialv) {
        return new DataBrowser(engine, primary, CommonUtils.toArray("user, computer, pid, when"), other, initialv, "beacon", "id");
    }

    public DataBrowser(Cortana engine, String primary, String[] cols, JComponent other, LinkedList initialv, String hook, String col) {
        this.hook = hook;
        this.engine = engine;
        this.col = col;
        this.setLayout(new BorderLayout());
        this.add(this.split, "Center");
        this.model = DialogUtils.setupModel(primary, cols, initialv);
        this.table = DialogUtils.setupTable(this.model, cols, false);
        this.table.setPopupMenu(this);
        this.table.getSelectionModel().addListSelectionListener(this);
        this.split.add(DialogUtils.FilterAndScroll(this.table));
        this.split.add(other);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }
        for (Object listener : this.listeners) {
            DataSelectionListener l = (DataSelectionListener) listener;
            l.selected(this.getSelectedValue());
        }
    }

    public void addDataSelectionListener(DataSelectionListener l) {
        this.listeners.add(l);
    }

    public Object getSelectedValue() {
        return this.model.getSelectedValue(this.table);
    }

    public ATable getTable() {
        return this.table;
    }

    @Override
    public void showPopup(MouseEvent ev) {
        Object[] selected = new Object[]{this.model.getSelectedValueFromColumn(this.table, this.col)};
        Stack<Scalar> args = new Stack<>();
        args.push(CommonUtils.toSleepArray(selected));
        this.engine.getMenuBuilder().installMenu(ev, this.hook, args);
    }

    public void addEntry(Map<String, Object> row) {
        CommonUtils.runSafe(() -> {
            DataBrowser.this.table.markSelections();
            DataBrowser.this.model.addEntry(row);
            DataBrowser.this.model.fireListeners();
            DataBrowser.this.table.restoreSelections();
            if (!DataBrowser.this.isShowing()) {
                DataBrowser.this.label.setForeground(Prefs.getPreferences().getColor("tab.highlight.color", "#0000ff"));
            }
        });
    }

    public void setTable(final Collection stuff) {
        CommonUtils.runSafe(() -> {
            DialogUtils.setTable(DataBrowser.this.table, DataBrowser.this.model, stuff);
            if (!DataBrowser.this.isShowing()) {
                DataBrowser.this.label.setForeground(Prefs.getPreferences().getColor("tab.highlight.color", "#0000ff"));
            }
        });
    }

}

