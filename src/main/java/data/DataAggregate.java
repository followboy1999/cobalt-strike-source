package data;

import aggressor.AggressorClient;
import common.Keys;

import java.util.*;

public class DataAggregate {
    protected AggressorClient client;
    protected LinkedList<Aggregator> aggregators;

    protected DataAggregate(AggressorClient client) {
        this.client = client;
        this.aggregators = new LinkedList<>();
    }

    public void register(Aggregator a) {
        this.aggregators.add(a);
    }

    public Map aggregate() {
        Map clients = this.client.getWindow().getClients();
        for (Object o : clients.values()) {
            AggressorClient client = (AggressorClient) o;
            for (Object aggregator : this.aggregators) {
                Aggregator ag = (Aggregator) aggregator;
                ag.extract(client);
            }
        }
        HashMap<String,LinkedList> results = new HashMap<>();
        for (Object aggregator : this.aggregators) {
            Aggregator ag = (Aggregator) aggregator;
            ag.publish(results);
        }
        return results;
    }

    public static Map AllModels(AggressorClient client) {
        DataAggregate aggr = new DataAggregate(client);
        Iterator i = Keys.getDataModelIterator();
        while (i.hasNext()) {
            aggr.register(new ModelAggregator((String) i.next()));
        }
        aggr.register(new ArchiveAggregator());
        aggr.register(new ProfileAggregator());
        return aggr.aggregate();
    }
}

