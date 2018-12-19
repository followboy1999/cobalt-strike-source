package aggressor.ui;

import javax.swing.*;

public class UseNimbus
        extends UseLookAndFeel {
    @Override
    public void setup() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (!"Nimbus".equals(info.getName())) continue;
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        } catch (Exception e) {
            // empty catch block
        }
    }
}

