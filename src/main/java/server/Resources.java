package server;

import common.*;
import logger.Archiver;
import logger.Logger;

import java.util.*;

public class Resources {
    protected HashMap<String, ManageUser> clients = new HashMap<>();
    protected HashMap<String, LinkedList<Transcript>> transcripts = new HashMap<>();
    protected ServerBus bus;
    protected final HashMap<String, Object> shared = new HashMap<>();
    protected HashMap<String, Object> replayme = new HashMap<>();
    protected Logger logger = new Logger(this);
    protected Archiver archiver;

    public void reset() {
        synchronized (this) {
            Set whitelist = CommonUtils.toSet("listeners, sites, users, metadata, localip, cmdlets");
            Iterator i = Keys.getDataModelIterator();
            while (i.hasNext()) {
                String model = (String) i.next();
                if (whitelist.contains(model)) continue;
                this.call(model + ".reset");
            }
            this.call("beacons.reset");
            if (this.archiver != null) {
                this.archiver.reset();
            }
            this.transcripts = new HashMap<>();
            this.broadcast("data_reset", new TranscriptReset(), false);
        }
    }

    public void archive(Informant i) {
        this.archiver.act(i);
    }

    public boolean isLimit(Collection stuff, String key) {
        return stuff.size() >= CommonUtils.limit(key);
    }

    public Resources(Map calls) {
        this.bus = new ServerBus(calls);
        this.archiver = new Archiver(this);
    }

    public Object get(String key) {
        synchronized (this.shared) {
            if (!this.shared.containsKey(key)) {
                CommonUtils.print_error("Shared resource: '" + key + "' does not exist [this is probably bad]");
                Thread.dumpStack();
            }
            return this.shared.get(key);
        }
    }

    public void put(String key, Object value) {
        synchronized (this.shared) {
            this.shared.put(key, value);
        }
    }

    public void backlog(String key, Transcript o) {
        synchronized (this) {
            LinkedList<Transcript> events = this.transcripts.computeIfAbsent(key, k -> new LinkedList<>());
            while (this.isLimit(events, key)) {
                events.removeFirst();
            }
            events.add(o);
        }
    }

    public void playback(String name) {
        synchronized (this) {
            PlaybackStatus status = new PlaybackStatus("syncing with server", this.transcripts.size() + this.replayme.size());
            this.send(name, "playback.status", status.copy());
            for (Object o1 : this.transcripts.entrySet()) {
                Map.Entry entry = (Map.Entry) o1;
                String key = entry.getKey() + "";
                LinkedList events = (LinkedList) entry.getValue();
                status.message("syncing " + key);
                this.send(name, "playback.status", status.copy());
                status.more(events.size());
                for (Object event : events) {
                    this.send(name, key, event);
                    status.sent();
                    this.send(name, "playback.status", status.copy());
                }
                status.sent();
            }
            this.send(name, "playback.status", status.copy());
            for (Object o : this.replayme.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                String key = entry.getKey() + "";
                Object value = entry.getValue();
                status.message("syncing " + key);
                this.send(name, "playback.status", status.copy());
                this.send(name, key, value);
                status.sent();
            }
            this.send(name, "playback.status", status.copy());
        }
    }

    public LinkedList<ManageUser> getClients() {
        LinkedList<ManageUser> results;
        synchronized (this) {
            results = new LinkedList<>(this.clients.values());
        }
        return results;
    }

    public HashSet<String> getUsers() {
        synchronized (this) {
            return new HashSet<>(this.clients.keySet());
        }
    }

    public void send(String user, String key, Object message) {
        synchronized (this) {
            ManageUser client = this.clients.get(user);
            this.send(client, key, message);
        }
    }

    public void send(ManageUser client, String key, Object message) {
        Reply sendme = new Reply(key, 0L, message);
        client.write(sendme);
    }

    public void sendAndProcess(ManageUser client, String key, Object message) {
        this.process(message);
        this.send(client, key, message);
    }

    public void process(Object message) {
        if (message instanceof Loggable) {
            this.logger.act(message);
        }
        if (message instanceof Informant) {
            this.archiver.act(message);
        }
    }

    public void broadcast(String key, Object message) {
        this.broadcast(key, message, false);
    }

    public void broadcast(String key, Object message, boolean remember) {
        this.broadcast(key, message, null, remember);
    }

    public void broadcast(String key, Object message, ChangeLog summary, boolean remember) {
        synchronized (this) {
            if (message instanceof Transcript) {
                this.backlog(key, (Transcript) message);
            } else if (remember) {
                this.replayme.put(key, message);
            }
            this.process(message);
            Reply sendme = new Reply(key, 0L, summary != null ? summary : message);
            for (ManageUser user : this.getClients()) {
                user.write(sendme);
            }
        }
    }

    public boolean isRegistered(String nickname) {
        synchronized (this) {
            return this.clients.containsKey(nickname);
        }
    }

    public void register(String nickname, ManageUser client) {
        synchronized (this) {
            this.clients.put(nickname, client);
            this.playback(nickname);
        }
        this.broadcast("users", this.getUsers());
    }

    public void deregister(String nickname, ManageUser client) {
        synchronized (this) {
            this.clients.remove(nickname);
        }
        this.broadcast("users", this.getUsers());
    }

    public void call(String name, Object[] args) {
        this.bus.addRequest(null, new Request(name, args, 0L));
    }

    public void call(String name) {
        this.bus.addRequest(null, new Request(name, new Object[0], 0L));
    }

    public void call(ManageUser client, Request request) {
        this.bus.addRequest(client, request);
    }
}

