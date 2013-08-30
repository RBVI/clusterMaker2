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
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.AbstractTask;
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
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ViewUtils;

/**
 * The NewFuzzyNetworkView creates a new network view for fuzzy clusters.
 * @author Abhiraj
 *
 */

public class NewFuzzyNetworkView extends AbstractTask implements ClusterViz, ClusterAlgorithm {
	
	private static String appName = "ClusterMaker New Fuzzy Network View";
	private boolean checkForAvailability = false;
	private ClusterManager manager;
	private String clusterAttribute = null;
	private EdgeAttributeHandler edgeConverterList = null;
	public static String CLUSTERNAME = "Create New Network from Fuzzy Clusters";
	public static String ATTRIBUTENAME = "Create New Network from Attributes";
	public static String CLUSTERSHORTNAME = "clusterview";
	public static String ATTRSHORTNAME = "attributeview";
	
	public CyNetworkView networkView = null;
	public CyTableManager tableManager = null;

	@Tunable(description="Network to look for cluster", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public NewNetworkViewContext context = null;

	public NewFuzzyNetworkView(CyNetwork network, ClusterManager manager) {
		this(null, manager, true);
		this.network = network;
		this.networkView = manager.getNetworkView();
		this.tableManager = manager.getTableManager();
	}

	public NewFuzzyNetworkView(NewNetworkViewContext context, ClusterManager manager, boolean available) {
		this.manager = manager;
		checkForAvailability = available;
		if (network == null)
			network = manager.getNetwork();

		if (!checkForAvailability) {
			this.context = context;
			context.setNetwork(network);
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
		if (isAvailable())
			createClusteredNetwork(clusterAttribute, monitor);
	}
	
	public boolean isAvailable() {
		if (!checkForAvailability) {
			clusterAttribute = context.attribute.getSelectedValue();
			return true;
		}
		
		CyTable networkTable = network.getDefaultNetworkTable();
		if (!CyTableUtil.getColumnNames(networkTable).contains(ClusterManager.CLUSTER_TYPE_ATTRIBUTE))
			return false;

		String cluster_type = network.getRow(network).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);
		if (manager.getAlgorithm(cluster_type) != null)
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
	private void createClusteredNetwork(String clusterAttribute, TaskMonitor monitor) {
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
				ModelUtils.createAndSetLocal(network, edge, clusterAttribute, new Integer(1), Integer.class, null);
				edgeList.add(edge);
			}
		}

		VisualStyle style = ViewUtils.getCurrentVisualStyle(manager);

		CyNetwork newNetwork = ModelUtils.createChildNetwork(manager, network, nodeList, edgeList, "--clustered");
		CyNetworkView view = ViewUtils.createView(manager, newNetwork, false);

		ViewUtils.doLayout(manager, view, monitor, "force-directed");

		// Now, if we're supposed to, restore the inter-cluster edges
		if (context != null && context.restoreEdges) {
			for (CyEdge edge: (List<CyEdge>)network.getEdgeList()) {
				if (!edgeMap.containsKey(edge)) {
					((CySubNetwork)view.getModel()).addEdge(edge);
					ModelUtils.createAndSetLocal(view.getModel(), edge, clusterAttribute, 
					                             new Integer(0), Integer.class, null);
				}
			}
			style = styleNewView(style, clusterAttribute);
		}
		

		ViewUtils.setVisualStyle(manager, view, style);
		ViewUtils.registerView(manager, view);
		
		new MembershipEdges(newNetwork,view,manager);

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
				List<Integer> clusterList = 
				network.getRow(node).getList(clusterAttribute, Integer.class);
				for (Integer cluster: clusterList){
					addNodeToMap(clusterMap, cluster, node);
				}
			}
			nodeList.add(node);
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

}	
	

