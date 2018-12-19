package beacon;

import common.CommandParser;
import common.CommonUtils;
import common.MudgeSanity;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BeaconCommands {
    public Map descriptions = new HashMap();
    public Map details = new HashMap();

    public BeaconCommands() {
        this.loadCommands();
        this.loadDetails();
    }

    public void register(String command, String description, String detailz) {
        this.descriptions.put(command, description);
        this.details.put(command, detailz);
    }

    public String getCommandFile() {
        return "resources/bhelp.txt";
    }

    public String getDetailFile() {
        return "resources/bdetails.txt";
    }

    protected void loadCommands() {
        try {
            InputStream i = CommonUtils.resource(this.getCommandFile());
            byte[] data = CommonUtils.readAll(i);
            i.close();
            String[] all = CommonUtils.bString(data).split("\n");
            for (int x = 0; x < all.length; ++x) {
                String[] entry = all[x].split("\t+");
                if (entry.length == 2) {
                    this.descriptions.put(entry[0], entry[1]);
                    continue;
                }
                CommonUtils.print_error("bhelp, line: " + x + " '" + all[x] + "'");
            }
        } catch (IOException ioex) {
            MudgeSanity.logException("Load Commands", ioex, false);
        }
    }

    protected void loadDetails() {
        try {
            InputStream i = CommonUtils.resource(this.getDetailFile());
            byte[] data = CommonUtils.readAll(i);
            i.close();
            String[] all = CommonUtils.bString(data).split("\n");
            String command = null;
            StringBuilder detail = new StringBuilder();
            for (String anAll : all) {
                CommandParser p = new CommandParser(anAll);
                if (p.is("beacon>")) {
                    if (!p.verify("AZ")) continue;
                    if (command != null) {
                        this.details.put(command, detail.toString().trim());
                    }
                    command = p.popString();
                    detail = new StringBuilder();
                    continue;
                }
                detail.append(anAll).append("\n");
            }
            this.details.put(command, detail.toString().trim());
        } catch (IOException ioex) {
            MudgeSanity.logException("Load Details", ioex, false);
        }
    }

    public List commands() {
        synchronized (this) {
            return new LinkedList(this.descriptions.keySet());
        }
    }

    public String getDetails(String command) {
        synchronized (this) {
            return this.details.get(command) + "";
        }
    }

    public String getDescription(String command) {
        synchronized (this) {
            return this.descriptions.get(command) + "";
        }
    }

    public boolean isHelpAvailable(String command) {
        synchronized (this) {
            return this.details.containsKey(command);
        }
    }
}

