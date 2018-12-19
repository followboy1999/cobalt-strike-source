package cloudstrike;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class ServeFile implements WebService {
    protected String mimetype;
    protected File resource;

    public ServeFile(File resource, String mimetype) {
        this.resource = resource;
        this.mimetype = mimetype;
    }

    @Override
    public void setup(WebServer w, String uri) {
        w.register(uri, this);
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties param) {
        try {
            return new Response("200 OK", this.mimetype, new FileInputStream(this.resource), this.resource.length());
        } catch (IOException ioex) {
            WebServer.logException("Could not serve: '" + this.resource + "'", ioex, false);
            return new Response("404 Not Found", "text/plain", "");
        }
    }

    public String toString() {
        return "Serves " + this.resource;
    }

    @Override
    public String getType() {
        return "page";
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

