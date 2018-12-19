package ui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CopyPopup {
    protected JPopupMenu menu = null;
    protected JTextComponent component;

    public CopyPopup(JTextComponent component) {
        this.component = component;
        this.createMenu();
    }

    public void createMenu() {
        if (this.menu != null) {
            return;
        }
        this.menu = new JPopupMenu();
        JMenuItem copy2 = new JMenuItem("Copy", 111);
        copy2.addActionListener(ev -> CopyPopup.this.component.copy());
        this.menu.add(copy2);
        this.component.addMouseListener(new MouseAdapter() {

            public void handle(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    CopyPopup.this.menu.show((JComponent) ev.getSource(), ev.getX(), ev.getY());
                }
            }

            @Override
            public void mousePressed(MouseEvent ev) {
                this.handle(ev);
            }

            @Override
            public void mouseClicked(MouseEvent ev) {
                this.handle(ev);
            }

            @Override
            public void mouseReleased(MouseEvent ev) {
                this.handle(ev);
            }
        });
    }

}

