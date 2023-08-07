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
	private List<NodeCluster> clusters = null;
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
		context.setNetwork(network);
    context.edgeAttributeHandler.adjustLoops = false;

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

		// Update our tunable results
		clusterAttributeName = context.getClusterAttribute();

    NodeCluster.reset();
		this.clusters = getClusters();
		this.cNumber = clusters.size();

		Long networkID = network.getSUID();

		CyTable nodeAttributes = network.getDefaultNodeTable();

		CyMatrix distanceMatrix = context.edgeAttributeHandler.getMatrix();
		if (distanceMatrix == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Can't get distance matrix: no attribute value?");
			return;
		}

		runFuzzifier = new RunFuzzifier(clusters, distanceMatrix, cNumber, 
		                                context.membershipThreshold.getValue(), 0, monitor);

		runFuzzifier.setDebug(debug);

		if (canceled) return;

		monitor.showMessage(TaskMonitor.Level.INFO,"Clustering...");

    FuzzyNodeCluster.reset();
		List<FuzzyNodeCluster> fuzzyClusters = runFuzzifier.run(network, context, monitor);
		if (fuzzyClusters == null) return; // Canceled?

		monitor.showMessage(TaskMonitor.Level.INFO,"Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(network, GROUP_ATTRIBUTE);

		monitor.showMessage(TaskMonitor.Level.INFO,"Creating groups");

		params = new ArrayList<String>();
		context.edgeAttributeHandler.setParams(params);

		List<List<CyNode>> nodeClusters = createFuzzyGroups(network, fuzzyClusters, GROUP_ATTRIBUTE);

		results = new AbstractClusterResults(network, fuzzyClusters);
		monitor.showMessage(TaskMonitor.Level.INFO, "Done.  Fuzzifier results:\n"+results);

		if (context.vizProperties.showUI) {
			monitor.showMessage(TaskMonitor.Level.INFO, 
		                      "Creating network");
			insertTasksAfterCurrentTask(new NewNetworkView(network, clusterManager, true,
			                                               context.vizProperties.restoreEdges,
																										 !context.edgeAttributeHandler.selectedOnly));
		} else {
			monitor.showMessage(TaskMonitor.Level.INFO, "Done.  Fuzzifier results:\n"+results);
		}

		// System.out.println("Creating fuzzy table");
		createFuzzyTable(fuzzyClusters);
		// System.out.println("Done");

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
		if (network == null) return nodeClusters;

		List<CyNode> nodeList = network.getNodeList();
		String clusterName = network.getRow(network).get("__clusterAttribute", String.class);

    // Special case.  If the clusterAttribute is __fuzzifierCluster, then we're repeating this,
    // so use the __fuzzifierSeed instead
    if (clusterName.equals(clusterAttributeName)) {
      // We're reclustering
      clusterName = network.getRow(network).get("__fuzzifierSeed", String.class);

      // We need to do some resetting
      if (ModelUtils.hasColumn(network, network.getDefaultNetworkTable(), "__fuzzifierCluster_Table.SUID")) {
        Long fuzzyTableSUID = network.getRow(network).get("__fuzzifierCluster_Table.SUID", Long.class);
        if (fuzzyTableSUID != null) {
          tableManager.deleteTable(fuzzyTableSUID);
          ModelUtils.deleteColumnLocal(network, CyNetwork.class, "__fuzzifierCluster_Table.SUID");
        }
      }
    } else {
      // Save the seed cluster we used
      ModelUtils.createAndSetLocal(network, network, "__fuzzifierSeed", 
                                   clusterName, String.class, null);
    }

    if (!ModelUtils.hasColumn(network, network.getDefaultNodeTable(), clusterName))
      throw new RuntimeException("Can't find cluster attribute: "+clusterName);

		for(CyNode node : nodeList){
			// System.out.println("Node SUID:"+node.getSUID());
			if (ModelUtils.hasAttribute(network, node, clusterName)){

				Integer cluster = network.getRow(node).get(clusterName, Integer.class);
				// System.out.println("Cluster for "+node.getSUID()+"--"+cluster);
				if (!clusterMap.containsKey(cluster))
					clusterMap.put(cluster, new ArrayList<CyNode>());
				clusterMap.get(cluster).add(node);

			}
		}

		for (int key : clusterMap.keySet()){
      if (clusterMap.get(key).size() > context.minClusterSize) {
        NodeCluster nodeCluster = new NodeCluster(key, clusterMap.get(key));
        nodeClusters.add(nodeCluster);
      }
		}
		// System.out.println("NodeCluster Size : " +nodeClusters.size());
		return nodeClusters;
	}

	@Override
	public void setUIHelper(TunableUIHelper helper) {context.setUIHelper(helper); }




}
