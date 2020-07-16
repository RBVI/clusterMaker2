package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Leiden;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobData;
import org.cytoscape.jobs.CyJobDataService;
import org.cytoscape.jobs.CyJobExecutionService;
import org.cytoscape.jobs.CyJobManager;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.jobs.SUIDUtil;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobData;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobDataService;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobExecutionService;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.RemoteServer;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobHandler;

public class LeidenCluster extends AbstractNetworkClusterer {
	public static String NAME = "Leiden Clusterer";
	public static String SHORTNAME = "leiden";
	final CyServiceRegistrar registrar;

	@ContainsTunables
	public LeidenContext context = null;
	
	public LeidenCluster(LeidenContext context, ClusterManager manager, CyServiceRegistrar registrar) {
		super(manager);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
		this.registrar = registrar;
	}

	@Override
	public String getShortName() {return SHORTNAME;}

	@Override
	public String getName() {return NAME;}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Get the execution service
		CyJobExecutionService executionService = 
						registrar.getService(CyJobExecutionService.class, "(title=ClusterJobExecutor)");
		CyApplicationManager appManager = registrar.getService(CyApplicationManager.class);
		CyNetwork currentNetwork = appManager.getCurrentNetwork(); //gets the network presented in Cytoscape
		System.out.println("Current network: " + currentNetwork.toString());
				
		HashMap<Long, String> nodeMap = getNetworkNodes(currentNetwork);
		List<String> nodeArray = new ArrayList<>();
		for (Long nodeSUID : nodeMap.keySet()) {
			nodeArray.add(nodeMap.get(nodeSUID));
		}
				
		System.out.println("Node array from the current network: " + nodeArray);
				
		List<String[]> edgeArray = getNetworkEdges(currentNetwork, nodeMap);
				
		String basePath = RemoteServer.getBasePath();
				
				// Get our initial job
		CyJob job = executionService.createCyJob("ClusterJob"); //creates a new ClusterJob object
				// Get the data service
		CyJobDataService dataService = job.getJobDataService(); //gets the dataService of the execution service
				// Add our data
		CyJobData jobData = dataService.addData(null, "nodes", nodeArray);
		jobData = dataService.addData(jobData, "edges", edgeArray);
				// Create our handler
		ClusterJobHandler jobHandler = new ClusterJobHandler(job, network);
		job.setJobMonitor(jobHandler);
				
				// Submit the job
		CyJobStatus exStatus = executionService.executeJob(job, basePath, null, jobData);
		if (exStatus.getStatus().equals(CyJobStatus.Status.ERROR) ||
					exStatus.getStatus().equals(CyJobStatus.Status.UNKNOWN)) {
			monitor.showMessage(TaskMonitor.Level.ERROR, exStatus.toString());
			return;
		}
		System.out.println("Back to SubmitJobTask! ExStatus is : " + exStatus);
				
				// Save our SUIDs in case we get saved and restored
		SUIDUtil.saveSUIDs(job, currentNetwork, currentNetwork.getNodeList());

		CyJobManager manager = registrar.getService(CyJobManager.class);
		manager.addJob(job, jobHandler, 5);
	}
	
	private HashMap<Long, String> getNetworkNodes(CyNetwork currentNetwork) {
		List<CyNode> cyNodeList = currentNetwork.getNodeList();
		
		HashMap<Long, String> nodeMap = new HashMap<>();
		for (CyNode node : cyNodeList) {
			String nodeName = currentNetwork.getRow(node).get(CyNetwork.NAME, String.class);
			nodeMap.put(node.getSUID(), nodeName);
		}
		
		return nodeMap;
	}
	
	private List<String[]> getNetworkEdges(CyNetwork currentNetwork, Map<Long, String> nodeMap) {
		CyTable edgeTable = currentNetwork.getDefaultEdgeTable();
		System.out.println("Edge columns: " + edgeTable.getColumns());
		List<CyEdge> cyEdgeList = currentNetwork.getEdgeList();
		
		List<String[]> edgeArray = new ArrayList<>();
		for (CyEdge edge : cyEdgeList) {
			
			String[] sourceTargetWeight = new String[3];
			
			CyNode source = edge.getSource();
			CyNode target = edge.getTarget();
			String sourceName = nodeMap.get(source.getSUID());
			sourceTargetWeight[0] = sourceName;
			String targetName = nodeMap.get(target.getSUID());
			sourceTargetWeight[1] = targetName;
			
			Double weight = currentNetwork.getRow(edge).get("weight", Double.class);
			
			if (weight == null) {
				sourceTargetWeight[2] = "1";
			} else {
				sourceTargetWeight[2] = String.valueOf(weight);
			}
			
			edgeArray.add(sourceTargetWeight);
		}
		
		return edgeArray;
	}
}
