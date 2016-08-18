package com.resinet.views;

import com.resinet.util.Strings;

import javax.swing.*;
import java.awt.*;

/**
 * Stellt ein Panel fÃ¼r eine Einzelkomponentenwahrscheinlichkeit mit Label und Textfeld dar
 */
public class SingleReliabilityPanel extends JPanel {
    private static final long serialVersionUID = -754335718725658583L;

    private final ProbabilitySpinner spinner;
    private final int element;

    /**
     * Die bevorzugte Breite
     */
    static final int PREF_WIDTH = 160;
    /**
     * Die bevorzugte HÃ¶he
     */
    private static final int PREF_HEIGHT = 30;

    /**
     * Konstruktor
     *
     * @param element 0 für Knoten, 1 für Kante, 2 für Hyperkante
     * @param number Die Nummer des Elements
     */
    public SingleReliabilityPanel(int element, int number) {
        super();
        this.element = element;

        String text;
        String type;
        if(element == 0 ){
        	type = Strings.getLocalizedString("vertex");
        } else if(element == 1){
        	type = Strings.getLocalizedString("edge");
        } else {
        	type = Strings.getLocalizedString("he");
        }
        
        text = type + " " + number;

        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        spinner = new ProbabilitySpinner(9);

        setLayout(new FlowLayout(FlowLayout.RIGHT));
        setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));

        //setDoubleBuffered(true);
        add(label);
        add(spinner);
    }

    public int isNode() {
        return element;
    }

    public ProbabilitySpinner getSpinner() {
        return spinner;
    }
}
