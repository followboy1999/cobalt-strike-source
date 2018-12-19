package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class DraggableTabbedPane
        extends JTabbedPane {
    private boolean dragging = false;
    private Image tabImage = null;
    private Point currentMouseLocation = null;
    private int draggedTabIndex = 0;

    public DraggableTabbedPane() {
        this.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!DraggableTabbedPane.this.dragging) {
                    int tabNumber = DraggableTabbedPane.this.getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), e.getY());
                    if (tabNumber >= 0) {
                        DraggableTabbedPane.this.draggedTabIndex = tabNumber;
                        Rectangle bounds = DraggableTabbedPane.this.getUI().getTabBounds(DraggableTabbedPane.this, tabNumber);
                        BufferedImage totalImage = new BufferedImage(DraggableTabbedPane.this.getWidth(), DraggableTabbedPane.this.getHeight(), 2);
                        Graphics totalGraphics = ((Image) totalImage).getGraphics();
                        totalGraphics.setClip(bounds);
                        DraggableTabbedPane.this.setDoubleBuffered(false);
                        DraggableTabbedPane.this.paint(totalGraphics);
                        DraggableTabbedPane.this.tabImage = new BufferedImage(bounds.width, bounds.height, 2);
                        Graphics graphics = DraggableTabbedPane.this.tabImage.getGraphics();
                        graphics.drawImage(totalImage, 0, 0, bounds.width, bounds.height, bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height, DraggableTabbedPane.this);
                        DraggableTabbedPane.this.dragging = true;
                        DraggableTabbedPane.this.repaint();
                        graphics.dispose();
                        totalGraphics.dispose();
                    }
                } else {
                    DraggableTabbedPane.this.currentMouseLocation = e.getPoint();
                    DraggableTabbedPane.this.repaint();
                }
                super.mouseDragged(e);
            }
        });
        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (DraggableTabbedPane.this.dragging) {
                    int tabNumber = DraggableTabbedPane.this.getUI().tabForCoordinate(DraggableTabbedPane.this, e.getX(), 10);
                    if (e.getX() < 0) {
                        tabNumber = 0;
                    } else if (tabNumber == -1) {
                        tabNumber = DraggableTabbedPane.this.getTabCount() - 1;
                    }
                    if (tabNumber >= 0) {
                        Component comp = DraggableTabbedPane.this.getComponentAt(DraggableTabbedPane.this.draggedTabIndex);
                        Component title = DraggableTabbedPane.this.getTabComponentAt(DraggableTabbedPane.this.draggedTabIndex);
                        DraggableTabbedPane.this.removeTabAt(DraggableTabbedPane.this.draggedTabIndex);
                        DraggableTabbedPane.this.insertTab("", null, comp, null, tabNumber);
                        DraggableTabbedPane.this.setTabComponentAt(tabNumber, title);
                        DraggableTabbedPane.this.setSelectedIndex(tabNumber);
                    }
                }
                DraggableTabbedPane.this.dragging = false;
                DraggableTabbedPane.this.tabImage = null;
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.dragging && this.currentMouseLocation != null && this.tabImage != null) {
            g.drawImage(this.tabImage, this.currentMouseLocation.x, this.currentMouseLocation.y, this);
        }
    }

}

