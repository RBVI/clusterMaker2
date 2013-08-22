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

import java.awt.Color;
import java.awt.Paint;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Cytoscape imports
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

// ClusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeWeightConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

/**
 * The ClusterViz class provides the primary interface to the
 * Cytoscape plugin mechanism
 */
public class NewNetworkView implements ClusterViz, ClusterAlgorithm {

	private static String appName = "ClusterMaker New Network View";
	private boolean checkForAvailability = false;
	private ClusterManager manager;
	private String clusterAttribute = null;
	private EdgeAttributeHandler edgeConverterList = null;

	@Tunable(description="Network to look for cluster", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	NewNetworkViewContext context = null;

	public NewNetworkView(NewNetworkViewContext context, ClusterManager manager, boolean available) {
		this.manager = manager;
		checkForAvailability = available;
		if (network == null)
			network = manager.getNetwork();

		if (!checkForAvailability) {
			this.context = context;
		} else {
			this.context = null;
		}
		edgeConverterList = new EdgeAttributeHandler(network, false);
	}

	public void setVisible(boolean visibility) {
	}

	public String getAppName() {
		return appName;
	}

	// ClusterViz methods
	public String getShortName() { return "newnetworkview"; }

	@ProvidesTitle
	public String getName() { 
		if (checkForAvailability) {
			return "Create New Network from Clusters";
		} else {
			return "Create New Network from Attribute"; 
		}
	}

	public ClusterResults getResults() { return null; }

	public void run(TaskMonitor monitor) {
		createClusteredNetwork(clusterAttribute);
	}

	public boolean isAvailable() {
		if (!checkForAvailability)
			return true;
		
		CyTable networkTable = network.getDefaultNetworkTable();
		if (!CyTableUtil.getColumnNames(networkTable).contains(ClusterManager.CLUSTER_TYPE_ATTRIBUTE))
			return false;

		String cluster_type = network.getRow(network).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);
		if (manager.getAlgorithm(cluster_type) == null || 
		    !manager.getAlgorithm(cluster_type).getTypeList().contains(ClusterTaskFactory.ClusterType.NETWORK))
			return false;

		if (CyTableUtil.getColumnNames(networkTable).contains(ClusterManager.CLUSTER_ATTRIBUTE)) {
			clusterAttribute = network.getRow(network).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
			if (clusterAttribute != null) return true;
		}
		return false;
	}

	public ClusterViz getVisualizer() {
		return this;
	}

	public NewNetworkViewContext getContext() { return context; }

	public void cancel() { }

	@SuppressWarnings("unchecked")
	private void createClusteredNetwork(String clusterAttribute) {
		// Get the clustering parameters
		Map<String, String> params = getParams();

		List<CyNode> nodeList = new ArrayList<CyNode>();
		Map<Integer, List<CyNode>> clusterMap = getClusterMap(clusterAttribute, nodeList);

		// Special handling for edge weight thresholds
		EdgeWeightConverter converter = 
				edgeConverterList.getConverter(getParam(params, "converter"));
		String dataAttribute = getParam(params, "dataAttribute");
		double cutOff = 0.0;
		if (getParam(params, "edgeCutOff") != null)
			cutOff = Double.parseDouble(getParam(params, "edgeCutOff"));

		HashMap<CyEdge,CyEdge> edgeMap = new HashMap<CyEdge,CyEdge>();
		List<CyEdge> edgeList = new ArrayList<CyEdge>();
		for (Integer cluster: clusterMap.keySet()) {
			// Get the list of nodes
			List<CyNode> clusterNodes = clusterMap.get(cluster); 
			// Get the list of edges
			List<CyEdge> connectingEdges = ModelUtils.getConnectingEdges(network, clusterNodes);
			for (CyEdge edge: connectingEdges) { 
				if (converter != null && dataAttribute != null) {
					if (edgeWeightCheck(edge, dataAttribute, converter, cutOff)) 
						continue;
				}
				edgeMap.put(edge,edge);
				// Add the cluster attribute to the edge so we can style it later
				ModelUtils.createAndSet(network, edge, clusterAttribute, new Integer(1), Integer.class, null);
				edgeList.add(edge);
			}
		}

		CyNetwork newNetwork = ((CySubNetwork)network).getRootNetwork().addSubNetwork(nodeList,edgeList);

/*
		// Create the network view
		CyNetworkView view = Cytoscape.createNetworkView(net);

		// If available, do a force-directed layout
		CyLayoutAlgorithm alg = CyLayouts.getLayout("force-directed");

		if (alg != null)
			view.applyLayout(alg);

		// Get the current visual mapper
		VisualStyle vm = Cytoscape.getVisualMappingManager().getVisualStyle();

		// Now, if we're supposed to, restore the inter-cluster edges
		if (restoreEdges) {
			// Create new visual style
			vm = createNewStyle(clusterAttribute, "-cluster");

			// Add edge width and opacity descrete mappers
			for (CyEdge edge: (List<CyEdge>)currentNetwork.edgesList()) {
				if (!edgeMap.containsKey(edge)) {
					net.addEdge(edge);
					edgeAttributes.setAttribute(edge.getIdentifier(), clusterAttribute, new Integer(0));
				}
			}
		}

		view.applyVizmapper(vm);

		Cytoscape.setCurrentNetwork(net.getIdentifier());
		Cytoscape.setCurrentNetworkView(view.getIdentifier());
*/
		return;
	}

	private Map<Integer, List<CyNode>> getClusterMap(String clusterAttribute, List<CyNode> nodeList) {
		// Two possibilities.  We may have a fuzzy cluster or a discrete cluster.  Figure
		// that out now.
		boolean isFuzzy = 
			network.getDefaultNodeTable().getColumn(clusterAttribute).getType().equals(List.class);

		// Create the cluster Map
		Map<Integer, List<CyNode>> clusterMap = new HashMap<Integer, List<CyNode>>();
		for (CyNode node: (List<CyNode>)network.getNodeList()) {
			// For each node -- see if it's in a cluster.  If so, add it to our map
			if (ModelUtils.hasAttribute(network, node, clusterAttribute)) {
				if (isFuzzy) {
					List<Integer> clusterList = 
						network.getRow(node).getList(clusterAttribute, Integer.class);
					for (Integer cluster: clusterList)
						addNodeToMap(clusterMap, cluster, node);
				} else {
					Integer cluster = network.getRow(node).get(clusterAttribute, Integer.class);
					addNodeToMap(clusterMap, cluster, node);
				}
			}
			nodeList.add(node);
		}
		return clusterMap;
	}

	private void addNodeToMap(Map<Integer, List<CyNode>> map, Integer cluster, CyNode node) {
		if (!map.containsKey(cluster))
			map.put(cluster, new ArrayList<CyNode>());
		map.get(cluster).add(node);
	}

	private boolean edgeWeightCheck(CyEdge edge, String dataAttribute,
	                                EdgeWeightConverter converter, double cutoff) {
		if (!ModelUtils.hasAttribute(network, edge, dataAttribute))
			return false;
		Class type = network.getRow(edge).getTable().getColumn(dataAttribute).getType();
		if (!type.isAssignableFrom(Number.class))
			return false;

		double val = network.getRow(edge).get(dataAttribute, (Class<? extends Number>)type).doubleValue();

		if (converter.convert(val, 0.0, Double.MAX_VALUE) > cutoff)
			return false;

		return true;
	}

/*
	@SuppressWarnings("deprecation")
	private VisualStyle createNewStyle(String attribute, String suffix) { 
		boolean newStyle = false;

		// Get our current vizmap
		VisualMappingManager manager = Cytoscape.getVisualMappingManager();
		CalculatorCatalog calculatorCatalog = manager.getCalculatorCatalog();

		// Get the current style
		VisualStyle style = Cytoscape.getCurrentNetworkView().getVisualStyle();
		// Create a new vizmap
		Set<String> styles = calculatorCatalog.getVisualStyleNames();
		if (styles.contains(style.getName()+suffix))
			style = calculatorCatalog.getVisualStyle(style.getName()+suffix);
		else {
			style = new VisualStyle(style, style.getName()+suffix);
			newStyle = true;
		}

		// Set up our line width descrete mapper
		DiscreteMapping lineWidth = new DiscreteMapping(new Double(.5), attribute, ObjectMapping.EDGE_MAPPING);
		lineWidth.putMapValue(new Integer(0), new Double(1));
		lineWidth.putMapValue(new Integer(1), new Double(5));
   	Calculator widthCalculator = new BasicCalculator("Edge Width Calculator",
		                                                 lineWidth, VisualPropertyType.EDGE_LINE_WIDTH);

		DiscreteMapping lineOpacity = new DiscreteMapping(new Integer(50), attribute, ObjectMapping.EDGE_MAPPING);
		lineOpacity.putMapValue(new Integer(0), new Integer(50));
		lineOpacity.putMapValue(new Integer(1), new Integer(255));
   	Calculator opacityCalculator = new BasicCalculator("Edge Opacity Calculator",
		                                                 lineOpacity, VisualPropertyType.EDGE_OPACITY);

		
		EdgeAppearanceCalculator edgeAppCalc = style.getEdgeAppearanceCalculator();
   	edgeAppCalc.setCalculator(widthCalculator);
   	edgeAppCalc.setCalculator(opacityCalculator);
		style.setEdgeAppearanceCalculator(edgeAppCalc);
		if (newStyle) {
			calculatorCatalog.addVisualStyle(style);
			manager.setVisualStyle(style);
		} 
		return style;
		return null;
	}
*/

	
	Map<String, String> getParams() {
		Map<String, String> map = new HashMap<String, String>();
		if (!ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE))
			return map;

		List<String> params = 
			network.getRow(network).getList(ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, String.class);

		for (String param: params) {
			String[] attr = param.split("=");
			if (attr.length == 2)
				map.put(attr[1], attr[2]);
		}
		return map;
	}

	String getParam(Map<String, String> params, String key) {
		if (!params.containsKey(key))
			return null;
		return params.get(key);
	}

}
