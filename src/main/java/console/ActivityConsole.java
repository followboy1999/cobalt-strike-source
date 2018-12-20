package console;

import aggressor.Prefs;

import javax.swing.*;
import java.awt.*;
import java.util.Properties;

public class ActivityConsole extends Console implements Activity {
    protected JLabel label;
    protected Color original;

    @Override
    public void registerLabel(JLabel l) {
        this.label = l;
        this.original = l.getForeground();
    }

    @Override
    public void resetNotification() {
        this.label.setForeground(this.original);
    }

    @Override
    protected void appendToConsole(String _text) {
        super.appendToConsole(_text);
        if (_text.length() > 0 && this.label != null && !this.isShowing()) {
            this.label.setForeground(Prefs.getPreferences().getColor("tab.highlight.color", "#0000ff"));
        }
    }

    public ActivityConsole(boolean status) {
        super(new Properties(), status);
    }

    public ActivityConsole(Properties preferences, boolean status) {
        super(preferences, status);
    }
}

