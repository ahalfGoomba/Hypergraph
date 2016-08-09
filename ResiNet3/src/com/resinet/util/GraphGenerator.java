package com.resinet.util;

import com.resinet.model.GraphWrapper;
import com.resinet.model.NodePoint;

import java.util.ArrayList;

public class GraphGenerator {

    /**
     * Generiert einen Linien-Graphen.
     *
     * @param nodeCount Die Anzahl Knoten
     * @return GraphWrapper mit den Knoten und Kanten der Linie
     */
    public static GraphWrapper generateLine(int nodeCount) {
        GraphWrapper graph = new GraphWrapper();

        //Distanz zwischen den x-Koordinaten der Knoten
        final int distance = 50;

        NodePoint lastNode = null;
        for (int i = 0; i < nodeCount; i++) {
            NodePoint np = new NodePoint(i * distance, 0, false);
            //Neuen Knoten hinzufügen
            graph.addNode(np);

            //Linie ziehen
            if (lastNode != null) {
                graph.addEdge(lastNode, np);
            }
            lastNode = np;
        }

        return graph;
    }

    /**
     * Generiert einen Ring-Graphen
     *
     * @param nodeCount Die Anzahl Knoten
     * @return GraphWrapper mit den Knoten und Kanten des Ringes
     */
    public static GraphWrapper generateRing(int nodeCount) {
        GraphWrapper graph = new GraphWrapper();

        //30 Pixel Abstand pro Knoten
        //radius = Umfang / 2*Pi
        double radius = (nodeCount * 50) / (2 * Math.PI);

        NodePoint lastNode = null;

        for (int i = 0; i < nodeCount; i++) {
            //Winkel bestimmen
            double angle = (i / (nodeCount * 1.0)) * (2.0 * Math.PI);

            //Position berechnen
            double x = radius * Math.cos(angle) + radius;
            double y = radius * Math.sin(angle) + radius;

            NodePoint np = new NodePoint(x, y, false);

            graph.addNode(np);
            //Verbindung zum letzten Knoten herstellen
            if (lastNode != null) {
                graph.addEdge(lastNode, np);
            }
            lastNode = np;
        }

        //Kreis schließen
        graph.addEdge(lastNode, graph.nodes.get(0));

        return graph;

    }

    /**
     * Generiert einen vollständigen Graphen.
     *
     * @param count Die Anzahl Knoten
     * @return GraphWrapper mit den Knoten und Kanten des vollständigen Graphen
     */
    public static GraphWrapper generateComplete(int count) {
        //Ringgraphen als Basis
        GraphWrapper graph = generateRing(count);

        //Kanten entfernen
        graph.edges.clear();

        //Alle Kanten hinzufügen
        for (NodePoint node1 : graph.nodes) {
            for (NodePoint node2 : graph.nodes) {
                if (node1 != node2) {
                    graph.addEdge(node1, node2);
                }
            }
        }

        return graph;
    }

    /**
     * Generiert ein Brückennetzwerk
     *
     * @return GraphWrapper mit den Knoten und Kanten des Brückennetzwerks
     */
    public static GraphWrapper generateBridge() {
        GraphWrapper graph = new GraphWrapper();

        NodePoint node1 = new NodePoint(0, 50, true);
        NodePoint node2 = new NodePoint(50, 0, false);
        NodePoint node3 = new NodePoint(100, 50, true);
        NodePoint node4 = new NodePoint(50, 100, false);

        graph.addNode(node1);
        graph.addNode(node2);
        graph.addNode(node3);
        graph.addNode(node4);

        graph.addEdge(node1, node2);
        graph.addEdge(node2, node3);
        graph.addEdge(node1, node4);
        graph.addEdge(node4, node3);
        graph.addEdge(node2, node4);

        return graph;

    }

    /**
     * Generiert eine Baumstruktur
     *
     * @param height           Die Höhe des Baumes
     * @param innerDescendants innere Kindsknoten
     * @param leafCount        Kindsknoten auf der untersten Ebene
     * @return GraphWrapper mit Knoten und Kanten der Baumstruktur
     */
    public static GraphWrapper generateTree(int height, int innerDescendants, int leafCount) {
        GraphWrapper graph = new GraphWrapper();

        //maximale Breite bestimmen
        double graphWidth = Math.pow(innerDescendants, height - 2) * leafCount * 35;

        ArrayList<NodePoint> currentLevelNodes = new ArrayList<>();
        ArrayList<NodePoint> nextLevelNodes = new ArrayList<>();

        //Wurzel hinzufügen
        NodePoint root = new NodePoint(graphWidth / 2, 0, false);
        graph.addNode(root);
        currentLevelNodes.add(root);

        for (int i = 0; i < height - 1; i++) {
            //Anzahl Kinderknoten bestimmen
            int descendantCount = innerDescendants;
            if (i == height - 2) {
                //letzte Ebene mit Blättern
                descendantCount = leafCount;
            }

            int offsetY = (i + 1) * 50;

            double nodeDistance = graphWidth / (currentLevelNodes.size() * descendantCount);

            //Anzahl bisher auf dieser Ebene erstellter Knoten
            int createdNodes = 0;
            for (NodePoint np : currentLevelNodes) {

                for (int j = 0; j < descendantCount; j++) {
                    //Knoten hinzufügen
                    NodePoint child = new NodePoint(createdNodes * nodeDistance + (nodeDistance * 0.5), offsetY, false);
                    graph.addNode(child);
                    //Kante zum Kindknoten hinzufügen
                    graph.addEdge(np, child);

                    createdNodes++;
                    nextLevelNodes.add(child);
                }
            }
            currentLevelNodes.clear();
            currentLevelNodes.addAll(nextLevelNodes);
            nextLevelNodes.clear();
        }

        return graph;
    }
}
