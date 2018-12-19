package report;

import common.CommonUtils;
import common.RegexParser;
import cortana.Cortana;
import dialog.DialogUtils;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.engine.Block;
import sleep.interfaces.Environment;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import java.awt.image.BufferedImage;
import java.util.*;

public class ReportBridge implements Function, Loadable, Environment {
    protected HashMap<String, String> descriptions = new HashMap<>();
    protected LinkedHashMap<String, SleepClosure> reports = new LinkedHashMap<>();

    public LinkedList<String> reportTitles() {
        return (LinkedList<String>) this.reports.keySet();
    }

    public String describe(String title) {
        return this.descriptions.get(title);
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.putenv(si, "li", this);
        Cortana.putenv(si, "nobreak", this);
        Cortana.putenv(si, "output", this);
        Cortana.putenv(si, "page", this);
        Cortana.putenv(si, "report", this);
        Cortana.putenv(si, "ul", this);
        Cortana.put(si, "&block", this);
        Cortana.put(si, "&bookmark", this);
        Cortana.put(si, "&describe", this);
        Cortana.put(si, "&formatTime", this);
        Cortana.put(si, "&b", this);
        Cortana.put(si, "&br", this);
        Cortana.put(si, "&color", this);
        Cortana.put(si, "&color2", this);
        Cortana.put(si, "&h1", this);
        Cortana.put(si, "&h2", this);
        Cortana.put(si, "&h2_img", this);
        Cortana.put(si, "&h3", this);
        Cortana.put(si, "&h4", this);
        Cortana.put(si, "&host_image", this);
        Cortana.put(si, "&img", this);
        Cortana.put(si, "&landscape", this);
        Cortana.put(si, "&layout", this);
        Cortana.put(si, "&li", this);
        Cortana.put(si, "&link", this);
        Cortana.put(si, "&kvtable", this);
        Cortana.put(si, "&nobreak", this);
        Cortana.put(si, "&output", this);
        Cortana.put(si, "&p", this);
        Cortana.put(si, "&p_formatted", this);
        Cortana.put(si, "&text", this);
        Cortana.put(si, "&table", this);
        Cortana.put(si, "&ts", this);
        Cortana.put(si, "&ul", this);
        Cortana.put(si, "&list_unordered", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @SuppressWarnings("unchecked")
    public Document buildReport(String rtype, String title, Stack args) {
        Document d = new Document(title, 0);
        SleepClosure script = this.reports.get(rtype);
        script.getOwner().getMetadata().put("document", d);
        SleepUtils.runCode(script, rtype, null, args);
        script.getOwner().getMetadata().put("document", null);
        script.getOwner().getMetadata().put("document_stack", null);
        return d;
    }

    public Document getCurrentDocument(ScriptInstance si) {
        Document d = (Document) si.getMetadata().get("document");
        if (d == null) {
            throw new RuntimeException("this function must be run within the context of a report!");
        }
        return d;
    }

    @SuppressWarnings("unchecked")
    public Stack getContentStack(ScriptInstance si) {
        Stack temp = (Stack) si.getMetadata().get("document_stack");
        if (temp == null) {
            temp = new Stack();
            si.getMetadata().put("document_stack", temp);
        }
        return temp;
    }

    public Content getContent(ScriptInstance si) {
        return (Content) this.getContentStack(si).peek();
    }

    @SuppressWarnings("unchecked")
    protected void eval(Content parent, ScriptInstance si, Block functionBody) {
        this.getContentStack(si).push(parent);
        SleepUtils.runCode(si, functionBody);
        this.getContentStack(si).pop();
    }

    @SuppressWarnings("unchecked")
    protected void eval(Content parent, SleepClosure s) {
        this.getContentStack(s.getOwner()).push(parent);
        SleepUtils.runCode(s, "", null, new Stack());
        this.getContentStack(s.getOwner()).pop();
    }

    @Override
    public void bindFunction(ScriptInstance si, String type, String fname, Block functionBody) {
        if ("report".equals(type)) {
            this.reports.put(fname, new SleepClosure(si, functionBody));
        } else if ("page".equals(type)) {
            int typez;
            switch (fname) {
                case "rest":
                    typez = 1;
                    break;
                case "first":
                    typez = 0;
                    break;
                case "first-center":
                    typez = 2;
                    break;
                case "single":
                    typez = 3;
                    break;
                default:
                    throw new RuntimeException("invalid page type '" + fname + "'");
            }
            this.eval(this.getCurrentDocument(si).addPage(typez), si, functionBody);
        }
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if ("sajkld".equals(name)) {
            return SleepUtils.getEmptyScalar();
        }
        String text;
        switch (name) {
            case "&bookmark": {
                if (args.size() == 2) {
                    String section = BridgeUtilities.getString(args, "");
                    String child = BridgeUtilities.getString(args, "");
                    this.getCurrentDocument(script).getBookmarks().bookmark(section, child);
                } else {
                    String section = BridgeUtilities.getString(args, "");
                    this.getCurrentDocument(script).getBookmarks().bookmark(section);
                }
                break;
            }
            case "&br": {
                this.getContent(script).br();
                break;
            }
            case "&describe": {
                String title = BridgeUtilities.getString(args, "");
                text = BridgeUtilities.getString(args, "");
                this.descriptions.put(title, text);
                break;
            }
            case ("&formatTime"): {
                long when = BridgeUtilities.getLong(args);
                return SleepUtils.getScalar(CommonUtils.formatTime(when));
            }
            case "&h1": {
                text = BridgeUtilities.getString(args, "");
                String id = BridgeUtilities.getString(args, text);
                String align = BridgeUtilities.getString(args, "left");
                this.getContent(script).h1(text, id, align);
                break;
            }
            case "&h2": {
                text = BridgeUtilities.getString(args, "");
                String id = BridgeUtilities.getString(args, text);
                this.getContent(script).h2(text, id);
                break;
            }
            case "&h2_img": {
                BufferedImage img = (BufferedImage) BridgeUtilities.getObject(args);
                text = BridgeUtilities.getString(args, "");
                String id = BridgeUtilities.getString(args, text);
                this.getContent(script).h2_img(img, text, id);
                break;
            }
            case "&h3": {
                text = BridgeUtilities.getString(args, "");
                this.getContent(script).h3(text);
                break;
            }
            case "&h4": {
                text = BridgeUtilities.getString(args, "");
                this.getContent(script).h4(text, "left");
                break;
            }
            case "&b": {
                text = BridgeUtilities.getString(args, "");
                this.getContent(script).b(text);
                break;
            }
            case "&color": {
                text = BridgeUtilities.getString(args, "");
                String color = BridgeUtilities.getString(args, "");
                this.getContent(script).color(text, color);
                break;
            }
            case "&color2": {
                text = BridgeUtilities.getString(args, "");
                String color = BridgeUtilities.getString(args, "");
                String bgcolor = BridgeUtilities.getString(args, "");
                this.getContent(script).color2(text, color, bgcolor);
                break;
            }
            case "&host_image": {
                String os = BridgeUtilities.getString(args, "");
                double ver = BridgeUtilities.getDouble(args, 0.0);
                boolean owned = !SleepUtils.isEmptyScalar(BridgeUtilities.getScalar(args));
                return SleepUtils.getScalar(DialogUtils.TargetVisualizationMedium(os, ver, owned, false));
            }
            case "&img": {

                text = BridgeUtilities.getString(args, "");
                String width = BridgeUtilities.getString(args, "");
                this.getContent(script).img(text, width);
                break;
            }
            case "&kvtable": {
                Scalar next = (Scalar) args.pop();
                LinkedHashMap<String, String> temp = new LinkedHashMap<>();
                for (Object o : next.getHash().getData().entrySet()) {
                    Map.Entry entry = (Map.Entry) o;
                    temp.put(entry.getKey().toString(), entry.getValue() != null ? entry.getValue().toString() : "");
                }
                this.getContent(script).kvtable(temp);
                break;
            }
            case "&landscape": {
                this.getCurrentDocument(script).setOrientation(1);
                break;
            }
            case "&li": {
                SleepClosure s = BridgeUtilities.getFunction(args, script);
                this.eval(this.getContent(script).li(), s);
                break;
            }
            case "&nobreak": {
                SleepClosure s = BridgeUtilities.getFunction(args, script);
                this.eval(this.getContent(script).nobreak(), s);
                break;
            }
            case "&output": {
                SleepClosure s = BridgeUtilities.getFunction(args, script);
                this.eval(this.getContent(script).output("800"), s);
                break;
            }
            case "&block": {
                SleepClosure s = BridgeUtilities.getFunction(args, script);
                String align = BridgeUtilities.getString(args, "left");
                this.eval(this.getContent(script).block(align), s);
                break;
            }
            case "&p": {
                text = BridgeUtilities.getString(args, "");
                String align = BridgeUtilities.getString(args, "left");
                this.getContent(script).p(text, align);
                break;
            }
            case "&p_formatted": {
                text = BridgeUtilities.getString(args, "");
                text = CommonUtils.strrep(text, "\n\n*", "\n*");
                List lines = CommonUtils.toList(text.split("\n"));
                LinkedList<String> items = new LinkedList<>();
                Iterator i = lines.iterator();
                while (i.hasNext()) {
                    RegexParser parser;
                    String temp = (String) i.next();
                    if (!(temp = temp.trim()).equals("") && temp.charAt(0) == '*' && temp.length() > 1) {
                        items.add(temp.substring(1));
                        continue;
                    }
                    if (items.size() > 0) {
                        this.getContent(script).list_formatted(items);
                        items = new LinkedList<>();
                        if ("".equals(temp)) continue;
                    }
                    if ((parser = new RegexParser(temp)).matches("===(.*?)===")) {
                        this.getContent(script).h4(parser.group(1), "left");
                        if (!i.hasNext() || "".equals(temp = (String) i.next())) continue;
                        this.getContent(script).p(temp, "left");
                        continue;
                    }
                    if ("".equals(temp)) {
                        this.getContent(script).br();
                        continue;
                    }
                    RegexParser parserz = new RegexParser(temp.trim());
                    if (parserz.matches("'''(.*?)'''(.*?)")) {
                        Content item = this.getContent(script).block("left");
                        item.b(parserz.group(1));
                        item.text(parserz.group(2));
                        continue;
                    }
                    this.getContent(script).p(temp, "left");
                }
                if (items.size() > 0) {
                    this.getContent(script).list(items);
                }
                break;
            }
            case "&text":
                text = BridgeUtilities.getString(args, "");
                this.getContent(script).text(text);
                break;
            case "&table":
            case "&layout":
                List cols = SleepUtils.getListFromArray((Scalar) args.pop());
                List widths = SleepUtils.getListFromArray((Scalar) args.pop());
                List rows = SleepUtils.getListFromArray((Scalar) args.pop());
                for (Object row : rows) {
                    for (Object o : ((Map) row).entrySet()) {
                        Map.Entry entry = (Map.Entry) o;
                        if (entry.getValue() instanceof SleepClosure) {
                            SleepClosure s = (SleepClosure) entry.getValue();
                            Content c = this.getContent(script).string();
                            this.eval(c, s);
                            StringBuilder buffer = new StringBuilder();
                            c.publish(buffer);
                            entry.setValue(buffer.toString());
                        } else {
                            entry.setValue(Content.fixText(entry.getValue() != null ? entry.getValue().toString() : ""));
                        }
                    }
                }
                if ("&table".equals(name)) {
                    this.getContent(script).table(cols, widths, rows);
                } else {
                    this.getContent(script).layout(cols, widths, rows);
                }
                break;
            case "&ts":
                text = BridgeUtilities.getString(args, "");
                this.getContent(script).ts();
                break;
            case "&ul": {
                SleepClosure s = BridgeUtilities.getFunction(args, script);
                this.eval(this.getContent(script).ul(), s);
                break;
            }
            case "&list_unordered": {
                List items = SleepUtils.getListFromArray((Scalar) args.pop());
                this.getContent(script).list(items);
                break;
            }
            case "&link":
                String desc = BridgeUtilities.getString(args, "");
                String url = BridgeUtilities.getString(args, "");
                this.getContent(script).link_bullet(desc, url);
                break;
            default:
        }
        return SleepUtils.getEmptyScalar();
    }
}

