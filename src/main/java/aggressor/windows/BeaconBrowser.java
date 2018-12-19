package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.browsers.Beacons;
import common.AObject;
import common.BeaconEntry;
import common.CommonUtils;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BeaconBrowser
        extends AObject implements ActionListener {
    protected AggressorClient client;
    protected Beacons browser;

    public BeaconBrowser(AggressorClient client) {
        this.client = client;
        this.browser = new Beacons(client, true);
    }

    public ActionListener cleanup() {
        return this.browser.cleanup();
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Interact".equals(ev.getActionCommand())) {
            String id = this.browser.getSelectedValue() + "";
            BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), id);
            BeaconConsole c = new BeaconConsole(id, this.client);
            this.client.getTabManager().addTab(entry.title(), c.getConsole(), c.cleanup(), "Beacon console");
        } else if ("Remove".equals(ev.getActionCommand())) {
            Object[] ids = this.browser.getSelectedValues();
            for (Object id : ids) {
                this.client.getConnection().call("beacons.remove", CommonUtils.args(id));
            }
        }
    }

    public JComponent getContent() {
        JPanel dialog = new JPanel();
        dialog.setLayout(new BorderLayout());
        JButton interact = new JButton("Interact");
        JButton delete = new JButton("Remove");
        JButton help = new JButton("Help");
        interact.addActionListener(this);
        delete.addActionListener(this);
        help.addActionListener(DialogUtils.gotoURL("https://www.cobaltstrike.com/help-beacon"));
        dialog.add(this.browser.getContent(), "Center");
        dialog.add(DialogUtils.center(interact, delete, help), "South");
        return dialog;
    }
}

