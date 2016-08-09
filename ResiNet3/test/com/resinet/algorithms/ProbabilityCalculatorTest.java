package com.resinet.algorithms;

import com.resinet.model.CalculationParams;
import com.resinet.model.Edge;
import com.resinet.model.Graph;
import com.resinet.model.Node;
import com.resinet.util.Constants;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class ProbabilityCalculatorTest implements Constants {
    private Graph graph;

    private static final int calculatedValuesScale = 15;
    private static final int otherValuesScale = 6;

    @Before
    public void setUp() {
        //Brückennetzwerk erstellen
        ArrayList<Node> nodeList = new ArrayList<>();
        ArrayList<Edge> edgeList = new ArrayList<>();
        Node node0 = new Node(0, true);
        Node node1 = new Node(1, false);
        Node node2 = new Node(2, true);
        Node node3 = new Node(3, false);

        Edge edge0 = new Edge(0, node0, node1);
        Edge edge1 = new Edge(1, node1, node2);
        Edge edge2 = new Edge(2, node0, node3);
        Edge edge3 = new Edge(3, node3, node2);
        Edge edge4 = new Edge(4, node1, node3);

        node0.add_Edge(edge0);
        node0.add_Edge(edge2);

        node1.add_Edge(edge0);
        node1.add_Edge(edge1);
        node1.add_Edge(edge4);

        node2.add_Edge(edge1);
        node2.add_Edge(edge3);

        node3.add_Edge(edge2);
        node3.add_Edge(edge3);
        node3.add_Edge(edge4);

        nodeList.add(node0);
        nodeList.add(node1);
        nodeList.add(node2);
        nodeList.add(node3);

        edgeList.add(edge0);
        edgeList.add(edge1);
        edgeList.add(edge2);
        edgeList.add(edge3);
        edgeList.add(edge4);
        graph = new Graph(nodeList, edgeList);
    }

    /**
     * Testet den Modus für gleiche Zuverlässigkeiten mit Knoten und Kanten
     */
    @Test
    public void testSameReliability() {
        CalculationParams params = new CalculationParams(CALCULATION_MODES.RELIABILITY, graph);
        params.setReliabilityMode(true);

        //Zuverlässigkeit aller Komponenten
        BigDecimal elementReliability = new BigDecimal(0.9);

        params.setSameReliabilityParams(elementReliability, elementReliability);

        ProbabilityCalculator calculator = ProbabilityCalculator.create(null, params);

        /**
         * Zuverlässigkeit des Graphen "von Hand" ausrechnen (nach den Zerlegungen von Heidtmann)
         * Ergebnisse nach Taschenrechner bei p=0.9 für alle Elemente:
         * n0 e0 n1 e1 n2 = 0,59049
         * n0 e2 n3 e3 n2  * (1- e0 n1 e1) = 0,59049 * (1-0,729) = 0,1600
         * n0 e0 n1 e4 n3 e3 n2 * (1- e1) * (1-e2) = 0,4782969 * 0.1 * 0.1 = 0,00478
         * n0 e2 n3 e4 n1 e1 n2 * (1-e0) * (1-e3) = 0,4782969 * 0,1 * 0,1 = 0,00478
         * Gesamt: 0.760078728
         */
        BigDecimal reliability = elementReliability.pow(5);
        reliability = reliability.add(elementReliability.pow(5).multiply(BigDecimal.ONE.subtract(elementReliability.pow(3))));
        reliability = reliability.add(elementReliability.pow(7).multiply((new BigDecimal(0.1)).pow(2)).multiply(new BigDecimal(2)));

        //Anzahl Nachkommastellen reduzieren
        BigDecimal calculatedReliability = calculator.getHeidtmannsReliability(false).setScale(calculatedValuesScale, BigDecimal.ROUND_HALF_DOWN);
        reliability = reliability.setScale(calculatedValuesScale, BigDecimal.ROUND_HALF_DOWN);

        assertEquals(calculatedReliability, reliability);
    }

    /**
     * Testet den Modus für gleiche Zuverlässigkeiten nur mit Kanten (d.h. Knoten sind perfekt, also Zuverlässigkeit 1)
     */
    @Test
    public void testSameReliabilityOnlyEdges() {
        CalculationParams params = new CalculationParams(CALCULATION_MODES.RELIABILITY, graph);
        params.setReliabilityMode(true);

        //Kantenzuverlässigkeit 0.9, Knotenzuverlässigkeit 1 für alle Elemente
        BigDecimal edgeReliability = new BigDecimal(0.9);
        BigDecimal nodeReliability = new BigDecimal(1);

        params.setSameReliabilityParams(edgeReliability, nodeReliability);

        ProbabilityCalculator calculator = ProbabilityCalculator.create(null, params);

        /**
         * Wie im oberen Test, wobei die Knotenzuverlässigkeiten ignoriert wurden
         */
        BigDecimal reliability = edgeReliability.pow(2);
        reliability = reliability.add(edgeReliability.pow(2).multiply(BigDecimal.ONE.subtract(edgeReliability.pow(2))));
        reliability = reliability.add(edgeReliability.pow(3).multiply((new BigDecimal(0.1)).pow(2)).multiply(new BigDecimal(2)));

        //Nachkommastellen reduzieren
        BigDecimal calculatedReliability = calculator.getHeidtmannsReliability(false).setScale(calculatedValuesScale, BigDecimal.ROUND_HALF_DOWN);
        reliability = reliability.setScale(calculatedValuesScale, BigDecimal.ROUND_HALF_DOWN);

        assertEquals(calculatedReliability, reliability);

        //Vergleich mit Ergebnis aus dem Resinet2-Applet
        assertEquals(new BigDecimal(0.988005).setScale(otherValuesScale, BigDecimal.ROUND_HALF_DOWN),
                calculator.getResilience(false).setScale(otherValuesScale, BigDecimal.ROUND_HALF_DOWN));
    }

    /**
     * Testet den Einzelwahrscheinlichkeitsmodus, jedoch auch mit gleichen Zuverlässigkeiten für alle Elemente
     */
    @Test
    public void testSingleReliabilityModeWithSameReliabilities() {
        CalculationParams params = new CalculationParams(CALCULATION_MODES.RELIABILITY, graph);
        params.setReliabilityMode(false);

        BigDecimal elementReliability = new BigDecimal(0.9);

        BigDecimal[] edgeProbabilities = new BigDecimal[5];
        BigDecimal[] nodeProbabilities = new BigDecimal[4];

        for (int i = 0; i < 4; i++) {
            edgeProbabilities[i] = elementReliability;
            nodeProbabilities[i] = elementReliability;
        }
        edgeProbabilities[4] = elementReliability;

        params.setSingleReliabilityParams(edgeProbabilities, nodeProbabilities);

        ProbabilityCalculator calculator = ProbabilityCalculator.create(null, params);

        //wie im test testSameReliability
        BigDecimal reliability = elementReliability.pow(5);
        reliability = reliability.add(elementReliability.pow(5).multiply(BigDecimal.ONE.subtract(elementReliability.pow(3))));
        reliability = reliability.add(elementReliability.pow(7).multiply((new BigDecimal(0.1)).pow(2)).multiply(new BigDecimal(2)));

        //Nachkommastellen reduzieren
        BigDecimal calculatedReliability = calculator.getHeidtmannsReliability(false).setScale(calculatedValuesScale, BigDecimal.ROUND_HALF_DOWN);
        reliability = reliability.setScale(calculatedValuesScale, BigDecimal.ROUND_HALF_DOWN);

        assertEquals(calculatedReliability, reliability);
    }

    /**
     * Testet den Einzelzuverlässigkeitsmodus mit verschiedenen Zuverlässigkeiten für alle Kanten.
     * Knoten werden nicht berücksichtigt.
     * Vergleichswerte stammen aus Resinet2-Applet
     */
    @Test
    public void testSingleReliabilityModeOnlyEdges() {
        CalculationParams params = new CalculationParams(CALCULATION_MODES.RELIABILITY, graph);
        params.setReliabilityMode(false);

        BigDecimal[] edgeProbabilities = new BigDecimal[5];
        BigDecimal[] nodeProbabilities = new BigDecimal[4];

        for (int i = 0; i < 4; i++) {
            nodeProbabilities[i] = BigDecimal.ONE;
        }

        edgeProbabilities[0] = new BigDecimal(0.9);
        edgeProbabilities[1] = new BigDecimal(0.8);
        edgeProbabilities[2] = new BigDecimal(0.7);
        edgeProbabilities[3] = new BigDecimal(0.6);
        edgeProbabilities[4] = new BigDecimal(0.5);

        params.setSingleReliabilityParams(edgeProbabilities, nodeProbabilities);

        ProbabilityCalculator calculator = ProbabilityCalculator.create(null, params);

        //Nachkommastellen reduzieren
        BigDecimal calculatedReliability = calculator.getHeidtmannsReliability(false).setScale(otherValuesScale, BigDecimal.ROUND_HALF_DOWN);
        assertEquals(new BigDecimal(0.865).setScale(otherValuesScale, BigDecimal.ROUND_HALF_DOWN), calculatedReliability);
        //Vergleichswerte aus Resinet2-Applet
        BigDecimal calculatedResilience = calculator.getResilience(false).setScale(otherValuesScale, BigDecimal.ROUND_HALF_DOWN);
        assertEquals(new BigDecimal(0.89649993).setScale(otherValuesScale, BigDecimal.ROUND_HALF_DOWN), calculatedResilience);
    }

}