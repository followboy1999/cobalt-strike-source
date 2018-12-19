package cortana.gui;

import ui.KeyHandler;

import javax.swing.*;

public interface ScriptableApplication {
    void bindKey(String var1, KeyHandler var2);

    JMenuBar getJMenuBar();

    boolean isHeadless();
}

