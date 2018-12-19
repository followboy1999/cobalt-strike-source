package server;

import common.CommonUtils;
import common.MudgeSanity;
import common.Reply;
import common.Request;

import java.util.LinkedList;
import java.util.Map;

public class ServerBus implements Runnable {
    protected LinkedList<ServerRequest> requests = new LinkedList<>();
    protected Map calls;

    protected ServerRequest grabRequest() {
        synchronized (this) {
            return this.requests.pollFirst();
        }
    }

    protected void addRequest(ManageUser client, Request request) {
        synchronized (this) {
            while (this.requests.size() > 100000) {
                this.requests.removeFirst();
            }
            this.requests.add(new ServerRequest(client, request));
        }
    }

    public ServerBus(Map calls) {
        this.calls = calls;
        new Thread(this, "server call bus").start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                ServerRequest next;
                if ((next = this.grabRequest()) != null) {
                    Request r = next.request;
                    if (this.calls.containsKey(r.getCall())) {
                        ServerHook callme = (ServerHook) this.calls.get(r.getCall());
                        callme.call(r, next.client);
                    } else if (next.client != null) {
                        next.client.write(new Reply("server_error", 0L, r + ": unknown call [or bad arguments]"));
                    } else {
                        CommonUtils.print_error("server_error " + next + ": unknown call " + r.getCall() + " [or bad arguments]");
                    }
                    Thread.yield();
                    continue;
                }
                Thread.sleep(25L);
            }
        } catch (Exception ex) {
            MudgeSanity.logException("server call bus loop", ex, false);
        }
    }

    private static class ServerRequest {
        public ManageUser client;
        public Request request;

        public ServerRequest(ManageUser client, Request request) {
            this.client = client;
            this.request = request;
        }
    }

}

