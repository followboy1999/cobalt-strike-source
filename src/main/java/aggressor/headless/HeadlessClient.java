package aggressor.headless;

import aggressor.AggressorClient;
import aggressor.MultiFrame;
import common.CommonUtils;
import common.MudgeSanity;
import common.TeamQueue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HeadlessClient
        extends AggressorClient implements Runnable {
    protected String scriptf;

    @Override
    public void disconnected() {
        CommonUtils.print_error("Disconnected from team server.");
        System.exit(0);
    }

    @Override
    public void result(String key, Object o) {
        if ("server_error".equals(key)) {
            CommonUtils.print_error("Server error: " + o);
        }
    }

    @Override
    public void loadScripts() {
        if (this.scriptf == null) {
            try {
                this.engine.loadScript("scripts/console.cna", CommonUtils.resource("scripts/console.cna"));
            } catch (Exception ex) {
                MudgeSanity.logException("Loading scripts/console.cna", ex, false);
            }
            new Thread(this, "Aggressor Script Console").start();
        } else {
            try {
                this.engine.loadScript(this.scriptf);
            } catch (Exception ex) {
                MudgeSanity.logException("Loading " + this.scriptf, ex, true);
                System.exit(0);
            }
        }
    }

    @Override
    public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            while (true) {
                try {
                    System.out.print("\u001b[4maggressor\u001b[0m> ");
                    String entry = in.readLine();
                    if (entry != null && !"".equals(entry)) {
                        this.engine.getConsoleInterface().processCommand(entry);
                    }
                } catch (IOException var3) {
                }
            }
        }
    }

    public HeadlessClient(MultiFrame window, TeamQueue conn, Map metadata, String scriptf) {
        this.scriptf = scriptf;
        this.setup(window, conn, metadata, new HashMap());
    }

    @Override
    public boolean isHeadless() {
        return true;
    }
}

