package aggressor.bridges;

import common.CommonUtils;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.*;

public class AttackBridge implements Function,
        Loadable {
    protected List ids = new LinkedList();
    protected Map data = new HashMap();

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&attack_tactics", this);
        Cortana.put(si, "&attack_name", this);
        Cortana.put(si, "&attack_describe", this);
        Cortana.put(si, "&attack_mitigate", this);
        Cortana.put(si, "&attack_detect", this);
        Cortana.put(si, "&attack_url", this);
    }

    public void loadAttackMatrix() {
        if (this.ids.size() > 0) {
            return;
        }
        List matrix = SleepUtils.getListFromArray((Scalar) CommonUtils.readObjectResource("resources/attack.bin"));
        for (Object aMatrix : matrix) {
            Map entry = (Map) aMatrix;
            String id = (String) entry.get("id");
            this.ids.add(id);
            this.data.put(id, entry);
        }
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        this.loadAttackMatrix();
        if ("&attack_tactics".equals(name)) {
            return SleepUtils.getArrayWrapper(this.ids);
        }
        String id = BridgeUtilities.getString(args, "");
        Map entry = (Map) this.data.get(id);
        if ("&attack_name".equals(name)) {
            return SleepUtils.getScalar((String) entry.get("name"));
        }
        if ("&attack_describe".equals(name)) {
            return SleepUtils.getScalar((String) entry.get("describe"));
        }
        if ("&attack_mitigate".equals(name)) {
            return SleepUtils.getScalar((String) entry.get("mitigate"));
        }
        if ("&attack_detect".equals(name)) {
            return SleepUtils.getScalar((String) entry.get("detect"));
        }
        if ("&attack_url".equals(name)) {
            return SleepUtils.getScalar("https://attack.mitre.org/wiki/Technique/" + id);
        }
        return SleepUtils.getEmptyScalar();
    }
}

