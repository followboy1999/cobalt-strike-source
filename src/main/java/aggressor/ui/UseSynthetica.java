package aggressor.ui;

import aggressor.Prefs;
import de.javasoft.plaf.synthetica.SyntheticaLookAndFeel;

import javax.swing.*;
import java.awt.*;

public class UseSynthetica extends UseLookAndFeel {
    @Override
    public void setup() {
        try {
            SyntheticaLookAndFeel.setWindowsDecorated(false);
            SyntheticaLookAndFeel.setFont("Microsoft YaHei UI", 12);
            UseSynthetica.set("Synthetica.extendedFileChooser.rememberPreferences", false);
            UseSynthetica.set("Synthetica.font.enabled", true);
            UseSynthetica.set("Synthetica.text.antialias", true);
            UseSynthetica.set("Synthetica.font.respectSystemDPI", false);
            UIManager.put("Synthetica.tabbedPane.keepOpacity", true);
            UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaSimple2DLookAndFeel");
            // UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlueIceLookAndFeel");
            // Font temp = Prefs.getPreferences().getFont("client.font.font", "Tahoma-BOLD-11");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static {
        //  String[] li = new String[]{"Licensee=Strategic Cyber LLC", "LicenseRegistrationNumber=404478475", "Product=Synthetica", "LicenseType=Small Business License", "ExpireDate=--.--.----", "MaxVersion=2.30.999"};
        //  UIManager.put("Synthetica.license.info", li);
        //  UIManager.put("Synthetica.license.key", "D6363B2A-F83CD00A-C4EB6105-31B2770B");
        String[] li = new String[]{"Licensee=AppWork GmbH", "LicenseRegistrationNumber=112044", "Product=Synthetica", "LicenseType=Enterprise Site License", "ExpireDate=--.--.----", "MaxVersion=2.31.999"};
        UIManager.put("Synthetica.license.info", li);
        UIManager.put("Synthetica.license.key", "66B60C88-C3B8A9E1-D1C26574-C91385F5");
    }
}

