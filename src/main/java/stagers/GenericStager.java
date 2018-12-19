package stagers;

import common.Listener;
import common.ListenerConfig;
import common.MudgeSanity;

public abstract class GenericStager implements Cloneable {
    protected Listener listener = null;

    public GenericStager(Listener l) {
    }

    public GenericStager create(Listener l) {
        try {
            GenericStager temp = (GenericStager) this.clone();
            temp.listener = l;
            return temp;
        } catch (CloneNotSupportedException cnse) {
            MudgeSanity.logException("can't clone", cnse, false);
            return null;
        }
    }

    public ListenerConfig getConfig() {
        return this.listener.getConfig();
    }

    public Listener getListener() {
        return this.listener;
    }

    public abstract String arch();

    public abstract String payload();

    public abstract byte[] generate();
}

