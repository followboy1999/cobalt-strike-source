package report;

public class NoBreak
        extends Content {
    public NoBreak(Document parent) {
        super(parent);
    }

    @Override
    public void publish(StringBuilder out) {
        out.append("<fo:block page-break-inside=\"avoid\" padding=\"0\" margin=\"0\">");
        super.publish(out);
        out.append("</fo:block>");
    }
}

