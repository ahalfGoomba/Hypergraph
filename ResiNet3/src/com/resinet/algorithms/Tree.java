package com.resinet.algorithms;/*Tree.java*/


import com.resinet.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

class Tree extends Thread {
    private final Graph graph;
    private final KTree kTree;
    final List<HashSet<GraphElement>> trs;
    //Menge aller k-Baeume
    private final Q q;

    public boolean dead = false;

    /**
     * Diese Klasse findet K-Bäume zu dem gegebenem Graphen als eigener Thread. Während des Pfadefindens wird der Graph
     * hierbei verändert, d.h. es werden Kanten entfernt und hinzugefügt. Aus diesem Grunde sollte der Graph vor jeder
     * Berechnung frisch kopiert werden.
     *
     * @param graph Der Graph, in dem K-Bäume gesucht werden sollen
     */
    public Tree(Graph graph) {
        this.graph = graph;
        q = new Q();
        kTree = new KTree();

        trs = Collections.synchronizedList(new ArrayList<>());
        //Initialisierung

        Node node;

        for (Node node2 : this.graph.nodeList) {
            if (node2.c_node) {
                node = node2;
                kTree.add_Node(node);
                outside_add(node);
                break;
            }
        }
        //Ende der Initialisierung
    }

    @Override
    public void run() {
        tree();

        dead = true;
        synchronized (trs) {
            trs.notifyAll();
        }
    }


    private Edge[] outside_add(Node w) {
        Edge[] edges = new Edge[w.degree];
        int addCount = 0;

        for (Edge edge : w.node_edge) {
            if (!edge.left_node.b_marked || !edge.right_node.b_marked) {
                q.add(edge);
                edges[addCount] = edge;
                addCount++;
            }
        }
        if (addCount == 0)
            return null;
        else
            return edges;
    }

    private Edge[] inside_delete(Node w) {
        Edge[] edges = new Edge[w.degree];
        int addCount = 0;

        for (Edge edge : w.node_edge) {
            Node node;

            if (edge.left_node == w)
                node = edge.right_node;
            else
                node = edge.left_node;

            if (edge.in_q && node.b_marked) {
                q.delete(edge);
                edges[addCount] = edge;
                addCount++;
            }
        }
        if (addCount == 0)
            return null;
        else
            return edges;
    }

    private void outside_delete(Edge[] edges) {
        if (edges == null)
            return;

        for (Edge edge : edges) {
            if (edge == null)
                break;
            q.delete(edge);
        }
    }

    private void inside_add(Edge[] edges) {
        if (edges == null)
            return;

        for (int i = edges.length - 1; i >= 0; i--) {
            if (edges[i] == null)
                continue;
            q.insert(edges[i]);
        }
    }

    private void output_ktree() {
        Edge e = kTree.first_br;

        HashSet<GraphElement> ktree = new HashSet<>();
        while (e != null) {
            if (!e.useless) {
                ktree.add(e);
            }
            e = e.right;
        }

        //An dieser Stelle wurde der Algorithmus erweitert.
        //Diese Liste enthält alle bisher hinzugefügten Knoten
        List<Node> consideredNodes = new ArrayList<>();

        //Eine flache Kopie des Baumes, durch die hindurchiteriert werden kann, da ktree um die Knoten erweitert wird.
        HashSet<Edge> treeCopy = (HashSet) ktree.clone();

        //String output = "Pfad";
        for (Edge edge : treeCopy) {
            //Prüft bei jeder Kante des Pfades für den linken und den rechten Knoten jeweils, ob er schon
            //zur Menge ktree hinzugefügt wurde, und fügt den Knoten hinzu, falls er noch nicht in der Menge ist.
            //output += " e" + edge.edge_no;

            if (!consideredNodes.contains(edge.left_node)) {
                consideredNodes.add(edge.left_node);
                ktree.add(edge.left_node);
                //output += " n" + edge.left_node.node_no;
            }

            if (!consideredNodes.contains(edge.right_node)) {
                consideredNodes.add(edge.right_node);
                ktree.add(edge.right_node);
                //output += " n" + edge.right_node.node_no;
            }
        }
        //System.out.println(output);

        synchronized (trs) {
            if (!trs.contains(ktree)) {
                trs.add(ktree);
                trs.notifyAll();
            }
        }
    }

    private void tree_search(Node node, Edge edge) {
        Node nextNode;
        boolean bl = true;

        for (Edge e : node.node_edge) {
            if (!e.b_marked)
                continue;
            if (e == edge)
                continue;

            if (e.left_node == node)
                nextNode = e.right_node;
            else
                nextNode = e.left_node;
            tree_search(nextNode, e);

            bl = bl && e.useless;
        }
        if (bl && !node.c_node) {
            edge.useless = true;
            kTree.no_use++;
            node.useless = true;
        }
    }


    private void tree() {
        Edge e;
        Node w;
        Edge[] addedEdges, deletedEdges, edgeList;

        edgeList = new Edge[q.count];
        int edgeListCounter = 0;

        if (q.count == 0) {
            Edge eg = kTree.last_br;
            while (eg != null) {
                eg.useless = false;
                eg = eg.left;
            }
            kTree.no_use = 0;
            //initialisierung fuer tree_search
            tree_search(kTree.first_nd, null);
            output_ktree();
            //if N(G,B)=0 then output spannender Baum B
        } else {
            while (Con_check.check(graph) < 0) {
                e = q.last;
                //e := letztes Element von Q

                q.delete(e);
                //Q := Q - e

                if (!e.left_node.b_marked)
                    w = e.left_node;
                else
                    w = e.right_node;
                //w := Endknoten von e der nicht in B ist

                kTree.add_Edge(e);
                kTree.add_Node(w);
                //B := B+{e}

                addedEdges = outside_add(w);
                //fuege N ={e<-E: I(e)={w,v},v nicht in B} bei Q hinten an

                deletedEdges = inside_delete(w);
                //entferne M = {e<-E: I(e)={u,w}, u in B} aus Q;

                tree();

                inside_add(deletedEdges);
                //fuege deletedEdges wieder Q bei

                outside_delete(addedEdges);
                //entferne N aus Q

                kTree.delete_Node(w);
                kTree.delete_Edge(e);

                //entfern e aus B;

                graph.delete_Edge(e);
                //entfern e aus G

                edgeList[edgeListCounter] = e;
                edgeListCounter++;
                //fuege e der Liste Q1 hinten an


		/* wenn diese Kante End-Kante des Graphens ist, dann
           bricht die While-Schleife ab. */
            }

            while (edgeListCounter > 0) {
                Edge edge;
                edge = edgeList[edgeListCounter - 1];
                q.add(edge);
                graph.add_Edge(edge);
                edgeListCounter--;
            }
        }
    }

    private static class Q {
        int count;
        Edge last;

        public Q() {
            count = 0;
        }

        void add(Edge edge) {
            if (count == 0)
                edge.q_left = null;
            else {
                edge.q_left = last;
                last.q_right = edge;
            }
            edge.in_q = true;
            edge.q_right = null;
            count++;
            last = edge;
        }

        void insert(Edge edge) {
            count++;
            edge.in_q = true;
            if (edge.q_left != null)
                edge.q_left.q_right = edge;
            if (edge.q_right != null)
                edge.q_right.q_left = edge;
            else
                last = edge;
        }

        void delete(Edge edge) {
            count--;
            edge.in_q = false;
            if (edge.q_left != null)
                edge.q_left.q_right = edge.q_right;
            if (edge.q_right != null)
                edge.q_right.q_left = edge.q_left;
            else
                last = edge.q_left;
        }

    }
}






/* Modifikation des Algorithmus von Kohlas

Da es sich hier um ein k-von-n Problem handelt, wobei k<=n, wird der
Algorithmus von Kohlas leicht modifiziert.

Line 231: Jedesmal, nachdem eine neue Kante e in den aktuellen Baum kTree hinzu-
gefuegt wurde, wird gepr�ft, ob die letzte Kante, d.h. e.left, eine s.graph. Endkante
und der dazugehoerige Knoten ein NICHT-Konnectionsknoten ist. Falls ja, wird 
weiter geprueft, ob der Knoten bereits erreicht wurde (w.reached == true). Falls
ja, wird die Schleife abgebrochen und kein Baum ausgegeben.

Line 342:
Wenn der Knoten w ein Nicht-K-Knoten ist und w.reached == true, w aber er kein
Endknoten ist, wird �berprueft, ob alle Knoten bzw. Kanten UNTER w "use-
less" sind, d.h. Nicht-k-Knoten. Wenn der Knoten EINMAL useless 
ist, wird der Knoten beim Loeschen als "reached"-Knoten gekenn-
zeichnet und erst bei erneutem Hinzufuegen die Attribute "reached" 
wieder auf "false" gesetzt . Beim Hinzufuegen eines Knotens in den Baum kTree wird
geprueft, ob das Attribut "reached" bereits "true" ist und der Knoten kein
Endknoten ist. Der Vorgang wird sonst trotzdem fortsetzt und das Attribut kTree.val
auf "false" gesetzt.
Beim Output eines Baums wird der Wert von kTree.val geprueft. Falls er "false"
ist, wird der Baum nicht ausgegeben.

Die Idee: Wenn ein Knoten "useless" ist, kann man ihn auch als Nicht-K- und
gleichzeitig End-Knoten betrachten, also als erreichten (reached) Knoten.

Alle erreichten Knoten sind Nicht-K-Knoten.

Erreichen ("reached"):
Da eine "useless" Kante geloescht werden muss, sollte eine solche Kante
in einer Schleife nur einmal gezaehlt werden. Die Baeume sollten nicht
ausgegeben (kTree.val = true) werden, nachdem die "useless"-Kante des Graphen geloescht
wurde, wenn der (ebenfalls "useless") Knoten ueber eine andere Kante NOCHMAL er-
reicht wird. Sonst kommt es zu doppelten Eintraegen in der Menge der K-Baeume.

Ueber com.resinet.model.KTree:
Alle K-Baeume werden direkt in die Datei ktree.txt geschrieben, damit 
das "Memory-Out-Problem" vermieden wird. S.a java.memory.tar/Tree.java.


*/  
