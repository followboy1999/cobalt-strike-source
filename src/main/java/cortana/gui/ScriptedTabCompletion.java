package cortana.gui;

import console.Console;
import console.GenericTabCompletion;

import java.util.Collection;

public class ScriptedTabCompletion
        extends GenericTabCompletion {
    protected Completer completer;

    public ScriptedTabCompletion(Console window, Completer c) {
        super(window);
        this.completer = c;
    }

    @Override
    public Collection getOptions(String text) {
        return this.completer.getOptions(text);
    }

    public interface Completer {
        Collection getOptions(String var1);
    }

}

