package cortana.core;

import common.MudgeSanity;
import sleep.runtime.SleepUtils;

import java.util.LinkedList;
import java.util.Stack;

public class EventQueue implements Runnable {
    protected EventManager manager;
    protected LinkedList queue = new LinkedList();
    protected boolean run = true;

    public EventQueue(EventManager manager) {
        this.manager = manager;
        new Thread(this, "Aggressor Script Event Queue").start();
    }

    public void add(String name, Stack args) {
        Event e = new Event();
        e.name = name;
        e.args = args;
        synchronized (this) {
            if (this.manager.hasWildcardListener()) {
                this.queue.add(e.wildcard());
            }
            this.queue.add(e);
        }
    }

    protected Event grabEvent() {
        synchronized (this) {
            return (Event) this.queue.pollFirst();
        }
    }

    public void stop() {
        this.run = false;
    }

    @Override
    public void run() {
        while (this.run) {
            Event ev = this.grabEvent();
            try {
                if (ev != null) {
                    this.manager.fireEventNoQueue(ev.name, ev.args, null);
                    continue;
                }
                Thread.sleep(25L);
            } catch (Exception ex) {
                if (ev != null) {
                    MudgeSanity.logException("event: " + ev.name + "/" + SleepUtils.describe(ev.args), ex, false);
                    continue;
                }
                MudgeSanity.logException("event (none)", ex, false);
            }
        }
    }

    private static class Event {
        public String name;
        public Stack args;

        private Event() {
        }

        public Event wildcard() {
            Event rv = new Event();
            rv.name = "*";
            rv.args = new Stack();
            rv.args.addAll(this.args);
            rv.args.push(SleepUtils.getScalar(this.name));
            return rv;
        }
    }

}

