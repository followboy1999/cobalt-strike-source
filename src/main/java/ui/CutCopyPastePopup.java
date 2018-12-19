package ui;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CutCopyPastePopup {
    protected JPopupMenu menu = null;
    protected JTextComponent component;

    public CutCopyPastePopup(JTextComponent component) {
        this.component = component;
        this.createMenu();
    }

    public void createMenu() {
        if (this.menu != null) {
            return;
        }
        this.menu = new JPopupMenu();
        JMenuItem cut = new JMenuItem("Cut", 67);
        cut.addActionListener(ev -> CutCopyPastePopup.this.component.cut());
        JMenuItem copy2 = new JMenuItem("Copy", 111);
        copy2.addActionListener(ev -> CutCopyPastePopup.this.component.copy());
        JMenuItem paste = new JMenuItem("Paste", 112);
        paste.addActionListener(ev -> CutCopyPastePopup.this.component.paste());
        JMenuItem clear = new JMenuItem("Clear", 108);
        clear.addActionListener(ev -> CutCopyPastePopup.this.component.setText(""));
        this.menu.add(cut);
        this.menu.add(copy2);
        this.menu.add(paste);
        this.menu.add(clear);
        this.component.addMouseListener(new MouseAdapter() {

            public void handle(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    CutCopyPastePopup.this.menu.show((JComponent) ev.getSource(), ev.getX(), ev.getY());
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

