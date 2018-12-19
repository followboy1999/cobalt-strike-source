package aggressor.windows;

import aggressor.Aggressor;
import aggressor.AggressorClient;
import aggressor.Prefs;
import common.AObject;
import common.CommonUtils;
import common.MudgeSanity;
import cortana.Cortana;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;
import sleep.error.YourCodeSucksException;
import ui.ATable;
import ui.GenericTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.List;

public class ScriptManager
        extends AObject implements ActionListener,
        SafeDialogCallback {
    protected GenericTableModel model;
    protected ATable table = null;
    protected String[] cols = new String[]{"path", "ready"};
    protected AggressorClient client;

    public ScriptManager(AggressorClient client) {
        this.client = client;
        this.model = DialogUtils.setupModel("path", this.cols, this.toModel());
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Load".equals(ev.getActionCommand())) {
            SafeDialogs.openFile("Load a script", null, null, false, false, this);
        } else if ("Unload".equals(ev.getActionCommand())) {
            String name = this.model.getSelectedValue(this.table) + "";
            for (Object o : Aggressor.getFrame().getScriptEngines()) {
                Cortana engine = (Cortana) o;
                engine.unloadScript(name);
            }
            List scripts = Prefs.getPreferences().getList("cortana.scripts");
            scripts.remove(name);
            Prefs.getPreferences().setList("cortana.scripts", scripts);
            Prefs.getPreferences().save();
            this.refresh();
        } else if ("Reload".equals(ev.getActionCommand())) {
            String name = this.model.getSelectedValue(this.table) + "";
            try {
                this.client.getScriptEngine().unloadScript(name);
                this.client.getScriptEngine().loadScript(name);
                DialogUtils.showInfo("Reloaded " + name);
            } catch (YourCodeSucksException yex) {
                MudgeSanity.logException("Load " + name, yex, true);
                DialogUtils.showError("Could not load " + name + ":\n\n" + yex.formatErrors());
            } catch (Exception ex) {
                MudgeSanity.logException("Load " + name, ex, false);
                DialogUtils.showError("Could not load " + name + "\n" + ex.getMessage());
            }
            try {
                for (Object o : Aggressor.getFrame().getOtherScriptEngines(this.client)) {
                    Cortana engine = (Cortana) o;
                    engine.unloadScript(name);
                    engine.loadScript(name);
                }
            } catch (Exception ex) {
                MudgeSanity.logException("Load " + name, ex, false);
            }
            this.refresh();
        }
    }

    @Override
    public void dialogResult(String r) {
        try {
            this.client.getScriptEngine().loadScript(r);
            for (Object o : Aggressor.getFrame().getOtherScriptEngines(this.client)) {
                Cortana engine = (Cortana) o;
                engine.loadScript(r);
            }
            List scripts = Prefs.getPreferences().getList("cortana.scripts");
            scripts.add(r);
            Prefs.getPreferences().setList("cortana.scripts", scripts);
            Prefs.getPreferences().save();
            this.refresh();
        } catch (YourCodeSucksException yex) {
            MudgeSanity.logException("Load " + r, yex, true);
            DialogUtils.showError("Could not load " + r + ":\n\n" + yex.formatErrors());
        } catch (Exception ex) {
            MudgeSanity.logException("Load " + r, ex, false);
            DialogUtils.showError("Could not load " + r + "\n" + ex.getMessage());
        }
    }

    public void refresh() {
        DialogUtils.setTable(this.table, this.model, this.toModel());
    }

    public LinkedList toModel() {
        HashSet scripts = new HashSet(this.client.getScriptEngine().getScripts());
        Iterator i = Prefs.getPreferences().getList("cortana.scripts").iterator();
        LinkedList<Map> r = new LinkedList<>();
        while (i.hasNext()) {
            String next = (String) i.next();
            if (scripts.contains(new File(next).getName())) {
                r.add(CommonUtils.toMap("path", next, "ready", "\u2713"));
                continue;
            }
            r.add(CommonUtils.toMap("path", next, "ready", ""));
        }
        return r;
    }

    public JComponent getContent() {
        JPanel dialog = new JPanel();
        dialog.setLayout(new BorderLayout());
        this.table = DialogUtils.setupTable(this.model, this.cols, true);
        DialogUtils.setTableColumnWidths(this.table, DialogUtils.toMap("path: 240, ready: 64"));
        this.table.getColumn("ready").setPreferredWidth(64);
        this.table.getColumn("ready").setMaxWidth(64);
        JButton load = new JButton("Load");
        JButton unload = new JButton("Unload");
        JButton reload = new JButton("Reload");
        JButton help = new JButton("Help");
        load.addActionListener(this);
        unload.addActionListener(this);
        reload.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-scripting"));
        dialog.add(new JScrollPane(this.table), "Center");
        dialog.add(DialogUtils.center(load, unload, reload, help), "South");
        return dialog;
    }
}

