package aggressor.bridges;

import aggressor.TabManager;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

public class TabBridge implements Function,
        Loadable {
    protected TabManager manager;
    protected Cortana engine;

    public TabBridge(Cortana e, TabManager m) {
        this.engine = e;
        this.manager = m;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&nextTab", this);
        Cortana.put(si, "&previousTab", this);
        Cortana.put(si, "&addTab", this);
        Cortana.put(si, "&removeTab", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        switch (name) {
            case "&nextTab":
                this.manager.nextTab();
                break;
            case "&previousTab":
                this.manager.previousTab();
                break;
            case "&addTab": {
                String title = BridgeUtilities.getString(args, "");
                Object comp = BridgeUtilities.getObject(args);
                String tool = BridgeUtilities.getString(args, null);
                this.manager.addTab(title, (JComponent) comp, new TabRemoveListener(script, title, (JComponent) comp), tool);
                break;
            }
            case "&removeTab": {
                Object comp = BridgeUtilities.getObject(args);
                this.manager.removeTab((JComponent) comp);
                break;
            }
        }
        return SleepUtils.getEmptyScalar();
    }

    private class TabRemoveListener implements ActionListener {
        protected String title;
        protected JComponent comp;
        protected ScriptInstance ctx;

        public TabRemoveListener(ScriptInstance ctx, String title, JComponent comp) {
            this.title = title;
            this.comp = comp;
            this.ctx = ctx;
        }

        @Override
        public void actionPerformed(ActionEvent ev) {
            Stack<Scalar> args = new Stack<>();
            args.push(SleepUtils.getScalar(this.comp));
            args.push(SleepUtils.getScalar(this.title));
        }
    }

}

