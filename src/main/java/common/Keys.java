package common;

import dialog.DialogUtils;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Keys {
    public static final Set models = CommonUtils.toSet("applications, c2info, credentials, services, sessions, targets, tokens");

    public static String[] getCols(String model) {
        switch (model) {
            case "applications":
                return CommonUtils.toArray("external, internal, application, version, date, id");
            case "c2info":
                return CommonUtils.toArray("bid, port, dns, ssl, domains");
            case "credentials":
                return CommonUtils.toArray("user, password, realm, source, host, note");
            case "services":
                return CommonUtils.toArray("address, port, banner, note");
            case "sessions":
                return CommonUtils.toArray("id, opened, external, internal, user, computer, pid, is64, pbid, note");
            case "targets":
                return CommonUtils.toArray("address, name, os, version, note");
            case "tokens":
                return CommonUtils.toArray("token, email, cid");
            default:
                return new String[0];
        }
    }

    public static int size() {
        return models.size();
    }

    public static boolean isDataModel(String key) {
        return models.contains(key);
    }

    public static Iterator getDataModelIterator() {
        return models.iterator();
    }

    public static String C2InfoKey(Map options) {
        return DialogUtils.string(options, "bid");
    }

    public static String SessionKey(Map session) {
        return DialogUtils.string(session, "id");
    }

    public static String TargetKey(Map options) {
        return DialogUtils.string(options, "address");
    }

    public static String ApplicationKey(Map options) {
        return DialogUtils.string(options, "nonce");
    }

    public static String TokenKey(Map options) {
        return DialogUtils.string(options, "token");
    }

    public static String ServiceKey(Map options) {
        String host = DialogUtils.string(options, "address");
        String port = DialogUtils.string(options, "port");
        return MessageFormat.format("{0}:{1}", host, port);
    }

    public static String CredKey(Map options) {
        String user = DialogUtils.string(options, "user");
        String pass = DialogUtils.string(options, "password");
        String realm = DialogUtils.string(options, "realm");
        String from = DialogUtils.string(options, "from");
        return MessageFormat.format("{0}.{1}.{2}.{3}", user, pass, realm, from);
    }
}

