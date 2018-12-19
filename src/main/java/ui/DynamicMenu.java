package ui;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class DynamicMenu
        extends JMenu implements MenuListener {
    protected DynamicMenuHandler handler = null;

    public DynamicMenu(String s) {
        super(s);
        this.addMenuListener(this);
    }

    public void setHandler(DynamicMenuHandler h) {
        this.handler = h;
    }

    @Override
    public void menuSelected(MenuEvent ev) {
        if (this.handler != null) {
            this.handler.setupMenu(this);
        }
    }

    @Override
    public void menuCanceled(MenuEvent ev) {
        this.removeAll();
    }

    @Override
    public void menuDeselected(MenuEvent ev) {
        this.removeAll();
    }

    public interface DynamicMenuHandler {
        void setupMenu(JMenu var1);
    }

}

