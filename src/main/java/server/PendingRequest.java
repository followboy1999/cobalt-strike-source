package server;

import common.Request;

public class PendingRequest {
    protected Request request;
    protected ManageUser client;

    public PendingRequest(Request r, ManageUser c) {
        this.request = r;
        this.client = c;
    }

    public void action(String response) {
        this.client.write(this.request.reply(response));
    }
}

