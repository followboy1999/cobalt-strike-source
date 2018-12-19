package aggressor;

import common.Callback;

import java.util.LinkedList;
import java.util.Map;

public interface GenericDataManager {
    void unsub(String var1, Callback var2);

    void subscribe(String var1, Callback var2);

    WindowCleanup unsubOnClose(String var1, Callback var2);

    Object get(String var1, Object var2);

    Map<String, Object> getMapSafe(String var1);

    LinkedList getListSafe(String var1);
}

