package ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DoubleClickWatch
        extends MouseAdapter {
    protected DoubleClickListener l;

    public DoubleClickWatch(DoubleClickListener l) {
        this.l = l;
    }

    @Override
    public void mouseClicked(MouseEvent ev) {
        if (ev.getClickCount() >= 2) {
            this.l.doubleClicked(ev);
            ev.consume();
        }
    }
}

