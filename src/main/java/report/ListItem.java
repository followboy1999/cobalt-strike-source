package report;

public class ListItem
        extends Content {
    public ListItem(Document parent) {
        super(parent);
    }

    @Override
    public void publish(StringBuilder out) {
        out.append("<fo:list-item>\n");
        out.append("\t<fo:list-item-label end-indent=\"label-end()\">\n");
        out.append("\t\t<fo:block font-family=\"sans-serif\">&#x2022;</fo:block>\n");
        out.append("\t</fo:list-item-label>\n");
        out.append("\t<fo:list-item-body start-indent=\"body-start()\">\n");
        out.append("\t\t<fo:block margin-left=\"0.05in\" font-family=\"sans-serif\">");
        super.publish(out);
        out.append("\t\t</fo:block>");
        out.append("\t</fo:list-item-body>");
        out.append("</fo:list-item>");
    }
}

