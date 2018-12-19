package beacon;

import common.*;

import java.io.Serializable;
import java.util.Map;

public interface CheckinListener {
    void checkin(BeaconEntry var1);

    void output(BeaconOutput var1);

    void update(String var1, long var2, String var4, boolean var5);

    void screenshot(Screenshot var1);

    void keystrokes(Keystrokes var1);

    void download(Download var1);

    void push(String var1, Serializable var2);

    BeaconEntry resolve(String var1);

    Map buildBeaconModel();
}

