package kerberos;

import common.CommonUtils;
import common.DataParser;

import java.io.IOException;

public class KeyBlock {
    protected int keytype;
    protected int etype;
    protected int keylen;
    protected byte[] keyvalue;

    public KeyBlock(DataParser parser) throws IOException {
        this.keytype = parser.readShort();
        this.etype = parser.readShort();
        this.keylen = parser.readShort();
        this.keyvalue = parser.readBytes(this.keylen);
    }

    public String toString() {
        return "KeyBlock: " + this.keytype + "/" + this.etype + " " + CommonUtils.toHexString(this.keyvalue);
    }
}

