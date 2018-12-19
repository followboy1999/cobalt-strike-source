package cortana;

import console.Console;
import console.GenericTabCompletion;

import java.util.Collection;

public class CortanaTabCompletion
        extends GenericTabCompletion {
    protected ConsoleInterface myinterface;

    @Override
    public String transformText(String text) {
        return text.replace(" ~", " " + System.getProperty("user.home"));
    }

    public CortanaTabCompletion(Console window, Cortana engine) {
        super(window);
        this.myinterface = engine.getConsoleInterface();
    }

    @Override
    public Collection getOptions(String text) {
        return this.myinterface.commandList(text);
    }
}

