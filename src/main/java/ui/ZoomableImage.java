package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ZoomableImage
        extends JLabel {
    protected Icon original = null;
    protected double zoom = 1.0;

    private JMenuItem zoomMenu(String label, final double level) {
        JMenuItem i = new JMenuItem(label);
        i.addActionListener(ev -> {
            ZoomableImage.this.zoom = level;
            ZoomableImage.this.updateIcon();
        });
        return i;
    }

    public ZoomableImage() {
        final JPopupMenu menu = new JPopupMenu();
        menu.add(this.zoomMenu("25%", 0.25));
        menu.add(this.zoomMenu("50%", 0.5));
        menu.add(this.zoomMenu("75%", 0.75));
        menu.add(this.zoomMenu("100%", 1.0));
        menu.add(this.zoomMenu("125%", 1.25));
        menu.add(this.zoomMenu("150%", 1.5));
        menu.add(this.zoomMenu("200%", 2.0));
        menu.add(this.zoomMenu("250%", 2.5));
        this.addMouseListener(new MouseAdapter() {

            public void check(MouseEvent ev) {
                if (ev.isPopupTrigger()) {
                    menu.show((JComponent) ev.getSource(), ev.getX(), ev.getY());
                    ev.consume();
                }
            }

            @Override
            public void mouseClicked(MouseEvent ev) {
                this.check(ev);
            }

            @Override
            public void mousePressed(MouseEvent ev) {
                this.check(ev);
            }

            @Override
            public void mouseReleased(MouseEvent ev) {
                this.check(ev);
            }
        });
        this.setHorizontalAlignment(SwingConstants.CENTER);
    }

    protected void updateIcon() {
        super.setIcon(this.resizeImage((ImageIcon) this.original));
    }

    @Override
    public void setIcon(Icon image) {
        this.original = image;
        this.updateIcon();
    }

    protected Icon resizeImage(ImageIcon image) {
        if (this.zoom == 1.0 || image == null) {
            return image;
        }
        int width = image.getIconWidth();
        int height = image.getIconHeight();
        BufferedImage buffer = new BufferedImage(width, height, 2);
        Graphics2D g = buffer.createGraphics();
        g.drawImage(image.getImage(), 0, 0, width, height, null);
        g.dispose();
        return new ImageIcon(buffer.getScaledInstance((int) ((double) width * this.zoom), (int) ((double) height * this.zoom), 4));
    }

}

