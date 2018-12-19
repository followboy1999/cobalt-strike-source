package filter;

import common.CommonUtils;

public class WildcardCriteria implements Criteria {
    protected String wildcard;

    public WildcardCriteria(String wildcard) {
        this.wildcard = wildcard.toLowerCase();
    }

    @Override
    public boolean test(Object value) {
        if (value == null) {
            return false;
        }
        return CommonUtils.iswm(this.wildcard, value.toString().toLowerCase());
    }
}

