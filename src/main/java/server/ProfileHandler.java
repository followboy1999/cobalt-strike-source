package server;

import common.CommonUtils;
import common.LoggedEvent;
import common.ProfilerEvent;
import profiler.SystemProfiler;

import java.util.HashMap;
import java.util.Map;

public class ProfileHandler implements SystemProfiler.ProfileListener {
    protected Resources resources;

    public ProfileHandler(Resources r) {
        this.resources = r;
    }

    @Override
    public void receivedProfile(String external, String internal, String useragent, Map applications, String id) {
        String os = "unknown";
        double ver = 0.0;
        for (Object o1 : applications.keySet()) {
            String app = ((String) o1).toLowerCase();
            if (CommonUtils.iswm("*windows*", app)) {
                os = "Windows";
                if (CommonUtils.isin("2000", app)) {
                    ver = 5.0;
                    continue;
                }
                if (CommonUtils.isin("xp", app) || CommonUtils.isin("2003", app)) {
                    ver = 5.1;
                    continue;
                }
                if (CommonUtils.isin("7", app) || CommonUtils.isin("vista", app)) {
                    ver = 6.0;
                    continue;
                }
                if (CommonUtils.isin("8", app)) {
                    ver = 6.2;
                    continue;
                }
                if (!CommonUtils.isin("10", app)) continue;
                ver = 10.0;
                continue;
            }
            if (CommonUtils.iswm("*mac*ip*", app)) {
                os = "Apple iOS";
                continue;
            }
            if (CommonUtils.iswm("*mac*os*x*", app)) {
                os = "MacOS X";
                continue;
            }
            if (CommonUtils.iswm("*linux*", app)) {
                os = "Linux";
                continue;
            }
            if (!CommonUtils.iswm("*android*", app)) continue;
            os = "Android";
        }
        for (Object o : applications.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            HashMap<String, Object> result = new HashMap<>();
            result.put("nonce", CommonUtils.ID());
            result.put("external", external);
            result.put("internal", internal);
            result.put("useragent", useragent);
            result.put("id", id);
            result.put("application", entry.getKey());
            result.put("version", entry.getValue());
            result.put("date", System.currentTimeMillis() + "");
            result.put("os", os);
            result.put("osver", ver + "");
            String key = CommonUtils.ApplicationKey(result);
            this.resources.call("applications.add", CommonUtils.args(key, result));
        }
        if (!"unknown".equals(internal)) {
            ServerUtils.addTarget(this.resources, internal, null, null, os, ver);
            if (!internal.equals(external)) {
                ServerUtils.addTarget(this.resources, external, null, null, "firewall", 0.0);
            }
        } else {
            ServerUtils.addTarget(this.resources, external, null, null, os, ver);
        }
        this.resources.call("applications.push");
        this.resources.broadcast("weblog", new ProfilerEvent(external, internal, useragent, applications, id));
        this.resources.broadcast("eventlog", LoggedEvent.Notify("received system profile (" + applications.size() + " applications)"));
    }
}

