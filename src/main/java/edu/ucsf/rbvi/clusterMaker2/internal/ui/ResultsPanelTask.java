package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class ResultsPanelTask extends AbstractTask implements ClusterViz, ClusterAlgorithm {

	private static String appName = "ClusterMaker Results Panel";
	private boolean checkForAvailability = false;
	private ClusterManager manager;
	private String clusterAttribute = null;
	public static String CLUSTERNAME = "Create Results Panel from Clusters";
	public static String CLUSTERSHORTNAME = "clusterResultsPanel";
	public ResultsPanel resultsPanel;
	private final CyServiceRegistrar registrar;

	public List<NodeCluster> clusters;
	public AbstractClusterResults clusterResults;
	private CyNetworkView networkView;

	public boolean createFlag;

	@Tunable(description="Network to look for cluster", context="nogui")
	public CyNetwork network = null;

	public ResultsPanelTask(CyNetwork network, ClusterManager manager,List<NodeCluster> clusters, boolean createFlag) {
		this(manager, true,createFlag);
		this.network = network;
	}


	public ResultsPanelTask( ClusterManager manager, boolean available, boolean createFlag) {
		this.manager = manager;
		this.createFlag = createFlag;
		networkView = manager.getNetworkView();

		registrar = manager.getService(CyServiceRegistrar.class);

		checkForAvailability = available;
		if (network == null)
			network = manager.getNetwork();

		//System.out.println("Number of clusters: "+clusters.size());

	}

	public List<NodeCluster> getClusters(){
		List<NodeCluster> clusters = new ArrayList<NodeCluster>();
		List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>();
		/*
		System.out.println(network.NAME);
		System.out.println(CyNetwork.LOCAL_ATTRS);
		System.out.println(ClusterManager.CLUSTER_ATTRIBUTE);
		*/
		clusterAttribute =
				network.getRow(network, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);

		//Create a temporary cluster map
		Map<Integer, ArrayList<CyNode>> clusterMap = new HashMap<Integer, ArrayList<CyNode>>();
		for (CyNode node: (List<CyNode>)network.getNodeList()) {
			// For each node -- see if it's in a cluster.  If so, add it to our map
			if (ModelUtils.hasAttribute(network, node, clusterAttribute)) {

				Integer cluster = network.getRow(node).get(clusterAttribute, Integer.class);
				if (!clusterMap.containsKey(cluster))
					clusterMap.put(cluster, new ArrayList<CyNode>());
				clusterMap.get(cluster).add(node);
			}
		}

		for(int clustNum : clusterMap.keySet()){
			NodeCluster cluster = new NodeCluster(clusterMap.get(clustNum));
			clusterList.add(clusterMap.get(clustNum));

			cluster.setClusterNumber(clustNum);
			//System.out.println("Adding cluster number "+clustNum);
			clusters.add(cluster);
		}

		//calculating the scores for each cluster
		clusterResults =  new AbstractClusterResults(network,clusterList);
		List<Double> modularityList = clusterResults.getModularityList();
		for(int i = 0; i < clusters.size() ;i++){
			clusters.get(i).setClusterScore(modularityList.get(i));
		}
		return clusters;

	}


	public void run(TaskMonitor monitor) {
		if (createFlag){
			monitor.setTitle("Creating a new results panel with cluster results");
			clusters = getClusters();
			// System.out.println("Before invoking RP");
			resultsPanel = new ResultsPanel(clusters,clusterResults,network,networkView,manager,monitor);
			// System.out.println("Before registering RP as cytoPanel");
			registrar.registerService(resultsPanel, CytoPanelComponent.class, new Properties());
			//System.out.println("After registering RP as cytoPanel");
			manager.setResultsPanel(resultsPanel);
			//System.out.println("After saving RP in manager");

		}
		else{
			//need to retrieve the saved resultsPanel to unregister it
			monitor.setTitle("Deleting the results panel");
			resultsPanel = manager.getResultsPanel();
			registrar.unregisterService(resultsPanel, CytoPanelComponent.class);
			manager.setResultsPanel(null);
		}

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
