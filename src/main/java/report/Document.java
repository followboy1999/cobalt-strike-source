package report;

import com.xmlmind.fo.converter.Converter;
import com.xmlmind.fo.converter.OutputDestination;
import common.AObject;
import common.CommonUtils;
import common.MudgeSanity;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

public class Document
        extends AObject implements ReportElement {
    public static final int ORIENTATION_PORTRAIT = 0;
    public static final int ORIENTATION_LANDSCAPE = 1;
    protected int orientation;
    protected List pages = new LinkedList();
    protected String title;
    protected Bookmarks bookmarks = new Bookmarks();
    protected FopFactory fopFactory = FopFactory.newInstance();

    public String register(String heading) {
        return this.bookmarks.register(heading);
    }

    public Bookmarks getBookmarks() {
        return this.bookmarks;
    }

    public void setOrientation(int type) {
        this.orientation = type;
    }

    public Document(String title, int orientation) {
        this.orientation = orientation;
        this.title = title;
    }

    public Page addPage(int type) {
        Page page = new Page(this, type, this.title);
        this.pages.add(page);
        return page;
    }

    public void toWord(File file) {
        try {
            Converter converter = new Converter();
            converter.setProperty("outputFormat", "docx");
            converter.setProperty("outputEncoding", "UTF-8");
            InputSource src = new InputSource(this.toStream());
            OutputDestination dst = new OutputDestination(file.getPath());
            converter.convert(src, dst);
        } catch (Exception ex) {
            MudgeSanity.logException("document -> toWord failed [see out.fso]: " + file, ex, false);
            this.toFSO(new File("out.fso"));
        }
    }

    public void toFSO(File file) {
        try {
            StringBuilder output = new StringBuilder(1048576);
            this.publish(output);
            FileOutputStream fos = new FileOutputStream("out.fso");
            fos.write(CommonUtils.toBytes(output.toString()));
            fos.close();
        } catch (Exception ex) {
            MudgeSanity.logException("document -> toFSO failed: " + file, ex, false);
        }
    }

    public void toPDF(File file) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            Fop fop = this.fopFactory.newFop("application/pdf", out);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            StreamSource src = new StreamSource(this.toStream());
            SAXResult res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
            ((OutputStream) out).close();
        } catch (Exception ex) {
            MudgeSanity.logException("document -> toPDF failed [see out.fso]: " + file, ex, false);
            this.toFSO(new File("out.fso"));
        }
    }

    protected InputStream toStream() {
        try {
            StringBuilder output = new StringBuilder(1048576);
            this.publish(output);
            return new ByteArrayInputStream(output.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            MudgeSanity.logException("output -> toStream failed", ex, false);
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public void publish(StringBuilder out) {
        if (this.orientation == 0) {
            out.append(CommonUtils.readResourceAsString("resources/fso/document_start_portrait.fso"));
        } else if (this.orientation == 1) {
            out.append(CommonUtils.readResourceAsString("resources/fso/document_start_landscape.fso"));
        }
        this.getBookmarks().publish(out);
        ReportUtils.PublishAll(out, this.pages);
        out.append("</fo:root>");
    }
}

