package graph;

import common.CommonUtils;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RefreshGraph implements Runnable {
    protected List nodes = new LinkedList();
    protected List highlights = new LinkedList();
    protected List<Route> routes = new LinkedList();
    protected Refreshable graph;

    public RefreshGraph(Refreshable graph) {
        this.graph = graph;
    }

    public void go() {
        CommonUtils.runSafe(this);
    }

    public void addRoute(Route route) {
        this.routes.add(route);
    }

    public void addNode(String id, String label, String description, Image iconz, String tooltip) {
        Node n = new Node();
        n.id = id;
        n.label = label;
        n.description = description;
        n.iconz = iconz;
        n.tooltip = tooltip;
        this.nodes.add(n);
    }

    public void addHighlight(String gateway, String host) {
        Highlight h = new Highlight();
        h.gateway = gateway;
        h.host = host;
        this.highlights.add(h);
    }

    @Override
    public void run() {
        this.graph.start();
        Iterator i = this.nodes.iterator();
        while (i.hasNext()) {
            Node n = (Node) i.next();
            this.graph.addNode(n.id, n.label, n.description, n.iconz, n.tooltip);
        }
        this.graph.setRoutes(this.routes.toArray(new Route[0]));
        i = this.highlights.iterator();
        while (i.hasNext()) {
            Highlight h = (Highlight) i.next();
            this.graph.highlightRoute(h.gateway, h.host);
        }
        this.graph.deleteNodes();
        this.graph.end();
    }

    private static class Node {
        public String id = "";
        public String label = "";
        public String description = "";
        public Image iconz = null;
        public String tooltip = "";

        private Node() {
        }
    }

    private static class Highlight {
        public String gateway = "";
        public String host = "";

        private Highlight() {
        }
    }

}

