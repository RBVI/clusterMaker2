package edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils;

import java.util.Map;

import org.cytoscape.jobs.CyJob;
import org.cytoscape.jobs.CyJobData;
import org.cytoscape.jobs.CyJobStatus;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TaskMonitor;
import org.json.simple.JSONArray;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.ScatterPlotDialog;

public class DimensionalityReductionJobHandler extends ClusterJobHandler {
	public boolean showScatterPlot = false;

	public DimensionalityReductionJobHandler(CyJob job, CyNetwork network, Boolean showScatterPlot) {
		super(job, network);
		this.showScatterPlot = showScatterPlot;
	}
	
	@Override
	public void loadData(CyJob job, TaskMonitor monitor) {
		CyJobData data = job.getJobDataService().getDataInstance();
		CyJobStatus status = job.getJobExecutionService().fetchResults(job, data);
		data.put("job", job);
		//CyNetwork network = job.getJobDataService().getNetworkData(data, "network");
		CyNetwork network = networkMap.get(job);
		System.out.println("network: " + network);
		
			// arranging the dimensionality reduction data into coordinates[] and nodes[] columns
		CyTable nodeTable = network.getDefaultNodeTable();
		JSONArray embedding = (JSONArray) data.get("embedding"); //getting the relevant data from the data object
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

		String newmapX = job.getJobName() + "_x";
		String newmapY = job.getJobName() + "_y";
			
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

			
		Map<String, Object> clusterData = ((ClusterJob) job).getClusterData().getAllValues();
		if (showScatterPlot) {
			ClusterManager manager = (ClusterManager) clusterData.get("manager");
			ScatterPlotDialog scatter = new ScatterPlotDialog(manager, job.getJobName(), null, nodes, coordinates);
		}
	}
	
}
