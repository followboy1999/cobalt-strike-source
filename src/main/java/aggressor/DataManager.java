//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package aggressor;

import common.AdjustData;
import common.Callback;
import common.ChangeLog;
import common.CommonUtils;
import common.Keys;
import common.PlaybackStatus;
import common.ScriptUtils;
import common.Scriptable;
import common.Transcript;
import common.TranscriptReset;
import cortana.Cortana;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import sleep.runtime.Scalar;

public class DataManager implements Callback, GenericDataManager {
    protected Cortana engine;
    protected HashMap<String, Object> store = new HashMap<>();
    protected HashMap<String, LinkedList<Callback>> subs = new HashMap<>();
    protected HashMap<String, LinkedList<Object>> transcripts = new HashMap<>();
    protected boolean syncing = true;
    protected boolean alive = true;

    public void dead() {
        this.alive = false;
    }

    public boolean isAlive() {
        return this.alive;
    }

    private LinkedList<Object> getTranscript(String key) {
        synchronized(this) {
            return this.transcripts.computeIfAbsent(key, (k) -> new LinkedList<>());
        }
    }

    public List<String> getDataKeys() {
        synchronized(this) {
            HashSet<String> temp = new HashSet<>();
            temp.addAll(this.store.keySet());
            temp.addAll(this.transcripts.keySet());
            return new LinkedList<>(temp);
        }
    }

    public Object getDataSafe(String key) {
        synchronized(this) {
            Object value = this.get(key, null);
            if (value == null) {
                return null;
            } else if (value instanceof Map) {
                return new HashMap((Map)value);
            } else if (value instanceof List) {
                return new LinkedList((List)value);
            } else {
                return value instanceof Collection ? new HashSet((Collection)value) : value;
            }
        }
    }

    public Map<String, Object> getMapSafe(String key) {
        synchronized(this) {
            return (Map<String, Object>)this.get(key, Collections.emptyMap());
        }
    }

    public HashSet getSetSafe(String key) {
        synchronized(this) {
            return (HashSet)this.get(key, Collections.emptySet());
        }
    }

    public LinkedList getListSafe(String key) {
        synchronized(this) {
            return (LinkedList)this.get(key, Collections.emptyList());
        }
    }

    public LinkedList<Map<String, Object>> populateListAndSubscribe(String key, AdjustData actor) {
        synchronized(this) {
            if (this.isTranscript(key)) {
                CommonUtils.print_warn("populateListAndSubscribe: " + key + ", " + actor + ": wrong function");
            }

            List results = (List)this.get(key, Collections.emptyList());
            LinkedList<Map<String, Object>> returnv = CommonUtils.apply(key, results, actor);
            this.subscribe(key, actor);
            return returnv;
        }
    }

    public LinkedList<Map<String, Object>> populateAndSubscribe(String key, AdjustData actor) {
        synchronized(this) {
            if (this.isStore(key)) {
                CommonUtils.print_warn("populateAndSubscribe: " + key + ", " + actor + ": wrong function");
            }

            LinkedList results = this.getTranscriptSafe(key);
            LinkedList<Map<String, Object>> returnv = CommonUtils.apply(key, results, actor);
            this.subscribe(key, actor);
            return returnv;
        }
    }

    public LinkedList getTranscriptAndSubscribeSafe(String key, Callback actor) {
        synchronized(this) {
            LinkedList results = this.getTranscriptSafe(key);
            this.subscribe(key, actor);
            return results;
        }
    }

    public LinkedList getTranscriptSafe(String key) {
        synchronized(this) {
            return this.getTranscript(key);
        }
    }

    protected boolean isTranscript(String key) {
        synchronized(this) {
            return this.transcripts.containsKey(key);
        }
    }

    protected boolean isStore(String key) {
        synchronized(this) {
            return this.store.containsKey(key);
        }
    }

    public WindowCleanup unsubOnClose(String key, Callback l) {
        return new WindowCleanup(this, key, l);
    }

    public DataManager(Cortana e) {
        this.engine = e;
    }

    public void unsub(String key, Callback listener) {
        synchronized(this) {
            List<Callback> listeners = this.subs.get(key);
            listeners.remove(listener);
        }
    }

    public String key() {
        return this.hashCode() + "";
    }

    protected LinkedList<Callback> getSubs(String key) {
        synchronized(this) {
            return this.subs.computeIfAbsent(key, (k) -> {
                return new LinkedList();
            });
        }
    }

    protected LinkedList<Callback> getSubsSafe(String key) {
        synchronized(this) {
            return new LinkedList<>(this.getSubs(key));
        }
    }

    public void subscribe(String key, Callback l) {
        synchronized(this) {
            this.getSubs(key).add(l);
        }
    }

    public Map getModelDirect(String key, String child) {
        synchronized(this) {
            if (this.isDataModel(key)) {
                Map temp = (Map)this.store.get(key);
                return (temp == null ? new HashMap() : (Map)temp.get(child));
            } else {
                throw new RuntimeException("'" + key + "' is not a data model!");
            }
        }
    }

    public Object get(String key, Object defaultz) {
        synchronized(this) {
            if (this.isTranscript(key)) {
                return this.getTranscript(key);
            } else if (this.isStore(key)) {
                return this.isDataModel(key) ? new LinkedList(((Map)this.store.get(key)).values()) : this.store.get(key);
            } else {
                return defaultz;
            }
        }
    }

    public Map<Object, Object> getDataModel(String key) {
        synchronized(this) {
            return this.isStore(key) && this.isDataModel(key) ? new HashMap<>((Map<Object, Object>)this.store.get(key)) : new HashMap<>();
        }
    }

    public void put(String key, Object data) {
        synchronized(this) {
            this.store.put(key, data);
        }
    }

    public void put(String keyA, String keyB, Object data) {
        synchronized(this) {
            if (!this.store.containsKey(keyA)) {
                this.store.put(keyA, new HashMap());
            }

            if (this.store.get(keyA) instanceof Map) {
                Map top = (Map)this.store.get(keyA);
                top.put(keyB, data);
            } else {
                CommonUtils.print_error("DataManager.put: " + keyA + " -> " + keyB + " -> " + data + " applied to a non-Map incumbent (ignoring)");
            }

        }
    }

    public boolean isDataModel(String key) {
        return Keys.isDataModel(key);
    }

    public void result(String key, Object data) {
        synchronized(this) {
            if (data instanceof Transcript) {
                LinkedList<Object> model = this.getTranscript(key);
                model.add(data);

                while(model.size() >= CommonUtils.limit(key)) {
                    model.removeFirst();
                }
            } else if (this.isDataModel(key)) {
                if (data instanceof ChangeLog) {
                    Map<String,Map> original = (HashMap)this.store.get(key);
                    ChangeLog summary;
                    if (original == null) {
                        CommonUtils.print_error("data manager does not have: " + key + " [will apply summary to empty model]");
                        original = new HashMap<>();
                        summary = (ChangeLog)data;
                        summary.applyForce(original);
                        this.store.put(key, original);
                    } else {
                        summary = (ChangeLog)data;
                        summary.applyForce(original);
                    }
                } else {
                    this.store.put(key, data);
                }

                data = this.get(key, null);
            } else if (data instanceof PlaybackStatus) {
                PlaybackStatus status = (PlaybackStatus)data;
                if (status.isDone()) {
                    this.syncing = false;
                }
            } else if (data instanceof TranscriptReset) {
                this.transcripts = new HashMap<>();
            } else {
                this.store.put(key, data);
            }
        }

        Iterator<Callback> var3 = this.getSubsSafe(key).iterator();

        while(var3.hasNext()) {
            Callback next = var3.next();
            next.result(key, data);
        }

        if (!this.syncing) {
            if (data instanceof Scriptable) {
                Scriptable event = (Scriptable)data;
                Stack args = event.eventArguments();
                String name = event.eventName();
                this.engine.getEventManager().fireEvent(name, args);
            }

            if (this.engine.getEventManager().isLiveEvent(key)) {
                Stack<Scalar> args = new Stack<>();
                args.push(ScriptUtils.convertAll(data));
                this.engine.getEventManager().fireEvent(key, args);
            }
        }

        if (GlobalDataManager.getGlobalDataManager().isGlobal(key)) {
            GlobalDataManager.getGlobalDataManager().report(this, key, data);
        }

    }
}
