package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;

import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.FuzzyNodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public abstract class AbstractNetworkClusterer extends AbstractClusterAlgorithm {
	
	// Shared instance variables
	CyTableManager tableManager = null;
	//TODO: add group support
	
	public AbstractNetworkClusterer(ClusterManager clusterManager) { 
		super(clusterManager); 
		tableManager = clusterManager.getTableManager();
	}

	@SuppressWarnings("unchecked")
	public List<List<CyNode>> getNodeClusters() {
		CyTable networkAttributes = network.getDefaultNetworkTable();
		Long netId = network.getSUID();

		String clusterAttribute = network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
		return getNodeClusters(clusterAttribute);
	}


	@SuppressWarnings("unchecked")
	public List<List<CyNode>> getNodeClusters(String clusterAttribute) {
		List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>(); // List of node lists
		CyTable nodeAttributes = network.getDefaultNodeTable();
		

		// Create the cluster Map
		HashMap<Integer, List<CyNode>> clusterMap = new HashMap<Integer, List<CyNode>>();
		for (CyNode node: (List<CyNode>)network.getNodeList()) {
			// For each node -- see if it's in a cluster.  If so, add it to our map
			if (nodeAttributes.getRow(node).get(clusterAttribute, Integer.class) != null) {
				Integer cluster = nodeAttributes.getRow(node).get(clusterAttribute, Integer.class);
				if (!clusterMap.containsKey(cluster)) {
					List<CyNode> nodeList = new ArrayList<CyNode>();
					clusterMap.put(cluster, nodeList);
						clusterList.add(nodeList);
				}
				clusterMap.get(cluster).add(node);
			}
		}
		return clusterList;
	}
	
	@SuppressWarnings("unchecked")
	public List<List<CyNode>> getFuzzyNodeClusters() {
		CyTable networkAttributes = network.getDefaultNetworkTable();
		Long netId = network.getSUID();

		String clusterAttribute = network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
		return getFuzzyNodeClusters(clusterAttribute);
	}
	
	@SuppressWarnings("unchecked")
	public List<List<CyNode>> getFuzzyNodeClusters(String clusterAttribute){
		List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>(); // List of node lists
		
		Long FuzzyClusterTableSUID = network.getRow(network, CyNetwork.LOCAL_ATTRS).get("FuzzyClusterTable.SUID", long.class);
		CyTable FuzzyClusterTable = tableManager.getTable(FuzzyClusterTableSUID);
		
		int numC = FuzzyClusterTable.getColumns().size() - 1;
		for(int i = 0; i < numC; i++){
			clusterList.add(new ArrayList<CyNode>());
		}
		
		List<CyNode> nodeList = network.getNodeList();
		for (CyNode node : nodeList){
			CyRow nodeRow = FuzzyClusterTable.getRow(node.getSUID());
			for(int i = 0; i < numC; i++){
				if(nodeRow.get("Cluster_"+ i, double.class) != null){
					clusterList.get(i).add(node);
				}
			}			
		}
						
		return clusterList;
	}

	protected List<List<CyNode>> createGroups(CyNetwork network, List<NodeCluster> clusters, String group_attr) {
		List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>(); // List of node lists
		List<Long>groupList = new ArrayList<Long>(); // keep track of the groups we create
		for (NodeCluster cluster: clusters) {
			int clusterNumber = cluster.getClusterNumber();
			String groupName = clusterAttributeName+"_"+clusterNumber;
			List<CyNode>nodeList = new ArrayList<CyNode>();

			for (CyNode node: cluster) {
				nodeList.add(node);
				ModelUtils.createAndSetLocal(network, node, clusterAttributeName, clusterNumber, Integer.class, null);
				if (NodeCluster.hasScore()) {
					ModelUtils.createAndSetLocal(network, node, clusterAttributeName+"_Score", clusterNumber, Double.class, null);
				}
			}

			if (createGroups) {
        CyGroup group = clusterManager.createGroup(network, clusterAttributeName+"_"+clusterNumber, nodeList, null, true);
				if (group != null)
					groupList.add(group.getGroupNode().getSUID());
			}
			clusterList.add(nodeList);
		}
		
		ModelUtils.createAndSetLocal(network, network, group_attr, groupList, List.class, Long.class);

		ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE, getShortName(), 
		                             String.class, null);
		ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_ATTRIBUTE, clusterAttributeName, 
		                             String.class, null);
		if (params != null)
			ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, params, 
		                               List.class, String.class);

		return clusterList;
	}
	
	protected List<List<CyNode>> createFuzzyGroups(CyNetwork network, 
	                                               List<FuzzyNodeCluster> clusters, String group_attr){
		
		List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>(); // List of node lists
		List<Long>groupList = new ArrayList<Long>(); // keep track of the groups we create
		
		for (FuzzyNodeCluster cluster: clusters) {
			int clusterNumber = cluster.getClusterNumber();
			String groupName = clusterAttributeName+"_"+clusterNumber;
			List<CyNode>nodeList = new ArrayList<CyNode>();

			for (CyNode node: cluster) {
				nodeList.add(node);
				//createAndSet(network, node, clusterAttributeName+"_"+clusterNumber, cluster.getMembership(node), Double.class, null);
				// network.getRow(node).set(clusterAttributeName, clusterNumber);
				if (FuzzyNodeCluster.hasScore()) {
					ModelUtils.createAndSetLocal(network, node, clusterAttributeName+"_"+clusterNumber+"_Membership", 
					                             cluster.getMembership(node), Double.class, null);
					// network.getRow(node).set(clusterAttributeName+"_Score", cluster.getClusterScore());
				}
			}

			if (createGroups) {
				CyGroup group = clusterManager.createGroup(network, clusterAttributeName+"_"+clusterNumber, nodeList, null, true);
				if (group != null)
					groupList.add(group.getGroupNode().getSUID());
			}
			clusterList.add(nodeList);
		}
		
		// Adding a column per node by the clusterAttributeName, which will store a list of all the clusters to which the node belongs
		List<CyNode> nodeList = network.getNodeList();
		
		for(int i = 0; i < nodeList.size(); i++ ){
			CyNode node = nodeList.get(i);
			List<Integer> listOfClusters = new ArrayList<Integer>();
			for(FuzzyNodeCluster cluster : clusters){
				if(cluster.getMembership(node) != null){
					listOfClusters.add(cluster.getClusterNumber());
				}
			}
			//ModelUtils.createAndSetLocal(network, node, clusterAttributeName, listOfClusters , Integer.class, null);
			
		}
		
		
		ModelUtils.createAndSetLocal(network, network, group_attr, groupList, List.class, Long.class);
		ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE, 
		                             getShortName(), String.class, null);
		ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_ATTRIBUTE, 
		                             clusterAttributeName, String.class, null);
		if (params != null)
			ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, 
		                               params, List.class, String.class);
				
		return clusterList;
	}

	
	
	protected void removeGroups(CyNetwork network, String group_attr) {
		if (network.getDefaultNetworkTable().getColumn(group_attr) != null) {
			List<Long> groupList = network.getRow(network, CyNetwork.LOCAL_ATTRS).getList(group_attr, Long.class);
			if (groupList != null) {
				for (Long groupSUID: groupList) {
					// remove the group
					clusterManager.removeGroup(network, groupSUID);
				}
			}
			network.getRow(network, CyNetwork.LOCAL_ATTRS).set(group_attr, null);
		}
	}
}
