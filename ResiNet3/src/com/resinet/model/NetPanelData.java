package com.resinet.model;

import com.resinet.util.NodeEdgeWrapper;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Verwaltet die Knoten- und Kantenmengen und deren Zustände, die im NetPanel dargestellt werden. Aktionen wie Knoten
 * hinzufügen/löschen können ausgeführt werden. Alle Aktionen werden auch in einem UndoManager registriert und können
 * auch rückgängig gemacht werden.
 * <p>
 * In Form von Unterklassen werden Aktionensklassen definiert. Jede Aktion hat diese drei Methoden:<ul><li>execute():
 * Führt die Aktion erstmalig aus</li><li>undo(): Macht die Aktion rückgängig</li><li>redo(): Wiederholt die Aktion,
 * wenn diese rückgängig gemacht wurde</li></ul> Zusätzlich hält jede Aktion natürlich alle Daten, die für die Methoden
 * benötigt werden.
 */
public class NetPanelData implements Serializable {
    private static final long serialVersionUID = -293719411015421415L;

    private final ArrayList<NodePoint> nodes;
    private final ArrayList<EdgeLine> edges;

    private final UndoManager undoManager;

    public NetPanelData() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        undoManager = new UndoManager();
    }

    /**
     * Fügt einen Knoten hinzu
     *
     * @param newNode Der neue Knoten
     */
    public void addNode(NodePoint newNode) {
        AddOrRemoveAction action = new AddOrRemoveAction(true, newNode);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Entfernt einen Knoten.
     *
     * @param node Der zu entfernende Knoten
     * @return Die Liste der damit zusätzlich entfernten Kanten
     */
    public List<Integer> removeNode(NodePoint node) {
        ArrayList<NodePoint> nodeWrapper = new ArrayList<>();
        ArrayList<EdgeLine> removeEdges = new ArrayList<>();
        ArrayList<Integer> removedEdgeIndices = new ArrayList<>();
        nodeWrapper.add(node);

        //Anliegende Kanten sammeln
        for (int i = 0; i < edges.size(); i++) {
            EdgeLine edge = edges.get(i);

            if (node.equals(edge.startNode) || node.equals(edge.endNode)) {
                removeEdges.add(edge);
                removedEdgeIndices.add(i);
            }
        }

        AddOrRemoveAction action = new AddOrRemoveAction(false, nodeWrapper, removeEdges);
        action.execute();
        undoManager.addEdit(action);

        return removedEdgeIndices;
    }

    /**
     * Fügt eine Kante hinzu.
     *
     * @param startNode Der Startknoten
     * @param endNode   Der Endknoten
     */
    public void addEdge(NodePoint startNode, NodePoint endNode) {
        EdgeLine newEdge = new EdgeLine(startNode, endNode);
        AddOrRemoveAction action = new AddOrRemoveAction(true, newEdge);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Entfernt eine Kante.
     *
     * @param edge Die zu entfernende Kante.
     */
    public void removeEdge(EdgeLine edge) {
        AddOrRemoveAction action = new AddOrRemoveAction(false, edge);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Entfernt eine Menge von Knoten und alle anliegenden Kanten.
     *
     * @param removeNodes Die Menge von zu entfernenden Knoten
     */
    public void removeNodes(ArrayList<NodePoint> removeNodes) {
        ArrayList<EdgeLine> removeEdges = new ArrayList<>();

        //Anliegende Kanten sammeln
        for (EdgeLine edge : edges) {
            if (removeNodes.contains(edge.startNode) || removeNodes.contains(edge.endNode)) {
                removeEdges.add(edge);
            }
        }

        AddOrRemoveAction action = new AddOrRemoveAction(false, removeNodes, removeEdges);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Erstellt ein Objekt mit den ausgewählten Knoten und den Kanten innerhalb der Knotenmenge. Dabei werden die Knoten
     * und Kanten geklont. Die Referenzen der Kanten werden auf die geklonten Knoten gesetzt.
     *
     * @return NodeEdgeWrapper
     */
    public NodeEdgeWrapper getSelectionCopyData() {
        //Diese Liste wird zum Entfernen der Knoten benötigt, falls die Aktion "Ausschneiden" ist
        ArrayList<NodePoint> originalSelectedNodes = new ArrayList<>();
        ArrayList<NodePoint> selectedNodes = new ArrayList<>();
        ArrayList<EdgeLine> selectedEdges = new ArrayList<>();

        //Ausgewählte Knoten sammeln
        for (NodePoint nodePoint : nodes) {
            if (nodePoint.selected) {
                selectedNodes.add(nodePoint);
            }
        }

        originalSelectedNodes.addAll(selectedNodes);

        //Anliegende Kanten klonen
        for (EdgeLine edgeLine : edges) {
            if (selectedNodes.contains(edgeLine.endNode) && selectedNodes.contains(edgeLine.startNode)) {
                selectedEdges.add((EdgeLine) edgeLine.clone());
            }
        }

        //Knoten klonen und dabei Referenzen der geklonten Kanten neu setzen
        for (int i = 0; i < selectedNodes.size(); i++) {
            NodePoint nodePoint = selectedNodes.get(i);

            NodePoint newNode = (NodePoint) nodePoint.clone();
            newNode.selected = false;

            //Referenzen neu setzen
            for (EdgeLine edgeLine : selectedEdges) {
                if (edgeLine.endNode.equals(nodePoint)) {
                    edgeLine.endNode = newNode;
                }
                if (edgeLine.startNode.equals(nodePoint)) {
                    edgeLine.startNode = newNode;
                }
            }
            //Aktuellen Knoten durch geklonten ersetzen
            selectedNodes.set(i, newNode);
        }
        return new NodeEdgeWrapper(originalSelectedNodes, selectedNodes, selectedEdges);
    }

    /**
     * Fügt eine Menge von Knoten und Kanten hinzu.
     *
     * @param nodes Die Knotenmenge
     * @param edges Die Kantenmenge
     */
    public void addNodesAndEdges(List<NodePoint> nodes, List<EdgeLine> edges) {
        AddOrRemoveAction action = new AddOrRemoveAction(true, nodes, edges);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Bewegt eine Menge von Knoten um die angegebenen Koordinaten. Diese Methode sollte nicht aufgerufen werden,
     * während das Verschieben noch im Gange ist.
     *
     * @param moveNodes Die Menge der zu bewegenden Knoten
     * @param amount    Die Dimension, um die die Knoten verschoben werden sollen
     */
    public void moveNodesFinal(ArrayList<NodePoint> moveNodes, Dimension amount) {
        MoveAction action = new MoveAction(moveNodes, amount);
        undoManager.addEdit(action);
    }

    /**
     * Bewegt eine Menge von Knoten um die angegebenen Koordinaten, aber fügt diese Aktion nicht in den UndoManager ein.
     * Diese Methode sollte aufgerufen werden, während das Verschieben am Gange ist.
     *
     * @param nodes  Die Menge der zu bewegenden Knoten
     * @param amount Die Dimension, um die die Knoten verschoben werden sollen
     */
    public void moveNodesNotFinal(ArrayList<NodePoint> nodes, Dimension amount) {
        MoveAction action = new MoveAction(nodes, amount);
        action.execute();
    }

    /**
     * Verändert die Position einer Knotenmenge innerhalb eines Auswählrechtecks. Diese Aktion wird ausgeführt, aber
     * nicht in den {@link UndoManager} eingetragen. Diese Methode sollte also aufgerufen werden, während das verzerren
     * im Gange ist.
     *
     * @param nodes              Die Knotenmenge
     * @param direction          Die Richtung, in der das Rechteck verzerrt wurde (Definiert in {@link
     *                           BorderRectangle#getResizableBorder(int, int, int)})
     * @param factorX            Faktor in X-Richtung
     * @param factorY            Faktor in Y-Richtung
     * @param selectionRectangle Das Auswählrechteck
     */
    public void resizeNodesNotFinal(List<NodePoint> nodes, int direction, double factorX, double factorY, BorderRectangle selectionRectangle) {
        ResizeAction action = new ResizeAction(nodes, direction, factorX, factorY, selectionRectangle);
        action.execute();
    }

    /**
     * Verändert die Position einer Knotenmenge innerhalb eines Auswählrechtecks. Diese Aktion wird nicht ausgeführt,
     * aber im Gegensatz zu {@link NetPanelData#resizeNodesNotFinal(List, int, double, double, BorderRectangle)} in den
     * {@link UndoManager} eingetragen. Diese Methode sollte also nach dem Verzerren aufgerufen werden, damit
     * Rückgängigmachen möglich ist.
     *
     * @param nodes              Die Knotenmenge
     * @param direction          Die Richtung, in der das Rechteck verzerrt wurde (Definiert in {@link
     *                           BorderRectangle#getResizableBorder(int, int, int)})
     * @param factorX            Faktor in X-Richtung
     * @param factorY            Faktor in Y-Richtung
     * @param selectionRectangle Das Auswählrechteck
     */
    public void resizeNodesFinal(List<NodePoint> nodes, int direction, double factorX, double factorY, BorderRectangle selectionRectangle) {
        ResizeAction action = new ResizeAction(nodes, direction, factorX, factorY, selectionRectangle);
        undoManager.addEdit(action);
    }

    /**
     * Bewegt alle Knoten um die angegebenen Koordinaten. Diese Aktion kann nicht rückgängig gemacht werden.
     *
     * @param amount Die Dimension, um die die Knoten verschoben werden sollen
     */
    public void moveAllNodesNoUndo(Dimension amount) {
        MoveAction action = new MoveAction(nodes, amount);
        action.execute();
    }

    /**
     * Entfernt alle Elemente des Graphen
     */
    public void resetGraph() {
        AddOrRemoveAction action = new AddOrRemoveAction(false, nodes, edges);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Entfernt alle ausgewählten Knoten.
     */
    public void removeSelectedNodes() {
        ArrayList<NodePoint> selectedNodes = new ArrayList<>();

        //Ausgewählte Knoten sammeln
        for (NodePoint nodePoint : nodes) {
            if (nodePoint.selected) {
                nodePoint.selected = false;
                selectedNodes.add(nodePoint);
            }
        }
        removeNodes(selectedNodes);
    }

    /**
     * Ändert den Terminalstatus eines Knotens (grafisch gesehen wird zwischen schwarz und weiß gewechselt).
     *
     * @param node Der Knoten, von dem der Terminalstatus geändert werden soll.
     */
    public void changeTerminalStatus(NodePoint node) {
        TerminalChangeAction action = new TerminalChangeAction(node);
        action.execute();
        undoManager.addEdit(action);
    }

    /**
     * Setzt bei allen Knoten den selected-Status auf false.
     */
    public void resetSelection() {
        for (NodePoint node : nodes) {
            node.selected = false;
        }
    }

    /**
     * Macht die letzte Aktion rückgängig, falls möglich.
     */
    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    /**
     * Macht das letzte Rückgängigmachen rückgängig, falls möglich.
     */
    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    /**
     * Gibt zurück, ob rückgängig gemacht werden kann.
     *
     * @return True, wenn rückgängig gemacht werden kann.
     */
    public boolean canUndo() {
        return undoManager.canUndo();
    }

    /**
     * Gibt zurück, ob wiederholt werden kann.
     *
     * @return True, wenn wiederholt werden kann.
     */
    public boolean canRedo() {
        return undoManager.canRedo();
    }

    /**
     * Gibt eine nicht veränderbare Listenrepräsentation der Knotenliste zurück.
     *
     * @return nicht veränderbare Listenrepräsentation der Knotenliste
     */
    public List<NodePoint> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /**
     * Gibt eine nicht veränderbare Listenrepräsentation der Kantenliste zurück.
     *
     * @return nicht veränderbare Listenrepräsentation der Kantenliste
     */
    public List<EdgeLine> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * Beschreibt alle Aktionen, bei denen Knoten und/oder Kanten hinzugefügt oder gelöscht werden.
     */
    private class AddOrRemoveAction extends AbstractUndoableEdit {
        private static final long serialVersionUID = 7183317665439824767L;
        List<NodePoint> affectedNodes;
        List<EdgeLine> affectedEdges;
        final boolean isAddAction;

        AddOrRemoveAction(boolean isAddAction, List<NodePoint> addedNodes, List<EdgeLine> addedEdges) {
            this.isAddAction = isAddAction;
            affectedNodes = new ArrayList<>(addedNodes);
            affectedEdges = new ArrayList<>(addedEdges);
        }

        AddOrRemoveAction(boolean isAddAction, NodePoint node) {
            this.isAddAction = isAddAction;
            affectedNodes = new ArrayList<>();
            affectedNodes.add(node);
        }

        AddOrRemoveAction(boolean isAddAction, EdgeLine edge) {
            this.isAddAction = isAddAction;
            affectedEdges = new ArrayList<>();
            affectedEdges.add(edge);
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            execute();
        }

        void execute() {
            if (affectedNodes != null) {
                if (isAddAction) {
                    nodes.addAll(affectedNodes);
                } else {
                    nodes.removeAll(affectedNodes);
                }
            }
            if (affectedEdges != null) {
                if (isAddAction) {
                    edges.addAll(affectedEdges);
                } else {
                    edges.removeAll(affectedEdges);
                }
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            if (affectedNodes != null) {
                if (isAddAction) {
                    nodes.removeAll(affectedNodes);
                } else {
                    nodes.addAll(affectedNodes);
                }
            }
            if (affectedEdges != null) {
                if (isAddAction) {
                    edges.removeAll(affectedEdges);
                } else {
                    edges.addAll(affectedEdges);
                }
            }
        }
    }

    /**
     * Beschreibt eine Aktion, bei der eine Menge von Knoten bewegt wird.
     */
    private class MoveAction extends AbstractUndoableEdit {
        private static final long serialVersionUID = 37760421287789359L;

        final List<NodePoint> movedNodes;
        final Dimension amount;

        MoveAction(ArrayList<NodePoint> nodes, Dimension amount) {
            movedNodes = new ArrayList<>(nodes);
            this.amount = amount;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            execute();
        }

        void execute() {
            for (NodePoint node : movedNodes) {
                node.x += amount.getWidth();
                node.y += amount.getHeight();
            }
            refreshEdges();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            for (NodePoint node : movedNodes) {
                node.x -= amount.getWidth();
                node.y -= amount.getHeight();
            }
            refreshEdges();
        }

        private void refreshEdges() {
            edges.stream().filter(
                    edgeLine -> movedNodes.contains(edgeLine.startNode) || movedNodes.contains(edgeLine.endNode))
                    .forEach(EdgeLine::refresh);
        }
    }

    /**
     * Beschreibt eine Aktion, bei der der Terminalstatus eines Knotens verändert wird
     */
    private static class TerminalChangeAction extends AbstractUndoableEdit {
        private static final long serialVersionUID = 8220691958588757429L;
        final NodePoint node;

        TerminalChangeAction(NodePoint node) {
            this.node = node;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            execute();
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            execute();
        }

        void execute() {
            node.c_node = !node.c_node;
        }
    }

    private class ResizeAction extends AbstractUndoableEdit {
        private static final long serialVersionUID = -807459713565607621L;

        final List<NodePoint> resizeNodes;
        final int direction;
        final double factorX, factorY;
        private final BorderRectangle selectionRectangle;

        ResizeAction(List<NodePoint> resizeNodes, int direction, double factorX, double factorY, BorderRectangle selectionRectangle) {
            this.resizeNodes = new ArrayList<>(resizeNodes);
            this.direction = direction;
            this.factorX = factorX;
            this.factorY = factorY;
            this.selectionRectangle = selectionRectangle;
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            execute();
        }

        void execute() {
            for (NodePoint node : resizeNodes) {
                double x = node.x - selectionRectangle.x + 10.0;
                double y = node.y - selectionRectangle.y + 10.0;

                if (direction == 1 || direction == 5 || direction == 8) {
                    //links
                    //rechter Rand vom Auswahlrechteck minus Abstand des Knotens von rechts um den Faktor erhöht
                    node.x = (selectionRectangle.x + selectionRectangle.width)
                            - ((selectionRectangle.width - x) * (factorX + 1.0)) - 10.0;
                }

                if (direction == 2 || direction == 5 || direction == 6) {
                    //oben
                    node.y = (selectionRectangle.y + selectionRectangle.height)
                            - ((selectionRectangle.height - y) * (factorY + 1.0)) - 10.0;
                }

                if (direction == 3 || direction == 6 || direction == 7) {
                    //rechts
                    node.x = selectionRectangle.x + x * (factorX + 1) - 10.0;
                }

                if (direction == 4 || direction == 7 || direction == 8) {
                    //unten
                    node.y = selectionRectangle.y + y * (factorY + 1) - 10.0;
                }
            }
            refreshEdges();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();

            //das selbe wie oben, mit mit Division statt Multiplikation mit Faktor
            for (NodePoint node : resizeNodes) {
                double x = node.x - selectionRectangle.x + 10.0;
                double y = node.y - selectionRectangle.y + 10.0;

                if (direction == 1 || direction == 5 || direction == 8) {
                    //links
                    //rechter Rand vom Auswahlrechteck minus Abstand des Knotens von rechts um den Faktor erhöht
                    node.x = (selectionRectangle.x + selectionRectangle.width)
                            - ((selectionRectangle.width - x) / (factorX + 1.0)) - 10.0;
                }

                if (direction == 2 || direction == 5 || direction == 6) {
                    //oben
                    node.y = (selectionRectangle.y + selectionRectangle.height)
                            - ((selectionRectangle.height - y) / (factorY + 1.0)) - 10.0;
                }

                if (direction == 3 || direction == 6 || direction == 7) {
                    //rechts
                    node.x = selectionRectangle.x + x / (factorX + 1) - 10.0;
                }

                if (direction == 4 || direction == 7 || direction == 8) {
                    //unten
                    node.y = selectionRectangle.y + y / (factorY + 1) - 10.0;
                }
            }
            refreshEdges();
        }

        private void refreshEdges() {
            edges.stream().filter(
                    edgeLine -> resizeNodes.contains(edgeLine.startNode) || resizeNodes.contains(edgeLine.endNode))
                    .forEach(EdgeLine::refresh);
        }
    }
}
