package com.resinet.util;

import com.resinet.algorithms.Con_check;
import com.resinet.model.*;
import com.resinet.views.NetPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Stellt Funktionen zur Verfügung, die beim Umgang mit Graphen helfen
 */
public class GraphUtil {
    /**
     * Bestimmt das den Graphen umgebende Rechteck
     *
     * @param nodes  Die Knotenliste
     * @param spaces Abstand des Rechtecks in alle Richtungen zum Graphen
     * @return Ein Rechteck, das den Graphen umschließt
     */
    public static BorderRectangle getGraphBounds(List<NodePoint> nodes, int spaces) {
        if (nodes.isEmpty()) {
            return new BorderRectangle(0, 0, 0, 0);
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = 0, maxY = 0;
        for (NodePoint drawnNode : nodes) {
            if (drawnNode.getX() < minX) {
                minX = (int) drawnNode.getX();
            }
            if (drawnNode.getMaxX() > maxX) {
                maxX = (int) drawnNode.getMaxX();
            }
            if (drawnNode.getY() < minY) {
                minY = (int) drawnNode.getY();
            }
            if (drawnNode.getMaxY() > maxY) {
                maxY = (int) drawnNode.getMaxY();
            }
        }

        return new BorderRectangle(Math.max(minX - spaces, 0), Math.max(minY - spaces, 0),
                Math.max(maxX - minX + 2 * spaces, 0), Math.max(maxY - minY + 2 * spaces, 0));
    }


    /**
     * Bestimmt das den Graphen umgebende Rechteck
     *
     * @param nodes Die Knotenliste
     * @return Ein Rechteck, das den Graphen umschließt
     */
    public static BorderRectangle getGraphBounds(List<NodePoint> nodes) {
        return getGraphBounds(nodes, 0);
    }


    /**
     * Prüft, ob der Graph alle notwendigen Bedingungen erfüllt
     *
     * @return Ob der Graph zulässig ist
     */
    public static boolean graphIsValid(Component parentComponent, Graph graph) {
        if (graph.edgeList.size() == 0) {
            //Dieser Block zeigt ein Hinweisfenster an, wenn keine Knoten vorhanden sind und bricht die Methode ab
            JOptionPane.showMessageDialog(parentComponent, Strings.getLocalizedString("error.no.edges"),
                    Strings.getLocalizedString("warning"), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Anzahl Konnektionsknoten bestimmen
        int cNodeCount = 0;
        for (Node node : graph.nodeList) {
            if (node.c_node)
                cNodeCount++;
        }

        if (cNodeCount < 2) {
            //Der Code in diesem Block zeigt nur ein Hinweisfenster an und bricht die Funktion ab
            JOptionPane.showMessageDialog(parentComponent, Strings.getLocalizedString("error.c_vertices.not.enough"),
                    Strings.getLocalizedString("warning"), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Prüfen ob das Netz zusammenhängt
        if (!Con_check.isConnected(graph)) {
            JOptionPane.showMessageDialog(parentComponent, Strings.getLocalizedString("your.graph.is.not.connected"),
                    Strings.getLocalizedString("error"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }


    /**
     * Erstellt aus den Knoten und Kanten des gezeichneten Graphen ein Graph-Objekt
     *
     * @return das Graph-Objekt zum gezeichneten Graph
     */
    public static Graph makeGraph(NetPanel netPanel) {
        List<EdgeLine> graphEdges = netPanel.getEdges();
        List<NodePoint> graphNodes = netPanel.getNodes();

        ArrayList<Node> nodeList = new ArrayList<>();
        ArrayList<Edge> edgeList = new ArrayList<>();

        int counter = 0;
        //Knoten eintragen
        for (NodePoint np : graphNodes) {
            Node node = new Node(counter, np.c_node);
            nodeList.add(node);
            counter++;
        }

        counter = 0;
        //Kanten eintragen
        for (EdgeLine e : graphEdges) {
            int m = graphNodes.indexOf(e.startNode);
            int n = graphNodes.indexOf(e.endNode);

            Node node1 = nodeList.get(m);
            Node node2 = nodeList.get(n);

            Edge edge = new Edge(counter, node1, node2);

            edgeList.add(edge);
            node1.add_Edge(edge);
            node2.add_Edge(edge);
            counter++;
        }
        return new Graph(nodeList, edgeList);
    }

}
