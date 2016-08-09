package com.resinet.controller;

import com.resinet.Resinet;
import com.resinet.algorithms.ProbabilityCalculator;
import com.resinet.model.CalculationParams;
import com.resinet.model.Graph;
import com.resinet.model.GraphWrapper;
import com.resinet.util.*;
import com.resinet.views.*;
import com.sun.istack.internal.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltet alle Events des MainFrames und vermittelt zwischen verschiedenen Komponenten wie Menüs, Datenspeicherung,
 * Berechnung und Darstellung.
 */
public class MainframeController extends WindowAdapter implements ActionListener, GraphChangedListener,
        CalculationProgressListener, Constants, ItemListener, ChangeListener, PropertyChangeListener, MenuListener {
    private Resinet mainFrame;

    /**
     * Nähere Beschreibung siehe Methode propertyChange
     */
    private JComponent permanentFocusOwner;

    /**
     * Listen für die Eingabefelder für die Zuverlässigkeiten von Knoten und Kanten
     */
    private final List<ProbabilitySpinner> edgeProbabilityBoxes = new ArrayList<>();
    private final List<ProbabilitySpinner> nodeProbabilityBoxes = new ArrayList<>();

    public MainframeController() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addPropertyChangeListener("permanentFocusOwner", this);
    }

    public void setMainFrame(Resinet mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (mainFrame == null)
            return;

        AbstractButton button = (AbstractButton) e.getSource();

        if (button == mainFrame.getResetMenuItem()) {
            resetGraph();
        } else if (button == mainFrame.getOpenMenuItem()) {
            loadSavedData();
        } else if (button == mainFrame.getSaveMenuItem()) {
            saveData();
        } else if (button == mainFrame.getCloseMenuItem()) {
            windowClosing(null);
        } else if (button == mainFrame.getAboutMenuItem()) {
            AboutFrame.show();
        } else if (button == mainFrame.getTutorialMenuItem()) {
            HelpFrame.show();
        } else if (button == mainFrame.getGenerateGraphMenuItem()) {
            GenerateGraphFrame.show(this::graphGenerated);
        } else if (button == mainFrame.getCenterGraphMenuItem()) {
            NetPanel netPanel = mainFrame.getNetPanel();
            netPanel.centerGraphOnNextPaint();
            netPanel.repaint();
        } else if (button == mainFrame.getAlignGraphMenuItem()) {
            //TODO graph ausrichten
        } else if (button == mainFrame.getCalcReliabilityBtn()) {
            startCalculation(CALCULATION_MODES.RELIABILITY);
        } else if (button == mainFrame.getCalcResilienceBtn()) {
            startCalculation(CALCULATION_MODES.RESILIENCE);
        } else if (button == mainFrame.getCollapseOutputBtn()) {
            mainFrame.setStatusBarCollapsed(true);
        } else if (permanentFocusOwner.equals(mainFrame.getNetPanel())) {
            mainFrame.getNetPanel().actionPerformed(e);
        }
    }
    //TODO Zuletzt geöffnet-Liste, Serienparallelreduktion
    //TODO Graph optimieren bezüglich Anordnung nach verschiedenen Algorithmen

    /**
     * Wird bei der Statusänderung einer Checkbox ausgelöst.
     *
     * @param e das ItemEvent
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (mainFrame == null)
            return;

        AbstractButton checkbox = (AbstractButton) e.getSource();
        if (checkbox == mainFrame.getCalculationSeriesCheckBox() || checkbox == mainFrame.getDifferentForTerminalCheckBox()) {
            mainFrame.setGuiState(GUI_STATES.ENTER_GRAPH, true);
        } else if (checkbox == mainFrame.getConsiderEdgesBox() || checkbox == mainFrame.getConsiderNodesBox()) {
            NetPanelController netPanelController = mainFrame.getNetPanel().getController();

            boolean considerNodes = mainFrame.getConsiderNodesBox().isSelected();
            boolean considerEdges = mainFrame.getConsiderEdgesBox().isSelected();

            netPanelController.setClickableElements(considerNodes, considerEdges);
            updateSingleReliabilityProbPanel();
        }
    }

    /**
     * Wird ausgelöst, wenn ein Graphelement hinzugefügt wird. Fügt ein entsprechendes Eingabefeld für die
     * Zuverlässigkeit hinzu.
     *
     * @param isNode True bei Knoten, false bei Kante
     * @param number Die Komponentennummer
     */
    @Override
    public void graphElementAdded(boolean isNode, int number) {
        //Feld nur hinzufügen, falls der Einzelzuverlässigkeitsmodus aktiv ist und die Komponente berücksichtigt werden soll
        if (mainFrame.getReliabilityMode() == RELIABILITY_MODES.SAME)
            return;

        if ((!isNode && !mainFrame.getConsiderEdgesBox().isSelected()) || (isNode && !mainFrame.getConsiderNodesBox().isSelected()))
            return;

        addFieldToProbPanel(number, isNode);

        //Scrollpane updaten
        refreshSingleReliabilityScrollPane();
    }

    /**
     * Wird ausgelöst, wenn ein Graphelement gelöscht wird. Entfernt das zugehörige Eingabefeld.
     *
     * @param isNode True bei Knoten, false bei Kante
     * @param number Die Komponentennummer
     */
    @Override
    public void graphElementDeleted(boolean isNode, int number) {
        if (mainFrame.getReliabilityMode() == RELIABILITY_MODES.SAME)
            return;

        List<ProbabilitySpinner> list;
        if (isNode) {
            list = nodeProbabilityBoxes;
        } else {
            list = edgeProbabilityBoxes;
        }

        //Alle Wahrscheinlichkeiten ein Feld vorrücken lassen
        for (int i = number; i < list.size() - 1; i++) {
            list.get(i).setValue(list.get(i + 1).getValue());
        }

        //Letztes Element aus Liste und aus GUI entfernen
        ProbabilitySpinner spinner = list.get(list.size() - 1);
        spinner.getParent().getParent().remove(spinner.getParent());
        list.remove(spinner);

        refreshSingleReliabilityScrollPane();
    }

    /**
     * Wird ausgelöst, wenn ein Element im Graphen angeklickt wird. Fokussiert das entsprechende Eingabefeld für die
     * Zuverlässigkeit.
     *
     * @param isNode True bei Knoten, false bei Kante
     * @param number Die Komponentennummer
     */
    @Override
    public void graphElementClicked(boolean isNode, int number) {
        if (mainFrame.getReliabilityMode() == RELIABILITY_MODES.SAME) {
            if (isNode) {
                mainFrame.getSameReliabilityNodeProbBox().requestFocusInWindow();
            } else {
                mainFrame.getSameReliabilityEdgeProbBox().requestFocusInWindow();
            }
        } else {
            if (isNode) {
                if (nodeProbabilityBoxes.size() > number) {
                    nodeProbabilityBoxes.get(number).requestFocusInWindow();
                }
            } else {
                if (edgeProbabilityBoxes.size() > number) {
                    edgeProbabilityBoxes.get(number).requestFocusInWindow();
                }
            }
        }
    }

    /**
     * Wird ausgelöst, wenn der Graph verändert wurde. Aktualisiert das Einzelzuverlässigkeitspanel.
     */
    @Override
    public void graphChanged() {
        updateSingleReliabilityProbPanel();
    }

    /**
     * Wird ausgelöst, wenn sich der Berechnungsfortschritt geändert hat. Setzt den Fortschritt in der
     * Fortschrittsleiste.
     *
     * @param currentStep Der aktuelle Schritt
     */
    @Override
    public void calculationProgressChanged(Integer currentStep) {
        //Falls das nicht der EventDispatchThread von Swing ist, auf dem entsprechenden Thread invoken
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> calculationProgressChanged(currentStep));
            return;
        }
        JProgressBar progressBar = mainFrame.getCalculationProgressBar();
        progressBar.setValue(currentStep);

        mainFrame.setResultText(MessageFormat.format(Strings.getLocalizedString("calculation.progress"), currentStep, progressBar.getMaximum()));
    }

    /**
     * Wird ausgelöst, wenn die Berechnung abgeschlossen ist.
     *
     * @param status Das Ergebnis
     */
    @Override
    public void calculationFinished(String status) {
        //Falls das nicht der EventDispatchThread von Swing ist, auf dem entsprechenden Thread invoken
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> calculationFinished(status));
            return;
        }
        mainFrame.setResultText(status);
        //GUI wieder aktivieren, da Berechnung fertig
        mainFrame.setGuiState(GUI_STATES.ENTER_GRAPH);
    }

    /**
     * Wird ausgelöst, um die Anzahl der Berechnungsschritte festzulegen. Aktualisiert die Fortschrittsleiste.
     *
     * @param stepCount Die maximale Anzahl an Schritten der aktuellen Berechnungsaufgabe
     */
    @Override
    public void reportCalculationStepCount(Integer stepCount) {
        //Falls das nicht der EventDispatchThread von Swing ist, auf dem entsprechenden Thread invoken
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> reportCalculationStepCount(stepCount));
            return;
        }
        JProgressBar progressBar = mainFrame.getCalculationProgressBar();
        progressBar.setValue(0);
        progressBar.setMaximum(stepCount);
        mainFrame.setResultText(MessageFormat.format(Strings.getLocalizedString("calculation.progress"), "0", stepCount));
    }

    /**
     * Wird beim Tabwechsel ausgelöst
     *
     * @param e das ChangeEvent
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (mainFrame == null)
            return;

        NetPanelController netPanelController = mainFrame.getNetPanel().getController();

        if (mainFrame.getReliabilityMode() == RELIABILITY_MODES.SAME) {
            netPanelController.setClickableElements(true, true);
        } else {
            boolean considerNodes = mainFrame.getConsiderNodesBox().isSelected();
            boolean considerEdges = mainFrame.getConsiderEdgesBox().isSelected();

            netPanelController.setClickableElements(considerNodes, considerEdges);
        }
        updateSingleReliabilityProbPanel();
    }

    /**
     * Zeigt einen Dialog an, damit das Fenster nur nach Bestätigung geschlossen wird.
     *
     * @param e Das Event
     */
    @Override
    public void windowClosing(@Nullable WindowEvent e) {
        /*int result = JOptionPane.showConfirmDialog(mainFrame.getContentPane(),
                "Do you really wanna close ResiNet?\nAll unsaved data will be lost.", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        //Programm beenden, wenn Ja angeklickt
        if (result == JOptionPane.YES_OPTION) {*/
        System.exit(0);
        //}
    }

    /**
     * Wird ausgelöst, wenn das fokussierte Element verändert wird. Hier wird der permanentFocusOwner gesetzt. Falls das
     * neu fokussierte Element kein Textfeld ist, wird permanentFocusOwner auf das NetPanel gesetzt, damit es Befehle
     * aus dem Bearbeiten-Menü ausführen kann.
     *
     * @param evt Das Event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object o = evt.getNewValue();
        if (o instanceof JTextComponent) {
            permanentFocusOwner = (JComponent) o;
        } else {
            permanentFocusOwner = mainFrame.getNetPanel();
        }
    }


    /**
     * Wird ausgelöst, wenn das Bearbeiten-Menü geöffnet wird, damit dann der Enabled-Status des Rückgängig-Buttons und
     * des Wiederholen-Buttons gesetzt werden kann.
     *
     * @param e Das Event
     */
    @Override
    public void menuSelected(MenuEvent e) {
        NetPanel netPanel = mainFrame.getNetPanel();
        mainFrame.getUndoMenuItem().setEnabled(netPanel.canUndo());
        mainFrame.getRedoMenuItem().setEnabled(netPanel.canRedo());
    }

    /**
     * Wird ausgelöst, wenn das Bearbeiten-Menü geschlossen/versteckt wird. Setzt die Rückgängig-/Wiederholen-Buttons
     * auf enabled, damit auf die Tastenkombinationen reagiert wird, falls sich am canUndo/canRedo etwas geändert hat.
     *
     * @param e Das Event
     */
    @Override
    public void menuDeselected(MenuEvent e) {
        mainFrame.getUndoMenuItem().setEnabled(true);
        mainFrame.getRedoMenuItem().setEnabled(true);
    }

    @Override
    public void menuCanceled(MenuEvent e) {
    }

    /**
     * Setzt den Graphen zurück und entfernt alle Zuverlässigkeitsfelder für einzelne Knoten.
     */
    private void resetGraph() {
        mainFrame.setGuiState(GUI_STATES.ENTER_GRAPH);

        mainFrame.getNetPanel().resetGraph();

        mainFrame.getSingleReliabilitiesContainer().removeAll();
        mainFrame.getSingleReliabilitiesContainer().repaint();

        nodeProbabilityBoxes.clear();
        edgeProbabilityBoxes.clear();
    }

    /**
     * Löst das Laden von gespeicherten Daten aus und lädt diese in die GUI.
     */
    private void loadSavedData() {
        NetPanel netPanel = mainFrame.getNetPanel();
        int width = netPanel.getWidth();
        int height = netPanel.getHeight();

        CalculationParams params = GraphSaver.inputNet(mainFrame.getContentPane(), width, height);

        if (params == null)
            return;

        resetGraph();

        //Graphelemente hinzufügen
        netPanel.addNodesAndEdges(params.graphNodes, params.graphEdges);

        updateSingleReliabilityProbPanel();
        if (params.probabilitiesLoaded) {
            //Nur wenn Wahrscheinlichkeiten eingespeichert wurden, diese auch laden

            if (params.sameReliabilityMode) {
                mainFrame.setReliabilityMode(RELIABILITY_MODES.SAME);

                mainFrame.getSameReliabilityNodeProbBox().setValue(params.nodeValue);
                mainFrame.getSameReliabilityEdgeProbBox().setValue(params.edgeValue);
                if (params.calculationSeries) {
                    mainFrame.getCalculationSeriesCheckBox().setSelected(true);

                    mainFrame.getNodeEndProbabilityBox().setValue(params.nodeEndValue);
                    mainFrame.getNodeProbabilityStepSizeBox().setValue(params.nodeStepSize);

                    mainFrame.getEdgeEndProbabilityBox().setValue(params.edgeEndValue);
                    mainFrame.getEdgeProbabilityStepSizeBox().setValue(params.edgeStepSize);
                }

                if (params.differentTerminalNodeReliability) {
                    mainFrame.getDifferentForTerminalCheckBox().setSelected(true);
                    mainFrame.getTerminalNodeProbBox().setValue(params.terminalNodeValue);
                }
            } else {
                mainFrame.setReliabilityMode(RELIABILITY_MODES.SINGLE);

                //Einzelwahrscheinlichkeiten in die Felder eintragen
                for (int i = 0; i < edgeProbabilityBoxes.size(); i++) {
                    edgeProbabilityBoxes.get(i).setValue(params.edgeProbabilities[i]);
                }

                for (int i = 0; i < nodeProbabilityBoxes.size(); i++) {
                    nodeProbabilityBoxes.get(i).setValue(params.nodeProbabilities[i]);
                }
            }
        }
        mainFrame.setGuiState(GUI_STATES.ENTER_GRAPH, true);
        netPanel.centerGraphOnNextPaint();
        //verzögert Repaint auslösen
        SwingUtilities.invokeLater(netPanel::repaint);
    }

    /**
     * Aktualisiert das Wahrscheinlichkeitspanel
     */
    private void updateSingleReliabilityProbPanel() {
        if (mainFrame == null || mainFrame.getReliabilityMode() == RELIABILITY_MODES.SAME)
            return;

        boolean considerNodes = mainFrame.getConsiderNodesBox().isSelected();
        boolean considerEdges = mainFrame.getConsiderEdgesBox().isSelected();

        //Knoten/Kantenzahl auf 0 setzen, wenn sie nicht berücksichtigt werden sollen
        int edgeCount = considerEdges ? mainFrame.getNetPanel().getEdges().size() : 0;
        int edgeBoxCount = edgeProbabilityBoxes.size();
        int nodeCount = considerNodes ? mainFrame.getNetPanel().getNodes().size() : 0;
        int nodeBoxCount = nodeProbabilityBoxes.size();

        //Fehlende Kantenwahrscheinlichkeitsfelder hinzufügen
        for (int i = edgeBoxCount; i < edgeCount; i++) {
            addFieldToProbPanel(i, false);
        }
        //Fehlende Knotenwahrscheinlichkeitsfelder hinzufügen
        for (int i = nodeBoxCount; i < nodeCount; i++) {
            addFieldToProbPanel(i, true);
        }

        //Überflüssige Kantenwahrscheinlichkeitsfelder entfernen
        for (int i = edgeBoxCount; i > edgeCount; i--) {
            ProbabilitySpinner textField = edgeProbabilityBoxes.get(edgeProbabilityBoxes.size() - 1);
            textField.getParent().getParent().remove(textField.getParent());
            edgeProbabilityBoxes.remove(textField);
        }

        //Überflüssige Knotenwahrscheinlichkeitsfelder entfernen
        for (int i = nodeBoxCount; i > nodeCount; i--) {
            ProbabilitySpinner textField = nodeProbabilityBoxes.get(nodeProbabilityBoxes.size() - 1);
            textField.getParent().getParent().remove(textField.getParent());
            nodeProbabilityBoxes.remove(textField);
        }

        //Elemente neu anordnen
        if (considerEdges && considerNodes) {
            realignProbabiliyPanels(nodeProbabilityBoxes, 0);
            realignProbabiliyPanels(edgeProbabilityBoxes, 1);
        } else if (!considerEdges) {
            realignProbabiliyPanels(nodeProbabilityBoxes, 2);
        } else {
            realignProbabiliyPanels(edgeProbabilityBoxes, 2);
        }

        refreshSingleReliabilityScrollPane();
    }

    /**
     * Verteilt die Panels auf die definierte Spalte
     *
     * @param list   Die Liste der {@link ProbabilitySpinner} innerhalb von {@link SingleReliabilityPanel}
     * @param column 0 und 1 für die jeweilige Spalte, 2 für Verteilung auf beide Spalten
     */
    private void realignProbabiliyPanels(List<ProbabilitySpinner> list, int column) {
        JPanel singleReliabilitiesPanel = mainFrame.getSingleReliabilitiesContainer();

        int currentPosition = 0;
        for (ProbabilitySpinner spinner : list) {
            SingleReliabilityPanel panel = (SingleReliabilityPanel) spinner.getParent();

            if (column == 2) {
                singleReliabilitiesPanel.add(panel, GbcBuilder.build(currentPosition % 2, currentPosition / 2));
            } else {
                singleReliabilitiesPanel.add(panel, GbcBuilder.build(column, currentPosition));
            }
            currentPosition++;
        }
    }

    /**
     * Lässt die Scrollpane sich revalidieren und neu zeichnen.
     */
    private void refreshSingleReliabilityScrollPane() {
        mainFrame.getSingleReliabilitiesScrollPane().revalidate();
        mainFrame.getSingleReliabilitiesScrollPane().repaint();
    }

    /**
     * Fügt dem Wahrscheinlichkeitspanel ein Panel für die Wahrscheinlichkeit einer Komponente hinzu
     *
     * @param number     Nummer des Felds
     * @param isNodeProb True, wenn das Feld für einen Knoten ist, false bei Kante
     */
    private void addFieldToProbPanel(int number, boolean isNodeProb) {
        SingleReliabilityPanel newPanel = new SingleReliabilityPanel(isNodeProb, number);
        JPanel singleReliabilitiesPanel = mainFrame.getSingleReliabilitiesContainer();

        if (isNodeProb) {
            //Falls es ein Knoten ist
            nodeProbabilityBoxes.add(newPanel.getSpinner());
            if (mainFrame.getConsiderEdgesBox().isSelected()) {
                //in linke spalte einfügen
                singleReliabilitiesPanel.add(newPanel, GbcBuilder.build(0, number));
            } else {
                //hinten anfügen
                int lastx = mainFrame.getLastSingleReliabilityComponentX();
                int newX = lastx == 1 ? 0 : 1;

                singleReliabilitiesPanel.add(newPanel, GbcBuilder.build(newX, number - newX));
            }
        } else {
            //Falls es eine Kante ist
            edgeProbabilityBoxes.add(newPanel.getSpinner());

            if (mainFrame.getConsiderNodesBox().isSelected()) {
                //in die rechte Spalte einfügen
                singleReliabilitiesPanel.add(newPanel, GbcBuilder.build(1, number));
            } else {
                //hinten anfügen
                int lastx = mainFrame.getLastSingleReliabilityComponentX();
                int newX = lastx == 1 ? 0 : 1;

                singleReliabilitiesPanel.add(newPanel, GbcBuilder.build(newX, number - newX));
            }
        }
    }

    /**
     * Löst das Speichern aller aktuell dargestellten Daten aus.
     */
    private void saveData() {
        NetPanel netPanel = mainFrame.getNetPanel();
        int width = netPanel.getWidth();
        int height = netPanel.getHeight();

        CalculationParams params = buildCalculationParams(null, true);

        GraphSaver.exportNet(params, mainFrame.getNetPanel(), width, height);
    }

    /**
     * Stellt die Berechnungsparameter zusammen.
     *
     * @param mode      Der Berechnungsmodus
     * @param forSaving True, wenn die Parameter zum Speichern gesammelt werden. In diesem Fall werden auch die
     *                  Rohlisten von Knoten und Kanten des NetPanels gesetzt.
     * @return CalculationParams-Objekt
     */
    private CalculationParams buildCalculationParams(CALCULATION_MODES mode, boolean forSaving) {
        NetPanel netPanel = mainFrame.getNetPanel();
        RELIABILITY_MODES reliabilityMode = mainFrame.getReliabilityMode();
        CalculationParams params;

        //Graph erzeugen
        Graph graph = GraphUtil.makeGraph(netPanel);

        //Falls der Graph nicht in Ordnung ist, wird hier schon eine Fehlermeldung ausgegeben
        if (!GraphUtil.graphIsValid(netPanel, graph))
            return null;

        //Das Graphobjekt muss auf jeden Fall erzeugt werden, um zu überprüfen, ob der Graph den Anforderungen entspricht
        params = new CalculationParams(mode, graph);

        //Falls die Parameter gespeichert werden sollen, die Graphelementlisten setzen
        if (forSaving) {
            params.setGraphLists(netPanel.getNodes(), netPanel.getEdges());
        }

        if (reliabilityMode == RELIABILITY_MODES.SAME) {
            //Gleiche Zuverlässigkeiten
            params.setReliabilityMode(true);

            //Einzelwerte auslesen
            BigDecimal edgeStartValue = mainFrame.getSameReliabilityEdgeProbBox().getBigDecimalValue();
            BigDecimal nodeStartValue = mainFrame.getSameReliabilityNodeProbBox().getBigDecimalValue();

            //Falls eine Berechnungsserie gemacht werden soll, entsprechende Parameter setzen, sonst nur die Einzelzuverlässigkeiten
            if (mainFrame.getCalculationSeriesCheckBox().isSelected()) {
                BigDecimal edgeEndValue = mainFrame.getEdgeEndProbabilityBox().getBigDecimalValue();
                BigDecimal nodeEndValue = mainFrame.getNodeEndProbabilityBox().getBigDecimalValue();
                BigDecimal edgeStepSize = mainFrame.getEdgeProbabilityStepSizeBox().getBigDecimalValue();
                BigDecimal nodeStepSize = mainFrame.getNodeProbabilityStepSizeBox().getBigDecimalValue();

                params.setSeriesParams(edgeStartValue, edgeEndValue, edgeStepSize,
                        nodeStartValue, nodeEndValue, nodeStepSize);
            } else {
                params.setSameReliabilityParams(edgeStartValue, nodeStartValue);
            }

            if (mainFrame.getDifferentForTerminalCheckBox().isSelected()) {
                //Falls für Terminalknoten eine andere Zuverlässigkeit verwendet werden soll, diese einlesen
                BigDecimal terminalNodeValue = mainFrame.getTerminalNodeProbBox().getBigDecimalValue();
                params.setDifferentTerminalNodeReliability(terminalNodeValue);
            }
        } else {
            params.setReliabilityMode(false);

            //Einzelintaktwahrscheinlichkeiten einlesen
            //Dabei die Intaktwahrscheinlichkeiten auf 1 setzen, wenn sie nicht berücksichtigt werden sollen.
            int edgeCount = edgeProbabilityBoxes.size();
            int nodeCount = nodeProbabilityBoxes.size();
            BigDecimal[] edgeProbabilities = new BigDecimal[edgeCount];
            BigDecimal[] nodeProbabilities = new BigDecimal[nodeCount];

            boolean considerNodes = mainFrame.getConsiderNodesBox().isSelected();
            boolean considerEdges = mainFrame.getConsiderEdgesBox().isSelected();

            for (int i = 0; i < edgeCount; i++) {
                if (considerEdges) {
                    edgeProbabilities[i] = edgeProbabilityBoxes.get(i).getBigDecimalValue();
                } else {
                    edgeProbabilities[i] = BigDecimal.ONE;
                }
            }

            for (int i = 0; i < nodeCount; i++) {
                if (considerNodes) {
                    nodeProbabilities[i] = nodeProbabilityBoxes.get(i).getBigDecimalValue();
                } else {
                    nodeProbabilities[i] = BigDecimal.ONE;
                }
            }

            params.setSingleReliabilityParams(edgeProbabilities, nodeProbabilities);
        }

        return params;
    }

    /**
     * Startet die Berechnung
     *
     * @param mode Resilienz oder Zuverlässigkeit
     */
    private void startCalculation(CALCULATION_MODES mode) {
        CalculationParams params = buildCalculationParams(mode, false);

        //Wenn params == null, sind nicht alle Voraussetzungen erfüllt und eine Meldung wurde angezeigt
        if (params == null)
            return;

        ProbabilityCalculator calculator = ProbabilityCalculator.create(this, params);

        //Elemente der GUI während der Berechnung deaktivieren
        mainFrame.setGuiState(GUI_STATES.CALCULATION_RUNNING);

        System.out.println("startCalculation: " + Thread.currentThread().getName());
        //Starte die Berechnung
        calculator.start();
    }

    /**
     * Wird ausgelöst, wenn ein Graph generiert wurde. Fügt die erzeugten Knoten und Kanten dem GraphPanel hinzu.
     *
     * @param graphWrapper Der Wrapper mit den generierten Knoten und Kanten
     */
    private void graphGenerated(GraphWrapper graphWrapper) {
        NetPanel netPanel = mainFrame.getNetPanel();
        netPanel.addGraphWrapperAndSelect(graphWrapper);
        updateSingleReliabilityProbPanel();
    }
}
