package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.dialogs.BypassUACDialog;
import aggressor.dialogs.ElevateDialog;
import beacon.BeaconCommands;
import beacon.EncodedCommandBuilder;
import beacon.PowerShellTasks;
import beacon.TaskBeacon;
import common.BeaconEntry;
import common.CommonUtils;
import common.TeamQueue;
import cortana.Cortana;
import dialog.DialogUtils;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.interfaces.Predicate;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

public class BeaconBridge implements Function,
        Loadable,
        Predicate {
    protected Cortana engine;
    protected TeamQueue conn;
    protected AggressorClient client;

    public BeaconBridge(AggressorClient c, Cortana e, TeamQueue q) {
        this.client = c;
        this.engine = e;
        this.conn = q;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&externalc2_start", this);
        Cortana.put(si, "&beacon_commands", this);
        Cortana.put(si, "&ssh_commands", this);
        Cortana.put(si, "&beacon_command_describe", this);
        Cortana.put(si, "&ssh_command_describe", this);
        Cortana.put(si, "&beacon_command_detail", this);
        Cortana.put(si, "&ssh_command_detail", this);
        Cortana.put(si, "&beacons", this);
        Cortana.put(si, "&beacon_data", this);
        Cortana.put(si, "&bdata", this);
        Cortana.put(si, "&beacon_info", this);
        Cortana.put(si, "&binfo", this);
        Cortana.put(si, "&beacon_note", this);
        Cortana.put(si, "&beacon_remove", this);
        Cortana.put(si, "&beacon_command_register", this);
        Cortana.put(si, "&ssh_command_register", this);
        Cortana.put(si, "&beacon_ids", this);
        Cortana.put(si, "&beacon_host_script", this);
        Cortana.put(si, "&beacon_host_imported_script", this);
        Cortana.put(si, "&beacon_execute_job", this);
        Cortana.put(si, "&bls", this);
        Cortana.put(si, "&bps", this);
        Cortana.put(si, "&bipconfig", this);
        Cortana.put(si, "&openOrActivate", this);
        Cortana.put(si, "&openBypassUACDialog", this);
        Cortana.put(si, "&openElevateDialog", this);
        si.getScriptEnvironment().getEnvironment().put("-isssh", this);
        si.getScriptEnvironment().getEnvironment().put("-isbeacon", this);
        si.getScriptEnvironment().getEnvironment().put("-isadmin", this);
        si.getScriptEnvironment().getEnvironment().put("-is64", this);
    }

    @Override
    public boolean decide(String predicateName, ScriptInstance anInstance, Stack passedInTerms) {
        String bid = BridgeUtilities.getString(passedInTerms, "");
        BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), bid);
        if (entry == null) {
            return false;
        }
        if ("-isssh".equals(predicateName)) {
            return entry.isSSH();
        }
        if ("-isbeacon".equals(predicateName)) {
            return entry.isBeacon();
        }
        if ("-isadmin".equals(predicateName)) {
            return entry.isAdmin();
        }
        if ("-is64".equals(predicateName)) {
            return entry.is64();
        }
        return false;
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    public static String[] bids(Stack args) {
        if (args.isEmpty()) {
            return new String[0];
        }
        Scalar temp = (Scalar) args.peek();
        if (temp.getArray() != null) {
            return CommonUtils.toStringArray(BridgeUtilities.getArray(args));
        }
        return new String[]{((Scalar) args.pop()).stringValue()};
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&externalc2_start")) {
            String bindaddr = BridgeUtilities.getString(args, "0.0.0.0");
            int bindport = BridgeUtilities.getInt(args, 2222);
            this.conn.call("exoticc2.start", CommonUtils.args(bindaddr, bindport));
            return SleepUtils.getEmptyScalar();
        }
        if (name.equals("&beacon_commands")) {
            BeaconCommands commands = DataUtils.getBeaconCommands(this.client.getData());
            return SleepUtils.getArrayWrapper(commands.commands());
        }
        if (name.equals("&ssh_commands")) {
            BeaconCommands commands = DataUtils.getSSHCommands(this.client.getData());
            return SleepUtils.getArrayWrapper(commands.commands());
        }
        if (name.equals("&beacon_command_describe")) {
            String command = BridgeUtilities.getString(args, "");
            BeaconCommands commands = DataUtils.getBeaconCommands(this.client.getData());
            return SleepUtils.getScalar(commands.getDescription(command));
        }
        if (name.equals("&ssh_command_describe")) {
            String command = BridgeUtilities.getString(args, "");
            BeaconCommands commands = DataUtils.getSSHCommands(this.client.getData());
            return SleepUtils.getScalar(commands.getDescription(command));
        }
        if (name.equals("&beacon_command_detail")) {
            String command = BridgeUtilities.getString(args, "");
            BeaconCommands commands = DataUtils.getBeaconCommands(this.client.getData());
            return SleepUtils.getScalar(commands.getDetails(command));
        }
        if (name.equals("&ssh_command_detail")) {
            String command = BridgeUtilities.getString(args, "");
            BeaconCommands commands = DataUtils.getSSHCommands(this.client.getData());
            return SleepUtils.getScalar(commands.getDetails(command));
        }
        switch (name) {
            case "&beacon_command_register": {
                String command = BridgeUtilities.getString(args, "");
                String desc = BridgeUtilities.getString(args, "");
                String details = BridgeUtilities.getString(args, "");
                BeaconCommands commands = DataUtils.getBeaconCommands(this.client.getData());
                commands.register(command, desc, details);
                break;
            }
            case "&ssh_command_register": {
                String command = BridgeUtilities.getString(args, "");
                String desc = BridgeUtilities.getString(args, "");
                String details = BridgeUtilities.getString(args, "");
                BeaconCommands commands = DataUtils.getSSHCommands(this.client.getData());
                commands.register(command, desc, details);
                break;
            }
            case "&beacon_note": {
                String[] bids = BeaconBridge.bids(args);
                String note = BridgeUtilities.getString(args, "");
                for (String bid : bids) {
                    this.conn.call("beacons.note", CommonUtils.args(bid, note));
                }
                break;
            }
            case "&beacon_remove": {
                String[] bids = BeaconBridge.bids(args);
                for (String bid : bids) {
                    this.conn.call("beacons.remove", CommonUtils.args(bid));
                }
                break;
            }
            default:
                if (name.equals("&beacons")) {
                    Map beacons = DataUtils.getBeacons(this.client.getData());
                    return CommonUtils.convertAll(new LinkedList(beacons.values()));
                }
                if (name.equals("&beacon_ids")) {
                    Map beacons = DataUtils.getBeacons(this.client.getData());
                    return CommonUtils.convertAll(new LinkedList(beacons.keySet()));
                }
                if (name.equals("&beacon_execute_job")) {
                    String[] bids = BeaconBridge.bids(args);
                    String cmd = BridgeUtilities.getString(args, "");
                    String argz = BridgeUtilities.getString(args, "");
                    int flags = BridgeUtilities.getInt(args, 0);
                    for (String bid : bids) {
                        EncodedCommandBuilder builder = new EncodedCommandBuilder(this.client);
                        builder.setCommand(78);
                        builder.addLengthAndEncodedString(bid, cmd);
                        builder.addLengthAndEncodedString(bid, argz);
                        builder.addShort(flags);
                        byte[] setuptask = builder.build();
                        this.conn.call("beacons.task", CommonUtils.args(bid, setuptask));
                    }
                } else {
                    if (name.equals("&beacon_host_imported_script")) {
                        String bid = BridgeUtilities.getString(args, "");
                        return SleepUtils.getScalar(new PowerShellTasks(this.client, bid).getImportCradle());
                    }
                    if (name.equals("&beacon_host_script")) {
                        String bid = BridgeUtilities.getString(args, "");
                        String data = BridgeUtilities.getString(args, "");
                        return SleepUtils.getScalar(new PowerShellTasks(this.client, bid).getScriptCradle(data));
                    }
                    if (name.equals("&beacon_info") || name.equals("&binfo")) {
                        String bid = BridgeUtilities.getString(args, "");
                        BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), bid);
                        if (entry == null) {
                            return SleepUtils.getEmptyScalar();
                        }
                        if (!args.isEmpty()) {
                            String key = BridgeUtilities.getString(args, "");
                            return CommonUtils.convertAll(entry.toMap().get(key));
                        }
                        return CommonUtils.convertAll(entry.toMap());
                    }
                    if (name.equals("&beacon_data") || name.equals("&bdata")) {
                        String bid = BridgeUtilities.getString(args, "");
                        String key = BridgeUtilities.getString(args, "");
                        BeaconEntry entry = DataUtils.getBeacon(this.client.getData(), bid);
                        if (entry == null) {
                            return SleepUtils.getEmptyScalar();
                        }
                        return CommonUtils.convertAll(entry.toMap());
                    }
                    switch (name) {
                        case "&bipconfig": {
                            String[] bids = BeaconBridge.bids(args);
                            final SleepClosure f = BridgeUtilities.getFunction(args, script);
                            for (final String bid : bids) {
                                this.conn.call("beacons.task_ipconfig", CommonUtils.args(bid), (call, result) -> {
                                    Stack<Scalar> args13 = new Stack<>();
                                    args13.push(CommonUtils.convertAll(result));
                                    args13.push(SleepUtils.getScalar(bid));
                                    SleepUtils.runCode(f, call, null, args13);
                                });
                            }
                            break;
                        }
                        case "&bls": {
                            String[] bids = BeaconBridge.bids(args);
                            final String folder = BridgeUtilities.getString(args, ".");
                            if (!args.isEmpty()) {
                                final SleepClosure f = BridgeUtilities.getFunction(args, script);
                                for (final String bid : bids) {
                                    this.conn.call("beacons.task_ls", CommonUtils.args(bid, folder), (call, result) -> {
                                        Stack<Scalar> args12 = new Stack<>();
                                        args12.push(CommonUtils.convertAll(result));
                                        args12.push(SleepUtils.getScalar(folder));
                                        args12.push(SleepUtils.getScalar(bid));
                                        SleepUtils.runCode(f, call, null, args12);
                                    });
                                }
                            } else {
                                TaskBeacon tasker = new TaskBeacon(this.client, this.client.getData(), this.conn, bids);
                                tasker.Ls(folder);
                            }
                            break;
                        }
                        case "&bps": {
                            String[] bids = BeaconBridge.bids(args);
                            if (args.isEmpty()) {
                                TaskBeacon tasker = new TaskBeacon(this.client, this.client.getData(), this.conn, bids);
                                tasker.Ps();
                            } else {
                                final SleepClosure f = BridgeUtilities.getFunction(args, script);
                                for (final String bid : bids) {
                                    this.conn.call("beacons.task_ps", CommonUtils.args(bid), (call, result) -> {
                                        Stack<Scalar> args1 = new Stack<>();
                                        args1.push(CommonUtils.convertAll(result));
                                        args1.push(SleepUtils.getScalar(bid));
                                        SleepUtils.runCode(f, call, null, args1);
                                    });
                                }
                            }
                            break;
                        }
                        case "&openOrActivate": {
                            String[] bids = BeaconBridge.bids(args);
                            if (bids.length == 1) {
                                DialogUtils.openOrActivate(this.client, bids[0]);
                            }
                            break;
                        }
                        case "&openBypassUACDialog": {
                            String[] bids = BeaconBridge.bids(args);
                            new BypassUACDialog(this.client, bids).show();
                            break;
                        }
                        case "&openElevateDialog": {
                            String[] bids = BeaconBridge.bids(args);
                            new ElevateDialog(this.client, bids).show();
                            break;
                        }
                    }
                }
                break;
        }
        return SleepUtils.getEmptyScalar();
    }

}

