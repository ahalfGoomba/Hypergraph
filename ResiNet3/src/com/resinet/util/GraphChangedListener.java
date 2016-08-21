package com.resinet.util;


/**
 * Dient zum Beobachten des NetPanels
 */
public interface GraphChangedListener {

    /**
     * Wird ausgelöst, wenn im Graphen eine Komponente hinzugefügt wird
     *
     * @param element 0 bei Knoten, 1 bei Kante, 2 bei HyperEdgePoint
     * @param number Die Komponentennummer
     */
    void graphElementAdded(int element, int number);

    /**
     * Wird ausgelöst, wenn im Graphen eine Komponente gelöscht wird.
     *
     * @param element 0 bei Knoten, 1 bei Kante, 2 bei HyperEdgePoint
     * @param number Die Komponentennummer
     */
    void graphElementDeleted(int element, int number);

    /**
     * Wird ausgelöst, wenn im Graphen eine Komponente angeklickt wird.
     *
     * @param element 0 bei Knoten, 1 bei Kante, 2 bei HyperEdgePoint
     * @param number Die Komponentennummer
     */
    void graphElementClicked(int element, int number);

    /**
     * Wird ausgelöst, wenn der Graph durch andere Operationen, wie Copy&Paste verändert wird.
     */
    void graphChanged();
}
