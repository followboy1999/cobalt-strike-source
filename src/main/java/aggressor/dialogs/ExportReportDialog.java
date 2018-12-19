package aggressor.dialogs;

import aggressor.AggressorClient;
import common.CommonUtils;
import data.DataAggregate;
import data.FieldSorter;
import dialog.*;
import report.Document;
import sleep.runtime.SleepUtils;
import ui.Sorters;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;

public class ExportReportDialog implements DialogListener, SafeDialogCallback, Runnable {
    protected AggressorClient client;
    protected String report;
    protected Map<String, Object> options;
    protected String file;

    public ExportReportDialog(AggressorClient client, String report) {
        this.client = client;
        this.report = report;
    }

    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.options = options;
        String output = DialogUtils.string(options, "output");
        String sel = CommonUtils.strrep(this.report.toLowerCase(), " ", "");
        sel = CommonUtils.strrep(sel, "&", "_");
        sel = CommonUtils.strrep(sel, ",", "");
        if ("PDF".equals(output)) {
            sel = sel + ".pdf";
        } else if ("MS Word".equals(output)) {
            sel = sel + ".docx";
        }

        SafeDialogs.saveFile(null, sel, this);
    }

    public void dialogResult(String file) {
        this.file = file;
        (new Thread(this, "export " + file)).start();
    }

    public void sort(Map models, String name, String key, Comparator sorter) {
        List temp = (List) models.get(name);
        if (temp == null) {
            CommonUtils.print_error("Model '" + name + "' doesn't exist. Can't sort by: '" + key + "'");
            Thread.currentThread();
            Thread.dumpStack();
        } else {
            temp.sort(new FieldSorter(key, sorter));
        }

    }

    public void mask(Map models, String name, String key) {
        List temp = (List) models.get(name);

        for (Object aTemp : temp) {
            Map next = (Map) aTemp;
            String value = DialogUtils.string(next, key);
            if (value.length() == 32) {
                next.put(key, value.replaceAll(".", "*"));
            } else {
                next.put(key, "********");
            }
        }

    }

    public void maskemail(Map models, String name, String key) {
        List temp = (List) models.get(name);

        for (Object aTemp : temp) {
            Map next = (Map) aTemp;
            String value = DialogUtils.string(next, key);
            if (value != null) {
                String[] data = value.split("@");
                next.put(key, CommonUtils.garbage(data[0]) + "@" + data[1]);
            }
        }

    }

    public void run() {
        String output = DialogUtils.string(this.options, "output");
        String titles = DialogUtils.string(this.options, "short");
        String titlel = DialogUtils.string(this.options, "long");
        String desc = DialogUtils.string(this.options, "description");
        boolean mask = DialogUtils.bool(this.options, "mask");
        ProgressMonitor monitor = new ProgressMonitor(this.client, "Export Report", "Starting...", 0, 5);
        monitor.setNote("Aggregate data...");
        Map models = DataAggregate.AllModels(this.client);
        monitor.setProgress(1);
        monitor.setNote("Sort targets");
        this.sort(models, "targets", "address", Sorters.getHostSorter());
        monitor.setNote("Sort services");
        this.sort(models, "services", "port", Sorters.getNumberSorter());
        monitor.setNote("Sort credentials");
        this.sort(models, "credentials", "password", Sorters.getStringSorter());
        this.sort(models, "credentials", "realm", Sorters.getStringSorter());
        this.sort(models, "credentials", "user", Sorters.getNumberSorter());
        monitor.setNote("Sort applications");
        this.sort(models, "applications", "application", Sorters.getStringSorter());
        monitor.setNote("Sort sessions");
        this.sort(models, "sessions", "opened", Sorters.getNumberSorter());
        monitor.setNote("Sort archives");
        this.sort(models, "archives", "when", Sorters.getNumberSorter());
        monitor.setProgress(2);
        if (mask) {
            this.mask(models, "credentials", "password");
            this.maskemail(models, "tokens", "email");
        }

        Stack<sleep.runtime.Scalar> args = new Stack<>();
        args.push(SleepUtils.getScalar(models));
        args.push(CommonUtils.convertAll(models));
        args.push(CommonUtils.convertAll(this.options));
        monitor.setNote("Build document...");
        Document structure = this.client.getReportEngine().buildReport(this.report, titles, args);
        monitor.setProgress(3);
        monitor.setNote("Export document...");
        if ("PDF".equals(output)) {
            structure.toPDF(new File(this.file));
        } else if ("MS Word".equals(output)) {
            structure.toWord(new File(this.file));
        }

        monitor.setProgress(4);
        monitor.close();
        DialogUtils.showInfo("Report " + this.file + " saved");
    }

    public void show() {
        JFrame dialog = DialogUtils.dialog("Export Report", 640, 480);
        DialogManager controller = new DialogManager(dialog);
        controller.addDialogListener(this);
        controller.set("output", "PDF");
        controller.set("short", this.report);
        controller.set("long", this.report);
        controller.set("description", this.client.getReportEngine().describe(this.report));
        controller.text("short", "Short Title:", 20);
        controller.text("long", "Long Title:", 20);
        controller.text_big("description", "Description:");
        controller.combobox("output", "Output:", CommonUtils.toArray("MS Word, PDF"));
        JComponent a = controller.layout();
        JComponent b = controller.checkbox("mask", "Mask email addresses and passwords");
        JButton ok = controller.action("Export");
        JButton help = controller.help("https://www.cobaltstrike.com/help-reporting");
        dialog.add(DialogUtils.stack(a, b), "Center");
        dialog.add(DialogUtils.center(ok, help), "South");
        dialog.pack();
        dialog.setVisible(true);
    }
}
