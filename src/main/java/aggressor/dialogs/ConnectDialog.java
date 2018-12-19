package aggressor.dialogs;

import aggressor.MultiFrame;
import aggressor.Prefs;
import dialog.DialogUtils;
import ui.Navigator;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class ConnectDialog {
    protected MultiFrame window;
    protected Navigator options = null;

    public ConnectDialog(MultiFrame window) {
        this.window = window;
    }

    public void show() {
        String last = "New Profile";
        JFrame dialog = DialogUtils.dialog("Connect", 640, 480);
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent ev) {
                ConnectDialog.this.window.closeConnect();
            }
        });
        this.options = new Navigator();
        this.options.addPage("New Profile", null, "This is the connect dialog. You should use it to connect to a Cobalt Strike (Aggressor) team server.", new Connect(this.window).getContent(dialog, "neo", "password", "127.0.0.1", "50050"));
        List temp = Prefs.getPreferences().getList("connection.profiles");
        for (Object aTemp : temp) {
            String next = (String) aTemp;
            String user = Prefs.getPreferences().getString("connection.profiles." + next + ".user", "neo");
            String pass = Prefs.getPreferences().getString("connection.profiles." + next + ".password", "password");
            String port = Prefs.getPreferences().getString("connection.profiles." + next + ".port", "50050");
            this.options.addPage(next, null, "This is the connect dialog. You should use it to connect to a Cobalt Strike (Aggressor) team server.", new Connect(this.window).getContent(dialog, user, pass, next, port));
            last = next;
        }
        this.options.set(last);
        dialog.add(this.options, "Center");
        dialog.pack();
        dialog.setVisible(true);
    }

}

