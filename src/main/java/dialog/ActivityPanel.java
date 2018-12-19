package dialog;

import aggressor.Prefs;
import common.CommonUtils;
import console.Activity;

import javax.swing.*;
import java.awt.*;

public class ActivityPanel
        extends JPanel implements Activity {
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

    public void touch() {
        CommonUtils.runSafe(() -> {
            if (ActivityPanel.this.label != null && !ActivityPanel.this.isShowing()) {
                ActivityPanel.this.label.setForeground(Prefs.getPreferences().getColor("tab.highlight.color", "#0000ff"));
            }
        });
    }

}

