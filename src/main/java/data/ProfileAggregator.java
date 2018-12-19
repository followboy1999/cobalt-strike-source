package data;

import aggressor.AggressorClient;
import aggressor.DataUtils;

import java.util.LinkedList;
import java.util.Map;

public class ProfileAggregator implements Aggregator {
    protected LinkedList<Map<String, Object>> samples = new LinkedList<>();

    @Override
    public void extract(AggressorClient client) {
        Map<String, Object> entry = DataUtils.getC2Info(client.getData());
        this.samples.add(entry);
    }

    @Override
    public void publish(Map<String, LinkedList> here) {
        here.put("c2samples", this.samples);
    }
}

