package kerberos;

import common.CommonUtils;
import common.DataParser;
import common.MudgeSanity;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Ccache {
    protected Principal primary_principal;
    protected List credentials = new LinkedList();

    public Ccache(String file) {
        this.parse(CommonUtils.readFile(file));
    }

    public void parse(byte[] data) {
        try {
            DataParser parser = new DataParser(data);
            parser.big();
            int version = parser.readShort();
            if (version != 1284) {
                CommonUtils.print_error("VERSION FAIL: " + version);
                return;
            }
            int headerlen = parser.readShort();
            parser.consume(headerlen);
            this.primary_principal = new Principal(parser);
            while (parser.more()) {
                this.credentials.add(new Credential(parser));
            }
        } catch (IOException ioex) {
            MudgeSanity.logException("CredCacheParse", ioex, false);
        }
    }

    public String toString() {
        return this.primary_principal + "\n" + this.credentials;
    }

    public static void main(String[] args) {
        CommonUtils.print_good(new Ccache(args[0]) + "");
    }
}

