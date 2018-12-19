package logger;

import common.CommonUtils;
import common.MudgeSanity;

import java.util.LinkedList;

public abstract class ProcessBackend implements Runnable {
    protected LinkedList<Object> tasks = new LinkedList<>();

    public void start(String processName) {
        new Thread(this, processName).start();
    }

    public void act(Object item) {
        synchronized (this) {
            this.tasks.add(item);
        }
    }

    protected Object grabTask() {
        synchronized (this) {
            return this.tasks.pollFirst();
        }
    }

    public abstract void process(Object var1);

    @Override
    public void run() {
        while (true) {
            Object next;
            if ((next = this.grabTask()) != null) {
                try {
                    this.process(next);
                } catch (Exception ex) {
                    MudgeSanity.logException("ProcessBackend: " + next.getClass(), ex, false);
                }
                Thread.yield();
                continue;
            }
            CommonUtils.sleep(10000L);
        }
    }
}

