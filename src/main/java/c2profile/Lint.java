package c2profile;

import cloudstrike.NanoHTTPD;
import cloudstrike.Response;
import common.CommonUtils;
import common.MudgeSanity;
import net.jsign.DigestAlgorithm;
import net.jsign.timestamp.TimestampingMode;
import pe.MalleablePE;
import pe.PEParser;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.*;

public class Lint {
    public static final int PROGRAM_TRANSFORM = 0;
    public static final int PROGRAM_RECOVER = 1;
    protected Profile prof;
    protected String uri = "";
    protected Map headers = new HashMap();

    public Lint(Profile p) {
        this.prof = p;
    }

    public void bounds(String key, int low, int high) {
        int a = CommonUtils.toNumber(this.prof.getString(key), 0);
        if (a < low) {
            CommonUtils.print_error("Option " + key + " is " + a + "; less than lower bound of " + low);
        }
        if (a > high) {
            CommonUtils.print_error("Option " + key + " is " + a + "; greater than upper bound of " + high);
        }
    }

    public void boundsLen(String key, int max) throws Exception {
        String a = this.prof.getString(key);
        if (a.length() > max) {
            CommonUtils.print_error("Length of option " + key + " is " + a.length() + "; greater than upper bound of " + max);
        }
    }

    public byte[] randomData(int size) {
        Random gen = new Random();
        byte[] temp = new byte[size];
        gen.nextBytes(temp);
        return temp;
    }

    public void verb_compatability() {
        if ("GET".equals(this.prof.getString(".http-get.verb")) && this.prof.posts(".http-get.client.metadata")) {
            CommonUtils.print_error(".http-get.verb is GET, but .http-get.client.metadata needs POST");
        }
        if ("GET".equals(this.prof.getString(".http-post.verb"))) {
            if (this.prof.posts(".http-post.client.id")) {
                CommonUtils.print_error(".http-post.verb is GET, but .http-post.client.id needs POST");
            }
            if (this.prof.posts(".http-post.client.output")) {
                CommonUtils.print_error(".http-post.verb is GET, but .http-post.client.output needs POST");
            }
        }
    }

    public void safetylen(String describe, String key, Map stuff) {
        for (Object o : stuff.entrySet()) {
            Map.Entry next = (Map.Entry) o;
            String v = next.getValue() + "";
            if (v.length() <= 1024) continue;
            CommonUtils.print_error(key + " " + describe + " '" + next.getKey() + "' is " + v.length() + " bytes [should be <1024 bytes]");
        }
    }

    public void safetyuri(String key, String uri2, Map stuff) {
        StringBuilder value = new StringBuilder();
        value.append(this.uri).append(uri2).append("?");
        for (Object o : stuff.entrySet()) {
            Map.Entry next = (Map.Entry) o;
            String k = next.getKey() + "";
            String v = next.getValue() + "";
            value.append(k).append("=").append(v);
        }
        if (value.toString().length() > 1024) {
            CommonUtils.print_error(key + " URI line (uri + parameters) is " + value.toString().length() + " bytes [should be <1024 bytes]");
        }
    }

    public void testuri(String key, String uri, int max) {
        if (uri.length() > max) {
            CommonUtils.print_error(key + " is too long! " + uri.length() + " bytes [should be <=" + max + " bytes]");
        }
        if (uri.contains("?")) {
            CommonUtils.print_error(key + " '" + uri + "' should not contain a ?");
        }
        if (!uri.startsWith("/")) {
            CommonUtils.print_error(key + " '" + uri + "' must start with a /");
        }
    }

    public void testuri_stager(String key) {
        String uri = this.prof.getString(key);
        String qstring = this.prof.getQueryString(".http-stager.client");
        if (!"".equals(uri)) {
            this.testuri(key, uri, 79);
        } else {
            uri = CommonUtils.MSFURI();
        }
        if (!"".equals(qstring) && (uri = uri + "?" + qstring).length() > 79) {
            CommonUtils.print_error(key + " URI line (uri + parameters) is " + uri.length() + " bytes [should be <80 bytes]");
        }
    }

    public void testuri(String key) {
        int max = 0;
        String[] options = this.prof.getString(key + ".uri").split(" ");
        for (String option : options) {
            if (option.length() > max) {
                this.uri = option;
                max = option.length();
            }
            this.testuri(key + ".uri", option, 63);
        }
    }

    public void testuriCompare(String keyA, String keyB) {
        LintURI linter = new LintURI();
        linter.add_split(keyA, this.prof.getString(keyA));
        linter.add_split(keyB, this.prof.getString(keyB));
        linter.add(".http-stager.uri_x86", this.prof.getString(".http-stager.uri_x86"));
        linter.add(".http-stager.uri_x64", this.prof.getString(".http-stager.uri_x64"));
        linter.checks();
    }

    public boolean test(String tkey, String rkey, int size) throws Exception {
        return this.test(tkey, rkey, size, false);
    }

    public boolean test(String tkey, String rkey, int size, boolean fulltest) throws Exception {
        String key;
        byte[] intermediate;
        String value;
        Response r = new Response("200 OK", null, (InputStream) null);
        byte[] original = this.randomData(size);
        byte[] sendme = Arrays.copyOf(original, original.length);
        if (rkey.equals(".id")) {
            original = "1234".getBytes(StandardCharsets.UTF_8);
            sendme = Arrays.copyOf(original, original.length);
        }
        if (fulltest) {
            this.prof.apply(tkey, r, sendme);
        } else {
            this.prof.apply(tkey + rkey, r, sendme);
        }
        if (r.data != null) {
            intermediate = new byte[r.data.available()];
            r.data.read(intermediate, 0, intermediate.length);
        } else {
            intermediate = new byte[]{};
        }
        this.safetyuri(tkey, r.uri, r.params);
        this.safetylen("parameter", tkey, r.params);
        this.safetylen("header", tkey, r.header);
        String rcvd = this.prof.recover(tkey + rkey, r.header, r.params, new String(intermediate, "ISO8859-1"), r.uri);
        byte[] result = Program.toBytes(rcvd);
        if (!Arrays.equals(result, original)) {
            CommonUtils.print_error(tkey + rkey + " transform+recover FAILED (" + size + " byte[s])");
            return false;
        }
        Iterator i = r.params.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry next = (Map.Entry) i.next();
            next.setValue(URLEncoder.encode(next.getValue() + "", "UTF-8"));
        }
        i = r.header.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry next = (Map.Entry) i.next();
            key = next.getKey() + "";
            value = next.getValue() + "";
            next.setValue(value.replaceAll("\\P{Graph}", ""));
            if (!".http-get.server".equals(tkey)) continue;
            this.headers.put(key.toLowerCase(), value.toLowerCase());
        }
        rcvd = this.prof.recover(tkey + rkey, r.header, r.params, new String(intermediate, "ISO8859-1"), r.uri);
        result = Program.toBytes(rcvd);
        if (!Arrays.equals(result, original)) {
            CommonUtils.print_error(tkey + rkey + " transform+mangle+recover FAILED (" + size + " byte[s]) - encode your data!");
            return false;
        }
        CommonUtils.print_good(tkey + rkey + " transform+mangle+recover passed (" + size + " byte[s])");
        return true;
    }

    public boolean checkProgramSizes(String key, int max, int type) throws IOException {
        byte[] program = type == 0 ? this.prof.apply_binary(key) : this.prof.recover_binary(key);
        if (program.length < max) {
            return true;
        }
        CommonUtils.print_error("Program " + key + " size check failed.\n\tProgram " + key + " must have a compiled size less than " + max + " bytes. Current size is: " + program.length);
        return false;
    }

    public boolean checkPost3x() throws IOException {
        int sz = this.prof.size(".http-post.client.output", 2097152);
        if (sz < 6291456) {
            return true;
        }
        CommonUtils.print_error("POST 3x check failed.\n\tEncoded HTTP POST must be less than 3x size of non-encoded post. Tested: 2097152 bytes; received " + sz + " bytes");
        return false;
    }

    public void checkHeaders() {
        if ("chunked".equals(this.headers.get("transfer-encoding"))) {
            CommonUtils.print_error("Remove 'Transfer-Encoding: chunked' header. It will interfere with C2.");
        }
    }

    public void checkCollissions(String key) {
        Program p = this.prof.getProgram(key);
        if (p == null) {
            return;
        }
        List d = p.collissions();
        for (Object aD : d) {
            String next = (String) aD;
            CommonUtils.print_error(key + " collission for " + next);
        }
    }

    public void checkKeystore() {
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(this.prof.getSSLKeystore(), this.prof.getSSLPassword().toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, this.prof.getSSLPassword().toCharArray());
            SSLContext sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(kmf.getKeyManagers(), new TrustManager[]{new NanoHTTPD.TrustEverything()}, new SecureRandom());
            SSLServerSocketFactory factory = sslcontext.getServerSocketFactory();
        } catch (Exception ex) {
            CommonUtils.print_error("Could not load SSL keystore: " + ex.getMessage());
        }
    }

    public void checkCodeSigner() {
        if ("".equals(this.prof.getString(".code-signer.alias"))) {
            CommonUtils.print_error(".code-signer.alias is empty. This is the keystore alias for your imported code signing cert");
        }
        if ("".equals(this.prof.getString(".code-signer.password"))) {
            CommonUtils.print_error(".code-signer.password is empty. This is the keystore password");
        }
        if (!"".equals(this.prof.getString(".code-signer.digest_algorithm"))) {
            String algo = this.prof.getString(".code-signer.digest_algorithm");
            try {
                DigestAlgorithm.valueOf(algo);
            } catch (Exception ex) {
                CommonUtils.print_error(".code-sign.digest_algorithm '" + algo + "' is not valid. (Acceptable values: " + CommonUtils.joinObjects(DigestAlgorithm.values(), ", ") + ")");
            }
        }
        if (!"".equals(this.prof.getString(".code-signer.timestamp_mode"))) {
            String mode = this.prof.getString(".code-signer.timestamp_mode");
            try {
                TimestampingMode.valueOf(mode);
            } catch (Exception ex) {
                CommonUtils.print_error(".code-sign.timestamp_mode '" + mode + "' is not valid. (Acceptable values: " + CommonUtils.joinObjects(TimestampingMode.values(), ", ") + ")");
            }
        }
        String keystore = this.prof.getString(".code-signer.keystore");
        String password = this.prof.getString(".code-signer.password");
        String alias = this.prof.getString(".code-signer.alias");
        try {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(keystore), password.toCharArray());
        } catch (Exception ex) {
            CommonUtils.print_error(".code-signer.keystore failed to load keystore: " + ex.getMessage());
        }
    }

    public void checkPE() {
        try {
            MalleablePE pe_x86;
            byte[] rdata;
            byte[] x86_result;
            PEParser parser_x86 = PEParser.load(CommonUtils.resource("resources/beacon.dll"));
            int sizex86 = this.prof.getInt(".stage.image_size_x86");
            int sizedll = parser_x86.get("SizeOfImage");
            if (sizex86 > 0 && sizex86 < sizedll) {
                CommonUtils.print_error(".stage.image_size_x86 must be larger than " + sizedll + " bytes");
            }
            PEParser parser_x64 = PEParser.load(CommonUtils.resource("resources/beacon.x64.dll"));
            int sizex64 = this.prof.getInt(".stage.image_size_x64");
            int sizedll64 = parser_x64.get("SizeOfImage");
            if (sizex64 > 0 && sizex64 < sizedll64) {
                CommonUtils.print_error(".stage.image_size_x64 must be larger than " + sizedll64 + " bytes");
            }
            if ((x86_result = (pe_x86 = new MalleablePE(this.prof)).process(CommonUtils.readAll(CommonUtils.resource("resources/beacon.dll")), "x86")).length > 271000) {
                CommonUtils.print_error(".stage.transform-x86 results in a stage that's too large");
            } else if (x86_result.length == 0) {
                CommonUtils.print_error(".stage.transform-x86 failed (unknown reason)");
            }
            MalleablePE pe_x64 = new MalleablePE(this.prof);
            byte[] x64_result = pe_x64.process(CommonUtils.readAll(CommonUtils.resource("resources/beacon.x64.dll")), "x64");
            if (x64_result.length > 271000) {
                CommonUtils.print_error(".stage.transform-x64 results in a stage that's too large");
            } else if (x64_result.length == 0) {
                CommonUtils.print_error(".stage.transform-x86 failed (unknown reason)");
            }
            String rich = this.prof.getString(".stage.rich_header");
            if (rich.length() > 256) {
                CommonUtils.print_error(".stage.rich_header is too big. Reduce to <=256 bytes");
            }
            if ((rdata = this.prof.getToString(".stage").getBytes()).length > 4096) {
                CommonUtils.print_error(".stage added " + rdata.length + " bytes of strings. Reduce to <=4096");
            }
            Set dlls_x86 = CommonUtils.toSetLC(CommonUtils.readResourceAsString("resources/dlls.x86.txt").split("\n"));
            String mods_x86 = this.prof.getString(".stage.module_x86").toLowerCase();
            if (!"".equals(mods_x86) && dlls_x86.contains(mods_x86)) {
                CommonUtils.print_error(".stage.module_x86 stomps '" + mods_x86 + "' needed by x86 Beacon DLL.");
            }
            Set dlls_x64 = CommonUtils.toSetLC(CommonUtils.readResourceAsString("resources/dlls.x64.txt").split("\n"));
            String mods_x64 = this.prof.getString(".stage.module_x64").toLowerCase();
            if (!"".equals(mods_x64) && dlls_x64.contains(mods_x64)) {
                CommonUtils.print_error(".stage.module_x64 stomps '" + mods_x64 + "' needed by x64 Beacon DLL.");
            }
            if (!"".equals(mods_x86) && sizex86 > sizedll) {
                CommonUtils.print_warn(".stage.module_x86 AND .stage.image_size_x86 are defined. Risky! Will " + mods_x86 + " hold ~" + sizex86 * 2 + " bytes?");
            }
            if (!"".equals(mods_x64) && sizex64 > sizedll64) {
                CommonUtils.print_warn(".stage.module_x64 AND .stage.image_size_x64 are defined. Risky! Will " + mods_x64 + " hold ~" + sizex64 * 2 + " bytes?");
            }
        } catch (Exception ex) {
            MudgeSanity.logException("pe check", ex, false);
        }
    }

    public void checkProcessInject() {
        boolean userwx = this.prof.option(".process-inject.userwx");
        boolean startrwx = this.prof.option(".process-inject.startrwx");
        int min_alloc = this.prof.getInt(".process-inject.min_alloc");
        boolean createremotethread = this.prof.option(".process-inject.CreateRemoteThread");
        boolean setthreadcontext = this.prof.option(".process-inject.SetThreadContext");
        boolean rtlcreateuserthread = this.prof.option(".process-inject.RtlCreateUserThread");
        LinkedList<String> errors = new LinkedList<>();
        this.bounds(".process-inject.min_alloc", 0, 268435455);
        if (!(createremotethread || setthreadcontext || rtlcreateuserthread)) {
            CommonUtils.print_error("All .process-inject options are disabled. A lot of Beacon features will fail. This is a bad idea.");
            return;
        }
        if (!createremotethread && !setthreadcontext) {
            errors.add("\tx86 -> x86 injection will fail.");
        }
        if (!rtlcreateuserthread) {
            errors.add("\tx86 -> x64 injection will fail.");
        }
        if (!rtlcreateuserthread && !createremotethread) {
            errors.add("\tx64 -> x86 injection will fail.");
        }
        if (!rtlcreateuserthread && !createremotethread) {
            errors.add("\tx64 -> x64 injection will fail.");
        }
        if (errors.size() > 0) {
            CommonUtils.print_warn(".process-inject disables several functions. As a result:\n" + CommonUtils.join(errors, "\n"));
        }
    }

    public void setupProcessInject(String arch) {
        byte[] appended;
        byte[] prepended = this.prof.getPrependedData(".process-inject.transform-" + arch);
        int tlen = prepended.length + (appended = this.prof.getAppendedData(".process-inject.transform-" + arch)).length;
        if (tlen > 252) {
            CommonUtils.print_error(".process-inject.transform-" + arch + " is " + tlen + " bytes. Reduce to <=252 bytes");
        }
    }

    public void checkSpawnTo(String var, String badpath, String goodpath) {
        String value = this.prof.getString(var);
        if (value.length() > 63) {
            CommonUtils.print_error(var + " is too long. Limit to 63 characters");
        }
        if (!value.contains("\\")) {
            CommonUtils.print_error(var + " should refer to a full path.");
        }
        if (value.contains("\\system32\\")) {
            CommonUtils.print_error(var + " references system32. This will break x86->x64 and x64->x86 spawns");
        }
        if (value.contains(badpath)) {
            CommonUtils.print_error(var + " references " + badpath + ". For this architecture, probably not what you want");
        }
        if (!value.contains(goodpath) && value.toLowerCase().contains(goodpath)) {
            int idx = value.toLowerCase().indexOf(goodpath);
            String wanted = value.substring(idx, idx + goodpath.length());
            CommonUtils.print_error(var + ": lowercase '" + wanted + "'. This allows runtime adjustments to work");
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            CommonUtils.print_info("Please specify a Beacon profile file\n\t./c2lint my.profile");
            return;
        }
        try {
            int st_off;
            Profile p = Loader.LoadProfile(args[0]);
            Lint l = new Lint(p);
            if (p == null) {
                return;
            }
            StringBuilder preview = new StringBuilder();
            preview.append("Profile compiled OK\n\n");
            preview.append("http-get");
            preview.append("\n\u001b[01;30m");
            preview.append("--------");
            preview.append("\n\u001b[01;31m");
            preview.append(p.getPreview().getClientSample(".http-get"));
            preview.append("\u001b[01;34m");
            preview.append(p.getPreview().getServerSample(".http-get"));
            preview.append("\u001b[0m\n\n");
            preview.append("http-post");
            preview.append("\n\u001b[01;30m");
            preview.append("---------");
            preview.append("\n\u001b[01;31m");
            preview.append(p.getPreview().getClientSample(".http-post"));
            preview.append("\u001b[01;34m");
            preview.append(p.getPreview().getServerSample(".http-post"));
            preview.append("\u001b[0m\n\n");
            if (p.getProgram(".http-stager") != null) {
                preview.append("http-stager");
                preview.append("\n\u001b[01;30m");
                preview.append("-----------");
                preview.append("\n\u001b[01;31m");
                preview.append(p.getPreview().getClientSample(".http-stager"));
                preview.append("\u001b[01;34m");
                preview.append(p.getPreview().getServerSample(".http-stager"));
                preview.append("\u001b[0m\n\n");
            }
            if (!"".equals(p.getString(".dns_stager_subhost"))) {
                String subhost = p.getString(".dns_stager_subhost");
                preview.append("\ndns staging host");
                preview.append("\n\u001b[01;30m");
                preview.append("----------------");
                preview.append("\n\u001b[01;31m");
                preview.append("aaa").append(subhost).append("<domain>");
                if (p.hasString(".dns_stager_prepend")) {
                    preview.append(" = ");
                    preview.append(p.getString(".dns_stager_prepend"));
                    preview.append("[...]");
                }
                preview.append("\n");
                preview.append("bdc").append(subhost).append("<domain>");
                preview.append("\u001b[0m\n");
            }
            CommonUtils.print_good(preview.toString());
            if (l.checkPost3x()) {
                CommonUtils.print_good("POST 3x check passed");
            }
            if (l.checkProgramSizes(".http-get.server.output", 252, 1)) {
                CommonUtils.print_good(".http-get.server.output size is good");
            }
            if (l.checkProgramSizes(".http-get.client", 252, 0)) {
                CommonUtils.print_good(".http-get.client size is good");
            }
            if (l.checkProgramSizes(".http-post.client", 252, 0)) {
                CommonUtils.print_good(".http-post.client size is good");
            }
            l.testuri(".http-get");
            l.test(".http-get.client", ".metadata", 1, true);
            l.test(".http-get.client", ".metadata", 100, true);
            l.test(".http-get.client", ".metadata", 128, true);
            l.test(".http-get.client", ".metadata", 256, true);
            l.test(".http-get.server", ".output", 0, true);
            l.test(".http-get.server", ".output", 1, true);
            l.test(".http-get.server", ".output", 48248, true);
            l.test(".http-get.server", ".output", 1048576, true);
            l.testuri(".http-post");
            l.test(".http-post.client", ".id", 4);
            l.test(".http-post.client", ".output", 0);
            l.test(".http-post.client", ".output", 1);
            if (p.shouldChunkPosts()) {
                CommonUtils.print_good(".http-post.client.output chunks results");
                l.test(".http-post.client", ".output", 33);
                l.test(".http-post.client", ".output", 128);
            } else {
                CommonUtils.print_good(".http-post.client.output POSTs results");
                l.test(".http-post.client", ".output", 48248);
                l.test(".http-post.client", ".output", 1048576);
            }
            l.verb_compatability();
            l.testuri_stager(".http-stager.uri_x86");
            l.testuri_stager(".http-stager.uri_x64");
            String st_cl_h = p.getHeaders(".http-stager.client");
            if (st_cl_h.length() > 303) {
                CommonUtils.print_error(".http-stager.client headers are " + st_cl_h.length() + " bytes. Max length is 303 bytes");
            }
            if ((st_off = (int) p.getHTTPContentOffset(".http-stager.server")) > 0) {
                if ("".equals(p.getString(".http-stager.uri_x86"))) {
                    CommonUtils.print_error(".http-stager.uri_x86 is not defined.");
                }
                if ("".equals(p.getString(".http-stager.uri_x64"))) {
                    CommonUtils.print_error(".http-stager.uri_x64 is not defined.");
                }
            }
            l.bounds(".sleeptime", 0, Integer.MAX_VALUE);
            l.bounds(".jitter", 0, 99);
            l.bounds(".maxdns", 1, 255);
            l.bounds(".dns_max_txt", 4, 252);
            l.bounds(".dns_ttl", 1, Integer.MAX_VALUE);
            int dns_max_txt = Integer.parseInt(p.getString(".dns_max_txt"));
            if (dns_max_txt % 4 != 0) {
                CommonUtils.print_error(".dns_max_txt value (" + dns_max_txt + ") must be divisible by four.");
            }
            l.testuriCompare(".http-get.uri", ".http-post.uri");
            l.boundsLen(".spawnto", 63);
            l.boundsLen(".useragent", 128);
            l.boundsLen(".pipename", 64);
            l.boundsLen(".pipename_stager", 64);
            if (p.getString(".pipename").equals(p.getString(".pipename_stager"))) {
                CommonUtils.print_error(".pipename and .pipename_stager are the same. Make these different strings.");
            }
            l.checkHeaders();
            l.checkCollissions(".http-get.client");
            l.checkCollissions(".http-get.server");
            l.checkCollissions(".http-post.client");
            l.checkCollissions(".http-post.server");
            l.checkCollissions(".http-stager.client");
            l.checkCollissions(".http-stager.server");
            if (p.option(".host_stage")) {
                CommonUtils.print_good(".host_stage: Will host payload stage (HTTP/DNS)");
            } else {
                CommonUtils.print_warn(".host_stage is FALSE. This will break staging over HTTP, HTTPS, and DNS!");
            }
            if (!"rundll32.exe".equals(p.getString(".spawnto"))) {
                CommonUtils.print_error(".spawnto is deprecated and has no effect. Set spawnto_x86 and spawnto_x64 instead.");
            }
            l.checkSpawnTo(".spawnto_x86", "sysnative", "syswow64");
            l.checkSpawnTo(".spawnto_x64", "syswow64", "sysnative");
            if (p.isFile(".code-signer.keystore")) {
                CommonUtils.print_good("Found code-signing configuration. Will sign executables and DLLs");
                l.checkCodeSigner();
            } else {
                CommonUtils.print_warn(".code-signer.keystore is missing. Will not sign executables and DLLs");
            }
            if (p.isFile(".https-certificate.keystore")) {
                CommonUtils.print_good("Found SSL certificate keystore");
                if (p.getSSLPassword() == null || p.getSSLPassword().length() == 0) {
                    CommonUtils.print_error(".https-certificate.password is empty. A password is required for your keystore.");
                } else if ("123456".equals(p.getSSLPassword())) {
                    CommonUtils.print_warn(".https-certificate.password is the default '123456'. Is this really your keystore password?");
                }
            } else if (p.regenerateKeystore()) {
                if (p.getSSLKeystore() != null) {
                    CommonUtils.print_good("SSL certificate generation OK");
                }
            } else {
                CommonUtils.print_warn(".https-certificate options are missing [will use built-in SSL cert]");
            }
            l.checkKeystore();
            l.checkPE();
            if (!"".equals(p.getString(".dns_stager_subhost"))) {
                String subhost = p.getString(".dns_stager_subhost");
                if (!subhost.endsWith(".")) {
                    CommonUtils.print_error(".dns_stager_subhost must end with a '.' (it's prepended to a parent domain)");
                }
                if (subhost.length() > 32) {
                    CommonUtils.print_error(".dns_stager_subhost is too long. Keep it under 32 characters.");
                }
                if (subhost.contains("..")) {
                    CommonUtils.print_error(".dns_stager_subhost contains '..'. This is not valid in a hostname");
                }
            }
            if (!p.option(".create_remote_thread")) {
                CommonUtils.print_warn(".create_remote_thread is deprecated and has no effect.");
            }
            if (!p.option(".hijack_remote_thread")) {
                CommonUtils.print_warn(".hijack_remote_thread is deprecated and has no effect.");
            }
            l.setupProcessInject("x86");
            l.setupProcessInject("x64");
            l.checkProcessInject();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

