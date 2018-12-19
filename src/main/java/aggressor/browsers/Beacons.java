package aggressor.browsers;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.AdjustData;
import common.BeaconEntry;
import common.CommonUtils;
import dialog.DialogUtils;
import filter.DataFilter;

import java.util.LinkedList;
import java.util.Map;

public class Beacons extends Sessions implements AdjustData {
    protected DataFilter filter = new DataFilter();

    public Beacons(AggressorClient client, boolean multipleSelect) {
        super(client, multipleSelect);
        this.filter.checkBeacon("id", false);
    }

    @Override
    public Map<String, Object> format(String key, Object value) {
        if (!this.filter.test((Map) value)) {
            return null;
        }
        return (Map<String, Object>) value;
    }

    @Override
    public void result(String key, Object o) {
        if (!this.table.isShowing()) {
            return;
        }
        LinkedList<Map<String, Object>> resultz = new LinkedList<>(DataUtils.getBeaconModelFromResult((Map<String, BeaconEntry>) o));
        resultz = CommonUtils.apply(key, resultz, this);
        DialogUtils.setTable(this.table, this.model, resultz);
    }
}

