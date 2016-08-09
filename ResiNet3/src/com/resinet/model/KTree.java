package com.resinet.model;/* com.resinet.model.KTree.java */

public class KTree {
    private int count_nd = 0;
    private int count_br = 0;
    private Node last_nd;
    public Node first_nd;
    public Edge last_br, first_br;


    public int no_use = 0;

    public void add_Node(Node node) {
        node.b_marked = true;
        if (count_nd == 0)
            first_nd = node;
        else {
            last_nd.right = node;
            node.left = last_nd;
        }
        last_nd = node;
        count_nd++;
    }

    public void add_Edge(Edge b) {
        b.b_marked = true;
        if (count_br == 0) {
            first_br = b;
            b.left = null;
            b.right = null;
        } else {
            last_br.right = b;
            b.left = last_br;
            b.right = null;
        }
        last_br = b;
        count_br++;
    }

    public void delete_Node(Node node) {

        node.b_marked = false;
        if (node.left != null)
            node.left.right = node.right;
        if (node.right != null)
            node.right.left = node.left;
        last_nd = node.left;
        count_nd--;
    }

    public void delete_Edge(Edge b) {
        b.b_marked = false;
        if (b.left != null)
            b.left.right = b.right;
        if (b.right != null)
            b.right.left = b.left;
        last_br = b.left;
        count_br--;
    }

}



/*

Wenn ein "reached" Knoten in den Baum eingefuegt wird,
wird die Variable "start" auf "true" gesetzt, und das Programm faengt an,
alle Knoten "UNTER" dem Knoten auf Nutzlosigkeit zu ueberpruefen. Dann
handelt es sich nicht um s.g. c_nodes.

Falls ja , wird die Variable "val" auf false geschaltetn und der Baum wird nicht
ausgegeben.

Idee:
Wenn alle Knoten UNTER dem Knoten node nutzlos sind, ist der gesamte Zweig
nutzlos. So kann man den Knote "node" auch als "reached" End-Knoten
betrachten wie im Algorithmus Tree.java.

*/
