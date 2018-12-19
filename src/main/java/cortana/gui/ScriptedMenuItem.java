package cortana.gui;

import cortana.core.EventManager;
import sleep.bridges.SleepClosure;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

public class ScriptedMenuItem
        extends JMenuItem implements ActionListener {
    protected String label;
    protected SleepClosure code;
    protected MenuBridge bridge;
    protected Stack args;

    public ScriptedMenuItem(String label, SleepClosure code, MenuBridge bridge) {
        if (label.indexOf(38) > -1) {
            this.setText(label.substring(0, label.indexOf(38)) + label.substring(label.indexOf(38) + 1));
            this.setMnemonic(label.charAt(label.indexOf(38) + 1));
        } else {
            this.setText(label);
        }
        this.code = code;
        this.bridge = bridge;
        this.label = label;
        this.args = bridge.getArguments();
        this.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        SleepUtils.runCode(this.code, this.label, null, EventManager.shallowCopy(this.args));
    }
}

