package stagers;

import common.CommonUtils;
import common.Listener;

public class ForeignHTTPStagerX86
        extends GenericHTTPStagerX86 {
    public ForeignHTTPStagerX86(Listener l) {
        super(l);
    }

    @Override
    public String payload() {
        return "windows/foreign/reverse_http";
    }

    @Override
    public String getURI() {
        return CommonUtils.MSFURI();
    }
}

