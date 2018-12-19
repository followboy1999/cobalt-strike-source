package aggressor.bridges;

import aggressor.AggressorClient;
import common.AddressList;
import common.CommonUtils;
import common.PowerShellUtils;
import common.RangeList;
import cortana.Cortana;
import dialog.DialogUtils;
import encoders.Base64;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.io.File;
import java.util.Stack;

public class UtilityBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public UtilityBridge(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&url_open", this);
        Cortana.put(si, "&licenseKey", this);
        Cortana.put(si, "&format_size", this);
        Cortana.put(si, "&script_resource", this);
        Cortana.put(si, "&base64_encode", this);
        Cortana.put(si, "&base64_decode", this);
        Cortana.put(si, "&str_encode", this);
        Cortana.put(si, "&str_decode", this);
        Cortana.put(si, "&powershell_encode_stager", this);
        Cortana.put(si, "&powershell_encode_oneliner", this);
        Cortana.put(si, "&gzip", this);
        Cortana.put(si, "&gunzip", this);
        Cortana.put(si, "&add_to_clipboard", this);
        Cortana.put(si, "&range", this);
        Cortana.put(si, "&iprange", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&url_open")) {
            DialogUtils.gotoURL(BridgeUtilities.getString(args, "")).actionPerformed(null);
        } else {
            if (name.equals("&licenseKey")) {
                String license = CommonUtils.bString(CommonUtils.readFile(new File(System.getProperty("user.home"), ".cobaltstrike.license").getAbsolutePath())).trim();
                return SleepUtils.getScalar(license);
            }
            if (name.equals("&format_size")) {
                long size = BridgeUtilities.getLong(args, 0L);
                String units = "b";
                if (size > 1024L) {
                    size /= 1024L;
                    units = "kb";
                }
                if (size > 1024L) {
                    size /= 1024L;
                    units = "mb";
                }
                if (size > 1024L) {
                    size /= 1024L;
                    units = "gb";
                }
                return SleepUtils.getScalar(size + units);
            }
            if (name.equals("&script_resource")) {
                return SleepUtils.getScalar(new File(new File(script.getName()).getParent(), BridgeUtilities.getString(args, "")).getAbsolutePath());
            }
            if (name.equals("&base64_encode")) {
                byte[] arg = CommonUtils.toBytes(BridgeUtilities.getString(args, ""));
                return SleepUtils.getScalar(Base64.encode(arg));
            }
            if (name.equals("&base64_decode")) {
                String arg = BridgeUtilities.getString(args, "");
                return SleepUtils.getScalar(Base64.decode(arg));
            }
            if (name.equals("&powershell_encode_stager")) {
                byte[] arg = CommonUtils.toBytes(BridgeUtilities.getString(args, ""));
                return SleepUtils.getScalar(new PowerShellUtils(this.client).encodePowerShellCommand(arg));
            }
            if (name.equals("&powershell_encode_oneliner")) {
                String command = BridgeUtilities.getString(args, "");
                return SleepUtils.getScalar(CommonUtils.EncodePowerShellOneLiner(command));
            }
            if (name.equals("&gzip")) {
                byte[] arg = CommonUtils.toBytes(BridgeUtilities.getString(args, ""));
                return SleepUtils.getScalar(CommonUtils.gzip(arg));
            }
            if (name.equals("&gunzip")) {
                byte[] arg = CommonUtils.toBytes(BridgeUtilities.getString(args, ""));
                return SleepUtils.getScalar(CommonUtils.gunzip(arg));
            }
            if (name.equals("&add_to_clipboard")) {
                String arg = BridgeUtilities.getString(args, "");
                DialogUtils.addToClipboard(arg);
            } else {
                if (name.equals("&range")) {
                    String arg = BridgeUtilities.getString(args, "");
                    RangeList range = new RangeList(arg);
                    if (range.hasError()) {
                        throw new RuntimeException(range.getError());
                    }
                    return SleepUtils.getArrayWrapper(range.toList());
                }
                if (name.equals("&iprange")) {
                    String arg = BridgeUtilities.getString(args, "");
                    AddressList range = new AddressList(arg);
                    if (range.hasError()) {
                        throw new RuntimeException(range.getError());
                    }
                    return SleepUtils.getArrayWrapper(range.toList());
                }
                if (name.equals("&str_encode")) {
                    String text = BridgeUtilities.getString(args, "");
                    String chst = BridgeUtilities.getString(args, "");
                    return SleepUtils.getScalar(CommonUtils.toBytes(text, chst));
                }
                if (name.equals("&str_decode")) {
                    byte[] data = CommonUtils.toBytes(BridgeUtilities.getString(args, ""));
                    String chst = BridgeUtilities.getString(args, "");
                    return SleepUtils.getScalar(CommonUtils.bString(data, chst));
                }
            }
        }
        return SleepUtils.getEmptyScalar();
    }
}

