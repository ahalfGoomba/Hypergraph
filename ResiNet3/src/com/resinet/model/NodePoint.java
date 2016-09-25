package com.resinet.model;

import java.awt.geom.Ellipse2D;

public class NodePoint extends Ellipse2D.Double {
    private static final long serialVersionUID = -9017839205054461834L;

    public boolean c_node = false;
    public boolean selected;
    private boolean selectedForHyperEdge;

    /**
     * Erstellt einen neuen Knoten
     *
     * @param x      die X-Koordinate
     * @param y      die y-Koordinate
     * @param c_node Ob der Knoten ein Terminalknoten ist
     */
    public NodePoint(double x, double y, boolean c_node) {
        super(x, y, 20, 20);
        this.c_node = c_node;
        selected = false;
        
    }

    /**
     * Gibt eine Kopie dieses Knotens wieder, der um 2 Pixel in alle Richtungen größer ist und 1 Pixel nach links und 1
     * Pixel nach oben verschoben ist
     *
     * @return vergrößerter Knoten
     */
    public NodePoint grow() {
        NodePoint np = new NodePoint(x - 1, y - 1, c_node);
        np.width += 2;
        np.height += 2;
        return np;
    }

    /**
     * Gibt eine Kopie dieses Knotens wieder, der um 1 Pixel in alle Richtungen kleiner ist und 1 Pixel nach unten und 1
     * Pixel nach rechts verschoben ist
     *
     * @return verkleinerter Knoten
     */
    public NodePoint shrink() {
        NodePoint np = new NodePoint(x + 1, y + 1, c_node);
        np.width -= 1;
        np.height -= 1;
        return np;
    }
    
    public void setSelected(boolean isSelected){
    	selected = isSelected;
    	selectedForHyperEdge = isSelected;
  
    }
    
    public boolean getSelected(){

    	return selected;
    	
    }
//    
//    public void setSelectedForHyperEdge(boolean isSelected){
//    	selectedForHyperEdge = isSelected;
//    }
//    
    public boolean getSelectedForHyperEdge(){
    	return selectedForHyperEdge;
    }
}
