package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.dimensionalityReduction.tSNERemote;

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
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.json.simple.JSONArray;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.ScatterPlotDialog;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJob;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.DimensionalityReductionJobHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.RemoteServer;

public class tSNERemote extends AbstractNetworkClusterer {
	public static String NAME = "tSNE (remote)";
	public static String SHORTNAME = "tsneremote";
	final CyServiceRegistrar registrar;
	public final static String GROUP_ATTRIBUTE = "__tSNERemoteGroups.SUID";
	
	@ContainsTunables
	public tSNERemoteContext context = null;
	
	public tSNERemote(tSNERemoteContext context, ClusterManager manager, CyServiceRegistrar registrar) {
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
  @ProvidesTitle
	public String getName() {return NAME;}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		monitor = taskMonitor;
		// Get the execution service
		CyJobExecutionService executionService = 
						registrar.getService(CyJobExecutionService.class, "(title=ClusterJobExecutor)");
		CyApplicationManager appManager = registrar.getService(CyApplicationManager.class);
		CyNetwork currentNetwork = appManager.getCurrentNetwork(); //gets the network presented in Cytoscape
		
		HashMap<String, Object> configuration = new HashMap<>();
		if (context.isSynchronous == true) {
			configuration.put("waitTime", -1);
		} else {
			configuration.put("waitTime", 20);
		}

    configuration.put("perplexity", context.perplexity);
    configuration.put("early_exaggeration", context.early_exaggeration);
    configuration.put("metric", context.metric.getSelectedValue());
    configuration.put("learning_rate", context.learning_rate);
    configuration.put("n_iter", context.n_iter);
    configuration.put("init", context.init.getSelectedValue());

				
		clusterAttributeName = "__tsneremote";
		List<String> attributes = context.getnodeAttributeList().getSelectedValues();
				
		// list of column names
		CyTable nodeTable = currentNetwork.getDefaultNodeTable();
		List<String> columns = new ArrayList<>();
		columns.add("name");
		columns.addAll(attributes);
				
		// creating the data itself, values of the columns chosen for each row (node)
		List<List<String>> data = new ArrayList<>();
		HashMap<Long, String> nodeMap = getNetworkNodes(currentNetwork);
				
		for (Long nodeSUID : nodeMap.keySet()) {
			CyRow row = nodeTable.getRow(nodeSUID);
      if (context.selectedOnly && !row.get(CyNetwork.SELECTED, Boolean.class))
        continue;
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
		ClusterJob job = (ClusterJob) executionService.createCyJob(SHORTNAME); //creates a new ClusterJob object
				// Get the data service
		CyJobDataService dataService = job.getJobDataService(); //gets the dataService of the execution service
				// Add our data
		CyJobData jobData = dataService.addData(null, "columns", columns);
		jobData = dataService.addData(jobData, "data", data);
		job.storeClusterData(clusterAttributeName, currentNetwork, clusterManager, createGroups, GROUP_ATTRIBUTE, null, getShortName());
				// Create our handler
		DimensionalityReductionJobHandler jobHandler = new DimensionalityReductionJobHandler(job, network, context.showScatterPlot);
		job.setJobMonitor(jobHandler);
				// Submit the job
		CyJobStatus exStatus = executionService.executeJob(job, basePath, configuration, jobData);
				
		CyJobStatus.Status status = exStatus.getStatus();
		System.out.println("Status: " + status);
		if (status == Status.FINISHED) {
			jobHandler.loadData(job, taskMonitor);
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
