package com.resinet.util;

/**
 * Definiert die nötigen Methoden eines Listeners
 */
public interface CalculationProgressListener {

    /**
     * Wird ausgelöst, wenn sich der Berechnungsstatus ändert
     *
     * @param currentStep Der aktuelle Schritt
     */
    void calculationProgressChanged(Integer currentStep);

    /**
     * Wird ausgelöst, wenn die Berechnung fertig ist
     *
     * @param status Das Ergebnis
     */
    void calculationFinished(String status);

    /**
     * Wird ausgelöst, um die Schrittzahl festzusetzen
     *
     * @param stepCount Die maximale Anzahl an Schritten der aktuellen Berechnungsaufgabe
     */
    void reportCalculationStepCount(Integer stepCount);
}