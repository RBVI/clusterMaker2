package org.cytoscape.myapp.internal.algorithms.networkClusterers;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import cytoscape.task.ui.JTaskConfig;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;

import org.cytoscape.myapp.internal.ClusterMaker;
import org.cytoscape.myapp.internal.ui.ClusterTask;
import org.cytoscape.myapp.internal.algorithms.AbstractClusterAlgorithm;
import org.cytoscape.myapp.internal.algorithms.NodeCluster;
import org.cytoscape.myapp.internal.algorithms.edgeConverters.EdgeAttributeHandler;

public abstract class AbstractNetworkClusterer extends AbstractClusterAlgorithm {
	
	// Shared instance variables
		protected EdgeAttributeHandler edgeAttributeHandler = null;
		protected TaskMonitor monitor = null;
		protected Logger logger = null;
		protected List<String>params = null;
		
		// For simple divisive clustering, these routines will do the group handling
		@SuppressWarnings("unchecked")
		protected void removeGroups(List<CyColumn> netAttributes, Long networkID) {
			// See if we already have groups defined (from a previous run?)
/* How to port this?*/			if (netAttributes.hasAttribute(networkID, GROUP_ATTRIBUTE)) {
				List<String> groupList = (List<String>)netAttributes.getListAttribute(networkID, GROUP_ATTRIBUTE);
				for (String groupName: groupList) {
					CyGroup group = CyGroupManager.findGroup(groupName);
					if (group != null)
						CyGroupManager.removeGroup(group);
				}
			}
		}
		
		// We don't want to autodispose our task monitors
		public JTaskConfig getDefaultTaskConfig() { return ClusterTask.getDefaultTaskConfig(true); }

		protected List<List<CyNode>> createGroups(CyTable netAttributes, 
		                                          Long networkID,
		                                          CyTable nodeAttributes, 
		                                          List<NodeCluster> cMap) { 

			List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>(); // List of node lists
			List<String>groupList = new ArrayList<String>(); // keep track of the groups we create
			CyGroup first = null;
			for (NodeCluster cluster: cMap) {
				int clusterNumber = cluster.getClusterNumber();
				String groupName = clusterAttributeName+"_"+clusterNumber;
				List<CyNode>nodeList = new ArrayList<CyNode>();

				for (CyNode node: cluster) {
					nodeList.add(node);
					nodeAttributes.getRow(node).set(clusterAttributeName, clusterNumber);
					if (NodeCluster.hasScore()) {
						nodeAttributes.getRow(node).set(clusterAttributeName+"_Score", cluster.getClusterScore());
					}
				}
				
				if (createGroups) {
					// Create the group
					CyNetwork network = CyNetworkManager.getNetwork(networkID);
					CyGroup newgroup = CyGroupFactory.createGroup(network, nodeList, null, (Boolean) null);
					
					
					if (newgroup != null) {
						first = newgroup;
						// Now tell the metanode viewer about it
						CyGroupManager.setGroupViewer(newgroup, "metaNode", 
						                              Cytoscape.getCurrentNetworkView(), false);
						// And, finally, set the score on the group itself
						nodeAttributes.getRow(newgroup.getGroupNode().getSUID()).set(clusterAttributeName, clusterNumber);
						if (NodeCluster.hasScore()) {
							nodeAttributes.getRow(newgroup.getGroupNode().getSUID()).set(clusterAttributeName+"_Score", cluster.getClusterScore());
						}
					}
				}
				clusterList.add(nodeList);
				groupList.add(groupName);
			}
			if (first != null)
				CyGroupManager.setGroupViewer(first, "metaNode", 
				                              Cytoscape.getCurrentNetworkView(), true);
			
			// Save the network attribute so we remember which groups are ours
/* not sure what is happening here*/			netAttributes.setListAttribute(networkID, GROUP_ATTRIBUTE, groupList);

			// Add parameters to our list
			params = new ArrayList<String>();
			setParams(params);
			
			// Set up the appropriate attributes
			CyAppAdapter adapter;
			CyApplicationManager manager = adapter.getCyApplicationManager();
			CyNetwork network = manager.getCurrentNetwork();
			Long netId = network.getSUID();
			netAttributes.getRow(netId).set(ClusterMaker.CLUSTER_TYPE_ATTRIBUTE, getShortName());
			netAttributes.getRow(netId).set(ClusterMaker.CLUSTER_ATTRIBUTE, clusterAttributeName);
			netAttributes.getRow(netId).set(ClusterMaker.CLUSTER_PARAMS_ATTRIBUTE, params);
		
			return clusterList;
		}
		
		protected void setParams(List<String> params) {
			if (edgeAttributeHandler != null)
				edgeAttributeHandler.setParams(params);
		}

		public boolean isAvailable() {
			CyAppAdapter adapter;
			CyApplicationManager manager = adapter.getCyApplicationManager();
			CyNetwork network = manager.getCurrentNetwork();
			CyTable networkAttributes = network.getDefaultNetworkTable();
			Long netId = network.getSUID();
/*CHECK*/			if (!networkAttributes.getRow(netId).get(ClusterMaker.CLUSTER_TYPE_ATTRIBUTE, String.class)) {
				return false;
			}

			String cluster_type = networkAttributes.getRow(netId).get(ClusterMaker.CLUSTER_TYPE_ATTRIBUTE, String.class);
			if (cluster_type == null || !cluster_type.toLowerCase().equals(getShortName()))
				return false;

			if (networkAttributes.getRow(netId).get(ClusterMaker.CLUSTER_TYPE_ATTRIBUTE, String.class)) {
				return true;
			}
			return false;
		}

		@SuppressWarnings("unchecked")
		public List<List<CyNode>> getNodeClusters() {
			CyAppAdapter adapter;
			CyApplicationManager manager = adapter.getCyApplicationManager();
			CyNetwork network = manager.getCurrentNetwork();
			CyTable networkAttributes = network.getDefaultNetworkTable();
			Long netId = network.getSUID();

			String clusterAttribute = networkAttributes.getStringAttribute(netId, ClusterMaker.CLUSTER_ATTRIBUTE);
			return getNodeClusters(clusterAttribute);
		}


		@SuppressWarnings("unchecked")
		public List<List<CyNode>> getNodeClusters(String clusterAttribute) {
			List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>(); // List of node lists
			CyAppAdapter adapter;
			CyApplicationManager manager = adapter.getCyApplicationManager();
			CyNetwork network = manager.getCurrentNetwork();
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



}
