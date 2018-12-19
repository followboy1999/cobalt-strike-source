package filter;

public class NTLMHashCriteria implements Criteria {
    @Override
    public boolean test(Object check) {
        if (check == null) {
            return false;
        }
        return check.toString().length() == 32;
    }
}

