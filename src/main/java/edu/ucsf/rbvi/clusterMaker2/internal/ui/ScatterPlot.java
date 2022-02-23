/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.swing.*;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.log4j.Logger;
import org.freehep.graphicsio.ps.PSGraphics2D;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.freehep.graphics2d.VectorGraphics;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ViewUtils;

/**
 *
 * @author root
 */
@SuppressWarnings("serial")
public class ScatterPlot extends JPanel implements MouseListener, MouseMotionListener{
  static final Logger logger = Logger.getLogger(CyUserLog.NAME);

	private float scale = 1;
	private int MAX_SCORE = 1;
	private int MIN_SCORE = -1;
	private static final int PREF_W = 500;
	private static final int PREF_H = 500;
	private static final int BORDER_GAP = 10;
	private static final int LABEL_GAP = 40;
	private static final int XSTART = BORDER_GAP+LABEL_GAP;
	private static final int YSTART = BORDER_GAP;
	private static final int GRAPH_HATCH_WIDTH = 2;
	private int graph_point_width = 1;

	private final Matrix loadings;
	private final CyMatrix[] scores;
	private final int xIndex;
	private final int yIndex;
	private Color pointColor;
	private int pointWidth;
	private final ClusterManager manager;

	private List<Point> graphPoints;
	private Map<String, Color> colorMap;

	private final Font scaleFont;
	private final Font labelFont;

	// Mouse management
	private int startingX, startingY, currentX, currentY, previousDX=0, previousDY=0;
	private boolean dragging = false;
	private boolean shift = false;

	private boolean useLoadings;

	public ScatterPlot(ClusterManager manager, CyMatrix[] scores, Matrix loadings, 
	                   int x, int y, Color pointColor, int pointWidth,
				             Map<String, Color> colorMap, boolean useLoadings) {
		this.scores = scores;
		this.loadings = loadings;
		this.useLoadings = useLoadings;
		this.xIndex = x;
		this.yIndex = y;
		this.pointColor = pointColor;
		this.pointWidth = pointWidth;
		this.colorMap = colorMap;
		this.graphPoints = new ArrayList<Point>();
		this.manager = manager;

		double max = scores[xIndex].getMaxValue();
		double min = scores[xIndex].getMinValue();
		// System.out.println("min = "+min+", max = "+max);
		if(max > MAX_SCORE || min < MIN_SCORE){
			if(max > Math.abs(min)){
				MAX_SCORE = (int) Math.ceil(max);
				MIN_SCORE = (int) ((int) -1 * Math.ceil(max));
			}else{
				MAX_SCORE = (int) Math.ceil(Math.abs(min));
				MIN_SCORE = (int) ((int) -1 * Math.ceil(Math.abs(min)));
			}
		}

		int scaleFontSize = 6;
		int labelFontSize = 12;

		scaleFont = new Font("sans-serif", Font.PLAIN, scaleFontSize);
		labelFont = new Font("sans-serif", Font.BOLD, labelFontSize);

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

  public void setPointSize(int size) {
    pointWidth = size;
    repaint();
  }

  public void setPointColor(Color color) {
    pointColor = color;
    repaint();
  }

  public void setColorMap(Map<String, Color> map) {
    colorMap = map;
    repaint();
  }

	@Override
	public Dimension getPreferredSize() { return new Dimension(PREF_W, PREF_H); }

	public void mouseClicked(MouseEvent event) {
		// Is it the correct button?
		if (event.getButton() != MouseEvent.BUTTON1)
			return;

		// Shift key down?
		shift = false;
		if ((event.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK)
			shift = true;

		int x = (int)((event.getPoint().x-previousDX)/scale);
		int y = (int)((event.getPoint().y-previousDY)/scale);
		int pointWidth = (int)(graph_point_width/scale);

		// System.out.println("getPoint: " + event.getPoint().x + " " + event.getPoint().y);
		// System.out.println("getXYOnScreen: " + event.getXOnScreen() + " " + event.getYOnScreen());
		if(!graphPoints.isEmpty()){
			boolean found = false;
			CyNetwork network = scores[xIndex].getNetwork();
			for(int i=0;i<graphPoints.size();i++){
				Point p = graphPoints.get(i);
				if(Math.abs(p.x - x) <= pointWidth && Math.abs(p.y - y) <= pointWidth){
					/*
					System.out.println("i and j: " + Math.floor(i/scores[xIndex].nRows()) + " " + 
					                   i%scores[xIndex].nRows());
					System.out.println("Node: "+scores[xIndex].getRowLabel(i)+" x="+
					                   scores[xIndex].getValue(0,i)+", y="+scores[yIndex].getValue(0,i));
					*/
					CyNode node = scores[xIndex].getRowNode(i);

					if (!shift) {
						ModelUtils.clearSelected(network, CyNode.class);
					}

					if (shift && ModelUtils.isSelected(network, node)) {
						ModelUtils.setSelected(network, node, false);
					} else {
						ModelUtils.setSelected(network, node, true);
					}
					found = true;
					break;
				}
			}
			if (!found) {
				ModelUtils.clearSelected(network, CyNode.class);
			}
		}
		shift = false;
		repaint();
	}

	
	public void mouseEntered(MouseEvent event) {
	}

	
	public void mouseExited(MouseEvent event) {
	}

	
	public void mousePressed(MouseEvent event) {
		Point point = event.getPoint();
		startingX = point.x;
		startingY = point.y;

		// Shift key down?
		shift = false;
		if ((event.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK)
			shift = true;

		dragging = true;
	}

	
	public void mouseReleased(MouseEvent event) {
		Point p = event.getPoint();
		currentX = p.x;
		currentY = p.y;

		if (shift) {
			// Find all of the points in the rectangle and select them
			shift = false;
			dragging = false;
			repaint();
			return;
		}
		dragging = false;
		previousDX += currentX - startingX;
		previousDY += currentY - startingY;
	}

	
	public void mouseDragged(MouseEvent event) {
		Point p = event.getPoint();
		currentX = p.x;
		currentY = p.y;
		if (shift) {
			int x1 = startingX;
			int x2 = currentX;
			int y1 = startingY;
			int y2 = currentY;
			if (currentX < startingX) {
				x2 = startingX;
				x1 = currentX;
			}
			if (currentY < startingY) {
				y2 = startingY;
				y1 = currentY;
			}
			// selectRange(startingX, startingY, currentX, currentY);
			selectRange(x1, y1, x2, y2);
		}
		repaint();
	}

	// Put up tooltips
	public void mouseMoved(MouseEvent me){
		Point tt = me.getPoint();
		int pointWidth = (int)(graph_point_width/scale);
		int x = (int)((tt.x-previousDX)/scale);
		int y = (int)((tt.y-previousDY)/scale);
		if (!graphPoints.isEmpty()) {
			for(int i=0;i<graphPoints.size();i++){
				Point p = graphPoints.get(i);
				if(Math.abs(p.x - x) <= pointWidth && Math.abs(p.y - y) <= pointWidth){
					// The mouse is over this circle
					CyNode node = scores[xIndex].getRowNode(i);
					setToolTipText(getLabel(scores[xIndex].getNetwork(), node));
				}
			}
		}
	}

	private void selectRange(int x1, int y1, int x2, int y2) {
		CyNetwork network = scores[xIndex].getNetwork();
		ModelUtils.clearSelected(network, CyNode.class);
		x1 = (int)((x1-previousDX)/scale);
		y1 = (int)((y1-previousDY)/scale);
		x2 = (int)((x2-previousDX)/scale);
		y2 = (int)((y2-previousDY)/scale);

		if(!graphPoints.isEmpty()){
			for(int i=0;i<graphPoints.size();i++){
				Point p = graphPoints.get(i);
				if(p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2) {
					CyNode node = scores[xIndex].getRowNode(i);
					ModelUtils.setSelected(network, node, true);
				}
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
	  super.paintComponent(g);
    drawAll(g);
  }

  private void drawAll(Graphics g) {
		String labelX = loadings.getColumnLabel(xIndex);
		String labelY = loadings.getColumnLabel(yIndex);

		int plotWidth = getWidth()-(BORDER_GAP*2)-LABEL_GAP;
		int plotHeight = getHeight()-(BORDER_GAP*2)-LABEL_GAP;

	  Graphics2D g2 = (Graphics2D)g;
	  g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	  // AffineTransform at = new AffineTransform();
	  if(dragging && !shift){
			int currentDX = currentX - startingX;
			int currentDY =  currentY - startingY;
			// at.setToTranslation(previousDX + currentDX, previousDY + currentDY);
      g2.translate(previousDX + currentDX, previousDY + currentDY);
      System.out.println("Dragging");
	  } else {
			// at.setToTranslation(previousDX, previousDY);
      g2.translate(previousDX, previousDY);
      System.out.println("Not dragging: dx = "+previousDX+" dy = "+previousDY);
		}
	  g2.scale(scale, scale);
	  // at.scale(scale, scale);
	  // g2.setTransform(at);

		drawAxes(g2, plotWidth, plotHeight, labelX, labelY);

	  double xScale = ((double) plotWidth) / (MAX_SCORE - MIN_SCORE);
	  double yScale = ((double) plotHeight) / (MAX_SCORE - MIN_SCORE);

		int newX = (int)(plotWidth/2.0)+LABEL_GAP+BORDER_GAP;
		int newY = (int)(plotHeight/2.0)+BORDER_GAP;

		// Draw the points
		graph_point_width = pointWidth;
		CyNetwork network = scores[xIndex].getNetwork();
		graphPoints.clear();
		if (scores.length == 1) {
			// Coordinate matrix
			for(int i=0; i<scores[xIndex].nRows();i++){
				CyNode node = scores[xIndex].getRowNode(i);
				boolean selected = ModelUtils.isSelected(network, node);
				int x1 = (int) (scores[0].getValue(i,0) * xScale + newX);
				int y1 = (int) (-1 * (scores[0].getValue(i,1) * yScale - newY));
				if (pointColor != null)
					drawPoint(g2, x1, y1, pointColor, selected);
				else
					drawPoint(g2, x1, y1, getColor(scores[0].getNetwork(), scores[0].getRowNode(i)), selected);
				graphPoints.add(new Point(x1, y1));
			}
		} else {
			for(int i=0; i<scores[xIndex].nRows();i++){
				CyNode node = scores[xIndex].getRowNode(i);
				for(int j=0;j<scores[xIndex].nColumns();j++){
					boolean selected = ModelUtils.isSelected(network, node);
					int x1 = (int) (scores[xIndex].getValue(i,j) * xScale + newX);
					int y1 = (int) (-1 * (scores[yIndex].getValue(i,j) * yScale - newY));
					if (pointColor != null)
						drawPoint(g2, x1, y1, pointColor, selected);
					else
						drawPoint(g2, x1, y1, getColor(scores[0].getNetwork(), scores[0].getRowNode(i)), selected);
					graphPoints.add(new Point(x1, y1));
				}
			}
		}

		if (useLoadings) {
			// Draw loadings
			for (int row = 0; row < loadings.nRows(); row++) {
				int x1 = newX;
				int y1 = newY;
				int x2 = (int) (loadings.getValue(row, xIndex) * xScale * MAX_SCORE + newX);
				int y2 = (int) (-1 * (loadings.getValue(row, yIndex) * yScale * MAX_SCORE - newY));
				String label = loadings.getRowLabel(row);
				if (colorMap.containsKey(label)) {
          if (colorMap.get(label).getAlpha() != 0) {
            drawArrow(g2, x1, y1, x2, y2, colorMap.get(label));
          }
        } else {
					drawArrow(g2, x1, y1, x2, y2, Color.RED);
        }
			}
		}

		// Finally, draw our selection rectangle if we're supposed to
		if (dragging && shift) {
			g2.setTransform(new AffineTransform());
			BasicStroke stroke = new BasicStroke(0.5f);
			g2.setStroke(stroke);
			g2.setColor(Color.BLACK);
			int x = startingX;
			int y = startingY;
			if (currentX < startingX) x = currentX;
			if (currentY < startingY) y = currentY;
			g2.drawRect(x, y, Math.abs(currentX-startingX), Math.abs(currentY-startingY));
		}
	}

	public void drawPoint(Graphics2D g2, int x1, int y1, Color color, boolean selected) {
		int x = x1 - graph_point_width / 2;
		int y = y1 - graph_point_width / 2;
		int ovalW = graph_point_width;
		int ovalH = graph_point_width;
		Shape s = new Ellipse2D.Float(x, y, ovalW, ovalH);
		float strokeWidth;
		if (graph_point_width < 6)
			strokeWidth = 1.0f;
		else
			strokeWidth = graph_point_width/3.0f;
		BasicStroke stroke = new BasicStroke(strokeWidth);
		g2.setStroke(stroke);
		if (selected) {
			g2.setColor(Color.YELLOW);
		} else {
			g2.setColor(color);
		}
		g2.draw(s);
		g2.setColor(color);
		g2.fill(s);
	}

	public void drawAxes(Graphics2D g2, int width, int height, String labelX, String labelY) {
		// Adjust the min and max values a little
		int stepSize = 1;
		if ((MAX_SCORE-MIN_SCORE) > 20) {
			int maxTicks = 10;
			stepSize = (int) Math.ceil( (double)MAX_SCORE / (double)maxTicks);
			MAX_SCORE = maxTicks * stepSize;
			MIN_SCORE = -MAX_SCORE;
		}

	  double xScale = ((double) width) / (MAX_SCORE - MIN_SCORE);
	  double yScale = ((double) height) / (MAX_SCORE - MIN_SCORE);

		// System.out.println("MAX_SCORE="+MAX_SCORE+", MIN_SCORE="+MIN_SCORE);
		// System.out.println("xScale="+xScale+", yScale="+yScale);

		g2.setFont(scaleFont);

		// create hatch marks for y axis.
		for (int i = 0; i <= MAX_SCORE - MIN_SCORE; i+=stepSize) {
			g2.setColor(Color.WHITE);

			int x0 = XSTART;
			int x1 = x0+width;
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

		for (int i = 0; i <= MAX_SCORE - MIN_SCORE; i += stepSize) {
			g2.setColor(Color.WHITE);
			int x0 = (int) (XSTART + i * xScale);
			int x1 = x0;
			int y0 = BORDER_GAP;
			int y1 = BORDER_GAP+height;
			g2.drawLine(x0, y0, x1, y1);

			// Get the label
			String number = "" + -1 * ( MAX_SCORE - i);

			// Figure out the string width
			FontMetrics fm = g2.getFontMetrics();
			int stringWidth = fm.stringWidth(number);
			int stringHeight = fm.getHeight();

			g2.setColor(Color.BLACK);
			int xLabel = x1-(int)(stringWidth/2.0);
			int yLabel = y1+stringHeight;
			g2.drawString(number, xLabel, yLabel);
		}

		g2.setFont(labelFont);
		FontMetrics fm = g2.getFontMetrics();
		int stringWidth = fm.stringWidth(labelX);
		g2.drawString(labelX, 
		              XSTART+width/2 - (int)(stringWidth/2.0), 
	                height + BORDER_GAP + (int)((LABEL_GAP+fm.getHeight())/2.0));

		AffineTransform savedTF = g2.getTransform();
		AffineTransform af = (AffineTransform)savedTF.clone();

		int xStart = BORDER_GAP+(int)(LABEL_GAP/2.0)-(int)(stringWidth/2.0);
		int yStart = (int)(height/2.0+BORDER_GAP+fm.getHeight()/2.0);
		af.rotate(-1.57, xStart, yStart);
		g2.setTransform(af);
		g2.drawString(labelY, xStart, yStart+fm.getHeight()/2);
		g2.setTransform(savedTF);

		Rectangle2D plot = new Rectangle2D.Float(XSTART, BORDER_GAP, 
		                                         width, height);
		g2.draw(plot);
		g2.drawLine(XSTART, BORDER_GAP+height/2,
		            XSTART+width, BORDER_GAP+height/2);
		g2.drawLine(XSTART+width/2, BORDER_GAP,
								XSTART+width/2, BORDER_GAP+height);
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

  public void print(String format, File file) {
    if (format.startsWith(".")) {
      format = format.substring(1);
    }
    int saveWidth = pointWidth;
    // pointWidth = 1;
		if (format.equals("png") || format.equals("jpg") || format.equals("bmp"))
			bitmapSave(format, file);
		else if (format.equals("pdf"))
			pdfSave(format, file);
		else if (format.equals("svg"))
			svgSave(format, file);

    pointWidth = saveWidth;
  }

	private Color getColor(CyNetwork network, CyNode node) {
		return ViewUtils.getColor(manager, network, node);
	}

	private String getLabel(CyNetwork network, CyNode node) {
		return ViewUtils.getLabel(manager, network, node);
	}
	
  private void pdfSave(String format, File file) {
		com.itextpdf.text.Rectangle pageSize = new com.itextpdf.text.Rectangle(getWidth(), getHeight());
    logger.info("Writing PDF document to "+file.getAbsolutePath());
    System.out.println("PageSize = "+pageSize.toString());
    System.out.println("Width X Height = "+getWidth()+" X "+getHeight());
		Document document = new Document(pageSize);
    float saveScale = scale;
		try {
			OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
			PdfWriter writer = PdfWriter.getInstance(document, output);
			document.open();
			PdfContentByte cb = writer.getDirectContent();

			Graphics2D g = cb.createGraphics(pageSize.getWidth(), pageSize.getHeight(), new DefaultFontMapper());
			drawAll(g);
      g.dispose();
      document.close();
    }
    catch (Exception e)
    {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				new JTextArea("Scatterplot export had problem " +  e ));
			// logger.error("Exception " + e);
    }
    scale = saveScale;
  
    document.close();
	}

	private void bitmapSave(String format, File file) {
    logger.info("Writing "+format+" document to "+file.getAbsolutePath());
    float saveScale = scale;
		try {
			OutputStream output = new BufferedOutputStream(new FileOutputStream(file));

			int extraWidth = BORDER_GAP*2;
			int extraHeight = BORDER_GAP*2;
			Rectangle destRect = new Rectangle(0,0, getWidth()*5, getHeight()*5);

      // scale = scale * 5;

			BufferedImage i;
      if (format.equals("png"))
        i = new BufferedImage(destRect.width + extraWidth, destRect.height + extraHeight, BufferedImage.TYPE_INT_ARGB);
      else
        i = new BufferedImage(destRect.width + extraWidth, destRect.height + extraHeight, BufferedImage.TYPE_INT_RGB);
			Graphics g = i.getGraphics();

      // For PNG, we want to allow a transparent background, so don't do the fill
      if (!format.equals("png")) {
        g.setColor(Color.white);
        g.fillRect(0,0,destRect.width+1 + extraWidth,  destRect.height+1+extraHeight);
      }
			g.setColor(Color.black);
			g.translate(extraHeight/2, extraWidth/2);

      // Our X and Y values are currently in the wrong scale -- we need to fix that
      ((Graphics2D)g).scale(5,5);
			drawAll(g);

			ImageIO.write(i,format,output);
			// ignore success, could keep window open on failure if save could indicate success.
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this,
				new JTextArea("Scatterplot export had problem " +  e ));
			// logger.error("Exception " + e);
		}
    scale = saveScale;
  }

	private void svgSave (String format, File file) {
    logger.info("Writing "+format+" document to "+file.getAbsolutePath());
		com.itextpdf.text.Rectangle pageSize = PageSize.LETTER;
		Properties p = new Properties();
		p.setProperty(PSGraphics2D.PAGE_SIZE,"Letter");
		p.setProperty("org.freehep.graphicsio.AbstractVectorGraphicsIO.TEXT_AS_SHAPES",
                  Boolean.toString(false));

		try {
			OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
			SVGGraphics2D g = new SVGGraphics2D(output, getPreferredSize());

      // Width and height aren't the same, so we need to fix this up
      double imageScale = Math.min(pageSize.getWidth()  / ((double) getWidth()+BORDER_GAP*2),
                                   pageSize.getHeight() / ((double) getHeight()+BORDER_GAP*2));
			g.setProperties(p);
			g.startExport();
      g.scale(imageScale, imageScale);
			drawAll(g);
			g.endExport();
			output.close();
    }
    catch (Exception e)
    {
			JOptionPane.showMessageDialog(this,
				new JTextArea("Scatterplot export had problem " +  e ));
			// logger.error("Exception " + e);
			// e.printStackTrace();
    }
	}

}
