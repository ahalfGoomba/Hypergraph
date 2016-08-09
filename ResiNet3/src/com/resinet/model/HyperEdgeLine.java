package com.resinet.model;

import java.awt.geom.Line2D;

public class HyperEdgeLine extends Line2D.Double {
	
	public NodePoint startNode;
	public HyperEdgePoint hyperEdgePoint;
	
	public HyperEdgeLine ( NodePoint start, HyperEdgePoint end){
		super();
		startNode = start;
		hyperEdgePoint = end;
		setLine(startNode.x, startNode.y, hyperEdgePoint.x, hyperEdgePoint.y);
	}

}
