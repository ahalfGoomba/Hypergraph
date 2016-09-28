package com.resinet.views;

import com.resinet.controller.NetPanelController;
import com.resinet.model.BorderRectangle;
import com.resinet.model.EdgeLine;
import com.resinet.model.HyperEdgePoint;
import com.resinet.model.HyperEdgeLine;
import com.resinet.model.GraphWrapper;
import com.resinet.model.NodePoint;
import com.resinet.util.GraphChangedListener;
import com.resinet.util.GraphUtil;
import com.resinet.util.NetPanelTransferHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class NetPanel extends JPanel {
    private static final long serialVersionUID = -124106422709849520L;

    private final NetPanelController controller;

    public final Timer selectionAnimationTimer;
    private float selectionAnimationPhase = 0;

    private boolean centerGraphOnNextPaint = false;
    private boolean coloredHyperedge = false;
    private boolean ellipseMode = false;
    private int alphaValue = 130;
    private final Cursor switchCursor, deleteCursor, plusCursor;
 

    public NetPanel(GraphChangedListener listener) {
        controller = new NetPanelController(this, listener);

        //EventListener setzen
        addMouseListener(controller);
        addMouseMotionListener(controller);
        addKeyListener(new MyKeyListener());

        //Standardcursor setzen
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

        /**
         * Damit Tastendrücke erkannt werden können
         */
        setFocusable(true);

        setOpaque(true);

        //Eigene Cursor initialisieren
        Image switchCursorImage = getToolkit().getImage(ClassLoader.getSystemResource("com/resinet/img/cursor_state_switch.png"));
        Image deleteCursorImage = getToolkit().getImage(ClassLoader.getSystemResource("com/resinet/img/cursor_delete.png"));
        Image plusCursorImage = getToolkit().getImage(ClassLoader.getSystemResource("com/resinet/img/cursor_plus.png"));

        switchCursor = getToolkit().createCustomCursor(switchCursorImage, new Point(0, 0), "Switch State");
        deleteCursor = getToolkit().createCustomCursor(deleteCursorImage, new Point(0, 0), "Delete Element");
        plusCursor = getToolkit().createCustomCursor(plusCursorImage, new Point(0, 0), "Select Node");

        selectionAnimationTimer = new Timer(30, (e) -> {
            //modulo, damit es nicht nach langer zeit zu einer Exception kommen kann
            selectionAnimationPhase = (selectionAnimationPhase + 0.5f) % 4;
            repaint();
        });

        //Aktionen für Kopieren, Ausschneiden und Einfügen registrieren
        ActionMap actionMap = this.getActionMap();
        actionMap.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
        actionMap.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
        actionMap.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());

        //Handler für kopieren und einfügen
        setTransferHandler(new NetPanelTransferHandler());
    }

    @Override
    public void paintComponent(Graphics g) {
 
    	List<NodePoint> drawnNodes = controller.getNodes();
        List<EdgeLine> drawnEdges = controller.getEdges();
        List<HyperEdgePoint> drawnHyperEdgePoints = controller.getHyperEdgePoints();
        List<HyperEdgeLine> drawnHyperEdgeLines = controller.getHyperEdgeLines();
        if (centerGraphOnNextPaint) {
            centerGraphOnNextPaint = false;
            controller.centerGraph();
        }

        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D imgGraphics = img.createGraphics();
        //Kantenglättung aktivieren
        imgGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        imgGraphics.setColor(Color.WHITE);
        //Hintergrund zeichnen
        imgGraphics.fillRect(0, 0, getWidth(), getHeight());
             	
        int colorCounter = 1;
        boolean bright = false;
        Color color1 = Color.black;

        for (HyperEdgePoint hep : drawnHyperEdgePoints){
          	if(coloredHyperedge){
          		switch(colorCounter){
          		case 1: color1 = Color.blue;
          			break;
          		case 2: color1 = Color.red;
          			break;
          		case 3: color1 = Color.green;
          			break;
          		case 4: color1 = Color.cyan;
          			break;
          		case 5: color1 = Color.orange;
          			break;
          		case 6: color1 = Color.magenta;
          			break;
          		case 7: color1 = Color.darkGray;
          			break;
          		case 8: color1 = Color.yellow;
          			break;
          		case 9: color1 = Color.gray;
          			break;
          		case 10: color1 = Color.lightGray;
          			break;
          		default: color1 = Color.black;
          			colorCounter =  0;
          			bright = true;
          			break;       
          		
          		}

          		colorCounter++;
          		if(bright){
          			color1.brighter();
          		}
          	}
          
          	hep.setColor(color1);
          	
        }
        
   //wenn der ellipseMode aus ist werden die kanten der Hyperedge in standart darstellugn gezeichnet
        
        if(!ellipseMode){        
        for(HyperEdgeLine hel : drawnHyperEdgeLines) {
  drawHyperEdgeLine(imgGraphics, hel, hel.hyperEdgePoint.getColor());  
        }
    
        }
        
        //HyperEdgePoint zeichen
       
        for (HyperEdgePoint hep : drawnHyperEdgePoints) {
        	
        	imgGraphics.setColor(hep.getColor());
        	List<NodePoint> nodes = hep.getNodePoints();
        	List<HyperEdgePoint> emptyList = new ArrayList<HyperEdgePoint>(); 
        	
        	
        	if(ellipseMode){
        	BorderRectangle border = GraphUtil.getGraphBounds(nodes, emptyList , 1);
        	Ellipse2D hyperEdgeBorder = new Ellipse2D.Double(1,1,1,1);
        	hyperEdgeBorder.setFrameFromCenter(border.getCenterX(), border.getCenterY(), border.getMinX() - border.getWidth()/5, border.getMaxY() + border.getHeight()/5);
            imgGraphics.setColor(hep.getColor());
            imgGraphics.draw(hyperEdgeBorder);
            //alpha wert der f�llfarbe �ndern
            Color color = new Color(hep.getColor().getRed(), hep.getColor().getGreen(), hep.getColor().getBlue(), alphaValue);
            imgGraphics.setColor(color);
            imgGraphics.fill(hyperEdgeBorder);
        	} else {            
            drawHyperEdgePoint(imgGraphics, hep, hep.getColor());  
        	}
        }

        //erst Kanten zeichnen, damit danach das Stück im inneren der Knoten überschrieben werden kann
        //und die Kanten demzufolge nur bis zu den Rändern der Knoten gehen
       
        imgGraphics.setColor(Color.black);
        for (EdgeLine edgeLine : drawnEdges) {
            if (edgeLine.equals(controller.getHoveredElement())) {
                imgGraphics.setStroke(new BasicStroke(2));
                imgGraphics.draw(edgeLine);
                imgGraphics.setStroke(new BasicStroke(1));
            } else {
                imgGraphics.draw(edgeLine);
            }

            String s = String.valueOf(drawnEdges.indexOf(edgeLine));
            imgGraphics.drawString(s, (float) edgeLine.textPositionX, (float) edgeLine.textPositionY);
        }
        
        //Knoten zeichnen
        int count = 0;
        for (NodePoint nodePoint : drawnNodes) {
      
            		imgGraphics.setColor(Color.black);
            	
           
            drawNode(imgGraphics, nodePoint);

            //Zahl im Knoten zeichnen
            String s = String.valueOf(count);
            if (count < 10)
                imgGraphics.drawString(s, (float) nodePoint.getX() + 6, (float) nodePoint.getY() + 15);
            else
                imgGraphics.drawString(s, (float) nodePoint.getX() + 3, (float) nodePoint.getY() + 15);
            count++;
        }

        imgGraphics.setColor(Color.BLACK);

        EdgeLine draggingLine = controller.getDraggingLine();

        //Linie während des Kantenziehens nur zeichnen, wenn die Maus bewegt wurde, also auch ein zweiter Punkt gesetzt wurde
        if (controller.isNewLineDragging() && draggingLine.x2 > 0 && draggingLine.y2 > 0) {
            imgGraphics.draw(draggingLine);
        }

        //Kasten zum auswählen zeichnen
        if (controller.isSelectDragging()) {
            //gestrichelte Linie
            imgGraphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1, 2}, 0));
            Rectangle selectRect = new Rectangle(controller.getSelectStartPoint());
            selectRect.add(controller.getCurrentMousePosition());
            imgGraphics.draw(selectRect);
        } else if (controller.isNodesSelected() || controller.isHyperEdgePointSelected()) {
            //Kasten um die ausgewählten Knoten zeichnen, wenn nicht gerade neu ausgewählt wird
            //animiert gestrichelte Linie
            imgGraphics.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 2}, selectionAnimationPhase));
            imgGraphics.draw(controller.getSelectionRectangle());
        } 

        g.drawImage(img, 0, 0, this);
    }

    /**
     * Zeichnet ein Knoten. Der Knoten wird hervorgehoben, wenn die Maus sich darüber findet
     *
     * @param imgGraphics die Zielgraphik
     * @param nodePoint   Der zu zeichnende Knoten
     */
    private void drawNode(Graphics2D imgGraphics, NodePoint nodePoint) {
        imgGraphics.setColor(Color.black);
        if (nodePoint.equals(controller.getHoveredElement())) {

            //wenn die Maus darüber ist, fett zeichnen
            if (nodePoint.c_node) {
                //oder wenns ein Terminalknoten ist, leicht größer füllen
                imgGraphics.fill(nodePoint.grow());
            } else {
                //Kreis erst weiß ausfüllen, damit die Kanten dadrin überschrieben werden
                imgGraphics.setColor(Color.white);
                imgGraphics.fill(nodePoint);
                imgGraphics.setColor(Color.black);

                imgGraphics.setStroke(new BasicStroke(2));
                imgGraphics.draw(nodePoint);
            }
        } else {
            if (nodePoint.c_node) {
                if (nodePoint.selected) {
                    //animierte umrandung, falls ausgewählt
                    //kleineren schwarzen Knoten ausgefüllt zeichnen
                    imgGraphics.fill(nodePoint.shrink());

                    imgGraphics.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 2}, selectionAnimationPhase));
                    imgGraphics.draw(nodePoint);
                } else {
                    //schwarz ausfüllen
                    imgGraphics.fill(nodePoint);
                }
            } else {
                //Kreis erst weiß ausfüllen, damit die Kanten dadrin überschrieben werden
                imgGraphics.setColor(Color.white);
                imgGraphics.fill(nodePoint);
                imgGraphics.setColor(Color.black);

                //Falls der Knoten ausgewählt ist, gestrichelte Umrandung zeichnen
                if (nodePoint.selected || nodePoint.getSelectedForHyperEdge()) {
                    imgGraphics.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 2}, selectionAnimationPhase));
                }

                imgGraphics.draw(nodePoint);
            }
        }

        if (nodePoint.c_node) {
            //Textfarbe weiß, da jetzt Hintergrund schwarz ist
            imgGraphics.setColor(Color.white);
        }
        imgGraphics.setStroke(new BasicStroke(1));
    }

    
    /**
     * Zeichnet einen HyperedgePoint
     */
    private void drawHyperEdgePoint(Graphics2D imgGraphics, HyperEdgePoint hep, Color color){
    	
    	 imgGraphics.draw(hep);
    	 imgGraphics.setColor(Color.white);
         imgGraphics.fill(hep);
         
         if (hep.equals(controller.getHoveredElement())) {
             imgGraphics.setColor(Color.white);
             imgGraphics.fill(hep);
             imgGraphics.setColor(Color.black);

             imgGraphics.setStroke(new BasicStroke(2));
             imgGraphics.draw(hep);
        	 
         }
         else{
             imgGraphics.setColor(Color.white);
             imgGraphics.fill(hep);
            
             imgGraphics.setColor(color);

            
             if (hep.getSelected()) {
                 imgGraphics.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{2, 2}, selectionAnimationPhase));
             }

             imgGraphics.draw(hep);
         }
         imgGraphics.setStroke(new BasicStroke(1));	
    }
    
    private void drawHyperEdgeLine(Graphics2D imgGraphics, HyperEdgeLine hel, Color color){
    	
    	imgGraphics.setColor(color);
    	imgGraphics.draw(hel);
    	
    }

    /**
     * Setzt die Flag, dass beim nächsten Zeichnen der Graph zentriert wird
     */
    public void centerGraphOnNextPaint() {
        centerGraphOnNextPaint = true;
    }

    /**
     * Setzt die bevorzugte Größe auf die vom Graphen eingenommene Fläche inklusive Offsets + 10 Pixel
     *
     * @return Bevorzugte Größe
     */
    @Override
    public Dimension getPreferredSize() {
        Rectangle2D graphRect = GraphUtil.getGraphBounds(controller.getNodes(), controller.getHyperEdgePoints());

        return new Dimension((int) (graphRect.getX() + graphRect.getWidth()) + 10,
                (int) (graphRect.getY() + graphRect.getHeight()) + 10);
    }

    /**
     * Setzt den Graph zurück
     */
    public void resetGraph() {
        controller.resetGraph();
    }

    /**
     * Wird vom Mainframe-Controller weitergegeben, wenn das NetPanel Fokus hat und dient dazu, Copy&Paste-Aktionen zu
     * behandeln.
     *
     * @param e Das ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        controller.actionPerformed(e);
    }

    /**
     * Reagiert auf Tastendrücke von Strg und Shift und verändert den Cursor entsprechend
     */
    private class MyKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent keyEvent) {
            setCursorHover(keyEvent.isShiftDown(), keyEvent.isControlDown());
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            setCursorHover(keyEvent.isShiftDown(), keyEvent.isControlDown());
        }
    }

    /**
     * Setzt den Cursor nach angegebenen Parametern
     *
     * @param shiftDown   Ob Shift gedrückt ist
     * @param controlDown Ob Strg gedrückt ist
     */
    public void setCursorHover(boolean shiftDown, boolean controlDown) {
        Shape hoveredElement = controller.getHoveredElement();

        if (controller.isCursorOnSelectionBorder()) {
            //wenn border oben oder unten ist
            switch (controller.getResizeBorder()) {
                case 5:
                    setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
                    break;
                case 6:
                    setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
                    break;
                case 7:
                    setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
                    break;
                case 8:
                    setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
                    break;
                case 2:
                case 4:
                    setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                    break;
                default: // 1 oder 3 bzw. links oder rechts
                    setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
                    break;
            }
        } else if (controller.isCursorInsideSelection()) {
            setCursor(new Cursor(Cursor.MOVE_CURSOR));
        } else if (shiftDown && hoveredElement != null) {
            setCursor(deleteCursor);
        } else if (controlDown && hoveredElement instanceof NodePoint) {
            setCursor(plusCursor);
        } else if (((hoveredElement instanceof NodePoint && controller.isNodeClickable()) ||
                (hoveredElement instanceof EdgeLine && controller.isEdgeClickable()) || 
                (hoveredElement instanceof HyperEdgePoint && controller.isHyperEdgePointClickable()))) {
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    /**
     * Fügt eine Menge von Knoten und Kanten hinzu.
     *
     * @param nodes Die Knotenmenge
     * @param edges Die Kantenmenge
     */
    public void addNodesAndEdges(List<NodePoint> nodes, List<EdgeLine> edges) {
        controller.addNodesAndEdges(nodes, edges);
    }

    /**
     * Fügt eine Menge von Knoten und Kanten hinzu.
     *
     * @param graphWrapper Wrapper mit Mengen von Knoten und Kanten
     */
    public void addGraphWrapperAndSelect(GraphWrapper graphWrapper) {
        controller.addGraphWrapperAndSelect(graphWrapper);
    }

    /**
     * Gibt zurück, ob rückgängig gemacht werden kann.
     *
     * @return True, wenn rückgängig gemacht werden kann.
     */
    public boolean canUndo() {
        return controller.getNetData().canUndo();
    }

    /**
     * Gibt zurück, ob wiederholt werden kann.
     *
     * @return True, wenn wiederholt werden kann.
     */
    public boolean canRedo() {
        return controller.getNetData().canRedo();
    }
    
    public void setColoredHypergraph(boolean value){
    	coloredHyperedge = value;
    }

    public List<NodePoint> getNodes() {
        return controller.getNodes();
    }

    public List<EdgeLine> getEdges() {
        return controller.getEdges();
    }
    
    public List<HyperEdgePoint> getHyperEdgePoints(){
    	return controller.getHyperEdgePoints();
    }
    
    public NetPanelController getController() {
        return controller;
    }
    
    public void setAlphaValue(int value){
    	alphaValue = value;
    }
    
    public void setEllipseMode(boolean mode){
    	ellipseMode = mode;
    }
}
