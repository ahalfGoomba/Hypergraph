package com.resinet.model;

import java.math.BigDecimal;

/**
 * Abstrakte Klasse, die die Gemeinsamkeiten von Knoten und Kanten beinhaltet.
 * Wird von Node und Edge erweitert.
 */
public abstract class GraphElement {

    //Intaktwahrscheinlichkeit des Elements
    public BigDecimal prob;

    //Für die Baumsuche, wird aber niemals ausgelesen
    public boolean useless = false;

    //mark fuer K-Baum (com.resinet.model.KTree.java)
    public boolean b_marked = false;
}
