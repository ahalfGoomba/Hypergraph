package com.resinet.algorithms;

import com.resinet.model.*;
import com.resinet.util.CalculationProgressListener;
import com.resinet.util.Constants;
import com.resinet.util.Strings;
import com.resinet.util.Util;
import com.sun.istack.internal.Nullable;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Diese Klasse führt die eigentlichen Zuverlässigkeitsberechnungen auf einem eigenen Thread durch
 */
public class ProbabilityCalculator extends Thread implements Constants {
    private final CalculationProgressListener listener;
    private final CalculationParams params;

    //Die Anzahl der Nachkommastellen, die der Output haben soll
    private static final Integer OUTPUT_PRECISION = 15;

    private Graph workingGraph;

    /**
     * Startet die Berechnungen
     */
    @Override
    public void run() {
        System.out.println("ProbabilityCalculator: " + Thread.currentThread().getName());
        if (params.calculationMode == CALCULATION_MODES.RELIABILITY) {
            //Zuverlässigkeit berechnen
            if (params.calculationSeries) {
                calculationSeries(CALCULATION_MODES.RELIABILITY);
            } else {
                getHeidtmannsReliability(true);
            }
        } else {
            //Resilienz berechnen
            if (params.calculationSeries) {
                calculationSeries(CALCULATION_MODES.RESILIENCE);
            } else {
                getResilience(true);
            }
        }
    }

    /**
     * Factory-Funktion, die ein Objekt erzeugt
     *
     * @param listener Das Objekt, dass über den Fortschritt informiert werden soll
     * @param params   Die Berechnungsparameter
     * @return Das neue ProbabilityCalculator-Objekt
     */
    public static ProbabilityCalculator create(@Nullable CalculationProgressListener listener, CalculationParams params) {
        return new ProbabilityCalculator(listener, params);
    }

    /**
     * Erzeugt ein neues Objekt und bereitet den Arbeitsgraphen vor. Der Konstruktor ist hier private; es macht aber
     * keinen Unterschied, wenn man die Factory-Funktion entfernt und diesen Konstruktor public macht.
     *
     * @param listener Das Objekt, dass über den Fortschritt informiert werden soll
     * @param params   Die Berechnungsparameter
     */
    private ProbabilityCalculator(CalculationProgressListener listener, CalculationParams params) {
        this.listener = listener;
        this.params = params;
        renewWorkingGraph();
        reassignProbabilities();
    }

    /**
     * Klont den Graphen aus dem Parameterobjekt, damit er nach jeder Berechnung "frisch" ist
     * <p>
     * Dies ist vorerst nötig, da beim Auffinden minimaler Pfade (Klasse Tree) der Graph verändert wird, es aber
     * notwendig ist, dass das selbe Objekt für die Berechnung verwendet wird, damit die Referenzen auf die Elemente
     * stimmen.
     */
    private void renewWorkingGraph() {
        try {
            workingGraph = (Graph) Util.serialClone(params.graph);
        } catch (IOException | ClassNotFoundException e1) {
            System.err.println(e1.toString());
        }
    }

    /**
     * Startet die Zerlegung des Graphen
     *
     * @return Das Zerleg-Objekt
     */
    private Zerleg getDecomposition() {
        Zerleg zer = new Zerleg(workingGraph);

        zer.start();
        try {
            zer.join();
        } catch (InterruptedException ignored) {
        }
        return zer;
    }

    /**
     * Startet die Berechnung der Zuverlässigkeit nach Heidtmann
     *
     * @param writeOutput Ob das Resultat als Ergebnis gemeldet werden soll
     * @return Die Zuverlässigkeit des Arbeitsgraphen
     */
    BigDecimal getHeidtmannsReliability(boolean writeOutput) {
        BigDecimal prob;

        reassignProbabilities();

        if (workingGraph.edgeList.size() == 1) {
            Edge e = workingGraph.getEdgelist().get(0);

            //Prüfe, ob die beiden Endknoten K-Knoten sind
            if ((e.left_node.c_node) && (e.right_node.c_node)) {
                prob = e.prob;
                prob = prob.multiply(e.left_node.prob);
                prob = prob.multiply(e.right_node.prob);
            } else
                prob = BigDecimal.ZERO;
            //reportResult("The reduced network contains only one edge.\nThe reliability of the network is:\nP=" + prob);

        } else {
            Zerleg zer = getDecomposition();

            //System.out.println("Laufzeit Heidtmann bis Zerlegung: " + ((new Date()).getTime() - startTime));
            prob = BigDecimal.ZERO;

            for (ArrayList<HashSet<GraphElement>> al : zer.hz) {

                //System.out.println("Neuer Pfad");

                HashSet<GraphElement> hs = al.get(0);

                BigDecimal p;
                //hs enthält hier anscheinend einen Pfad im Graphen zwischen den K-Knoten
                //Das erste hs ist der Hin-Pfad
                p = getPathProbability(hs);
                //Die weiteren Pfade entstehen beim Disjunktmachen und Invertieren
                for (int i = 1; i < al.size(); i++) {
                    HashSet<GraphElement> hs1 = al.get(i);
                    if (hs1.isEmpty())
                        continue;
                    p = p.multiply(BigDecimal.ONE.subtract(getPathProbability(hs1)));

                }
                //System.out.println("Wahrscheinlichkeit: " + p.toString());
                prob = prob.add(p);

            }
        }
        //long runningTime = new Date().getTime() - startTime;
        //System.out.println("Laufzeit Heidtmann: " + runningTime);
        //System.out.println("Prob. Heidtmann: " + prob);

        if (writeOutput) {
            BigDecimal output = prob;
            if (prob.scale() > OUTPUT_PRECISION) {
                output = prob.setScale(OUTPUT_PRECISION, BigDecimal.ROUND_HALF_DOWN);
            }
            reportResult(MessageFormat.format(Strings.getLocalizedString("result.reliability"), output.toPlainString()));
        }

        return prob;
    }


    /**
     * Hauptmethode, die den Algorithmus zur Berechnung der Resilienz beinhaltet.
     *
     * @param writeOutput True, wenn das Ergebnis als Statusupdate ausgegeben werden soll
     * @return Die Resilienz des Netzwerks
     */
    BigDecimal getResilience(boolean writeOutput) {
        long start = new Date().getTime();

        //Anzahl der Knoten
        int total_nodes = workingGraph.nodeList.size();

        //Anzahl der K-Knoten
        int c_nodes = 0;

        // Sicherung der K-Knotenliste
        String cNodeList = "";
        for (int i = 0; i < total_nodes; i++) {
            Node nodeSave = workingGraph.nodeList.get(i);
            if (nodeSave.c_node) {
                c_nodes++;
                cNodeList += "1";
            } else {
                cNodeList += "0";
            }
        }

        // Berechne Anzahl der Kombinationen
        BigInteger combinationCount = Util.binomial(total_nodes, c_nodes);

        //Wenn keine Berechnungsserie, dann Schrittzahl mitteilen
        if (!params.calculationSeries) {
            reportStepCount(combinationCount.intValue());
        }

        // Erzeuge leere Menge für die Knotenmengen.
        Set<HashSet<Integer>> combinationSet = new HashSet<>();

        //Hier werden alle Kombinationen an Binärstrings erzeugt, die k Einsen und n-k Nullen haben
        Set<String> combinationStrings = generateCombinations(cNodeList);

        for (String binary : combinationStrings) {
            // Erzeuge neue Teilmenge
            HashSet<Integer> subset = new HashSet<>();

            // Für jeden Knoten: Wenn in der Binärzahl an der Stelle j eine 1 steht, füge den Knoten j der Teilmenge hinzu.
            for (int j = 0; j < total_nodes; j++) {
                if (binary.charAt(j) == '1') {
                    subset.add(j);
                }
            }
            combinationSet.add(subset);
        }

        int counter = 0;
        BigDecimal result = BigDecimal.ZERO;

        // Für jede Kombination der K-Knoten
        for (HashSet<Integer> combination : combinationSet) {
            renewWorkingGraph();

            // Für jeden Knoten: Falls er in der aktuellen Kombination enthalten ist, setze ihn auf "K-Knoten".
            for (int i = 0; i < total_nodes; i++) {
                // Entsprechenden Knoten holen
                Node node1 = workingGraph.nodeList.get(i);

                // Dann auf true, falls K-Knoten
                node1.c_node = combination.contains(i);

                // Schreibe jeden Knoten neu in die Knotenliste. ob das sinnvoll ist, weiß ich nicht, da die Referenz trotzdem drinne ist.
                workingGraph.nodeList.set(i, node1);
            }

            // Erhöhe pro Kombination den Zähler um 1.
            counter++;

            // Wahrscheinlichkeiten neu zuordnen.
            reassignProbabilities();

            //nur wenn keine Serienberechnung, dann Fortschritt angeben
            if (!params.calculationSeries) {
                reportProgressChange(counter);
            }
            //reportResult("Step " + counter + " of " + combinationCount);

            // Berechne die Zuverlässigkeit für die aktuelle Kombination und addiere sie zur bisherigen Summe.
            result = result.add(getHeidtmannsReliability(false));
        }

        // Teile die Summe der Zuverlässigkeiten durch die Anzahl der Kombinationen.
        result = result.divide(new BigDecimal(combinationCount), BigDecimal.ROUND_HALF_EVEN);

        long runningTime = new Date().getTime() - start;
        System.out.println("Laufzeit Resilienz: " + runningTime);

        if (writeOutput) {
            BigDecimal output = result;
            if (result.scale() > OUTPUT_PRECISION) {
                output = result.setScale(OUTPUT_PRECISION, BigDecimal.ROUND_HALF_DOWN);
            }
            reportResult(MessageFormat.format(Strings.getLocalizedString("result.resilience"), total_nodes, c_nodes, combinationCount, output.toPlainString()));
        }

        return result;
    }


    /**
     * Hilfsmethode zum Erzeugen aller Kombinationen von K-Knoten
     *
     * @param inputString Bisherige Kombinationen?
     * @return Alle möglichen Kombinationen
     */
    private static Set<String> generateCombinations(String inputString) {
        Set<String> combinationsSet = new HashSet<>();
        if (inputString.length() == 0)
            return combinationsSet;

        Character c = inputString.charAt(0);

        if (inputString.length() > 1) {
            inputString = inputString.substring(1);

            Set<String> permSet = generateCombinations(inputString);

            for (String s : permSet) {
                for (int i = 0; i <= s.length(); i++) {
                    combinationsSet.add(s.substring(0, i) + c + s.substring(i));
                }
            }
        } else {
            combinationsSet.add(String.valueOf(c));
        }
        return combinationsSet;
    }


    /**
     * Führt die Serienberechnung aus
     *
     * @param calculationSeriesMode Der Berechnungsmodus (Zuverlässigkeit oder Resilienz)
     */
    private void calculationSeries(CALCULATION_MODES calculationSeriesMode) {

        //Dialog zum Datei auswählen
        JFileChooser chooseSaveFile = new JFileChooser();
        chooseSaveFile.setDialogType(JFileChooser.SAVE_DIALOG);

        FileNameExtensionFilter resultFilter = new FileNameExtensionFilter(Strings.getLocalizedString("text.files"), "txt");
        chooseSaveFile.setFileFilter(resultFilter);
        chooseSaveFile.setDialogTitle(Strings.getLocalizedString("save.results.as"));
        chooseSaveFile.setSelectedFile(new File("myResults.txt"));

        String filepath;

        int state = chooseSaveFile.showSaveDialog(null);
        if (state == JFileChooser.APPROVE_OPTION) {
            filepath = chooseSaveFile.getSelectedFile().toString();
        } else {
            reportResult(Strings.getLocalizedString("calculation.cancelled"));
            return;
        }

        //Ab hier in die Datei schreiben
        Writer writer;

        try {
            writer = new FileWriter(filepath);

            if (calculationSeriesMode == CALCULATION_MODES.RESILIENCE) {
                writer.write("Reliability of every edge                 Reliability of every vertex               Resilience of the network");
            } else {
                writer.write("Reliability of every edge                 Reliability of every vertex               Reliability of the network");
            }

            writer.append(System.getProperty("line.separator"));

            //Ab hier Berechnungsserie
            int counter = 1;


            BigInteger edgeStepCount = params.edgeEndValue.subtract(params.edgeStartValue).divide(params.edgeStepSize, BigDecimal.ROUND_FLOOR)
                    .add(BigDecimal.ONE).toBigInteger();
            BigInteger nodeStepCount = params.nodeEndValue.subtract(params.nodeStartValue).divide(params.nodeStepSize, BigDecimal.ROUND_FLOOR)
                    .add(BigDecimal.ONE).toBigInteger();

            Integer stepCount = edgeStepCount.multiply(nodeStepCount).intValue();

            //Schrittzahl weitergeben
            reportStepCount(stepCount);

            for (BigDecimal currentEdgeProb = params.edgeStartValue; currentEdgeProb.compareTo(params.edgeEndValue) <= 0;
                 currentEdgeProb = currentEdgeProb.add(params.edgeStepSize)) {

                for (BigDecimal currentNodeProb = params.nodeStartValue; currentNodeProb.compareTo(params.nodeEndValue) <= 0;
                     currentNodeProb = currentNodeProb.add(params.nodeStepSize)) {

                    reportProgressChange(counter);
                    counter++;

                    //Neue/aktuelle Wahrscheinlichkeiten zuweisen
                    params.setSameReliabilityParams(currentEdgeProb, currentNodeProb);

                    BigDecimal prob;
                    if (calculationSeriesMode == CALCULATION_MODES.RESILIENCE) {
                        //Resilienz
                        prob = getResilience(false);
                    } else {
                        //Reliability
                        // Wahrscheinlichkeiten neu zuordnen. (wird in getResilience() auch gemacht)
                        reassignProbabilities();

                        prob = getHeidtmannsReliability(false);
                    }

                    writer.append(System.getProperty("line.separator"));

                    String reliabilityString = currentEdgeProb.toString();

                    //Nullen hinzufügen, damit alle Ausgangswahrscheinlichkeiten die selbe Länge haben
                    while (reliabilityString.length() < params.edgeStepSize.toString().length()) {
                        reliabilityString += "0";
                    }

                    while (reliabilityString.length() < 42) {
                        reliabilityString += " ";
                    }

                    reliabilityString += currentNodeProb.toString();
                    while (reliabilityString.length() < 42 + params.nodeStepSize.toString().length()) {
                        reliabilityString += "0";
                    }

                    while (reliabilityString.length() < 84) {
                        reliabilityString += " ";
                    }

                    prob = prob.setScale(OUTPUT_PRECISION, BigDecimal.ROUND_HALF_DOWN);
                    writer.write(reliabilityString + prob.toPlainString());
                }

            }
            writer.close();

            reportResult(Strings.getLocalizedString("calculation.series.finished"));
        } catch (IOException e) {
            e.printStackTrace();
            reportResult(Strings.getLocalizedString("calculation.cancelled.due.to.an.error"));
            return;
        }
        // Wahrscheinlichkeiten neu zuordnen.
        reassignProbabilities();

    }

    /**
     * Berechnet die Intaktwahrscheinlichkeit eines Pfades
     *
     * @param path Der Pfad
     * @return Die Intaktwahrscheinlichkeit
     */
    private static BigDecimal getPathProbability(HashSet<GraphElement> path) {
        BigDecimal p = BigDecimal.ONE;

        for (GraphElement el : path) {
            p = p.multiply(el.prob);
        }

        return p;
    }

    /**
     * Weist alle Wahrscheinlichkeiten aus den Eingabefeldern den Elementen im Graphen neu zu
     */
    private void reassignProbabilities() {
        /*
        Diese Listen sind Klone der Kantenlisten des Graphen, aber halten die selben Referenzen auf Kanten
        wie die Kantenliste des Graphen, also eine flache Kopie.
        */
        ArrayList<Edge> edgeList = workingGraph.getEdgelist();
        int edgeCount = edgeList.size();
        //Kantenwahrscheinlichkeiten
        for (int i = 0; i < edgeCount; i++) {
            Edge edge = edgeList.get(i);

            if (params.sameReliabilityMode) {
                edge.prob = params.edgeValue;
            } else {
                edge.prob = params.edgeProbabilities[i];
            }
        }

        ArrayList<Node> nodeList = workingGraph.getNodelist();
        int nodeCount = nodeList.size();

        //Knotenwahrscheinlichkeiten
        for (int i = 0; i < nodeCount; i++) {
            Node node = nodeList.get(i);

            if (params.sameReliabilityMode) {
                if (params.differentTerminalNodeReliability && node.c_node) {
                    node.prob = params.terminalNodeValue;
                } else {
                    node.prob = params.nodeValue;
                }
            } else {
                node.prob = params.nodeProbabilities[i];
            }
        }
    }

    /**
     * Teilt dem Listener das Ergebnis mit
     *
     * @param status Das Ergebnis
     */
    private void reportResult(String status) {
        if (listener != null)
            listener.calculationFinished(status);
    }

    /**
     * Teilt dem Listener die maximale Berechnungsschrittzahl mit
     *
     * @param stepCount Die Schrittzahl
     */
    private void reportStepCount(Integer stepCount) {
        if (listener != null)
            listener.reportCalculationStepCount(stepCount);
    }

    /**
     * Teilt dem Listener den aktuellen Fortschritt mit
     *
     * @param currentStep Der aktuelle Fortschritt (muss kleiner als stepCount in reportStepCount sein)
     */
    private void reportProgressChange(Integer currentStep) {
        if (listener != null)
            listener.calculationProgressChanged(currentStep);
    }

}
