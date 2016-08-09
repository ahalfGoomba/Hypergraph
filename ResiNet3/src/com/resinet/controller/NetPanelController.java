package com.resinet.controller;

import com.resinet.model.*;
import com.resinet.util.GraphChangedListener;
import com.resinet.util.GraphUtil;
import com.resinet.util.NodeEdgeWrapper;
import com.resinet.views.NetPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NetPanelController implements MouseListener, MouseMotionListener {
    private final NetPanel netPanel;
    private final GraphChangedListener listener;

    private final NetPanelData netData;

    //Variablen für Verschieben und Skalieren
    private boolean cursorInsideSelection;
    private boolean cursorOnSelectionBorder = false;
    private int resizeBorder = 0;
    private boolean selectedNodesDragging = false;
    private boolean selectedNodesResizing = false;
    private Point selectionDraggingStart;

    //Variablen für das Auswählen
    private boolean selectDragging = false;
    private Point selectStartPoint;
    private boolean nodesSelected = false;
    private BorderRectangle selectionRectangle;
    private BorderRectangle beginSelectionRectangle;

    //Werden false, wenn etwa Knoten nicht berücksichtigt werden sollen
    private boolean nodeClickable = true;
    private boolean edgeClickable = true;

    //Aktuelle Mausposition
    private Point currentMousePosition;
    //aktuell von der Maus anklickbares Element (Knoten/Kante)
    private Shape hoveredElement;

    //die Kante, die gezeichnet wird, während man die Maus gedrückt hält (beim Kanten erstellen)
    private EdgeLine draggingLine;
    private boolean newLineDragging = false;

    //Distanz der Maus von einer Kante, in der diese mit der Maus anklickbar ist.
    private static final int EDGE_HOVER_DISTANCE = 7;
    //Distanz der Maus vom Auswahlrechteck, von der aus die Größe des Recktecks verändert werden kann.
    private static final int RESIZE_DISTANCE = 4;

    public NetPanelController(NetPanel netPanel, GraphChangedListener listener) {
        this.netPanel = netPanel;
        this.listener = listener;

        netData = new NetPanelData();
    }

    /**
     * Wird vom Mainframe-Controller weitergegeben, wenn das NetPanel Fokus hat und dient dazu, Copy&Paste-Aktionen und
     * Aktionen mit definiertem ActionCommand zu behandeln.
     *
     * @param e Das ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        switch (action) {
            case "delete":
                removeSelectedNodes();
                break;
            case "undo":
                netData.undo();
                resetSelection();
                break;
            case "redo":
                netData.redo();
                resetSelection();
                break;
            case "select_all":
                selectAllNodes();
                break;
            case "select overlapping":
                selectOverlappingNodes();
                break;
            default:
                Action a = netPanel.getActionMap().get(action);
                if (a != null) {
                    a.actionPerformed(new ActionEvent(netPanel, ActionEvent.ACTION_PERFORMED, null));
                }
                break;
        }
        listener.graphChanged();
        netPanel.repaint();
    }

    /**
     * Zentriert den Graphen, wenn eine Flag dafür gesetzt wurde, etwa nach dem Laden eines Graphen aus einer Datei.
     */
    public void centerGraph() {
        int panelHeight = netPanel.getHeight();
        int panelWidth = netPanel.getWidth();

        //Direktes Vaterelement ist der ViewPort der Scrollpane, und davon das Vaterelement ist die Scrollpane
        Container parent = netPanel.getParent().getParent();
        if (parent instanceof JScrollPane) {
            panelHeight = netPanel.getParent().getHeight();
            panelWidth = netPanel.getParent().getWidth();
        }

        Rectangle2D graphRect = GraphUtil.getGraphBounds(netData.getNodes());

        Integer offsetX, offsetY;

        //X-Offset bestimmen
        if (graphRect.getX() < 0) {
            offsetX = Math.abs(((int) graphRect.getX()));
        } else {
            offsetX = (int) ((panelWidth - graphRect.getWidth()) / 2 - graphRect.getX());
        }
        if (graphRect.getX() + offsetX < 0) {
            offsetX = -((int) graphRect.getX());
        }
        //Y-Offset bestimmen
        if (graphRect.getY() < 0) {
            offsetY = Math.abs((int) graphRect.getY());
        } else {
            offsetY = (int) ((panelHeight - graphRect.getHeight()) / 2 - graphRect.getY());
        }
        if (graphRect.getY() + offsetY < 0) {
            offsetY = -((int) graphRect.getY());
        }

        //Aktion zum verschieben auslösen
        netData.moveAllNodesNoUndo(new Dimension(offsetX, offsetY));
    }

    /**
     * Entfernt die Knoten und deren anliegende Kanten vom Graphen.
     *
     * @param nodes Die zu entfernende Knotenliste
     */
    public void removeNodes(ArrayList<NodePoint> nodes) {
        netData.removeNodes(nodes);
        listener.graphChanged();
        netPanel.repaint();
    }

    /**
     * Setzt den Graph zurück
     */
    public void resetGraph() {
        netData.resetGraph();
        newLineDragging = false;
        draggingLine = null;
        resetSelection();
        netPanel.repaint();
    }


    /**
     * Setzt die Auswahl zurück
     */
    public void resetSelection() {
        nodesSelected = false;
        netPanel.selectionAnimationTimer.stop();
        netData.resetSelection();
    }

    /**
     * Entfernt alle ausgewählten Knoten vom Graphen
     */
    private void removeSelectedNodes() {
        if (!nodesSelected)
            return;

        netData.removeSelectedNodes();
        resetSelection();
    }

    /**
     * Erstellt ein Objekt mit den ausgewählten Knoten und den Kanten innerhalb der Knotenmenge. Dabei werden die Knoten
     * und Kanten geklont. Die Referenzen der Kanten werden auf die geklonten Knoten gesetzt.
     *
     * @return NodeEdgeWrapper
     */
    public NodeEdgeWrapper getSelectionCopyData() {
        if (!nodesSelected) {
            return null;
        }
        return netData.getSelectionCopyData();
    }


    /**
     * Fügt Knoten und Kanten in den Graphen ein, welche vorher möglicherweise kopiert/ausgeschnitten wurden. Die Knoten
     * werden automatisch ausgewählt, damit sie verschoben werden können. Zum Einfügen wird ein freier Platz ausgehend
     * von der Mausposition gesucht, falls die eingefügten Knoten mit bestehenden Knoten überlappen.
     *
     * @param nodeEdgeWrapper Das Wrapper-Objekt mit Knoten- und Kantenmenge
     */
    public void pasteNodesAndEdges(NodeEdgeWrapper nodeEdgeWrapper) {
        if (nodeEdgeWrapper == null)
            return;

        //Auswahl zurücksetzen, damit die neuen Knoten nicht automatisch zusätzlich ausgewählt sind
        resetSelection();

        ArrayList<NodePoint> nodes = nodeEdgeWrapper.nodes;

        //Freie Stelle für neue Knoten suchen
        BorderRectangle pasteRectangle = GraphUtil.getGraphBounds(nodeEdgeWrapper.nodes);
        Point2D originalLocation = pasteRectangle.getLocation();

        //Wenn das Rechteck um die eingefügten Knoten andere Knoten kreuzt, neue Position suchen
        if (intersectsNode(pasteRectangle)) {
            //Mausposition testen
            Point mousePosition = netPanel.getMousePosition();
            //Mausposition kann null sein, etwa wenn die Maus nicht über dem NetPanel ist
            if (mousePosition != null) {
                pasteRectangle.setLocation(mousePosition);
            }

            if (intersectsNode(pasteRectangle)) {
                //Falls an der Mausposition nicht möglich, sicheres Rechteck suchen

                Point2D whiteRectanglePosition = searchSafeRectanglePosition(pasteRectangle);
                if (whiteRectanglePosition != null) {
                    pasteRectangle.setLocation(whiteRectanglePosition);
                } else {
                    //Alle Methoden erfolglos, Rechteck außerhalb der Zeichenfläche verschieben, damit die Zeichenfläche vergrößert wird.
                    pasteRectangle.x = netPanel.getWidth();
                }
            }
        }

        //Knoten wie das Rechteck verschieben
        for (NodePoint nodePoint : nodes) {
            nodePoint.x += pasteRectangle.getX() - originalLocation.getX();
            nodePoint.y += pasteRectangle.getY() - originalLocation.getY();
        }

        //Kantenkoordinaten neu setzen
        nodeEdgeWrapper.edges.forEach(EdgeLine::refresh);

        //Neue Knoten und Kanten hinzufügen
        netData.addNodesAndEdges(nodes, nodeEdgeWrapper.edges);

        listener.graphChanged();

        //Eingefügte Knoten auswählen
        nodeEdgeWrapper.nodes.forEach((nodePoint -> nodePoint.selected = true));
        selectionRectangle = GraphUtil.getGraphBounds(nodeEdgeWrapper.nodes, 5);
        nodesSelected = true;
        netPanel.selectionAnimationTimer.restart();

        //Scrollbar refreshen
        revalidateScrollPane();
        netPanel.repaint();
    }


    /**
     * Sucht eine Position für das Rechteck, an der es mit keinem Knoten überlappt.
     *
     * @param rectangle Das Rechteck
     * @return neue Position oder null
     */
    private Point2D searchSafeRectanglePosition(BorderRectangle rectangle) {
        double xRange = netPanel.getWidth() - rectangle.getWidth();
        double yRange = netPanel.getHeight() - rectangle.getHeight();

        for (int x = 0; x < xRange; x += 20) {
            for (int y = 0; y < yRange; y += 20) {
                rectangle.setLocation(x, y);

                if (!intersectsNode(rectangle)) {
                    return rectangle.getLocation();
                }
            }
        }
        return null;
    }


    /**
     * Prüft, ob das Rechteck einen Knoten schneidet.
     *
     * @param pasteRectangle Das zu überprüfende Rechteck
     * @return Boolean
     */
    private boolean intersectsNode(Rectangle2D pasteRectangle) {
        List<NodePoint> drawnNodes = netData.getNodes();

        for (NodePoint nodePoint : drawnNodes) {
            if (nodePoint.intersects(pasteRectangle)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Lässt die Scrollpane revalidieren, damit etwa die Scrollbars an die veränderte Größe des Graphen angepasst
     * werden.
     */
    private void revalidateScrollPane() {
        Component parent2 = netPanel.getParent().getParent();
        parent2.revalidate();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        netPanel.requestFocusInWindow();

        if (nodesSelected) {
            resetSelection();
            netPanel.repaint();
            return;
        }
        List<NodePoint> drawnNodes = netData.getNodes();
        List<EdgeLine> drawnEdges = netData.getEdges();

        Integer clickX = mouseEvent.getX();
        Integer clickY = mouseEvent.getY();
        boolean nodeClicked = false;

        //Prüfen, ob der Click einen Knoten getroffen hat
        for (NodePoint currentNode : drawnNodes) {

            if (currentNode.contains(clickX, clickY)) {
                //Knoten wurde angeklickt
                nodeClicked = true;

                if (mouseEvent.isShiftDown() || SwingUtilities.isMiddleMouseButton(mouseEvent)) {
                    //mit Shift geklickt oder mit mittlerer Maustaste
                    //Knoten löschen
                    int currentNodeIndex = drawnNodes.indexOf(currentNode);

                    List<Integer> removedEdgeIndices = netData.removeNode(currentNode);
                    listener.graphElementDeleted(true, currentNodeIndex);

                    for (Integer pos : removedEdgeIndices) {
                        listener.graphElementDeleted(false, pos);
                    }

                } else if (SwingUtilities.isRightMouseButton(mouseEvent)) {
                    //Rechtsklick oder mit Strg
                    //Knoten zum K-Knoten machen oder umgekehrt
                    netData.changeTerminalStatus(currentNode);

                } else if (nodeClickable) {
                    //Event auslösen
                    listener.graphElementClicked(true, drawnNodes.indexOf(currentNode));
                }
                break;
            }
        }

        if (!nodeClicked) {
            boolean edgeClicked = false;
            //Wenn kein Knoten angeklickt wurde, auf Kante prüfen
            for (EdgeLine edgeLine : drawnEdges) {

                if (edgeLine.ptSegDist(clickX, clickY) < EDGE_HOVER_DISTANCE) {
                    if (mouseEvent.isShiftDown() || SwingUtilities.isMiddleMouseButton(mouseEvent)) {
                        //mit shift oder mit mittlerer Maustaste geklickt --> Kante entfernen
                        netData.removeEdge(edgeLine);

                        int edgeIndex = drawnEdges.indexOf(edgeLine);
                        listener.graphElementDeleted(false, edgeIndex);

                    } else if (edgeClickable) {
                        //Event auslösen
                        listener.graphElementClicked(false, drawnEdges.indexOf(edgeLine));
                    }
                    edgeClicked = true;
                    break;
                }
            }

            if (!edgeClicked) {
                //Wenn kein bestehendes Element angeklickt wurde, neuen Knoten erzeugen
                int x = clickX - 10;
                int y = clickY - 10;

                if(mouseEvent.isControlDown())
            	{
                	System.out.println("test");
                	HyperEdgePoint newHEP = new HyperEdgePoint(x, y);
                	//TODO hep erstellen + multiselection


            	} else {
                //isMetaDown() ist beim Rechtsklick true
                boolean c_node = mouseEvent.isMetaDown();

                NodePoint newNode = new NodePoint(x, y, c_node);
                //Prüfen, ob der neue Knoten mit einem anderen überlappt
                for (NodePoint currentNode : drawnNodes) {

                    //Falls er überlappt, den neuen Knoten in die entsprechende Richtung verschieben
                    if (currentNode.getFrame().intersects(newNode.getFrame())) {
                        if (x > currentNode.getX() && y > currentNode.getY()) {
                            newNode = new NodePoint(x + 10, y + 10, c_node);
                        } else if (x > currentNode.getX() && y < currentNode.getY()) {
                            newNode = new NodePoint(x + 10, y - 10, c_node);
                        } else if (x < currentNode.getX() && y > currentNode.getY()) {
                            newNode = new NodePoint(x - 10, y + 10, c_node);
                        } else {
                            newNode = new NodePoint(x - 10, y - 10, c_node);
                        }
                    }
                }
                //neuen Knoten hinzufügen
                netData.addNode(newNode);
                listener.graphElementAdded(true, drawnNodes.size() - 1);
            	}
                }
        }
        //neu zeichnen
        netPanel.repaint();
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.isMetaDown()) {
            //Rechtsklick ignorieren
            return;
        }
        //Mausposition setzen
        currentMousePosition = mouseEvent.getPoint();

        newLineDragging = false;
        int x = mouseEvent.getX();
        int y = mouseEvent.getY();

        //Prüfen, ob die Auswahl verschoben werden soll oder ob die Auswahlgröße geändert werden soll
        if (cursorInsideSelection || cursorOnSelectionBorder) {
            selectedNodesDragging = cursorInsideSelection;
            selectedNodesResizing = !cursorInsideSelection;
            selectionDraggingStart = mouseEvent.getPoint();
            hoveredElement = null;
            beginSelectionRectangle = (BorderRectangle) selectionRectangle.clone();
            return;
        }

        //Prüfen, ob in ein Knoten gedrückt wurde, damit das Ziehen einer Kante gestartet wird
        List<NodePoint> drawnNodes = netData.getNodes();
        for (NodePoint node : drawnNodes) {
            if (node.contains(x, y)) {
                //Es wurde in den Kreis geklickt, also Kantenziehen starten
                draggingLine = new EdgeLine(node, null);
                newLineDragging = true;
                return;
            }
        }

        //Ansonsten auswählen beginnen
        selectDragging = true;
        //Startpunkt setzen
        selectStartPoint = mouseEvent.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent.isMetaDown())
            return;

        List<NodePoint> drawnNodes = netData.getNodes();
        List<EdgeLine> drawnEdges = netData.getEdges();

        if (newLineDragging) {
            newLineDragging = false;
            int x = mouseEvent.getX();
            int y = mouseEvent.getY();

            //Prüft, ob die Maus innerhalb eines Knotens losgelassen wurde
            for (NodePoint currentNode : drawnNodes) {

                if (currentNode.contains(x, y) && draggingLine.startNode != currentNode) {

                    //Prüfen, ob eine Kante mit genau diesen anliegenden Knoten bereits existiert
                    for (EdgeLine currentEdge : drawnEdges) {

                        //Wenn die Kante existiert, dann abbrechen
                        if (currentEdge.startNode == currentNode && currentEdge.endNode == draggingLine.startNode
                                || currentEdge.startNode == draggingLine.startNode && currentEdge.endNode == currentNode) {
                            netPanel.repaint();
                            return;
                        }
                    }

                    //Maus wurde in diesem Knoten losgelassen -> als Endknoten der Kante festlegen
                    netData.addEdge(draggingLine.startNode, currentNode);
                    listener.graphElementAdded(false, drawnEdges.size() - 1);

                    netPanel.repaint();
                    return;
                }
            }
        }
        //Falls ausgewählt wurde
        if (selectDragging) {
            selectDragging = false;

            //Rechteck mit dem Startpunkt und der aktuellen Position erstellen
            selectionRectangle = new BorderRectangle(mouseEvent.getPoint(), selectStartPoint);

            //Liste mit ausgewählten Knoten
            ArrayList<NodePoint> selectedNodes = new ArrayList<>();

            for (NodePoint node : drawnNodes) {
                if (node.intersects(selectionRectangle)) {
                    node.selected = true;
                    nodesSelected = true;
                    selectedNodes.add(node);
                } else {
                    node.selected = false;
                }
            }

            //Rechteck erstellen, dass mit 5 Pixel Abstand alle ausgewählten Knoten umschließt
            //Falls keine Knoten ausgewählt wurden, hat das Reckteck alle Parameter auf 0
            selectionRectangle = GraphUtil.getGraphBounds(selectedNodes, 5);

            if (nodesSelected) {
                //Timer (neu) starten (falls er bereits läuft)
                netPanel.selectionAnimationTimer.restart();
            }
        }

        if (selectedNodesDragging) {
            selectedNodesDragging = false;

            //Ausgewählte Knoten sammeln
            ArrayList<NodePoint> selectedNodes = new ArrayList<>(
                    drawnNodes.stream().filter(nodePoint -> nodePoint.selected).collect(Collectors.toList()));

            Point endPoint = mouseEvent.getPoint();
            Dimension moveAmount = new Dimension(endPoint.x - selectionDraggingStart.x, endPoint.y - selectionDraggingStart.y);

            netData.moveNodesFinal(selectedNodes, moveAmount);
            //Scrollpane aktualisieren
            revalidateScrollPane();
        }

        if (selectedNodesResizing) {
            selectedNodesResizing = false;

            //Ausgewählte Knoten sammeln
            ArrayList<NodePoint> selectedNodes = new ArrayList<>(
                    drawnNodes.stream().filter(nodePoint -> nodePoint.selected).collect(Collectors.toList()));

            double factorX = selectionRectangle.width / beginSelectionRectangle.width - 1.0;
            double factorY = selectionRectangle.height / beginSelectionRectangle.height - 1.0;

            netData.resizeNodesFinal(selectedNodes, resizeBorder, factorX, factorY, selectionRectangle);
        }
        netPanel.repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        if (newLineDragging) {
            //Während des Kantenziehens die Endposition der Kante aktualisieren, damit Sie immer unter dem Cursor endet
            int x = evt.getX();
            int y = evt.getY();
            draggingLine.x2 = x;
            draggingLine.y2 = y;
            netPanel.repaint();
        } else if (selectDragging) {
            //Mausposition setzen und Auswählrechteck neu zeichnen
            currentMousePosition = evt.getPoint();
            netPanel.repaint();
        } else if (selectedNodesDragging) {
            Point newMousePosition = evt.getPoint();

            //Offsets bestimmen
            int offsetX = newMousePosition.x - currentMousePosition.x;
            int offsetY = newMousePosition.y - currentMousePosition.y;

            //Auswahlrechteck verschieben
            selectionRectangle.addToX(offsetX);
            selectionRectangle.addToY(offsetY);

            List<NodePoint> drawnNodes = netData.getNodes();
            ArrayList<NodePoint> selectedNodes = new ArrayList<>();

            //Ausgewählte Knoten sammeln
            for (NodePoint nodePoint : drawnNodes) {
                if (nodePoint.selected) {
                    selectedNodes.add(nodePoint);
                }
            }
            netData.moveNodesNotFinal(selectedNodes, new Dimension(offsetX, offsetY));

            //Neue Mausposition setzen
            currentMousePosition = evt.getPoint();
            netPanel.repaint();
        } else if (selectedNodesResizing) {
            Point newMousePosition = evt.getPoint();

            List<NodePoint> drawnNodes = netData.getNodes();
            //Ausgewählte Knoten sammeln
            ArrayList<NodePoint> selectedNodes = new ArrayList<>(
                    drawnNodes.stream().filter(nodePoint -> nodePoint.selected).collect(Collectors.toList()));

            double factorX = 0;
            double factorY = 0;
            if (resizeBorder == 1 || resizeBorder == 5 || resizeBorder == 8) {
                //links, links oben, links unten
                double distance = currentMousePosition.getX() - newMousePosition.getX();

                distance = selectionRectangle.resizeLeft(distance);
                factorX = distance / selectionRectangle.width;
            }

            if (resizeBorder == 2 || resizeBorder == 5 || resizeBorder == 6) {
                //oben, oben links, oben rechts
                double distance = currentMousePosition.getY() - newMousePosition.getY();

                distance = selectionRectangle.resizeTop(distance);
                factorY = distance / selectionRectangle.height;
            }

            if (resizeBorder == 3 || resizeBorder == 6 || resizeBorder == 7) {
                //rechts, oben rechts, unten rechts
                double distance = newMousePosition.getX() - currentMousePosition.getX();

                distance = selectionRectangle.resizeRight(distance);
                factorX = distance / selectionRectangle.width;
            }

            if (resizeBorder == 4 || resizeBorder == 7 || resizeBorder == 8) {
                //unten, unten links, unten rechts
                double distance = newMousePosition.getY() - currentMousePosition.getY();

                distance = selectionRectangle.resizeBottom(distance);
                factorY = distance / selectionRectangle.height;
            }

            if (factorX != 0 || factorY != 0) {
                netData.resizeNodesNotFinal(selectedNodes, resizeBorder, factorX, factorY, selectionRectangle);
            }

            currentMousePosition = evt.getPoint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        if (newLineDragging || selectDragging || selectedNodesResizing)
            return;

        int x = mouseEvent.getX();
        int y = mouseEvent.getY();
        boolean consumed = false;

        /**
         * Prüfen, ob der Cursor auf einem markierten Bereich ist oder Nahe einer Kante davon ist
         */
        if (nodesSelected) {
            resizeBorder = selectionRectangle.getResizableBorder(x, y, RESIZE_DISTANCE);
            if (resizeBorder > 0) {
                cursorOnSelectionBorder = true;
                cursorInsideSelection = false;
                consumed = true;
                hoveredElement = null;
            } else if (selectionRectangle.contains(x, y)) {
                cursorInsideSelection = true;
                cursorOnSelectionBorder = false;
                consumed = true;
                hoveredElement = null;
            } else {
                cursorInsideSelection = false;
                cursorOnSelectionBorder = false;
            }
        } else {
            cursorOnSelectionBorder = false;
            cursorInsideSelection = false;
        }

        List<NodePoint> drawnNodes = netData.getNodes();
        List<EdgeLine> drawnEdges = netData.getEdges();

        /**
         * Prüfen, ob ein Knoten getroffen wird
         */
        if (!consumed) {
            for (NodePoint nodePoint : drawnNodes) {

                if (nodePoint.contains(x, y)) {
                    hoveredElement = nodePoint;
                    netPanel.repaint();
                    consumed = true;
                    break;
                }
            }
        }

        /**
         * Prüfen, ob der Cursor nahe einer Kante ist (nah ist hier maximal 5px Abstand)
         */
        if (!consumed) {
            for (EdgeLine edgeLine : drawnEdges) {

                if (edgeLine.ptSegDist(x, y) < EDGE_HOVER_DISTANCE) {
                    hoveredElement = edgeLine;
                    netPanel.repaint();
                    consumed = true;
                    break;
                }
            }
        }

        if (!consumed && hoveredElement != null) {
            //Cursor zurücksetzen, falls er auf keinem Element mehr ist
            hoveredElement = null;
            netPanel.repaint();
        }
        netPanel.setCursorHover(mouseEvent.isShiftDown(), mouseEvent.isControlDown());
    }

    /**
     * Wählt alle Knoten aus
     */
    private void selectAllNodes() {
        List<NodePoint> drawnNodes = netData.getNodes();
        selectNodes(drawnNodes);
    }

    /**
     * Wählt eine Menge an Knoten aus. Knoten können auch doppelt in nodesToSelect auftauchen, dies macht jedoch keinen
     * Unterschied.
     *
     * @param nodesToSelect die auszuwählenden Knoten
     */
    private void selectNodes(List<NodePoint> nodesToSelect) {
        //Ohne Knoten gibts nichts auszuwählen
        if (nodesToSelect.isEmpty())
            return;

        //Alle Knoten als ausgewählt markieren
        for (NodePoint node : nodesToSelect) {
            node.selected = true;
        }

        //Rechteck erstellen, dass mit 5 Pixel Abstand alle ausgewählten Knoten umschließt
        //Falls keine Knoten ausgewählt wurden, hat das Reckteck alle Parameter auf 0
        selectionRectangle = GraphUtil.getGraphBounds(nodesToSelect, 5);

        nodesSelected = true;
        //Timer (neu) starten (falls er bereits läuft)
        netPanel.selectionAnimationTimer.restart();
    }

    /**
     * Wählt alle Knoten aus, die einen anderen Knoten schneiden
     */
    private void selectOverlappingNodes() {
        List<NodePoint> nodes = new ArrayList<>(netData.getNodes());

        ArrayList<NodePoint> intersectingNodes = new ArrayList<>();

        //Alle Knotenkombinationen durchgehen
        for (int i1 = 0, nodesSize1 = nodes.size(); i1 < nodesSize1; i1++) {
            NodePoint node1 = nodes.get(i1);

            for (int i2 = 0, nodesSize2 = nodes.size(); i2 < nodesSize2; i2++) {
                NodePoint node2 = nodes.get(i2);

                if (i1 != i2 && (node1.intersects(node2.getFrame()) || node1.getBounds().equals(node2.getBounds()))) {
                    //spielt durch die implementation von selectNodes keine Rolle, ob ein Knoten doppelt in der Liste ist
                    intersectingNodes.add(node2);

                    nodes.remove(i2);

                    nodesSize1--;
                    nodesSize2--;
                    i2--;

                    //damit ein Eintrag nicht doppelt überprüft wird
                    if (i1 > i2) {
                        i1--;
                    }
                }
            }
        }

        selectNodes(intersectingNodes);
    }

    /**
     * Fügt eine Menge von Knoten und Kanten hinzu.
     *
     * @param nodes Die Knotenmenge
     * @param edges Die Kantenmenge
     */
    public void addNodesAndEdges(List<NodePoint> nodes, List<EdgeLine> edges) {
        netData.addNodesAndEdges(nodes, edges);
    }

    /**
     * Fügt eine Menge von Knoten und Kanten hinzu und wählt diese aus
     *
     * @param graphWrapper Wrapper mit Mengen von Knoten und Kanten
     */
    public void addGraphWrapperAndSelect(GraphWrapper graphWrapper) {
        netData.addNodesAndEdges(graphWrapper.nodes, graphWrapper.edges);

        //Eingefügte Knoten auswählen
        graphWrapper.nodes.forEach((nodePoint -> nodePoint.selected = true));
        selectionRectangle = GraphUtil.getGraphBounds(graphWrapper.nodes, 5);
        nodesSelected = true;
        netPanel.selectionAnimationTimer.restart();

        //Scrollbar refreshen
        revalidateScrollPane();
        netPanel.repaint();
    }

    //Ein paar Getter- und Setter-Methoden

    public boolean isCursorInsideSelection() {
        return cursorInsideSelection;
    }

    public boolean isCursorOnSelectionBorder() {
        return cursorOnSelectionBorder;
    }

    public int getResizeBorder() {
        return resizeBorder;
    }

    public List<NodePoint> getNodes() {
        return netData.getNodes();
    }

    public List<EdgeLine> getEdges() {
        return netData.getEdges();
    }

    public boolean isNodeClickable() {
        return nodeClickable;
    }

    public boolean isEdgeClickable() {
        return edgeClickable;
    }

    public void setClickableElements(boolean nodeClickable, boolean edgeClickable) {
        this.nodeClickable = nodeClickable;
        this.edgeClickable = edgeClickable;
    }

    public Point getCurrentMousePosition() {
        return currentMousePosition;
    }

    public Shape getHoveredElement() {
        return hoveredElement;
    }

    public EdgeLine getDraggingLine() {
        return draggingLine;
    }

    public boolean isNewLineDragging() {
        return newLineDragging;
    }

    public boolean isSelectDragging() {
        return selectDragging;
    }

    public Point getSelectStartPoint() {
        return selectStartPoint;
    }

    public boolean isNodesSelected() {
        return nodesSelected;
    }

    public Rectangle2D getSelectionRectangle() {
        return selectionRectangle;
    }

    public NetPanelData getNetData() {
        return netData;
    }
}
