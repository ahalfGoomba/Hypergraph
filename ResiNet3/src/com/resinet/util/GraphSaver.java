package com.resinet.util;

import com.resinet.model.BorderRectangle;
import com.resinet.model.CalculationParams;
import com.resinet.model.EdgeLine;
import com.resinet.model.HyperEdgePoint;
import com.resinet.model.NodePoint;
import com.resinet.views.NetPanel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse enthält nur statische Methoden, mit denen Netzwerke und dessen Berechnungsparameter in Dateien
 * gespeichtert werden können.
 * <p>
 * Dabei wurde mit ResiNet3 ein neues und eigenes Dateiformat basierend auf XML eingeführt. Des weiteren können auch
 * Graphen im Pajek-Format eingelesen und gespeichert werden.
 */
@SuppressWarnings("HardCodedStringLiteral")
public final class GraphSaver {

    /**
     * Methode zum Einlesen von Netzen aus Textdateien im Pajek-Format.
     *
     * @param dialogParentComponent Eine GUI-Komponente, damit Dialoge angezeigt werden können
     * @param graphWidth            Graphbreite
     * @param graphHeight           Graphhöhe
     * @return CalculationParams-Objekt mit den eingelesenen Daten, oder null
     */
    public static CalculationParams inputNet(Component dialogParentComponent, int graphWidth, int graphHeight) {
        //Dialog zum Datei auswählen
        JFileChooser chooseFile = new JFileChooser();
        chooseFile.setDialogTitle(Strings.getLocalizedString("open.file"));

        //Dateifilter für Resinet- und Pajek-Netzwerke einfpgen
        FileNameExtensionFilter resinetFilter = new FileNameExtensionFilter(Strings.getLocalizedString("resinetv.networks"), "resinet");
        FileNameExtensionFilter pajekFilter = new FileNameExtensionFilter(Strings.getLocalizedString("pajek.networks"), "txt", "net");
        chooseFile.setFileFilter(resinetFilter);
        chooseFile.addChoosableFileFilter(pajekFilter);

        int state = chooseFile.showOpenDialog(null);
        File netFile;
        //Falls abgebrochen wurde, auch hier abbrechen
        if (state == JFileChooser.APPROVE_OPTION) {
            netFile = chooseFile.getSelectedFile();
        } else {
            return null;
        }

        if (resinetFilter.accept(netFile)) {
            return readResinetNetwork(netFile, dialogParentComponent);
        } else if (pajekFilter.accept(netFile)) {
            return readPajekNetwork(netFile, dialogParentComponent, graphWidth, graphHeight);
            //resinet3.updateSingleReliabilityProbPanel();
        } else {
            JOptionPane.showMessageDialog(dialogParentComponent, Strings.getLocalizedString("selected.file.hash.unknown.extension"), Strings.getLocalizedString("failed"), JOptionPane.ERROR_MESSAGE);
        }
        SwingUtilities.invokeLater(() -> {
            //resinet3.netPanel.centerGraphOnNextPaint();
            //resinet3.netPanel.repaint();
        });
        return null;
    }

    /**
     * Liest ein Netzwerk im Pajek-Format ein und lässt es im NetPanel darstellen
     *
     * @param netFile Die Pajek-Netzwerk-Datei
     */
    private static CalculationParams readPajekNetwork(File netFile, Component parentComponent, int width, int height) {
        ArrayList<NodePoint> nodeList = new ArrayList<>();
        ArrayList<EdgeLine> edgeList = new ArrayList<>();
        ArrayList<HyperEdgePoint> hyperEdgePointList = new ArrayList<>();
        //Ab hier zeilenweises Einlesen der ausgewählten Datei
        String actRow;
        LineNumberReader lineReader;

        try {
            lineReader = new LineNumberReader(new FileReader(netFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        try {
            actRow = lineReader.readLine();
            actRow = actRow.substring(10);
            int nodesCount = Integer.parseInt(actRow);
            int panelHeight = height - 20;
            int panelWidth = width - 20;

            //Erzeuge Knoten
            for (int i = 0; i < nodesCount; i++) {
                actRow = lineReader.readLine();

                //Hole x-Koordinate
                int position = actRow.indexOf('.');

                Double xCoordinate = Double.parseDouble(actRow.substring(position - 1, position + 5));

                //Hole y-Koordinate
                Double yCoordinate = Double.parseDouble(actRow.substring(position + 10, position + 16));

                NodePoint node1 = new NodePoint(xCoordinate * panelWidth, yCoordinate * panelHeight, false);
                nodeList.add(node1);
            }

            //Zeile überspringen: *Arcs oder *Edges
            lineReader.readLine();

            //Lies Kanten aus
            while (lineReader.ready()) {
                actRow = lineReader.readLine();

                //Achtung, Bei Leerzeile wird abgebrochen!
                if (actRow == null || actRow.length() == 0) {
                    System.out.println("Leerzeile in der Quelldatei. Lesevorgang abgebrochen.");
                    return null;
                }

                //Startknoten
                String startnode = "";
                int position = 0;

                //Gehe in der Zeile nach rechts bis zur ersten Ziffer
                while (actRow.charAt(position) == ' ') {
                    position++;
                }

                //Hole Startknoten
                while (actRow.charAt(position) != ' ') {
                    startnode += actRow.charAt(position);
                    position++;
                }

                //Endknoten
                String endnode = "";

                //Gehe in der Zeile nach rechts bis zur ersten Ziffer
                while (actRow.charAt(position) == ' ') {
                    position++;
                }

                //Hole Endknoten
                while (actRow.charAt(position) != ' ') {
                    endnode += actRow.charAt(position);
                    position++;
                }

                //Füge aktuelle Kante inkl. Start- und Endknoten hinzu
                Integer node1 = Integer.parseInt(startnode) - 1;
                Integer node2 = Integer.parseInt(endnode) - 1;

                NodePoint startNodePoint = nodeList.get(node1);
                NodePoint endNodePoint = nodeList.get(node2);

                EdgeLine edge1 = new EdgeLine(startNodePoint, endNodePoint);

                edgeList.add(edge1);
            }
            CalculationParams result = new CalculationParams();
            result.setGraphLists(nodeList, edgeList, hyperEdgePointList);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            inputError(parentComponent);
        }
        return null;
    }

    /**
     * Liest ein Resinet-Netzwerk ein und überträgt alle Daten in die GUI
     *
     * @param netFile Die Resinet-Netzwerk-Datei
     */
    private static CalculationParams readResinetNetwork(File netFile, Component parentComponent) {
    	CalculationParams calculationParams = new CalculationParams();
    	boolean hyperGraphMode = false;
    	ArrayList<NodePoint> drawnNodes = new ArrayList<>();
        ArrayList<EdgeLine> drawnEdges = new ArrayList<>();
        ArrayList<HyperEdgePoint> drawnHyperEdgePoints = new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(netFile);

            //Normalisieren (hier wahrscheinlich unnötig, aber kann Fehler vermeiden)
            doc.getDocumentElement().normalize();

            
            //Hypergraph status einlesen
            NodeList stateList = doc.getElementsByTagName("state");
            	Element state = (Element) stateList.item(0);
            	hyperGraphMode = Boolean.parseBoolean(state.getAttribute("hyperEdgeState"));
            	
            calculationParams.hypergraphMode = hyperGraphMode;            
            	
            	
            //Knotern einlesen
            NodeList nodeList = doc.getElementsByTagName("node");
            for (Integer i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element nodeElement = (Element) node;
                    //Nummer und Koordinaten einlesen
                    Integer position = Integer.parseInt(nodeElement.getAttribute("node_number"));
                    int x = Integer.parseInt(nodeElement.getAttribute("x"));
                    int y = Integer.parseInt(nodeElement.getAttribute("y"));
                    boolean c_node = Boolean.parseBoolean(nodeElement.getAttribute("c_node"));

                    NodePoint np = new NodePoint(x, y, c_node);
                    //An der richtigen Position einfügen
                    drawnNodes.add(position, np);
                }
            }

            //Kanten einlesen
            nodeList = doc.getElementsByTagName("edge");
            for (Integer i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element nodeElement = (Element) node;
                    //Anliegende Knoten bestimmen
                    Integer node1Number = Integer.parseInt(nodeElement.getAttribute("node1"));
                    Integer node2Number = Integer.parseInt(nodeElement.getAttribute("node2"));
                    NodePoint node1 = drawnNodes.get(node1Number);
                    NodePoint node2 = drawnNodes.get(node2Number);

                    EdgeLine el = new EdgeLine(node1, node2);

                    //An der richtigen Position einfügen
                    Integer position = Integer.parseInt(nodeElement.getAttribute("edge_number"));
                    drawnEdges.add(position, el);
                }
            }
            
            //hyperEdgePoints einlesen
            
            
            nodeList = doc.getElementsByTagName("hyperEdgePoint");
            for (Integer i = 0; i < nodeList.getLength(); i++) {
            Node heNode = nodeList.item(i);
            Element ele = (Element) heNode;
            int x = Integer.parseInt(ele.getAttribute("x"));
            int y = Integer.parseInt(ele.getAttribute("y"));
            
            List<NodePoint> heNodeList = new ArrayList<NodePoint>();
            
            int nodeCounter = Integer.parseInt(ele.getAttribute("nodeCount"));
            for(int t = 0; t < nodeCounter; t++){
            	NodePoint npTemp = drawnNodes.get(Integer.parseInt(ele.getAttribute("node"+t)));
            	heNodeList.add(npTemp);
            }
            

            HyperEdgePoint hep = new HyperEdgePoint(x, y);
            hep.setNodePointList(heNodeList);
            drawnHyperEdgePoints.add(hep);

            }
           
            //Zuverlässigkeiten einlesen
            if(!hyperGraphMode){
            //Modus einlesen
            nodeList = doc.getElementsByTagName("reliabilityMode");
            Node reliabilityModeNode = nodeList.item(0);
            Element reliabilityModeNodeElement = (Element) reliabilityModeNode;
            Boolean sameReliabilityMode = Boolean.parseBoolean(reliabilityModeNodeElement.getAttribute("same_reliability"));
            calculationParams.setReliabilityMode(sameReliabilityMode);

            if (sameReliabilityMode) {
                nodeList = doc.getElementsByTagName("seriesParam");
                //Parameter für die Serienberechnung und den Modus für gleiche Wahrscheinlichkeiten einlesen
                for (Integer i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element nodeElement = (Element) node;

                        switch (nodeElement.getAttribute("type")) {
                            case "nodeStart":
                                calculationParams.nodeStartValue = new BigDecimal(nodeElement.getAttribute("value"));
                                calculationParams.nodeValue = calculationParams.nodeStartValue;
                                break;
                            case "nodeEnd":
                                calculationParams.nodeEndValue = new BigDecimal(nodeElement.getAttribute("value"));
                                break;
                            case "nodeStepSize":
                                calculationParams.nodeStepSize = new BigDecimal(nodeElement.getAttribute("value"));
                                break;
                            case "edgeStart":
                                calculationParams.edgeStartValue = new BigDecimal(nodeElement.getAttribute("value"));
                                calculationParams.edgeValue = calculationParams.edgeStartValue;
                                break;
                            case "edgeEnd":
                                calculationParams.edgeEndValue = new BigDecimal(nodeElement.getAttribute("value"));
                                break;
                            case "edgeStepSize":
                                calculationParams.edgeStepSize = new BigDecimal(nodeElement.getAttribute("value"));
                                break;
                            case "terminalValue":
                                calculationParams.setDifferentTerminalNodeReliability(
                                        new BigDecimal(nodeElement.getAttribute("value")));
                                break;
                        }
                    }
                }
                calculationParams.probabilitiesLoaded = true;
                //Falls Berechnungsserienparameter vorliegen, müssen alle abgespeichert worden sein,
                //also reicht es hier, einen Parameter zu prüfen
                if (calculationParams.edgeEndValue != null) {
                    calculationParams.calculationSeries = true;
                }
            } else {
                nodeList = doc.getElementsByTagName("singleReliability");

                int nodeCount = nodeList.getLength();
                //Arraylisten mit der größe der gesamten Liste initialisieren, damit mit Sicherheit alle reinpassen
                ArrayList<BigDecimal> edgeProbabilities = new ArrayList<>(nodeCount),
                        nodeProbabilities = new ArrayList<>(nodeCount);

                for (Integer i = 0; i < nodeCount; i++) {
                    Node node = nodeList.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element nodeElement = (Element) node;
                        Integer position = Integer.parseInt(nodeElement.getAttribute("number"));
                        BigDecimal reliability = new BigDecimal(nodeElement.getAttribute("reliability"));

                        if (nodeElement.getAttribute("type").equals("node")) {
                            nodeProbabilities.add(position, reliability);
                        } else {
                            edgeProbabilities.add(position, reliability);
                        }
                    }
                }
                //Vector in primitive Arrays umwandeln und dann in das Objekt einspeisen
                calculationParams.setSingleReliabilityParams(edgeProbabilities.toArray(new BigDecimal[edgeProbabilities.size()]),
                        nodeProbabilities.toArray(new BigDecimal[nodeProbabilities.size()]));

            }
            }
            calculationParams.setGraphLists(drawnNodes, drawnEdges, drawnHyperEdgePoints);
            return calculationParams;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            inputError(parentComponent);
        }
        return null;
    }

    /**
     * Zeigt eine Fehlermeldung mit einem Text an, der aussagt, dass die Eingabedatei nicht korrekt ist.
     *
     * @param parentComponent Irgendeine UI-Komponente als Referenz für das Popup-Fenster
     */
    private static void inputError(Component parentComponent) {
        //Error-Popup ausgeben
        String str = Strings.getLocalizedString("invalid.selected.file");

        JOptionPane.showMessageDialog(parentComponent, str, Strings.getLocalizedString("error"), JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Zeigt einen JFileChooser-Dialog an und speichert das aktuelle Netzwerk im gewünschten Format.
     *
     * @param params   Die zu speichernden Daten
     * @param netPanel Das NetPanel
     */
    public static void exportNet(CalculationParams params, NetPanel netPanel, int graphWidth, int graphHeight) {
        //Dialog zum Datei auswählen
        JFileChooser chooseSaveFile = new JFileChooser();
        chooseSaveFile.setDialogType(JFileChooser.SAVE_DIALOG);

        //Speichern in zwei Formaten anbieten
        FileNameExtensionFilter pajekFilter = new FileNameExtensionFilter(Strings.getLocalizedString("pajek.networks"), "net");
        FileNameExtensionFilter resinetvFilter = new FileNameExtensionFilter(Strings.getLocalizedString("resinetv.networks"), "resinet");
        FileNameExtensionFilter imageFilter = new FileNameExtensionFilter("Portable Network Graphics", "png");
        chooseSaveFile.setFileFilter(resinetvFilter);
        chooseSaveFile.addChoosableFileFilter(pajekFilter);
        chooseSaveFile.addChoosableFileFilter(imageFilter);
        chooseSaveFile.setDialogTitle(Strings.getLocalizedString("save.network.as"));
        chooseSaveFile.setSelectedFile(new File("myNetwork"));

        File saveNetFile;
        String path;

        int state = chooseSaveFile.showSaveDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            path = chooseSaveFile.getSelectedFile().toString();
            saveNetFile = new File(path);

            if (pajekFilter.accept(saveNetFile)) {
                writePajekNetwork(path, params, netPanel, graphWidth, graphHeight);
            } else if (resinetvFilter.accept(saveNetFile)) {
                writeResinetNetwork(path, params, netPanel);
            } else if (imageFilter.accept(saveNetFile)) {
                writeImage(path, netPanel);
            } else {
                //Dateierweiterung fehlt wohl
                //Ausgewählten Filter finden
                javax.swing.filechooser.FileFilter selectedFilter = chooseSaveFile.getFileFilter();

                //entsprechende Dateierweiterung an den Pfad anfügen und dann so speichern
                if (selectedFilter.equals(pajekFilter)) {
                    writePajekNetwork(path + ".net", params, netPanel, graphWidth, graphHeight);
                } else if (selectedFilter.equals(resinetvFilter)) {
                    writeResinetNetwork(path + ".resinet", params, netPanel);
                } else if (selectedFilter.equals(imageFilter)) {
                    writeImage(path + ".png", netPanel);
                }
            }
        }
    }

    /**
     * Schreibt das Netzwerk in ein Pajek-kompatibles Dateiformat
     *
     * @param path Der Zielpfad
     */
    private static void writePajekNetwork(String path, CalculationParams params, Component parentComponent, int graphWidth, int graphHeight) {
        List<NodePoint> nodeList = params.graphNodes;
        List<EdgeLine> edgeList = params.graphEdges;

        //Ab hier in die Datei schreiben
        Writer writer;

        try {
            writer = new FileWriter(path);
            writer.write("*Vertices " + nodeList.size());


            int nodesDigitsCount = String.valueOf(nodeList.size()).length();

            //Für jeden Knoten eine Zeile schreiben
            for (int i = 1; i < nodeList.size() + 1; i++) {
                writer.append(System.getProperty("line.separator"));

                int digitsCurrentNode = String.valueOf(i).length();
                int addSpaces = nodesDigitsCount - digitsCurrentNode;
                int spacesLength = 0;

                //Leerzeichen vorne auffüllen
                for (int j = 0; j < addSpaces + 1; j++) {
                    writer.write(" ");
                    spacesLength++;
                }

                //Schreibe Knotennummer
                String nodesNumber = Integer.toString(i) + " \"v" + Integer.toString(i) + "\"";
                writer.write(nodesNumber);

                //Nochmal so viele Leerzeichen auffüllen bis Position 46 erreicht ist
                for (int k = 0; k < (46 - spacesLength - nodesNumber.length()); k++) {
                    writer.write(" ");
                }

                NodePoint node = nodeList.get(i - 1);
                double xCoordinate = node.x;
                double yCoordinate = node.y;

                if (xCoordinate < 5) {
                    xCoordinate = 5;
                }
                if (yCoordinate < 5) {
                    yCoordinate = 5;
                }

                xCoordinate = xCoordinate / (double) (graphWidth);
                yCoordinate = yCoordinate / (double) (graphHeight);

                // Auf 4 Nachkommastellen runden
                xCoordinate = Math.round(xCoordinate * 10000.0) / 10000.0;
                yCoordinate = Math.round(yCoordinate * 10000.0) / 10000.0;

                String xCoordinateString = Double.toString(xCoordinate);
                String yCoordinateString = Double.toString(yCoordinate);

                // Stellen auffüllen, z.b. 0.25 => 0.2500
                while (xCoordinateString.length() < 6) {
                    xCoordinateString += "0";
                }

                while (yCoordinateString.length() < 6) {
                    yCoordinateString += "0";
                }

                //Schreibe Koordinaten in die Datei
                writer.write(xCoordinateString + "    " + yCoordinateString + "    0.5000");
            }

            writer.append(System.getProperty("line.separator"));
            writer.write("*Edges");

            //Für jede Kante eine Zeile
            for (EdgeLine edge : edgeList) {
                writer.append(System.getProperty("line.separator"));
                String node1 = Integer.toString(nodeList.indexOf(edge.startNode) + 1);
                String node2 = Integer.toString(nodeList.indexOf(edge.endNode) + 1);

                while (node1.length() < Integer.toString(nodeList.size()).length() + 1) {
                    node1 = " " + node1;
                }

                while (node2.length() < Integer.toString(nodeList.size()).length() + 1) {
                    node2 = " " + node2;
                }
                writer.write(node1 + " " + node2 + " 1");
            }
            writer.close();

            JOptionPane.showMessageDialog(parentComponent, Strings.getLocalizedString("successfully.saved"));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentComponent, Strings.getLocalizedString("saving.failed"), Strings.getLocalizedString("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Speichert den Graphen und alle Parameter in einem eigenen XML-Format
     *
     * @param path Der Zieldateipfad
     */
    private static void writeResinetNetwork(String path, CalculationParams params, Component parentComponent) {

        if (params == null) {
            //Voraussetzungen zum Speichern sind nicht erfüllt --> Abbruch

            JOptionPane.showMessageDialog(parentComponent,
                    Strings.getLocalizedString("values.missing.for.saving.text")
                    , Strings.getLocalizedString("error"), JOptionPane.ERROR_MESSAGE);

            return;
        }

        List<NodePoint> drawnNodes = params.graphNodes;
        List<EdgeLine> drawnEdges = params.graphEdges;
        List<HyperEdgePoint> drawnHEPs = params.graphHEPs; 

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("graph");
            doc.appendChild(rootElement);

            /**
             * Elemente ganz links und ganz oben bestimmen, damit deren x bzw. y-Koordinate als Offset verwendet werden kann.
             * Das heißt, dass alle Elemente um x nach links und y nach oben verschoben werden, damit der Graph nach
             * dem Laden richtig zentriert werden kann.
             */
            Rectangle2D graphRect = GraphUtil.getGraphBounds(drawnNodes, drawnHEPs);

            //Breite und Höhe schreiben
            Element sizeNode = doc.createElement("size");
            sizeNode.setAttribute("width", Integer.toString((int) graphRect.getWidth()));
            sizeNode.setAttribute("height", Integer.toString((int) graphRect.getHeight()));
            rootElement.appendChild(sizeNode);

            //HyperGraph status schreiben
            Element state = doc.createElement("state");
            	state.setAttribute("hyperEdgeState", Boolean.toString(params.hypergraphMode));
            	//System.out.println(state);
            	rootElement.appendChild(state);
            	
            //Knoten schreiben
            for (Integer i = 0; i < drawnNodes.size(); i++) {
                NodePoint graphNode = drawnNodes.get(i);
                graphNode.saveNum = i;
                Element node = doc.createElement("node");
                node.setAttribute("node_number", i.toString());
                node.setAttribute("x", Integer.toString((int) (graphNode.x - graphRect.getX())));
                node.setAttribute("y", Integer.toString((int) (graphNode.y - graphRect.getY())));
                node.setAttribute("c_node", Boolean.toString(graphNode.c_node));

                rootElement.appendChild(node);
            }


            //Kanten schreiben
            for (Integer i = 0; i < drawnEdges.size(); i++) {
                EdgeLine graphEdge = drawnEdges.get(i);

                Element edge = doc.createElement("edge");
                edge.setAttribute("edge_number", i.toString());
                edge.setAttribute("node1", Integer.toString(drawnNodes.indexOf(graphEdge.startNode)));
                edge.setAttribute("node2", Integer.toString(drawnNodes.indexOf(graphEdge.endNode)));

                rootElement.appendChild(edge);
            }
            
            //HyperKantenPunkte schreiben
            for (int i = 0; i < drawnHEPs.size(); i++) {
                HyperEdgePoint graphHyperEdgePoint = drawnHEPs.get(i);

                Element hyperEdgePoint = doc.createElement("hyperEdgePoint");
                
                hyperEdgePoint.setAttribute("nodeCount", Integer.toString(graphHyperEdgePoint.getNodePoints().size()));
                int counter = 0;
                for(NodePoint np : graphHyperEdgePoint.getNodePoints()){
                hyperEdgePoint.setAttribute("node" + counter, Integer.toString(np.saveNum));
                counter++;
                }
                hyperEdgePoint.setAttribute("x", Integer.toString((int) (graphHyperEdgePoint.x - graphRect.getX())));
                hyperEdgePoint.setAttribute("y", Integer.toString((int) (graphHyperEdgePoint.y - graphRect.getY())));

                rootElement.appendChild(hyperEdgePoint);
            }

            if(!params.graph.hyperGraph){
            //Wahrscheinlichkeiten schreiben
            Element reliabilityMode = doc.createElement("reliabilityMode");
            reliabilityMode.setAttribute("same_reliability", Boolean.toString(params.sameReliabilityMode));
            rootElement.appendChild(reliabilityMode);

            if (params.sameReliabilityMode) {
                //Startwahrscheinlichkeiten speichern
                Element nodeStartValue = doc.createElement("seriesParam");
                nodeStartValue.setAttribute("type", "nodeStart");
                nodeStartValue.setAttribute("value", params.nodeValue.toString());
                rootElement.appendChild(nodeStartValue);

                Element edgeStartValue = doc.createElement("seriesParam");
                edgeStartValue.setAttribute("type", "edgeStart");
                edgeStartValue.setAttribute("value", params.edgeValue.toString());
                rootElement.appendChild(edgeStartValue);

                if (params.differentTerminalNodeReliability) {
                    Element terminalValue = doc.createElement("seriesParam");
                    terminalValue.setAttribute("type", "terminalValue");
                    terminalValue.setAttribute("value", params.terminalNodeValue.toString());
                    rootElement.appendChild(terminalValue);
                }

                if (params.calculationSeries) {
                    //Serienberechnungsparameter speichern
                    Element nodeEndValue = doc.createElement("seriesParam");
                    nodeEndValue.setAttribute("type", "nodeEnd");
                    nodeEndValue.setAttribute("value", params.nodeEndValue.toString());
                    rootElement.appendChild(nodeEndValue);

                    Element nodeStepSizeValue = doc.createElement("seriesParam");
                    nodeStepSizeValue.setAttribute("type", "nodeStepSize");
                    nodeStepSizeValue.setAttribute("value", params.nodeStepSize.toString());
                    rootElement.appendChild(nodeStepSizeValue);


                    Element edgeEndValue = doc.createElement("seriesParam");
                    edgeEndValue.setAttribute("type", "edgeEnd");
                    edgeEndValue.setAttribute("value", params.edgeEndValue.toString());
                    rootElement.appendChild(edgeEndValue);

                    Element edgeStepSizeValue = doc.createElement("seriesParam");
                    edgeStepSizeValue.setAttribute("type", "edgeStepSize");
                    edgeStepSizeValue.setAttribute("value", params.edgeStepSize.toString());
                    rootElement.appendChild(edgeStepSizeValue);
                }
            } else {
                //Einzelwahrscheinlichkeitsmodus

                //Knotenwahrscheinlichkeiten
                for (Integer i = 0; i < params.nodeProbabilities.length; i++) {
                    Element singleReliability = doc.createElement("singleReliability");
                    singleReliability.setAttribute("type", "node");
                    singleReliability.setAttribute("number", i.toString());
                    singleReliability.setAttribute("reliability", params.nodeProbabilities[i].toString());
                    rootElement.appendChild(singleReliability);
                }

                //Kantenwahrscheinlichkeiten
                for (Integer i = 0; i < params.edgeProbabilities.length; i++) {
                    Element singleReliability = doc.createElement("singleReliability");
                    singleReliability.setAttribute("type", "edge");
                    singleReliability.setAttribute("number", i.toString());
                    singleReliability.setAttribute("reliability", params.edgeProbabilities[i].toString());
                    rootElement.appendChild(singleReliability);
                }
            }
            }
            //In XML-Datei schreiben
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            Source source = new DOMSource(doc);
            Result result = new StreamResult(new File(path));

            transformer.transform(source, result);
            JOptionPane.showMessageDialog(parentComponent, Strings.getLocalizedString("successfully.saved"));

        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentComponent, Strings.getLocalizedString("saving.failed"),
                    Strings.getLocalizedString("error"), JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * Speichert den Graphen als Bild im PNG-Format
     *
     * @param path     Der Pfad zur Datei
     * @param netPanel Das NetPanel als Quelle
     */
    private static void writeImage(String path, NetPanel netPanel) {
        BufferedImage image = new BufferedImage(netPanel.getWidth(), netPanel.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

        //Auswahl zurücksetzen, damit diese nich mitgespeichert wird
        netPanel.getController().resetSelection();
        //vorher neu zeichnen, damit es nicht zu Anzeigebugs kommt
        netPanel.repaint();
        //Panel in BufferedImage zeichnen
        netPanel.print(image.createGraphics());

        //Bild auf den Graphen zuschneiden
        BorderRectangle bounds = GraphUtil.getGraphBounds(netPanel.getNodes(), netPanel.getHyperEdgePoints(), 2);
        image = image.getSubimage((int) bounds.getX(), (int) bounds.getY(), (int) bounds.getWidth(), (int) bounds.getHeight());

        try {
            //Bild speichern
            ImageIO.write(image, "png", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(netPanel, Strings.getLocalizedString("saving.failed"),
                    Strings.getLocalizedString("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

}
