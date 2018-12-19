package report;

import aggressor.AggressorClient;
import aggressor.Prefs;
import aggressor.bridges.AggregateBridge;
import aggressor.bridges.AttackBridge;
import common.CommonUtils;
import common.MudgeSanity;
import sleep.error.YourCodeSucksException;
import sleep.runtime.ScriptInstance;
import sleep.runtime.ScriptLoader;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

public class ReportEngine {
    protected AggressorClient client;
    protected ReportBridge bridge;
    protected LinkedList<String> reportassets = new LinkedList<>();

    public ReportEngine(AggressorClient client) {
        this.client = client;
        this.bridge = new ReportBridge();
    }

    public List reportTitles() {
        this.rehash();
        List titles = this.bridge.reportTitles();
        Collections.sort(titles);
        return titles;
    }

    public String describe(String title) {
        return this.bridge.describe(title);
    }

    public Document buildReport(String rtype, String title, Stack args) {
        return this.bridge.buildReport(rtype, title, args);
    }

    public void load(String reportFile, InputStream instream) {
        Hashtable environment = new Hashtable();
        ScriptLoader loader = new ScriptLoader();
        try {
            loader.addGlobalBridge(this.bridge);
            loader.addGlobalBridge(this.client.getScriptEngine());
            loader.addGlobalBridge(new AggregateBridge(this.client));
            loader.addGlobalBridge(new AttackBridge());
            ScriptInstance scripti = loader.loadScript(reportFile, instream);
            scripti.addWarningWatcher(this.client.getScriptEngine());
            scripti.runScript();
        } catch (YourCodeSucksException yex) {
            CommonUtils.print_error("Could not load: " + reportFile + " (syntax errors; go to View -> Script Console)");
            this.client.getScriptEngine().perror("Could not load " + reportFile + ":\n" + yex.formatErrors());
        } catch (Exception ex) {
            this.client.getScriptEngine().perror("Could not load " + reportFile + ": " + ex.getMessage());
            MudgeSanity.logException("Could not load:" + reportFile, ex, false);
        }
    }

    public void registerInternal(String reportFile) {
        this.reportassets.add(reportFile);
        this.rehash();
    }

    public void rehash() {
        this.bridge = new ReportBridge();
        Iterator i = this.reportassets.iterator();
        while (i.hasNext()) {
            String next = (String) i.next();
            try {
                this.load(next, CommonUtils.resource(next));
            } catch (Exception ex) {
                MudgeSanity.logException("asset: " + next, ex, false);
            }
        }
        i = Prefs.getPreferences().getList("reporting.custom_reports").iterator();
        while (i.hasNext()) {
            String next = (String) i.next();
            try {
                this.load(next, new FileInputStream(next));
            } catch (Exception ex) {
                MudgeSanity.logException("file: " + next, ex, false);
            }
        }
    }
}

