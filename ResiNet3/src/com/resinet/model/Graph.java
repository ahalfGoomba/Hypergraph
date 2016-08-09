package com.resinet.model;

/* Auf den Klassen "com.resinet.model.Node" und "Edge" wird ein Zufallsgraph generiert. Mit einer Zufallszahl
werden die Konnektionsknoten festgelegt. Anschliessend werden die K-Baeume vom com.resinet.model.Graph abgeleitet.
Durch die K-Baeume erhaelt man die Minimalkombinationen.
*/

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Stellt einen Graph für die Berechnung dar. Enthält eine Menge von Knoten und eine Menge von Kanten
 */
public class Graph implements Serializable {

    private static final long serialVersionUID = -7423579476281719826L;
    public ArrayList<Node> nodeList;
    public ArrayList<Edge> edgeList;

    public Graph(ArrayList<Node> nodeList, ArrayList<Edge> edgeList) {
        this.nodeList = nodeList;
        this.edgeList = edgeList;
    }

    /**
     * Entfernt eine Kante vom Graphen.
     *
     * @param edge Die zu entfernende Kante
     */
    public void delete_Edge(Edge edge) {
        Node left = edge.left_node;
        Node right = edge.right_node;

        left.delete_Edge(edge);
        right.delete_Edge(edge);
        edgeList.remove(edge);
    }

    /**
     * Fügt eine Kante hinzu.
     *
     * @param edge Die neue Kante
     */
    public void add_Edge(Edge edge) {
        if (!edgeList.contains(edge)) {
            edgeList.add(edge);

            Node left = edge.left_node;
            Node right = edge.right_node;

            left.add_Edge(edge);
            right.add_Edge(edge);
        }
    }

    public ArrayList<Node> getNodelist() {
        return nodeList;
    }

    public ArrayList<Edge> getEdgelist() {
        return edgeList;
    }
}











