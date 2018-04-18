/* vim: set ts=2: */
/**
 * Copyright (c) 2008 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package edu.ucsf.rbvi.clusterMaker2.internal.ui;

// System imports
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Observer;
import java.util.Observable;
import java.util.Properties;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

// Cytoscape imports
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

// ClusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeList;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hierarchical.HierarchicalCluster;

// TreeView imports
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.FileSet;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.LoadException;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.PropertyConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeViewApp;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeViewFrame;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ViewFrame;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.TreeViewModel;

/**
 * The ClusterViz class provides the primary interface to the
 * Cytoscape plugin mechanism
 */
public class HeatMapView extends TreeViewApp implements Observer, ObservableTask,
                                                        RowsSetListener,
                                                        ClusterViz, ClusterAlgorithm {
	public static String SHORTNAME = "heatmapview";
	public static String NAME =  "JTree HeatMapView (unclustered)";

	private URL codeBase = null;
	protected ViewFrame viewFrame = null;
	protected TreeSelectionI geneSelection = null;
	protected TreeSelectionI arraySelection = null;
	protected TreeViewModel dataModel = null;
	protected CyNetworkView myView = null;
	protected TaskMonitor monitor = null;
	private	List<CyNode>selectedNodes;
	private	List<CyNode>selectedArrays;
	private boolean disableListeners = false;
	private boolean ignoreSelection = false;
	protected ClusterManager manager = null;
	protected CyNetworkTableManager networkTableManager = null;

	@Tunable(description="Network to view heatmap for", context="nogui")
	public CyNetwork myNetwork = null;

	@ContainsTunables
	public HeatMapContext context = null;

	private static String appName = "clusterMaker HeatMapView";

	public HeatMapView(HeatMapContext context, ClusterManager manager) {
		super();
		// setExitOnWindowsClosed(false);
		selectedNodes = new ArrayList<CyNode>();
		selectedArrays = new ArrayList<CyNode>();
		this.manager = manager;
		this.context = context;
		networkTableManager = manager.getService(CyNetworkTableManager.class);
		if (myNetwork == null) myNetwork = manager.getNetwork();
		context.setNetwork(myNetwork);
	}

	public HeatMapView(PropertyConfig propConfig) {
		super(propConfig);
		selectedNodes = new ArrayList<CyNode>();
		selectedArrays = new ArrayList<CyNode>();
		// setExitOnWindowsClosed(false);
	}

	public void setVisible(boolean visibility) {
		if (viewFrame != null)
			viewFrame.setVisible(visibility);
	}

	public String getAppName() {
		return appName;
	}

	public Object getContext() { return context; }

	// ClusterViz methods
	public String getShortName() { return SHORTNAME; }

	@ProvidesTitle
	public String getName() { return NAME; }

	public ClusterResults getResults() { return null; }

	public void run(TaskMonitor monitor) {
		monitor.setTitle("Creating heat map");
		myView = manager.getNetworkView();
		this.monitor = monitor;
		// Sanity check
		if (isAvailable()) {
			startup();
		} else {
			monitor.showMessage(TaskMonitor.Level.ERROR, "No compatible cluster results available");
		}
	}

	public boolean isAvailable() {
		return true;
	}

	public boolean isAvailable(CyNetwork network) {
		return true;
	}

	public void cancel() {}

	protected void startup() {
		CyProperty cyProperty = manager.getService(CyProperty.class, 
		                                           "(cyPropertyName=cytoscape3.props)");

		List<String> nodeAttributeList = context.attributeList.getNodeAttributeList();
		String edgeAttribute = context.attributeList.getEdgeAttribute();

		if (nodeAttributeList == null && edgeAttribute == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Must select either one edge column or two or more node columns");
			return;
		}

		if (nodeAttributeList != null && nodeAttributeList.size() > 0 && edgeAttribute != null) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Can't have both node and edge columns selected");
			return;
		}

		if (nodeAttributeList != null && nodeAttributeList.size() < 2) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Must have at least two node columns for cluster weighting");
			return;
		}

		String nodeArray[];

		// Handle selected only
		if (!context.selectedOnly) {
			nodeArray = new String[myNetwork.getNodeCount()];
			int index = 0;
			for (CyNode node: myNetwork.getNodeList()) {
				nodeArray[index++] = ModelUtils.getName(myNetwork, node);
			}
		} else {
			if (edgeAttribute != null && edgeAttribute.length() > 0) {
				List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(myNetwork, CyNetwork.SELECTED, true);
				Set<CyNode> nodesSeen = new HashSet<CyNode>();
				for (CyEdge edge: selectedEdges) {
					nodesSeen.add(edge.getSource());
					nodesSeen.add(edge.getTarget());
				}
				int index = 0;
				nodeArray = new String[nodesSeen.size()];
				for (CyNode node: nodesSeen) nodeArray[index++] = ModelUtils.getName(myNetwork, node);
			} else {
				List<CyNode> selectedNodes = CyTableUtil.getNodesInState(myNetwork, CyNetwork.SELECTED, true);
				nodeArray = new String[selectedNodes.size()];
				int index = 0;
				for (CyNode node: selectedNodes) {
					nodeArray[index++] = ModelUtils.getName(myNetwork, node);
				}
			}
		}

		Arrays.sort(nodeArray);
		ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.NODE_ORDER_ATTRIBUTE, 
		                             Arrays.asList(nodeArray), List.class, String.class);

		// Edge attribute?
		if (edgeAttribute != null && edgeAttribute.length() > 0) {
			ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.ARRAY_ORDER_ATTRIBUTE, 
		                               Arrays.asList(nodeArray), List.class, String.class);
			ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.CLUSTER_EDGE_ATTRIBUTE, 
		                               "edge."+edgeAttribute, String.class, null);
		} else {
			int index = 0;
			Collections.sort(nodeAttributeList);

			ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.ARRAY_ORDER_ATTRIBUTE, 
		                               nodeAttributeList, List.class, String.class);
		}

		// Get our data model
		dataModel = new TreeViewModel(monitor, myNetwork, myView, manager);

		// Set up the global config
		setConfigDefaults(new PropertyConfig(cyProperty, globalConfigName(),"ProgramConfig"));

		// Set up our configuration
		PropertyConfig documentConfig = new PropertyConfig(cyProperty, getShortName(),"DocumentConfig");
		dataModel.setDocumentConfig(documentConfig);

		// Create our view frame
		TreeViewFrame frame = new TreeViewFrame(this, appName);

		// Set the data model
		frame.setDataModel(dataModel);
		frame.setLoaded(true);
		frame.addWindowListener(this);
		frame.setVisible(true);
		geneSelection = frame.getGeneSelection();
		geneSelection.addObserver(this);
		arraySelection = frame.getArraySelection();
		arraySelection.addObserver(this);

		manager.registerService(this, RowsSetListener.class, new Properties());

	}

	public void handleEvent(RowsSetEvent e) {
		if (!e.containsColumn(CyNetwork.SELECTED))
			return;

		if (ignoreSelection)
			return;

		CyTable table = e.getSource();
		CyNetwork net = networkTableManager.getNetworkForTable(table);
		Class type = networkTableManager.getTableType(table);

		if (type.equals(CyNode.class)) {
			if (dataModel.isSymmetrical()) return;

			List<CyNode> selectedNodes = CyTableUtil.getNodesInState(net, CyNetwork.SELECTED, true);
			setNodeSelection(selectedNodes, true);
		} else if (type.equals(CyEdge.class) && dataModel.isSymmetrical()) {
			List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(net, CyNetwork.SELECTED, true);
			setEdgeSelection(selectedEdges, true);
		}
		
	}

	@Override
	public void closeAllWindows() {
		super.closeAllWindows();
		manager.unregisterService(this, RowsSetListener.class);
	}

	public void update(Observable o, Object arg) {
		CyNetworkView currentView = manager.getNetworkView();
		CyNetwork currentNetwork = manager.getNetwork();

		// See if we're supposed to disable our listeners
		if ((o == arraySelection) && (arg instanceof Boolean)) {
			// System.out.println("Changing disable listeners to: "+arg.toString());
			disableListeners = ((Boolean)arg).booleanValue();
		}

		if (disableListeners) return;

		if (o == geneSelection) {
			// System.out.println("gene selection");
			selectedNodes.clear();
			int[] selections = geneSelection.getSelectedIndexes();
			HeaderInfo geneInfo = dataModel.getGeneHeaderInfo();
			String [] names = geneInfo.getNames();
			for (int i = 0; i < selections.length; i++) {
				String nodeName = geneInfo.getHeader(selections[i])[0];
				CyNode node = (CyNode)ModelUtils.getNetworkObjectWithName(currentNetwork, nodeName, CyNode.class);
				// Now see if this network has this node
				if (node != null && !currentNetwork.containsNode(node)) {
					// No, try dropping any suffixes from the node name
					String[] tokens = nodeName.split(" ");
					node = (CyNode)ModelUtils.getNetworkObjectWithName(currentNetwork, tokens[0], CyNode.class);
				}
				if (node != null)
					selectedNodes.add(node);
			}
			// System.out.println("Selecting "+selectedNodes.size()+" nodes");
			if (!dataModel.isSymmetrical() || selectedArrays.size() == 0) {
				List<CyNode> nodesToClear = CyTableUtil.getNodesInState(currentNetwork, CyNetwork.SELECTED, true);
				ignoreSelection = true;
				for (CyNode node: nodesToClear) {
					myNetwork.getRow(node).set(CyNetwork.SELECTED, Boolean.FALSE);
					if (currentNetwork.containsNode(node))
						currentNetwork.getRow(node).set(CyNetwork.SELECTED, Boolean.FALSE);
				}

				for (CyNode node: selectedNodes) {
					if (currentNetwork.containsNode(node))
						currentNetwork.getRow(node).set(CyNetwork.SELECTED, Boolean.TRUE);
					myNetwork.getRow(node).set(CyNetwork.SELECTED, Boolean.TRUE);
				}
				manager.getService(CyEventHelper.class).flushPayloadEvents();
				ignoreSelection = false;

				if (currentView != null)
					currentView.updateView();
			}
			return;
		} else if (o == arraySelection) {
			// System.out.println("array selection");
			// We only care about array selection for symmetrical models
			if (!dataModel.isSymmetrical())
				return;

			selectedArrays.clear();
			int[] selections = arraySelection.getSelectedIndexes();
			if (selections.length == dataModel.nExpr())
				return;
			HeaderInfo arrayInfo = dataModel.getArrayHeaderInfo();
			String [] names = arrayInfo.getNames();
			for (int i = 0; i < selections.length; i++) {
				String nodeName = arrayInfo.getHeader(selections[i])[0];
				CyNode node = (CyNode)ModelUtils.getNetworkObjectWithName(currentNetwork, nodeName, CyNode.class);
				if (node != null && !currentNetwork.containsNode(node)) {
					// No, try dropping any suffixes from the node name
					String[] tokens = nodeName.split(" ");
					node = (CyNode)ModelUtils.getNetworkObjectWithName(currentNetwork, tokens[0], CyNode.class);
				}
				if (node != null)
					selectedArrays.add(node);
			}
		}

		HashMap<CyEdge,CyEdge>edgesToSelect = new HashMap<CyEdge,CyEdge>();
		ignoreSelection = true;
		List<CyEdge> edgesToClear = CyTableUtil.getEdgesInState(currentNetwork, CyNetwork.SELECTED, true);
		for (CyEdge edge: edgesToClear) {
			myNetwork.getRow(edge).set(CyNetwork.SELECTED, Boolean.FALSE);
			if (currentNetwork.containsEdge(edge))
				currentNetwork.getRow(edge).set(CyNetwork.SELECTED, Boolean.FALSE);
		}

		for (CyNode node1: selectedNodes) {
			for (CyNode node2: selectedArrays) {
				List<CyEdge> edges = currentNetwork.getConnectingEdgeList(node1, node2, CyEdge.Type.ANY);
				if (edges == null) {
					continue;
				}
				for (CyEdge edge: edges) {
					myNetwork.getRow(edge).set(CyNetwork.SELECTED, Boolean.TRUE);
					if (currentNetwork.containsEdge(edge))
						currentNetwork.getRow(edge).set(CyNetwork.SELECTED, Boolean.TRUE);
				}
			}
		}
		manager.getService(CyEventHelper.class).flushPayloadEvents();
		ignoreSelection = false;

		if (currentView != null)
			currentView.updateView();
		selectedNodes.clear();
		selectedArrays.clear();
	}

	private void setEdgeSelection(List<CyEdge> edgeArray, boolean select) {
		HeaderInfo geneInfo = dataModel.getGeneHeaderInfo();
		HeaderInfo arrayInfo = dataModel.getArrayHeaderInfo();

		// Avoid loops -- delete ourselves as observers
		geneSelection.deleteObserver(this);
		arraySelection.deleteObserver(this);

		// Clear everything that's currently selected
		geneSelection.setSelectedNode(null);
		geneSelection.deselectAllIndexes();
		arraySelection.setSelectedNode(null);
		arraySelection.deselectAllIndexes();

		// Do the actual selection
		for (CyEdge cyEdge: edgeArray) {
			if (!myNetwork.containsEdge(cyEdge))
				continue;
			CyNode source = (CyNode)cyEdge.getSource();
			CyNode target = (CyNode)cyEdge.getTarget();
			int geneIndex = geneInfo.getHeaderIndex(ModelUtils.getName(myNetwork, source));
			int arrayIndex = arrayInfo.getHeaderIndex(ModelUtils.getName(myNetwork, target));
			geneSelection.setIndex(geneIndex, select);
			arraySelection.setIndex(arrayIndex, select);
		}

		// Notify all of the observers
		geneSelection.notifyObservers();
		arraySelection.notifyObservers();

		// OK, now we can listen again
		geneSelection.addObserver(this);
		arraySelection.addObserver(this);
	}

	private void setNodeSelection(List<CyNode> nodeArray, boolean select) {
		HeaderInfo geneInfo = dataModel.getGeneHeaderInfo();
		geneSelection.deleteObserver(this);
		geneSelection.setSelectedNode(null);
		geneSelection.deselectAllIndexes();
		for (CyNode cyNode: nodeArray) {
			if (!myNetwork.containsNode(cyNode))
				continue;
			int geneIndex = geneInfo.getHeaderIndex(ModelUtils.getName(myNetwork, cyNode));
			// System.out.println("setting "+cyNode.getIdentifier()+"("+geneIndex+") to "+select);
			geneSelection.setIndex(geneIndex, select);
		}
		geneSelection.deleteObserver(this);
		geneSelection.notifyObservers();
		geneSelection.addObserver(this);
	}

	@Override
  public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class, String.class);
	}

	@Override
  public <R> R getResults(Class<? extends R> requestedType) {
		if (requestedType.equals(String.class))
			return (R)"Heatmap is shown";
		else if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				return "{}";
			};
			return (R)res;
		}
		return (R)"";
	}
}
