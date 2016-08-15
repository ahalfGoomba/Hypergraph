package com.resinet.model;

import java.awt.geom.Ellipse2D;

public class HyperEdgePoint extends Ellipse2D.Double {
	
	private static final long serialVersionUID = 1L;
	public boolean selected = false;
	
	   /**
    * Erstellt einen Knoten als Zentrum der Hyperkante(kein Knoten des Graphen)
    *
    * @param x      die X-Koordinate
    * @param y      die y-Koordinate
    */
   public HyperEdgePoint(double x, double y) {
       super(x, y, 10, 10);

   }

}
