package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.BeaconCommands;
import beacon.SecureShellTabCompletion;
import common.BeaconOutput;
import common.CommandParser;
import common.CommonUtils;
import console.Colors;
import console.GenericTabCompletion;
import dialog.SafeDialogs;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Stack;

public class SecureShellConsole
        extends BeaconConsole {
    public SecureShellConsole(String bid, AggressorClient client) {
        super(bid, client);
    }

    @Override
    public String getPrompt() {
        return Colors.underline("ssh") + "> ";
    }

    @Override
    public String Script(String name) {
        return "SSH_" + name;
    }

    @Override
    public GenericTabCompletion getTabCompletion() {
        return new SecureShellTabCompletion(this.bid, this.client, this.console);
    }

    @Override
    public void showPopup(String word, MouseEvent ev) {
        Stack<Scalar> arg = new Stack<>();
        LinkedList<String> items = new LinkedList<>();
        items.add(this.bid);
        arg.push(SleepUtils.getArrayWrapper(items));
        this.engine.getMenuBuilder().installMenu(ev, "ssh", arg);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        String text = ev.getActionCommand().trim();
        ((JTextField) ev.getSource()).setText("");
        CommandParser parser = new CommandParser(text);
        if (this.client.getSSHAliases().isAlias(parser.getCommand())) {
            this.master.input(text);
            this.client.getSSHAliases().fireCommand(this.bid, parser.getCommand(), parser.getArguments());
            return;
        }
        if (parser.is("help") || parser.is("?")) {
            this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, text)) + "\n");
            if (parser.verify("Z") || parser.reset()) {
                String command = parser.popString();
                BeaconCommands details = DataUtils.getSSHCommands(this.data);
                if (details.isHelpAvailable(command)) {
                    Stack<Scalar> temp = new Stack<>();
                    temp.push(SleepUtils.getScalar(command));
                    this.console.append(this.engine.format("SSH_OUTPUT_HELP_COMMAND", temp) + "\n");
                } else {
                    parser.error("no help is available for '" + command + "'");
                }
            } else {
                this.console.append(this.engine.format("SSH_OUTPUT_HELP", new Stack()) + "\n");
            }
            if (parser.hasError()) {
                this.console.append(this.formatLocal(BeaconOutput.Error(this.bid, parser.error())) + "\n");
            }
            return;
        }
        if (parser.is("downloads")) {
            this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, text)) + "\n");
            this.conn.call("beacons.downloads", CommonUtils.args(this.bid), (key, o) -> {
                Stack<Scalar> args = new Stack<>();
                args.push(CommonUtils.convertAll(o));
                args.push(SleepUtils.getScalar(SecureShellConsole.this.bid));
                SecureShellConsole.this.console.append(SecureShellConsole.this.engine.format("BEACON_OUTPUT_DOWNLOADS", args) + "\n");
            });
            return;
        }
        this.master.input(text);
        if (parser.is("cancel")) {
            if (parser.verify("Z")) {
                this.master.Cancel(parser.popString());
            }
        } else if (parser.is("cd")) {
            if (parser.verify("Z")) {
                this.master.Cd(parser.popString());
            }
        } else if (parser.is("clear")) {
            this.master.Clear();
        } else if (parser.is("download")) {
            if (parser.verify("Z")) {
                this.master.Download(parser.popString());
            }
        } else if (parser.is("exit")) {
            this.master.Die();
        } else if (parser.is("getuid")) {
            this.master.GetUID();
        } else if (parser.is("note")) {
            if (parser.verify("Z")) {
                String note = parser.popString();
                this.master.Note(note);
            } else if (parser.isMissingArguments()) {
                this.master.Note("");
            }
        } else if (parser.is("pwd")) {
            this.master.Pwd();
        } else if (parser.is("rportfwd")) {
            if (parser.verify("IAI") || parser.reset()) {
                int fport = parser.popInt();
                String fhost = parser.popString();
                int bport = parser.popInt();
                this.master.PortForward(bport, fhost, fport);
            } else if (parser.verify("AI")) {
                int bport = parser.popInt();
                String verb = parser.popString();
                if (!"stop".equals(verb)) {
                    parser.error("only acceptable argument is stop");
                } else {
                    this.master.PortForwardStop(bport);
                }
            }
        } else if (parser.is("shell")) {
            if (parser.verify("Z")) {
                this.master.Shell(parser.popString());
            }
        } else if (parser.is("sleep")) {
            if (parser.verify("I%") || parser.reset()) {
                int jitter = parser.popInt();
                int time = parser.popInt();
                this.master.Sleep(time, jitter);
            } else if (parser.verify("I")) {
                this.master.Sleep(parser.popInt(), 0);
            }
        } else if (parser.is("socks")) {
            if (parser.verify("I") || parser.reset()) {
                this.master.SocksStart(parser.popInt());
            } else if (parser.verify("Z")) {
                if (!parser.popString().equals("stop")) {
                    parser.error("only acceptable argument is stop or port");
                } else {
                    this.master.SocksStop();
                }
            }
        } else if (parser.is("sudo")) {
            if (parser.verify("AZ")) {
                String password = parser.popString();
                String command = parser.popString();
                this.master.ShellSudo(command, password);
            }
        } else if (parser.is("upload") && parser.empty()) {
            SafeDialogs.openFile("Select file to upload", null, null, false, false, r -> {
                if (CommonUtils.lof(r) > 786432L) {
                    SecureShellConsole.this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(SecureShellConsole.this.bid, "File " + r + " is larger than 768KB")));
                } else {
                    SecureShellConsole.this.master.Upload(r);
                }
            });
        } else if (parser.is("upload")) {
            if (parser.verify("F")) {
                String r = parser.popString();
                if (CommonUtils.lof(r) > 786432L) {
                    this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, "File " + r + " is larger than 768KB")));
                } else {
                    this.master.Upload(r);
                }
            }
        } else {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, "Unknown command: " + text)));
        }
        if (parser.hasError()) {
            this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, parser.error())));
        }
    }

}

