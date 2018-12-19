package common;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Timers implements Runnable {
    private static Timers mytimer = null;
    protected List timers = new LinkedList();

    public static synchronized Timers getTimers() {
        if (mytimer == null) {
            mytimer = new Timers();
        }

        return mytimer;
    }

    public void every(long every, String msg, Do action) {
        synchronized (this) {
            this.timers.add(new Timers.ActionItem(action, msg, every));
        }
    }

    private Timers() {
        (new Thread(this, "global timer")).start();
    }

    public void fire(Timers.ActionItem item) {
        item.moment();
    }

    public void run() {
        LinkedList working;

        while (true) {
            synchronized (this) {
                working = new LinkedList(this.timers);
            }

            long now = System.currentTimeMillis();

            for (Object aWorking : working) {
                ActionItem next = (ActionItem) aWorking;
                if (next.isDue(now)) {
                    this.fire(next);
                }
            }

            synchronized (this) {
                Iterator j = this.timers.iterator();

                while (true) {
                    if (!j.hasNext()) {
                        break;
                    }

                    Timers.ActionItem next = (Timers.ActionItem) j.next();
                    if (!next.shouldKeep()) {
                        j.remove();
                    }
                }
            }

            CommonUtils.sleep(1000L);
        }
    }

    private static class ActionItem {
        public Do action;
        public long every;
        public long last;
        public boolean keep = true;
        public String msg;

        public ActionItem(Do action, String msg, long every) {
            this.action = action;
            this.every = every;
            this.last = 0L;
            this.msg = msg;
        }

        public boolean isDue(long now) {
            return now - this.last >= this.every;
        }

        public void moment() {
            try {
                this.last = System.currentTimeMillis();
                this.keep = this.action.moment(this.msg);
            } catch (Exception var2) {
                MudgeSanity.logException("timer to " + this.action.getClass() + "/" + this.msg + " every " + this.last + "ms", var2, false);
                this.keep = false;
            }

        }

        public boolean shouldKeep() {
            return this.keep;
        }
    }
}
