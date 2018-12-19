package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.dialogs.ExportDataDialog;
import aggressor.dialogs.ExportReportDialog;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.util.Stack;

public class ReportingBridge implements Function,
        Loadable {
    protected AggressorClient client;

    public ReportingBridge(AggressorClient c) {
        this.client = c;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&reports", this);
        Cortana.put(si, "&reportDescription", this);
        Cortana.put(si, "&openReportDialog", this);
        Cortana.put(si, "&openExportDataDialog", this);
        Cortana.put(si, "&rehash_reports", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if ("&reports".equals(name)) {
            return SleepUtils.getArrayWrapper(this.client.getReportEngine().reportTitles());
        }
        if ("&reportDescription".equals(name)) {
            String title = BridgeUtilities.getString(args, "");
            return SleepUtils.getScalar(this.client.getReportEngine().describe(title));
        }
        if ("&openReportDialog".equals(name)) {
            String title = BridgeUtilities.getString(args, "");
            ExportReportDialog dialog = new ExportReportDialog(this.client, title);
            dialog.show();
        } else if ("&openExportDataDialog".equals(name)) {
            ExportDataDialog dialog = new ExportDataDialog(this.client);
            dialog.show();
        } else if ("&rehash_reports".equals(name)) {
            this.client.getReportEngine().rehash();
            return SleepUtils.getScalar("done");
        }
        return SleepUtils.getEmptyScalar();
    }
}

