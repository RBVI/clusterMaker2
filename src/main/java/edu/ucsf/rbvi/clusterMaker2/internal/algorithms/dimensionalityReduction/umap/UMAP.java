package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.umap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobData;
import org.cytoscape.jobs.CyJobDataService;
import org.cytoscape.jobs.CyJobExecutionService;
import org.cytoscape.jobs.CyJobManager;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.jobs.SUIDUtil;
import org.cytoscape.jobs.CyJobStatus.Status;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.json.simple.JSONArray;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.ScatterPlotDialog;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJob;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.RemoteServer;

public class UMAP extends AbstractNetworkClusterer {
	public static String NAME = "UMAP remote";
	public static String SHORTNAME = "umap";
	final CyServiceRegistrar registrar;
	public final static String GROUP_ATTRIBUTE = "__UMAPGroups.SUID";
	
	@ContainsTunables
	public UMAPContext context = null;
	
	public UMAP(UMAPContext context, ClusterManager manager, CyServiceRegistrar registrar) {
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
		monitor = taskMonitor;
		// Get the execution service
		CyJobExecutionService executionService = registrar.getService(CyJobExecutionService.class, "(title=ClusterJobExecutor)");
		CyApplicationManager appManager = registrar.getService(CyApplicationManager.class);
        CyNetwork currentNetwork = appManager.getCurrentNetwork(); //gets the network presented in Cytoscape
				
		clusterAttributeName = "__umap";

        List<String> attributes = context.getnodeAttributeList().getSelectedValues(); // rather than get single select attribute, make it multiple select

		CyTable nodeTable = currentNetwork.getDefaultNodeTable();
		List<String> columns = new ArrayList<>();
		columns.add("name");
		columns.addAll(attributes);
				
				// creating the data itself, values of the columns chosen for each row (node)
		List<List<String>> data = new ArrayList<>();
		HashMap<Long, String> nodeMap = getNetworkNodes(currentNetwork);
				
		for (Long nodeSUID : nodeMap.keySet()) {
			CyRow row = nodeTable.getRow(nodeSUID);
			List<String> rowList = new ArrayList<>();
			for (String columnName : columns) {
				CyColumn column = nodeTable.getColumn(columnName);
				if (row.get(columnName, column.getType()) != null) {
					rowList.add(row.get(columnName, column.getType()).toString());
				} else {
					rowList.add("0");
				}
			}
			data.add(rowList);
		}
						
		String basePath = RemoteServer.getBasePath();
						
						// Get our initial job
		ClusterJob job = (ClusterJob) executionService.createCyJob("ClusterJob"); //creates a new ClusterJob object
						// Get the data service
		CyJobDataService dataService = job.getJobDataService(); //gets the dataService of the execution service
						// Add our data
		CyJobData jobData = dataService.addData(null, "columns", columns);
		jobData = dataService.addData(jobData, "data", data);
		job.storeClusterData(clusterAttributeName, currentNetwork, clusterManager, createGroups, GROUP_ATTRIBUTE, null, getShortName());
						// Create our handler
		ClusterJobHandler jobHandler = new ClusterJobHandler(job, network);
		job.setJobMonitor(jobHandler);
						// Submit the job
		CyJobStatus exStatus = executionService.executeJob(job, basePath, null, jobData);
				
		CyJobStatus.Status status = exStatus.getStatus();
		System.out.println("Status: " + status);
		
		if (status == Status.FINISHED) {
			
			CyJobData cyjobdata = dataService.getDataInstance();
			executionService.fetchResults(job, cyjobdata);
			
			// arranging the dimensionality reduction data into coordinates[] and nodes[] columns
			JSONArray embedding = (JSONArray) cyjobdata.get("embedding"); //getting the relevant data from the data object
			int size = embedding.size(); 
    
			CyNode[] nodes = new CyNode[size-1]; 
			double[][] coordinates = new double[size-1][2];
			
			for (int i = 1; i < size; i++) {
				JSONArray nodeData = (JSONArray) embedding.get(i);
				String nodeName = (String) nodeData.get(0);
				
				for (CyNode cyNode : network.getNodeList()) {// getting the cyNode object with the name of the node
					if (network.getRow(cyNode).get(CyNetwork.NAME, String.class).equals(nodeName)) {
						nodes[i-1] = cyNode;

					}
				} 
				
				double x = (Double) nodeData.get(1); 
				double y = (Double) nodeData.get(2); 
				coordinates[i-1][0] = x;
				coordinates[i-1][1] = y;
			}

			String newmapX = SHORTNAME + "_x";
			String newmapY = SHORTNAME + "_y";
			
			Boolean columnExists = false;
			for(CyColumn col : nodeTable.getColumns()) {
				if (col.getName().equals(newmapX) || col.getName().equals(newmapY)) {
					columnExists = true;
					break;
				}
			}
			
			if (!columnExists) {
				nodeTable.createColumn(newmapX, Double.class, false);
				nodeTable.createColumn(newmapY, Double.class, false);
			}
			
			
			for (int i = 0; i < nodes.length; i++) {
			   if (nodes[i] != null) {
				   network.getRow(nodes[i]).set(newmapX, coordinates[i][0]);
				   System.out.println("X value from the table : " + network.getRow(nodes[i]).get(newmapX, Double.class));
				   network.getRow(nodes[i]).set(newmapY, coordinates[i][1]);
				   System.out.println("Y value from the table : " + network.getRow(nodes[i]).get(newmapY, Double.class));
			   }
			}

			
			Map<String, Object> clusterData = job.getClusterData().getAllValues();
			if (context.showScatterPlot) {
				ClusterManager manager = (ClusterManager) clusterData.get("manager");
				ScatterPlotDialog scatter = new ScatterPlotDialog(manager, "UMAP", null, nodes, coordinates);
			}
					
		} else if (status == Status.RUNNING 
				|| status == Status.SUBMITTED 
				|| status == Status.QUEUED) {
			CyJobManager manager = registrar.getService(CyJobManager.class);
			manager.addJob(job, jobHandler, 5); //this one shows the load button
					
		} else if (status == Status.ERROR 
				|| status == Status.UNKNOWN  
				|| status == Status.CANCELED 
				|| status == Status.FAILED
				|| status == Status.TERMINATED 
				|| status == Status.PURGED) {
			monitor.setStatusMessage("Job status: " + status);
		}
				
						// Save our SUIDs in case we get saved and restored
		SUIDUtil.saveSUIDs(job, currentNetwork, currentNetwork.getNodeList());

	}

}
