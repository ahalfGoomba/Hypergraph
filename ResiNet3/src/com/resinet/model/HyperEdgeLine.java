package com.resinet.model;

import java.awt.geom.Line2D;

public class HyperEdgeLine extends Line2D.Double {
	
	public NodePoint startNode;
	public HyperEdgePoint hyperEdgePoint;
	
	public HyperEdgeLine ( NodePoint start, HyperEdgePoint end){
		super();
		startNode = start;
		hyperEdgePoint = end;
		setLine(startNode.x + 10, startNode.y + 10, hyperEdgePoint.x + 5, hyperEdgePoint.y + 5);
	}

}
