package filter;

import common.AddressList;

public class NetworkCriteria implements Criteria {
    protected AddressList hosts;

    public NetworkCriteria(String description) {
        this.hosts = new AddressList(description);
    }

    @Override
    public boolean test(Object value) {
        if (value == null) {
            return false;
        }
        return this.hosts.hit(value.toString());
    }
}

