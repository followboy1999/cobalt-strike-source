package report;

import common.CommonUtils;
import common.RegexParser;

import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Content implements ReportElement {
    protected LinkedList<ReportElement> elements = new LinkedList<>();
    protected Document parent;

    public static boolean isAllowed(char x) {
        return x == '\t' || x == '\n' || x == '\r' || x >= ' ' && x <= '\ud7ff' || x >= '\ue000' && x <= '\ufffd';
    }

    public static String fixText(String text) {
        StringBuilder result = new StringBuilder(text.length() * 2);
        char[] chars = text.toCharArray();
        for (char aChar : chars) {
            if (aChar == '&') {
                result.append("&amp;");
                continue;
            }
            if (aChar == '<') {
                result.append("&lt;");
                continue;
            }
            if (aChar == '>') {
                result.append("&gt;");
                continue;
            }
            if (aChar == '\"') {
                result.append("&quot;");
                continue;
            }
            if (aChar == '\'') {
                result.append("&apos;");
                continue;
            }
            if (!Content.isAllowed(aChar)) continue;
            if (aChar > '\u00ff') {
                String temp = CommonUtils.toHex(aChar);
                String pad = CommonUtils.padr(temp, "0", 4);
                result.append("&#x");
                result.append(pad);
                result.append(";");
                continue;
            }
            result.append(aChar);
        }
        return result.toString();
    }

    public Content(Document parent) {
        this.parent = parent;
    }

    public void h1(String text) {
        this.h1(text, text, "left");
    }

    public void h1(String text, String id, String align) {
        text = Content.fixText(text);
        String result = "<fo:block font-size=\"18pt\"\n" +
                "\t\tfont-family=\"sans-serif\"\n" +
                "\t\tid=\"" + this.parent.register(id) + "\"\n" +
                "\t\tfont-weight=\"bold\"\n" +
                "\t\tline-height=\"24pt\"\n" +
                "\t\tspace-after.optimum=\"15pt\"\n" +
                "\t\tcolor=\"black\"\n" +
                "\t\ttext-align=\"" + align + "\"\n" +
                "\t\tpadding-top=\"12pt\">\n" +
                "\t" + text + "\n" +
                "</fo:block>";
        this.elements.add(new Piece(result));
    }

    public void h2(String text) {
        this.h2(text, text);
    }

    public void h2(String text, String id) {
        text = Content.fixText(text);
        String result = "<fo:block font-size=\"15pt\"\n" +
                "\t\tfont-family=\"sans-serif\"\n" +
                "\t\tfont-weight=\"bold\"\n" +
                "\t\tid=\"" + this.parent.register(id) + "\"\n" +
                "\t\tline-height=\"24pt\"\n" +
                "\t\tspace-after.optimum=\"15pt\"\n" +
                "\t\tcolor=\"black\"\n" +
                "\t\ttext-align=\"left\"\n" +
                "\t\tpadding-top=\"6pt\"\n" +
                "\t\tpadding-bottom=\"6pt\"\n" +
                "\t\tmargin-bottom=\"0\">\n" +
                "\t<fo:inline text-decoration=\"underline\">" + text + "</fo:inline>\n" +
                "</fo:block>";
        this.elements.add(new Piece(result));
    }

    public void img(String pointer, String width) {
        this.elements.add(new Piece("<fo:external-graphic src=\"" + pointer + "\" content-width=\"" + width + "\" />"));
    }

    public void h2_img(BufferedImage image, String text) {
        this.h2_img(image, text, text);
    }

    public void h2_img(BufferedImage image, String text, String id) {
        StringBuilder result = new StringBuilder();
        result.append("<fo:table border-separation=\"0\" margin-top=\"4pt\" margin-bottom=\"8pt\" width=\"100%\">\n");
        result.append("\t<fo:table-body>\n");
        result.append("\t\t<fo:table-row>\n");
        result.append("\t\t\t<fo:table-cell display-align=\"after\" width=\"0.6in\">\n");
        result.append("\t\t\t\t<fo:block padding=\"0\" margin=\"0\">\n");
        this.elements.add(new Piece(result.toString()));
        this.img(ReportUtils.image(image), "0.5in");
        result = new StringBuilder();
        result.append("\t\t\t\t</fo:block>\n");
        result.append("\t\t\t</fo:table-cell>\n");
        result.append("\t\t\t<fo:table-cell display-align=\"center\" width=\"6in\">\n");
        this.elements.add(new Piece(result.toString()));
        this.h2(text, id);
        result = new StringBuilder();
        result.append("\t\t\t</fo:table-cell>\n");
        result.append("\t\t</fo:table-row>\n");
        result.append("\t</fo:table-body>\n");
        result.append("</fo:table>\n");
        this.elements.add(new Piece(result.toString()));
    }

    public void h3(String text) {
        text = Content.fixText(text);
        String result = "<fo:block font-size=\"14pt\"\n" +
                "\t\tfont-family=\"sans-serif\"\n" +
                "\t\tfont-weight=\"bold\"\n" +
                "\t\tline-height=\"24pt\"\n" +
                "\t\tspace-after.optimum=\"15pt\"\n" +
                "\t\tcolor=\"black\"\n" +
                "\t\ttext-align=\"left\"\n" +
                "\t\tpadding-top=\"6pt\"\n" +
                "\t\tpadding-bottom=\"6pt\"\n" +
                "\t\tmargin-bottom=\"0\">\n" +
                "\t" + text + "\n" +
                "</fo:block>";
        this.elements.add(new Piece(result));
    }

    public void kvtable(Map entries) {
        this.elements.add(new KVTable(entries));
    }

    public Content block(String align) {
        FoBlock result = new FoBlock(this.parent, align);
        this.elements.add(result);
        return result;
    }

    public Content string() {
        return new Content(this.parent);
    }

    public Content output(String width) {
        Output result = new Output(this.parent, width);
        this.elements.add(result);
        return result;
    }

    public Content nobreak() {
        NoBreak result = new NoBreak(this.parent);
        this.elements.add(result);
        return result;
    }

    public void list(List items) {
        Content result = this.ul();
        for (Object next : items) {
            if (next == null) continue;
            result.li().text(next.toString());
        }
    }

    public void list_formatted(List items) {
        Content result = this.ul();
        for (Object item1 : items) {
            String next = (item1 + "").trim();
            RegexParser parser = new RegexParser(next);
            if (parser.matches("'''(.*?)'''(.*?)")) {
                Content item = result.li();
                item.b(parser.group(1));
                item.text(parser.group(2));
                continue;
            }
            if ("".equals(next)) continue;
            result.li().text(next);
        }
    }

    public void link(String text, String url) {
        text = Content.fixText(text);
        this.elements.add(new Piece(ReportUtils.a(text, url)));
    }

    public void link_bullet(String name, String link) {
        Content result = this.ul();
        result.li().link(name, link);
    }

    public Content li() {
        ListItem result = new ListItem(this.parent);
        this.elements.add(result);
        return result;
    }

    public Content ul() {
        UnorderedList result = new UnorderedList(this.parent);
        this.elements.add(result);
        return result;
    }

    public void b(String text) {
        text = Content.fixText(text);
        this.elements.add(new Piece("<fo:inline font-weight=\"bold\">" + text + "</fo:inline>"));
    }

    public void text(String textz) {
        textz = Content.fixText(textz);
        this.elements.add(new Piece("<fo:inline>" + textz + "</fo:inline>"));
    }

    public void color(String text, String color) {
        text = Content.fixText(text);
        this.elements.add(new Piece("<fo:inline color=\"" + color + "\">" + text + "</fo:inline>"));
    }

    public void color2(String text, String fgcolor, String bgcolor) {
        text = Content.fixText(text);
        this.elements.add(new Piece("<fo:inline color=\"" + fgcolor + "\" background-color=\"" + bgcolor + "\">" + text + "</fo:inline>"));
    }

    public void h4(String text, String align) {
        text = Content.fixText(text);
        this.elements.add(new Piece("<fo:block font-size=\"12pt\" font-family=\"sans-serif\" font-weight=\"bold\" color=\"black\" text-align=\"" + align + "\">" + text + "</fo:block>"));
    }

    public void p(String text, String align) {
        text = Content.fixText(text);
        this.elements.add(new Piece("<fo:block font-size=\"12pt\" font-family=\"sans-serif\" font-weight=\"normal\" color=\"black\" text-align=\"" + align + "\">" + text + "</fo:block>"));
    }

    public void br() {
        this.elements.add(new Piece("<fo:block font-size=\"12pt\" font-family=\"sans-serif\" font-weight=\"normal\" color=\"black\" text-align=\"left\">&#160;</fo:block>"));
    }

    public void table(List cols, List widths, List rows) {
        this.elements.add(new Table(cols, widths, rows));
    }

    public void layout(List cols, List widths, List rows) {
        this.elements.add(new Layout(cols, widths, rows));
    }

    public void ts() {
        String ts = DateFormat.getDateInstance(2).format(new Date());
        this.elements.add(new Piece("<fo:block font-size=\"12pt\" padding-bottom=\"8pt\" font-family=\"sans-serif\" font-style=\"italic\" font-weight=\"normal\" color=\"black\" text-align=\"left\">" + ts + "</fo:block>"));
    }

    @Override
    public void publish(StringBuilder out) {
        ReportUtils.PublishAll(out, this.elements);
    }

    public boolean isEmpty() {
        return this.elements.size() == 0;
    }

    public void bookmark(String heading) {
        this.parent.getBookmarks().bookmark(heading);
    }

    public void bookmark(String heading, String child) {
        this.parent.getBookmarks().bookmark(heading, child);
    }
}

