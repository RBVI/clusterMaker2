package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.ChengChurch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.FuzzyNodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractAttributeClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.BiclusterView;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class ChengChurch extends AbstractAttributeClusterer {

	public static String SHORTNAME = "cheng&church";
	public static String NAME = "Cheng & Church's  bi-cluster";
	public final static String GROUP_ATTRIBUTE = SHORTNAME+"Groups.SUID";
	
	CyTableManager tableManager = null;
	private CyTableFactory tableFactory = null;
	
	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public ChengChurchContext context = null;
	
	public ChengChurch(ChengChurchContext context, ClusterManager clusterManager) {
		super(clusterManager);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
		
		tableManager = clusterManager.getTableManager();
		tableFactory = clusterManager.getTableFactory();
	}

	public String getShortName() {return SHORTNAME;}

	@ProvidesTitle
	public String getName() {return NAME;}
	
	public ClusterViz getVisualizer() {
		return null;
	}
	
	public void run(TaskMonitor monitor){
		
		this.monitor = monitor;
		monitor.setTitle("Performing "+getName());
		List<String> nodeAttributeList = context.attributeList.getNodeAttributeList();
		String edgeAttribute = context.attributeList.getEdgeAttribute();
		
		clusterAttributeName = "CnC_Bicluster";
		
		if(network.getRow(network, CyNetwork.LOCAL_ATTRS).getTable().getColumn(ClusterManager.CLUSTER_ATTRIBUTE)==null){
			network.getRow(network, CyNetwork.LOCAL_ATTRS).getTable().createColumn(ClusterManager.CLUSTER_ATTRIBUTE, String.class, false);
		}
		network.getRow(network, CyNetwork.LOCAL_ATTRS).set(ClusterManager.CLUSTER_ATTRIBUTE, clusterAttributeName);
		
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

		if (context.selectedOnly && CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true).size() < 3) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Must have at least three nodes to cluster");
			return;
		}

		createGroups = context.createGroups;

		// To make debugging easier, sort the attribute list
		Collections.sort(nodeAttributeList);

		// Get our attributes we're going to use for the cluster
		String[] attributeArray;
		if (nodeAttributeList != null && nodeAttributeList.size() > 1) {
			attributeArray = new String[nodeAttributeList.size()];
			int i = 0;
			for (String attr: nodeAttributeList) { attributeArray[i++] = "node."+attr; }
		} else {
			attributeArray = new String[1];
			attributeArray[0] = "edge."+edgeAttribute;
		}

		monitor.setStatusMessage("Initializing");

		resetAttributes(network, GROUP_ATTRIBUTE);
		
		
		// Create a new clusterer
		RunChengChurch algorithm = new RunChengChurch(network, attributeArray, monitor, context);
						
		String resultsString = "ChengChurch results:";

		// Cluster the nodes
		monitor.setStatusMessage("Clustering nodes");
		Integer[] rowOrder = algorithm.cluster(false);
		//System.out.println("Nclusters: "+algorithm.getNClusters());
		//if (algorithm.getMatrix()==null)System.out.println("get matrix returns null : ");
		//if (!algorithm.getMatrix().isTransposed())
			//createGroups(network,algorithm.getMatrix(),algorithm.getNClusters(), clusters, "cheng&hurch");
		
		//createBiclusterGroups(algorithm.getClusterNodes());
		Matrix biclusterMatrix = algorithm.getBiclusterMatrix();
		int clusters[] = new int[biclusterMatrix.nRows()];
		createGroups(network,biclusterMatrix,1, clusters, "cheng&hurch");
		updateAttributes(network, GROUP_ATTRIBUTE, rowOrder, attributeArray, getAttributeList(), 
		                 algorithm.getBiclusterMatrix());
		
		createBiclusterTable(algorithm.getClusterNodes(),algorithm.getClusterAttrs());
		
		// System.out.println(resultsString);
		if (context.showUI) {
			insertTasksAfterCurrentTask(new BiclusterView(clusterManager));
		}
	}
	
	
	protected void createBiclusterGroups(Map<Integer, List<Long>> clusterNodes){
		
		List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>(); // List of node lists
		List<Long>groupList = new ArrayList<Long>(); // keep track of the groups we create
		createGroups = context.createGroups;
		attrList = new ArrayList<String>();
		
		for(Integer bicluster: clusterNodes.keySet()){
			String groupName = clusterAttributeName+"_"+bicluster;
			List<Long>suidList = clusterNodes.get(bicluster);
			List<CyNode>nodeList = new ArrayList<CyNode>();
			
			for(Long suid: suidList){
				CyNode node = network.getNode(suid);				
				attrList.add(network.getRow(node).get(CyNetwork.NAME, String.class)+"\t"+bicluster);
				nodeList.add(node);
			}
			
			if (createGroups) {
				CyGroup group = clusterManager.createGroup(network, groupName, nodeList, null, true);
				if (group != null)
					groupList.add(group.getGroupNode().getSUID());
			}			
		}
						
		// Adding a column per node by the clusterAttributeName, which will store a list of all the clusters to which the node belongs
				
		
		ModelUtils.createAndSetLocal(network, network, GROUP_ATTRIBUTE, groupList, List.class, Long.class);
		ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE, 
		                             getShortName(), String.class, null);
		ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_ATTRIBUTE, 
		                             clusterAttributeName, String.class, null);
		if (params != null)
			ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, 
		                               params, List.class, String.class);
				
	}

	
	public void createBiclusterTable(Map<Integer, List<Long>> clusterNodes ,Map<Integer, List<String>> clusterAttrs){
		CyTable networkTable = network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);
		CyTable BiClusterNodeTable = null;
		CyTable BiClusterAttrTable = null;		
				
		if(!CyTableUtil.getColumnNames(networkTable).contains(clusterAttributeName + "_NodeTable.SUID")){
			
			network.getDefaultNetworkTable().createColumn(clusterAttributeName + "_NodeTable.SUID", Long.class, false);
			BiClusterNodeTable = tableFactory.createTable(clusterAttributeName + "_NodeTable", "Node.SUID", Long.class, true, true);
			BiClusterNodeTable.createListColumn("Bicluster List", Integer.class, false);
		}
		else{
			long BiClusterTableSUID = network.getRow(network).get(clusterAttributeName + "_NodeTable.SUID", Long.class);
			BiClusterNodeTable = tableManager.getTable(BiClusterTableSUID);
			
		}
		
		if(!CyTableUtil.getColumnNames(networkTable).contains(clusterAttributeName + "_AttrTable.SUID")){
			
			network.getDefaultNetworkTable().createColumn(clusterAttributeName + "_AttrTable.SUID", Long.class, false);
			BiClusterAttrTable = tableFactory.createTable(clusterAttributeName + "_AttrTable", "BiCluster Number", Integer.class, true, true);
			BiClusterAttrTable.createListColumn("Bicluster Attribute List", String.class, false);
		}
		else{
			long BiClusterTableSUID = network.getRow(network).get(clusterAttributeName + "_AttrTable.SUID", Long.class);
			BiClusterAttrTable = tableManager.getTable(BiClusterTableSUID);
		}
				
		Map<Long,List<Integer>> biclusterList = new HashMap<Long,List<Integer>>();
		for(Integer clust : clusterNodes.keySet()){
			List<Long> temp = clusterNodes.get(clust);
			for(Long node : temp){
				if(biclusterList.containsKey(node)){
					biclusterList.get(node).add(clust);
				}
				else{
					List<Integer> newlist = new ArrayList<Integer>();
					newlist.add(clust);
					biclusterList.put(node, newlist);
				}
			}
		}
		
		CyRow TableRow;
		
		for(Long node:biclusterList.keySet()){
			TableRow = BiClusterNodeTable.getRow(node);
			TableRow.set("Bicluster List", biclusterList.get(node));			
		}
		
		for(Integer clust : clusterAttrs.keySet()){
			TableRow = BiClusterAttrTable.getRow(clust);
			TableRow.set("Bicluster Attribute List", clusterAttrs.get(clust));
		}
		
		network.getRow(network).set(clusterAttributeName + "_NodeTable.SUID", BiClusterNodeTable.getSUID());
		network.getRow(network).set(clusterAttributeName + "_AttrTable.SUID", BiClusterAttrTable.getSUID());
		tableManager.addTable(BiClusterNodeTable);
		tableManager.addTable(BiClusterAttrTable);
	}	
	
}
