package cortana.gui;

import cortana.core.EventManager;
import sleep.bridges.SleepClosure;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.util.Stack;

public class ScriptedMenu
        extends JMenu implements MenuListener {
    protected MenuBridge bridge;
    protected SleepClosure f;
    protected String label;
    protected Stack args;

    public ScriptedMenu(String _label, SleepClosure f, MenuBridge bridge) {
        if (_label.indexOf(38) > -1) {
            this.setText(_label.substring(0, _label.indexOf(38)) + _label.substring(_label.indexOf(38) + 1));
            this.setMnemonic(_label.charAt(_label.indexOf(38) + 1));
        } else {
            this.setText(_label);
        }
        this.label = _label;
        this.bridge = bridge;
        this.f = f;
        this.args = bridge.getArguments();
        this.addMenuListener(this);
    }

    @Override
    public void menuSelected(MenuEvent e) {
        this.bridge.push(this, this.args);
        SleepUtils.runCode(this.f, this.label, null, EventManager.shallowCopy(this.args));
        this.bridge.pop();
    }

    @Override
    public void menuDeselected(MenuEvent e) {
        this.removeAll();
    }

    @Override
    public void menuCanceled(MenuEvent e) {
        this.removeAll();
    }
}

