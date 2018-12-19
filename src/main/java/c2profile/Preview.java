package c2profile;

import beacon.BeaconSetup;
import cloudstrike.Response;
import common.CommonUtils;
import common.License;
import common.MudgeSanity;
import pe.MalleablePE;
import pe.PEParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class Preview {
    protected Profile c2profile;
    protected LinkedHashMap<String, Object> characteristics = null;
    protected LinkedList<String> notes = new LinkedList<>();

    public Preview(Profile c2profile) {
        this.c2profile = c2profile;
    }

    public String getSampleName() {
        return this.c2profile.getString(".sample_name");
    }

    public void summarize(Map<String, Object> metadata) {
        metadata.put("c2sample.client", this.getClientSample());
        metadata.put("c2sample.server", this.getServerSample());
        metadata.put("c2sample.name", this.getSampleName());
        metadata.put("c2sample.strings", this.getStrings());
        metadata.put("c2sample.pe", this.getPE());
    }

    public void note(String text) {
        this.notes.add(text);
    }

    public Map getPE() {
        if (this.characteristics != null) {
            return this.characteristics;
        }
        byte[] dll = CommonUtils.readResource("resources/beacon.dll");
        MalleablePE temp = new MalleablePE(this.c2profile);
        byte[] real = temp.pre_process(dll, "x86");
        PEParser parser = PEParser.load(real);
        this.characteristics = new LinkedHashMap<>();
        this.characteristics.put("Checksum", parser.get("CheckSum"));
        this.characteristics.put("Compilation Timestamp", parser.getDate("TimeDateStamp"));
        this.characteristics.put("Entry Point", parser.get("AddressOfEntryPoint"));
        this.characteristics.put("Name", parser.getString("Export.Name").replaceAll("\\P{Print}", "."));
        this.characteristics.put("Size", parser.get("SizeOfImage"));
        this.characteristics.put("Target Machine", "x86");
        if (License.isTrial()) {
            this.note("EICAR strings were observed within this payload and its traffic. This is a clever technique to detect and evade anti-virus products.");
        }
        if (this.c2profile.option(".stage.obfuscate")) {
            this.characteristics.remove("Name");
            if (!this.c2profile.option(".stage.cleanup")) {
                this.note("The final payload DLL is obfuscated in memory.");
                this.note("The package that loads the payload DLL is less obfuscated.");
            } else {
                this.note("The payload DLL obfuscates itself in memory.");
            }
        } else if (this.c2profile.option(".stage.stomppe")) {
            this.note("The payload DLL clears its in-memory MZ, PE, and e_lfanew header values. This is a common obfuscation for memory injected DLLs.");
        }
        if (this.c2profile.option(".stage.userwx")) {
            if ("".equals(this.c2profile.getString(".stage.module_x86"))) {
                this.note("This payload resides in memory pages with RWX permissions. These memory pages are not backed by a file on disk.");
            } else {
                this.note("This payload resides in memory pages with RWX permissions.");
            }
        }
        if (!"".equals(this.c2profile.getString(".stage.module_x86"))) {
            this.note("This payload loads " + this.c2profile.getString(".stage.module_x86") + " and overwrites its location in memory. This hides the payload within memory backed by this legitimate file.");
        }
        if (this.notes.size() > 0) {
            this.characteristics.put("Notes", CommonUtils.join(this.notes, " "));
        }
        return this.characteristics;
    }

    public String getClientSample() {
        return this.getClientSample(".http-get");
    }

    public String getServerSample() {
        return this.getServerSample(".http-get");
    }

    public String getStrings() {
        return this.c2profile.getToStringLog(".stage");
    }

    public String getClientSample(String tx) {
        Response response = new Response("200 OK", null, (InputStream) null);
        byte[] metadata = CommonUtils.randomData(16);
        String uri;
        String verb;
        if (tx.equals(".http-stager")) {
            uri = this.c2profile.getString(tx + ".uri_x86");
            if ("".equals(uri)) {
                uri = CommonUtils.MSFURI();
            }
            verb = "GET";
        } else {
            uri = CommonUtils.pick(this.c2profile.getString(tx + ".uri").split(" "));
            verb = this.c2profile.getString(tx + ".verb");
        }
        if (tx.equals(".http-post")) {
            metadata = CommonUtils.toBytes(CommonUtils.rand(99999) + "");
        }
        this.c2profile.apply(tx + ".client", response, metadata);
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
        uri = qstring.length() > 0 ? uri + response.uri + "?" + qstring : uri + response.uri;
        StringBuilder client = new StringBuilder();
        client.append(verb).append(" ").append(uri).append(" HTTP/1.1\n");
        if (!response.header.containsKey("User-Agent")) {
            response.header.put("User-Agent", BeaconSetup.randua(this.c2profile));
        }
        for (Object o : response.header.entrySet()) {
            Map.Entry next = (Map.Entry) o;
            String key = next.getKey() + "";
            String value = next.getValue() + "";
            next.setValue(value.replaceAll("\\P{Graph}", ""));
            client.append(key).append(": ").append(value).append("\n");
        }
        if (response.data != null) {
            try {
                byte[] intermediate = new byte[response.data.available()];
                response.data.read(intermediate, 0, intermediate.length);
                client.append("\n").append(CommonUtils.bString(intermediate).replaceAll("\\P{Print}", "."));
                client.append("\n");
            } catch (Exception ex) {
                MudgeSanity.logException("sample generate", ex, false);
            }
        }
        client.append("\n");
        return client.toString();
    }

    public String getServerSample(String tx) {
        try {
            Response response = new Response("200 OK", null, (InputStream) null);
            byte[] original = CommonUtils.randomData(64);
            if (".http-post".equals(tx)) {
                original = new byte[]{};
            }
            this.c2profile.apply(tx + ".server", response, original);
            StringBuilder server = new StringBuilder();
            server.append("HTTP/1.1 200 OK\n");
            for (Object o : response.header.entrySet()) {
                Map.Entry next = (Map.Entry) o;
                String key = next.getKey() + "";
                String value = next.getValue() + "";
                next.setValue(value.replaceAll("\\P{Graph}", ""));
                server.append(key).append(": ").append(value).append("\n");
            }
            byte[] intermediate = new byte[]{};
            if (response.data != null) {
                intermediate = new byte[response.data.available()];
                response.data.read(intermediate, 0, intermediate.length);
            }
            server.append("\n").append(CommonUtils.bString(intermediate).replaceAll("\\P{Print}", "."));
            return server.toString();
        } catch (IOException ioex) {
            MudgeSanity.logException("getServerSample: " + tx, ioex, false);
            return "";
        }
    }
}

