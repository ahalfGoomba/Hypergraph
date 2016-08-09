package com.resinet.algorithms;/* com.resinet.algorithms.Con_check.java */

import com.resinet.model.Edge;
import com.resinet.model.Graph;
import com.resinet.model.Node;

import java.util.ArrayList;

public class Con_check {


    private static void depth_search(Graph g, int i)
    // von Knoten i aus wird eine Tiefensuche durchgefuehrt.
    {
        ArrayList<Node> nd;

        nd = g.nodeList;

        Node node = nd.get(i);
        node.marked = true;

        // wenn jeder Knoten von Knoten i aus erreichbar ist, ist der com.resinet.model.Graph auch zusammenhaengend
        for (Edge edge : node.node_edge) {
            int n;

            if (edge.left_node.node_no != i)
                n = edge.left_node.node_no;
            else
                n = edge.right_node.node_no;
            Node next_node = nd.get(n);
            if (!next_node.marked)
                depth_search(g, n);

        }
    }

    private static void reset_mark(Graph g) {
        for (Node node : g.nodeList) {
            node.marked = false;
        }
    }

    public static boolean isConnected(Graph g) {
        return check(g) == -1;
    }

    public static int check(Graph g) {
        ArrayList<Node> nd;
        nd = g.nodeList;

        reset_mark(g);

        depth_search(g, 0);
        //wir fangen immer mit dem ersten Knoten 0 an

        for (Node node : nd) {
            if (!node.marked) {
                return nd.indexOf(node);
            }
        }
        return (-1);
    }

}


