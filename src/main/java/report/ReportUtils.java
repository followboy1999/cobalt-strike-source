package report;

import aggressor.Prefs;
import common.CommonUtils;
import common.MudgeSanity;
import encoders.Base64;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class ReportUtils {
    public static String accent() {
        return Prefs.getPreferences().getString("reporting.accent.color", "#003562");
    }

    public static String a(String text, String url) {
        StringBuilder result = new StringBuilder();
        result.append("<fo:basic-link color=\"").append(ReportUtils.accent()).append("\" text-decoration=\"underline\" external-destination=\"").append(url).append("\">");
        result.append("<fo:inline>");
        result.append(text);
        result.append("</fo:inline>");
        result.append("</fo:basic-link>");
        return result.toString();
    }

    public static String b(String text) {
        return "<fo:inline font-weight=\"bold\">" + text + "</fo:inline>";
    }

    public static String br() {
        return "<fo:block> </fo:block>";
    }

    public static String u(String text) {
        return "<fo:inline text-decoration=\"underline\">" + text + "</fo:inline>";
    }

    public static String i(String text) {
        return "<fo:inline font-style=\"italic\">" + text + "</fo:inline>";
    }

    public static String code(String text) {
        return "<fo:inline font-weight=\"monospace\">" + text + "</fo:inline>";
    }

    public static String logo() {
        String file = Prefs.getPreferences().getString("reporting.header_image.file", "");
        if ("".equals(file)) {
            byte[] resources = CommonUtils.readResource("resources/fso/logo2.png");
            return "url(&#34;data:image/png;base64," + Base64.encode(resources) + "&#xA;&#34;)";
        }
        return file;
    }

    public static String image(RenderedImage a) {
        try {
            ByteArrayOutputStream data = new ByteArrayOutputStream(524288);
            ImageIO.write(a, "png", data);
            return "url(&#34;data:image/png;base64," + Base64.encode(data.toByteArray()) + "&#xA;&#34;)";
        } catch (Exception ex) {
            MudgeSanity.logException("could not transform image", ex, false);
            return "";
        }
    }

    public static String image(String pointer) {
        byte[] resources = CommonUtils.readResource(pointer);
        return "url(&#34;data:image/png;base64," + Base64.encode(resources) + "&#xA;&#34;)";
    }

    public static String ColumnWidth(String width) {
        return "\t<fo:table-column column-width=\"" + width + "\" />";
    }

    public static void PublishAll(StringBuilder out, List elements) {
        for (Object element : elements) {
            ReportElement next = (ReportElement) element;
            next.publish(out);
        }
    }
}

