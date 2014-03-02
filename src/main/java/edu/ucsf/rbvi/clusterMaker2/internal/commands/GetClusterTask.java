package edu.ucsf.rbvi.clusterMaker2.internal.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

//clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class GetClusterTask extends AbstractTask implements ObservableTask {
	ClusterManager clusterManager;
	List<String>clusterParams;
	// The order of the clusters
	List<String>orderList = null;
	// The clusters
	List<String>clusterList = null;
	String algName;
	String clusterType;

	@Tunable(description="Network to look for cluster in", context="nogui")
	public CyNetwork network;

	@Tunable(description="Cluster algorithm", context="nogui")
	public String algorithm;

	@Tunable(description="Node clusters or Attribute clusters", context="nogui")
	public ListSingleSelection<String> type = new ListSingleSelection("node", "attribute");

	public GetClusterTask(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	public void run(TaskMonitor monitor) {
		if (network == null)
			network = clusterManager.getNetwork();

		if (algorithm == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Algorithm must be specified");
			return;
		}

		ClusterTaskFactory algTF = clusterManager.getAlgorithm(algorithm);
		if (algTF == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Can't find algorithm: '"+algorithm+"'");
		}
	
		if (!algTF.getTypeList().contains(ClusterTaskFactory.ClusterType.ATTRIBUTE)) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Algorithm: '"+algorithm+"' is not an attribute clusterer");
			return;
		}

		if (!algTF.isAvailable(network)) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "No data for algorithm: '"+algorithm+"' in this network");
			return;
		}

		algName = algTF.getShortName();

		if (ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE))
			clusterType = network.getRow(network).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);

		if (ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE))
			clusterParams = network.getRow(network).getList(ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, String.class);

		if (type.getSelectedValue().equals("node"))
  		getEisenClusters(ClusterManager.NODE_ORDER_ATTRIBUTE, ClusterManager.CLUSTER_NODE_ATTRIBUTE);
		else
  		getEisenClusters(ClusterManager.ARRAY_ORDER_ATTRIBUTE, ClusterManager.CLUSTER_ATTR_ATTRIBUTE);

	}

	private void getEisenClusters(String orderAttribute, String clusterAttribute) {

		// Get the order of the clusters
		orderList = network.getRow(network).getList(orderAttribute, String.class);

		// Get the clusters
		clusterList = network.getRow(network).getList(clusterAttribute, String.class);
	}

	public Object getResults(Class clzz) {
		if (orderList == null || clusterList == null) 
			return null;
		
		if (clzz.equals(List.class)) {
			List<List<String>> result = new ArrayList<List<String>>();
			result.add(orderList);
			result.add(clusterList);
			return result;
		} else if (clzz.equals(Map.class)) {
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("algorithm", algName);
			result.put("type", type.getSelectedValue());
			result.put("order", orderList);
			result.put("cluster", clusterList);
			return result;
		}

		String typeString;
		if (type.getSelectedValue().equals("node")) {
			typeString = "Node (gene) ";
		} else {
			typeString = "Attribute (array) ";
		}

		String result = typeString+"cluster results for "+algName+": \n";

    for (String cluster: clusterList) {
      result += "   "+cluster+"\n";
    }

		result += typeString+"cluster order for "+algName+": \n";

    for (String node: orderList) {
      result += "   "+node+"\n";
    }
		return result;
	}
}

