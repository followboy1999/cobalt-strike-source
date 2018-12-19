package cortana.gui;

import cortana.core.EventManager;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;
import ui.DynamicMenu;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

public class MenuBridge implements Loadable,
        Function,
        Environment {
    protected Stack<JComponent> parents = new Stack<>();
    protected Map<String, LinkedList<SleepClosure>> menus = new HashMap<>();
    protected Stack<Stack> data = new Stack<>();
    protected ScriptableApplication application;
    protected MenuBuilder builder;

    public Stack getArguments() {
        return (Stack) this.data.peek();
    }

    public MenuBridge(ScriptableApplication application, MenuBuilder parent) {
        this.application = application;
        this.builder = parent;
    }

    public void push(JComponent menu, Stack arguments) {
        this.parents.push(menu);
        this.data.push(arguments);
    }

    public void pop() {
        this.parents.pop();
        this.data.pop();
    }

    public JComponent getTopLevel() {
        if (this.parents.isEmpty()) {
            throw new RuntimeException("menu has no parent");
        }
        return this.parents.peek();
    }

    @Override
    public void bindFunction(ScriptInstance si, String name, String desc, Block body) {
        SleepClosure f = new SleepClosure(si, body);
        switch (name) {
            case "menu":
                this.createMenu(desc, f);
                break;
            case "item":
                this.createItem(desc, f);
                break;
            case "popup":
                this.registerTopLevel(desc, f);
                break;
        }
    }

    public void registerTopLevel(String name, SleepClosure f) {
        if (!this.menus.containsKey(name)) {
            this.menus.put(name, new LinkedList<>());
        }
        LinkedList<SleepClosure> m = this.menus.get(name);
        m.add(f);
    }

    public void clearTopLevel(String name) {
        this.menus.remove(name);
    }

    public boolean isPopulated(String name) {
        return this.menus.containsKey(name) && (this.menus.get(name)).size() > 0;
    }

    public LinkedList<SleepClosure> getMenus(String name) {
        if (this.menus.containsKey(name)) {
            return this.menus.get(name);
        }
        return new LinkedList<>();
    }

    public void createMenu(String name, SleepClosure f) {
        JComponent top = this.getTopLevel();
        ScriptedMenu next = new ScriptedMenu(name, f, this);
        top.add(next);
    }

    public void createItem(String name, SleepClosure f) {
        JComponent top = this.getTopLevel();
        ScriptedMenuItem next = new ScriptedMenuItem(name, f, this);
        top.add(next);
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        switch (name) {
            case "&separator":
                if (this.getTopLevel() instanceof JMenu) {
                    ((JMenu) this.getTopLevel()).addSeparator();
                } else if (this.getTopLevel() instanceof JPopupMenu) {
                    ((JPopupMenu) this.getTopLevel()).addSeparator();
                }
                break;
            case "&show_menu": {
                String hook = BridgeUtilities.getString(args, "");
                if (args.size() > 0) {
                    this.builder.setupMenu(this.getTopLevel(), hook, EventManager.shallowCopy(args));
                } else {
                    this.builder.setupMenu(this.getTopLevel(), hook, this.getArguments());
                }
                break;
            }
            case "&show_popup": {
                MouseEvent ev = (MouseEvent) BridgeUtilities.getObject(args);
                String hook = BridgeUtilities.getString(args, "");
                JPopupMenu popup = new JPopupMenu();
                this.push(popup, EventManager.shallowCopy(args));
                this.builder.setupMenu(this.getTopLevel(), hook, this.getArguments());
                this.pop();
                popup.show((JComponent) ev.getSource(), ev.getX(), ev.getY());
                break;
            }
            case "&insert_menu": {
                String hook = BridgeUtilities.getString(args, "");
                this.push(this.getTopLevel(), EventManager.shallowCopy(args));
                this.builder.setupMenu(this.getTopLevel(), hook, this.getArguments());
                this.pop();
                break;
            }
            case "&menubar": {
                String _label = BridgeUtilities.getString(args, "");
                final String hook = BridgeUtilities.getString(args, "");
                int offset = BridgeUtilities.getInt(args, 2);
                DynamicMenu menu = new DynamicMenu("");
                if (_label.indexOf(38) > -1) {
                    menu.setText(_label.substring(0, _label.indexOf(38)) + _label.substring(_label.indexOf(38) + 1));
                    menu.setMnemonic(_label.charAt(_label.indexOf(38) + 1));
                } else {
                    menu.setText(_label);
                }
                menu.setHandler(parent -> {
                    MenuBridge.this.builder.setupMenu(parent, hook, new Stack());
                    if (!MenuBridge.this.isPopulated(hook)) {
                        MenuBridge.this.application.getJMenuBar().remove(parent);
                        MenuBridge.this.application.getJMenuBar().validate();
                    }
                });
                MenuElement[] menus = this.application.getJMenuBar().getSubElements();
                for (MenuElement menu1 : menus) {
                    JMenu temp = (JMenu) menu1.getComponent();
                    if (!temp.getText().equals(menu.getText())) continue;
                    this.application.getJMenuBar().remove(temp);
                }
                this.application.getJMenuBar().add(menu);
                this.application.getJMenuBar().validate();
                break;
            }
            case "&popup_clear": {
                String desc = BridgeUtilities.getString(args, "");
                this.clearTopLevel(desc);
                break;
            }
            default: {
                String desc = BridgeUtilities.getString(args, "");
                SleepClosure f = BridgeUtilities.getFunction(args, script);
                switch (name) {
                    case "&menu":
                        this.createMenu(desc, f);
                        break;
                    case "&item":
                        this.createItem(desc, f);
                        break;
                    case "&popup":
                        this.registerTopLevel(desc, f);
                        break;
                }
                break;
            }
        }
        return SleepUtils.getEmptyScalar();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void scriptLoaded(ScriptInstance si) {
        si.getScriptEnvironment().getEnvironment().put("popup", this);
        si.getScriptEnvironment().getEnvironment().put("&popup", this);
        si.getScriptEnvironment().getEnvironment().put("menu", this);
        si.getScriptEnvironment().getEnvironment().put("&menu", this);
        si.getScriptEnvironment().getEnvironment().put("item", this);
        si.getScriptEnvironment().getEnvironment().put("&item", this);
        si.getScriptEnvironment().getEnvironment().put("&separator", this);
        si.getScriptEnvironment().getEnvironment().put("&menubar", this);
        si.getScriptEnvironment().getEnvironment().put("&show_menu", this);
        si.getScriptEnvironment().getEnvironment().put("&insert_menu", this);
        si.getScriptEnvironment().getEnvironment().put("&show_popup", this);
        si.getScriptEnvironment().getEnvironment().put("&popup_clear", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

}

