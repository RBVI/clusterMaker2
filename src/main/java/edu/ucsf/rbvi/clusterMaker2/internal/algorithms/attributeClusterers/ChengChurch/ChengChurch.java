package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.ChengChurch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractAttributeClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.KnnView;

public class ChengChurch extends AbstractAttributeClusterer {

	public static String SHORTNAME = "cheng&church";
	public static String NAME = "Cheng & Church's  bi-cluster";
	public static String GROUP_ATTRIBUTE = SHORTNAME;
	
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

		// Cluster the attributes, if requested
		if (context.clusterAttributes && attributeArray.length > 1) {
			monitor.setStatusMessage("Clustering attributes");
			int[] clusters = algorithm.cluster(true);
			if (!algorithm.getMatrix().isTransposed())
				createGroups(network,algorithm.getMatrix(),algorithm.getNClusters(), clusters, "cheng&hurch");
			
			Integer[] rowOrder = algorithm.getMatrix().indexSort(clusters, clusters.length);
			updateAttributes(network, GROUP_ATTRIBUTE, rowOrder, attributeArray, getAttributeList(), 
			                 algorithm.getMatrix());
		}

		// Cluster the nodes
		monitor.setStatusMessage("Clustering nodes");
		int[] clusters = algorithm.cluster(false);
		//System.out.println("Nclusters: "+algorithm.getNClusters());
		//if (algorithm.getMatrix()==null)System.out.println("get matrix returns null : ");
		if (!algorithm.getMatrix().isTransposed())
			createGroups(network,algorithm.getMatrix(),algorithm.getNClusters(), clusters, "cheng&hurch");
		
		Integer[] rowOrder = algorithm.getMatrix().indexSort(clusters, clusters.length);
		updateAttributes(network, GROUP_ATTRIBUTE, rowOrder, attributeArray, getAttributeList(), 
		                 algorithm.getMatrix());
		
		createBiclusterTable(algorithm.getClusterNodes(),algorithm.getClusterAttrs());
		
		// System.out.println(resultsString);
		if (context.showUI) {
			insertTasksAfterCurrentTask(new KnnView(clusterManager));
		}
	}
	
	public void createBiclusterTable(HashMap<Integer, ArrayList<Long>> clusterNodes ,HashMap<Integer, ArrayList<String>> clusterAttrs){
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
				
		HashMap<Long,ArrayList<Integer>> biclusterList = new HashMap<Long,ArrayList<Integer>>();
		for(Integer clust : clusterNodes.keySet()){
			ArrayList<Long> temp = clusterNodes.get(clust);
			for(Long node : temp){
				if(biclusterList.containsKey(node)){
					biclusterList.get(node).add(clust);
				}
				else{
					ArrayList<Integer> newlist = new ArrayList<Integer>();
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
