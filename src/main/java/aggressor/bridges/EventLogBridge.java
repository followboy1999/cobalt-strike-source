package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.CommonUtils;
import common.LoggedEvent;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.LinkedList;
import java.util.Stack;

public class EventLogBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public EventLogBridge(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&say", this);
        Cortana.put(si, "&privmsg", this);
        Cortana.put(si, "&action", this);
        Cortana.put(si, "&users", this);
        Cortana.put(si, "&elog", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if ("&say".equals(name)) {
            String nick = DataUtils.getNick(this.client.getData());
            String text = BridgeUtilities.getString(args, "");
            this.client.getConnection().call("aggressor.event", CommonUtils.args(LoggedEvent.Public(nick, text)), null);
        } else if ("&privmsg".equals(name)) {
            String nick = DataUtils.getNick(this.client.getData());
            String target = BridgeUtilities.getString(args, "");
            String text = BridgeUtilities.getString(args, "");
            this.client.getConnection().call("aggressor.event", CommonUtils.args(LoggedEvent.Private(nick, target, text)), null);
        } else if ("&action".equals(name)) {
            String nick = DataUtils.getNick(this.client.getData());
            String text = BridgeUtilities.getString(args, "");
            this.client.getConnection().call("aggressor.event", CommonUtils.args(LoggedEvent.Action(nick, text)), null);
        } else if ("&elog".equals(name)) {
            String text = BridgeUtilities.getString(args, "");
            this.client.getConnection().call("aggressor.event", CommonUtils.args(LoggedEvent.Notify(text)), null);
        } else if ("&users".equals(name)) {
            LinkedList users = new LinkedList(DataUtils.getUsers(this.client.getData()));
            return SleepUtils.getArrayWrapper(users);
        }
        return SleepUtils.getEmptyScalar();
    }
}

