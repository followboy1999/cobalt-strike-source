package cortana.gui;

import cortana.core.EventManager;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Loadable;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Stack;

public class MenuBuilder {
    protected MenuBridge bridge;

    public MenuBuilder(ScriptableApplication app) {
        this.bridge = new MenuBridge(app, this);
    }

    public Loadable getBridge() {
        return this.bridge;
    }

    public void installMenu(MouseEvent ev, String key, Stack argz) {
        if (ev.isPopupTrigger() && this.bridge.isPopulated(key)) {
            JPopupMenu menu = new JPopupMenu();
            this.setupMenu(menu, key, argz);
            if (this.bridge.isPopulated(key)) {
                menu.show((JComponent) ev.getSource(), ev.getX(), ev.getY());
                ev.consume();
            }
        }
    }

    public void setupMenu(JComponent parent, String key, Stack argz) {
        if (!this.bridge.isPopulated(key)) {
            return;
        }
        this.bridge.push(parent, argz);
        Iterator i = this.bridge.getMenus(key).iterator();
        while (i.hasNext()) {
            SleepClosure f = (SleepClosure) i.next();
            if (f.getOwner().isLoaded()) {
                SleepUtils.runCode(f, key, null, EventManager.shallowCopy(argz));
                continue;
            }
            i.remove();
        }
        this.bridge.pop();
    }
}

