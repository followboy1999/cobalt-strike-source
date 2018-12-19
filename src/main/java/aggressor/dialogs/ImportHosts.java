package aggressor.dialogs;

import aggressor.AggressorClient;
import common.CommonUtils;
import dialog.DialogUtils;
import importers.ImportHandler;
import importers.Importer;

import java.io.File;
import java.util.HashMap;

public class ImportHosts implements ImportHandler,
        Runnable {
    protected int hosts = 0;
    protected int services = 0;
    protected File target;
    protected AggressorClient client;

    public ImportHosts(AggressorClient client, File target) {
        this.target = target;
        this.client = client;
        new Thread(this, "import " + target).start();
    }

    @Override
    public void run() {
        for (Object o : Importer.importers(this)) {
            Importer next = (Importer) o;
            if (!next.process(this.target)) continue;
            this.finish();
            return;
        }
        DialogUtils.showError(this.target.getName() + " is not a recognized format");
    }

    public void finish() {
        if (this.hosts > 0) {
            this.client.getConnection().call("targets.push");
        }
        if (this.services > 0) {
            this.client.getConnection().call("services.push");
        }
        if (this.hosts == 1) {
            DialogUtils.showInfo("Imported " + this.hosts + " host");
        } else {
            DialogUtils.showInfo("Imported " + this.hosts + " hosts");
        }
    }

    @Override
    public void host(String address, String name, String os, double ver) {
        ++this.hosts;
        HashMap<String, String> options = new HashMap<>();
        options.put("address", address);
        if (name != null) {
            options.put("name", name);
        }
        if (os != null) {
            options.put("os", os);
            if (ver != 0.0) {
                options.put("version", ver + "");
            }
        }
        this.client.getConnection().call("targets.update", CommonUtils.args(CommonUtils.TargetKey(options), options));
    }

    @Override
    public void service(String address, String port, String description) {
        ++this.services;
        HashMap<String, String> options = new HashMap<>();
        options.put("address", address);
        options.put("port", port);
        if (description != null) {
            options.put("banner", description);
        }
        this.client.getConnection().call("services.update", CommonUtils.args(CommonUtils.ServiceKey(options), options));
    }
}

