package stagers;

import common.Listener;

public abstract class GenericHTTPSStagerX86
        extends GenericHTTPStager {
    public GenericHTTPSStagerX86(Listener l) {
        super(l);
    }

    @Override
    public int getExitOffset() {
        return 745;
    }

    @Override
    public int getPortOffset() {
        return 196;
    }

    @Override
    public int getSkipOffset() {
        return 773;
    }

    @Override
    public String arch() {
        return "x86";
    }

    @Override
    public String getStagerFile() {
        return "resources/httpsstager.bin";
    }
}

