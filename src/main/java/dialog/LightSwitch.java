package dialog;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.LinkedList;
import java.util.List;

public class LightSwitch implements ChangeListener {
    protected JCheckBox the_switch = null;
    protected LinkedList components = new LinkedList();
    protected boolean negate = false;

    @Override
    public void stateChanged(ChangeEvent ev) {
        this.check();
    }

    public void check() {
        boolean doit = this.the_switch.isSelected();
        if (this.negate) {
            doit = !doit;
        }
        for (Object component : this.components) {
            JComponent c = (JComponent) component;
            c.setEnabled(doit);
        }
    }

    public void set(JCheckBox s, boolean n) {
        this.the_switch = s;
        this.negate = n;
        this.the_switch.addChangeListener(this);
        this.check();
    }

    public void set(DialogManager.DialogRow row, boolean n) {
        this.set((JCheckBox) row.get(1), n);
    }

    public void add(DialogManager.DialogRow row) {
        this.add(row.get(0));
        this.add(row.get(1));
        this.add(row.get(2));
    }

    public void add(List rows) {
        for (Object row : rows) {
            DialogManager.DialogRow next = (DialogManager.DialogRow) row;
            this.add(next);
        }
    }

    public void add(JComponent component) {
        if (component != null) {
            this.components.add(component);
        }
    }
}

