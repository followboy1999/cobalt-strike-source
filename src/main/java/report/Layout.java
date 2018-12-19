package report;

import dialog.DialogUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Layout implements ReportElement {
    protected List columns;
    protected List widths;
    protected List rows;

    public Layout(List columns, List widths, List rows) {
        this.columns = columns;
        this.widths = widths;
        this.rows = rows;
    }

    @Override
    public void publish(StringBuilder out) {
        if (this.rows.size() == 0) {
            out.append(ReportUtils.br());
            return;
        }
        out.append("<fo:table border-bottom=\"none\" border-left=\"none\" border-right=\"none\" width=\"100%\" border-separation=\"0\">\n");
        for (Object width1 : this.widths) {
            String width = (String) width1;
            out.append(ReportUtils.ColumnWidth(width)).append("\n");
        }
        out.append("\t<fo:table-body>\n");
        Iterator k = this.rows.iterator();
        int x = 0;
        while (k.hasNext()) {
            Map row = (Map) k.next();
            out.append("\t\t<fo:table-row>\n");
            Iterator l = this.columns.iterator();
            int y = 0;
            while (l.hasNext()) {
                String col = (String) l.next();
                out.append("\t\t\t<fo:table-cell>\n");
                String foo = DialogUtils.string(row, col);
                if (foo == null) {
                    foo = "";
                }
                out.append("\t\t\t\t<fo:block font-family=\"sans-serif\">").append(foo).append("</fo:block>\n");
                out.append("\t\t\t</fo:table-cell>\n");
                ++y;
            }
            out.append("\t\t</fo:table-row>\n");
            ++x;
        }
        out.append("\t</fo:table-body>\n");
        out.append("</fo:table>");
    }
}

