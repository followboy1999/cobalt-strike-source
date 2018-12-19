package server;

import beacon.BeaconSetup;
import c2profile.Profile;
import common.BeaconEntry;
import common.CommonUtils;
import common.GenericEvent;
import common.Keys;

import java.util.HashMap;
import java.util.Map;

public class ServerUtils {
    public static Profile getProfile(Resources r) {
        return (Profile) r.get("c2profile");
    }

    public static String randua(Resources r) {
        return BeaconSetup.randua(ServerUtils.getProfile(r));
    }

    public static WebCalls getWebCalls(Resources r) {
        return (WebCalls) r.get("webcalls");
    }

    public static boolean hasPublicStage(Resources r) {
        return ServerUtils.getProfile(r).option(".host_stage");
    }

    public static byte[] getBeaconStage(Resources r) {
        return (byte[]) r.get("BeaconStage");
    }

    public static byte[] getBeaconStage64(Resources r) {
        return (byte[]) r.get("BeaconStageX64");
    }

    public static byte[] getBeaconDLL(Resources r) {
        return (byte[]) r.get("BeaconDLL");
    }

    public static String getMyIP(Resources r) {
        return (String) r.get("localip");
    }

    public static String getBeaconDNSStage(Resources r) {
        return (String) r.get("BeaconDNSStage");
    }

    public static String getServerPassword(Resources r, String user) {
        return (String) r.get("password");
    }

    public static BeaconEntry getBeacon(Resources r, String bid) {
        return ((Beacons) r.get("beacons")).resolve(bid);
    }

    public static void addToken(Resources r, String token, String email, String cid) {
        HashMap<String, String> temp = new HashMap<>();
        temp.put("token", token);
        temp.put("email", email);
        temp.put("cid", cid);
        r.call("tokens.add", CommonUtils.args(Keys.TokenKey(temp), temp));
    }

    public static void addSession(Resources r, Map temp) {
        temp.put("opened", System.currentTimeMillis() + "");
        r.call("sessions.addnew", CommonUtils.args(Keys.SessionKey(temp), temp));
        r.call("sessions.push");
    }

    public static void addC2Info(Resources r, Map temp) {
        r.call("c2info.addnew", CommonUtils.args(Keys.C2InfoKey(temp), temp));
        r.call("c2info.push");
    }

    public static void addCredential(Resources r, String user, String pass, String realm, String source, String from, long logon) {
        HashMap<String, String> temp = new HashMap<>();
        temp.put("user", user);
        temp.put("password", pass);
        temp.put("realm", realm);
        temp.put("source", source);
        temp.put("host", from);
        if (logon > 0L) {
            temp.put("logon", Long.toString(logon));
        }
        String key = CommonUtils.CredKey(temp);
        r.call("credentials.addnew", CommonUtils.args(key, temp));
    }

    public static void addCredential(Resources r, String user, String pass, String realm, String source, String from) {
        ServerUtils.addCredential(r, user, pass, realm, source, from, 0L);
    }

    public static void addTarget(Resources r, String ipaddr, String name, String note, String os, double ver) {
        HashMap<String, String> temp = new HashMap<>();
        temp.put("address", ipaddr);
        if (name != null) {
            temp.put("name", name);
        }
        if (note != null) {
            temp.put("note", note);
        }
        if (os != null) {
            temp.put("os", os);
        }
        if (ver != 0.0) {
            temp.put("version", ver + "");
        }
        String key = CommonUtils.TargetKey(temp);
        r.call("targets.update", CommonUtils.args(key, temp));
        r.call("targets.push");
    }

    public static void fireEvent(Resources r, String name, String arg) {
        r.broadcast("propagate", new GenericEvent(name, arg));
    }
}

