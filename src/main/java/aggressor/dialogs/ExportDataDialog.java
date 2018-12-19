package aggressor.dialogs;

import aggressor.AggressorClient;
import common.CommonUtils;
import common.Keys;
import data.DataAggregate;
import dialog.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;

public class ExportDataDialog implements DialogListener,
        SafeDialogCallback,
        Runnable {
    protected AggressorClient client;
    protected String file;
    protected String output;

    public ExportDataDialog(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.output = DialogUtils.string(options, "output");
        SafeDialogs.openFile("Save to...", null, null, false, true, this);
    }

    @Override
    public void dialogResult(String file) {
        this.file = file;
        new File(file).mkdirs();
        new Thread(this, "export " + file).start();
    }

    public void dump(List model, String name, String[] cols) {
        if ("XML".equals(this.output)) {
            this.dumpXML(model, name, cols);
        } else {
            this.dumpTSV(model, name, cols);
        }
    }

    public void dumpXML(List model, String name, String[] cols) {
        StringBuilder output = new StringBuilder();
        output.append("<").append(name).append(">\n");
        for (Object aModel : model) {
            Map next = (Map) aModel;
            output.append("\t<entry>\n");
            for (String col : cols) {
                output.append("\t\t\t<").append(col).append(">");
                output.append(DialogUtils.string(next, col));
                output.append("</").append(col).append(">\n");
            }
            output.append("\t</entry>\n");
        }
        output.append("</").append(name).append(">\n");
        CommonUtils.writeToFile(new File(this.file, name + ".xml"), CommonUtils.toBytes(output.toString(), "UTF-8"));
    }

    public void dumpTSV(List model, String name, String[] cols) {
        StringBuilder output = new StringBuilder();
        for (int x = 0; x < cols.length; ++x) {
            output.append(cols[x]);
            if (x + 1 >= cols.length) continue;
            output.append("\t");
        }
        output.append("\n");
        for (Object aModel : model) {
            Map next = (Map) aModel;
            for (int x = 0; x < cols.length; ++x) {
                output.append(DialogUtils.string(next, cols[x]));
                if (x + 1 >= cols.length) continue;
                output.append("\t");
            }
            output.append("\n");
        }
        CommonUtils.writeToFile(new File(this.file, name + ".tsv"), CommonUtils.toBytes(output.toString(), "UTF-8"));
    }

    public static List getKey(List data, String a) {
        LinkedList<Map> results = new LinkedList<>();
        for (Object aData : data) {
            Map next = (Map) aData;
            String type = DialogUtils.string(next, "type");
            if (!type.equals(a)) continue;
            results.add(next);
        }
        return results;
    }

    public static List getBeaconStuff(List data) {
        LinkedList<Map> results = new LinkedList<>();
        for (Object aData : data) {
            Map next = (Map) aData;
            String type = DialogUtils.string(next, "type");
            if (!type.equals("checkin") && !type.equals("input") && !type.equals("output") && !type.equals("indicator") && !type.equals("task") && !type.equals("beacon_initial"))
                continue;
            results.add(next);
        }
        return results;
    }

    @Override
    public void run() {
        ProgressMonitor monitor = new ProgressMonitor(this.client, "Export Data", "Starting...", 0, 6 + Keys.size());
        int count = 0;
        monitor.setNote("Aggregate data...");
        Map models = DataAggregate.AllModels(this.client);
        monitor.setProgress(1);
        ++count;
        monitor.setNote("webhits");
        this.dump(ExportDataDialog.getKey((List) models.get("archives"), "webhit"), "webhits", CommonUtils.toArray("when, token, data"));
        monitor.setProgress(2);
        ++count;
        monitor.setNote("campaigns");
        this.dump(ExportDataDialog.getKey((List) models.get("archives"), "sendmail_start"), "campaigns", CommonUtils.toArray("cid, when, url, attachment, template, subject"));
        monitor.setProgress(3);
        ++count;
        monitor.setNote("sentemails");
        this.dump(ExportDataDialog.getKey((List) models.get("archives"), "sendmail_post"), "sentemails", CommonUtils.toArray("token, cid, when, status, data"));
        monitor.setProgress(4);
        ++count;
        monitor.setNote("activity");
        this.dump(ExportDataDialog.getBeaconStuff((List) models.get("archives")), "activity", CommonUtils.toArray("bid, type, when, data"));
        monitor.setProgress(5);
        ++count;
        monitor.setNote("events");
        this.dump(ExportDataDialog.getKey((List) models.get("archives"), "notify"), "events", CommonUtils.toArray("when, data"));
        monitor.setProgress(6);
        ++count;
        Iterator i = Keys.getDataModelIterator();
        while (i.hasNext()) {
            String model = (String) i.next();
            monitor.setNote(model);
            this.dump((List) models.get(model), model, Keys.getCols(model));
            monitor.setProgress(count);
            ++count;
        }
        monitor.close();
        DialogUtils.showInfo("Exported data to " + this.file);
    }

    public void show() {
        JFrame dialog = DialogUtils.dialog("Export Data", 640, 480);
        DialogManager controller = new DialogManager(dialog);
        controller.addDialogListener(this);
        controller.combobox("output", "Output:", CommonUtils.toArray("TSV, XML"));
        JButton ok = controller.action("Export");
        JButton help = controller.help("https://www.cobaltstrike.com/help-export-data");
        dialog.add(controller.layout(), "Center");
        dialog.add(DialogUtils.center(ok, help), "South");
        dialog.pack();
        dialog.setVisible(true);
    }
}

