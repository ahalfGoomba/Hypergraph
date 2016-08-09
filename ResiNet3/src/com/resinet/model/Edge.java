package com.resinet.model;/* Edge.java */

import java.io.Serializable;

/**
 * Kante für die Berechnung
 */
public class Edge extends GraphElement implements Serializable {

    private static final long serialVersionUID = 2149306518837874937L;
    private final int edge_no;

    // Knoten einer Kante
    public Node left_node, right_node;

    public Edge q_left;
    public Edge q_right;

    public Edge left, right;
    //for com.resinet.model.KTree.java

    public boolean in_q = false;
    //Mark fuer K-Baum (com.resinet.model.KTree.java)

    public Edge(int edge_no, Node left_node, Node right_node) {
        this.edge_no = edge_no;
        this.left_node = left_node;
        this.right_node = right_node;
    }
}




