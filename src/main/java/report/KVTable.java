package report;

import java.util.Map;

public class KVTable implements ReportElement {
    protected Map entries;

    public KVTable(Map entries) {
        this.entries = entries;
    }

    @Override
    public void publish(StringBuilder out) {
        out.append("<fo:table width=\"100%\" border-separation=\"0\" margin-top=\"8pt\" margin-bottom=\"8pt\">\n");
        out.append(ReportUtils.ColumnWidth("2in")).append("\n");
        out.append(ReportUtils.ColumnWidth("4.5in")).append("\n");
        out.append("<fo:table-body>\n");
        for (Object o : this.entries.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            out.append("\t\t<fo:table-row>\n");
            out.append("\t\t\t<fo:table-cell>\n");
            out.append("<fo:block font-weight=\"bold\" font-family=\"sans-serif\">").append(Content.fixText((String) entry.getKey())).append(":</fo:block>\n");
            out.append("\t\t\t</fo:table-cell>\n");
            out.append("\t\t\t<fo:table-cell>\n");
            out.append("<fo:block font-family=\"sans-serif\">").append(Content.fixText((String) entry.getValue())).append("</fo:block>\n");
            out.append("\t\t\t</fo:table-cell>\n");
            out.append("\t\t</fo:table-row>\n");
        }
        out.append("\t</fo:table-body>\n");
        out.append("</fo:table>");
    }
}

