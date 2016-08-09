package com.resinet.views;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Erweitert die Klasse JSpinner. Wird auf das Wahrscheinlichkeitsformat eingestellt, sodass die Eingaben nicht mehr
 * validiert werden müssen.
 */
public class ProbabilitySpinner extends JSpinner {

    private static final long serialVersionUID = -1634127332862046049L;
    /**
     * Die Spaltenzahl des Eingabefeldes
     */
    private static final int COLUMNS = 15;

    /**
     * Erstellt einen neuen JSpinner für Wahrscheinlichkeiten mit der Spaltenanzahl 15
     *
     * @param value Der Startwert
     */
    public ProbabilitySpinner(String value) {
        this(COLUMNS, value);
    }

    /**
     * Erstellt einen neuen JSpinner für Wahrscheinlichkeiten mit dem Startwert 1
     *
     * @param inputColumns Spaltenanzahl des Eingabefeldes
     */
    ProbabilitySpinner(int inputColumns) {
        this(inputColumns, "1");
    }

    /**
     * Erstellt einen neuen JSpinner für Wahrscheinlichkeiten mit der Spaltenanzahl 15 und Startwert 1
     */
    public ProbabilitySpinner() {
        this(COLUMNS, "1");
    }

    /**
     * Erstellt einen neuen JSpinner für Wahrscheinlichkeiten
     *
     * @param inputColumns Spaltenanzahl des Eingabefeldes
     * @param value        Der Startwert
     */
    private ProbabilitySpinner(int inputColumns, String value) {
        super();
        setModel(new ReliabilityNumberModel(value));

        JSpinner.NumberEditor editor = ((JSpinner.NumberEditor) getEditor());
        JFormattedTextField textField = editor.getTextField();
        editor.getTextField().setColumns(inputColumns);

        //Anzahl Nachkommastellen auf 50 festlegen
        DecimalFormat format = editor.getFormat();
        format.setMaximumFractionDigits(50);
        format.setMinimumFractionDigits(0);

        textField.addFocusListener(new MyFocusAdapter(textField));

        textField.addKeyListener(new MyKeyAdapter(textField));
    }

    /**
     * Gibt den angezeigten Wert als BigDecimal zurück
     *
     * @return Wert als BigDecimal
     */
    public BigDecimal getBigDecimalValue() {
        JSpinner.NumberEditor editor = ((JSpinner.NumberEditor) getEditor());
        JFormattedTextField textField = editor.getTextField();

        return new BigDecimal(textField.getText());
    }

    /**
     * Sorgt dafür, dass der Text markiert wird, wenn der JSpinner fokussiert wird
     */
    private static class MyFocusAdapter extends FocusAdapter {
        private final JFormattedTextField textField;

        MyFocusAdapter(JFormattedTextField textField) {
            this.textField = textField;
        }

        @Override
        public void focusGained(FocusEvent e) {
            SwingUtilities.invokeLater(textField::selectAll);
        }
    }

    /**
     * Ersetzt automatisch ein eingegebenes Komma durch einen Punkt und lässt das Textfeld nicht auf weitere Kommata
     * oder Punkte reagieren, wenn bereits eines im Textfeld ist.
     */
    private static class MyKeyAdapter extends KeyAdapter {
        private final JFormattedTextField textField;

        MyKeyAdapter(JFormattedTextField textField) {
            this.textField = textField;
        }

        public void keyTyped(KeyEvent e) {
            char c = e.getKeyChar();
            if ((c == ',' || c == '.') && textField.getText().indexOf('.') >= 0) {
                //Konsumiert den Tastenschlag, also damit nichts weiteres passiert
                e.consume();
            } else if (c == ',')
                e.setKeyChar('.');
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        JSpinner.NumberEditor editor = ((JSpinner.NumberEditor) getEditor());
        JFormattedTextField textField = editor.getTextField();
        return textField.requestFocusInWindow();
    }

    /**
     * Ein {@link SpinnerModel} für {@link BigDecimal}.
     */
    private static class ReliabilityNumberModel extends SpinnerNumberModel {
        private static final long serialVersionUID = 4830497721488522051L;
        private static BigDecimal minimum = new BigDecimal("0");
        private static BigDecimal maximum = new BigDecimal("1");
        private static BigDecimal stepSize = new BigDecimal("0.01");
        private BigDecimal value;

        ReliabilityNumberModel(String value) {
            this.value = new BigDecimal(value);
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public void setValue(Object value) {
            if ((value == null) || !(value instanceof BigDecimal)) {
                throw new IllegalArgumentException("illegal value");
            }
            if (!value.equals(this.value)) {
                this.value = (BigDecimal) value;
                fireStateChanged();
            }
        }

        @Override
        public Object getNextValue() {
            return incrementValue(true);
        }

        @Override
        public Object getPreviousValue() {
            return incrementValue(false);
        }

        /**
         * Gibt den nächsten oder vorherigen Wert in der Zahlensequenz zurück.
         *
         * @param add True, wenn der nächste Wert zurückgegeben werden soll.
         * @return Nächster oder vorheriger Wert
         */
        private Number incrementValue(boolean add) {
            BigDecimal newValue;
            if (add) {
                newValue = value.add(stepSize);
            } else {
                newValue = value.subtract(stepSize);
            }

            //neuer Wert muss in den Grenzen liegen
            if ((maximum != null) && (maximum.compareTo(newValue) < 0)) {
                return null;
            }
            if ((minimum != null) && (minimum.compareTo(newValue) > 0)) {
                return null;
            }

            return newValue;
        }
    }
}
