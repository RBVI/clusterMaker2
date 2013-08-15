package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL;

import java.util.ArrayList;
import java.util.List;

//Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TaskMonitor;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;

public class MCLCluster extends AbstractNetworkClusterer   {
	RunMCL runMCL;
	public static String SHORTNAME = "mcl";
	public static String NAME = "MCL Cluster";

	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public MCLContext context = null;
	
	public MCLCluster(MCLContext context, ClusterManager manager) {
		super(manager);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
	}

	public String getShortName() { return SHORTNAME; }

	@ProvidesTitle
	public String getName() { return NAME; }
	
	public void run(TaskMonitor monitor) {
		monitor.setTitle("Performing MCL cluster");
		this.monitor = monitor;
		if (network == null)
			network = clusterManager.getNetwork();
		
		DistanceMatrix matrix = context.edgeAttributeHandler.getMatrix();
		if (matrix == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Can't get distance matrix: no attribute value?");
			return;
		}

		// Update our tunable results
		clusterAttributeName = context.getClusterAttribute();

		if (canceled) return;

		//Cluster the nodes
		runMCL = new RunMCL(matrix, context.inflation_parameter, context.iterations, 
		                    context.clusteringThresh, context.maxResidual, context.maxThreads, monitor);

		runMCL.setDebug(debug);

		if (canceled) return;

		monitor.showMessage(TaskMonitor.Level.INFO,"Clustering...");

		// results = runMCL.run(monitor);
		List<NodeCluster> clusters = runMCL.run(network, monitor);
		if (clusters == null) return; // Canceled?

		monitor.showMessage(TaskMonitor.Level.INFO,"Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(network);

		monitor.showMessage(TaskMonitor.Level.INFO,"Creating groups");

		params = new ArrayList<String>();
		context.edgeAttributeHandler.setParams(params);

		List<List<CyNode>> nodeClusters = createGroups(network, clusters);

		results = new AbstractClusterResults(network, nodeClusters);
		monitor.showMessage(TaskMonitor.Level.INFO, "Done.  MCL results:\n"+results);
	}

	public void cancel() {
		canceled = true;
		runMCL.cancel();
	}
}
	
