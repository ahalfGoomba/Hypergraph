package com.resinet.util;

import com.resinet.model.EdgeLine;
import com.resinet.model.HyperEdgeLine;
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
    ArrayList<HyperEdgePoint> originalhyperEdgePoints;

    public final ArrayList<NodePoint> nodes;
    public final ArrayList<EdgeLine> edges;
    public final ArrayList<HyperEdgePoint> hyperEdgePoints;
    public final ArrayList<HyperEdgeLine> hyperEdgeLines;
    
    private NodeEdgeWrapper(ArrayList<NodePoint> nodes, ArrayList<EdgeLine> edges, ArrayList<HyperEdgePoint> hyperEdgePoints, ArrayList<HyperEdgeLine> hyperEdgeLines) {
        this.nodes = nodes;
        this.edges = edges;
        this.hyperEdgePoints = hyperEdgePoints;
        this.hyperEdgeLines = hyperEdgeLines; 
    }

    public NodeEdgeWrapper(ArrayList<NodePoint> originalNodes, ArrayList<NodePoint> nodes, ArrayList<EdgeLine> edges,  ArrayList<HyperEdgePoint> originalhyperEdgePoints,
    		ArrayList<HyperEdgePoint> hyperEdgePoints, ArrayList<HyperEdgeLine> hyperEdgeLines) {
        this(nodes, edges, hyperEdgePoints, hyperEdgeLines);
        this.originalNodes = originalNodes;
        this.originalhyperEdgePoints = originalhyperEdgePoints;
    }
}
