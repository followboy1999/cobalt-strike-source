package filter;

import common.CommonUtils;

public class BeaconCriteria implements Criteria {
    @Override
    public boolean test(Object check) {
        if (check == null) {
            return false;
        }
        String bidz = check.toString();
        int bid = CommonUtils.toNumber(bidz, 0);
        return bid <= 500000;
    }
}

