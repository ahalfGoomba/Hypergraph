package com.resinet.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Node extends GraphElement implements Serializable {
    private static final long serialVersionUID = 7960307183209192115L;
    //Knotennummer
    public final int node_no;

    // Kennzeichen fuer Konnektionsknoten
    public boolean c_node = false;

    //Attribut für KTree
    Node left, right;
    //Anliegende Kanten
    public final List<Edge> node_edge = new ArrayList<>();

    //Grad des Knotens, d.h. Anzahl anliegender Kanten
    public int degree = 0;

    //Für die Tiefensuche beim Prüfen, ob der Graph zusammenhängend ist
    public boolean marked = false;


    public Node(int node_no, boolean c_node) {
        this.node_no = node_no;
        this.c_node = c_node;
    }

    /**
     * Fügt eine anliegende Kante hinzu.
     *
     * @param edge Die neue Kante
     */
    public void add_Edge(Edge edge) {
        node_edge.add(edge);
        degree = node_edge.size();
    }

    /**
     * Entfernt eine anliegende Kante.
     *
     * @param edge Die zu entfernende Kante
     */
    void delete_Edge(Edge edge) {
        node_edge.remove(edge);
        degree = node_edge.size();
    }

}
