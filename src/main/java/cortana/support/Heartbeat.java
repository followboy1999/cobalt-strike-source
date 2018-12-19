package cortana.support;

import common.MudgeSanity;
import cortana.Cortana;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Heartbeat implements Runnable {
    protected Cortana engine;
    protected List beats;

    public Heartbeat(Cortana e) {
        this.engine = e;
        this.beats = new LinkedList();
        this.beats.add(new Beat("heartbeat_1s", 1000L));
        this.beats.add(new Beat("heartbeat_5s", 5000L));
        this.beats.add(new Beat("heartbeat_10s", 10000L));
        this.beats.add(new Beat("heartbeat_15s", 15000L));
        this.beats.add(new Beat("heartbeat_30s", 30000L));
        this.beats.add(new Beat("heartbeat_1m", 60000L));
        this.beats.add(new Beat("heartbeat_5m", 300000L));
        this.beats.add(new Beat("heartbeat_10m", 600000L));
        this.beats.add(new Beat("heartbeat_15m", 900000L));
        this.beats.add(new Beat("heartbeat_20m", 1200000L));
        this.beats.add(new Beat("heartbeat_30m", 1800000L));
        this.beats.add(new Beat("heartbeat_60m", 3600000L));
    }

    public void start() {
        new Thread(this, "heartbeat thread").start();
    }

    @Override
    public void run() {
        while (this.engine.isActive()) {
            try {
                long now = System.currentTimeMillis();
                for (Object beat : this.beats) {
                    Beat temp = (Beat) beat;
                    temp.check(now);
                }
                Thread.sleep(1000L);
            } catch (Exception ex) {
                MudgeSanity.logException("heartbeat error", ex, false);
            }
        }
        this.engine = null;
    }

    private class Beat {
        protected long next;
        protected long mark;
        protected String event;

        public Beat(String event, long mark2) {
            this.mark = mark2;
            this.event = event;
            this.next = System.currentTimeMillis() + mark2;
        }

        public void check(long now) {
            if (this.next <= now) {
                this.next = System.currentTimeMillis() + this.mark;
                Heartbeat.this.engine.getEventManager().fireEvent(this.event, new Stack());
            }
        }
    }

}

