package data;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.CommonUtils;
import common.Transcript;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ArchiveAggregator implements Aggregator {
    protected LinkedList<HashMap<String, Long>> archives = new LinkedList<>();

    @Override
    public void extract(AggressorClient client) {
        LinkedList<Transcript> entries = client.getData().getTranscriptSafe("archives");
        for (Transcript entry : entries) {
            Map temp = (Map) entry;
            HashMap<String, Long> val = new HashMap<String, Long>(temp);
            if (val.containsKey("when")) {
                long valz = CommonUtils.toLongNumber(val.get("when") + "", 0L);
                val.put("when", DataUtils.AdjustForSkew(client.getData(), valz));
            }
            this.archives.add(val);
        }
    }

    @Override
    public void publish(Map<String, LinkedList> here) {
        here.put("archives", this.archives);
    }
}

