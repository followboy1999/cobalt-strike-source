package ui;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ATextField
        extends JTextField {
    protected JPopupMenu menu = null;

    public ATextField(int cols) {
        super(cols);
        this.createMenu();
    }

    public ATextField(Document doc, String text, int cols) {
        super(doc, text, cols);
        this.createMenu();
    }

    public ATextField(String text, int cols) {
        super(text, cols);
        this.createMenu();
    }

    public ATextField() {
        this.createMenu();
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
        cut.addActionListener(ev -> ATextField.this.cut());
        copy2.addActionListener(ev -> ATextField.this.copy());
        paste.addActionListener(ev -> ATextField.this.paste());
        clear.addActionListener(ev -> ATextField.this.setText(""));
        this.menu.add(cut);
        this.menu.add(copy2);
        this.menu.add(paste);
        this.menu.add(clear);
        this.addMouseListener(new MouseAdapter() {

            public void handle(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    ATextField.this.menu.show((JComponent) ev.getSource(), ev.getX(), ev.getY());
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

