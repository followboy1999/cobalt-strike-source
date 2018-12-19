package filter;

public class NegateCriteria implements Criteria {
    protected Criteria parent;

    public NegateCriteria(Criteria parent) {
        this.parent = parent;
    }

    @Override
    public boolean test(Object check) {
        return !this.parent.test(check);
    }
}

