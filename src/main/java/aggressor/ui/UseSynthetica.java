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
            UseSynthetica.set("Synthetica.extendedFileChooser.rememberPreferences", false);
            UseSynthetica.set("Synthetica.font.enabled", true);
            UseSynthetica.set("Synthetica.text.antialias", false);
            UseSynthetica.set("Synthetica.textArea.border.opaqueBackground", false);
            UIManager.put("Synthetica.font.scaleFactor", 115);
            UseSynthetica.set("Synthetica.font.respectSystemDPI", false);
            UIManager.setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlueIceLookAndFeel");
            Font temp = Prefs.getPreferences().getFont("client.font.font", "Tahoma-BOLD-11");
            SyntheticaLookAndFeel.setFont(temp, true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static {
        String[] li = new String[]{"Licensee=Strategic Cyber LLC", "LicenseRegistrationNumber=404478475", "Product=Synthetica", "LicenseType=Small Business License", "ExpireDate=--.--.----", "MaxVersion=2.30.999"};
        UIManager.put("Synthetica.license.info", li);
        UIManager.put("Synthetica.license.key", "D6363B2A-F83CD00A-C4EB6105-31B2770B");
    }
}

