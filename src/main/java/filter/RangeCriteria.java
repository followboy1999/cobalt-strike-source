package filter;

import common.CommonUtils;
import common.RangeList;

public class RangeCriteria implements Criteria {
    protected RangeList range;

    public RangeCriteria(String description) {
        this.range = new RangeList(description);
    }

    @Override
    public boolean test(Object value) {
        if (value == null) {
            return false;
        }
        return this.range.hit(CommonUtils.toNumber(value.toString(), 0));
    }
}

