package cortana;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class CortanaPipe implements Runnable {
    protected PipedInputStream readme;
    protected PipedOutputStream writeme;
    protected boolean run = true;
    protected List listeners = new LinkedList();

    public OutputStream getOutput() {
        return this.writeme;
    }

    public CortanaPipe() {
        try {
            this.readme = new PipedInputStream(1048576);
            this.writeme = new PipedOutputStream(this.readme);
        } catch (IOException ioex) {
            MudgeSanity.logException("create cortana pipe", ioex, false);
        }
    }

    public void addCortanaPipeListener(CortanaPipeListener l) {
        synchronized (this) {
            this.listeners.add(l);
        }
        if (this.listeners.size() == 1) {
            new Thread(this, "cortana pipe reader").start();
        }
    }

    public void close() {
        try {
            this.run = false;
            this.writeme.close();
        } catch (IOException ioex) {
            MudgeSanity.logException("close cortana pipe", ioex, false);
        }
    }

    @Override
    public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(this.readme));

        while (this.run) {
            try {
                String entry = in.readLine();
                if (entry != null) {
                    synchronized (this) {

                        for (Object listener : this.listeners) {
                            CortanaPipeListener l = (CortanaPipeListener) listener;
                            l.read(entry);
                        }
                    }
                }
            } catch (IOException var9) {
                CommonUtils.sleep(500L);
            }
        }
    }

    public interface CortanaPipeListener {
        void read(String var1);
    }

}

