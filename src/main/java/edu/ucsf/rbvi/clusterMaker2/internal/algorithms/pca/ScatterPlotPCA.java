/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import org.jdesktop.swingx.JXCollapsiblePane;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

/**
 *
 * @author root
 */
@SuppressWarnings("serial")
public class ScatterPlotPCA extends JPanel implements MouseListener, MouseMotionListener{
	private float scale = 1;
	private static int MAX_SCORE = 1;
	private static int MIN_SCORE = -1;
	private static final int PREF_W = 500;
	private static final int PREF_H = 500;
	private static final int BORDER_GAP = 10;
	private static final int LABEL_GAP = 40;
	private static final int GRAPH_HATCH_WIDTH = 2;
	private int graph_point_width = 6;

	private final Matrix loadings;
	private final CyMatrix[] scores;
	private final int xIndex;
	private final int yIndex;
	private final Color pointColor;
	private final int pointWidth;

	private List<Point> graphPoints;
	private Map<String, Color> colorMap;

	private int startingX, startingY, currentX, currentY, previousDX=0, previousDY=0;
	private boolean dragging = false;

	public ScatterPlotPCA(CyMatrix[] scores, Matrix loadings, 
	                      int x, int y, Color pointColor, int pointWidth,
				                Map<String, Color> colorMap) {
		this.scores = scores;
		this.loadings = loadings;
		this.xIndex = x;
		this.yIndex = y;
		this.pointColor = pointColor;
		this.pointWidth = pointWidth;
		this.colorMap = colorMap;

		double max = scores[xIndex].getMaxValue();
		double min = scores[xIndex].getMinValue();
		System.out.println("min,max = "+min+","+max);
		if(max > MAX_SCORE || min < MIN_SCORE){
			if(max > Math.abs(min)){
				MAX_SCORE = (int) Math.ceil(max);
				MIN_SCORE = (int) ((int) -1 * Math.ceil(max));
			}else{
				MAX_SCORE = (int) Math.ceil(Math.abs(min));
				MIN_SCORE = (int) ((int) -1 * Math.ceil(Math.abs(min)));
			}
		}

		System.out.println("min,max = "+MIN_SCORE+","+MAX_SCORE);

		addMouseWheelListener(new MouseAdapter() {

				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					double delta = 0.05f * e.getPreciseWheelRotation();
					scale += delta;

					// Move the panel so our mouse is in the same
					// place after the zoom
					Point point = e.getPoint();
					double dx = point.x*delta;
					double dy = point.y*delta;
					previousDX -= dx;
					previousDY -= dy;

					revalidate();
					repaint();
				}

			});

		addMouseListener(this);

		addMouseMotionListener(this);

	}

	@Override
	public Dimension getPreferredSize() { return new Dimension(PREF_W, PREF_H); }

	
	public void mouseClicked(MouseEvent event) {
		int x = event.getPoint().x;
		int y = event.getPoint().y;
		System.out.println("getPoint: " + event.getPoint().x + " " + event.getPoint().y);
		System.out.println("getXYOnScreen: " + event.getXOnScreen() + " " + event.getYOnScreen());
		if(!graphPoints.isEmpty()){
			for(int i=0;i<graphPoints.size();i++){
				Point p = graphPoints.get(i);
				if(Math.abs(p.x - x) <= graph_point_width && Math.abs(p.y - y) <= graph_point_width){
					System.out.println("i and j: " + Math.floor(i/scores[xIndex].nRows()) + " " + 
					                   i%scores[xIndex].nRows());
					System.out.println("Node: "+scores[xIndex].getRowLabel(i)+" x="+
					                   scores[xIndex].getValue(0,i)+", y="+scores[yIndex].getValue(0,i));
					CyNetwork network = scores[xIndex].getNetwork();
					CyNode node = scores[xIndex].getRowNode(i);
					network.getRow(node).set(CyNetwork.SELECTED, true);
					break;
				}
			}
		}
	}

	
	public void mouseEntered(MouseEvent event) {
	}

	
	public void mouseExited(MouseEvent event) {
	}

	
	public void mousePressed(MouseEvent event) {
		Point point = event.getPoint();
		startingX = point.x;
		startingY = point.y;
		dragging = true;
	}

	
	public void mouseReleased(MouseEvent event) {
		dragging = false;
		previousDX += currentX - startingX;
		previousDY += currentY - startingY;
	}

	
	public void mouseDragged(MouseEvent event) {
		Point p = event.getPoint();
		currentX = p.x;
		currentY = p.y;
		if (dragging) {
			repaint();
		}
	}

	public void mouseMoved(MouseEvent me){
	}

	@Override
	protected void paintComponent(Graphics g) {
	  super.paintComponent(g);
		String labelX = loadings.getColumnLabel(xIndex);
		String labelY = loadings.getColumnLabel(yIndex);

		int plotWidth = getWidth()-(BORDER_GAP*2)-LABEL_GAP;
		int plotHeight = getHeight()-(BORDER_GAP*2)-LABEL_GAP;

	  Graphics2D g2 = (Graphics2D)g;
	  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	  AffineTransform at = new AffineTransform();
	  if(dragging){
			int currentDX = currentX - startingX;
			int currentDY =  currentY - startingY;
			at.setToTranslation(previousDX + currentDX, previousDY + currentDY);
	  } else {
			at.setToTranslation(previousDX, previousDY);
		}
	  at.scale(scale, scale);
	  g2.setTransform(at);

	  double xScale = ((double) plotWidth) / (MAX_SCORE - MIN_SCORE);
	  double yScale = ((double) plotHeight) / (MAX_SCORE - MIN_SCORE);

		int scaleFontSize = 8;
		int labelFontSize = 12;
		int fontChange = MAX_SCORE/10;
		if (fontChange > 1) {
			scaleFontSize = scaleFontSize-fontChange*1;
		}

		Font scaleFont = new Font("default", Font.PLAIN, scaleFontSize);
		Font labelFont = new Font("default", Font.PLAIN, labelFontSize);

		g2.setFont(scaleFont);
		// create hatch marks for y axis.
		for (int i = 0; i <= MAX_SCORE - MIN_SCORE; i++) {
			g2.setColor(Color.WHITE);

			int x0 = BORDER_GAP+LABEL_GAP;
			int x1 = x0+plotWidth;
			int y0 = (int) (BORDER_GAP + i * yScale);
			int y1 = y0;
			g2.drawLine(x0, y0, x1, y1);

			// Get the label
			g2.setColor(Color.BLACK);
			String number = "" + ( MAX_SCORE - i);

			// Figure out the string width
			FontMetrics fm = g2.getFontMetrics();
			int stringWidth = fm.stringWidth(number);
			int stringHeight = fm.getHeight();

			g2.drawString(number, x0 - stringWidth - GRAPH_HATCH_WIDTH, 
			              y1+(int)((stringHeight-GRAPH_HATCH_WIDTH)/2.0));
		}

		// and for x axis
		g2.setFont(scaleFont);
		for (int i = 0; i <= MAX_SCORE - MIN_SCORE; i++) {
			g2.setColor(Color.WHITE);
			int x0 = (int) (LABEL_GAP+BORDER_GAP + i * xScale);
			int x1 = x0;
			int y0 = BORDER_GAP;
			int y1 = BORDER_GAP+plotHeight;
			g2.drawLine(x0, y0, x1, y1);

			// Get the label
			String number = "" + -1 * ( MAX_SCORE - i);

			// Figure out the string width
			FontMetrics fm = g2.getFontMetrics();
			int stringWidth = fm.stringWidth(number);
			int stringHeight = fm.getHeight();

			g2.setColor(Color.BLACK);
			g2.drawString(number, x1-(int)(stringWidth/2.0), y1+stringHeight);
		}

		g2.setFont(labelFont);
		FontMetrics fm = g2.getFontMetrics();
		int stringWidth = fm.stringWidth(labelX);
		g2.drawString(labelX, 
		              BORDER_GAP+LABEL_GAP+plotWidth/2 - (int)(stringWidth/2.0), 
	                plotHeight + BORDER_GAP + (int)((LABEL_GAP+fm.getHeight())/2.0));

		AffineTransform savedTF = g2.getTransform();
		AffineTransform af = (AffineTransform)savedTF.clone();

		int xStart = BORDER_GAP+(int)(LABEL_GAP/2.0)-(int)(stringWidth/2.0);
		int yStart = (int)(plotHeight/2.0+BORDER_GAP+fm.getHeight()/2.0);
		af.rotate(-1.57, xStart, yStart);
		g2.setTransform(af);
		g2.drawString(labelY, xStart, yStart+fm.getHeight()/2);
		g2.setTransform(savedTF);

		Rectangle2D plot = new Rectangle2D.Float(BORDER_GAP+LABEL_GAP, BORDER_GAP, 
		                                         plotWidth, plotHeight);
		g2.draw(plot);
		g2.drawLine(BORDER_GAP+LABEL_GAP, BORDER_GAP+plotHeight/2,
		            BORDER_GAP+LABEL_GAP+plotWidth, BORDER_GAP+plotHeight/2);
		g2.drawLine(LABEL_GAP+BORDER_GAP+plotWidth/2, BORDER_GAP,
								LABEL_GAP+BORDER_GAP+plotWidth/2, BORDER_GAP+plotHeight);

		int newX = (int)(plotWidth/2.0)+LABEL_GAP+BORDER_GAP;
		int newY = (int)(plotHeight/2.0)+BORDER_GAP;

		graphPoints = new ArrayList<Point>();
		for(int i=0; i<scores[xIndex].nRows();i++){
			for(int j=0;j<scores[xIndex].nColumns();j++){
				int x1 = (int) (scores[xIndex].getValue(i,j) * xScale + newX);
				int y1 = (int) (-1 * (scores[yIndex].getValue(i,j) * yScale - newY));
				graphPoints.add(new Point(x1, y1));
			}
		}
		g2.setColor(pointColor);
		graph_point_width = pointWidth;
		for (Point graphPoint : graphPoints) {
			int x = graphPoint.x - graph_point_width / 2;
			int y = graphPoint.y - graph_point_width / 2;
			int ovalW = graph_point_width;
			int ovalH = graph_point_width;
			g2.fillOval(x, y, ovalW, ovalH);
		}

		// Draw loadings
		for (int row = 0; row < loadings.nRows(); row++) {
			int x1 = newX;
			int y1 = newY;
			int x2 = (int) (loadings.getValue(row, xIndex) * xScale * MAX_SCORE + newX);
			int y2 = (int) (-1 * (loadings.getValue(row, yIndex) * yScale * MAX_SCORE - newY));
			String label = loadings.getRowLabel(row);
			if (colorMap.containsKey(label))
				drawArrow(g2, x1, y1, x2, y2, colorMap.get(label));
			else
				drawArrow(g2, x1, y1, x2, y2, Color.RED);
		}
	}

	public void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2, Color color) {
		// Draw our line
		BasicStroke stroke = new BasicStroke(2.0f);
	  g2.setColor(color);
		g2.setStroke(stroke);
		g2.drawLine(x1, y1, x2, y2);

		// Set up the transform
		AffineTransform oldTx = g2.getTransform();
		AffineTransform tx = new AffineTransform(oldTx);
		int dx = x2 - x1, dy = y2 - y1;
		double angle = Math.atan2(dy, dx);
		tx.translate(x2, y2);
		// tx.rotate((angle-Math.PI/2d));
		tx.rotate((angle));

		// Draw the arrowhead
		g2.setTransform(tx);
		float arrowRatio = 0.5f;
		float arrowLength = 10.0f;
		float endX = 10.0f;
		float veeX = endX - stroke.getLineWidth() * 0.5f / arrowRatio;
		Path2D.Float path = new Path2D.Float();
		float waisting = 0.5f;

		float waistX = endX - arrowLength * 0.5f;
		// float waistX = endX + arrowLength;
		// float waistX = endX;
		float waistY = arrowRatio * arrowLength * 0.5f * waisting;
		float arrowWidth = arrowRatio*arrowLength;
		path.moveTo (veeX - arrowLength, -arrowWidth);
		path.quadTo (waistX, -waistY, endX, 0.0f);
		path.quadTo (waistX, waistY, veeX - arrowLength, arrowWidth );

		path.lineTo (veeX - arrowLength * 0.75f, 0.0f );
		path.lineTo (veeX - arrowLength, -arrowWidth);

		g2.fill(path);
		g2.setTransform(oldTx);
	}

}
