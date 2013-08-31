/* vim: set ts=2:

  File: NetworkColorDialog.java

  Copyright (c) 2008, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - University of California San Francisco
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  Dout of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataModel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ViewFrame;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.dendroview.ColorExtractor;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ViewUtils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * The NetworkColorDialog is a dialog that provides a mechanism to map colors from
 * the HeatMap to the network.
 */
public class NetworkColorDialog extends JDialog 
                                implements ActionListener, ListSelectionListener, ChangeListener,
	                                         WindowListener {

	private ColorExtractor colorExtractor = null;
	private ViewFrame viewFrame = null;
	private String attribute = null;
	private List<String>attributeList = null;
	private double maxValue;
	private double minValue;
	protected String currentAttribute = null;
	protected VisualStyle[] styles = null;

	// Dialog components
	private JLabel titleLabel; // Our title
	private JPanel mainPanel; // The main content pane
	protected JList attributeSelector; // Attribute list
	protected JSlider animationSlider;
	private JButton animateButton;
	private JButton nodeChartButton = null;
	private ClusterManager clusterManager = null;
	private TaskManager taskManager = null;

	private boolean animating = false;
	private boolean listening = true;

	/**
	 * Creates a new NetworkColorDialog object.
	 */
	public NetworkColorDialog(JFrame parent, ColorExtractor ce, List<String>attributes, 
	                          ViewFrame viewFrame, ClusterManager clusterManager,
	                          double minValue, double maxValue, 
	                          boolean symmetric) {
		super(parent, "Map Colors to Network", false);
		colorExtractor = ce;
		attributeList = attributes;
		this.maxValue = maxValue;
		this.minValue = minValue;
		this.viewFrame = viewFrame;
		this.clusterManager = clusterManager;

		taskManager = clusterManager.getService(TaskManager.class); // Get a task manager

		if (symmetric) {
			CyNetwork network = clusterManager.getNetwork();
			if (!ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_EDGE_ATTRIBUTE))
				return;

			MapTask task = new MapTask(attribute.substring(5), "-heatMap", true);
			taskManager.execute(new TaskIterator(task));
			return;
		}
		// How many attributes are there?
		if (attributeList.size() == 1) {
			// Only one, so just do it (no dialog)
			MapTask task = new MapTask(attributeList.get(0), "-heatMap", false);
			taskManager.execute(new TaskIterator(task));
			return;
		} else {
			initializeOnce(); // Initialize the components we only do once
			pack();
			setVisible(true);
		}

		addWindowListener(this);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param e DOCUMENT ME!
	 */
	public void actionPerformed(ActionEvent e) {
		// Are we the source of the event?
		String command = e.getActionCommand();

		if (command.equals("done")) {
			animating = false;
			this.setVisible(false);
		} else if (command.equals("up")) {
			shiftList(-1);
		} else if (command.equals("down")) {
			shiftList(1);
		} else if (command.equals("vizmap")) {
			String attribute = (String)attributeSelector.getSelectedValue();

			MapTask task = new MapTask(attribute, "-heatMap", false);
			taskManager.execute(new TaskIterator(task));
		} else if (command.equals("animate")) {
			if (animating) {
				animating = false;
				animateButton.setText("Animate Vizmap");
				return;
			}

			// Get the selected attributes
			Object[] attributeArray = attributeSelector.getSelectedValues();
			if (attributeArray.length < 2) {
				// Really nothing to animate if we only have one map
				MapTask task = new MapTask((String)attributeArray[0], "-heatMap", false);
				taskManager.execute(new TaskIterator(task));
				return;
			}

			if (currentAttribute == null) {
				// Build the necessary vizmap entries
				styles = new VisualStyle[attributeArray.length];
				for (int i = 0; i < attributeArray.length; i++) {
					styles[i] = createNewStyle((String)attributeArray[i], "-"+(String)attributeArray[i], false, false);
				}
			}

			// Change the animate button
			animateButton.setText("Stop animation");
			animating = true;
			// Set up the animation task
			Animate a = new Animate(styles, attributeArray);
			a.start();
		} else if (command.equals("heatstrip")) {
			// Get the selected attributes
			Object[] attributeArray = attributeSelector.getSelectedValues();

			// We need to use enhancedgraphics for this.
			// 1) create a heatstrip command
			// 2) Write it into a column for every node
			// 3) Add a passthrough mapper for custom graphics

/*
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("nodelist","all");

			try {
				// Clear any nodecharts
				CyCommandManager.execute(NODECHARTS, "clear", args);
			} catch (Exception e1) {
				// Ignore the clear...
			}


			// Construct our command
			List<String> attributeList = new ArrayList<String>();
			for (Object attr: attributeArray) {
				attributeList.add(attr.toString());
			}
			// attributeList = attributeList.substring(1);
			args.put("attributelist",attributeList);
			// Get our colors
			String colorSpec = getColorSpec();
			// System.out.println("ColorSpec = "+colorSpec);
			args.put("colorlist",colorSpec);
			args.put("position","south");
			args.put("showlabels","false");
			args.put("size","30x60");

			try {
				// Execute
				CyCommandManager.execute(NODECHARTS, "heatstrip", args);
			} catch (Exception e2) {
				CyLogger.getLogger(this.getClass()).error("Error from nodecharts: "+e2.getMessage());
			}
*/
			clusterManager.getNetworkView().updateView();
		}
	}

	// WindowListener methods
	public void	windowActivated(WindowEvent e) {}
 	public void	windowClosed(WindowEvent e) {}
 	public void	windowClosing(WindowEvent e) {
		animating = false;
	}
 	public void	windowDeactivated(WindowEvent e) {}
 	public void	windowDeiconified(WindowEvent e) {}
 	public void	windowIconified(WindowEvent e) {}
 	public void	windowOpened(WindowEvent e) {}

	private VisualStyle createNewStyle(String attribute, String suffix, boolean update, boolean edge) { 
		boolean newStyle = false;

		// Get our current vizmap
		VisualMappingManager vmm = clusterManager.getService(VisualMappingManager.class);
		VisualStyle style = ViewUtils.getCurrentVisualStyle(clusterManager);

		// Get our colors
		Color missingColor = colorExtractor.getMissing();

		// Get the type of "attribute"
		CyNetwork network = clusterManager.getNetwork();
		VisualProperty<Paint> property;
		CyColumn column;
		if (edge) {
			column = network.getDefaultEdgeTable().getColumn(attribute);
			property = BasicVisualLexicon.EDGE_PAINT;
		} else {
			column = network.getDefaultNodeTable().getColumn(attribute);
			property = BasicVisualLexicon.NODE_FILL_COLOR;
		}
		if (column == null) return null;

		Class type = column.getType();

		if (!style.getTitle().endsWith(suffix)) {
			style = ViewUtils.copyStyle(clusterManager, style, suffix);
			newStyle = true;
		}

		// Get a function factory
		VisualMappingFunctionFactory vmff = clusterManager.getService(VisualMappingFunctionFactory.class, 
		                                                              "(mapping.type=continuous)");
		ContinuousMapping colorMapping =
			(ContinuousMapping) vmff.createVisualMappingFunction(attribute, type, property);

		double minStep = minValue/5.0;
		for (int i = 0; i < 5; i++) {
			Color color = colorExtractor.getColor(minValue-(minStep*i));
			// System.out.println("Value: "+(minValue-(minStep*i))+" Color: "+color.toString());
			colorMapping.addPoint (minValue-(minStep*i),
				new BoundaryRangeValues<Paint>(color, color, color));
		}

		{
			Color color = colorExtractor.getColor(0.0f);
			colorMapping.addPoint (0.0f,
				new BoundaryRangeValues<Paint>(color, color, color));
		}

		double maxStep = maxValue/5.0;
		for (int i = 1; i <= 5; i++) {
			Color color = colorExtractor.getColor(maxStep*i);
			// System.out.println("Value: "+(maxStep*i)+" Color: "+color.toString());
			colorMapping.addPoint (maxStep*i,
				new BoundaryRangeValues<Paint>(color, color, color));
		}

		style.addVisualMappingFunction(colorMapping);
		if (update) {
			ViewUtils.setVisualStyle(clusterManager, clusterManager.getNetworkView(), style);
		}

		return style;
	}

	private void initializeOnce() {
		boolean enableAnimation = false;

		setDefaultCloseOperation(HIDE_ON_CLOSE);

		// Create our main panel
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		// See if we have more than one attribute
		if (attributeList.size() > 1) {
			// We do, so enable the animation UI
			enableAnimation = true;
		}

		// Create our JList
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.LINE_AXIS));
		this.attributeSelector = new JList(attributeList.toArray());
		attributeSelector.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		attributeSelector.addListSelectionListener(this);
		JScrollPane listScroller = new JScrollPane(attributeSelector);
		listScroller.setPreferredSize(new Dimension(200,100));
		listPanel.add(listScroller);
		Border listBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder titleBorder = BorderFactory.createTitledBorder(listBorder, "Attribute List");
		titleBorder.setTitlePosition(TitledBorder.LEFT);
		titleBorder.setTitlePosition(TitledBorder.TOP);
		listPanel.setBorder(titleBorder);

		// Now add the sorting arrows
		JPanel sortPanel = new JPanel();
		sortPanel.setLayout(new BoxLayout(sortPanel, BoxLayout.PAGE_AXIS));
		{
			JButton upButton = new JButton("^");
			upButton.setActionCommand("up");
			upButton.addActionListener(this);
			sortPanel.add(upButton);
		}

		{
			JButton downButton = new JButton("v");
			downButton.setActionCommand("down");
			downButton.addActionListener(this);
			sortPanel.add(downButton);
		}

		listPanel.add(sortPanel);
		mainPanel.add(listPanel);

		// Create a panel for our animation speed slider
		JPanel sliderBox = new JPanel();
		Border sliderBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder tSliderBorder = BorderFactory.createTitledBorder(sliderBorder, "Animation Speed");
		tSliderBorder.setTitlePosition(TitledBorder.LEFT);
		tSliderBorder.setTitlePosition(TitledBorder.TOP);
		sliderBox.setBorder(tSliderBorder);
		this.animationSlider = new JSlider(0,100);
		animationSlider.addChangeListener(this);

		// Create the labels
		Hashtable<Integer,JLabel> labels = new Hashtable<Integer,JLabel>();
		labels.put(new Integer(1), new JLabel("Slower"));
		labels.put(new Integer(100), new JLabel("Faster"));
		animationSlider.setLabelTable(labels);
		animationSlider.setPaintLabels(true);
		animationSlider.setEnabled(false);
		sliderBox.add(animationSlider);

		// Create a panel for our button box
		JPanel buttonBox = new JPanel();

		JButton doneButton = new JButton("Done");
		doneButton.setActionCommand("done");
		doneButton.addActionListener(this);

		JButton vizmapButton = new JButton("Create Vizmap");
		vizmapButton.setActionCommand("vizmap");
		vizmapButton.addActionListener(this);

		animateButton = new JButton("Animate Vizmap");
		animateButton.setActionCommand("animate");
		animateButton.addActionListener(this);
		animateButton.setEnabled(false);

		if (checkNodeCharts()) {
			nodeChartButton = new JButton("Create HeatStrips");
			nodeChartButton.setActionCommand("heatstrip");
			nodeChartButton.addActionListener(this);
			nodeChartButton.setEnabled(false);
			buttonBox.add(nodeChartButton);
		}

		buttonBox.add(animateButton);
		buttonBox.add(vizmapButton);
		buttonBox.add(doneButton);
		buttonBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

		// TODO: Fix up our heights and widths
		Dimension buttonDim = buttonBox.getPreferredSize();
		buttonBox.setMinimumSize(buttonDim);
		buttonBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,buttonDim.height));
		animationSlider.setPreferredSize(buttonDim);
		sliderBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,buttonDim.height));
	
		mainPanel.add(sliderBox);
		mainPanel.add(buttonBox);

		// TODO: Set up the minimum size of our dialog
		setContentPane(mainPanel);
	}

	public void stateChanged(ChangeEvent e) {
	}

	public void valueChanged(ListSelectionEvent e) {
		// Get the selected items
		int[] selIndices = attributeSelector.getSelectedIndices();

		if (!listening)
			return;

		// Clear our memory
		currentAttribute = null;

		// If there are more then one, enable the animate button
		if (selIndices.length > 1) {
			animateButton.setEnabled(true);
			animationSlider.setEnabled(true);
			if (nodeChartButton != null)
				nodeChartButton.setEnabled(true);
		} else {
			animateButton.setEnabled(false);
			animationSlider.setEnabled(false);
			if (nodeChartButton != null)
				nodeChartButton.setEnabled(false);
		}
	}

	public void shiftList(int amount) {
		int[] selIndices = attributeSelector.getSelectedIndices();
		// Remove each of he values from the list
		String[] removedValues = new String[selIndices.length];
		for (int i = 0; i < selIndices.length; i++) {
			removedValues[i] = attributeList.get(selIndices[i]);
		}

		// OK, now remove them
		for (int i = 0; i < selIndices.length; i++) {
			attributeList.remove(removedValues[i]);
			selIndices[i] += amount;
			if (selIndices[i] < 0) 
				selIndices[i] = 0;
			if (selIndices[i] > attributeList.size())
				selIndices[i] = attributeList.size();
		}

		// Re-insert them one up
		for (int i = 0; i < selIndices.length; i++) {
			attributeList.add(selIndices[i], removedValues[i]);
		}

		// OK, now update the list
		attributeSelector.setListData(attributeList.toArray());
		attributeSelector.setSelectedIndices(selIndices);

		// And reset our animator
		currentAttribute = null;
	}

	private static String NODECHARTS = "nodecharts";
	private boolean checkNodeCharts() {
/*
		try {
			CyCommandManager.getCommand(NODECHARTS, "clear");
		} catch (RuntimeException e) {
			System.out.println("Got runtime error: "+e);
			return false;
		}
		return true;
*/
		return false;
	}

	private String getColorSpec() {
		Color downColor = colorExtractor.getColor(minValue);
		Color upColor = colorExtractor.getColor(maxValue);
		Color zeroColor = colorExtractor.getColor(0.0f);
		return "up:#"+colorToHex(upColor)+",zero:#"+colorToHex(zeroColor)+",down:#"+colorToHex(downColor);
	}

	private String colorToHex(Color c) {
		return Integer.toHexString(c.getRGB()).substring(2);
	}

	private class Animate extends Thread {
		VisualStyle[] styles;
		Object[] attributes;
		// JSlider animationSlider;
		String current;

		public Animate(VisualStyle[] styles, Object[] attributes) {
			this.styles = styles;
			this.attributes = attributes;
		}
		public void run() {
			int firstIndex = 0;

			listening = false;

			TreeSelectionI arraySelection = viewFrame.getArraySelection();
			DataModel dataModel = viewFrame.getDataModel();
			HeaderInfo arrayInfo = dataModel.getArrayHeaderInfo();

			// Get the currently selected array indices
			int[] selectedAttributes = arraySelection.getSelectedIndexes();
			arraySelection.deselectAllIndexes();

			// Disable the Cytoscape selection
			arraySelection.notifyObservers(Boolean.TRUE);

			// Wrap everything in a try in case
			// our dialog goes away before we do
			try {
				attributeSelector.clearSelection();
				// See if we have a "current" attribute
				if (currentAttribute != null) {
					for (int i = 0; i < attributes.length; i++) {
						if (currentAttribute.equals((String)attributes[i])) {
							firstIndex =  i;
							break;
						}
					}
				}
				while (animating) {
					// Cycle through the vizmaps we created
					for (int i = firstIndex; i < styles.length && animating; i++) {
						ViewUtils.setVisualStyle(clusterManager, clusterManager.getNetworkView(), styles[i]);

						attributeSelector.setSelectedValue(attributes[i], true);
						currentAttribute = (String)attributes[i];
						// Now show the selection in the tree view
						int arrayIndex = arrayInfo.getHeaderIndex(currentAttribute);
						viewFrame.seekArray(arrayIndex);
						
						int wait = animationSlider.getValue();
						this.sleep((100-wait)*20);
					}
					firstIndex = 0;
				}
				// Reset our selected attributes
				int [] selectedIndices = new int[attributes.length];
				int selIndex = 0;
				for (int index = 0; index < attributes.length; index++) {
					selectedIndices[selIndex++] = attributeList.indexOf(attributes[index]);
				}
				attributeSelector.setSelectedIndices(selectedIndices);
				// Enable the Cytoscape selection
			} catch (Exception e) {
				// This is almost certainly a class cast because the dialog
				// went away before we completed.  Just ignore it
			}
			listening = true;

			// Now, restore the user's original attribute selection
			arraySelection.deselectAllIndexes();
			for (int index: selectedAttributes) {
				arraySelection.setIndex(index, true);
			}

			// And re-enable notification
			arraySelection.notifyObservers(Boolean.FALSE);
		}
	}

	private class MapTask implements Task {
		TaskMonitor monitor;
		String attribute;
		String suffix;
		boolean edge;

		public MapTask(String attribute, String suffix, boolean edge) {
			this.attribute = attribute;
			this.suffix = suffix;
			this.edge = edge;
		}

		public void run(TaskMonitor monitor) {
			createNewStyle(attribute, suffix, true, edge);
		}

		public void cancel() {}

	}

}
