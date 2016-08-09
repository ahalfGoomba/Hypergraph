package com.resinet.util;

/**
 * Interface, welches Konstanten verfügbar macht
 */
public interface Constants {

    enum CALCULATION_MODES {
        RESILIENCE,
        RELIABILITY,
    }

    enum GUI_STATES {
        ENTER_GRAPH,
        CALCULATION_RUNNING
    }

    enum RELIABILITY_MODES {
        SINGLE,
        SAME
    }

}
