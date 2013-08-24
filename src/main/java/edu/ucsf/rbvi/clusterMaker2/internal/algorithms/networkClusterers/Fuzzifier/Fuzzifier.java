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

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM.FCMContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM.RunFCM;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.RunMCL;

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
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeWeightConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.SilhouetteCalculator;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.Silhouettes;

/**
 * Fuzzifier creates fuzzy clusters from already existing clusters.
 * All the nodes are assigned membership values corresponding to the clusters, based on edge attributes
 * @author Abhiraj
 *
 */

public class Fuzzifier extends AbstractNetworkClusterer{
	
	RunFuzzifier runFuzzifier = null;
	public static String SHORTNAME = "fuzzifier";
	public static String NAME = "Fuzzifier Cluster";
	
	public static final String NONEATTRIBUTE = "--None--";
	public final static String GROUP_ATTRIBUTE = "__FuzzyGroups.SUID";
	protected Matrix dataMatrix;
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
		this(context, manager, null);
	}

	public Fuzzifier(FuzzifierContext context, ClusterManager manager, List<NodeCluster> Custers) {
		super(manager);
		this.context = context;
		this.Clusters = Clusters;
		this.cNumber = Clusters.size();
		if (network == null){
			network = clusterManager.getNetwork();
			tableFactory = clusterManager.getTableFactory();
			tableManager = clusterManager.getTableManager();
		}	
		context.setNetwork(network);
	}
	
	public String getShortName() {return SHORTNAME;};
	
	@ProvidesTitle
	public String getName() { return NAME; }
	
	public void run( TaskMonitor monitor) {
		monitor.setTitle("Performing Fuzzifier clustering");
		this.monitor = monitor;
		if (network == null)
			network = clusterManager.getNetwork();
		
		Long networkID = network.getSUID();

		CyTable netAttributes = network.getDefaultNetworkTable();
		CyTable nodeAttributes = network.getDefaultNodeTable();
		CyTable edgeAttributes = network.getDefaultEdgeTable();
		
		DistanceMatrix distanceMatrix = context.edgeAttributeHandler.getMatrix();
		if (distanceMatrix == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Can't get distance matrix: no attribute value?");
			return;
		}
				
		DistanceMetric distMetric = context.distanceMetric.getSelectedValue();
		runFuzzifier = new RunFuzzifier(Clusters, distanceMatrix,cNumber, distMetric, 
									context.clusteringThresh, context.maxThreads, monitor);

		
		//RunFCM (Matrix data,DistanceMatrix dMat, int num_iterations, int cClusters,DistanceMetric metric, double findex, double beta, int maxThreads, Logger logger)
		runFuzzifier.setDebug(debug);

		if (canceled) return;
		
		monitor.showMessage(TaskMonitor.Level.INFO,"Clustering...");

		// results = runMCL.run(monitor);
		
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
		
		createFuzzyTable(FuzzyClusters, nodeAttributes, dataMatrix, runFuzzifier.clusterMemberships);
		
	}	
	
	public void cancel() {
		canceled = true;
		runFuzzifier.cancel();
	}

	@Override
	public void setUIHelper(TunableUIHelper helper) {context.setUIHelper(helper); }

		
		/**
		 * This method adds the membership value array of each CyNode to the node attributes table 
		 * Method also creates a new table- FuzzyClusterTable which stores all the FuzzyNodeClusters and 
		 * the corresponding membership values of the nodes in the network
		 * 
		 * @param clusters the list of FuzzyNodeCluster for the current network
		 * @param nodeAttributes :Attribute Table for nodes
		 * @param data : data matrix for the current set of nodes
		 * @param clusterMemberships : 2D array of membership values
		 */
		
		private void createFuzzyTable(List<FuzzyNodeCluster> clusters, CyTable nodeAttributes, Matrix data, double[][] clusterMemberships){
			/*
			CyNode node; 
			for(int i = 0; i < data.nRows(); i++ ){
				node = data.getRowNode(i);
				nodeAttributes.getRow(node).set(clusterAttributeName + "_MembershipValues", clusterMemberships[i]);
			}
			*/	
			CyTable FuzzyClusterTable = tableFactory.createTable("Fuzzy_Cluster_Table", "FuzzyCluster", CyNode.class, true, true);
			FuzzyClusterTable.createColumn("Fuzzy_Node.SUID", CyNode.class, false);
			
			for(FuzzyNodeCluster cluster : clusters){
				
				FuzzyClusterTable.createColumn("Cluster_"+cluster.getClusterNumber(), double.class, false);
			}
			
			CyRow TableRow;
			for(CyNode node: network.getNodeList()){
				TableRow = FuzzyClusterTable.getRow(node);
				for(FuzzyNodeCluster cluster : clusters){
					TableRow.set("Cluster_"+cluster.getClusterNumber(), cluster.getMembership(node));
				}
			}
			
			tableManager.addTable(FuzzyClusterTable);			
			
			
		}
		
	
	

}
