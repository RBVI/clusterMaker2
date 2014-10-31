package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.TunableUIHelper;



public class TransClustCluster extends AbstractNetworkClusterer{
	private List<CyNode> nodes;
	public static String SHORTNAME = "transclust";
	public static String NAME = "Transitivity Clustering";
	public final static String GROUP_ATTRIBUTE = "__TransClustGroups.SUID";
	
	private static final long serialVersionUID = 1L;

	private RunTransClust runTransClust;
	
	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public TransClusterContext context = null;
	

	/**
 	 * Main constructor -- calls constructor of the superclass and initializes
 	 * all of our tunables.
 	 */
	public TransClustCluster(TransClusterContext context, ClusterManager manager) {
		super(manager);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
	}

	public String getShortName() {return SHORTNAME;};

	@ProvidesTitle
	public String getName() {return NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	/**
 	 * Update all of our tunables
 	 */
	public void updateSettings() {
		// Advanced Settings

		// Find Exact Solution
		try {
			TaskConfig.fixedParameterMax = new Integer(context.maxSubclusterSize);
		} catch (Exception e) {
			TaskConfig.fixedParameterMax = 20;
		}
		
		try {
			TaskConfig.fpMaxTimeMillis = new Integer(context.maxTime)*1000;
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if(!context.mergeSimilar){
			TaskConfig.upperBound = Float.MAX_VALUE;
		}

		try {
			if(context.mergeSimilar){
				TaskConfig.upperBound = new Float(context.mergeThreshold);
			}else{
				TaskConfig.upperBound = Float.MAX_VALUE;
			}
		} catch (Exception e) {
			TaskConfig.upperBound = Float.MAX_VALUE;
		}
		
		TaskConfig.maxNoThreads = context.processors;
		
	}

	/**
 	 * Perform the actual clustering.  For TransClust, there are really
 	 * two steps:
 	 * 	1) Assign all of the connected components
 	 * 	2) Do the TransClust clustering.
 	 *
 	 * There is also an optional approach called evolutionary parameter
 	 * tuning, which takes a lot longer and is probably less relevant for
 	 * the Cytoscape integration.
 	 *
 	 * @param monitor the TaskMonitor to use
 	 */
	public void run(TaskMonitor monitor) {
		monitor.setTitle("Performing Transitivity clustering");
		this.monitor = monitor;
		if (network == null)
			network = clusterManager.getNetwork();

		// Make sure to update the context
		context.setNetwork(network);
		
		DistanceMatrix matrix = context.edgeAttributeHandler.getMatrix();
		if (matrix == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Can't get distance matrix: no attribute value?");
			return;
		}
		
		updateSettings();
		runTransClust = new RunTransClust(matrix,context.edgeAttributeHandler.edgeCutOff.getValue(),monitor);

		if (canceled) return;

		monitor.showMessage(TaskMonitor.Level.INFO,"Clustering...");
		createGroups = context.advancedAttributes.createGroups;

		//Cluster the nodes

		List<NodeCluster> clusters = runTransClust.run(monitor, network);
		if (clusters == null) return; // Canceled?

		monitor.showMessage(TaskMonitor.Level.INFO,"Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(network, GROUP_ATTRIBUTE);

		monitor.showMessage(TaskMonitor.Level.INFO,"Creating groups");

		params = new ArrayList<String>();
		context.edgeAttributeHandler.setParams(params);

		List<List<CyNode>> nodeClusters = createGroups(network, clusters, GROUP_ATTRIBUTE);

		results = new AbstractClusterResults(network, nodeClusters);

		monitor.setStatusMessage("Done.  TransClust results:\n"+results);

		if (context.vizProperties.showUI) {
			monitor.showMessage(TaskMonitor.Level.INFO, 
		                      "Creating network");
			insertTasksAfterCurrentTask(new NewNetworkView(network, clusterManager, true,
			                                               context.vizProperties.restoreEdges));
		}
	}

	public void cancel() {
		canceled = true;
		runTransClust.cancel();
	}
	
	public void setParams(List<String>params) {
		params.add("mergeSimilar="+context.mergeSimilar);
		params.add("mergeThreshold="+context.mergeThreshold);
		params.add("maxSubclusterSize="+context.maxSubclusterSize);
		params.add("maxTime="+context.maxTime);
	}

	@Override
	public void setUIHelper(TunableUIHelper helper) {context.setUIHelper(helper); }
}
