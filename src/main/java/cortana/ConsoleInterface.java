package cortana;

import cortana.core.CommandManager;
import sleep.error.YourCodeSucksException;
import sleep.interfaces.Loadable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ConsoleInterface {
    protected Cortana engine;
    protected CommandManager commands;

    public ConsoleInterface(Cortana engine) {
        this.engine = engine;
        this.commands = new CommandManager();
    }

    public Loadable getBridge() {
        return this.commands.getBridge();
    }

    public List commandList(String filter) {
        String[] data = filter.trim().split("\\s+");
        if ("reload".equals(data[0]) || "pron".equals(data[0]) || "profile".equals(data[0]) || "proff".equals(data[0]) || "tron".equals(data[0]) || "unload".equals(data[0]) || "troff".equals(data[0])) {
            LinkedList<String> res = this.engine.scripts.keySet().stream().map(name -> data[0] + " " + new File(name).getName()).collect(Collectors.toCollection(LinkedList::new));
            Cortana.filterList(res, filter);
            Collections.sort(res);
            return res;
        }
        if ("load".equals(data[0]) && filter.length() > 5) {
            String file = filter.substring(5);
            File temp = new File(file);
            if (!temp.exists() || !temp.isDirectory()) {
                temp = temp.getParentFile();
            }
            LinkedList<String> res = new LinkedList<>();
            if (temp == null) {
                res.add(filter);
                return res;
            }
            File[] s = temp.listFiles();
            for (int x = 0; s != null && x < s.length; ++x) {
                if (!s[x].isDirectory() && !s[x].getName().endsWith(".cna")) continue;
                res.add(data[0] + " " + s[x].getAbsolutePath());
            }
            Cortana.filterList(res, filter);
            Collections.sort(res);
            return res;
        }
        LinkedList<String> cmdl = new LinkedList<>(this.commands.commandList(filter));
        cmdl.add("help");
        cmdl.add("ls");
        cmdl.add("reload");
        cmdl.add("unload");
        cmdl.add("load");
        cmdl.add("pron");
        cmdl.add("proff");
        cmdl.add("profile");
        cmdl.add("tron");
        cmdl.add("troff");
        Collections.sort(cmdl);
        Cortana.filterList(cmdl, filter);
        return cmdl;
    }

    public void processCommand(final String text) {
        final String[] data = text.trim().split("\\s+");
        HashSet<String> states = new HashSet<>();
        states.add("tron");
        states.add("troff");
        states.add("profile");
        states.add("pron");
        states.add("proff");
        HashSet<String> cmds = new HashSet<>(states);
        cmds.add("unload");
        cmds.add("load");
        cmds.add("reload");
        if ("ls".equals(text)) {
            this.engine.p("");
            this.engine.p("Scripts");
            this.engine.pdark("-------");
            for (String temp : this.engine.scripts.keySet()) {
                if (temp == null) continue;
                File script = new File(temp);
                this.engine.p(script.getName());
            }
            this.engine.p("");
        } else if (cmds.contains(data[0]) && data.length != 2) {
            this.engine.perror("Missing arguments");
        } else if (states.contains(data[0]) && data.length == 2) {
            String script = this.engine.findScript(data[1]);
            if (script == null) {
                this.engine.perror("Could not find '" + data[1] + "'");
            } else {
                Loader loader = (Loader) this.engine.scripts.get(script);
                if ("tron".equals(data[0])) {
                    this.engine.pgood("Tracing '" + data[1] + "'");
                    loader.setDebugLevel(8);
                } else if ("troff".equals(data[0])) {
                    this.engine.pgood("Stopped trace of '" + data[1] + "'");
                    loader.unsetDebugLevel(8);
                } else if ("pron".equals(data[0])) {
                    this.engine.pgood("Profiling '" + data[1] + "'");
                    loader.setDebugLevel(24);
                } else if ("profile".equals(data[0]) || "proff".equals(data[0])) {
                    if ("proff".equals(data[0])) {
                        this.engine.pgood("Stopped profile of '" + data[1] + "'");
                        loader.unsetDebugLevel(24);
                    }
                    this.engine.p("");
                    this.engine.p("Profile " + data[1]);
                    this.engine.pdark("-------");
                    loader.printProfile(this.engine.cortana_io.getOutputStream());
                    this.engine.p("");
                }
            }
        } else if ("unload".equals(data[0]) && data.length == 2) {
            String script = this.engine.findScript(data[1]);
            if (script == null) {
                this.engine.perror("Could not find '" + data[1] + "'");
            } else {
                this.engine.pgood("Unload " + script);
                this.engine.unloadScript(script);
            }
        } else if ("load".equals(data[0]) && data.length == 2) {
            this.engine.pgood("Load " + data[1]);
            try {
                this.engine.loadScript(data[1]);
            } catch (YourCodeSucksException yex) {
                this.engine.p(yex.formatErrors());
            } catch (Exception ex) {
                this.engine.perror("Could not load: " + ex.getMessage());
            }
        } else if ("reload".equals(data[0]) && data.length == 2) {
            String script = this.engine.findScript(data[1]);
            if (script == null) {
                this.engine.perror("Could not find '" + data[1] + "'");
            } else {
                this.engine.pgood("Reload " + script);
                try {
                    this.engine.unloadScript(script);
                    this.engine.loadScript(script);
                } catch (IOException ioex) {
                    this.engine.perror("Could not load: '" + data[1] + "' " + ioex.getMessage());
                } catch (YourCodeSucksException yex) {
                    this.engine.p(yex.formatErrors());
                }
            }
        } else if ("help".equals(text)) {
            this.engine.p("");
            this.engine.p("Commands");
            this.engine.pdark("--------");
            for (Object o : this.commandList("")) {
                this.engine.p(o + "");
            }
            this.engine.p("");
        } else if (this.engine.getScriptableApplication().isHeadless()) {
            if (!this.commands.fireCommand(data[0], text)) {
                this.engine.perror("Command not found");
            }
        } else {
            new Thread(() -> {
                if (!ConsoleInterface.this.commands.fireCommand(data[0], text)) {
                    ConsoleInterface.this.engine.perror("Command not found");
                }
            }, "cortana command: " + data[0]).start();
        }
    }

}

