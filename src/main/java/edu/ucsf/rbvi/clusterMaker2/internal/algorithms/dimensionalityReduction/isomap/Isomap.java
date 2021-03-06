package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.isomap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.jobs.CyJobData;
import org.cytoscape.jobs.CyJobDataService;
import org.cytoscape.jobs.CyJobExecutionService;
import org.cytoscape.jobs.CyJobManager;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.jobs.SUIDUtil;
import org.cytoscape.jobs.CyJobStatus.Status;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJob;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.RemoteServer;

public class Isomap extends AbstractNetworkClusterer {
	public static String NAME = "Isomap remote";
	public static String SHORTNAME = "isomap";
	final CyServiceRegistrar registrar;
	public final static String GROUP_ATTRIBUTE = "__IsomapGroups.SUID";
	
	@ContainsTunables
	public IsomapContext context = null;
	
	public Isomap(IsomapContext context, ClusterManager manager, CyServiceRegistrar registrar) {
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
				
		clusterAttributeName = "__isomap";
        List<String> attributes = context.getnodeAttributeList().getSelectedValues(); // rather than get single select attribute, make it multiple select
				
				// list of column names wanted to use in UMAP
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
			executionService.fetchResults(job, dataService.getDataInstance()); 
					
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
