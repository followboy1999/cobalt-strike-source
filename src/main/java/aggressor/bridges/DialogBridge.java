package aggressor.bridges;

import aggressor.AggressorClient;
import common.CommonUtils;
import cortana.Cortana;
import dialog.DialogListener;
import dialog.DialogManager;
import dialog.DialogUtils;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

public class DialogBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public DialogBridge(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&dialog", this);
        Cortana.put(si, "&dialog_show", this);
        Cortana.put(si, "&dialog_description", this);
        Cortana.put(si, "&drow_checkbox", this);
        Cortana.put(si, "&drow_combobox", this);
        Cortana.put(si, "&drow_file", this);
        Cortana.put(si, "&drow_text", this);
        Cortana.put(si, "&drow_text_big", this);
        Cortana.put(si, "&drow_beacon", this);
        Cortana.put(si, "&drow_exploits", this);
        Cortana.put(si, "&drow_interface", this);
        Cortana.put(si, "&drow_krbtgt", this);
        Cortana.put(si, "&drow_listener", this);
        Cortana.put(si, "&drow_listener_stage", this);
        Cortana.put(si, "&drow_listener_smb", this);
        Cortana.put(si, "&drow_mailserver", this);
        Cortana.put(si, "&drow_proxyserver", this);
        Cortana.put(si, "&drow_site", this);
        Cortana.put(si, "&dbutton_action", this);
        Cortana.put(si, "&dbutton_help", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&dialog")) {
            String title = BridgeUtilities.getString(args, "");
            Map defaults = SleepUtils.getMapFromHash(BridgeUtilities.getHash(args));
            SleepClosure callback = BridgeUtilities.getFunction(args, script);
            ScriptedDialog dialog = new ScriptedDialog(title, 640, 480, callback);
            for (Object o : defaults.entrySet()) {
                Map.Entry next = (Map.Entry) o;
                String key = next.getKey().toString();
                String value = next.getValue().toString();
                dialog.controller.set(key, value);
            }
            return SleepUtils.getScalar(dialog);
        }
        switch (name) {
            case "&dialog_description": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                dialog.description = BridgeUtilities.getString(args, "");
                break;
            }
            case "&dialog_show": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                dialog.show();
                break;
            }
            case "&drow_checkbox": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                String desc = BridgeUtilities.getString(args, "");
                dialog.controller.checkbox_add(varname, label, desc);
                break;
            }
            case "&drow_combobox": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                String[] items = CommonUtils.toArray(SleepUtils.getListFromArray(BridgeUtilities.getArray(args)));
                dialog.controller.combobox(varname, label, items);
                break;
            }
            case "&drow_file": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.file(varname, label);
                break;
            }
            case "&drow_text": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                int cols = BridgeUtilities.getInt(args, 20);
                dialog.controller.text(varname, label, cols);
                break;
            }
            case "&drow_text_big": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.text_big(varname, label);
                break;
            }
            case "&drow_beacon": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.beacon(varname, label, this.client);
                break;
            }
            case "&drow_exploits": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.exploits(varname, label, this.client);
                break;
            }
            case "&drow_interface": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.interfaces(varname, label, this.client.getConnection(), this.client.getData());
                break;
            }
            case "&drow_krbtgt": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.krbtgt(varname, label, this.client);
                break;
            }
            case "&drow_listener": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.listener(varname, label, this.client.getConnection(), this.client.getData());
                break;
            }
            case "&drow_listener_smb": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.listenerWithSMB(varname, label, this.client.getConnection(), this.client.getData());
                break;
            }
            case "&drow_listener_stage": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.listener_stages(varname, label, this.client);
                break;
            }
            case "&drow_mailserver": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.mailserver(varname, label);
                break;
            }
            case "&drow_proxyserver": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.proxyserver(varname, label, this.client);
                break;
            }
            case "&drow_site": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String varname = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                dialog.controller.site(varname, label, this.client.getConnection(), this.client.getData());
                break;
            }
            case "&dbutton_action": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String label = BridgeUtilities.getString(args, "");
                JButton temp = dialog.controller.action(label);
                dialog.buttons.add(temp);
                break;
            }
            case "&dbutton_help": {
                ScriptedDialog dialog = (ScriptedDialog) BridgeUtilities.getObject(args);
                String url = BridgeUtilities.getString(args, "");
                JButton temp = dialog.controller.help(url);
                dialog.buttons.add(temp);
                break;
            }
        }
        return SleepUtils.getEmptyScalar();
    }

    private class ScriptedDialog implements DialogListener {
        protected DialogManager controller;
        protected JFrame body;
        protected LinkedList buttons = new LinkedList();
        protected String description = "";
        protected SleepClosure callback;

        public ScriptedDialog(String title, int width, int height, SleepClosure callback) {
            this.body = DialogUtils.dialog(title, width, height);
            this.controller = new DialogManager(this.body);
            this.controller.addDialogListener(this);
            this.callback = callback;
        }

        @Override
        public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
            Stack<Scalar> args = new Stack<>();
            args.push(SleepUtils.getHashWrapper(options));
            args.push(SleepUtils.getScalar(event.getActionCommand()));
            args.push(SleepUtils.getScalar(this));
            SleepUtils.runCode(this.callback, "", null, args);
        }

        public void show() {
            if (!"".equals(this.description)) {
                this.body.add(DialogUtils.description(this.description), "North");
            }
            this.body.add(this.controller.layout(), "Center");
            if (this.buttons.size() > 0) {
                this.body.add(DialogUtils.center(this.buttons), "South");
            }
            this.body.pack();
            this.body.setVisible(true);
        }
    }

}

