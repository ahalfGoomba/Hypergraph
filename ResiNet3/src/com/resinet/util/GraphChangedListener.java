package com.resinet.util;


/**
 * Dient zum Beobachten des NetPanels
 */
public interface GraphChangedListener {

    /**
     * Wird ausgelöst, wenn im Graphen eine Komponente hinzugefügt wird
     *
     * @param isNode True bei Knoten, false bei Kante
     * @param number Die Komponentennummer
     */
    void graphElementAdded(boolean isNode, int number);

    /**
     * Wird ausgelöst, wenn im Graphen eine Komponente gelöscht wird.
     *
     * @param isNode True bei Knoten, false bei Kante
     * @param number Die Komponentennummer
     */
    void graphElementDeleted(boolean isNode, int number);

    /**
     * Wird ausgelöst, wenn im Graphen eine Komponente angeklickt wird.
     *
     * @param isNode True bei Knoten, false bei Kante
     * @param number Die Komponentennummer
     */
    void graphElementClicked(boolean isNode, int number);

    /**
     * Wird ausgelöst, wenn der Graph durch andere Operationen, wie Copy&Paste verändert wird.
     */
    void graphChanged();
}
