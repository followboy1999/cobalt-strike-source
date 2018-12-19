package common;

import sleep.runtime.Scalar;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class GenericEvent implements Serializable,
        Scriptable {
    protected String name;
    protected List args;

    public GenericEvent(String name, String argument) {
        this.name = name;
        this.args = new LinkedList();
        this.args.add(argument);
    }

    @Override
    public String eventName() {
        return this.name;
    }

    @Override
    public Stack eventArguments() {
        Stack<Scalar> temp = new Stack<>();
        for (Object arg : this.args) {
            temp.push(ScriptUtils.convertAll(arg));
        }
        return temp;
    }
}

