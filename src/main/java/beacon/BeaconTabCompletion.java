package beacon;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import common.BeaconEntry;
import common.CommonUtils;
import common.Listener;
import common.StringStack;
import console.Console;
import console.GenericTabCompletion;
import cortana.Cortana;

import java.io.File;
import java.util.*;

public class BeaconTabCompletion extends GenericTabCompletion {
    protected AggressorClient client;
    protected String bid;

    public BeaconTabCompletion(String bid, AggressorClient client, Console window) {
        super(window);
        this.client = client;
        this.bid = bid;
    }

    public static void filterList(List l, String filter) {
        Iterator i = l.iterator();

        while (i.hasNext()) {
            String cmd = i.next() + "";
            if (!cmd.startsWith(filter)) {
                i.remove();
            }
        }

    }

    public String transformText(String text) {
        return text.replace(" ~", " " + System.getProperty("user.home"));
    }

    public Collection getOptionsFromList(String text, List o2) {
        LinkedList options = new LinkedList();
        StringStack weasel = new StringStack(text, " ");
        weasel.pop();

        for (Object anO2 : o2) {
            options.add(weasel.toString() + " " + anO2);
        }

        Collections.sort(options);
        filterList(options, text);
        return options;
    }

    public boolean isFoo(String text) {
        return text.matches("elevate .*? .*") || text.matches("spawn x.. .*") || text.matches("spawnu \\d+ .*") || text.matches("inject \\d+ .*") || text.matches("psexec_psh .*? .*") || text.matches("winrm .*? .*") || text.matches("wmi .*? .*");
    }

    public boolean isBar(String text) {
        return text.matches("psexec .*? .*? .*") || text.matches("spawnas .*? .*? .*");
    }

    public Collection getOptions(String text) {
        List options = DataUtils.getBeaconCommands(this.client.getData()).commands();
        options.addAll(this.client.getAliases().commands());
        Collections.sort(options);
        Cortana.filterList(options, text);
        List targets;
        int thefirst;
        int thesecond;
        Iterator x;
        if (options.size() == 0 && text.matches("inject \\d+ x.. .*")) {
            targets = Listener.getListenerNamesWithSMB(this.client.getData());
            if (targets.size() == 0) {
                options.add(text);
            } else {
                thefirst = text.indexOf(" ");
                thesecond = text.indexOf(" ", thefirst + 1);
                options = new LinkedList();
                x = targets.iterator();

                while (x.hasNext()) {
                    options.add(text.substring(0, text.indexOf(" ", thesecond + 1)) + " " + x.next());
                }
            }

            Collections.sort(options);
            filterList(options, text);
        } else {
            Iterator i;
            if (options.size() == 0 && this.isFoo(text)) {
                targets = Listener.getListenerNamesWithSMB(this.client.getData());
                if (targets.size() == 0) {
                    options.add(text);
                } else {
                    thefirst = text.indexOf(" ");
                    options = new LinkedList();
                    i = targets.iterator();

                    while (i.hasNext()) {
                        options.add(text.substring(0, text.indexOf(" ", thefirst + 1)) + " " + i.next());
                    }
                }

                Collections.sort(options);
                filterList(options, text);
            } else if (options.size() == 0 && this.isBar(text)) {
                targets = Listener.getListenerNamesWithSMB(this.client.getData());
                if (targets.size() == 0) {
                    options.add(text);
                } else {
                    thefirst = text.indexOf(" ");
                    thesecond = text.indexOf(" ", thefirst + 1);
                    options = new LinkedList();
                    x = targets.iterator();

                    while (x.hasNext()) {
                        options.add(text.substring(0, text.indexOf(" ", thesecond + 1)) + " " + x.next());
                    }
                }

                Collections.sort(options);
                filterList(options, text);
            } else if (options.size() != 0 || !text.startsWith("spawn ") && !text.startsWith("bypassuac ")) {
                if (options.size() == 0 && text.startsWith("elevate ")) {
                    targets = DataUtils.getBeaconExploits(this.client.getData()).exploits();
                    return this.getOptionsFromList(text, targets);
                }

                if (options.size() != 0 || !text.startsWith("kerberos_ticket_use ") && !text.startsWith("kerberos_ccache_use ") && !text.startsWith("upload ") && !text.startsWith("powershell-import ")) {
                    if (options.size() != 0 || !text.matches("execute-assembly .*") && !text.matches("shspawn x.. .*") && !text.matches("shinject \\d+ x.. .*") && !text.matches("dllinject \\d+ .*") && !text.matches("ssh-key .*? .*? .*")) {
                        if (options.size() == 0 && (text.startsWith("help ") || text.startsWith("? "))) {
                            targets = CommonUtils.toList("net computers, net dclist, net domain_trusts, net group, net localgroup, net logons, net sessions, net share, net user, net view");
                            List builtin = DataUtils.getBeaconCommands(this.client.getData()).commands();
                            return this.getOptionsFromList(text, CommonUtils.combine(targets, builtin));
                        }

                        if (options.size() == 0 && (text.startsWith("psexec_psh ") || text.startsWith("psexec ") || text.startsWith("wmi ") || text.startsWith("winrm ") || text.startsWith("link ") || text.startsWith("ssh ") || text.startsWith("ssh-key "))) {
                            targets = DataUtils.getTargetNames(this.client.getData());
                            return this.getOptionsFromList(text, targets);
                        }

                        LinkedList results;
                        LinkedList res;
                        String cmd;
                        if (options.size() == 0 && (text.startsWith("powershell ") || text.startsWith("powerpick ") || text.matches("psinject \\d+ x.. .*"))) {
                            results = new LinkedList(DataUtils.getBeaconPowerShellCommands(this.client.getData(), this.bid));
                            res = new LinkedList();
                            i = results.iterator();

                            while (i.hasNext()) {
                                cmd = i.next() + "";
                                if (cmd.length() > 0) {
                                    res.add(cmd);
                                    res.add("Get-Help " + cmd + " -full");
                                }
                            }

                            return this.getOptionsFromList(text, res);
                        }

                        if (options.size() == 0 && text.matches("reg query.*? x.. .*")) {
                            return this.getOptionsFromList(text, CommonUtils.toList("HKCC\\, HKCR\\, HKCU\\, HKLM\\, HKU\\"));
                        }

                        if (options.size() == 0 && (text.startsWith("reg query ") || text.startsWith("reg queryv "))) {
                            return this.getOptionsFromList(text, CommonUtils.toList("x64, x86"));
                        }

                        if (options.size() == 0 && text.startsWith("reg ")) {
                            return this.getOptionsFromList(text, CommonUtils.toList("query, queryv"));
                        }

                        if (options.size() == 0 && text.startsWith("net ")) {
                            return this.getOptionsFromList(text, CommonUtils.toList("computers, dclist, domain_trusts, group, localgroup, logons, sessions, share, time, user, view"));
                        }

                        if (options.size() == 0 && text.startsWith("note ")) {
                            BeaconEntry me = DataUtils.getBeacon(this.client.getData(), this.bid);
                            if (me != null) {
                                res = new LinkedList();
                                res.add(me.getNote());
                                return this.getOptionsFromList(text, res);
                            }

                            return this.getOptionsFromList(text, new LinkedList());
                        }

                        if (options.size() == 0 && text.startsWith("covertvpn ")) {
                            return this.getOptionsFromList(text, DataUtils.getInterfaceList(this.client.getData()));
                        }

                        if (options.size() == 0 && text.startsWith("desktop ")) {
                            return this.getOptionsFromList(text, CommonUtils.toList("high, low"));
                        }

                        if (options.size() == 0 && text.startsWith("unlink ")) {
                            results = new LinkedList();
                            BeaconEntry me = DataUtils.getBeacon(this.client.getData(), this.bid);
                            if (me != null && me.getParentId() != null) {
                                BeaconEntry parent = DataUtils.getBeacon(this.client.getData(), me.getParentId());
                                if (parent != null) {
                                    results.add(parent.getInternal());
                                }
                            }

                            i = DataUtils.getBeaconChildren(this.client.getData(), this.bid).iterator();

                            while (i.hasNext()) {
                                BeaconEntry child = (BeaconEntry) i.next();
                                results.add(child.getInternal());
                            }

                            return this.getOptionsFromList(text, results);
                        }

                        if (options.size() == 0 && text.startsWith("mimikatz ")) {
                            results = new LinkedList(CommonUtils.toList(CommonUtils.readResourceAsString("resources/mimikatz.txt").trim().split("\n")));
                            res = new LinkedList();
                            i = results.iterator();

                            while (i.hasNext()) {
                                cmd = (i.next() + "").trim();
                                res.add(cmd);
                                res.add("!" + cmd);
                                res.add("@" + cmd);
                            }

                            return this.getOptionsFromList(text, res);
                        }
                    } else {
                        StringStack weasel = new StringStack(text, " ");
                        String file = weasel.pop();
                        File temp = new File(file);
                        if (!temp.exists() || !temp.isDirectory()) {
                            temp = temp.getParentFile();
                        }

                        options = new LinkedList();
                        if (temp == null) {
                            options.add(text);
                            return options;
                        }

                        File[] s = temp.listFiles();

                        for (int y = 0; s != null && y < s.length; ++y) {
                            options.add(weasel.toString() + " " + s[y].getAbsolutePath());
                        }

                        Collections.sort(options);
                        filterList(options, text);
                    }
                } else {
                    String file = text.substring(text.indexOf(" ") + 1);
                    File temp = new File(file);
                    if (!temp.exists() || !temp.isDirectory()) {
                        temp = temp.getParentFile();
                    }

                    options = new LinkedList();
                    if (temp == null) {
                        options.add(text);
                        return options;
                    }

                    File[] s = temp.listFiles();

                    for (int y = 0; s != null && y < s.length; ++y) {
                        if (s[y].isDirectory() || !text.startsWith("powershell-import") || s[y].getName().endsWith(".ps1")) {
                            options.add(text.substring(0, text.indexOf(" ")) + " " + s[y].getAbsolutePath());
                        }
                    }

                    Collections.sort(options);
                    filterList(options, text);
                }
            } else {
                targets = Listener.getListenerNamesWithSMB(this.client.getData());
                if (targets.size() == 0) {
                    options.add(text);
                } else {
                    options = new LinkedList();

                    for (Object target : targets) {
                        options.add(text.substring(0, text.indexOf(" ")) + " " + target);
                    }
                }

                Collections.sort(options);
                filterList(options, text);
            }
        }

        return options;
    }
}
