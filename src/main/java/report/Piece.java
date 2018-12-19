package report;

public class Piece implements ReportElement {
    protected String text;

    public Piece(String text) {
        this.text = text;
    }

    @Override
    public void publish(StringBuilder out) {
        out.append(this.text);
    }
}

