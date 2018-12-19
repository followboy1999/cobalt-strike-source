package c2profile;

import cloudstrike.Response;
import cloudstrike.WebServer;
import cloudstrike.WebService;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class MalleableHook implements WebService {
    protected MyHook hook = null;
    protected Profile profile;
    protected String desc;
    protected String type;
    protected String key = "";

    public MalleableHook(Profile profile, String type, String desc) {
        this.profile = profile;
        this.type = type;
        this.desc = desc;
    }

    public void setup(WebServer w, String key, MyHook hook) {
        this.hook = hook;
        this.key = key;
        w.register("beacon" + key, this);
        String[] uriz = this.profile.getString(key + ".uri").split(" ");
        for (String anUriz : uriz) {
            w.registerSecondary(anUriz, this);
            w.setSpecialPostURI(anUriz);
        }
    }

    @Override
    public void setup(WebServer w, String key) {
        throw new RuntimeException("Missing arguments");
    }

    @Override
    public Response serve(String uri, String method, Properties header, Properties param) {
        try {
            Response r = new Response("200 OK", null, (InputStream) null);
            byte[] b = this.hook.serve(uri, method, header, param);
            this.profile.apply(this.key + ".server", r, b);
            return r;
        } catch (Exception ex) {
            ex.printStackTrace();
            return new Response("500 Internal Server Error", "text/plain", "Oops... something went wrong");
        }
    }

    public String toString() {
        return this.desc;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public List cleanupJobs() {
        return new LinkedList();
    }

    @Override
    public boolean suppressEvent(String uri) {
        return true;
    }

    @Override
    public boolean isFuzzy() {
        return true;
    }

    public interface MyHook {
        byte[] serve(String var1, String var2, Properties var3, Properties var4);
    }

}

