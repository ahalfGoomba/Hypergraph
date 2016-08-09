package com.resinet.util;

import com.resinet.model.GraphWrapper;

@FunctionalInterface
public interface GraphGeneratorListener {
    void graphGenerated(GraphWrapper graphWrapper);
}