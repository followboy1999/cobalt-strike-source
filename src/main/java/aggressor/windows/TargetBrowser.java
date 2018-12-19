package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.browsers.Targets;
import aggressor.dialogs.ImportHosts;
import aggressor.dialogs.TargetDialog;
import common.AObject;
import common.CommonUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;
import dialog.SafeDialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TargetBrowser
        extends AObject implements ActionListener {
    protected AggressorClient client;
    protected Targets browser;
    protected ActivityPanel dialog = null;

    public TargetBrowser(AggressorClient client) {
        this.client = client;
        this.browser = new Targets(client);
    }

    public ActionListener cleanup() {
        return this.browser.cleanup();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Add".equals(ev.getActionCommand())) {
            new TargetDialog(this.client).show();
        } else if ("Import".equals(ev.getActionCommand())) {
            SafeDialogs.openFile("Choose a file", null, null, false, false, r -> new ImportHosts(TargetBrowser.this.client, new File(r)));
        } else if ("Remove".equals(ev.getActionCommand())) {
            Map[] options = this.browser.getSelectedRows();
            for (Map option : options) {
                this.client.getConnection().call("targets.remove", CommonUtils.args(CommonUtils.TargetKey(option)));
            }
            this.client.getConnection().call("targets.push");
        } else if ("Note...".equals(ev.getActionCommand())) {
            SafeDialogs.ask("Set Note to:", "", r -> {
                Map[] options = TargetBrowser.this.browser.getSelectedRows();
                for (Map option : options) {
                    HashMap<String, String> temp = new HashMap<String, String>(option);
                    temp.put("note", r);
                    temp.remove("image");
                    TargetBrowser.this.client.getConnection().call("targets.add", CommonUtils.args(CommonUtils.TargetKey(temp), temp));
                }
                TargetBrowser.this.client.getConnection().call("targets.push");
            });
        }
    }

    public JComponent getContent() {
        this.dialog = new ActivityPanel();
        this.dialog.setLayout(new BorderLayout());
        this.browser.notifyOnResult(this.dialog);
        JButton add = new JButton("Add");
        JButton importf = new JButton("Import");
        JButton delete = new JButton("Remove");
        JButton note = new JButton("Note...");
        JButton help = new JButton("Help");
        add.addActionListener(this);
        importf.addActionListener(this);
        delete.addActionListener(this);
        note.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-targets"));
        this.dialog.add(this.browser.getContent(), "Center");
        this.dialog.add(DialogUtils.center(add, importf, delete, note, help), "South");
        return this.dialog;
    }

}

