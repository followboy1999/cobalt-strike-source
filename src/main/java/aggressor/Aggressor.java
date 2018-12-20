package aggressor;

import aggressor.dialogs.ConnectDialog;
import aggressor.ui.UseSynthetica;
import common.Authorization;
import common.License;
import common.Requirements;
import de.javasoft.plaf.synthetica.painter.SyntheticaPainter;
import sleep.parser.ParserConfig;

public class Aggressor {
    public static final String VERSION = "3.12 (20180906) " + (License.isTrial() ? "Trial" : "Licensed");
    public static MultiFrame frame = null;

    public static MultiFrame getFrame() {
        return frame;
    }

    public static void main(String[] args) {
        ParserConfig.installEscapeConstant('c', "\u0003");
        ParserConfig.installEscapeConstant('U', "\u001f");
        ParserConfig.installEscapeConstant('o', "\u000f");
        new UseSynthetica().setup();
        Requirements.checkGUI();
        License.checkLicenseGUI(new Authorization());
        frame = new MultiFrame();
        new ConnectDialog(frame).show();
    }
}

