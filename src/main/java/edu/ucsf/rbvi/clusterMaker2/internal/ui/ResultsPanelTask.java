package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class ResultsPanelTask extends AbstractTask implements ClusterViz, ClusterAlgorithm {

	private static String appName = "ClusterMaker Results Panel";
	private boolean checkForAvailability = false;
	private ClusterManager manager;
	private String clusterAttribute = null;
	public static String CLUSTERNAME = "Create Results Panel from Clusters";
	public static String CLUSTERSHORTNAME = "clusterResultsPanel";
	public ResultsPanel resultsPanel;
	
	public final List<NodeCluster> clusters;
	private CyNetworkView networkView;
	
	@Tunable(description="Network to look for cluster", context="nogui")
	public CyNetwork network = null;
	
	public ResultsPanelTask(CyNetwork network, ClusterManager manager,List<NodeCluster> clusters) {
		this(manager, true);
		this.network = network;
	}

	
	public ResultsPanelTask( ClusterManager manager, boolean available) {
		this.manager = manager;
		networkView = manager.getNetworkView();
		
		this.clusters = getClusters();
		checkForAvailability = available;
		if (network == null)
			network = manager.getNetwork();
		
	}
	
	public List<NodeCluster> getClusters(){
		List<NodeCluster> clusters = null;
		
		return clusters;
		
	}
	

	public void run(TaskMonitor monitor) {
		monitor.setTitle("Creating a new results panel with cluster results");
		
		resultsPanel = new ResultsPanel(clusters, network,networkView,manager,monitor);
		
	}

	public ClusterResults getResults() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getShortName() {
		if (checkForAvailability) {
			return CLUSTERSHORTNAME;
		} else {
			return null; 
		}		
	}

	public String getName() {
		if (checkForAvailability) {
			return CLUSTERNAME;
		} else {
			return null; 
		}
	}

	public Object getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAvailable() {
		boolean available = ResultsPanelTask.isReady(network, manager);
		clusterAttribute =
			network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
		return available;
	}
	
	public static boolean isReady(CyNetwork network, ClusterManager manager) {
		if (network == null) 
			return false;

		CyTable networkTable = network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);
		if (!CyTableUtil.getColumnNames(networkTable).contains(ClusterManager.CLUSTER_TYPE_ATTRIBUTE))
			return false;

		String cluster_type = network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);
		if (manager.getAlgorithm(cluster_type) != null)
		if (manager.getAlgorithm(cluster_type) == null || 
		    !manager.getAlgorithm(cluster_type).getTypeList().contains(ClusterTaskFactory.ClusterType.NETWORK))
			return false;

		if (CyTableUtil.getColumnNames(networkTable).contains(ClusterManager.CLUSTER_ATTRIBUTE)) {
			String clusterAttribute = network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
			if (clusterAttribute != null) return true;
		}
		return false;
	}

}
