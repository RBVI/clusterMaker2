package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DBSCAN;

import java.util.Collections;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractAttributeClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.fft.FFTContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.fft.RunFFT;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.KnnView;

public class DBSCAN extends AbstractAttributeClusterer {

	public static String SHORTNAME = "dbscan";
	public static String NAME = "DBSCAN cluster";
	public static String GROUP_ATTRIBUTE = SHORTNAME;
	
	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public DBSCANContext context = null;
	
	public DBSCAN(DBSCANContext context, ClusterManager clusterManager) {
		super(clusterManager);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
	}

	public String getShortName() {return SHORTNAME;}

	@ProvidesTitle
	public String getName() {return NAME;}
	
	public ClusterViz getVisualizer() {
		return null;
	}

	
	public void run(TaskMonitor monitor){
		
		this.monitor = monitor;
		monitor.setTitle("Performing "+getName());
		List<String> nodeAttributeList = context.attributeList.getNodeAttributeList();
		String edgeAttribute = context.attributeList.getEdgeAttribute();
		
		if (nodeAttributeList == null && edgeAttribute == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Must select either one edge column or two or more node columns");
			return;
		}

		if (nodeAttributeList != null && nodeAttributeList.size() > 0 && edgeAttribute != null) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Can't have both node and edge columns selected");
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

		// To make debugging easier, sort the attribute list
		Collections.sort(nodeAttributeList);

		// Get our attributes we're going to use for the cluster
		String[] attributeArray;
		if (nodeAttributeList != null && nodeAttributeList.size() > 1) {
			attributeArray = new String[nodeAttributeList.size()];
			int i = 0;
			for (String attr: nodeAttributeList) { attributeArray[i++] = "node."+attr; }
		} else {
			attributeArray = new String[1];
			attributeArray[0] = "edge."+edgeAttribute;
		}

		monitor.setStatusMessage("Initializing");

		resetAttributes(network, GROUP_ATTRIBUTE);
		
		
		// Create a new clusterer
		RunDBSCAN algorithm = new RunDBSCAN(network, attributeArray, distanceMetric, monitor, context);
						
		String resultsString = "DBSCAN results:";

		// Cluster the attributes, if requested
		if (context.clusterAttributes && attributeArray.length > 1) {
			monitor.setStatusMessage("Clustering attributes");
			int[] clusters = algorithm.cluster(true);
			if (!algorithm.getMatrix().isTransposed())
				createGroups(algorithm.getMatrix(),algorithm.getNClusters(), clusters, "dbscan");
			
			Integer[] rowOrder = algorithm.getMatrix().indexSort(clusters, clusters.length);
			//Integer[] rowOrder = algorithm.cluster(context.kcluster.kNumber,1, true, "dbscan", context.kcluster);
			updateAttributes(network, GROUP_ATTRIBUTE, rowOrder, attributeArray, getAttributeList(), 
			                 algorithm.getMatrix());
		}

		// Cluster the nodes
		monitor.setStatusMessage("Clustering nodes");
		int[] clusters = algorithm.cluster(false);
		if (!algorithm.getMatrix().isTransposed())
			createGroups(algorithm.getMatrix(),algorithm.getNClusters(), clusters, "dbscan");
		
		Integer[] rowOrder = algorithm.getMatrix().indexSort(clusters, clusters.length);
		//Integer[] rowOrder = algorithm.cluster(context.kcluster.kNumber,1, false, "dbscan", context.kcluster);
		updateAttributes(network, GROUP_ATTRIBUTE, rowOrder, attributeArray, getAttributeList(), 
		                 algorithm.getMatrix());

		// System.out.println(resultsString);
		if (context.showUI) {
			insertTasksAfterCurrentTask(new KnnView(clusterManager));
		}
	}

	
}
