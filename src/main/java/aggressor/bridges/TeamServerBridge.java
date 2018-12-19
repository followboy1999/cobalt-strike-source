package aggressor.bridges;

import common.Callback;
import common.DisconnectListener;
import common.TeamQueue;
import common.TeamSocket;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.engine.ObjectUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.Stack;
import java.util.stream.IntStream;

public class TeamServerBridge implements Function,
        Loadable,
        DisconnectListener {
    protected TeamQueue conn;
    protected Cortana engine;

    public TeamServerBridge(Cortana e, TeamQueue c) {
        this.engine = e;
        this.conn = c;
        c.addDisconnectListener(this);
    }

    @Override
    public void disconnected(TeamSocket s) {
        this.engine.getEventManager().fireEvent("disconnect", new Stack());
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&call", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&call")) {
            String method = BridgeUtilities.getString(args, "");
            Scalar func = (Scalar) args.pop();
            Callback cb = SleepUtils.isEmptyScalar(func) ? null : (Callback) ObjectUtilities.buildArgument(Callback.class, func, script);
            Object[] argz = IntStream.range(0, args.size()).mapToObj(x -> BridgeUtilities.getObject(args)).toArray();
            this.conn.call(method, argz, cb);
        }
        return SleepUtils.getEmptyScalar();
    }
}

