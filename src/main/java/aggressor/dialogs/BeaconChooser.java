package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.browsers.Beacons;
import common.AObject;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BeaconChooser
        extends AObject implements ActionListener {
    protected JFrame dialog = null;
    protected SafeDialogCallback callback;
    protected Beacons browser;

    public BeaconChooser(AggressorClient client, SafeDialogCallback callback) {
        this.callback = callback;
        this.browser = new Beacons(client, false);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        if ("Choose".equals(ev.getActionCommand())) {
            String bid = (String) this.browser.getSelectedValue();
            this.dialog.setVisible(false);
            this.dialog.dispose();
            this.callback.dialogResult(bid);
        }
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Choose a Beacon", 800, 240);
        this.dialog.setLayout(new BorderLayout());
        JButton choose = new JButton("Choose");
        choose.addActionListener(this);
        this.dialog.add(this.browser.getContent(), "Center");
        this.dialog.add(DialogUtils.center(choose), "South");
        this.dialog.addWindowListener(this.browser.onclose());
        this.dialog.setVisible(true);
        this.dialog.show();
    }
}

