package parser;

import common.MudgeSanity;
import server.Resources;

public abstract class Parser {
    protected Resources resources;

    public Parser(Resources r) {
        this.resources = r;
    }

    public abstract boolean check(String var1, int var2);

    public abstract void parse(String var1, String var2) throws Exception;

    public boolean isOutput(int type) {
        return type == 0 || type == 30 || type == 32;
    }

    public void process(String text, String bid, int type) {
        try {
            if (this.check(text, type)) {
                this.parse(text, bid);
            }
        } catch (Exception ex) {
            MudgeSanity.logException("output parser", ex, false);
        }
    }
}

