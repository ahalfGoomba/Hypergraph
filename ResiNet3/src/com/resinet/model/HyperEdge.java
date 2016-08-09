package com.resinet.model;


import java.io.Serializable;
import java.util.ArrayList;

/*
 * Hyperkante für die Berechnungen
 */
public class HyperEdge extends GraphElement implements Serializable {
	
	private final int hyperEdge_no;
	
	public ArrayList<Node> nodeList;
	
	public HyperEdge(int no, ArrayList<Node> nodes){
		hyperEdge_no = no;
		nodeList = nodes;		
	}
	

}
