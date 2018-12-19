package c2profile;

import beacon.BeaconSetup;
import cloudstrike.Response;
import common.CodeSigner;
import common.CommonUtils;
import common.MudgeSanity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class Profile {
    protected HashMap<String,Object> data = new HashMap<>();
    protected Preview preview = null;

    public void addParameter(String key, String parameter) {
        this.data.put(key, parameter);
    }

    public void logToString(String namespace, String original) {
        String key = namespace + ".log.string";
        if (!this.data.containsKey(key)) {
            this.data.put(key, new LinkedList<String>());
        }
        LinkedList<String> temp = (LinkedList<String>) this.data.get(key);
        temp.add(original.trim());
    }

    public String getToStringLog(String namespace) {
        String key = namespace + ".log.string";
        if (!this.data.containsKey(key)) {
            return null;
        }
        LinkedList<String> temp = (LinkedList<String>) this.data.get(key);
        return CommonUtils.join(temp, "\n");
    }

    public void addToString(String namespace, byte[] dataz) {
        String key = namespace + ".string";
        if (!this.data.containsKey(key)) {
            this.data.put(key, new SmartBuffer());
        }
        SmartBuffer temp = (SmartBuffer) this.data.get(key);
        temp.append(dataz);
    }

    public SmartBuffer getToString(String namespace) {
        String key = namespace + ".string";
        if (!this.data.containsKey(key)) {
            return new SmartBuffer();
        }
        return (SmartBuffer) this.data.get(key);
    }

    public boolean isSealed(String key) {
        Program p = this.getProgram(key);
        return p != null && p.isSealed();
    }

    public Preview getPreview() {
        synchronized (this) {
            if (this.preview == null) {
                this.preview = new Preview(this);
            }
            return this.preview;
        }
    }

    public void addCommand(String key, String command, String argument) {
        if (!this.data.containsKey(key)) {
            this.data.put(key, new Program(this));
        }
        Program sofar = (Program) this.data.get(key);
        sofar.addStep(command, argument);
    }

    public void apply(String key, Response r, byte[] c) {
        Program sofar = (Program) this.data.get(key);
        if (sofar != null) {
            sofar.transform(r, c);
        }
    }

    public String recover(String key, Map headers, Map parameters, String input, String uri) {
        Program sofar = (Program) this.data.get(key);
        return sofar.recover(headers, parameters, input, uri);
    }

    public Program getProgram(String key) {
        return (Program) this.data.get(key);
    }

    public byte[] apply_binary(String key) throws IOException {
        Program sofar = (Program) this.data.get(key);
        return sofar.transform_binary();
    }

    public byte[] recover_binary(String key) throws IOException {
        Program sofar = (Program) this.data.get(key);
        return sofar.recover_binary();
    }

    public int size(String key, int hypothetical) throws IOException {
        byte[] data = new byte[hypothetical];
        Response r = new Response("200 OK", null, (InputStream) null);
        this.apply(key, r, data);
        if (r.data != null) {
            return r.data.available();
        }
        return 0;
    }

    public boolean hasString(String key) {
        return this.data.containsKey(key);
    }

    public String getString(String key) {
        return this.data.get(key) + "";
    }

    public boolean option(String key) {
        return this.getString(key).equals("true");
    }

    public File getFile(String key) {
        return new File(this.getString(key));
    }

    public CodeSigner getCodeSigner() {
        return new CodeSigner(this);
    }

    public boolean isFile(String key) {
        if ("".equals(this.getString(key))) {
            return false;
        }
        return this.getFile(key).exists();
    }

    public boolean posts(String key) {
        Program p = this.getProgram(key);
        if (p == null) {
            return false;
        }
        return p.postsData();
    }

    public boolean shouldChunkPosts() {
        return !this.posts(".http-post.client.output");
    }

    public boolean exerciseCFGCaution() {
        return !"".equals(this.getString(".stage.module_x86")) || !"".equals(this.getString(".stage.module_x64"));
    }

    public int getInt(String key) {
        return Integer.parseInt(this.getString(key));
    }

    protected String certDescription() {
        return "CN=" + this.getString(".https-certificate.CN") + ", OU=" + this.getString(".https-certificate.OU") + ", O=" + this.getString(".https-certificate.O") + ", L=" + this.getString(".https-certificate.L") + ", ST=" + this.getString(".https-certificate.ST") + ", C=" + this.getString(".https-certificate.C");
    }

    public boolean regenerateKeystore() {
        return !"CN=, OU=, O=, L=, ST=, C=".equals(this.certDescription()) || this.getInt(".https-certificate.validity") != 3650;
    }

    public String getSSLPassword() {
        return this.getString(".https-certificate.password");
    }

    public boolean hasValidSSL() {
        return this.isFile(".https-certificate.keystore");
    }

    public InputStream getSSLKeystore() {
        try {
            if (this.isFile(".https-certificate.keystore")) {
                return new FileInputStream(this.getFile(".https-certificate.keystore"));
            }
            if (!this.regenerateKeystore()) {
                return null;
            }
            File temp = new File("./ssl" + System.currentTimeMillis() + ".store");
            temp.deleteOnExit();
            LinkedList<String> args = new LinkedList<>();
            args.add("keytool");
            args.add("-keystore");
            args.add(temp.getAbsolutePath());
            args.add("-storepass");
            args.add("123456");
            args.add("-keypass");
            args.add("123456");
            args.add("-genkey");
            args.add("-keyalg");
            args.add("RSA");
            args.add("-alias");
            args.add("cobaltstrike");
            args.add("-dname");
            args.add(this.certDescription());
            args.add("-validity");
            args.add(this.getString(".https-certificate.validity"));
            ProcessBuilder builder = new ProcessBuilder(args);
            builder.inheritIO();
            Process keystore = builder.start();
            keystore.waitFor();
            return new FileInputStream(temp);
        } catch (Exception ex) {
            CommonUtils.print_error("SSL certificate generation failed:\n\t" + ex.getMessage());
            return null;
        }
    }

    public byte[] getPrependedData(String pkey) {
        Program prog = this.getProgram(pkey);
        if (prog == null) {
            return new byte[0];
        }
        return prog.getPrependedData();
    }

    public byte[] getAppendedData(String pkey) {
        Program prog = this.getProgram(pkey);
        if (prog == null) {
            return new byte[0];
        }
        return prog.getAppendedData();
    }

    public long getHTTPContentOffset(String pkey) {
        Response response = new Response("200 OK", null, (InputStream) null);
        byte[] metadata = CommonUtils.randomData(16);
        this.apply(pkey, response, metadata);
        return response.offset;
    }

    public String getHeaders(String pkey) {
        Response response = new Response("200 OK", null, (InputStream) null);
        byte[] metadata = CommonUtils.randomData(16);
        this.apply(pkey, response, metadata);
        StringBuilder headers = new StringBuilder();
        if (!response.header.containsKey("User-Agent")) {
            response.header.put("User-Agent", BeaconSetup.randua(this));
        }
        for (Object o : response.header.entrySet()) {
            Map.Entry next = (Map.Entry) o;
            String key = next.getKey() + "";
            String value = next.getValue() + "";
            headers.append(key).append(": ").append(value).append("\r\n");
        }
        return headers.toString();
    }

    public String getQueryString(String pkey) {
        Response response = new Response("200 OK", null, (InputStream) null);
        byte[] metadata = CommonUtils.randomData(16);
        this.apply(pkey, response, metadata);
        StringBuilder qstring = new StringBuilder();
        Iterator i = response.params.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry next = (Map.Entry) i.next();
            String key = next.getKey() + "";
            String value = next.getValue() + "";
            try {
                next.setValue(URLEncoder.encode(next.getValue() + "", "UTF-8"));
            } catch (Exception ex) {
                MudgeSanity.logException("url encoding: " + next, ex, false);
            }
            qstring.append(key).append("=").append(value);
            if (!i.hasNext()) continue;
            qstring.append("&");
        }
        if (qstring.length() == 0) {
            return "";
        }
        return "?" + qstring;
    }
}

