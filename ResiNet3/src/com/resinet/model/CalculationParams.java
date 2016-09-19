package com.resinet.model;

import com.resinet.util.Constants;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dies ist eine Wrapper-Klasse für die Berechnungsparameter
 */
public class CalculationParams implements Constants {
    public final Graph graph;
    public boolean sameReliabilityMode = false;
    public boolean calculationSeries = false;
    public boolean probabilitiesLoaded = false;
    public boolean differentTerminalNodeReliability = false;
    public final CALCULATION_MODES calculationMode;
    public boolean hypergraphMode;

    //für die Serienberechnung
    public BigDecimal edgeStartValue, edgeEndValue, edgeStepSize, nodeStartValue, nodeEndValue, nodeStepSize;

    //Für Berechnung mit gleichen Wahrscheinlichkeiten für alle Komponenten
    public BigDecimal edgeValue, nodeValue, terminalNodeValue;

    //Für Berechnung mit einzelnen Wahrscheinlichkeiten
    public BigDecimal[] edgeProbabilities, nodeProbabilities;

    public List<NodePoint> graphNodes;
    public List<EdgeLine> graphEdges;
    public List<HyperEdgePoint> graphHEPs; 

    /**
     * Parameterloser Konstruktor zur Benutzung als Datenwrapper.
     */
    public CalculationParams() {
        //dummywerte
        graph = null;
        calculationMode = null;
    }

    /**
     * Erzeugt ein Objekt für die Berechnungsparameter
     *
     * @param mode  Der Berechnungsmodus (Resilienz oder Zuverlässigkeit)
     * @param graph Der Graph
     */
    public CalculationParams(CALCULATION_MODES mode, Graph graph) {
        this.calculationMode = mode;
        this.graph = graph;
    }

    /**
     * Setzt die Parameter für die Berechnung mit einzelnen Wahrscheinlichkeiten
     *
     * @param edgeProbabilities Array aus Kantenwahrscheinlichkeiten
     * @param nodeProbabilities Array aus Knotenwahrscheinlichkeiten
     */
    public void setSingleReliabilityParams(BigDecimal[] edgeProbabilities, BigDecimal[] nodeProbabilities) {
        this.edgeProbabilities = edgeProbabilities;
        this.nodeProbabilities = nodeProbabilities;
        probabilitiesLoaded = true;
    }

    /**
     * Setzt den Modus fest
     *
     * @param isSameReliabilityMode True, wenn alle Komponenten die selben Wahrscheinlichkeiten haben
     */
    public void setReliabilityMode(boolean isSameReliabilityMode) {
        sameReliabilityMode = isSameReliabilityMode;
    }

    /**
     * Setzt die Parameter für die Berechnung mit gleichen Wahrscheinlichkeiten
     *
     * @param edgeValue Wahrscheinlichkeit für alle Kanten
     * @param nodeValue Wahrscheinlichkeit für alle Knoten
     */
    public void setSameReliabilityParams(BigDecimal edgeValue, BigDecimal nodeValue) {
        this.edgeValue = edgeValue;
        this.nodeValue = nodeValue;
        probabilitiesLoaded = true;
    }

    /**
     * Setzt die Parameter für die Berechnung mit gleichn Wahrscheinlichkeiten
     *
     * @param terminalNodeValue Zuverlässigkeit für alle Terminalknoten
     */
    public void setDifferentTerminalNodeReliability(BigDecimal terminalNodeValue) {
        this.terminalNodeValue = terminalNodeValue;
        differentTerminalNodeReliability = true;
    }

    /**
     * Setzt die Parameter für die Serienberechnung
     *
     * @param edgeStartValue Der Startwert für die Kanten
     * @param edgeEndValue   Der Endwert für die Kanten
     * @param edgeStepSize   Die Schrittweiter für die Kanten
     * @param nodeStartValue Der Startwert für die Kanten
     * @param nodeEndValue   Der Endwert für die Kanten
     * @param nodeStepSize   Die Schrittweite für die Kanten
     */
    public void setSeriesParams(BigDecimal edgeStartValue, BigDecimal edgeEndValue, BigDecimal edgeStepSize,
                                BigDecimal nodeStartValue, BigDecimal nodeEndValue, BigDecimal nodeStepSize) {
        this.edgeValue = edgeStartValue;
        this.edgeStartValue = edgeStartValue;
        this.edgeEndValue = edgeEndValue;
        this.edgeStepSize = edgeStepSize;

        this.nodeValue = nodeStartValue;
        this.nodeStartValue = nodeStartValue;
        this.nodeEndValue = nodeEndValue;
        this.nodeStepSize = nodeStepSize;

        calculationSeries = true;
        probabilitiesLoaded = true;
    }

    /**
     * Setzt die Knoten- und Kantenliste für das Netpanel.
     *
     * @param nodes Die Knotenliste
     * @param edges Die Kantenliste
     */
    public void setGraphLists(List<NodePoint> nodes, List<EdgeLine> edges) {
        graphNodes = nodes;
        graphEdges = edges;
       
    }
}
