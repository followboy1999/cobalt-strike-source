package logger;

import common.BeaconEntry;
import common.CommonUtils;
import common.Loggable;
import common.MudgeSanity;
import server.Resources;
import server.ServerUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger
        extends ProcessBackend {
    protected Resources r;
    private static final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyMMdd");

    public Logger(Resources r) {
        this.r = r;
        this.start("logger");
    }

    protected File base(String subf) {
        Date d = new Date(System.currentTimeMillis());
        File temp = new File("logs");
        temp = CommonUtils.SafeFile(temp, fileDateFormat.format(d));
        if (subf != null) {
            temp = CommonUtils.SafeFile(temp, subf);
        }
        if (!temp.exists()) {
            temp.mkdirs();
        }
        return temp;
    }

    protected File beacon(String bid, String subf) {
        File parent = this.base(null);
        BeaconEntry entry = ServerUtils.getBeacon(this.r, bid);
        File temp = entry == null || "".equals(entry.getInternal()) ? CommonUtils.SafeFile(parent, "unknown") : CommonUtils.SafeFile(parent, entry.getInternal());
        if (subf != null) {
            temp = CommonUtils.SafeFile(temp, subf);
        }
        if (!temp.exists()) {
            temp.mkdirs();
        }
        return temp;
    }

    @Override
    public void process(Object _next) {
        Loggable next = (Loggable) _next;
        String bid = next.getBeaconId();
        File parent;
        parent = bid != null ? CommonUtils.SafeFile(this.beacon(bid, next.getLogFolder()), next.getLogFile()) : CommonUtils.SafeFile(this.base(next.getLogFolder()), next.getLogFile());
        try {
            FileOutputStream out = new FileOutputStream(parent, true);
            DataOutputStream data = new DataOutputStream(out);
            next.formatEvent(data);
            data.flush();
            data.close();
        } catch (IOException ioex) {
            MudgeSanity.logException("Writing to: " + parent, ioex, false);
        }
    }
}

