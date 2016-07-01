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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
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
	private static final int BORDER_GAP = 30;
	private static final int GRAPH_HATCH_WIDTH = 10;
	private int graph_point_width = 6;

	private final Matrix loadings;
	private final CyMatrix[] scores;
	private final int xIndex;
	private final int yIndex;

	private List<Point> graphPoints;
	// private static CyMatrix[] allComponents;
	private static double[] variances;
	private static String[] PCs;
	private static final Color[] colors =
		{Color.black, Color.blue, Color.cyan, Color.darkGray,
		 Color.gray, Color.green,
		 Color.yellow, Color.lightGray,
		 Color.magenta, Color.orange, Color.pink,
		 Color.red, Color.white };

	private static final String[] colorNames =
		{ "Black", "Blue", "Cyan", "Dark Gray",
		  "Gray", "Green",
			"Yellow", "Light Gray",
			"Magneta", "Orange", "Pink",
			"Red", "White" };

	private static final JPanel container = new JPanel();
	private static final JPanel panelXAxis = new JPanel();
	private static final JPanel panelYAxis = new JPanel();
	private static final JPanel panelButtons = new JPanel();
	private static final JLabel labelXAxis = new JLabel("X - Axis: ");
	private static final JLabel labelYAxis = new JLabel("Y - Axis: ");
	private static final JLabel labelColor = new JLabel("Color of points: ");
	private static final JLabel labelPointSize = new JLabel("Size of points: ");
	private static final JTextField textFieldPointSize = new JTextField(6);
	private static final JXCollapsiblePane collapsiblePaneOptions = new JXCollapsiblePane();
	private static JLabel labelXVariance;
	private static JLabel labelYVariance;
	private static JComboBox<String> comboXAxis;
	private static JComboBox<String> comboYAxis;
	private static JComboBox<String> comboColors;
	private static final JButton buttonPlot = new JButton("Plot");
	private static final JButton buttonOptions = new JButton("Advance Options");

	private static int startingX, startingY, currentX, currentY, previousDX=0, previousDY=0;
	private static int currentCenterX=0, currentCenterY=0;
	private static boolean dragging = false;

	public ScatterPlotPCA(CyMatrix[] scores, Matrix loadings, int x, int y) {
		this.scores = scores;
		this.loadings = loadings;
		this.xIndex = x;
		this.yIndex = y;

		// this.scoresX = scores[x];
		// this.scoresY = scores[y];
		// this.lableX = loadings.getColumnLabel(x);
		// this.lableY = loadings.getColumnLabel(y);

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

	  double xScale = ((double) getWidth() - 2 * BORDER_GAP) / (MAX_SCORE - MIN_SCORE);
	  double yScale = ((double) getHeight() - 2 * BORDER_GAP) / (MAX_SCORE - MIN_SCORE);

	  // create x and y axes
	  g2.drawLine(BORDER_GAP, getHeight()/2, getWidth() - BORDER_GAP, getHeight()/2);
	  g2.drawLine(getWidth()/2, BORDER_GAP, getWidth()/2, getHeight() - BORDER_GAP);

	  // create hatch marks for y axis.
	  for (int i = 0; i <= MAX_SCORE - MIN_SCORE; i++) {
		 int x0 = getWidth()/2;
		 int x1 = GRAPH_HATCH_WIDTH + getWidth()/2;
		 int y0 = (int) (BORDER_GAP + i * yScale);
		 int y1 = y0;
		 g2.drawLine(x0, y0, x1, y1);

		 String number = "" + ( MAX_SCORE - i);
		 if ((MAX_SCORE-i) < 0)
		 	g2.drawString(number, x1 - 3*GRAPH_HATCH_WIDTH, y1 + GRAPH_HATCH_WIDTH/2);
		 else
		 	g2.drawString(number, x1 - 2*GRAPH_HATCH_WIDTH, y1 + GRAPH_HATCH_WIDTH/2);
	  }
	  g2.setFont(new Font("default", Font.BOLD, g2.getFont().getSize()));
	  g2.drawString(labelY, getWidth()/2 - (labelY.length()/2)*5, getHeight() - BORDER_GAP/2);
	  g2.setFont(new Font("default", Font.PLAIN, g2.getFont().getSize()));

	  // and for x axis
	  for (int i = 0; i <= MAX_SCORE - MIN_SCORE; i++) {
		 int x0 = (int) (BORDER_GAP + i * xScale);
		 int x1 = x0;
		 int y0 = getHeight()/2;
		 int y1 = y0 + GRAPH_HATCH_WIDTH;
		 g2.drawLine(x0, y0, x1, y1);

		 String number = "" + -1 * ( MAX_SCORE - i);
		 if(!number.equals("0"))
			g2.drawString(number, x1, y1 - 2*GRAPH_HATCH_WIDTH);
	  }
	  g2.setFont(new Font("default", Font.BOLD, g2.getFont().getSize()));
	  g2.drawString(labelX, getWidth() - BORDER_GAP - (labelX.length()/2)*5, getHeight()/2 + BORDER_GAP);
	  g2.setFont(new Font("default", Font.PLAIN, g2.getFont().getSize()));

	  int newX = getWidth()/2;
	  int newY = getHeight()/2;

	  graphPoints = new ArrayList<Point>();
	  for(int i=0; i<scores[xIndex].nRows();i++){
		  for(int j=0;j<scores[xIndex].nColumns();j++){
			  int x1 = (int) (scores[xIndex].getValue(i,j) * xScale + newX);
			  int y1 = (int) (-1 * (scores[yIndex].getValue(i,j) * yScale - newY));
			  graphPoints.add(new Point(x1, y1));
		  }
	  }
	  g2.setColor(colors[comboColors.getSelectedIndex()]);
	  graph_point_width = Integer.parseInt(textFieldPointSize.getText());
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
			drawArrow(g2, x1, y1, x2, y2, Color.RED);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = new Dimension(PREF_W, PREF_H);
		//size.width = Math.round(size.width * scale);
		//size.height = Math.round(size.height * scale);
		return size;
	}

	public static JXCollapsiblePane createAdvanceOptionPane(){
		JPanel control = new JPanel();

		comboColors = new JComboBox(colorNames);
		comboColors.setSelectedIndex(1);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(10, 10, 10, 10);

		// add components to the panel
		constraints.gridx = 0;
		constraints.gridy = 0;	
		control.add(labelPointSize, constraints);

		constraints.gridx = 1;
		control.add(textFieldPointSize, constraints);
		
		constraints.gridx = 0;
		constraints.gridy = 1;	
		control.add(labelColor, constraints);
		
		constraints.gridx = 1;
		control.add(comboColors, constraints);

		// set border for the panel
		control.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Advanced Options"));

		collapsiblePaneOptions.removeAll();
		collapsiblePaneOptions.add("Center", control);
		collapsiblePaneOptions.setCollapsed(!collapsiblePaneOptions.isCollapsed());

		return collapsiblePaneOptions;
	}

	public static JPanel createControlJPanel(final CyMatrix[] components, final Matrix loadings){
		JPanel control = new JPanel(new GridBagLayout());

		PCs = new String[components.length];
		for(int i=0;i<PCs.length;i++)
			PCs[i] = "PC " + (i+1);

		comboXAxis = new JComboBox(PCs);
		comboYAxis = new JComboBox(PCs);
		comboYAxis.setSelectedIndex(1);
		textFieldPointSize.setText("6");
		labelXVariance = new JLabel(String.valueOf(variances[0]) + "% variance");
		labelYVariance = new JLabel(String.valueOf(variances[1]) + "% variance");

		panelXAxis.setLayout(new BoxLayout(panelXAxis, BoxLayout.X_AXIS));
		panelXAxis.removeAll();
		panelXAxis.add(comboXAxis);
		panelXAxis.add(Box.createRigidArea(new Dimension(5,0)));
		panelXAxis.add(labelXVariance);

		panelYAxis.setLayout(new BoxLayout(panelYAxis, BoxLayout.X_AXIS));
		panelYAxis.removeAll();
		panelYAxis.add(comboYAxis);
		panelYAxis.add(Box.createRigidArea(new Dimension(5,0)));
		panelYAxis.add(labelYVariance);

		if(buttonOptions.getActionListeners().length == 0){
			buttonOptions.addActionListener(collapsiblePaneOptions.getActionMap().get("toggle"));
		}
		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
		panelButtons.add(buttonOptions);
		panelButtons.add(buttonPlot);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(10, 10, 10, 10);

		// add components to the panel
		constraints.gridx = 0;
		constraints.gridy = 0;
		control.add(labelXAxis, constraints);

		constraints.gridx = 1;
		control.add(panelXAxis, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		control.add(labelYAxis, constraints);

		constraints.gridx = 1;
		control.add(panelYAxis, constraints);

		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 2;
		constraints.anchor = GridBagConstraints.CENTER;
		control.add(createAdvanceOptionPane(), constraints);

		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.gridwidth = 2;
		constraints.anchor = GridBagConstraints.CENTER;
		control.add(panelButtons, constraints);

		comboXAxis.addActionListener (new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				labelXVariance.setText(variances[comboXAxis.getSelectedIndex()] + "% variance");
			}
		});

		comboYAxis.addActionListener (new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				labelYVariance.setText(variances[comboYAxis.getSelectedIndex()] + "% variance");
			}
		});

		buttonPlot.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				try{
					Integer.parseInt(textFieldPointSize.getText());
				}catch (NumberFormatException er) {
					  JOptionPane.showMessageDialog(null,textFieldPointSize.getText() + " is not a number","Error: Size of point",JOptionPane.ERROR_MESSAGE);
					  return;
				}

				//Execute when button is pressed
				container.remove(0);
				ScatterPlotPCA scatterPlot = new ScatterPlotPCA(components, loadings, 
				                                                comboXAxis.getSelectedIndex(), 
																				                comboYAxis.getSelectedIndex());
				container.add(scatterPlot, 0);
				container.updateUI();
			}

		});

		// set border for the panel
		control.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), ""));

		return control;
	}

	public void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2, Color color) {
		/*
		int dx = x2 - x1, dy = y2 - y1;
		double D = Math.sqrt(dx*dx + dy*dy);
		double d = 10, h = 5.0;
		double xm = D - d, xn = xm, ym = h, yn = -h, x;
		double sin = dy/D, cos = dx/D;

		x = (xm)*cos - (ym)*sin + x1;
		ym = (xm)*sin + (ym)*cos + y1;
		xm = x;
		x = (xn)*cos - (yn)*sin + x1;
		yn = (xn)*sin + (yn)*cos + y1;
		xn = x;

		int[] xpoints = {x2, (int) xm, (int) xn};
		int[] ypoints = {y2, (int) ym, (int) yn};

	  g2.setColor(color);
		g2.setStroke(new BasicStroke(2.0f));
		g2.drawLine(x1, y1, x2, y2);
		g2.setStroke(new BasicStroke(0.0f));
		g2.fillPolygon(xpoints, ypoints, 3);
		*/

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

	public static void createAndShowGui(CyMatrix[] components, Matrix loading, double[] varianceArray) {

		if(components == null){
			return;
		}else if(components.length < 2){
			return;
		}
		variances = varianceArray;

		ScatterPlotPCA scatterPlot = 
		 				new ScatterPlotPCA(components, loading, 0, 1);

		JFrame frame = new JFrame("Scatter Plot");

		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		container.removeAll();
		previousDX = previousDY = 0;
		container.add(scatterPlot);
		container.add(createControlJPanel(components, loading));

		frame.getContentPane().add(container);
		frame.pack();
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}
}
