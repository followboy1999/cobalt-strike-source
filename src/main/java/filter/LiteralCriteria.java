package filter;

public class LiteralCriteria implements Criteria {
    protected String value;

    public LiteralCriteria(String value) {
        this.value = value;
    }

    @Override
    public boolean test(Object check) {
        if (check == null) {
            return this.value.length() == 0;
        }
        return this.value.equals(check.toString());
    }
}

