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
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

// ClusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeWeightConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.FuzzyNodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ViewUtils;

/**
 * The ClusterViz class provides the primary interface to the
 * Cytoscape plugin mechanism
 */
public class NewNetworkView extends AbstractTask implements ClusterViz, ClusterAlgorithm, ObservableTask {

	private static String appName = "ClusterMaker New Network View";
	private boolean checkForAvailability = false;
	private boolean restoreEdges = false;
	private boolean addSingletons = true;
	private ClusterManager manager;
	private String clusterAttribute = null;
	private EdgeAttributeHandler edgeConverterList = null;
	public static String CLUSTERNAME = "Create New Network from Clusters";
	public static String ATTRIBUTENAME = "Create New Network from Attributes";
	public static String CLUSTERSHORTNAME = "clusterview";
	public static String ATTRSHORTNAME = "attributeview";

	@Tunable(description="Network to look for cluster", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public NewNetworkViewContext context = null;

	private CyNetworkView networkView = null;

	public NewNetworkView(CyNetwork network, ClusterManager manager) {
		this(manager, true, true);
		this.network = network;
	}

	public NewNetworkView(CyNetwork network, ClusterManager manager, 
			              boolean available, boolean restoreEdges, boolean addSingletons) {
		this(manager, true, addSingletons);
		this.network = network;
		this.restoreEdges = restoreEdges;
    this.context = null;
	}

	public NewNetworkView(ClusterManager manager, boolean available, boolean addSingletons) {
		this.manager = manager;
		checkForAvailability = available;
		this.addSingletons = addSingletons;
		if (network == null)
			network = manager.getNetwork();

		if (!checkForAvailability) {
			this.context = new NewNetworkViewContext(true);
			context.setNetwork(network);
		} else {
			this.context = new NewNetworkViewContext(false);
		}
		edgeConverterList = new EdgeAttributeHandler(network, false);
	}

	public void setVisible(boolean visibility) {
	}

	public String getAppName() {
		return appName;
	}

	// ClusterViz methods
	public String getShortName() { 
		if (checkForAvailability) {
			return CLUSTERSHORTNAME;
		} else {
			return ATTRSHORTNAME; 
		}
	}

	@ProvidesTitle
	public String getName() { 
		if (checkForAvailability) {
			return CLUSTERNAME;
		} else {
			return ATTRIBUTENAME; 
		}
	}

	public ClusterResults getResults() { return null; }

	public void run(TaskMonitor monitor) {
		monitor.setTitle("Creating a new network view with cluster results");
		if (isAvailable())
			createClusteredNetwork(clusterAttribute, monitor);
	}

	public boolean isAvailable() {
		if (!checkForAvailability) {
			clusterAttribute = context.attribute.getSelectedValue();
			return true;
		}

		boolean available = NewNetworkView.isReady(network, manager);
		clusterAttribute =
			network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
		return available;
	}

	public static boolean isReady(CyNetwork network, ClusterManager manager) {
		if (network == null) 
			return false;

		CyTable networkTable = network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);
		if (!CyTableUtil.getColumnNames(networkTable).contains(ClusterManager.CLUSTER_TYPE_ATTRIBUTE))
			return false;

		String cluster_type = network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);
		if (manager.getAlgorithm(cluster_type) != null)
		if (manager.getAlgorithm(cluster_type) == null || 
		    !manager.getAlgorithm(cluster_type).getTypeList().contains(ClusterTaskFactory.ClusterType.NETWORK))
			return false;

		if (CyTableUtil.getColumnNames(networkTable).contains(ClusterManager.CLUSTER_ATTRIBUTE)) {
			String clusterAttribute = network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
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
	@Override
  public <R> R getResults(Class<? extends R> requestedType) {
		if (requestedType.equals(String.class))
			return (R)("Created new network view: "+networkView.toString());
		else if (requestedType.equals(CyNetwork.class)) {
			return (R)networkView;
		} else if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (networkView == null) return "{}";
				return "{\"view\":"+networkView.getSUID()+"}";
			};
			return (R)res;
		}
		return (R)networkView.toString();
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class, CyNetworkView.class, String.class);
	}

	@SuppressWarnings("unchecked")
	private void createClusteredNetwork(String clusterAttribute, TaskMonitor monitor) {

    boolean selectedOnly = false;
    if (context != null)
      selectedOnly = context.selectedOnly;
		boolean isFuzzy = isFuzzy(clusterAttribute);

		// Get the clustering parameters
		Map<String, String> params = getParams();

		List<CyNode> nodeList = new ArrayList<CyNode>();
		Map<Integer, List<CyNode>> clusterMap = getClusterMap(clusterAttribute, selectedOnly, nodeList);

		// Special handling for edge weight thresholds
		EdgeWeightConverter converter = 
				edgeConverterList.getConverter(getParam(params, "converter"));
		String dataAttribute = getParam(params, "dataAttribute");
		double cutOff = 0.0;
		if (getParam(params, "edgeCutOff") != null)
			cutOff = Double.parseDouble(getParam(params, "edgeCutOff"));

		HashMap<CyEdge,CyEdge> edgeMap = new HashMap<CyEdge,CyEdge>();
		List<CyEdge> edgeList = new ArrayList<CyEdge>();
		// System.out.println("Getting the edges");
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
				ModelUtils.createAndSetLocal(network, edge, clusterAttribute, new Integer(1), Integer.class, null);
				edgeList.add(edge);
			}
		}

		// System.out.println("Getting the style");
		VisualStyle style = ViewUtils.getCurrentVisualStyle(manager);

		// System.out.println("Creating the network");
		CyNetwork newNetwork = ModelUtils.createChildNetwork(manager, network, nodeList, edgeList, "--clustered");
		// Now, copy the cluster attribute from the original network to this one
		ModelUtils.copyLocalColumn(network, newNetwork, CyNode.class, clusterAttribute);

		// Copy the clustering information over
		ModelUtils.copyLocalColumn(network, newNetwork, CyNetwork.class, "__clusterType");
		ModelUtils.copyLocalColumn(network, newNetwork, CyNetwork.class, "__clusterAttribute");
		ModelUtils.copyLocalColumn(network, newNetwork, CyNetwork.class, "__clusterParams");

    String fuzzySeed = null;
		// Finally, if we're fuzzy, see if we had an initial seed and copy that over
		if (isFuzzy && ModelUtils.hasAttribute(network, network, "__fuzzifierSeed")) {
			ModelUtils.copyLocalColumn(network, newNetwork, CyNetwork.class, "__fuzzifierSeed");
			fuzzySeed =
				network.getRow(network, CyNetwork.LOCAL_ATTRS).get("__fuzzifierSeed", String.class);
			ModelUtils.copyLocalColumn(network, newNetwork, CyNode.class, fuzzySeed);
		}

		// System.out.println("Getting the view");
		networkView = ViewUtils.createView(manager, newNetwork, false);

    // If we fuzzy, we probably don't want to relayout the network -- we want
    // to map the fuzzy nodes onto the existing network instead
    if (!isFuzzy)
      ViewUtils.doLayout(manager, networkView, monitor, "force-directed");
    else
      ViewUtils.copyLayout(manager, manager.getNetworkView(network), networkView);

		// Now, if we're supposed to, restore the inter-cluster edges
		if (restoreEdges || (context != null && context.restoreEdges)) {
			for (CyEdge edge: network.getEdgeList()) {
				if (!edgeMap.containsKey(edge)) {
					((CySubNetwork)networkView.getModel()).addEdge(edge);
					ModelUtils.createAndSetLocal(networkView.getModel(), edge, clusterAttribute,
							                     new Integer(0), Integer.class, null);
				}
			}
			style = styleNewView(style, clusterAttribute);
		}
		// System.out.println("Setting the style");
		ViewUtils.setVisualStyle(manager, networkView, style);

		if(isFuzzy){
			ModelUtils.copyLocalColumn(network, newNetwork, CyNetwork.class, clusterAttribute + "_Table.SUID");
			long fuzzyClusterTableSUID = network.getRow(network).get(clusterAttribute + "_Table.SUID", Long.class);
			newNetwork.getRow(newNetwork).set(clusterAttribute + "_Table.SUID", fuzzyClusterTableSUID);
			CyTable fuzzyClusterTable = manager.getTableManager().getTable(fuzzyClusterTableSUID);
      List<FuzzyNodeCluster> fuzzyClusters = getFuzzyClusters(network, selectedOnly, fuzzyClusterTable);
			createMembershipEdges(newNetwork, selectedOnly, networkView,manager, fuzzyClusters, fuzzySeed);
		}

		ViewUtils.registerView(manager, networkView);

		return;
	}

  private List<FuzzyNodeCluster> getFuzzyClusters(CyNetwork network, boolean selectedOnly, CyTable fuzzyClusterTable) {
    List<FuzzyNodeCluster> fClusters = new ArrayList<>();

		Set<String> columns = CyTableUtil.getColumnNames(fuzzyClusterTable);
    int clusters[] = new int[columns.size()];
    int cluster_index = 0;
    for (String colName: columns) {
      if (colName.startsWith("Cluster_")) {
        int cluster_number = Integer.parseInt(colName.substring(8));
        clusters[cluster_index++] = cluster_number;
        FuzzyNodeCluster fnc = new FuzzyNodeCluster();
        fnc.setClusterNumber(cluster_number);
        fClusters.add(fnc);
      }
    }

		List<CyNode> nodeList = ModelUtils.getNodeList(network,selectedOnly);
    for (CyNode node: nodeList) {
      CyRow row = fuzzyClusterTable.getRow(node.getSUID());
      if (row == null) continue;

      Map<String, Object> rowData = row.getAllValues();
      cluster_index = 0;
      for (String colName: columns) {
        if (!colName.startsWith("Cluster_"))
          continue;
        FuzzyNodeCluster fnc = fClusters.get(cluster_index++);
        Double value = (Double)rowData.get(colName);
        if (value != null)
          fnc.add(node, value);
      }
    }

    return fClusters;
  }

	private Map<Integer, List<CyNode>> getClusterMap(String clusterAttribute, boolean selectedOnly, List<CyNode> nodeList) {
		// Two possibilities.  We may have a fuzzy cluster or a discrete cluster.  Figure
		// that out now.
		boolean isFuzzy = 
			network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(clusterAttribute).getType().equals(List.class);

		// Create the cluster Map
		Map<Integer, List<CyNode>> clusterMap = new HashMap<Integer, List<CyNode>>();
		for (CyNode node: ModelUtils.getNodeList(network, selectedOnly)) {
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
				nodeList.add(node);
			} else if (addSingletons) {
				nodeList.add(node);
			}
		}
		return clusterMap;
	}

	private VisualStyle	styleNewView(VisualStyle style, String clusterAttribute) {
		VisualStyle newStyle = ViewUtils.copyStyle(manager, style, "--clustered");
		VisualMappingFunctionFactory vmff = manager.getService(VisualMappingFunctionFactory.class);
		DiscreteMapping edgeWidth = 
			(DiscreteMapping) vmff.createVisualMappingFunction(clusterAttribute, Integer.class, 
					                                           BasicVisualLexicon.EDGE_WIDTH);
		edgeWidth.putMapValue(new Integer(0), new Double(1));
		edgeWidth.putMapValue(new Integer(1), new Double(5));

		DiscreteMapping edgeTrans = 
			(DiscreteMapping) vmff.createVisualMappingFunction(clusterAttribute, Integer.class, 
					                                           BasicVisualLexicon.EDGE_TRANSPARENCY);
		edgeTrans.putMapValue(new Integer(0), new Integer(50));
		edgeTrans.putMapValue(new Integer(1), new Integer(255));

		newStyle.addVisualMappingFunction(edgeWidth);
		newStyle.addVisualMappingFunction(edgeTrans);
		return newStyle;
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

	Map<String, String> getParams() {
		Map<String, String> map = new HashMap<String, String>();
		if (!ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE))
			return map;

		List<String> params = 
			network.getRow(network).getList(ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, String.class);

		for (String param: params) {
			String[] attr = param.split("=");
			if (attr.length == 2)
				map.put(attr[0], attr[1]);
		}
		return map;
	}

	String getParam(Map<String, String> params, String key) {
		if (!params.containsKey(key))
			return null;
		return params.get(key);
	}

	/**
	 * Method to add the membership edges
	 */
	private void createMembershipEdges(CyNetwork network, boolean selectedOnly, CyNetworkView networkView, 
	                                   ClusterManager manager, List<FuzzyNodeCluster> fuzzyClusters, String fuzzySeed) {

		CyTable localTable = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		localTable.createColumn("isFClusterNode", Boolean.class, false, Boolean.FALSE);


    Map<Integer,List<CyEdge>> allEdges = new HashMap<>();
    Map<CyNode, double[]> centroids = new HashMap<>();

		int clusterNumber = 0;
		for(FuzzyNodeCluster cluster :fuzzyClusters){
			CyNode centroid = network.addNode();


			network.getRow(centroid).set(CyNetwork.NAME, "FCluster" + cluster.getClusterNumber() );
			network.getRow(centroid).set("isFClusterNode", Boolean.TRUE);
      // For convenience, add the base cluster number to our fuzzy cluster
			network.getRow(centroid).set(fuzzySeed, cluster.getClusterNumber());
 
			// System.out.println("Centroid SUID: " + centroid.getSUID());
			//View<CyNode> nodeView = networkView.getNodeView(centroid);
			double x = 0.0;
			double y = 0.0;
			double weightSum = 0;

			List<CyEdge> membershipEdges = new ArrayList<CyEdge>();

			for (CyNode node : cluster) {
				View<CyNode> nodeView = networkView.getNodeView(node);
				//System.out.println("NodeView SUID: " + nodeView.getSUID());

				Double membership = cluster.getMembership(node);
				x += nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION)*membership;
				y += nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)*membership;
        weightSum += membership;
				//System.out.println("Read x = "+ x +", y = "+ y);
				CyEdge membershipEdge = network.addEdge(centroid, node, false);
				ModelUtils.createAndSetLocal(network, membershipEdge, "MembershipEdge", 
	                                   Boolean.TRUE, Boolean.class, null);
				ModelUtils.createAndSetLocal(network, membershipEdge, "Membership_%", 100*membership, Double.class, null);
				membershipEdges.add(membershipEdge);
			}
			// networkView.updateView();

			if(weightSum!=0){
				x = x/weightSum;
				y = y/weightSum;
        double[] p = new double[]{x,y};
        centroids.put(centroid, p);
        // View<CyNode> centroidView = networkView.getNodeView(centroid);
        // System.out.println("CentroidView SUID: " + centroidView.getSUID());
        // System.out.println("x = "+ x +", y = "+ y);
			}
			// View<CyNode> centroidView = networkView.getNodeView(centroid);
			// System.out.println("CentroidView SUID: " + centroidView.getSUID());
			// System.out.println("x = "+ x +", y = "+ y);
			// centroidView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
			// centroidView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);

      allEdges.put(clusterNumber, membershipEdges);
			clusterNumber++;
			// membershipEdgeStyle(networkView, clusterNumber, membershipEdges, fuzzyClusterTable);
		}
    networkView.updateView();

    // First, create all of our centroid node views
    for (CyNode centroid: centroids.keySet()) {
			View<CyNode> centroidView = networkView.getNodeView(centroid);
			centroidView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, centroids.get(centroid)[0]);
			centroidView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, centroids.get(centroid)[1]);
    }

    System.out.println("Creating membership edges");
    for (int cluster_index = 0; cluster_index < fuzzyClusters.size(); cluster_index++) {
      membershipEdgeStyle(networkView, allEdges.get(cluster_index));
    }
		networkView.updateView();
	}

	/**
	 * method to add additional styling to newly created membership edges
	 *
	 * @param networkView the new network view
	 * @param cNum Cluster Number
	 * @param edgeList List of added membership edges for the cluster
	 * @param fuzzyClusterTable table having information about the fuzzy clusters
	 */
	private void membershipEdgeStyle(CyNetworkView networkView, List<CyEdge> edgeList) {
    CyNetwork network = networkView.getModel();
		for (CyEdge edge : edgeList){
      Double membership = network.getRow(edge).get("Membership_%", Double.class);
      if (membership == null)
        continue;

      int trans = membership.intValue() * 255/100;

			View<CyEdge> edgeView = networkView.getEdgeView(edge);

			CyNode node = edge.getTarget();
			//edgeView.setVisualProperty(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.DASH_DOT);
			edgeView.setLockedValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.DASH_DOT);
      edgeView.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, trans);

		}

	}

	private boolean isFuzzy(String clusterAttribute) {
		CyColumn column = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(clusterAttribute);
		if (column == null) return false;
		if (column.getType().equals(List.class))
			return true;
		return false;
	}
}
