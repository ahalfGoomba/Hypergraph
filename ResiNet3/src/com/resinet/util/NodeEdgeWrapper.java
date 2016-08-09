package com.resinet.util;

import com.resinet.model.EdgeLine;
import com.resinet.model.NodePoint;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Dient zum Wrappen von Knoten- und Kantenmengen. Wird etwa für die Copy&Paste-Aktionen des NetPanels verwendet.
 */
public class NodeEdgeWrapper implements Serializable {
    private static final long serialVersionUID = -615808185568770646L;

    /**
     * Die Originalliste, wird etwa zum Löschen von ausgeschnittenen Knoten benötigt, da die Knoten in der anderen Liste
     * dann geklont sind.
     */
    ArrayList<NodePoint> originalNodes;

    public final ArrayList<NodePoint> nodes;
    public final ArrayList<EdgeLine> edges;

    private NodeEdgeWrapper(ArrayList<NodePoint> nodes, ArrayList<EdgeLine> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public NodeEdgeWrapper(ArrayList<NodePoint> originalNodes, ArrayList<NodePoint> nodes, ArrayList<EdgeLine> edges) {
        this(nodes, edges);
        this.originalNodes = originalNodes;
    }
}
