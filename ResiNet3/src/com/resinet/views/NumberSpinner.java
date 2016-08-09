package com.resinet.views;

import javax.swing.*;
import java.awt.event.KeyListener;

class NumberSpinner extends JSpinner {

    private static final long serialVersionUID = -4874300191518893893L;
    /**
     * Die Spaltenzahl des Eingabefeldes
     */
    private static final int COLUMNS = 10;

    /**
     * Erstellt einen neuen JSpinner für Ganzzahlen mit der Spaltenanzahl 10
     */
    NumberSpinner(int value, int min, int max) {
        this(COLUMNS, value, min, max);
    }

    /**
     * Erstellt einen neuen JSpinner für ganze Zahlen
     *
     * @param inputColumns Spaltenanzahl des Eingabefeldes
     */
    private NumberSpinner(int inputColumns, int value, int min, int max) {
        super();
        setModel(new SpinnerNumberModel(value, min, max, 1));

        JSpinner.NumberEditor editor = ((JSpinner.NumberEditor) getEditor());
        JFormattedTextField textField = editor.getTextField();
        textField.setColumns(inputColumns);
    }

    /**
     * Gibt den aktuellen Wert als Ganzzahl zurück
     *
     * @return Wert als Ganzzahl
     */
    int getIntValue() {
        JSpinner.NumberEditor editor = ((JSpinner.NumberEditor) getEditor());
        JFormattedTextField textField = editor.getTextField();

        return Integer.parseInt(textField.getText());
    }

    /**
     * Fügt dem Editorfeld einen {@link KeyListener} hinzu.
     *
     * @param l Der {@link KeyListener}
     */
    @Override
    public synchronized void addKeyListener(KeyListener l) {
        JSpinner.NumberEditor editor = ((JSpinner.NumberEditor) getEditor());
        JFormattedTextField textField = editor.getTextField();
        textField.addKeyListener(l);
    }
}
