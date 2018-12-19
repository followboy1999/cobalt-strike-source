package server;

import common.Reply;
import common.Request;
import phish.Campaign;

import java.util.HashMap;
import java.util.Map;

public class Phisher implements ServerHook {
    protected Resources resources;
    protected Map<String, Campaign> campaigns = new HashMap<>();

    @Override
    public void register(HashMap<String, Object> calls) {
        calls.put("cloudstrike.go_phish", this);
        calls.put("cloudstrike.stop_phish", this);
    }

    public Phisher(Resources r) {
        this.resources = r;
    }

    @Override
    public void call(Request r, ManageUser client) {
        if (r.is("cloudstrike.go_phish", 3)) {
            synchronized (this) {
                String sid = (String) r.arg(0);
                this.campaigns.put(sid, new Campaign(this, r, client, this.resources));
            }
        } else if (r.is("cloudstrike.stop_phish", 1)) {
            synchronized (this) {
                String sid = (String) r.arg(0);
                Campaign temp = this.campaigns.get(sid);
                if (temp != null) {
                    temp.cancel();
                    this.campaigns.remove(sid);
                }
            }
        } else {
            client.writeNow(new Reply("server_error", 0L, r + ": incorrect number of arguments"));
        }
    }
}

