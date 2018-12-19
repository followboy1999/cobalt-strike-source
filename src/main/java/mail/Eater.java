package mail;

import org.apache.james.mime4j.dom.*;
import org.apache.james.mime4j.field.address.AddressBuilder;
import org.apache.james.mime4j.message.*;
import org.apache.james.mime4j.storage.DefaultStorageProvider;
import org.apache.james.mime4j.storage.StorageBodyFactory;
import org.apache.james.mime4j.storage.TempFileStorageProvider;
import org.apache.james.mime4j.stream.Field;
import org.apache.james.mime4j.stream.MimeConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Eater {
    protected Message message;

    public Eater(String name) throws IOException {
        this(new FileInputStream(name));
    }

    public Eater(InputStream fis) {
        DefaultMessageBuilder builder = new DefaultMessageBuilder();
        MimeConfig config = new MimeConfig();
        config.setMaxLineLen(-1);
        config.setMaxHeaderLen(-1);
        config.setMaxHeaderCount(-1);
        builder.setMimeEntityConfig(config);
        try {
            this.message = builder.parseMessage(fis);
            this.stripHeaders(this.message);
            this.stripAttachments(this.message);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void done() {
        this.message.dispose();
    }

    public static void main(String[] args) throws Exception {
        Eater temp = new Eater(args[0]);
        try {
            if (args.length == 2) {
                temp.attachFile("build.xml");
            }
            System.out.println(new String(temp.getMessage("Raphael Mudge <rsmudge@gmail.com>", "Test User <test@aol.com>"), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void stripHeaders(Message message) {
        Header h = message.getHeader();
        h.removeFields("Authentication-Results");
        h.removeFields("Delivered-To");
        h.removeFields("DKIM-Signature");
        h.removeFields("DomainKey-Signature");
        h.removeFields("DomainKey-Status");
        h.removeFields("In-Reply-To");
        h.removeFields("Message-ID");
        h.removeFields("Received");
        h.removeFields("Received-SPF");
        h.removeFields("References");
        h.removeFields("Reply-To");
        h.removeFields("Return-Path");
        h.removeFields("Sender");
        h.removeFields("X-AUTH-Result");
        h.removeFields("X-Message-Delivery");
        h.removeFields("X-Message-Info");
        h.removeFields("X-Message-Status");
        h.removeFields("X-Original-Authentication-Results");
        h.removeFields("X-OriginalArrivalTime");
        h.removeFields("X-Original-Sender");
        h.removeFields("X-SID-PRA");
        h.removeFields("CC");
        h.removeFields("Return-Path");
        h.removeFields("Envelope-to");
        h.removeFields("Delivery-date");
        h.removeFields("X-Sender");
        h.removeFields("X-AntiAbuse");
        h.removeFields("X-Filter-ID");
        h.removeFields("X-Originating-IP");
        h.removeFields("X-SpamExperts-Domain");
        h.removeFields("X-SpamExperts-Username");
        h.removeFields("X-SpamExperts-Outgoing-Class");
        h.removeFields("X-SpamExperts-Outgoing-Evidence");
        h.removeFields("X-Recommended-Action");
        h.removeFields("X-DKIM");
        h.removeFields("X-DomainKeys");
        h.removeFields("X-Spam-Checker-Version");
        h.removeFields("X-Spam-Checker");
        h.removeFields("X-Spam-Level");
        h.removeFields("X-Spam-Status");
        h.removeFields("X-MS-Has-Attach");
        h.removeFields("X-MS-TNEF-Correlator");
        h.removeFields("x-ms-exchange-transport-fromentityheader");
        h.removeFields("x-microsoft-antispam");
        h.removeFields("x-forefront-prvs");
        h.removeFields("x-forefront-antispam-report");
        h.removeFields("X-WSS-ID");
        h.removeFields("X-M-MSG");
    }

    protected String extractContent(Entity ent) {
        try {
            SingleBody body = (SingleBody) ent.getBody();
            InputStream stream = body.getInputStream();
            InputStreamReader r = new InputStreamReader(stream, ent.getCharset());
            char[] buffer = new char[2097152];
            int len = r.read(buffer);
            return new String(buffer, 0, len);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public String getMessageEntity(String mimetype) {
        return this.getMessageEntity(this.message, mimetype);
    }

    protected String getMessageEntity(Entity ent, String mimetype) {
        String result;
        if (ent.getBody() instanceof SingleBody && mimetype.equals(ent.getMimeType())) {
            return this.extractContent(ent);
        }
        if (ent.getBody() instanceof Multipart) {
            Multipart body = (Multipart) ent.getBody();
            for (Entity e : body.getBodyParts()) {
                result = this.getMessageEntity(e, mimetype);
                if (result == null) continue;
                return result;
            }
        }
        return null;
    }

    protected void fixMessageType(Entity ent) {
        if (!(ent.getBody() instanceof SingleBody)) {
            return;
        }
        if ("text/plain".equals(ent.getMimeType())) {
            try {
                SingleBody body = (SingleBody) ent.getBody();
                InputStream stream = body.getInputStream();
                InputStreamReader r = new InputStreamReader(stream, ent.getCharset());
                char[] buffer = new char[2097152];
                int len = r.read(buffer);
                String changeme = new String(buffer, 0, len);
                changeme = changeme.replaceAll("(?i:(http[s]{0,1}://[^\\n\\s]*))", "<a href=\"$1\">$1</a>");
                changeme = changeme.replaceAll("\n", "\n<br />");
                TextBody hotty = new BasicBodyFactory().textBody(changeme, ent.getCharset());
                AbstractEntity entity = (AbstractEntity) ent;
                entity.setContentTransferEncoding("7bit");
                ent.removeBody();
                ((AbstractEntity) ent).setText(hotty, "html");
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }

    protected void fixMessageEncoding(Entity ent, String transferEncoding) {
        block5:
        {
            String type;
            block4:
            {
                type = ent.getDispositionType();
                if (!ent.isMultipart()) break block4;
                Multipart mbody = (Multipart) ent.getBody();
                for (Entity next : mbody.getBodyParts()) {
                    this.fixMessageEncoding(next, next.getContentTransferEncoding());
                }
                break block5;
            }
            if (!(ent.getBody() instanceof SingleBody)) {
                return;
            }
            if ("inline".equals(type) && "base64".equals(transferEncoding) || "attachment".equals(type) && this.getContentId(ent) != null && "base64".equals(transferEncoding) || "base64".equals(transferEncoding) && this.getContentId(ent) != null || !"quoted-printable".equals(transferEncoding) && !"base64".equals(transferEncoding))
                break block5;
            try {
                SingleBody body = (SingleBody) ent.getBody();
                InputStream stream = body.getInputStream();
                TextBody hotty = new BasicBodyFactory().textBody(stream, ent.getCharset());
                AbstractEntity entity = (AbstractEntity) ent;
                entity.setContentTransferEncoding("7bit");
                ent.removeBody();
                ent.setBody(hotty);
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }

    public String getContentId(Entity e) {
        if (e == null) {
            return null;
        }
        Header header = e.getHeader();
        if (header == null) {
            return null;
        }
        Field field = header.getField("Content-ID");
        if (field == null) {
            return null;
        }
        return field.getBody();
    }

    protected void stripAttachments(Message message) throws IOException {
        if (!message.isMultipart()) {
            this.fixMessageEncoding(message, message.getContentTransferEncoding());
            this.fixMessageType(message);
            return;
        }
        do {
            Multipart multipart = (Multipart) message.getBody();
            List<Entity> tempz = multipart.getBodyParts();
            int x = 0;
            int y = -1;
            for (Entity ent : tempz) {
                if ("attachment".equals(ent.getDispositionType()) && this.getContentId(ent) == null) {
                    y = x;
                } else {
                    this.fixMessageEncoding(ent, ent.getContentTransferEncoding());
                }
                ++x;
            }
            if (y == -1) break;
            multipart.removeBodyPart(y);
        } while (true);
    }

    public void attachFile(String file) {
        if (!this.message.isMultipart()) {
            this.attachFileSingle(file);
        } else {
            this.attachFileMultipart(file);
        }
    }

    public void attachFileSingle(String file) {
        try {
            MultipartImpl container = new MultipartImpl("mixed");
            Body oldmessage = this.message.removeBody();
            BodyPart content = new BodyPart();
            content.setBody(oldmessage, this.message.getMimeType());
            BodyPart attachment = this.createAttachment(file);
            container.addBodyPart(content);
            container.addBodyPart(attachment);
            ((AbstractEntity) this.message).setMultipart(container);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void attachFileMultipart(String file) {
        try {
            Multipart multipart = (Multipart) this.message.getBody();
            BodyPart attachment = this.createAttachment(file);
            if ("mixed".equals(multipart.getSubType())) {
                multipart.addBodyPart(attachment);
            } else {
                MultipartImpl container = new MultipartImpl("mixed");
                Entity favored = null;
                for (Entity ent : multipart.getBodyParts()) {
                    if ("text/html".equals(ent.getMimeType())) {
                        favored = ent;
                        continue;
                    }
                    if (favored != null) continue;
                    favored = ent;
                    this.fixMessageType(favored);
                }
                if (favored != null) {
                    container.addBodyPart(favored);
                }
                container.addBodyPart(attachment);
                this.message.removeBody();
                ((AbstractEntity) this.message).setMultipart(container);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getSubject() {
        return this.message.getSubject();
    }

    public byte[] getMessage(String from, String to) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4194304);
        try {
            this.message.setDate(new Date());
            if (from != null) {
                this.message.setFrom(AddressBuilder.DEFAULT.parseMailbox(from));
            }
            if (to != null) {
                this.message.setTo(AddressBuilder.DEFAULT.parseMailbox(to));
            }
            DefaultMessageWriter writer = new DefaultMessageWriter();
            writer.writeMessage(this.message, output);
        } catch (Exception ioex) {
            throw new RuntimeException(ioex);
        }
        return output.toByteArray();
    }

    private BodyPart createAttachment(String name) throws IOException {
        File file = new File(name);
        String namez = file.getName();
        BinaryBody body = new StorageBodyFactory().binaryBody(new FileInputStream(name));
        HashMap<String, String> temp = new HashMap<>();
        temp.put("name", namez);
        BodyPart bodyPart = new BodyPart();
        bodyPart.setBody(body, "application/octet-stream", temp);
        bodyPart.setContentTransferEncoding("base64");
        bodyPart.setContentDisposition("attachment");
        bodyPart.setFilename(namez);
        return bodyPart;
    }

    static {
        TempFileStorageProvider storageProvider = new TempFileStorageProvider();
        DefaultStorageProvider.setInstance(storageProvider);
    }
}

