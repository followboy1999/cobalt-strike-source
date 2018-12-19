package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.browsers.Applications;
import common.AObject;
import common.CommonUtils;
import dialog.ActivityPanel;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class ApplicationManager
        extends AObject implements ActionListener {
    protected AggressorClient client;
    protected Applications browser;
    protected ActivityPanel dialog;

    public ApplicationManager(AggressorClient client) {
        this.client = client;
        this.browser = new Applications(client);
    }

    public ActionListener cleanup() {
        return this.browser.cleanup();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Remove".equals(ev.getActionCommand())) {
            Map[] options = this.browser.getSelectedRows();
            for (Map option : options) {
                this.client.getConnection().call("applications.remove", CommonUtils.args(CommonUtils.ApplicationKey(option)));
            }
            this.client.getConnection().call("applications.push");
        }
    }

    public JComponent getContent() {
        this.dialog = new ActivityPanel();
        this.dialog.setLayout(new BorderLayout());
        this.browser.notifyOnResult(this.dialog);
        JButton del = new JButton("Remove");
        JButton help = new JButton("Help");
        del.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-application-browser"));
        this.dialog.add(this.browser.getContent(), "Center");
        this.dialog.add(DialogUtils.center(del, help), "South");
        return this.dialog;
    }
}

