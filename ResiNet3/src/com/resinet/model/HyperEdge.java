package com.resinet.model;


import java.io.Serializable;
import java.util.ArrayList;

/*
 * Hyperkante für die Berechnungen
 */
public class HyperEdge extends GraphElement implements Serializable {
	

	
	public ArrayList<Node> nodeList;
	
	public HyperEdge(ArrayList<Node> nodes){
		nodeList = nodes;		
	}
	

}
