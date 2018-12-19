package console;

import aggressor.Prefs;
import common.CommonUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class StatusBar
        extends JPanel {
    protected JTextPane left = new JTextPane();
    protected JTextPane right = new JTextPane();
    protected Colors colors;

    public StatusBar(Properties props) {
        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.left.setEditable(false);
        this.right.setEditable(false);
        Color foreground = Prefs.getPreferences().getColor("statusbar.foreground.color", "#000000");
        this.left.setForeground(foreground);
        this.right.setForeground(foreground);
        this.add(this.left, "West");
        this.add(this.right, "East");
        this.colors = new Colors(props);
        Color background = Prefs.getPreferences().getColor("statusbar.background.color", "#d3d3d3");
        this.setBackground(background);
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        if (this.left != null) {
            this.left.setBackground(color);
        }
        if (this.right != null) {
            this.right.setBackground(color);
        }
    }

    public void left(String text) {
        this.updateText(this.left, text);
    }

    public void right(String text) {
        this.updateText(this.right, text);
    }

    public void set(final String textl, final String textr) {
        CommonUtils.runSafe(() -> {
            StatusBar.this.colors.setNoHack(StatusBar.this.left, textl);
            StatusBar.this.colors.setNoHack(StatusBar.this.right, textr);
        });
    }

    protected void updateText(final JTextPane side, final String text) {
        CommonUtils.runSafe(() -> StatusBar.this.colors.set(side, text));
    }

    @Override
    public void setFont(Font f) {
        if (this.left != null) {
            this.left.setFont(f);
        }
        if (this.right != null) {
            this.right.setFont(f);
        }
    }

}

