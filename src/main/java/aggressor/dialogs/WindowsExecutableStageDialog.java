package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.ArtifactUtils;
import common.Callback;
import common.CommonUtils;
import common.ResourceUtils;
import dialog.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WindowsExecutableStageDialog implements DialogListener, Callback, SafeDialogCallback {
    protected JFrame dialog = null;
    protected ActionEvent event = null;
    protected Map options = null;
    protected String outfile = "";
    protected AggressorClient client;

    public WindowsExecutableStageDialog(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.event = event;
        this.options = options;
        String type = options.get("output") + "";
        String ext = "";
        if (type.contains("PowerShell")) {
            ext = "beacon.ps1";
        } else if (type.contains("Raw")) {
            ext = "beacon.bin";
        } else if (type.contains("EXE")) {
            ext = "beacon.exe";
        } else if (type.contains("DLL")) {
            ext = "beacon.dll";
        }
        SafeDialogs.saveFile(null, ext, this);
    }

    @Override
    public void dialogResult(String r) {
        this.outfile = r;
        String lname = DialogUtils.string(this.options, "stage");
        String arch = DialogUtils.bool(this.options, "x64") ? "x64" : "x86";
        String proxy = DialogUtils.string(this.options, "proxy");
        this.client.getConnection().call("listeners.export", CommonUtils.args(lname, arch, proxy), this);
    }

    @Override
    public void result(String call, Object result) {
        byte[] stage = (byte[]) result;
        String type = this.options.get("output") + "";
        boolean x64 = DialogUtils.bool(this.options, "x64");
        boolean sign = DialogUtils.bool(this.options, "sign");
        if (x64) {
            switch (type) {
                case "Windows EXE":
                    new ArtifactUtils(this.client).patchArtifact(stage, "artifact64big.exe", this.outfile);
                    break;
                case "Windows Service EXE":
                    new ArtifactUtils(this.client).patchArtifact(stage, "artifact64svcbig.exe", this.outfile);
                    break;
                default:
                    if (type.equals("Windows DLL (32-bit)")) {
                        DialogUtils.showError("I can't generate an x86 artifact for an x64 payload.");
                        return;
                    }
                    switch (type) {
                        case "Windows DLL (64-bit)":
                            new ArtifactUtils(this.client).patchArtifact(stage, "artifact64big.x64.dll", this.outfile);
                            break;
                        case "PowerShell":
                            new ResourceUtils(this.client).buildPowerShell(stage, this.outfile, true);
                            break;
                        default:
                            CommonUtils.writeToFile(new File(this.outfile), stage);
                            break;
                    }
                    break;
            }
        } else if (type.equals("Windows EXE")) {
            new ArtifactUtils(this.client).patchArtifact(stage, "artifact32big.exe", this.outfile);
        } else if (type.equals("Windows Service EXE")) {
            new ArtifactUtils(this.client).patchArtifact(stage, "artifact32svcbig.exe", this.outfile);
        } else if (type.equals("Windows DLL (32-bit)")) {
            new ArtifactUtils(this.client).patchArtifact(stage, "artifact32big.dll", this.outfile);
        } else if (type.equals("Windows DLL (64-bit)")) {
            new ArtifactUtils(this.client).patchArtifact(stage, "artifact64big.dll", this.outfile);
        } else if (type.equals("PowerShell")) {
            new ResourceUtils(this.client).buildPowerShell(stage, this.outfile);
        } else {
            CommonUtils.writeToFile(new File(this.outfile), stage);
        }
        if (sign) {
            if (this.outfile.toLowerCase().endsWith(".exe") || this.outfile.toLowerCase().endsWith(".dll")) {
                DataUtils.getSigner(this.client.getData()).sign(new File(this.outfile));
            } else {
                DialogUtils.showError("Can only sign EXE and DLL files");
                return;
            }
        }
        DialogUtils.showInfo("Saved " + type + " to\n" + this.outfile);
        this.client.getConnection().call("armitage.broadcast", CommonUtils.args("manproxy", DialogUtils.string(this.options, "proxy")));
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Windows Executable (Stageless)", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("proxy", DataUtils.getManualProxySetting(this.client.getData()));
        controller.set("output", "Windows EXE");
        controller.listener_stages("stage", "Stage:", this.client);
        controller.proxyserver("proxy", "Proxy:", this.client);
        controller.combobox("output", "Output:", CommonUtils.toArray("PowerShell, Raw, Windows EXE, Windows Service EXE, Windows DLL (32-bit), Windows DLL (64-bit)"));
        controller.checkbox_add("x64", "x64:", "Use x64 payload");
        controller.checkbox_add("sign", "sign:", "Sign executable file", DataUtils.getSigner(this.client.getData()).available());
        JButton ok = controller.action("Generate");
        JButton help = controller.help("https://www.cobaltstrike.com/help-staged-exe");
        this.dialog.add(DialogUtils.description("Export a stageless Beacon as a Windows executable. Use Cobalt Strike Arsenal scripts (Help -> Arsenal) to customize this process."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

