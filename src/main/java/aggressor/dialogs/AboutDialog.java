package aggressor.dialogs;

import aggressor.Aggressor;
import common.AObject;
import common.CommonUtils;
import dialog.DialogUtils;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class AboutDialog
        extends AObject {
    public void show() {
        JFrame dialog = DialogUtils.dialog("About", 320, 200);
        dialog.setLayout(new BorderLayout());
        JLabel label = new JLabel(DialogUtils.getIcon("resources/armitage-logo.gif"));
        label.setBackground(Color.black);
        label.setForeground(Color.gray);
        label.setOpaque(true);
        JTextArea credits = new JTextArea();
        credits.setBackground(Color.black);
        credits.setForeground(Color.gray);
        credits.setEditable(false);
        credits.setFocusable(false);
        credits.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        credits.setOpaque(false);
        credits.setLineWrap(true);
        credits.setWrapStyleWord(true);
        String about = CommonUtils.bString(CommonUtils.readResource("resources/about.html"));
        label.setText(about);
        credits.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        ((DefaultCaret) credits.getCaret()).setUpdatePolicy(1);
        JScrollPane scroll = new JScrollPane(credits, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(scroll.getWidth(), 100));
        String text = CommonUtils.bString(CommonUtils.readResource("resources/credits.txt"));
        credits.setText(text);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        dialog.add(label, "Center");
        dialog.add(scroll, "South");
        dialog.pack();
        dialog.setLocationRelativeTo(Aggressor.getFrame());
        dialog.setVisible(true);
    }
}

