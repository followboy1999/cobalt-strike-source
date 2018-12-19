package kerberos;

import common.DataParser;

import java.io.IOException;

public class Times {
    protected int authtime;
    protected int starttime;
    protected int endtime;
    protected int renew_till;

    public Times(DataParser parser) throws IOException {
        this.authtime = parser.readInt();
        this.starttime = parser.readInt();
        this.endtime = parser.readInt();
        this.renew_till = parser.readInt();
    }

    public String toString() {
        return "Times... meh";
    }
}

