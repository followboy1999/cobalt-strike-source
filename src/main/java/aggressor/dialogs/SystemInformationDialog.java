package aggressor.dialogs;

import aggressor.AggressorClient;
import common.Callback;
import common.CommonUtils;
import common.MudgeSanity;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class SystemInformationDialog implements SafeDialogCallback,
        Callback,
        ActionListener {
    protected AggressorClient client;
    protected JTextArea contents;

    public SystemInformationDialog(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void result(String call, Object result) {
        this.contents.append("\n\n*** Client Information ***\n\n");
        this.contents.append(MudgeSanity.systemInformation());
        this.contents.append("\n\n== Loaded Scripts ==\n\n");
        for (Object o : this.client.getScriptEngine().getScripts()) {
            this.contents.append(o + "\n");
        }
        this.contents.append("\n\n*** Team Server Information ***\n\n");
        this.contents.append(result.toString());
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        SafeDialogs.saveFile(null, "debug.txt", this);
    }

    @Override
    public void dialogResult(String r) {
        CommonUtils.writeToFile(new File(r), CommonUtils.toBytes(this.contents.getText()));
        DialogUtils.showInfo("Saved " + r);
    }

    public void show() {
        JFrame dialog = DialogUtils.dialog("System Information", 640, 480);
        this.contents = new JTextArea();
        JButton save = new JButton("Save");
        save.addActionListener(this);
        dialog.add(DialogUtils.description("This dialog provides information about your Cobalt Strike client and server. This information can greatly speed up support requests."), "North");
        dialog.add(new JScrollPane(this.contents), "Center");
        dialog.add(DialogUtils.center(save), "South");
        dialog.setVisible(true);
        this.client.getConnection().call("aggressor.sysinfo", this);
    }
}

