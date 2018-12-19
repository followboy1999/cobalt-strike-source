package server;

import common.ChangeLog;
import common.Reply;
import common.Request;

import java.util.HashMap;
import java.util.Map;

public class DataCalls implements ServerHook {
    protected Resources resources;
    protected HashMap<String,Map> data;
    protected PersistentData<HashMap<String,Map>> store;
    protected String model;
    protected long previousid = -1L;
    protected ChangeLog changes;

    public void save() {
        this.store.save(this.data);
    }

    @Override
    public void register(HashMap<String, Object> calls) {
        calls.put(this.model + ".add", this);
        calls.put(this.model + ".addnew", this);
        calls.put(this.model + ".update", this);
        calls.put(this.model + ".remove", this);
        calls.put(this.model + ".push", this);
        calls.put(this.model + ".reset", this);
    }

    public DataCalls(Resources r, String m) {
        this.resources = r;
        this.model = m;
        this.changes = new ChangeLog(m);
        this.store = new PersistentData<>(this.model, this);
        this.data = this.store.getValue(new HashMap<>());
        this.resources.broadcast(this.model, this.buildDataModel(), true);
    }

    public void push() {
        synchronized (this) {
            this.changes.applyOptimize(this.data);
            if (this.changes.isDifferent()) {
                this.save();
                if (this.changes.size() < this.data.size()) {
                    this.resources.broadcast(this.model, this.data, this.changes, true);
                } else {
                    this.resources.broadcast(this.model, this.data, true);
                }
            }
            this.changes = new ChangeLog(this.model);
        }
    }

    public HashMap buildDataModel() {
        synchronized (this) {
            return this.data;
        }
    }

    @Override
    public void call(Request r, ManageUser client) {
        if (r.is(this.model + ".add", 2)) {
            String key = (String) r.arg(0);
            Map entry = (Map) r.arg(1);
            synchronized (this) {
                this.changes.add(key, entry);
            }
        } else if (r.is(this.model + ".addnew", 2)) {
            String key = (String) r.arg(0);
            Map entry = (Map) r.arg(1);
            synchronized (this) {
                this.changes.addnew(key, entry);
            }
        } else if (r.is(this.model + ".update", 2)) {
            String key = (String) r.arg(0);
            Map entry = (Map) r.arg(1);
            synchronized (this) {
                this.changes.update(key, entry);
            }
        } else if (r.is(this.model + ".remove", 1)) {
            String key = (String) r.arg(0);
            synchronized (this) {
                this.changes.delete(key);
            }
        } else if (r.is(this.model + ".push", 0)) {
            this.push();
        } else if (r.is(this.model + ".reset", 0)) {
            synchronized (this) {
                this.changes = new ChangeLog(this.model);
                this.data = new HashMap<>();
                this.save();
                this.resources.broadcast(this.model, this.data, true);
            }
        } else {
            client.writeNow(new Reply("server_error", 0L, r + ": incorrect number of arguments"));
        }
    }
}

