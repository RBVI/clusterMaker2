package edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils;

import java.util.Map;

import org.cytoscape.jobs.AbstractCyJob;
import org.cytoscape.jobs.CyJobData;
import org.cytoscape.jobs.CyJobDataService;
import org.cytoscape.jobs.CyJobExecutionService;
import org.cytoscape.jobs.CyJobMonitor;
import org.cytoscape.model.CyNetwork;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;

public class ClusterJob extends AbstractCyJob {
	
	private CyJobData clusterData = dataService.getDataInstance();

	public ClusterJob(String name, String basePath, 
	                  CyJobExecutionService executionService, 
										CyJobDataService dataService, CyJobMonitor jobHandler,
										String jobId) {
		super(name, basePath, executionService, dataService, jobHandler);
		this.jobId = jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public void setBasePath(String basePath) {
		this.path = basePath;
	}
	
	public String getJobId() {
		return this.jobId;
	}
	
	public String getBasePath() {
		return this.path;
	}
	
	public void storeClusterData(String clusterAttributeName, CyNetwork network, ClusterManager manager, Boolean createGroups, String group_attr) {
		dataService.addData(clusterData, "clusterAttributeName", clusterAttributeName);
		dataService.addData(clusterData, "network", network);
		dataService.addData(clusterData, "manager", manager);
		dataService.addData(clusterData, "createGroups", createGroups);
		dataService.addData(clusterData, "group_attr", group_attr);
	}
	
	public CyJobData getClusterData() {
		return this.clusterData;
	}
	
}
