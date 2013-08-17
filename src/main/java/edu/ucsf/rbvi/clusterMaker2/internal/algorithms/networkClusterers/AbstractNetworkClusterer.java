package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.FuzzyNodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;

public abstract class AbstractNetworkClusterer extends AbstractClusterAlgorithm {
	
	// Shared instance variables
	protected TaskMonitor monitor = null;
	protected List<String>params = null;
	protected CyNetwork network = null;

	//TODO: add group support
	
	public AbstractNetworkClusterer(ClusterManager manager) { super(manager); }

	public boolean isAvailable() {
		if (network.getRow(network).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class) == null) {
			return false;
		}

		String cluster_type = network.getRow(network).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);
		if (cluster_type == null || !cluster_type.toLowerCase().equals(getShortName()))
			return false;

		if (network.getRow(network).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class) != null) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public List<List<CyNode>> getNodeClusters() {
		CyTable networkAttributes = network.getDefaultNetworkTable();
		Long netId = network.getSUID();

		String clusterAttribute = network.getRow(network).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
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

	protected List<List<CyNode>> createGroups(CyNetwork network, List<NodeCluster> clusters) {
		List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>(); // List of node lists
		List<String>groupList = new ArrayList<String>(); // keep track of the groups we create
		for (NodeCluster cluster: clusters) {
			int clusterNumber = cluster.getClusterNumber();
			String groupName = clusterAttributeName+"_"+clusterNumber;
			List<CyNode>nodeList = new ArrayList<CyNode>();

			for (CyNode node: cluster) {
				nodeList.add(node);
				createAndSet(network, node, clusterAttributeName, clusterNumber, Integer.class, null);
				// network.getRow(node).set(clusterAttributeName, clusterNumber);
				if (NodeCluster.hasScore()) {
					createAndSet(network, node, clusterAttributeName+"_Score", clusterNumber, Double.class, null);
					// network.getRow(node).set(clusterAttributeName+"_Score", cluster.getClusterScore());
				}
			}

			if (createGroups) {
        // Create the group
        //         CyGroup newgroup = CyGroupManager.createGroup(groupName, nodeList, null);
        //
			}
			clusterList.add(nodeList);
			groupList.add(groupName);
		}
		
		// network.getRow(network).set(GROUP_ATTRIBUTE, groupList);
		createAndSet(network, network, GROUP_ATTRIBUTE, groupList, List.class, String.class);

		createAndSet(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE, getShortName(), String.class, null);
		createAndSet(network, network, ClusterManager.CLUSTER_ATTRIBUTE, clusterAttributeName, String.class, null);
		if (params != null)
			createAndSet(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, params, List.class, String.class);

		// network.getRow(network).set(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, getShortName());
		// network.getRow(network).set(ClusterManager.CLUSTER_ATTRIBUTE, clusterAttributeName);
		// network.getRow(network).set(ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, params);

		return clusterList;
	}
	
	protected List<List<CyNode>> createFuzzyGroups(CyNetwork network, List<FuzzyNodeCluster> clusters){
		
		List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>(); // List of node lists
		List<String>groupList = new ArrayList<String>(); // keep track of the groups we create
		
		for (FuzzyNodeCluster cluster: clusters) {
			int clusterNumber = cluster.getClusterNumber();
			String groupName = clusterAttributeName+"_"+clusterNumber;
			List<CyNode>nodeList = new ArrayList<CyNode>();

			for (CyNode node: cluster) {
				nodeList.add(node);
				//createAndSet(network, node, clusterAttributeName+"_"+clusterNumber, cluster.getMembership(node), Double.class, null);
				// network.getRow(node).set(clusterAttributeName, clusterNumber);
				if (FuzzyNodeCluster.hasScore()) {
					createAndSet(network, node, clusterAttributeName+"_"+clusterNumber+"_Membership", cluster.getMembership(node), Double.class, null);
					// network.getRow(node).set(clusterAttributeName+"_Score", cluster.getClusterScore());
				}
			}

			if (createGroups) {
        // Create the group
        //         CyGroup newgroup = CyGroupManager.createGroup(groupName, nodeList, null);
        //
			}
			clusterList.add(nodeList);
			groupList.add(groupName);
		}
		
		
		createAndSet(network, network, GROUP_ATTRIBUTE, groupList, List.class, String.class);

		createAndSet(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE, getShortName(), String.class, null);
		createAndSet(network, network, ClusterManager.CLUSTER_ATTRIBUTE, clusterAttributeName, String.class, null);
		if (params != null)
			createAndSet(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, params, List.class, String.class);
				
		return clusterList;
	}

	
	
	protected void removeGroups(CyNetwork network) {
		if (network.getDefaultNetworkTable().getColumn(GROUP_ATTRIBUTE) != null) {
			List<String> groupList = network.getRow(network).getList(GROUP_ATTRIBUTE, String.class);
			if (groupList != null) {
				for (String groupName: groupList) {
					// remove the group
				}
			}
		}
	}
}
