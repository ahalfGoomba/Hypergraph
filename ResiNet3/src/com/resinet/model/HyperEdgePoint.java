package com.resinet.model;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

public class HyperEdgePoint extends Ellipse2D.Double {
	

	private static final long serialVersionUID = -2331803725388061479L;
	public boolean selected = false;
	private Color color;
	private List<HyperEdgeLine> edges;

	
	/**
    * Erstellt einen Knoten als Zentrum der Hyperkante(kein Knoten des Graphen)
    *
    * @param x      die X-Koordinate
    * @param y      die y-Koordinate
    */
   public HyperEdgePoint(double x, double y) {
       super(x, y, 10, 10);
       selected = false;
       edges = new ArrayList<HyperEdgeLine>();
   }
   
   public void setSelected(boolean isSelected){
   	selected = isSelected;
 
   }
   
   public void setColor(Color clr){
	   color = clr;
   }
   
   public boolean getSelected(){
   	return selected;
   }
   
   public Color getColor(){
	   return color;
   }
   
   public void addHyperEdgeLine(HyperEdgeLine line){
	   edges.add(line);
   }
   
   public List<HyperEdgeLine> getHyperedges(){
	   return edges;
   }
 
}
