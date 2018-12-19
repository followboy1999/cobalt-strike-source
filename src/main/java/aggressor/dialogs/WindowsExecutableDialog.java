package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.ArtifactUtils;
import common.CommonUtils;
import dialog.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WindowsExecutableDialog implements DialogListener,
        SafeDialogCallback {
    protected JFrame dialog = null;
    protected AggressorClient client;
    protected Map options = null;
    protected byte[] stager;

    public WindowsExecutableDialog(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.options = options;
        this.stager = DialogUtils.getStager(options);
        if (this.stager.length == 0) {
            return;
        }
        String type = options.get("output") + "";
        String ext = "";
        if (type.contains("EXE")) {
            ext = "artifact.exe";
        } else if (type.contains("DLL")) {
            ext = "artifact.dll";
        }
        SafeDialogs.saveFile(null, ext, this);
    }

    @Override
    public void dialogResult(String outfile) {
        String type = this.options.get("output") + "";
        String listener = this.options.get("listener") + "";
        boolean x64 = DialogUtils.bool(this.options, "x64");
        boolean sign = DialogUtils.bool(this.options, "sign");
        if (x64) {
            switch (type) {
                case "Windows EXE":
                    new ArtifactUtils(this.client).patchArtifact(this.stager, "artifact64.exe", outfile);
                    break;
                case "Windows Service EXE":
                    new ArtifactUtils(this.client).patchArtifact(this.stager, "artifact64svc.exe", outfile);
                    break;
                default:
                    if (type.equals("Windows DLL (32-bit)")) {
                        DialogUtils.showError("I can't generate an x86 artifact for an x64 payload.");
                        return;
                    }
                    if (type.equals("Windows DLL (64-bit)")) {
                        new ArtifactUtils(this.client).patchArtifact(this.stager, "artifact64.x64.dll", outfile);
                    }
                    break;
            }
        } else if (type.equals("Windows EXE")) {
            new ArtifactUtils(this.client).patchArtifact(this.stager, "artifact32.exe", outfile);
        } else if (type.equals("Windows Service EXE")) {
            new ArtifactUtils(this.client).patchArtifact(this.stager, "artifact32svc.exe", outfile);
        } else if (type.equals("Windows DLL (32-bit)")) {
            new ArtifactUtils(this.client).patchArtifact(this.stager, "artifact32.dll", outfile);
        } else if (type.equals("Windows DLL (64-bit)")) {
            new ArtifactUtils(this.client).patchArtifact(this.stager, "artifact64.dll", outfile);
        }
        if (sign) {
            DataUtils.getSigner(this.client.getData()).sign(new File(outfile));
        }
        DialogUtils.showInfo("Saved " + type + " to\n" + outfile);
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Windows Executable", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.listener("listener", "Listener:", this.client.getConnection(), this.client.getData());
        controller.combobox("output", "Output:", CommonUtils.toArray("Windows EXE, Windows Service EXE, Windows DLL (32-bit), Windows DLL (64-bit)"));
        controller.checkbox_add("x64", "x64:", "Use x64 payload");
        controller.checkbox_add("sign", "sign:", "Sign executable file", DataUtils.getSigner(this.client.getData()).available());
        JButton ok = controller.action("Generate");
        JButton help = controller.help("https://www.cobaltstrike.com/help-windows-exe");
        this.dialog.add(DialogUtils.description("This dialog generates a Windows executable. Use Cobalt Strike Arsenal scripts (Help -> Arsenal) to customize this process."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

