package server;

import common.CommonUtils;
import common.MudgeSanity;

import java.io.*;

public class PersistentData<T> implements Runnable {
    protected String model;
    protected T value = null;
    protected final Object lock;

    public PersistentData(String model, Object lock) {
        this.model = model;
        this.lock = lock;
        new Thread(this, "save thread for: " + model).start();
    }

    public void save(T value) {
        synchronized (this.lock) {
            this.value = value;
        }
    }

    private void _save() {
        try {
            new File("data").mkdirs();
            File outf = CommonUtils.SafeFile("data", this.model + ".bin");
            ObjectOutputStream objout = new ObjectOutputStream(new FileOutputStream(outf, false));
            objout.writeObject(this.value);
            objout.close();
        } catch (Exception ex) {
            MudgeSanity.logException("save " + this.model, ex, false);
        }
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this.lock) {
                if (this.value != null) {
                    this._save();
                    this.value = null;
                }
            }
            CommonUtils.sleep(10000L);
        }
    }

    @SuppressWarnings("unchecked")
    public T getValue(T defaultv) {
        try {
            File temp = CommonUtils.SafeFile("data", this.model + ".bin");
            if (temp.exists()) {
                ObjectInputStream objin = new ObjectInputStream(new FileInputStream(temp));
                T value =(T) objin.readObject();
                objin.close();

                return value;
            }
        } catch (Exception ex) {
            MudgeSanity.logException("load " + this.model, ex, false);
            CommonUtils.print_error("the " + this.model + " model will start empty [everything is OK]");
        }
        return defaultv;
    }
}

