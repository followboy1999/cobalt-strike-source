package aggressor.bridges;

import aggressor.AggressorClient;
import cortana.Cortana;
import dialog.DialogUtils;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.Stack;

public class SafeDialogBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public SafeDialogBridge(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&show_message", this);
        Cortana.put(si, "&show_error", this);
        Cortana.put(si, "&prompt_confirm", this);
        Cortana.put(si, "&prompt_text", this);
        Cortana.put(si, "&prompt_file_open", this);
        Cortana.put(si, "&prompt_directory_open", this);
        Cortana.put(si, "&prompt_file_save", this);
    }

    public SafeDialogCallback popCallback(Stack args, ScriptInstance script) {
        final SleepClosure f = BridgeUtilities.getFunction(args, script);
        return r -> {
            if (r == null) {
                return;
            }
            Stack<Scalar> args1 = new Stack<>();
            args1.push(SleepUtils.getScalar(r));
            SleepUtils.runCode(f, "dialogResult", null, args1);
        };
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if ("&show_message".equals(name)) {
            String text = BridgeUtilities.getString(args, "");
            DialogUtils.showInfo(text);
        } else if ("&show_error".equals(name)) {
            String text = BridgeUtilities.getString(args, "");
            DialogUtils.showError(text);
        } else if ("&prompt_confirm".equals(name)) {
            String text = BridgeUtilities.getString(args, "");
            String title = BridgeUtilities.getString(args, "");
            SafeDialogs.askYesNo(text, title, this.popCallback(args, script));
        } else if ("&prompt_text".equals(name)) {
            String text = BridgeUtilities.getString(args, "");
            String dval = BridgeUtilities.getString(args, "");
            SafeDialogs.ask(text, dval, this.popCallback(args, script));
        } else if ("&prompt_file_open".equals(name)) {
            String title = BridgeUtilities.getString(args, "");
            String dval = BridgeUtilities.getString(args, "");
            boolean multi = SleepUtils.isTrueScalar(BridgeUtilities.getScalar(args));
            SafeDialogs.openFile(title, dval, null, multi, false, this.popCallback(args, script));
        } else if ("&prompt_directory_open".equals(name)) {
            String title = BridgeUtilities.getString(args, "");
            String dval = BridgeUtilities.getString(args, "");
            boolean multi = SleepUtils.isTrueScalar(BridgeUtilities.getScalar(args));
            SafeDialogs.openFile(title, dval, null, multi, true, this.popCallback(args, script));
        } else if ("&prompt_file_save".equals(name)) {
            String dval = BridgeUtilities.getString(args, "");
            SafeDialogs.saveFile(null, dval, this.popCallback(args, script));
        }
        return SleepUtils.getEmptyScalar();
    }

}

