package common;

import java.io.Serializable;

public class Request implements Serializable {
    protected String call;
    protected Object[] args;
    protected long callback_ref;

    public Request(String call, Object[] args, long callback_ref) {
        this.call = call;
        this.args = args;
        this.callback_ref = callback_ref;
    }

    public Reply reply(Object reply) {
        return new Reply(this.call, this.callback_ref, reply);
    }

    public Request derive(String call, Object[] args) {
        return new Request(call, args, this.callback_ref);
    }

    public Request derive(String call) {
        return new Request(call, this.args, this.callback_ref);
    }

    public String getCall() {
        return this.call;
    }

    public boolean is(String name) {
        return this.call.equals(name);
    }

    public boolean is(String name, int targs) {
        return this.getCall().equals(name) && this.size() == targs;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public Object arg(int x) {
        return this.args[x];
    }

    public String argz(int x) {
        return (String) this.arg(x);
    }

    public int size() {
        if (this.args == null) {
            return 0;
        }
        return this.args.length;
    }

    public String toString() {
        return "Request '" + this.getCall() + "' with " + this.size() + " args";
    }
}

