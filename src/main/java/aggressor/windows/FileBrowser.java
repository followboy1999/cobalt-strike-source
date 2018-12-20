package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.TaskBeacon;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import common.StringStack;
import dialog.DialogUtils;
import dialog.SafeDialogs;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;
import ui.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

public class FileBrowser extends AObject implements Callback, ActionListener, TablePopup, DoubleClickListener {
    protected String bid;
    protected AggressorClient client;
    protected GenericTableModel model = null;
    protected ATable table = null;
    protected String[] cols = new String[]{"D", "Name", "Size", "Modified"};
    protected ATextField folder = null;
    protected JButton up = null;
    protected String cwd = "";

    public FileBrowser(AggressorClient client, String bid) {
        this.client = client;
        this.bid = bid;
    }

    public void ls(String location) {
        String folder_encoded = CommonUtils.bString(DataUtils.encodeForBeacon(this.client.getData(), this.bid, location));
        this.client.getConnection().call("beacons.task_ls", CommonUtils.args(this.bid, folder_encoded), this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        String command = ev.getActionCommand();
        if (ev.getSource() == this.folder) {
            this.ls(this.folder.getText());
        } else if (ev.getSource() == this.up) {
            StringStack temp = new StringStack(this.folder.getText(), "\\");
            temp.pop();
            if (!"".equals(temp.toString())) {
                this.ls(temp.toString());
            }
        } else {
            switch (command) {
                case "Upload...":
                    SafeDialogs.openFile("Upload...", null, null, false, false, r -> {
                        TaskBeacon tasker = new TaskBeacon(FileBrowser.this.client, FileBrowser.this.client.getData(), FileBrowser.this.client.getConnection(), new String[]{FileBrowser.this.bid});
                        tasker.input("upload " + r + " (" + FileBrowser.this.cwd + "\\" + new File(r).getName() + ")");
                        tasker.Upload(r, FileBrowser.this.cwd + "\\" + new File(r).getName());
                        FileBrowser.this.ls(FileBrowser.this.cwd);
                    });
                    break;
                case "Make Directory":
                    SafeDialogs.ask("Which folder?", "", r -> {
                        TaskBeacon tasker = new TaskBeacon(FileBrowser.this.client, FileBrowser.this.client.getData(), FileBrowser.this.client.getConnection(), new String[]{FileBrowser.this.bid});
                        tasker.input("mkdir " + FileBrowser.this.cwd + "\\" + r);
                        tasker.MkDir(FileBrowser.this.cwd + "\\" + r);
                        FileBrowser.this.ls(FileBrowser.this.cwd);
                    });
                    break;
                case "List Drives":
                    this.client.getConnection().call("beacons.task_drives", CommonUtils.args(this.bid), (key, o) -> {
                        String[] drives = CommonUtils.toArray(CommonUtils.drives(o + ""));
                        LinkedList<Map<String, Object>> results = new LinkedList<>();
                        for (String drive : drives) {
                            HashMap<String, Object> next = new HashMap<>();
                            next.put("D", "drive");
                            next.put("Name", drive);
                            results.add(next);
                        }
                        DialogUtils.setTable(FileBrowser.this.table, FileBrowser.this.model, results);
                    });
                    break;
                case "Refresh":
                    this.ls(this.cwd);
                    break;
            }
        }
    }

    @Override
    public void doubleClicked(MouseEvent ev) {
        String name = (String) this.model.getSelectedValue(this.table);
        String type = (String) this.model.getSelectedValueFromColumn(this.table, "D");
        if (type.equals("dir")) {
            StringStack temp = new StringStack(this.folder.getText(), "\\");
            temp.push(name);
            this.ls(temp.toString());
        } else if (type.equals("drive")) {
            this.ls(name);
        }
    }

    @Override
    public void showPopup(MouseEvent ev) {
        Stack<Scalar> args = new Stack<>();
        args.push(SleepUtils.getScalar(this));
        args.push(CommonUtils.toSleepArray(this.model.getSelectedValues(this.table)));
        args.push(SleepUtils.getScalar(this.cwd));
        args.push(SleepUtils.getScalar(this.bid));
        this.client.getScriptEngine().getMenuBuilder().installMenu(ev, "filebrowser", args);
    }

    public JComponent getContent() {
        JPanel dialog = new JPanel();
        dialog.setLayout(new BorderLayout());
        this.model = DialogUtils.setupModel("Name", this.cols, new LinkedList<>());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        this.table.getColumn("D").setMaxWidth(38);
        this.table.getColumn("Size").setCellRenderer(ATable.getSizeTableRenderer());
        this.table.getColumn("D").setCellRenderer(ATable.getFileTypeTableRenderer());
        this.table.getColumn("Name").setCellRenderer(ATable.getSimpleTableRenderer());
        TableRowSorter<GenericTableModel> sorter = new TableRowSorter<>(this.model);
        sorter.toggleSortOrder(0);
        this.table.setRowSorter(sorter);
        sorter.setComparator(2, Sorters.getNumberSorter());
        // sorter.setComparator(3, Sorters.getDateSorter("MM/dd/yyyy HH:mm:ss"));
        sorter.setComparator(3, Sorters.getDateSorter("yyyy/MM/dd HH:mm:ss"));
        this.table.setPopupMenu(this);
        this.table.addMouseListener(new DoubleClickWatch(this));
        JButton upload = DialogUtils.Button("Upload...", this);
        JButton mkdir = DialogUtils.Button("Make Directory", this);
        JButton drives = DialogUtils.Button("List Drives", this);
        JButton refresh = DialogUtils.Button("Refresh", this);
        JButton help = DialogUtils.Button("Help", this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-file-browser"));
        this.folder = new ATextField("", 80);
        this.folder.addActionListener(this);
        FileSystemView fsv = FileSystemView.getFileSystemView();
        Icon chooser = fsv.getSystemIcon(fsv.getDefaultDirectory());
        this.up = new JButton(chooser);
        this.up.addActionListener(this);
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());
        top.add(this.folder, "Center");
        top.add(DialogUtils.pad(this.up, 0, 0, 0, 4), "West");
        dialog.add(DialogUtils.pad(top, 3, 3, 3, 3), "North");
        dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        dialog.add(DialogUtils.center(upload, mkdir, drives, refresh, help), "South");
        this.ls(".");
        return dialog;
    }

    @Override
    public void result(String key, Object o) {
        LinkedList<Map<String, Object>> results = new LinkedList<>();
        String[] rows = o.toString().trim().split("\n");
        this.cwd = rows[0].substring(0, rows[0].length() - 2);
        for (int x = 1; x < rows.length; ++x) {
            String[] cols = rows[x].split("\t");
            HashMap<String, Object> next = new HashMap<>();
            if (cols[0].equals("D") && !".".equals(cols[3]) && !"..".equals(cols[3])) {
                next.put("D", "dir");
                next.put("Modified", cols[2]);
                next.put("Name", cols[3]);
                results.add(next);
                continue;
            }
            if (!cols[0].equals("F")) continue;
            next.put("D", "fil");
            next.put("Size", cols[1]);
            next.put("Modified", cols[2]);
            next.put("Name", cols[3]);
            results.add(next);
        }
        DialogUtils.setText(this.folder, this.cwd);
        DialogUtils.setTable(this.table, this.model, results);
    }

}

