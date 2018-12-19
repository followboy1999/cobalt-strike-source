package aggressor.dialogs;

import aggressor.AggressorClient;

import java.util.Stack;

public class JavaSmartAppletDialog
        extends JavaAppletDialog {
    public JavaSmartAppletDialog(AggressorClient client) {
        super(client);
    }

    @Override
    public String getResourceName() {
        String override = this.client.getScriptEngine().format("SMART_APPLET_RESOURCE", new Stack());
        if (override != null) {
            return override;
        }
        return "resources/applet_rhino.jar";
    }

    @Override
    public String getMainClass() {
        String override = this.client.getScriptEngine().format("SMART_APPLET_MAINCLASS", new Stack());
        if (override != null) {
            return override;
        }
        return "JavaApplet.class";
    }

    @Override
    public String getShortDescription() {
        return "smart applet";
    }

    @Override
    public String getTitle() {
        return "Smart Applet Attack";
    }

    @Override
    public String getURL() {
        return "https://www.cobaltstrike.com/help-java-smart-applet-attack";
    }

    @Override
    public String getDescription() {
        return "<html><body>The Smart Applet detects the Java version and uses an embedded exploit to disable the Java security sandbox. This attack is cross-platform and cross-browser.<p><b>Vulnerable Java Versions</b></p><ul><li>Java 1.6.0_45 and below</li><li>Java 1.7.0_21 and below</li></ul></body></html>";
    }

    @Override
    public String getDefaultURL() {
        return "/SiteLoader";
    }
}

