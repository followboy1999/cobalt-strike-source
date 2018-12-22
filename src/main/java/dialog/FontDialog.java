package dialog;

import common.CommonUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.LinkedList;

public class FontDialog implements ItemListener,
        DialogListener {
    protected Font font;
    protected JLabel preview;
    protected JComboBox size;
    protected JComboBox family;
    protected JComboBox style;
    protected LinkedList listeners = new LinkedList();
    protected JFrame dialog;

    public FontDialog(Font f) {
        this.font = f;
    }

    public void addFontChooseListener(SafeDialogCallback l) {
        this.listeners.add(l);
    }

    public JComboBox act(DialogManager.DialogRow r) {
        JComboBox box = (JComboBox) r.c[1];
        box.addItemListener(this);
        return box;
    }

    public String getResult() {
        return this.family.getSelectedItem() + "-" + this.style.getSelectedItem().toString().toUpperCase() + "-" + this.size.getSelectedItem();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Font f = Font.decode(this.getResult());
        this.preview.setFont(f);
        this.preview.revalidate();
    }

    @Override
    public void dialogAction(ActionEvent event, HashMap<String, Object> options) {
        for (Object listener : this.listeners) {
            SafeDialogCallback next = (SafeDialogCallback) listener;
            next.dialogResult(this.getResult());
        }
        this.dialog.dispose();
    }

    public void show() {
        this.dialog = DialogUtils.dialog("Choose a font", 640, 240);
        this.dialog.setLayout(new BorderLayout());
        DialogManager controller = new DialogManager(this.dialog);
        controller.addDialogListener(this);
        controller.set("size", this.font.getSize() + "");
        controller.set("family", this.font.getFamily());
        String stylez = "Plain";
        if (this.font.isItalic()) {
            stylez = "Italic";
        } else if (this.font.isBold()) {
            stylez = "Bold";
        }
        controller.set("style", stylez);
        GraphicsEnvironment graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        this.family = this.act(controller.combobox("family", "Family", graphics.getAvailableFontFamilyNames()));
        this.style = this.act(controller.combobox("style", "Style", CommonUtils.toArray("Bold, Italic, Plain")));
        this.size = this.act(controller.combobox("size", "Size", CommonUtils.toArray("5, 8, 9, 10, 11, 12, 13, 14, 15, 16, 20, 23, 26, 30, 33, 38")));
        this.preview = new JLabel("nEWBS gET pWNED by km-r4d h4x0rz \u8089\u9e21");
        this.preview.setFont(this.font);
        this.preview.setBackground(Color.white);
        this.preview.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.preview.setOpaque(true);
        JButton choose = controller.action("Choose");
        this.dialog.add(controller.layout(), "North");
        this.dialog.add(this.preview, "Center");
        this.dialog.add(DialogUtils.center(choose), "South");
        this.dialog.pack();
        this.dialog.setVisible(true);
    }
}

