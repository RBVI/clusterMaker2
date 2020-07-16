package edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils;

import java.util.Map;

import org.cytoscape.jobs.AbstractCyJob;
import org.cytoscape.jobs.CyJobDataService;
import org.cytoscape.jobs.CyJobExecutionService;
import org.cytoscape.jobs.CyJobMonitor;

public class ClusterJob extends AbstractCyJob {

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
}
