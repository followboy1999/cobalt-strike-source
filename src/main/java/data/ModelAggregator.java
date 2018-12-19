package data;

import aggressor.AggressorClient;
import common.ChangeLog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ModelAggregator implements Aggregator {
    protected ChangeLog changes;
    protected String name;
    protected HashMap model = new HashMap();

    public ModelAggregator(String name) {
        this.name = name;
        this.changes = new ChangeLog(name);
    }

    @Override
    public void extract(AggressorClient client) {
        Map data = client.getData().getDataModel(this.name);
        this.merge(data);
    }

    public void merge(Map data) {
        for (Object o : data.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = (String) entry.getKey();
            Map value = (Map) entry.getValue();
            this.changes.update(key, value);
        }
        this.changes.applyForce(this.model);
        this.changes = new ChangeLog(this.name);
    }

    @Override
    public void publish(Map<String, LinkedList> here) {
        here.put(this.name, new LinkedList<>(this.model.values()));
    }
}

