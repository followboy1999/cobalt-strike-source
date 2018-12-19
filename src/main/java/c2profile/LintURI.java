package c2profile;

import common.CommonUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LintURI {
    protected List<Map<String, String>> uris = new LinkedList<>();

    public static String KEY(Map v) {
        return (String) v.get("key");
    }

    public static String URI(Map v) {
        return (String) v.get("uri");
    }

    public void add(String key, String uri) {
        if (uri != null && !"".equals(uri)) {
            this.uris.add(CommonUtils.toMap("uri", uri, "key", key));
        }
    }

    public void add_split(String key, String uri) {
        String[] uris = uri.split(" ");
        for (String uri1 : uris) {
            this.add(key, uri1);
        }
    }

    public void check(Map valueA, Map valueB) {
        String uriB;
        String uriA = LintURI.URI(valueA);
        if (uriA.equals(uriB = LintURI.URI(valueB))) {
            CommonUtils.print_error(LintURI.KEY(valueA) + " and " + LintURI.KEY(valueB) + " have same URI '" + uriA + "'. These values must be unique");
        } else if (uriA.startsWith(uriB)) {
            CommonUtils.print_warn(LintURI.KEY(valueB) + " URI " + uriB + " has common base with " + LintURI.KEY(valueA) + " URI " + uriA + " (this may confuse uri-append)");
        }
    }

    public void checks() {
        for (Map<String, String> uri1 : this.uris) {
            for (Map<String, String> uri2 : this.uris) {
                if (LintURI.KEY(uri1).equals(LintURI.KEY(uri2))) continue;
                this.check(uri1, uri2);
            }
        }
    }
}

