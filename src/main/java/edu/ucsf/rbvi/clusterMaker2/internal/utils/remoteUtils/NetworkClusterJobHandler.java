package edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils;

import java.util.List;
import java.util.Map;

import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobData;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;

public class NetworkClusterJobHandler extends ClusterJobHandler {
	public boolean showUI;
	public boolean restoreEdges;
	
	public NetworkClusterJobHandler(CyJob job, CyNetwork network, Boolean showUI, Boolean restoreEdges) {
		super(job, network);
		this.showUI = showUI;
		this.restoreEdges = restoreEdges;
	}
	
	@Override
	public void loadData(CyJob job, TaskMonitor monitor) {
		CyJobData data = job.getJobDataService().getDataInstance();
		CyJobStatus status = job.getJobExecutionService().fetchResults(job, data);
		data.put("job", job);
		//CyNetwork network = job.getJobDataService().getNetworkData(data, "network");
		CyNetwork network = networkMap.get(job);
		// System.out.println("network: " + network);
		
		// network clustering algorithm
		
		Map<String, Object> clusterData = ((ClusterJob) job).getClusterData().getAllValues();
		
		String clusterAttributeName = (String) clusterData.get("clusterAttributeName");
		Boolean createGroups = (Boolean) clusterData.get("createGroups");
		String group_attr = (String) clusterData.get("group_attr");
		List<String> params  = (List<String>) clusterData.get("params");
		ClusterManager clusterManager = (ClusterManager) clusterData.get("manager");

		List<NodeCluster> nodeClusters = ClusterJobExecutionService.createClusters(data, clusterAttributeName, network); //move this to remote utils
		// System.out.println("NodeClusters: " + nodeClusters);

		AbstractNetworkClusterer.createGroups(network, nodeClusters, group_attr, clusterAttributeName, 
				clusterManager, createGroups, params, job.getJobName()); 
		
		if (showUI) {
			TaskManager manager = clusterManager.getService(TaskManager.class);
			manager.execute(new TaskIterator(new NewNetworkView(network, clusterManager, true, restoreEdges, false)));
		}
	}
		
}
