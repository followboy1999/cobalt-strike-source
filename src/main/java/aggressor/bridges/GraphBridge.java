package aggressor.bridges;

import aggressor.TabManager;
import cortana.Cortana;
import graph.NetworkGraph;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class GraphBridge implements Function,
        Loadable {
    protected TabManager manager;
    protected Cortana engine;
    protected static Map imageCache = new HashMap();

    public GraphBridge(Cortana e, TabManager m) {
        this.engine = e;
        this.manager = m;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&graph", this);
        Cortana.put(si, "&graph_start", this);
        Cortana.put(si, "&graph_end", this);
        Cortana.put(si, "&graph_add", this);
        Cortana.put(si, "&graph_connect", this);
        Cortana.put(si, "&image_overlay", this);
        Cortana.put(si, "&graph_zoom", this);
        Cortana.put(si, "&graph_zoom_reset", this);
        Cortana.put(si, "&graph_layout", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&graph")) {
            return SleepUtils.getScalar(new NetworkGraph());
        }
        switch (name) {
            case "&graph_start": {
                NetworkGraph graph = (NetworkGraph) BridgeUtilities.getObject(args);
                graph.start();
                break;
            }
            case "&graph_end": {
                NetworkGraph graph = (NetworkGraph) BridgeUtilities.getObject(args);
                graph.deleteNodes();
                graph.end();
                break;
            }
            case "&graph_add": {
                NetworkGraph graph = (NetworkGraph) BridgeUtilities.getObject(args);
                String id = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                String desc = BridgeUtilities.getString(args, "");
                Image img = (Image) BridgeUtilities.getObject(args);
                String tt = BridgeUtilities.getString(args, "");
                graph.addNode(id, label, desc, img, tt);
                break;
            }
            case "&graph_connect": {
                NetworkGraph graph = (NetworkGraph) BridgeUtilities.getObject(args);
                String a = BridgeUtilities.getString(args, "");
                String b = BridgeUtilities.getString(args, "");
                String color = BridgeUtilities.getString(args, "");
                String width = BridgeUtilities.getString(args, "");
                String style = BridgeUtilities.getString(args, "");
                String label = BridgeUtilities.getString(args, "");
                graph.addEdge(a, b, color, width, style, label);
                break;
            }
            default:
                if (name.equals("&image_overlay")) {
                    String key = BridgeUtilities.getString(args, "");
                    Map a = imageCache;
                    synchronized (a) {
                        if (imageCache.containsKey(key)) {
                            return SleepUtils.getScalar(imageCache.get(key));
                        }
                    }
                    BufferedImage buffered = new BufferedImage(1000, 776, 2);
                    Graphics2D graphics = buffered.createGraphics();
                    while (!args.isEmpty()) {
                        try {
                            String file = BridgeUtilities.getString(args, "");
                            FileInputStream i = new FileInputStream(file);
                            BufferedImage image = ImageIO.read(new FileInputStream(file));
                            ((InputStream) i).close();
                            graphics.drawImage(image, 0, 0, 1000, 776, null);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    graphics.dispose();
                    Map ex = imageCache;
                    synchronized (ex) {
                        imageCache.put(key, buffered);
                    }
                    return SleepUtils.getScalar(buffered);
                }
                switch (name) {
                    case "&graph_zoom_reset": {
                        NetworkGraph graph = (NetworkGraph) BridgeUtilities.getObject(args);
                        graph.resetZoom();
                        break;
                    }
                    case "&graph_zoom": {
                        NetworkGraph graph = (NetworkGraph) BridgeUtilities.getObject(args);
                        double zoomf = BridgeUtilities.getDouble(args);
                        graph.zoom(zoomf);
                        break;
                    }
                    case "&graph_layout": {
                        NetworkGraph graph = (NetworkGraph) BridgeUtilities.getObject(args);
                        String type = BridgeUtilities.getString(args, "");
                        graph.setAutoLayout(type);
                        break;
                    }
                }
                break;
        }
        return SleepUtils.getEmptyScalar();
    }
}

