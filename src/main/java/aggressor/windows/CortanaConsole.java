package aggressor.windows;

import console.Console;
import console.ConsolePopup;
import cortana.ConsoleInterface;
import cortana.Cortana;
import cortana.CortanaPipe;
import cortana.CortanaTabCompletion;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Stack;

public class CortanaConsole implements CortanaPipe.CortanaPipeListener,
        ActionListener,
        ConsolePopup {
    protected Console console = new Console();
    protected Cortana engine;
    protected ConsoleInterface myinterface;

    public CortanaConsole(Cortana engine) {
        this.console.updatePrompt("\u001faggressor\u000f> ");
        engine.addTextListener(this);
        this.console.getInput().addActionListener(this);
        this.engine = engine;
        this.myinterface = engine.getConsoleInterface();
        new CortanaTabCompletion(this.console, engine);
        this.console.setPopupMenu(this);
    }

    public Console getConsole() {
        return this.console;
    }

    @Override
    public void showPopup(String word, MouseEvent ev) {
        this.engine.getMenuBuilder().installMenu(ev, "aggressor", new Stack());
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        String text = ev.getActionCommand();
        this.console.append("\u001faggressor\u000f> " + text + "\n");
        ((JTextField) ev.getSource()).setText("");
        if (!"".equals(text)) {
            this.myinterface.processCommand(text);
        }
    }

    @Override
    public void read(String text) {
        this.console.append(text + "\n");
    }
}

