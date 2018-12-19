package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.Prefs;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.List;
import java.util.Stack;

public class PreferencesBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public PreferencesBridge(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&pref_set", this);
        Cortana.put(si, "&pref_set_list", this);
        Cortana.put(si, "&pref_get", this);
        Cortana.put(si, "&pref_get_list", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if ("&pref_set".equals(name)) {
            String key = BridgeUtilities.getString(args, "");
            String val = BridgeUtilities.getString(args, "");
            Prefs.getPreferences().set(key, val);
            Prefs.getPreferences().save();
        } else if ("&pref_set_list".equals(name)) {
            String key = BridgeUtilities.getString(args, "");
            List val = SleepUtils.getListFromArray(BridgeUtilities.getScalar(args));
            Prefs.getPreferences().setList(key, val);
            Prefs.getPreferences().save();
        } else {
            if ("&pref_get".equals(name)) {
                String key = BridgeUtilities.getString(args, "");
                String dval = BridgeUtilities.getString(args, "");
                return SleepUtils.getScalar(Prefs.getPreferences().getString(key, dval));
            }
            if ("&pref_get_list".equals(name)) {
                String key = BridgeUtilities.getString(args, "");
                return SleepUtils.getArrayWrapper(Prefs.getPreferences().getList(key));
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}

