package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractAttributeClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.SplitCost;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam.PAMContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam.RunPAM;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.SummaryMethod;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.KnnView;

public class HopachPAMClusterer extends AbstractAttributeClusterer {
	public static final String SHORTNAME="hopach";
	public static final String NAME="HOPACH-PAM cluster";
	
	
	@Tunable (description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public HopachPAMContext context = null;
	
	public HopachPAMClusterer(HopachPAMContext context, ClusterManager clusterManager) {
		super(clusterManager);
		this.context = context;
		if (network == null)
			network=clusterManager.getNetwork();
		context.setNetwork(network);
	}

	public String getShortName() {
		return SHORTNAME;
	}

	@ProvidesTitle
	public String getName() {
		return NAME;
	}

	@Override
	public void run(TaskMonitor monitor) {
		this.monitor = monitor;
		monitor.setTitle("Performing "+getName());
		List<String> nodeAttributeList = context.attributeList.getNodeAttributeList();
		
		
		// sanity check of parameters
		if (nodeAttributeList == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Must select either one edge column or two or more node columns");
			return;
		}

		if (nodeAttributeList != null && nodeAttributeList.size() < 2) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Must have at least two node columns for cluster weighting");
			return;
		}

		if (context.selectedOnly && CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true).size() < 3) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Must have at least three nodes to cluster");
			return;
		}

		Collections.sort(nodeAttributeList);
		
		// Get our attributes we're going to use for the cluster
		String[] attributeArray;
		attributeArray = new String[nodeAttributeList.size()];
		int i = 0;
		for (String attr: nodeAttributeList) { attributeArray[i++] = "node."+attr; }


		monitor.setStatusMessage("Initializing");
		// System.out.println("Initializing");

		resetAttributes(network, SHORTNAME);

		RunHopachPAM algo = 
			new RunHopachPAM(network, attributeArray, distanceMetric, monitor, context);
		algo.setParameters(context.splitCost.getSelectedValue(), 
					       context.summaryMethod.getSelectedValue(), 
					       context.maxLevel, context.K, context.L, 
					       context.forceInitSplit, context.minCostReduction);

	/*	
		algo.setCreateGroups(createGroups);
		algo.setIgnoreMissing(true);
		algo.setSelectedOnly(selectedOnly);
		algo.setDebug(debug);
		algo.setUseSilhouette(useSilhouette);
		algo.setClusterInterface(this);
		algo.setParameters(splitCost, summaryMethod, maxLevel, K, L, forceInitSplit, minCostReduction);
	*/	
		String resultsString = getName() + " results:";
		
		int nIterations = 0;
		
		// Cluster the attributes
		if (context.clusterAttributes && attributeArray.length > 1) {
			monitor.setStatusMessage("Clustering attributes");
			Integer[] rowOrder = algo.cluster(0,  nIterations,  true, getShortName(), context.kcontext);
			updateAttributes(network, SHORTNAME, rowOrder, attributeArray, algo.getAttributeList(),
					algo.getMatrix());

		}
		
		// Cluster the nodes
		monitor.setStatusMessage("Clustering nodes");
		Integer[] rowOrder = algo.cluster(0, nIterations, false, getShortName(), context.kcontext);
		updateAttributes(network, SHORTNAME, rowOrder, attributeArray, algo.getAttributeList(),
				algo.getMatrix());

		if (context.showUI)
			insertTasksAfterCurrentTask(new KnnView(clusterManager));

		
	}
}
