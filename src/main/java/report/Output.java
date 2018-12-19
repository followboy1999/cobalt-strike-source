package report;

public class Output
        extends Content {
    protected String width;

    public Output(Document parent, String width) {
        super(parent);
        this.width = width;
    }

    @Override
    public void publish(StringBuilder out) {
        out.append("<fo:block background-color=\"#eeeeee\" content-width=\"").append(this.width).append("\" linefeed-treatment=\"preserve\" padding=\"8pt\">");
        super.publish(out);
        out.append("</fo:block>");
    }
}

