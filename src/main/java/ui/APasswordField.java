package ui;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class APasswordField
        extends JPasswordField {
    protected JPopupMenu menu = null;

    public APasswordField(int cols) {
        super(cols);
        this.createMenu();
    }

    public APasswordField(Document doc, String text, int cols) {
        super(doc, text, cols);
        this.createMenu();
    }

    public APasswordField(String text, int cols) {
        super(text, cols);
        this.createMenu();
    }

    public APasswordField() {
        this.createMenu();
    }

    public String getPass() {
        return new String(this.getPassword());
    }

    public void createMenu() {
        if (this.menu != null) {
            return;
        }
        this.menu = new JPopupMenu();
        JMenuItem cut = new JMenuItem("Cut", 67);
        JMenuItem copy2 = new JMenuItem("Copy", 111);
        JMenuItem paste = new JMenuItem("Paste", 80);
        JMenuItem clear = new JMenuItem("Clear", 108);
        cut.addActionListener(ev -> APasswordField.this.cut());
        copy2.addActionListener(ev -> APasswordField.this.copy());
        paste.addActionListener(ev -> APasswordField.this.paste());
        clear.addActionListener(ev -> APasswordField.this.setText(""));
        this.menu.add(cut);
        this.menu.add(copy2);
        this.menu.add(paste);
        this.menu.add(clear);
        this.addMouseListener(new MouseAdapter() {

            public void handle(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    APasswordField.this.menu.show((JComponent) ev.getSource(), ev.getX(), ev.getY());
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

