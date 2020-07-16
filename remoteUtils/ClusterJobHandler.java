package edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobData;
import org.cytoscape.jobs.CyJobMonitor;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.model.CyNetwork;

import org.cytoscape.work.TaskMonitor;

public class ClusterJobHandler implements CyJobMonitor {
	static Map<CyJob, CyNetwork> networkMap = new HashMap<>();

	public ClusterJobHandler(CyJob job, CyNetwork network) {
		networkMap.put(job, network); 
	}

	@Override
	public void jobStatusChanged(CyJob job, CyJobStatus status) {
		System.out.println("Job status changed to "+status.toString());
	}

	@Override
	public void loadData(CyJob job, TaskMonitor monitor) {
		CyJobData data = job.getJobDataService().getDataInstance();
		CyJobStatus status = job.getJobExecutionService().fetchResults(job, data);

		// We need to save the job so the ClusterDataService can
		// restore the SUIDs
		data.put("job", job);
		// Now we need to extract the network from the data
		CyNetwork network = job.getJobDataService().getNetworkData(data, "network");
	}
}
