package logger;

import common.ArchiveMap;
import common.CommonUtils;
import common.Informant;
import server.PersistentData;
import server.Resources;

import java.util.LinkedList;

public class Archiver
        extends ProcessBackend {
    protected Resources resources;
    protected PersistentData<LinkedList<ArchiveMap>> store;
    protected LinkedList<ArchiveMap> model = null;

    public Archiver(Resources r) {
        this.resources = r;
        this.load();
    }

    public void load() {
        this.store = new PersistentData<>("archives", this);
        this.model = this.store.getValue(new LinkedList<>());
        for (ArchiveMap temp : this.model) {
            this.resources.broadcast("archives", temp);
        }
        this.start("archiver");
    }

    public void reset() {
        synchronized (this) {
            this.tasks = new LinkedList<>();
            this.model = new LinkedList<>();
            this.store.save(this.model);
        }
    }

    @Override
    public void process(Object _next) {
        Informant data = (Informant) _next;
        if (data.hasInformation()) {
            ArchiveMap temp = new ArchiveMap(data.archive());
            this.resources.broadcast("archives", temp);
            synchronized (this) {
                this.model.add(temp);
                while (this.model.size() > CommonUtils.limit("archives")) {
                    this.model.removeFirst();
                }
                this.store.save(this.model);
            }
        }
    }
}

