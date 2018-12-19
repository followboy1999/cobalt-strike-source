package cortana.core;

import sleep.bridges.SleepClosure;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import java.util.*;

public class CommandManager {
    protected HashMap<String, SleepClosure> commands = new HashMap<>();

    protected SleepClosure getCommand(String name) {
        if (this.commands.containsKey(name)) {
            SleepClosure temp = (SleepClosure) this.commands.get(name);
            if (temp.getOwner().isLoaded()) {
                return temp;
            }
            this.commands.remove(name);
        }
        return null;
    }

    public List<String> commandList(String filter) {
        LinkedList<String> res = new LinkedList<>();
        for (Map.Entry<String, SleepClosure> temp : this.commands.entrySet()) {
            String command = temp.getKey() + "";
            SleepClosure f = temp.getValue();
            if (filter != null && !command.startsWith(filter)) continue;
            if (f.getOwner().isLoaded()) {
                res.add(command);
                continue;
            }
            this.commands.remove(command, f);
        }
        return res;
    }

    public Loadable getBridge() {
        return new Commands(this);
    }

    public void registerCommand(String command, SleepClosure c) {
        this.commands.put(command, c);
    }

    public boolean fireCommand(String command, String args) {
        Stack<Scalar> tokens = new Stack<>();
        StringBuilder token = new StringBuilder();
        for (int x = 0; x < args.length(); ++x) {
            char temp = args.charAt(x);
            if (temp == ' ') {
                if (token.length() > 0) {
                    tokens.add(0, SleepUtils.getScalar(token.toString()));
                }
                token = new StringBuilder();
                continue;
            }
            if (temp == '\"' && token.length() == 0) {
                ++x;
                while (x < args.length() && args.charAt(x) != '\"') {
                    token.append(args.charAt(x));
                    ++x;
                }
                tokens.add(0, SleepUtils.getScalar(token.toString()));
                token = new StringBuilder();
                continue;
            }
            token.append(temp);
        }
        if (token.length() > 0) {
            tokens.add(0, SleepUtils.getScalar(token.toString()));
        }
        tokens.pop();
        return this.fireCommand(command, args, tokens);
    }

    public boolean fireCommand(String command, String argz, Stack args) {
        SleepClosure c = this.getCommand(command);
        if (c == null) {
            return false;
        }
        SleepUtils.runCode(c, argz, null, EventManager.shallowCopy(args));
        return true;
    }
}

