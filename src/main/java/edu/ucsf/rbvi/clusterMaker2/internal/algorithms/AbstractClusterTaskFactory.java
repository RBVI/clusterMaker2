package edu.ucsf.rbvi.clusterMaker2.internal.algorithms;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;

public abstract class AbstractClusterTaskFactory implements ClusterTaskFactory {
	protected ClusterManager clusterManager;

	public AbstractClusterTaskFactory(ClusterManager manager) {
		this.clusterManager = manager;
	}
	
	public boolean isReady() {
		if (clusterManager.getNetwork() == null)
			return false;
		return true;
	}

	public boolean isAvailable(CyNetwork network) {
		if (network == null) 
			return false;

		CyTable networkTable = network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);
		if (!CyTableUtil.getColumnNames(networkTable).contains(ClusterManager.CLUSTER_TYPE_ATTRIBUTE))
			return false;

		String cluster_type = 
			network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);

		if (!getShortName().equals(cluster_type))
			return false;

		if (getTypeList().contains(ClusterTaskFactory.ClusterType.ATTRIBUTE)) {
			if (CyTableUtil.getColumnNames(networkTable).contains(ClusterManager.NODE_ORDER_ATTRIBUTE)) {
				List<String>geneList = network.getRow(network).getList(ClusterManager.NODE_ORDER_ATTRIBUTE, String.class);
				if (geneList != null && geneList.size() > 0) return true;
			}

			if (CyTableUtil.getColumnNames(networkTable).contains(ClusterManager.ARRAY_ORDER_ATTRIBUTE)) {
				List<String>arrayList = network.getRow(network).getList(ClusterManager.ARRAY_ORDER_ATTRIBUTE, String.class);
				if (arrayList != null && arrayList.size() > 0) return true;
			}
		} else if (getTypeList().contains(ClusterTaskFactory.ClusterType.NETWORK)) {
			if (CyTableUtil.getColumnNames(networkTable).contains(ClusterManager.CLUSTER_ATTRIBUTE)) {
				String clusterAttribute = 
					network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
				if (clusterAttribute != null) 
					return true;
			}
		}

		return false;
	}
}
