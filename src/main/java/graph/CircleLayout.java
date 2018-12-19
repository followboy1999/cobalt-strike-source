package graph;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.model.mxIGraphModel;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import java.util.ArrayList;

public class CircleLayout
        extends mxCircleLayout {
    public CircleLayout(mxGraph graph, double r) {
        super(graph, r);
    }

    public void execute(Object parent, int width, int height, double zoom) {
        mxIGraphModel model = this.graph.getModel();
        model.beginUpdate();
        try {
            double max = 0.0;
            Double top = null;
            Double left = null;
            ArrayList<Object> vertices = new ArrayList<>();
            int childCount = model.getChildCount(parent);
            for (int i = 0; i < childCount; ++i) {
                Object cell = model.getChildAt(parent, i);
                if (!this.isVertexIgnored(cell)) {
                    vertices.add(cell);
                    mxRectangle bounds = this.getVertexBounds(cell);
                    top = top == null ? Double.valueOf(bounds.getY()) : Double.valueOf(Math.min(top, bounds.getY()));
                    left = left == null ? Double.valueOf(bounds.getX()) : Double.valueOf(Math.min(left, bounds.getX()));
                    max = Math.min(max, Math.max(bounds.getWidth(), bounds.getHeight()));
                    continue;
                }
                if (this.isEdgeIgnored(cell)) continue;
                if (this.isResetEdges()) {
                    this.graph.resetEdge(cell);
                }
                if (!this.isDisableEdgeStyle()) continue;
                this.setEdgeStyleEnabled(cell, false);
            }
            int vertexCount = vertices.size();
            double r = (double) (width > height ? height : width) / (2.8 * zoom);
            if (this.moveCircle) {
                top = this.x0;
                left = this.y0;
            }
            this.circle(vertices.toArray(), r, left, top);
        } finally {
            model.endUpdate();
        }
    }
}

