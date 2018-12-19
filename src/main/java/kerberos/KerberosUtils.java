package kerberos;

import common.CommonUtils;
import common.MudgeSanity;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.ScriptLoader;
import sleep.runtime.SleepUtils;

import java.util.Hashtable;
import java.util.Stack;

public class KerberosUtils {
    private static ScriptInstance converter = null;

    public static byte[] ConvertCCacheToKrbCred(String name) {
        Class<KerberosUtils> class_ = KerberosUtils.class;
        synchronized (KerberosUtils.class) {
            if (converter == null) {
                try {
                    ScriptLoader loader = new ScriptLoader();
                    converter = loader.loadScript("ccache_krbcred.sl", CommonUtils.resource("resources/ccache_krbcred.sl"), new Hashtable());
                    converter.runScript();
                } catch (Exception ex) {
                    MudgeSanity.logException("compile converter", ex, false);
                    // ** MonitorExit[var1_1] (shouldn't be in output)
                    return new byte[0];
                }
            }
            Stack<Scalar> args = new Stack<>();
            args.push(SleepUtils.getScalar(name));
            Scalar result = converter.callFunction("&convert", args);
            // ** MonitorExit[var1_1] (shouldn't be in output)
            return CommonUtils.toBytes(result.toString());
        }
    }
}

