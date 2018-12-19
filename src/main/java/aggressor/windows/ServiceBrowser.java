package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.browsers.Services;
import common.AObject;
import common.CommonUtils;
import dialog.DialogUtils;
import dialog.SafeDialogs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

public class ServiceBrowser
        extends AObject implements ActionListener {
    protected AggressorClient client;
    protected Services browser;

    public ServiceBrowser(AggressorClient client, String[] targets) {
        this.client = client;
        this.browser = new Services(client, targets);
    }

    public ActionListener cleanup() {
        return this.browser.cleanup();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Remove".equals(ev.getActionCommand())) {
            Map[] options = this.browser.getSelectedRows();
            for (Map option : options) {
                this.client.getConnection().call("services.remove", CommonUtils.args(CommonUtils.ServiceKey(option)));
            }
            this.client.getConnection().call("services.push");
        } else if ("Note...".equals(ev.getActionCommand())) {
            SafeDialogs.ask("Set Note to:", "", r -> {
                Map[] options = ServiceBrowser.this.browser.getSelectedRows();
                for (Map option : options) {
                    option.put("note", r);
                    ServiceBrowser.this.client.getConnection().call("services.add", CommonUtils.args(CommonUtils.ServiceKey(option), option));
                }
                ServiceBrowser.this.client.getConnection().call("services.push");
            });
        }
    }

    public JComponent getContent() {
        JPanel dialog = new JPanel();
        dialog.setLayout(new BorderLayout());
        JButton delete = new JButton("Remove");
        JButton note = new JButton("Note...");
        JButton help = new JButton("Help");
        delete.addActionListener(this);
        note.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-targets"));
        dialog.add(this.browser.getContent(), "Center");
        dialog.add(DialogUtils.center(delete, note, help), "South");
        return dialog;
    }

}

