package dialog;

import javax.swing.*;
import java.awt.*;

public class SolidIcon implements Icon {
    private int width;
    private int height;
    private Color color;

    public SolidIcon(Color c, int w, int h) {
        this.width = w;
        this.height = h;
        this.color = c;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    @Override
    public int getIconWidth() {
        return this.width;
    }

    @Override
    public int getIconHeight() {
        return this.height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(this.color);
        g.fillRect(x, y, this.width - 1, this.height - 1);
    }
}

