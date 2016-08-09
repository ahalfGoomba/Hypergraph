package com.resinet.model;

import java.util.ArrayList;

/**
 * Wrapper für eine Menge von Knoten und eine Menge von Kanten aus dem Graphen.
 */
public class GraphWrapper {
    public final ArrayList<NodePoint> nodes;
    public final ArrayList<EdgeLine> edges;

    public GraphWrapper() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
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
     * Fügt eine Kante hinzu.
     *
     * @param node1 Der Startknoten der Kante
     * @param node2 Der Endknoten der Kante
     */
    public void addEdge(NodePoint node1, NodePoint node2) {
        edges.add(new EdgeLine(node1, node2));
    }
}
