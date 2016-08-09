package com.resinet.util;

import java.awt.*;

/**
 * Eine Hilfsklasse zum Erstellen von GridBagConstraints
 */
public class GbcBuilder extends GridBagConstraints {

    /**
     * Erstellt ein GbcBuilder-Objekt mit den angegebenen Parametern
     *
     * @param x       X-Position im Grid (gridx)
     * @param y       Y-Position im Grid (gridy)
     * @param width   beanspruchte Spaltenanzahl im Grid (gridwidth)
     * @param height  beanspruchte Zeilenanzahl im Grid (gridheight)
     * @param weightx Gewichtung auf X-Achse (für Verteilung des freien Platzes)
     * @param weighty Gewichtung auf Y-Achse (für Verteilung des freien Platzes
     */
    private GbcBuilder(int x, int y, int width, int height, double weightx, double weighty) {
        gridx = x;
        gridy = y;
        gridwidth = width;
        gridheight = height;

        this.weightx = weightx;
        this.weighty = weighty;
    }

    /**
     * Erstellt ein GbcBuilder-Objekt mit den angegebenen Parametern
     *
     * @param x       X-Position im Grid (gridx)
     * @param y       Y-Position im Grid (gridy)
     * @param width   beanspruchte Spaltenanzahl im Grid (gridwidth)
     * @param height  beanspruchte Zeilenanzahl im Grid (gridheight)
     * @param weightx Gewichtung auf X-Achse (für Verteilung des freien Platzes)
     * @param weighty Gewichtung auf Y-Achse (für Verteilung des freien Platzes
     * @return GbcBuilder
     */
    public static GbcBuilder build(int x, int y, int width, int height, double weightx, double weighty) {
        return new GbcBuilder(x, y, width, height, weightx, weighty);
    }

    /**
     * Erstellt ein GbcBuilder-Objekt mit den angegebenen Parametern <br> weightx = 0, weighty = 0
     *
     * @param x      X-Position im Grid (gridx)
     * @param y      Y-Position im Grid (gridy)
     * @param width  beanspruchte Spaltenanzahl im Grid (gridwidth)
     * @param height beanspruchte Zeilenanzahl im Grid (gridheight)
     * @return GbcBuilder
     */
    public static GbcBuilder build(int x, int y, int width, int height) {
        return build(x, y, width, height, 0, 0);
    }

    /**
     * Erstellt ein GbcBuilder-Objekt mit den angegebenen Parametern. <br> width = 1, height = 1, weightx = 0, weighty =
     * 0
     *
     * @param x X-Position im Grid (gridx)
     * @param y Y-Position im Grid (gridy)
     * @return GbcBuilder
     */
    public static GbcBuilder build(int x, int y) {
        return build(x, y, 1, 1, 0, 0);
    }

    /**
     * Fülleffekt in alle Richtungen setzen
     *
     * @return GbcBuilder-Objekt
     */
    public GbcBuilder fillBoth() {
        fill = GridBagConstraints.BOTH;
        return this;
    }

    /**
     * Legt den Innenabstand nach unten fest
     *
     * @param padding Der Abstand
     * @return GbcBuilder-Objekt
     */
    public GbcBuilder bottom(int padding) {
        insets.bottom = padding;
        return this;
    }

    /**
     * Zentriert das Element in der Zelle
     *
     * @return GbcBuilder-Objekt
     */
    public GbcBuilder center() {
        anchor = GridBagConstraints.CENTER;
        return this;
    }

    /**
     * Setzt die Ausrichtung nach links
     *
     * @return GbcBuilder-Objekt
     */
    public GbcBuilder left() {
        anchor = GridBagConstraints.LINE_START;
        return this;
    }

    /**
     * Setzt den Innenabstand nach oben und unten fest
     *
     * @param verticalInset der Abstand
     * @return GbcBuilder-Objekt
     */
    public GbcBuilder vertical(int verticalInset) {
        insets.bottom = verticalInset;
        insets.top = verticalInset;
        return this;
    }

    /**
     * Setzt das Gewicht auf der X-Achse
     *
     * @param weightx Gewichtung auf X-Achse (für Verteilung des freien Platzes)
     * @return GbcBuilder-Objekt
     */
    public GbcBuilder weightx(int weightx) {
        this.weightx = weightx;
        return this;
    }
}
