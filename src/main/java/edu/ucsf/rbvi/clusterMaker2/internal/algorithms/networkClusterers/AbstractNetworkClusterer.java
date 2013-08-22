package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;

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

public abstract class AbstractNetworkClusterer extends AbstractClusterAlgorithm 
                                               implements RequestsUIHelper {
	
	// Shared instance variables
	protected TaskMonitor monitor = null;
	protected List<String>params = null;
	protected CyNetwork network = null;
	protected Window parent = null;

	//TODO: add group support
	
	public AbstractNetworkClusterer(ClusterManager clusterManager) { super(clusterManager); }

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
		List<Long>groupList = new ArrayList<Long>(); // keep track of the groups we create
		for (NodeCluster cluster: clusters) {
			int clusterNumber = cluster.getClusterNumber();
			String groupName = clusterAttributeName+"_"+clusterNumber;
			List<CyNode>nodeList = new ArrayList<CyNode>();

			for (CyNode node: cluster) {
				nodeList.add(node);
				ModelUtils.createAndSet(network, node, clusterAttributeName, clusterNumber, Integer.class, null);
				if (NodeCluster.hasScore()) {
					ModelUtils.createAndSet(network, node, clusterAttributeName+"_Score", clusterNumber, Double.class, null);
				}
			}

			if (createGroups) {
        CyGroup group = clusterManager.createGroup(network, clusterAttributeName+"_"+clusterNumber, nodeList, null, true);
				if (group != null)
					groupList.add(group.getGroupNode().getSUID());
			}
			clusterList.add(nodeList);
		}
		
		ModelUtils.createAndSet(network, network, GROUP_ATTRIBUTE, groupList, List.class, String.class);

		ModelUtils.createAndSet(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE, getShortName(), String.class, null);
		ModelUtils.createAndSet(network, network, ClusterManager.CLUSTER_ATTRIBUTE, clusterAttributeName, String.class, null);
		if (params != null)
			ModelUtils.createAndSet(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, params, List.class, String.class);

		return clusterList;
	}
	
	protected List<List<CyNode>> createFuzzyGroups(CyNetwork network, List<FuzzyNodeCluster> clusters){
		
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
					ModelUtils.createAndSet(network, node, clusterAttributeName+"_"+clusterNumber+"_Membership", cluster.getMembership(node), Double.class, null);
					// network.getRow(node).set(clusterAttributeName+"_Score", cluster.getClusterScore());
				}
			}

			if (createGroups) {
        // Create the group
        //         CyGroup newgroup = CyGroupManager.createGroup(groupName, nodeList, null);
        //
			}
			clusterList.add(nodeList);
		}
		
		
		ModelUtils.createAndSet(network, network, GROUP_ATTRIBUTE, groupList, List.class, String.class);

		ModelUtils.createAndSet(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE, getShortName(), String.class, null);
		ModelUtils.createAndSet(network, network, ClusterManager.CLUSTER_ATTRIBUTE, clusterAttributeName, String.class, null);
		if (params != null)
			ModelUtils.createAndSet(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, params, List.class, String.class);
				
		return clusterList;
	}

	
	
	protected void removeGroups(CyNetwork network) {
		if (network.getDefaultNetworkTable().getColumn(GROUP_ATTRIBUTE) != null) {
			List<Long> groupList = network.getRow(network).getList(GROUP_ATTRIBUTE, Long.class);
			if (groupList != null) {
				for (Long groupSUID: groupList) {
					// remove the group
					clusterManager.removeGroup(network, groupSUID);
				}
			}
			network.getRow(network).set(GROUP_ATTRIBUTE, null);
		}
	}

	public void setUIHelper(TunableUIHelper helper) { }

}
