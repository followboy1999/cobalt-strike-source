package report;

import common.CommonUtils;
import common.MudgeSanity;
import sleep.error.RuntimeWarningWatcher;
import sleep.error.ScriptWarning;
import sleep.error.YourCodeSucksException;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.ScriptLoader;
import sleep.runtime.SleepUtils;

import java.io.File;
import java.io.InputStream;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Stack;

public class ReportTest implements RuntimeWarningWatcher {
    protected ReportBridge bridge = new ReportBridge();

    @Override
    public void processScriptWarning(ScriptWarning warning) {
        String from = warning.getNameShort() + ":" + warning.getLineNumber();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        Date adate = new Date();
        String niced = format.format(adate, new StringBuffer(), new FieldPosition(0)).toString();
        if (warning.isDebugTrace()) {
            CommonUtils.print_info("[" + niced + "] Trace: " + warning.getMessage() + " at " + from);
        } else {
            CommonUtils.print_info("[" + niced + "] " + warning.getMessage() + " at " + from);
        }
    }

    public Document buildReport(String title, Stack args) {
        return this.bridge.buildReport(title, title, args);
    }

    public void load(String reportFile, InputStream instream) {
        Hashtable environment = new Hashtable();
        ScriptLoader loader = new ScriptLoader();
        try {
            loader.addGlobalBridge(this.bridge);
            ScriptInstance scripti = loader.loadScript(reportFile, instream);
            scripti.addWarningWatcher(this);
            scripti.runScript();
        } catch (YourCodeSucksException yex) {
            CommonUtils.print_error("Could not load: " + reportFile + "\n" + yex.formatErrors());
        } catch (Exception ex) {
            MudgeSanity.logException("Could not load:" + reportFile, ex, false);
        }
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            CommonUtils.print_warn("ReportTest [file.rpt] [title] [/path/to/out.pdf] [args...]");
            return;
        }
        try {
            ReportTest report = new ReportTest();
            report.load(args[0], CommonUtils.resource(args[0]));
            Stack<Scalar> argz = new Stack<>();
            for (int x = 3; x < args.length; ++x) {
                argz.add(0, SleepUtils.getScalar(args[x]));
            }
            Document d = report.buildReport(args[1], argz);
            d.toPDF(new File(args[2]));
        } catch (Exception ex) {
            MudgeSanity.logException("Error", ex, false);
        }
    }
}

