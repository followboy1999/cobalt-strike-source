package console;

import common.CommonUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

public abstract class GenericTabCompletion {
    protected final Console window;
    protected String last = null;
    protected Iterator tabs = null;

    public Console getWindow() {
        return this.window;
    }

    public GenericTabCompletion(Console windowz) {
        this.window = windowz;
        this.window.addActionForKey("pressed TAB", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                GenericTabCompletion.this.tabComplete(ev);
            }
        });
    }

    public abstract Collection getOptions(String var1);

    public String transformText(String text) {
        return text;
    }

    private void tabCompleteFirst(String text) {
        try {
            String option;
            text = this.transformText(text);
            LinkedHashSet<String> responses = new LinkedHashSet<>();
            Collection options = this.getOptions(text);
            if (options == null) {
                return;
            }
            for (Object option1 : options) {
                String end;
                Object begin;
                option = option1 + "";
                if (text.length() > option.length()) {
                    begin = option;
                    end = "";
                } else {
                    begin = option.substring(0, text.length());
                    end = option.substring(text.length());
                }
                int nextSlash = end.indexOf(47);
                if (nextSlash > -1 && nextSlash + 1 < end.length()) {
                    end = end.substring(0, nextSlash);
                }
                responses.add(begin + end);
            }
            responses.add(text);
            synchronized (this.window) {
                this.tabs = responses.iterator();
                this.last = (String) this.tabs.next();
            }
            CommonUtils.runSafe(() -> GenericTabCompletion.this.window.getInput().setText(GenericTabCompletion.this.last));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void tabComplete(ActionEvent ev) {
        final String text = this.window.getInput().getText();
        if (text.length() == 0) {
            return;
        }
        synchronized (this.window) {
            if (this.tabs != null && this.tabs.hasNext() && text.equals(this.last)) {
                this.last = (String) this.tabs.next();
                this.window.getInput().setText(this.last);
                return;
            }
            new Thread(() -> GenericTabCompletion.this.tabCompleteFirst(text)).start();
        }
    }

}

