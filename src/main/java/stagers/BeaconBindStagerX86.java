package stagers;

import common.Listener;

public class BeaconBindStagerX86
        extends GenericBindStager {
    public BeaconBindStagerX86(Listener l) {
        super(l);
    }

    @Override
    public String arch() {
        return "x86";
    }

    @Override
    public String getFile() {
        return "resources/bind.bin";
    }

    @Override
    public int getPortOffset() {
        return 204;
    }

    @Override
    public int getDataOffset() {
        return 312;
    }
}

