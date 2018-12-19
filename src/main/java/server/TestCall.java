package server;

import common.Request;

import java.util.HashMap;
import java.util.Map;

public class TestCall implements ServerHook {
    @Override
    public void register(HashMap<String, Object> calls) {
        calls.put("test.a", this);
        calls.put("test.b", this);
        calls.put("test.beep", this);
    }

    @Override
    public void call(Request r, ManageUser client) {
        System.err.println("Received : " + r);
        client.writeNow(r.reply("Thanks for: " + r.getCall()));
    }
}

