package graph;

import aggressor.Prefs;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxStackLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.handler.mxRubberband;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.view.mxInteractiveCanvas;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import common.CommonUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class NetworkGraph
        extends JComponent implements ActionListener {
    protected mxGraph graph;
    protected mxGraphComponent component;
    protected Object parent;
    protected boolean isAlive = true;
    protected String layout;
    protected Map nodeImages = new HashMap();
    protected GraphPopup popup = null;
    protected double zoom = 1.0;
    protected TouchMap nodes = new TouchMap();
    protected LinkedList edges = new LinkedList();
    protected Map tooltips = new HashMap();

    @Override
    public void actionPerformed(ActionEvent ev) {
        this.isAlive = false;
    }

    public boolean isAlive() {
        return this.isAlive;
    }

    public GraphPopup getGraphPopup() {
        return this.popup;
    }

    public void setGraphPopup(GraphPopup popup) {
        this.popup = popup;
    }

    public Image getScreenshot() {
        LinkedList<Object> cells = new LinkedList<>();
        for (Object node : this.nodes.values()) {
            cells.addAll(Arrays.asList(this.graph.getEdges(node)));
        }
        cells.addAll(this.nodes.values());
        return mxCellRenderer.createBufferedImage(this.graph, cells.toArray(), this.zoom, null, true, null, new NetworkGraphCanvas());
    }

    @Override
    public void setTransferHandler(TransferHandler h) {
        this.component.setTransferHandler(h);
    }

    public void clearSelection() {
        this.graph.clearSelection();
    }

    public void selectAll() {
        this.graph.selectAll();
    }

    public NetworkGraph() {
        mxConstants.VERTEX_SELECTION_COLOR = Prefs.getPreferences().getColor("graph.selection.color", "#00ff00");
        mxConstants.EDGE_SELECTION_COLOR = Prefs.getPreferences().getColor("graph.edge.color", "#3c6318");
        this.graph = new mxGraph() {

            @Override
            public String getToolTipForCell(Object cell) {
                if (NetworkGraph.this.tooltips.get(cell) == null) {
                    return "";
                }
                return NetworkGraph.this.tooltips.get(cell) + "";
            }
        };
        this.graph.setAutoOrigin(true);
        this.graph.setCellsEditable(false);
        this.graph.setCellsResizable(false);
        this.graph.setCellsBendable(false);
        this.graph.setAllowDanglingEdges(false);
        this.graph.setSplitEnabled(false);
        this.graph.setKeepEdgesInForeground(false);
        this.graph.setKeepEdgesInBackground(true);
        this.parent = this.graph.getDefaultParent();
        this.component = new NetworkGraphComponent(this.graph);
        this.component.setFoldingEnabled(true);
        this.component.setConnectable(false);
        this.component.setCenterPage(true);
        this.component.setToolTips(true);
        this.graph.setDropEnabled(true);
        new mxRubberband(this.component);
        this.addPopupListener();
        this.layout = Prefs.getPreferences().getString("graph.default_layout.layout", "none");
        this.component.getViewport().setOpaque(false);
        this.component.setOpaque(true);
        this.component.setBackground(Prefs.getPreferences().getColor("graph.background.color", "#111111"));
        this.setLayout(new BorderLayout());
        this.add(this.component, "Center");
        this.setupShortcuts();
    }

    public void addActionForKeyStroke(KeyStroke key, Action action) {
        this.component.getActionMap().put(key.toString(), action);
        this.component.getInputMap().put(key, key.toString());
    }

    public void addActionForKey(String key, Action action) {
        this.addActionForKeyStroke(KeyStroke.getKeyStroke(key), action);
    }

    public void addActionForKeySetting(String key, String dvalue, Action action) {
        KeyStroke temp = KeyStroke.getKeyStroke(dvalue);
        if (temp != null) {
            this.addActionForKeyStroke(temp, action);
        }
    }

    public void doStackLayout() {
        if (this.layout != null) {
            this.layout = "stack";
        }
        mxStackLayout layout = new mxStackLayout(this.graph, true, 25);
        layout.execute(this.parent);
    }

    public void doTreeLeftLayout() {
        if (this.layout != null) {
            this.layout = "tree-left";
        }
        mxHierarchicalLayout layout = new mxHierarchicalLayout(this.graph, 7);
        layout.execute(this.parent);
    }

    public void doTreeRightLayout() {
        if (this.layout != null) {
            this.layout = "tree-right";
        }
        mxHierarchicalLayout layout = new mxHierarchicalLayout(this.graph, 3);
        layout.execute(this.parent);
    }

    public void doTreeTopLayout() {
        if (this.layout != null) {
            this.layout = "tree-top";
        }
        mxHierarchicalLayout layout = new mxHierarchicalLayout(this.graph, 1);
        layout.execute(this.parent);
    }

    public void doTreeBottomLayout() {
        if (this.layout != null) {
            this.layout = "tree-bottom";
        }
        mxHierarchicalLayout layout = new mxHierarchicalLayout(this.graph, 5);
        layout.execute(this.parent);
    }

    public void doCircleLayout() {
        if (this.layout != null) {
            this.layout = "circle";
        }
        CircleLayout layout = new CircleLayout(this.graph, 1.0);
        layout.execute(this.parent);
    }

    public void doTreeLayout() {
        mxFastOrganicLayout layout = new mxFastOrganicLayout(this.graph);
        layout.execute(this.parent);
    }

    private void setupShortcuts() {
        this.addActionForKeySetting("graph.clear_selection.shortcut", "pressed ESCAPE", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                NetworkGraph.this.clearSelection();
            }
        });
        this.addActionForKeySetting("graph.select_all.shortcut", "ctrl pressed A", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                NetworkGraph.this.selectAll();
            }
        });
        this.addActionForKeySetting("graph.zoom_in.shortcut", "ctrl pressed EQUALS", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                NetworkGraph.this.zoom(0.1);
            }
        });
        this.addActionForKeySetting("graph.zoom_out.shortcut", "ctrl pressed MINUS", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                NetworkGraph.this.zoom(-0.1);
            }
        });
        this.addActionForKeySetting("graph.zoom_reset.shortcut", "ctrl pressed 0", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                NetworkGraph.this.resetZoom();
            }
        });
        this.addActionForKeySetting("graph.arrange_icons_stack.shortcut", "ctrl pressed S", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                NetworkGraph.this.doStackLayout();
            }
        });
        this.addActionForKeySetting("graph.arrange_icons_circle.shortcut", "ctrl pressed C", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                NetworkGraph.this.doCircleLayout();
            }
        });
        this.addActionForKeySetting("graph.arrange_icons_hierarchical.shortcut", "ctrl pressed H", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent ev) {
                NetworkGraph.this.doTreeLeftLayout();
            }
        });
    }

    public String getCellAt(Point p) {
        Point q = this.component.getViewport().getViewPosition();
        Point z = new Point((int) (p.getX() + q.getX()), (int) (p.getY() + q.getY()));
        mxCell cell = (mxCell) this.component.getCellAt((int) z.getX(), (int) z.getY());
        if (cell != null) {
            return cell.getId();
        }
        return null;
    }

    public String[] getSelectedHosts() {
        LinkedList<String> sel;
        Object[] cells = this.graph.getSelectionCells();
        sel = Arrays.stream(cells).map(cell1 -> (mxCell) cell1).filter(cell -> this.nodes.containsKey(cell.getId())).map(mxCell::getId).collect(Collectors.toCollection(LinkedList::new));
        String[] selected = new String[sel.size()];
        Iterator i = sel.iterator();
        int x = 0;
        while (i.hasNext()) {
            selected[x] = i.next() + "";
            ++x;
        }
        return selected;
    }

    private void addPopupListener() {
        this.component.getGraphControl().addMouseListener(new MouseAdapter() {

            public void handleEvent(MouseEvent ev) {
                if (ev.isPopupTrigger() && NetworkGraph.this.getGraphPopup() != null) {
                    NetworkGraph.this.getGraphPopup().showGraphPopup(NetworkGraph.this.getSelectedHosts(), ev);
                    ev.consume();
                } else if (ev.getClickCount() < 2 || !ev.isConsumed()) {
                    // empty if block
                }
            }

            @Override
            public void mousePressed(MouseEvent ev) {
                this.handleEvent(ev);
            }

            @Override
            public void mouseReleased(MouseEvent ev) {
                this.handleEvent(ev);
            }

            @Override
            public void mouseClicked(MouseEvent ev) {
                this.handleEvent(ev);
            }
        });
    }

    public void resetZoom() {
        this.zoom = 1.0;
        this.zoom(0.0);
    }

    public void zoom(double factor) {
        this.zoom += factor;
        this.component.zoomTo(this.zoom, true);
    }

    public void start() {
        this.graph.getModel().beginUpdate();
        this.nodes.startUpdates();
        for (Object edge : this.edges) {
            mxCell cell = (mxCell) edge;
            this.graph.getModel().remove(cell);
        }
        this.edges = new LinkedList();
    }

    public void setAutoLayout(String layout) {
        this.layout = layout;
        this.autoLayout();
    }

    public void autoLayout() {
        if (this.layout == null) {
            return;
        }
        if (this.layout.equals("circle")) {
            this.doCircleLayout();
        }
        if (this.layout.equals("stack")) {
            this.doStackLayout();
        }
        if (this.layout.equals("tree-left")) {
            this.doTreeLeftLayout();
        }
        if (this.layout.equals("tree-top")) {
            this.doTreeTopLayout();
        }
        if (this.layout.equals("tree-right")) {
            this.doTreeRightLayout();
        }
        if (this.layout.equals("tree-bottom")) {
            this.doTreeBottomLayout();
        }
    }

    public void end() {
        this.graph.getModel().endUpdate();
        CommonUtils.runSafe(() -> {
            NetworkGraph.this.autoLayout();
            NetworkGraph.this.graph.refresh();
        });
    }

    public void deleteNodes(String[] ids) {
        Object[] cells = Arrays.stream(ids).map(id -> this.nodes.remove(id)).toArray();
        this.graph.removeCells(cells, true);
    }

    public void deleteNodes() {
        List untouched = this.nodes.clearUntouched();
        Object[] cells = new Object[untouched.size()];
        Iterator i = untouched.iterator();
        int x = 0;
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            cells[x] = entry.getValue();
            ++x;
        }
        this.graph.removeCells(cells, true);
    }

    public void addEdge(String src, String dst, String color, String width, String stylez, String label) {
        mxCell start = (mxCell) this.nodes.get(src);
        mxCell dest = (mxCell) this.nodes.get(dst);
        mxCell edge = (mxCell) this.graph.insertEdge(this.parent, null, label, start, dest);
        StringBuilder style = new StringBuilder();
        style.append("fontColor=").append(Prefs.getPreferences().getString("graph.foreground.color", "#cccccc")).append(";");
        Font font = Prefs.getPreferences().getFont("graph.font.font", "Monospaced BOLD 14");
        style.append("fontSize=").append(font.getSize()).append(";");
        style.append("fontFamily=").append(font.getFamily()).append(";");
        style.append("fontStyle=").append(font.getStyle()).append(";");
        style.append("strokeColor=").append(color).append(";strokeWidth=").append(width).append(";dashed=").append(stylez);
        if (color.equals("#00FF00")) {
            style.append(";startArrow=classic;endArrow=none");
        }
        edge.setStyle(style.toString());
        this.edges.add(edge);
    }

    public Object addNode(String id, String label, String description, Image image, String tooltip) {
        mxCell cell;
        this.nodeImages.put(id, image);
        if (label.length() > 0) {
            description = description.length() > 0 ? description + "\n" + label : label;
        }
        if (!this.nodes.containsKey(id)) {
            cell = (mxCell) this.graph.insertVertex(this.parent, id, description, 0.0, 0.0, 125.0, 97.0);
            this.nodes.put(id, cell);
        } else {
            cell = (mxCell) this.nodes.get(id);
            cell.setValue(description);
        }
        this.nodes.touch(id);
        this.tooltips.put(cell, tooltip);
        StringBuilder style = new StringBuilder();
        style.append("shape=image;image=").append(id).append(";");
        style.append("fontColor=").append(Prefs.getPreferences().getString("graph.foreground.color", "#cccccc")).append(";");
        Font font = Prefs.getPreferences().getFont("graph.font.font", "Monospaced BOLD 14");
        style.append("fontSize=").append(font.getSize()).append(";");
        style.append("fontFamily=").append(font.getFamily()).append(";");
        style.append("fontStyle=").append(font.getStyle()).append(";");
        style.append("verticalLabelPosition=bottom;verticalAlign=top");
        cell.setStyle(style.toString());
        return cell;
    }

    @Override
    public boolean requestFocusInWindow() {
        return this.component.requestFocusInWindow();
    }

    private class NetworkGraphComponent
            extends mxGraphComponent {
        public NetworkGraphComponent(mxGraph graph) {
            super(graph);
            this.setBorder(BorderFactory.createEmptyBorder());
            this.getHorizontalScrollBar().setUnitIncrement(15);
            this.getHorizontalScrollBar().setBlockIncrement(60);
            this.getVerticalScrollBar().setUnitIncrement(15);
            this.getVerticalScrollBar().setBlockIncrement(60);
        }

        @Override
        public mxInteractiveCanvas createCanvas() {
            return new NetworkGraphCanvas();
        }
    }

    private class NetworkGraphCanvas
            extends mxInteractiveCanvas {
        private NetworkGraphCanvas() {
        }

        @Override
        public Image loadImage(String image) {
            if (NetworkGraph.this.nodeImages.containsKey(image)) {
                return (Image) NetworkGraph.this.nodeImages.get(image);
            }
            return super.loadImage(image);
        }
    }

}

