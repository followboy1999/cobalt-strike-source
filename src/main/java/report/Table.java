package report;

import dialog.DialogUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Table implements ReportElement {
    protected List columns;
    protected List widths;
    protected List rows;

    public Table(List columns, List widths, List rows) {
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
        out.append("<fo:table border-bottom=\"1pt solid black\" margin-bottom=\"12pt\" border-left=\"none\" border-right=\"none\" width=\"100%\" border-separation=\"0\">\n");
        for (Object width1 : this.widths) {
            String width = (String) width1;
            out.append(ReportUtils.ColumnWidth(width)).append("\n");
        }
        out.append("\t<fo:table-header border-bottom=\"1pt solid black\" background-color=\"#cccccc\">\n");
        Iterator j = this.columns.iterator();
        int x = 0;
        while (j.hasNext()) {
            String col = (String) j.next();
            out.append("\t\t<fo:table-cell>\n");
            out.append("\t\t\t<fo:block font-weight=\"bold\" ");
            if (x == 0) {
                out.append("margin-left=\"2pt\"");
            }
            out.append(" font-family=\"sans-serif\" padding=\"0.02in\" padding-left=\"0in\">\n");
            out.append(Content.fixText(col));
            out.append("\t\t\t</fo:block>\n");
            out.append("\t\t</fo:table-cell>\n");
            ++x;
        }
        out.append("\t</fo:table-header>\n");
        out.append("\t<fo:table-body>\n");
        Iterator k = this.rows.iterator();
        int x2 = 0;
        while (k.hasNext()) {
            Map row = (Map) k.next();
            if (x2 % 2 == 1) {
                out.append("\t\t<fo:table-row background-color=\"#eeeeee\" border=\"none\" margin=\"0\" padding=\"0\">\n");
            } else {
                out.append("\t\t<fo:table-row>\n");
            }
            Iterator l = this.columns.iterator();
            int y = 0;
            while (l.hasNext()) {
                String col = (String) l.next();
                if (y == 0) {
                    out.append("\t\t\t<fo:table-cell margin-left=\"2pt\">\n");
                } else {
                    out.append("\t\t\t<fo:table-cell>\n");
                }
                String foo = DialogUtils.string(row, col);
                if (foo == null) {
                    foo = "";
                }
                out.append("\t\t\t\t<fo:block padding-top=\"2pt\" padding-bottom=\"1pt\" font-family=\"sans-serif\">").append(foo).append("</fo:block>\n");
                out.append("\t\t\t</fo:table-cell>\n");
                ++y;
            }
            out.append("\t\t</fo:table-row>\n");
            ++x2;
        }
        out.append("\t</fo:table-body>\n");
        out.append("</fo:table>");
    }
}

