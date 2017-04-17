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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import org.jdesktop.swingx.JXCollapsiblePane;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;

/**
 *
 * @author root
 */
@SuppressWarnings("serial")
public class ScatterPlotDialog extends JDialog {

	private static DecimalFormat format = new DecimalFormat("0.##");

	private final ClusterManager manager;
	private final Matrix loadings;
	private final CyMatrix[] scores;

	private final double[] variances;
	private String[] PCs;
	private Color pointColor = Color.BLUE;

	private JPanel container;
	private JPanel panelXAxis;
	private JPanel panelYAxis;
	private JPanel panelButtons;
	private JLabel labelXAxis;
	private JLabel labelYAxis;
	private JLabel labelPointSize;
	private JTextField textFieldPointSize;
	private JXCollapsiblePane collapsiblePaneOptions;
	private JPanel legendPanel;
	private JLabel labelXVariance;
	private JLabel labelYVariance;
	private JComboBox<String> comboXAxis;
	private JComboBox<String> comboYAxis;
	private JButton buttonColor;
	private JButton buttonOptions;
	private JButton buttonPlot;
	private JButton buttonLegend;
	private Map<String, Color> loadingsColorMap;

	// For inner classes
	private final ScatterPlotDialog thisDialog;

	private boolean useLoadings;

	// Entry point for tSNE and related
	public ScatterPlotDialog(ClusterManager manager, String title, TaskMonitor monitor, CyMatrix coordinates) {
		super();
		setTitle(title);
		monitor.setTitle(title);
		useLoadings = false;
		this.manager = manager;
		this.scores = new CyMatrix[1];
		this.scores[0] = coordinates;

		this.variances = null;
		this.loadings = CyMatrixFactory.makeSmallMatrix(scores[0].getNetwork(), 1, 2);
		loadings.setColumnLabel(0, "X Axis");
		loadings.setColumnLabel(1, "Y Axis");
		thisDialog = this;

		if (coordinates.nColumns() != 2) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Coordinate scatterplot must have 2 columns!");
			return;
		}

		container = new JPanel();
		createUI();
		getContentPane().add(container);

		pack();
		setLocationByPlatform(true);
		setVisible(true);
	}

	// Entry point for PCoA and related
	public ScatterPlotDialog(ClusterManager manager, String title, TaskMonitor monitor, 
	                         CyMatrix[] components, double[] varianceArray) {
		super();
		setTitle(title);
		monitor.setTitle(title);
		useLoadings = false;
		this.scores = components;
		this.manager = manager;
		this.loadings = CyMatrixFactory.makeSmallMatrix(components[0].getNetwork(), 1, components.length);
		for (int col=0; col < components.length; col++) {
			loadings.setColumnLabel(col, "PC "+(col+1));
		}

		this.variances = varianceArray;

		thisDialog = this;

		if(scores == null || scores.length < 2 || scores.length != varianceArray.length) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Coordinate scatterplot must at least 2 columns!");
			return;
		}

		container = new JPanel();
		createUI();
		getContentPane().add(container);

		pack();
		setLocationByPlatform(true);
		setVisible(true);
	}

	// Entry point for PCA
	public ScatterPlotDialog(ClusterManager manager, String title, TaskMonitor monitor, 
	                         CyMatrix[] components, Matrix loading, double[] varianceArray) {
		super();
		setTitle(title);
		monitor.setTitle(title);

		this.scores = components;
		this.manager = manager;
		loadingsColorMap = new HashMap<String, Color>();
		this.loadings = loading;
		useLoadings = true;
		initializeColors();

		this.variances = varianceArray;

		thisDialog = this;

		// Sanity check
		if(scores == null || scores.length < 2 || scores.length != varianceArray.length) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Scatterplot must at least 2 columns!");
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
		panelXAxis = new JPanel();
		panelYAxis = new JPanel();
		panelButtons = new JPanel();
		labelXAxis = new JLabel("X - Axis: ");
		labelYAxis = new JLabel("Y - Axis: ");
		labelPointSize = new JLabel("Size of points: ");
		textFieldPointSize = new JTextField(6);
		collapsiblePaneOptions = new JXCollapsiblePane();
		// collapsiblePaneLegend = new JXCollapsiblePane();
		buttonPlot = new JButton("Plot");
		buttonOptions = new JButton("Advanced");
		if (loadings != null)
			buttonLegend = new JButton("Arrow Legend");

		buttonColor = new JButton("Get Colors");

		container.setLayout(new GridBagLayout());
		container.removeAll();

		ScatterPlot scatterPlot = 
		 				new ScatterPlot(manager, scores, loadings, 0, 1, pointColor, 6, loadingsColorMap, useLoadings);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(5, 5, 5, 5);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;

		container.add(scatterPlot, constraints);

		if (useLoadings) {
			constraints.gridx = 1;
			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.fill = GridBagConstraints.NONE;
			constraints.anchor = GridBagConstraints.NORTHEAST;
			legendPanel = createLegendPane();
			container.add(legendPanel, constraints);
		}

		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		constraints.weightx = 1.0;
		constraints.weighty = 0;
		constraints.anchor = GridBagConstraints.SOUTHWEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		container.add(createControlJPanel(scores, loadings), constraints);

		container.setBorder(BorderFactory.createEtchedBorder());

	}


	public JPanel createLegendPane(){
		JPanel legend = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 0, 0, 0);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;	
		constraints.fill = GridBagConstraints.NONE;	

		// set border for the panel
		legend.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Legend"));

		for (int row=0; row < loadings.nRows(); row++) {
			String label = loadings.getRowLabel(row);
			Color color = Color.RED;
			if (loadingsColorMap.containsKey(label))
				color = loadingsColorMap.get(label);
			JButton lButton = new JButton(label);
			lButton.setForeground(color);
			lButton.setActionCommand(label);
			lButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String label = e.getActionCommand();
					Color clr = JColorChooser.showDialog(thisDialog, "Choose color for "+label+" arrow", thisDialog.getBackground());
					if (clr != null) {
						loadingsColorMap.put(label, clr);
						repaintScatterPlot();
						((JButton)e.getSource()).setForeground(clr);
					}
				}
			});
			legend.add(lButton, constraints);
			constraints.gridy += 1;
		}

		return legend;
	}

	public void toggleLegendPane() {
		if (container.isAncestorOf(legendPanel)) {
			container.remove(1);
			container.doLayout();
		} else {
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.insets = new Insets(5, 5, 5, 5);

			constraints.weightx = 0;
			constraints.weighty = 0;
			constraints.gridx = 1;
			constraints.fill = GridBagConstraints.NONE;
			constraints.anchor = GridBagConstraints.NORTHEAST;
			legendPanel = createLegendPane();
			container.add(legendPanel, constraints, 1);
			container.doLayout();
			container.repaint();
		}
	}

	public JXCollapsiblePane createAdvanceOptionPane(){
		JPanel control = new JPanel(new GridBagLayout());

		JButton colorButton = new JButton("Point Color");
		colorButton.addActionListener (new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				Color clr = JColorChooser.showDialog(thisDialog, "Choose color of points", thisDialog.getBackground());
				if (clr != null)
					pointColor = clr;
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

		if (variances != null) {
			PCs = new String[components.length];
			for(int i=0;i<PCs.length;i++)
				PCs[i] = "PC " + (i+1);

			comboXAxis = new JComboBox<String>(PCs);
			comboYAxis = new JComboBox<String>(PCs);
			comboYAxis.setSelectedIndex(1);
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
		}

		if(buttonOptions.getActionListeners().length == 0){
			buttonOptions.addActionListener(collapsiblePaneOptions.getActionMap().get("toggle"));
		}

		if (useLoadings) {
			if(buttonLegend.getActionListeners().length == 0){
				buttonLegend.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						toggleLegendPane();
					}
				});
			}
		}

		textFieldPointSize.setText("6");

		panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
		panelButtons.add(buttonOptions);
		panelButtons.add(buttonPlot);
		panelButtons.add(buttonColor);
		if (useLoadings)
			panelButtons.add(buttonLegend);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets(10, 10, 10, 10);
		constraints.fill = GridBagConstraints.HORIZONTAL;

		int gridy = 0;
		if (variances != null) {
			// add components to the panel
			constraints.gridx = 0;
			constraints.gridy = gridy++;
			control.add(labelXAxis, constraints);

			constraints.gridx = 1;
			control.add(panelXAxis, constraints);

			constraints.gridx = 0;
			constraints.gridy = gridy++;
			control.add(labelYAxis, constraints);

			constraints.gridx = 1;
			control.add(panelYAxis, constraints);
		}

		constraints.gridx = 0;
		constraints.gridy = gridy++;
		constraints.gridwidth = 2;
		constraints.anchor = GridBagConstraints.CENTER;
		control.add(createAdvanceOptionPane(), constraints);

		constraints.gridx = 0;
		constraints.gridy = gridy++;
		constraints.gridwidth = 2;
		constraints.anchor = GridBagConstraints.CENTER;
		control.add(panelButtons, constraints);

		buttonPlot.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				repaintScatterPlot();
			}

		});

		buttonColor.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				pointColor = null;
				repaintScatterPlot();
			}

		});

		// set border for the panel
		control.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), ""));

		return control;
	}

	public void repaintScatterPlot() {
		int pointSize = 6;
		try{
			pointSize = Integer.parseInt(textFieldPointSize.getText());
		}catch (NumberFormatException er) {
			  JOptionPane.showMessageDialog(null,
												textFieldPointSize.getText() + 
												" is not a number","Error: Size of point",JOptionPane.ERROR_MESSAGE);
			  return;
		}

		//Execute when button is pressed
		container.remove(0);
		for (ComponentListener cl: container.getComponentListeners())
			container.removeComponentListener(cl);

		int xAxis = 0;
		int yAxis = 1;
		if (variances != null) {
			xAxis = comboXAxis.getSelectedIndex();
			yAxis = comboYAxis.getSelectedIndex();
		}

		ScatterPlot scatterPlot = new ScatterPlot(manager, scores, loadings, 
		                                          xAxis, 
																		          yAxis,
																							pointColor, pointSize, loadingsColorMap,
																							useLoadings);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(5, 5, 5, 5);

		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		container.add(scatterPlot, constraints, 0);
		container.updateUI();
	}

	private void initializeColors() {
		float hue = 0f;
		float saturation = .8f;
		float brightness = .8f;
		for (int row=0; row < loadings.nRows(); row++) {
			String label = loadings.getRowLabel(row);
			hue += .65f;
			int clr = Color.HSBtoRGB(hue, saturation, brightness);
			loadingsColorMap.put(label, new Color(clr));
		}
	}
}
