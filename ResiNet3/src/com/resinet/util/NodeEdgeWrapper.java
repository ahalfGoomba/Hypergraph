package com.resinet.util;

import com.resinet.model.EdgeLine;
import com.resinet.model.HyperEdgePoint;
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
    ArrayList<HyperEdgePoint> originalHEPs;

    public final ArrayList<NodePoint> nodes;
    public final ArrayList<EdgeLine> edges;
    public final ArrayList<HyperEdgePoint> heps;
    
    private NodeEdgeWrapper(ArrayList<NodePoint> nodes, ArrayList<EdgeLine> edges, ArrayList<HyperEdgePoint> heps) {
        this.nodes = nodes;
        this.edges = edges;
        this.heps = heps;
    }

    public NodeEdgeWrapper(ArrayList<NodePoint> originalNodes, ArrayList<NodePoint> nodes, ArrayList<EdgeLine> edges,  ArrayList<HyperEdgePoint> originalHEPs, ArrayList<HyperEdgePoint> heps) {
        this(nodes, edges, heps);
        this.originalNodes = originalNodes;
        this.originalHEPs = originalHEPs;
    }
}
