package aggressor.ui;

import javax.swing.*;

public abstract class UseLookAndFeel {
    public static void set(String name, boolean value) {
        if (value) {
            UIManager.put(name, Boolean.TRUE);
        } else {
            UIManager.put(name, Boolean.FALSE);
        }
    }

    public abstract void setup();
}

