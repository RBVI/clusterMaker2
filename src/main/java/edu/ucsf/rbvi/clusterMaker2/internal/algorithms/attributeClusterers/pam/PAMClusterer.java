package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractAttributeClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.Silhouettes;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.KnnView;



/**
 * 
 * @author DavidS
 *
 */
public class PAMClusterer extends AbstractAttributeClusterer {
	public static String SHORTNAME="PAM Cluster";
	public static String NAME="Partition Around Medoids (PAM) cluster";

	@Tunable (description="Network to cluster", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public PAMContext context = null;

	private List<String> attributeOrder = null;
	private List<String> attributeList = null;
	private Silhouettes attributeSilhouette = null;
	private List<String> nodeOrder = null;
	private List<String> nodeList = null;
	private Silhouettes nodeSilhouette = null;

	public PAMClusterer(PAMContext context, ClusterManager clusterManager) {
		super(clusterManager);
		this.context = context;
		if (network == null)
			network=clusterManager.getNetwork();
		context.setNetwork(network);
	}


	@ProvidesTitle
	public String getShortName() {
		return SHORTNAME;
	}

	public String getName() {
		return NAME;
	}

	public ClusterViz getVisualizer() {
		return new KnnView(clusterManager);
	}

	@Override
	public void run(TaskMonitor monitor) {
		this.monitor = monitor;
		monitor.setTitle("Performing "+getName());
		List<String> nodeAttributeList = context.attributeList.getNodeAttributeList();
		String edgeAttribute = context.attributeList.getEdgeAttribute();

		if (cancelled()) return;


		// sanity check of parameters
		if (nodeAttributeList == null && edgeAttribute == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Must select either one edge column or two or more node columns");
			return;
		}

		if (nodeAttributeList != null && nodeAttributeList.size() > 0 && edgeAttribute != null) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Can't have both node and edge columns selected");
			return;
		}

		if (context.selectedOnly && CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true).size() < 3) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Must have at least three nodes to cluster");
			return;
		}

		createGroups = context.createGroups;

		if (nodeAttributeList != null && nodeAttributeList.size() > 0) {
			// To make debugging easier, sort the attribute list
			Collections.sort(nodeAttributeList);
		}

		// Get our attributes we're going to use for the cluster
		String[] attributeArray;
		if (nodeAttributeList != null && nodeAttributeList.size() > 0) {
			attributeArray = new String[nodeAttributeList.size()];
			int i = 0;
			for (String attr: nodeAttributeList) { attributeArray[i++] = "node."+attr; }
		} else {
			attributeArray = new String[1];
			attributeArray[0] = "edge."+edgeAttribute;
		}
		monitor.setStatusMessage("Performing PAM cluster with k = " + 
		                         context.kcluster.kNumber + " using " + 
				                     distanceMetric + " and attributes: " + attributeArray);

		monitor.setStatusMessage("Initializing");
		// System.out.println("Initializing");

		resetAttributes(network, SHORTNAME);

		RunPAM algo = new RunPAM(network, attributeArray, distanceMetric, monitor, context, this);

		String resultsString = "PAM results:";

		int nIterations = 0;

		// Cluster the attributes
		if (context.clusterAttributes && attributeArray.length > 1) {
			if (cancelled()) {
				return;
			}

			monitor.setStatusMessage("Clustering attributes");
			Integer[] rowOrder = algo.cluster(clusterManager, context.kcluster.kNumber,  
				                                nIterations,  true, getShortName(), context.kcluster, false);

			attributeList = algo.getAttributeList();
			updateAttributes(network, SHORTNAME, rowOrder, attributeArray, attributeList,
			                 algo.getMatrix());
			attributeSilhouette = algo.getSilhouettes();
			attributeOrder = getOrder(rowOrder, algo.getMatrix());
		}

		if (cancelled()) {
			return;
		}

		// Cluster the nodes
		monitor.setStatusMessage("Clustering nodes");

		Integer[] rowOrder = algo.cluster(clusterManager, context.kcluster.kNumber, 
		                                  nIterations, false, getShortName(),
		                                  context.kcluster, createGroups);

		nodeList = algo.getAttributeList();
		updateAttributes(network, SHORTNAME, rowOrder, attributeArray, algo.getAttributeList(),
		                 algo.getMatrix());
		nodeSilhouette = algo.getSilhouettes();
		nodeOrder = getOrder(rowOrder, algo.getMatrix());

		if (context.showUI)
			insertTasksAfterCurrentTask(new KnnView(clusterManager));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> R getResults(Class<? extends R> requestedType) {
		if (requestedType.equals(Map.class))
			return (R)getMapResults(nodeOrder, nodeList, nodeSilhouette, attributeOrder, attributeList, attributeSilhouette);
		else if (requestedType.equals(String.class)) 
			return (R)getStringResults(nodeOrder, nodeList, nodeSilhouette, attributeOrder, attributeList, attributeSilhouette);
		else if (requestedType.equals(JSONResult.class)) 
			return (R)getJSONResults(nodeOrder, nodeList, nodeSilhouette, attributeOrder, attributeList, attributeSilhouette);
		return (R)"";
	}

}
