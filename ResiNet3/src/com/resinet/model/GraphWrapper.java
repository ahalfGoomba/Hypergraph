package com.resinet.model;

import java.util.ArrayList;

/**
 * Wrapper für eine Menge von Knoten und eine Menge von Kanten aus dem Graphen.
 */
public class GraphWrapper {
    public final ArrayList<NodePoint> nodes;
    public final ArrayList<EdgeLine> edges;
    public final ArrayList<HyperEdgePoint> hep; 
    public final ArrayList<HyperEdgeLine> hel; 
    
    public GraphWrapper() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        hep = new ArrayList<>();
        hel = new ArrayList<>();
    }

    /**
     * Fügt einen Knoten hinzu.
     *
     * @param np Der neue Knoten
     */
    public void addNode(NodePoint np) {
        nodes.add(np);
    }
    /**
     * f�gt einen HyperEdgePoint hinzu.
     * 
     * @param hp
     */
    public void addHyperEdgePoint(HyperEdgePoint hp){
    	hep.add(hp);
    }

    /**
     * Fügt eine Kante hinzu.
     *
     * @param node1 Der Startknoten der Kante
     * @param node2 Der Endknoten der Kante
     */
    public void addEdge(NodePoint node1, NodePoint node2) {
        edges.add(new EdgeLine(node1, node2));
    }
}
