import java.awt.*;

public class local_fonts {
    public static void main(String[] args) {
        GraphicsEnvironment grapEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNameList = grapEnv.getAvailableFontFamilyNames();
        for (String fontName : fontNameList) {
            System.out.println(fontName);
        }
    }
}
