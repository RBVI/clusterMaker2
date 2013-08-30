package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM;

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
 * The FCMCluster class is for implementing the Fuzzy C-Means algorithm for clustering. 
 * The fuzzy clusters are created based on the edge properties and each node gets assigned a 
 * degree of membership to each cluster.
 */

public class FCMCluster extends AbstractNetworkClusterer {
	
	RunFCM runFCM = null;
	public static String SHORTNAME = "fcml";
	public static String NAME = "Fuzzy C-Means Cluster";
	public final static String GROUP_ATTRIBUTE = "__FCMGroups.SUID";
	
	public static final String NONEATTRIBUTE = "--None--";
	protected Matrix dataMatrix;
	private boolean selectedOnly = false;
	private boolean ignoreMissing = true;
	CyTableFactory tableFactory = null;
	CyTableManager tableManager = null;
	private Silhouettes[] silhouetteResults = null;
	
	private String[] attributeArray = new String[1];
	
	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public FCMContext context = null;
	
	public FCMCluster(FCMContext context, ClusterManager manager) {
		super(manager);
		this.context = context;
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
		monitor.setTitle("Performing FCM cluster");
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

		createGroups = context.advancedAttributes.createGroups;
		/*
		// Update our tunable results
		clusterAttributeName = context.getClusterAttribute();
		
		if (canceled) return;
		
		// Getting the attribute array to make the data matrix context.getAttributeList()
		List <String> dataAttributes = context.attributeList.getSelectedValues();
		String[] attributeArray = new String[dataAttributes.size()];
		for (int i = 0; i < dataAttributes.size(); i++){
			
			attributeArray[i] = dataAttributes.get(i);
		}
		
		Arrays.sort(attributeArray);
		
		// Create the matrix of data with the attributes for calculating distances
		dataMatrix = new Matrix(network, attributeArray, true, ignoreMissing, selectedOnly);
		dataMatrix.setUniformWeights();
		//Cluster the nodes
		*/
		context.cNumber = cEstimate();
		DistanceMetric distMetric = context.distanceMetric.getSelectedValue();
		runFCM = new RunFCM(distanceMatrix, context.iterations, context.cNumber, distMetric, 
									context.fIndex, context.beta, context.membershipThreshold.getValue(), context.maxThreads, monitor);

		
		//RunFCM (Matrix data,DistanceMatrix dMat, int num_iterations, int cClusters,DistanceMetric metric, double findex, double beta, int maxThreads, Logger logger)
		runFCM.setDebug(debug);

		if (canceled) return;
		
		monitor.showMessage(TaskMonitor.Level.INFO,"Clustering...");

		// results = runMCL.run(monitor);
		
		List<FuzzyNodeCluster> clusters = runFCM.run(network, monitor);
		if (clusters == null) return; // Canceled?
		
		monitor.showMessage(TaskMonitor.Level.INFO,"Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(network, GROUP_ATTRIBUTE);
		
		monitor.showMessage(TaskMonitor.Level.INFO,"Creating groups");

		params = new ArrayList<String>();
		context.edgeAttributeHandler.setParams(params);

		List<List<CyNode>> nodeClusters = createFuzzyGroups(network, clusters, GROUP_ATTRIBUTE);

		results = new AbstractClusterResults(network, nodeClusters);
		monitor.showMessage(TaskMonitor.Level.INFO, "Done.  FCM results:\n"+results);
		
		createFuzzyTable(clusters, nodeAttributes, dataMatrix, runFCM.clusterMemberships);
		
	}
	
	public void cancel() {
		canceled = true;
		runFCM.cancel();
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
			
			network.getDefaultNetworkTable().createColumn("FuzzyClusterTable.SUID", long.class, false);
			network.getRow(network).set("FuzzyClusterTable.SUID", FuzzyClusterTable.getSUID());
			tableManager.addTable(FuzzyClusterTable);			
			
		}
		
		private int cEstimate(){
			int nClusters = -1;
			TaskMonitor saveMonitor = monitor;
			monitor = null;
			silhouetteResults = new Silhouettes[context.cMax];

			int nThreads = Runtime.getRuntime().availableProcessors()-1;
			if (nThreads > 1)
				runThreadedSilhouette(context.cMax, context.iterations, nThreads, saveMonitor);
			else
				runLinearSilhouette(context.cMax, context.iterations, saveMonitor);

			// Now get the results and find our best k
			double maxSil = Double.MIN_VALUE;
			for (int cEstimate = 2; cEstimate < context.cMax; cEstimate++) {
				double sil = silhouetteResults[cEstimate].getMean();
				// System.out.println("Average silhouette for "+kEstimate+" clusters is "+sil);
				if (sil > maxSil) {
					maxSil = sil;
					nClusters = cEstimate;
				}
			}
			
			return nClusters;
		}
		
		private void runThreadedSilhouette(int kMax, int nIterations, int nThreads, TaskMonitor saveMonitor) {
			// Set up the thread pools
			ExecutorService[] threadPools = new ExecutorService[nThreads];
			for (int pool = 0; pool < threadPools.length; pool++)
				threadPools[pool] = Executors.newFixedThreadPool(1);

			// Dispatch a kmeans calculation to each pool
			for (int kEstimate = 2; kEstimate < kMax; kEstimate++) {
				int[] clusters = new int[dataMatrix.nRows()];
				Runnable r = new RunCMeans(dataMatrix, clusters, kEstimate, nIterations, saveMonitor);
				threadPools[(kEstimate-2)%nThreads].submit(r);
				// threadPools[0].submit(r);
			}

			// OK, now wait for each thread to complete
			for (int pool = 0; pool < threadPools.length; pool++) {
				threadPools[pool].shutdown();
				try {
					boolean result = threadPools[pool].awaitTermination(7, TimeUnit.DAYS);
				} catch (Exception e) {}
			}
		}

		private void runLinearSilhouette(int kMax, int nIterations, TaskMonitor saveMonitor) {
			for (int kEstimate = 2; kEstimate < kMax; kEstimate++) {
				int[] clusters = new int[dataMatrix.nRows()];
				if (cancelled()) return;
				if (saveMonitor != null) saveMonitor.setStatusMessage("Getting silhouette with a k estimate of "+kEstimate);
				//int ifound = kcluster(kEstimate, nIterations, dataMatrix, metric, clusters);
				silhouetteResults[kEstimate] = SilhouetteCalculator.calculate(dataMatrix, context.distanceMetric.getSelectedValue(), clusters);
			}
		}
		
		private class RunCMeans implements Runnable {
			Matrix matrix;
			int[] clusters;
			int cEstimate;
			int nIterations;
			TaskMonitor saveMonitor = null;

			public RunCMeans (Matrix matrix, int[] clusters, int c, int nIterations, TaskMonitor saveMonitor) {
				this.matrix = matrix;
				this.clusters = clusters;
				this.cEstimate = c;
				this.nIterations = nIterations;
				this.saveMonitor = saveMonitor;
			}

			public void run() {
				int[] clusters = new int[matrix.nRows()];
				if (cancelled()) return;
				if (saveMonitor != null) saveMonitor.setStatusMessage("Getting silhouette with a c estimate of "+cEstimate);
				//int ifound = kcluster(kEstimate, nIterations, matrix, metric, clusters);
				try {
					silhouetteResults[cEstimate] = SilhouetteCalculator.calculate(matrix, context.distanceMetric.getSelectedValue(), clusters);
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
}
