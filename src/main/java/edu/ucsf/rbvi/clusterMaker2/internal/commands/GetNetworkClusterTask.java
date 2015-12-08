package edu.ucsf.rbvi.clusterMaker2.internal.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

//clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class GetNetworkClusterTask extends AbstractTask implements ObservableTask {
	ClusterManager clusterManager;
	Map<Integer, List<CyNode>>clusterMap;
	String algName;

	@Tunable(description="Network to look for cluster in", context="nogui")
	public CyNetwork network;

	@Tunable(description="Cluster algorithm", context="nogui")
	public String algorithm;

	public GetNetworkClusterTask(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	public void run(TaskMonitor monitor) {
		System.out.println("GetNetworkClusterTask.run()");
		if (network == null)
			network = clusterManager.getNetwork();

		if (algorithm == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Algorithm must be specified");
			return;
		}

		ClusterTaskFactory algTF = clusterManager.getAlgorithm(algorithm);
		if (algTF == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Can't find algorithm: '"+algorithm+"'");
			return;
		}

		if (!algTF.getTypeList().contains(ClusterTaskFactory.ClusterType.NETWORK) &&
        !algTF.getTypeList().contains(ClusterTaskFactory.ClusterType.FILTER)) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Algorithm: '"+algorithm+"' is not a network clusterer");
			return;
		}

		if (!algTF.isAvailable(network)) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "No data for algorithm: '"+algorithm+"' in this network");
			return;
		}

		algName = algTF.getShortName();

		String clusterAttribute =
			network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);

		boolean isFuzzy = 
			network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS).getColumn(clusterAttribute).getType().equals(List.class);

		clusterMap = new HashMap<Integer, List<CyNode>>();
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
		}
	}

	private void addNodeToMap(Map<Integer, List<CyNode>> map, Integer cluster, CyNode node) {
		if (!map.containsKey(cluster))
			map.put(cluster, new ArrayList<CyNode>());
		map.get(cluster).add(node);
	}

	public Object getResults(Class clzz) {
		if (clusterMap == null) return null;
		if (clzz.equals(Map.class)) {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("type", algName);
			resultMap.put("networkclusters", clusterMap.values());
			return resultMap;			
		}

		String resultString = "Network clusters: \n";
		for (Integer clusterNumber: clusterMap.keySet()) {
			List<CyNode> nodeList = clusterMap.get(clusterNumber);
			String out = "   "+clusterNumber+": [";
			for (CyNode node: nodeList) {
				out += node.getSUID()+",";
			}
			resultString += out.substring(0, out.length()-1)+"]\n";
		}
		return resultString;
	}
}

