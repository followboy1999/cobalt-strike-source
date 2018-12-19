package report;

public class FoBlock
        extends Content {
    protected String align;

    public FoBlock(Document parent, String align) {
        super(parent);
        this.align = align;
    }

    @Override
    public void publish(StringBuilder out) {
        out.append("<fo:block linefeed-treatment=\"preserve\" text-align=\"").append(this.align).append("\">");
        super.publish(out);
        out.append("</fo:block>");
    }
}

