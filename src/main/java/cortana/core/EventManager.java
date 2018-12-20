package cortana.core;

import sleep.bridges.SleepClosure;
import sleep.interfaces.Loadable;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.*;

public class EventManager {
    protected Map<String, LinkedList<Listener>> listeners = new HashMap<>();
    protected EventQueue queue = new EventQueue(this);
    protected boolean wildcards;

    protected List<Listener> getListener(String name) {
        synchronized (this) {
            if (this.listeners.containsKey(name)) {
                return this.listeners.get(name);
            }
            this.listeners.put(name, new LinkedList<>());
            return this.listeners.get(name);
        }
    }

    public Loadable getBridge() {
        return new Events(this);
    }

    public void stop() {
        this.queue.stop();
    }

    public boolean hasWildcardListener() {
        return this.wildcards;
    }

    public boolean isLiveEvent(String name) {
        return this.hasWildcardListener() || this.hasListener(name);
    }

    public void addListener(String listener, SleepClosure c, boolean temporary) {
        synchronized (this) {
            if ("*".equals(listener)) {
                this.wildcards = true;
            }
            this.getListener(listener).add(new Listener(c, temporary));
        }
    }

    public static Stack shallowCopy(Stack args) {
        Stack<Object> copy2 = new Stack<>();
        for (Object arg : args) {
            copy2.push(arg);
        }
        return copy2;
    }

    public void fireEvent(String eventName, Stack args) {
        this.queue.add(eventName, args);
    }

    public boolean hasListener(String eventName) {
        synchronized (this) {
            if (this.getListener(eventName).size() == 0) {
                return false;
            }
        }
        return true;
    }

    protected List<SleepClosure> getListeners(String eventName, ScriptInstance local) {
        Object lid = null;
        if (local != null) {
            lid = local.getMetadata().get("%scriptid%");
        }
        synchronized (this) {
            LinkedList<SleepClosure> callme = new LinkedList<>();
            Iterator<Listener> i = this.getListener(eventName).iterator();
            while (i.hasNext()) {
                Listener l = i.next();
                if (!l.getClosure().getOwner().isLoaded()) {
                    i.remove();
                    continue;
                }
                if (lid != null && !lid.equals(l.getClosure().getOwner().getMetadata().get("%scriptid%"))) continue;
                callme.add(l.getClosure());
                if (!l.isTemporary()) continue;
                i.remove();
            }
            return callme;
        }
    }

    public void fireEventNoQueue(String eventName, Stack args, ScriptInstance local) {
        if (this.hasListener(eventName)) {
            for (SleepClosure o : this.getListeners(eventName, local)) {
                SleepUtils.runCode(o, eventName, null, EventManager.shallowCopy(args));
            }
        }
    }

    private static class Listener {
        protected SleepClosure listener;
        protected boolean temporary;

        public Listener(SleepClosure listener, boolean temporary) {
            this.listener = listener;
            this.temporary = temporary;
        }

        public SleepClosure getClosure() {
            return this.listener;
        }

        public boolean isTemporary() {
            return this.temporary;
        }
    }

}

