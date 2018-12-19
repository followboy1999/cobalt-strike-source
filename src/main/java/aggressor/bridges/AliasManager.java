package aggressor.bridges;

import common.ScriptUtils;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Loadable;
import sleep.runtime.SleepUtils;

import java.util.*;

public class AliasManager {
    protected Map commands = new HashMap();

    protected SleepClosure getCommand(String name) {
        synchronized (this) {
            if (this.commands.containsKey(name)) {
                SleepClosure temp = (SleepClosure) this.commands.get(name);
                if (temp.getOwner().isLoaded()) {
                    return temp;
                }
                this.commands.remove(name);
            }
            return null;
        }
    }

    public List commands() {
        synchronized (this) {
            return new LinkedList(this.commands.keySet());
        }
    }

    public Loadable getBridge() {
        return new Aliases(this);
    }

    public void registerCommand(String command, SleepClosure c) {
        synchronized (this) {
            this.commands.put(command, c);
        }
    }

    public boolean isAlias(String command) {
        return this.getCommand(command) != null;
    }

    public boolean fireCommand(String bid, String command, String args) {
        SleepClosure c = this.getCommand(command);
        if (c == null) {
            return false;
        }
        Stack argz = ScriptUtils.StringToArguments(command + " " + args);
        argz.push(SleepUtils.getScalar(bid));
        SleepUtils.runCode(c, command + " " + args, null, argz);
        return true;
    }
}

