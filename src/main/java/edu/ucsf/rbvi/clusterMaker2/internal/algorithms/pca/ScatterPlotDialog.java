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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
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
public class ScatterPlotDialog extends JDialog implements ComponentListener {

	private static DecimalFormat format = new DecimalFormat("0.##");

	private final Matrix loadings;
	private final CyMatrix[] scores;

	private final double[] variances;
	private String[] PCs;
	private Color pointColor = Color.BLUE;

	private JPanel container;
	private static final JPanel panelXAxis = new JPanel();
	private static final JPanel panelYAxis = new JPanel();
	private static final JPanel panelButtons = new JPanel();
	private static final JLabel labelXAxis = new JLabel("X - Axis: ");
	private static final JLabel labelYAxis = new JLabel("Y - Axis: ");
	private static final JLabel labelPointSize = new JLabel("Size of points: ");
	private static final JTextField textFieldPointSize = new JTextField(6);
	private static final JXCollapsiblePane collapsiblePaneOptions = new JXCollapsiblePane();
	private static JLabel labelXVariance;
	private static JLabel labelYVariance;
	private static JComboBox<String> comboXAxis;
	private static JComboBox<String> comboYAxis;
	private static final JButton buttonPlot = new JButton("Plot");
	private static final JButton buttonOptions = new JButton("Advance Options");

	private static int startingX, startingY, currentX, currentY, previousDX=0, previousDY=0;
	private static int currentCenterX=0, currentCenterY=0;
	private static boolean dragging = false;

	private final ScatterPlotDialog thisDialog;

	public ScatterPlotDialog(CyMatrix[] components, Matrix loading, double[] varianceArray) {
		super();
		setTitle("PCA ScatterPlot");

		this.scores = components;
		this.loadings = loading;
		this.variances = varianceArray;

		thisDialog = this;

		if(scores == null){
			return;
		}else if(scores.length < 2){
			return;
		}

		container = new JPanel();
		createUI();
		getContentPane().add(container);

		pack();
		setLocationByPlatform(true);
		setVisible(true);
	}

	private void createUI() {
		container.setLayout(new GridBagLayout());
		container.removeAll();

		ScatterPlotPCA scatterPlot = 
		 				new ScatterPlotPCA(scores, loadings, 0, 1, pointColor, 6);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(5, 5, 5, 5);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;

		container.add(scatterPlot, constraints);

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.anchor = GridBagConstraints.SOUTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		container.add(createControlJPanel(scores, loadings), constraints);
		container.setBorder(BorderFactory.createEtchedBorder());

		container.addComponentListener(this);
	}


	public JXCollapsiblePane createAdvanceOptionPane(){
		JPanel control = new JPanel();

		JButton colorButton = new JButton("Point Color");
		colorButton.addActionListener (new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				pointColor = JColorChooser.showDialog(thisDialog, "Choose color of points", thisDialog.getBackground());
			}
		});

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
		control.add(colorButton, constraints);
		
		// set border for the panel
		control.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Advanced Options"));

		collapsiblePaneOptions.removeAll();
		collapsiblePaneOptions.add("Center", control);
		collapsiblePaneOptions.setCollapsed(!collapsiblePaneOptions.isCollapsed());

		return collapsiblePaneOptions;
	}

	public JPanel createControlJPanel(final CyMatrix[] components, final Matrix loadings){
		JPanel control = new JPanel(new GridBagLayout());

		PCs = new String[components.length];
		for(int i=0;i<PCs.length;i++)
			PCs[i] = "PC " + (i+1);

		comboXAxis = new JComboBox(PCs);
		comboYAxis = new JComboBox(PCs);
		comboYAxis.setSelectedIndex(1);
		textFieldPointSize.setText("6");
		labelXVariance = new JLabel(format.format(variances[0]) + "% variance");
		labelYVariance = new JLabel(format.format(variances[1]) + "% variance");

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
		constraints.gridy = 1;
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
				labelXVariance.setText(format.format(variances[comboXAxis.getSelectedIndex()]) + "% variance");
			}
		});

		comboYAxis.addActionListener (new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				labelYVariance.setText(format.format(variances[comboYAxis.getSelectedIndex()]) + "% variance");
			}
		});

		buttonPlot.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				repaintScatterPlot();
			}

		});

		// set border for the panel
		control.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), ""));

		return control;
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void repaintScatterPlot() {
		int pointSize = 6;
		try{
			pointSize = Integer.parseInt(textFieldPointSize.getText());
		}catch (NumberFormatException er) {
			  JOptionPane.showMessageDialog(null,textFieldPointSize.getText() + " is not a number","Error: Size of point",JOptionPane.ERROR_MESSAGE);
			  return;
		}

		//Execute when button is pressed
		container.remove(0);
		for (ComponentListener cl: container.getComponentListeners())
			container.removeComponentListener(cl);

		ScatterPlotPCA scatterPlot = new ScatterPlotPCA(scores, loadings, 
		                                                comboXAxis.getSelectedIndex(), 
																		                comboYAxis.getSelectedIndex(),
																										pointColor, pointSize);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(5, 5, 5, 5);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		container.add(scatterPlot, constraints, 0);
		container.addComponentListener(thisDialog);
		container.updateUI();
	}
}
