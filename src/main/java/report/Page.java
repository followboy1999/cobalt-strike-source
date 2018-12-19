package report;

import common.CommonUtils;

public class Page
        extends Content {
    public static final int PAGE_FIRST = 0;
    public static final int PAGE_REST = 1;
    public static final int PAGE_FIRST_CENTER = 2;
    public static final int PAGE_SINGLE = 3;
    protected int type;
    protected String title;

    public Page(Document parent, int type, String title) {
        super(parent);
        this.type = type;
        this.title = title;
    }

    @Override
    public void publish(StringBuilder out) {
        if (this.isEmpty()) {
            return;
        }
        if (this.type == 0) {
            out.append("<fo:page-sequence master-reference=\"first\">\n");
            out.append("<fo:static-content flow-name=\"xsl-region-before\" color=\"black\">\n");
            out.append("\t<fo:block border-bottom=\"2pt solid ").append(ReportUtils.accent()).append("\">\n");
            out.append("\t\t<fo:external-graphic src=\"").append(ReportUtils.logo()).append("\" />\n");
            out.append("\t</fo:block>\n");
            out.append("</fo:static-content>");
        } else if (this.type == 2) {
            out.append("<fo:page-sequence master-reference=\"first\">\n");
            out.append("\t<fo:static-content flow-name=\"xsl-region-before\" color=\"black\">\n");
            out.append("\t\t<fo:block>\n");
            out.append("\t\t\t<fo:table border-bottom=\"2pt solid ").append(ReportUtils.accent()).append("\">");
            out.append("\t\t\t\t").append(ReportUtils.ColumnWidth("2.35in")).append("\n");
            out.append("\t\t\t\t").append(ReportUtils.ColumnWidth("4.0in")).append("\n");
            out.append("\t\t\t\t").append(ReportUtils.ColumnWidth("2.35in")).append("\n");
            out.append("\t\t\t\t<fo:table-body>\n");
            out.append("\t\t\t\t\t<fo:table-row>\n");
            out.append("\t\t\t\t\t\t<fo:table-cell><fo:block></fo:block></fo:table-cell>\n");
            out.append("\t\t\t\t\t\t<fo:table-cell>\n");
            out.append("\t\t\t\t\t\t\t<fo:block>\n");
            out.append("\t\t\t\t\t\t\t\t<fo:external-graphic src=\"").append(ReportUtils.logo()).append("\" />\n");
            out.append("\t\t\t\t\t\t\t</fo:block>\n");
            out.append("\t\t\t\t\t\t</fo:table-cell>\n");
            out.append("\t\t\t\t\t\t<fo:table-cell><fo:block></fo:block></fo:table-cell>\n");
            out.append("\t\t\t\t\t</fo:table-row>\n");
            out.append("\t\t\t\t</fo:table-body>\n");
            out.append("\t\t\t</fo:table>\n");
            out.append("\t\t</fo:block>\n");
            out.append("\t</fo:static-content>\n");
        } else if (this.type == 1) {
            out.append("<fo:page-sequence master-reference=\"rest\">\n");
            out.append("<fo:static-content flow-name=\"xsl-region-before\" color=\"black\">\n");
            out.append("\t<fo:block border-bottom=\"2pt solid black\" font-family=\"sans-serif\">\n");
            out.append("\t\t\t").append(Content.fixText(this.title)).append("\n");
            out.append("\t</fo:block>");
            out.append("</fo:static-content>");
            out.append(CommonUtils.readResourceAsString("resources/fso/page_footer.fso"));
        } else if (this.type == 3) {
            out.append("<fo:page-sequence master-reference=\"first\">\n");
            out.append("\t<fo:static-content flow-name=\"xsl-region-before\" color=\"black\">\n");
            out.append("\t\t<fo:block>\n");
            out.append("\t\t\t<fo:table>");
            out.append("\t\t\t\t").append(ReportUtils.ColumnWidth("1.1in")).append("\n");
            out.append("\t\t\t\t").append(ReportUtils.ColumnWidth("4.0in")).append("\n");
            out.append("\t\t\t\t").append(ReportUtils.ColumnWidth("1.1in")).append("\n");
            out.append("\t\t\t\t<fo:table-body>\n");
            out.append("\t\t\t\t\t<fo:table-row>\n");
            out.append("\t\t\t\t\t\t<fo:table-cell><fo:block></fo:block></fo:table-cell>\n");
            out.append("\t\t\t\t\t\t<fo:table-cell>\n");
            out.append("\t\t\t\t\t\t\t<fo:block>\n");
            out.append("\t\t\t\t\t\t\t\t<fo:external-graphic src=\"").append(ReportUtils.logo()).append("\" />\n");
            out.append("\t\t\t\t\t\t\t</fo:block>\n");
            out.append("\t\t\t\t\t\t</fo:table-cell>\n");
            out.append("\t\t\t\t\t\t<fo:table-cell><fo:block></fo:block></fo:table-cell>\n");
            out.append("\t\t\t\t\t</fo:table-row>\n");
            out.append("\t\t\t\t</fo:table-body>\n");
            out.append("\t\t\t</fo:table>\n");
            out.append("\t\t</fo:block>\n");
            out.append("</fo:static-content>");
        }
        out.append("\t\t<fo:flow flow-name=\"xsl-region-body\">");
        super.publish(out);
        out.append("\t</fo:flow>\n</fo:page-sequence>");
    }
}

