package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Fuzzifier;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

//Cytoscape imports
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableUtil;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractFuzzyNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;

import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.FuzzyNodeCluster;
//import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeWeightConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.SilhouetteCalculator;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.Silhouettes;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

/**
 * Fuzzifier creates fuzzy clusters from already existing clusters.
 * All the nodes are assigned membership values corresponding to the clusters, based on edge attributes
 * @author Abhiraj
 *
 */

public class Fuzzifier extends AbstractFuzzyNetworkClusterer{

	RunFuzzifier runFuzzifier = null;
	public static String SHORTNAME = "fuzzifier";
	public static String NAME = "Cluster Fuzzifier";

	public static final String NONEATTRIBUTE = ModelUtils.NONEATTRIBUTE;
	public final static String GROUP_ATTRIBUTE = "__FuzzyGroups.SUID";
	private boolean selectedOnly = false;
	private boolean ignoreMissing = true;
	CyTableFactory tableFactory = null;
	CyTableManager tableManager = null;
	private List<NodeCluster> Clusters = null;
	private int cNumber;
	private String[] attributeArray = new String[1];

	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public FuzzifierContext context = null;


	public Fuzzifier(FuzzifierContext context, ClusterManager manager) {
		super(manager);
		this.context = context;

		if (network == null){
			network = clusterManager.getNetwork();
			tableFactory = clusterManager.getTableFactory();
			tableManager = clusterManager.getTableManager();
		}
		this.Clusters = getClusters();
		this.cNumber = Clusters.size();
		context.setNetwork(network);
		super.network = network;
	}

	public String getShortName() {return SHORTNAME;};

	@ProvidesTitle
	public String getName() { return NAME; }

	/**
	 * The method run creates an instance of the RunFuzzifier and creates the fuzzy clusters 
	 * by calling the fuzzifier algorithm.
	 * Also creates fuzzy groups and the Fuzzy Cluster Table
	 * 
	 * @param Task Monitor
	 */
	public void run( TaskMonitor monitor) {
		monitor.setTitle("Performing Fuzzifier clustering");
		this.monitor = monitor;
		if (network == null)
			network = clusterManager.getNetwork();
		super.network = network;

		// Make sure to update the context
		context.setNetwork(network);

		Long networkID = network.getSUID();

		CyTable nodeAttributes = network.getDefaultNodeTable();

		CyMatrix distanceMatrix = context.edgeAttributeHandler.getMatrix();
		if (distanceMatrix == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Can't get distance matrix: no attribute value?");
			return;
		}

		// Update our tunable results
		clusterAttributeName = context.getClusterAttribute();

		runFuzzifier = new RunFuzzifier(Clusters, distanceMatrix, cNumber, 
		                                context.membershipThreshold.getValue(), context.maxThreads, monitor);

		runFuzzifier.setDebug(debug);

		if (canceled) return;

		monitor.showMessage(TaskMonitor.Level.INFO,"Clustering...");

		List<FuzzyNodeCluster> FuzzyClusters = runFuzzifier.run(network, monitor);
		if (FuzzyClusters == null) return; // Canceled?

		monitor.showMessage(TaskMonitor.Level.INFO,"Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(network, GROUP_ATTRIBUTE);

		monitor.showMessage(TaskMonitor.Level.INFO,"Creating groups");

		params = new ArrayList<String>();
		context.edgeAttributeHandler.setParams(params);

		List<List<CyNode>> nodeClusters = createFuzzyGroups(network, FuzzyClusters, GROUP_ATTRIBUTE);

		results = new AbstractClusterResults(network, nodeClusters);
		monitor.showMessage(TaskMonitor.Level.INFO, "Done.  Fuzzifier results:\n"+results);

		if (context.vizProperties.showUI) {
			monitor.showMessage(TaskMonitor.Level.INFO, 
		                      "Creating network");
			insertTasksAfterCurrentTask(new NewNetworkView(network, clusterManager, true,
			                                               context.vizProperties.restoreEdges));
		} else {
			monitor.showMessage(TaskMonitor.Level.INFO, "Done.  Fizzifier results:\n"+results);
		}

		System.out.println("Creating fuzzy table");
		createFuzzyTable(FuzzyClusters);
		System.out.println("Done");

	}

	public void cancel() {
		canceled = true;
		runFuzzifier.cancel();
	}

	/**
	 * The method creates a list of NodeCLusters from the cluster attributes of the network
	 * This serves as the input for the fuzzifeir algorithm
	 * @return A list of NodeClusters
	 */
	public List<NodeCluster> getClusters(){

		List<NodeCluster> nodeClusters = new ArrayList<NodeCluster>();
		HashMap<Integer,List<CyNode>> clusterMap = new HashMap<Integer,List<CyNode>>();
		List<CyNode> nodeList = network.getNodeList();
		clusterAttributeName = network.getRow(network).get("__clusterAttribute", String.class);

		// Save the seed cluster we used
		ModelUtils.createAndSetLocal(network, network, "__fuzzifierSeed", 
		                             clusterAttributeName, String.class, null);

		for(CyNode node : nodeList){
			// System.out.println("Node SUID:"+node.getSUID());
			if (ModelUtils.hasAttribute(network, node, clusterAttributeName)){

				Integer cluster = network.getRow(node).get(clusterAttributeName, Integer.class);
				// System.out.println("Cluster for "+node.getSUID()+"--"+cluster);
				if (!clusterMap.containsKey(cluster))
					clusterMap.put(cluster, new ArrayList<CyNode>());
				clusterMap.get(cluster).add(node);

			}
		}

		for (int key : clusterMap.keySet()){
			nodeClusters.add(new NodeCluster(clusterMap.get(key)));
		}
		// System.out.println("NodeCluster Size : " +nodeClusters.size());
		return nodeClusters;
	}

	@Override
	public void setUIHelper(TunableUIHelper helper) {context.setUIHelper(helper); }




}
