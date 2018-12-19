package importers;

import common.CommonUtils;
import common.MudgeSanity;
import common.OperatingSystem;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;

public class NmapXML
        extends Importer {
    public NmapXML(ImportHandler handler) {
        super(handler);
    }

    public boolean isNmapXML(File f) {
        String check = CommonUtils.peekFile(f, 1024);
        return check.startsWith("<?xml") && check.indexOf("<nmaprun") > 0;
    }

    @Override
    public boolean parse(File file) {
        if (!this.isNmapXML(file)) {
            return false;
        }
        try {
            SAXParserFactory parserFactor = SAXParserFactory.newInstance();
            SAXParser parser = parserFactor.newSAXParser();
            parser.parse(new FileInputStream(file), new NmapHandler());
            return true;
        } catch (Exception ex) {
            MudgeSanity.logException("Nmap XML is partially corrupt: " + file, ex, false);
            return true;
        }
    }

    class NmapHandler
            extends DefaultHandler {
        protected String host;
        protected boolean up = false;
        protected String port;
        protected String product = null;
        protected String version = null;
        protected boolean hasport = false;
        protected OperatingSystem os = null;
        protected int osscore = 0;

        NmapHandler() {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if ("host".equals(qName)) {
                this.os = null;
                this.osscore = 0;
                this.port = null;
                this.up = false;
                this.host = null;
                this.product = null;
                this.version = null;
                this.hasport = false;
            } else if ("status".equals(qName)) {
                this.up = "up".equals(attributes.getValue("state"));
            } else if ("address".equals(qName) && "ipv4".equals(attributes.getValue("addrtype"))) {
                this.host = attributes.getValue("addr");
            } else if ("address".equals(qName) && "ipv6".equals(attributes.getValue("addrtype"))) {
                this.host = attributes.getValue("addr");
            } else if ("port".equals(qName)) {
                this.port = attributes.getValue("portid");
                this.product = null;
                this.version = null;
            } else if ("service".equals(qName)) {
                this.product = attributes.getValue("product");
                this.version = attributes.getValue("version");
            } else if ("state".equals(qName)) {
                this.hasport = true;
            } else if ("os".equals(qName)) {
                this.os = null;
                this.osscore = 0;
            } else if ("osclass".equals(qName)) {
                String family = attributes.getValue("osfamily");
                String osgen = attributes.getValue("osgen");
                int accuracy = CommonUtils.toNumber(attributes.getValue("accuracy"), 0);
                OperatingSystem temp = new OperatingSystem(family + " " + osgen);
                if (accuracy > this.osscore && !temp.isUnknown()) {
                    this.os = temp;
                    this.osscore = accuracy;
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (this.hasport && "host".equals(qName)) {
                if (this.os != null) {
                    NmapXML.this.host(this.host, null, this.os.getName(), this.os.getVersion());
                } else {
                    NmapXML.this.host(this.host, null, null, 0.0);
                }
            } else if (this.up && "service".equals(qName)) {
                if (this.product != null && this.version != null) {
                    NmapXML.this.service(this.host, this.port, this.product + " " + this.version);
                } else if (this.product != null) {
                    NmapXML.this.service(this.host, this.port, this.product);
                } else {
                    NmapXML.this.service(this.host, this.port, null);
                }
            }
        }
    }

}

