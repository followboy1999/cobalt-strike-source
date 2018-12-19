package beacon;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.BeaconEntry;
import common.StringStack;
import console.Console;
import console.GenericTabCompletion;
import cortana.Cortana;

import java.io.File;
import java.util.*;

public class SecureShellTabCompletion
        extends GenericTabCompletion {
    protected AggressorClient client;
    protected String bid;

    public SecureShellTabCompletion(String bid, AggressorClient client, Console window) {
        super(window);
        this.client = client;
        this.bid = bid;
    }

    public static void filterList(List l, String filter) {
        Iterator i = l.iterator();
        while (i.hasNext()) {
            String cmd = i.next() + "";
            if (cmd.startsWith(filter)) continue;
            i.remove();
        }
    }

    @Override
    public String transformText(String text) {
        return text.replace(" ~", " " + System.getProperty("user.home"));
    }

    public Collection getOptionsFromList(String text, List o2) {
        LinkedList<String> options = new LinkedList<>();
        StringStack weasel = new StringStack(text, " ");
        weasel.pop();
        for (Object anO2 : o2) {
            options.add(weasel.toString() + " " + anO2);
        }
        Collections.sort(options);
        SecureShellTabCompletion.filterList(options, text);
        return options;
    }

    @Override
    public Collection getOptions(String text) {
        List options = DataUtils.getSSHCommands(this.client.getData()).commands();
        options.addAll(this.client.getSSHAliases().commands());
        Collections.sort(options);
        Cortana.filterList(options, text);
        if (options != null && options.size() == 0 && text.startsWith("upload ")) {
            String file = text.substring(text.indexOf(" ") + 1);
            File temp = new File(file);
            if (!temp.exists() || !temp.isDirectory()) {
                temp = temp.getParentFile();
            }
            options = new LinkedList<String>();
            if (temp == null) {
                options.add(text);
                return options;
            }
            File[] s = temp.listFiles();
            for (int x = 0; s != null && x < s.length; ++x) {
                options.add(text.substring(0, text.indexOf(" ")) + " " + s[x].getAbsolutePath());
            }
            Collections.sort(options);
            SecureShellTabCompletion.filterList(options, text);
        } else {
            if (options != null && options.size() == 0 && (text.startsWith("help ") || text.startsWith("? "))) {
                List builtin = DataUtils.getSSHCommands(this.client.getData()).commands();
                return this.getOptionsFromList(text, builtin);
            }
            if (options != null && options.size() == 0 && text.startsWith("note ")) {
                BeaconEntry me = DataUtils.getBeacon(this.client.getData(), this.bid);
                if (me != null) {
                    LinkedList<String> o = new LinkedList<>();
                    o.add(me.getNote());
                    return this.getOptionsFromList(text, o);
                }
                return this.getOptionsFromList(text, new LinkedList());
            }
        }
        return options;
    }
}

