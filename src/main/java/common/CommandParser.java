package common;

import java.io.File;
import java.util.Set;
import java.util.Stack;

public class CommandParser {
    protected StringStack parse;
    protected String command;
    protected Stack args = new Stack();
    protected String error = null;
    protected String text;
    protected boolean missing = false;

    public CommandParser(String text) {
        this.text = text;
        this.reset();
    }

    public boolean is(String command) {
        return this.command.equals(command);
    }

    public String getCommand() {
        return this.command;
    }

    public String getArguments() {
        return this.parse.toString();
    }

    public String error() {
        return this.command + " error: " + this.error;
    }

    public boolean isMissingArguments() {
        boolean result = this.missing;
        if (this.missing) {
            this.reset();
        }
        return result;
    }

    public void error(String er) {
        this.error = er;
    }

    public boolean empty() {
        return this.parse.isEmpty();
    }

    public boolean hasError() {
        return this.error != null;
    }

    public boolean reset() {
        this.parse = new StringStack(this.text);
        this.command = this.parse.shift();
        this.error = null;
        this.args = new Stack();
        this.missing = false;
        return false;
    }

    public boolean verify(String description) {
        char[] v = description.toCharArray();
        for (char aV : v) {
            int i;
            String temp;
            if (this.parse.isEmpty()) {
                this.error = "not enough arguments";
                this.missing = true;
                return false;
            }
            if (aV == 'A') {
                this.args.push(this.parse.shift());
                continue;
            }
            if (aV == 'C') {
                temp = this.parse.shift();
                if (temp.equals("dns") || temp.equals("dns-txt") || temp.equals("http") || temp.equals("smb") || temp.equals("dns6")) {
                    this.args.push(temp);
                    continue;
                }
                this.error = "argument '" + temp + "' is not 'dns', 'dns6', 'dns-txt', 'http', or 'smb'";
                return false;
            }
            if (aV == 'D') {
                temp = this.parse.shift();
                if (temp.equals("icmp") || temp.equals("arp") || temp.equals("none")) {
                    this.args.push(temp);
                    continue;
                }
                this.error = "argument '" + temp + "' is not 'arp', 'icmp', or 'none'";
                return false;
            }
            if (aV == 'g') {
                temp = this.parse.shift();
                if (temp.equals("query") || temp.equals("queryv")) {
                    this.args.push(temp);
                    continue;
                }
                this.error = "argument '" + temp + "' is not 'query or queryv'";
                return false;
            }
            if (aV == 'H') {
                temp = this.parse.shift();
                if (temp.length() == 65 && temp.charAt(32) == ':') {
                    temp = temp.substring(33);
                }
                if (temp.length() != 32) {
                    this.error = "argument '" + temp + "' is not an NTLM hash";
                    return false;
                }
                this.args.push(temp);
                continue;
            }
            if (aV == 'I') {
                temp = this.parse.shift();
                try {
                    i = Integer.parseInt(temp);
                    this.args.push(i);
                    continue;
                } catch (Exception ex) {
                    this.error = "'" + temp + "' is not a number";
                    return false;
                }
            }
            if (aV == 'f') {
                File file = new File(this.parse.toString());
                if (!file.exists()) {
                    this.error = "'" + file.getAbsolutePath() + "' does not exist";
                    return false;
                }
                if (!file.canRead()) {
                    this.error = "'" + file.getAbsolutePath() + "' is not readable";
                    return false;
                }
                if (file.isDirectory()) {
                    this.error = "'" + file.getAbsolutePath() + "' is a directory";
                    return false;
                }
                this.args.push(file.getAbsolutePath());
                continue;
            }
            if (aV == 'F') {
                File file = new File(this.parse.toString());
                if (!file.exists()) {
                    this.error = "'" + file.getAbsolutePath() + "' does not exist";
                    return false;
                }
                if (!file.canRead()) {
                    this.error = "'" + file.getAbsolutePath() + "' is not readable";
                    return false;
                }
                if (file.isDirectory()) {
                    this.error = "'" + file.getAbsolutePath() + "' is a directory";
                    return false;
                }
                if (file.length() > 0x100000L) {
                    this.error = "max upload size is 1MB";
                    return false;
                }
                this.args.push(file.getAbsolutePath());
                continue;
            }
            if (aV == 'L') {
                String name = this.parse.toString();
                if (!Listener.isListener(name)) {
                    this.error = "Listener '" + name + "' does not exist";
                    return false;
                }
                this.args.push(name);
                continue;
            }
            if (aV == 'p') {
                File file = new File(this.parse.shift());
                if (!file.exists()) {
                    this.error = "'" + file.getAbsolutePath() + "' does not exist";
                    return false;
                }
                if (!file.canRead()) {
                    this.error = "'" + file.getAbsolutePath() + "' is not readable";
                    return false;
                }
                if (file.isDirectory()) {
                    this.error = "'" + file.getAbsolutePath() + "' is a directory";
                    return false;
                }
                this.args.push(file.getAbsolutePath());
                continue;
            }
            if (aV == 'Q') {
                temp = this.parse.shift();
                if (temp.equals("high") || temp.equals("low")) {
                    this.args.push(temp);
                    continue;
                }
                this.error = "argument '" + temp + "' is not 'high' or 'low'";
                return false;
            }
            if (aV == 'R') {
                temp = this.parse.shift();
                PortFlipper flipper = new PortFlipper(temp);
                flipper.parse();
                if (flipper.hasError()) {
                    this.error = flipper.getError();
                    return false;
                }
                this.args.push(temp);
                continue;
            }
            if (aV == 'T') {
                temp = this.parse.shift();
                AddressList alist = new AddressList(temp);
                if (alist.hasError()) {
                    this.error = alist.getError();
                    return false;
                }
                this.args.push(temp);
                continue;
            }
            if (aV == 'U') {
                temp = this.parse.shift();
                if (temp.startsWith("\\\\")) {
                    this.args.push(temp.substring(2));
                    continue;
                }
                this.error = "argument '" + temp + "' is not a \\\\target";
                return false;
            }
            if (aV == 'V') {
                temp = this.parse.shift();
                Set good = CommonUtils.toSet("computers, dclist, domain_trusts, group, localgroup, logons, sessions, share, time, user, view");
                if (good.contains(temp)) {
                    this.args.push(temp);
                    continue;
                }
                this.error = "argument '" + temp + "' is not a net command";
                return false;
            }
            if (aV == 'X') {
                temp = this.parse.shift();
                if (temp.equals("x86") || temp.equals("x64")) {
                    this.args.push(temp);
                    continue;
                }
                this.error = "argument '" + temp + "' is not 'x86' or 'x64'";
                return false;
            }
            if (aV == 'Z') {
                this.args.push(this.parse.toString());
                continue;
            }
            if (aV == '%') {
                temp = this.parse.shift();
                try {
                    i = Integer.parseInt(temp);
                    if (i < 0 || i > 99) {
                        this.error = "argument " + i + " is not a value 0-99";
                        return false;
                    }
                    this.args.push(i);
                    continue;
                } catch (Exception ex) {
                    this.error = "'" + temp + "' is not a number";
                    return false;
                }
            }
            if (aV != '?') continue;
            temp = this.parse.shift();
            if (temp.equals("start") || temp.equals("on") || temp.equals("true")) {
                this.args.push(Boolean.TRUE);
                continue;
            }
            if (temp.equals("stop") || temp.equals("off") || temp.equals("false")) {
                this.args.push(Boolean.FALSE);
                continue;
            }
            this.error = "'" + temp + "' is not a boolean value";
            return false;
        }
        return true;
    }

    public int popInt() {
        return (Integer) this.args.pop();
    }

    public String popString() {
        return this.args.pop() + "";
    }

    public boolean popBoolean() {
        return (Boolean) this.args.pop();
    }
}

