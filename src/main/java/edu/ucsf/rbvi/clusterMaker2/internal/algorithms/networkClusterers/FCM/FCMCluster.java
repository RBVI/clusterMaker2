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
import org.cytoscape.model.CyTableUtil;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractFuzzyNetworkClusterer;
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
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.SilhouetteCalculator;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.Silhouettes;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;


/**
 * The FCMCluster class is for implementing the Fuzzy C-Means algorithm for clustering. 
 * The fuzzy clusters are created based on the edge properties and each node gets assigned a 
 * degree of membership to each cluster.
 */

public class FCMCluster extends AbstractFuzzyNetworkClusterer {
	
	RunFCM runFCM = null;
	public static String SHORTNAME = "fcml";
	public static String NAME = "Fuzzy C-Means Cluster";
	public final static String GROUP_ATTRIBUTE = "__FCMGroups.SUID";
	
	public static final String NONEATTRIBUTE = "--None--";
	protected Matrix distanceDataMatrix;
	private boolean selectedOnly = false;
	private boolean ignoreMissing = true;
	private CyTableFactory tableFactory = null;
	private CyTableManager tableManager = null;
	private DistanceMatrix distanceMatrix = null;
	private TaskMonitor monitor = null;
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
	
	/**
	 * The method run creates an instance of the RunFCM and creates the fuzzy clusters by applying the fuzzy c means algorithm.
	 * Also creates fuzzy groups and the Fuzzy Cluster Table
	 * 
	 * @param Task Monitor
	 */
	public void run( TaskMonitor taskmonitor) {
		this.monitor = taskmonitor;
		monitor.setTitle("Performing FCM cluster");
		
		if (network == null)
			network = clusterManager.getNetwork();
		
		Long networkID = network.getSUID();

		CyTable netAttributes = network.getDefaultNetworkTable();
		CyTable nodeAttributes = network.getDefaultNodeTable();
		CyTable edgeAttributes = network.getDefaultEdgeTable();
		
		distanceMatrix = context.edgeAttributeHandler.getMatrix();
		if (distanceMatrix == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Can't get distance matrix: no attribute value?");
			return;
		}

		createGroups = context.advancedAttributes.createGroups;
		
		// Update our tunable results
		clusterAttributeName = "CnC_Bicluster";
		
		distanceDataMatrix = new Matrix(network,0,0);
		distanceDataMatrix.buildDistanceMatrix(distanceMatrix);
		
		// System.out.println("distanceMatrix (0,1) and (1,3) : "+ distanceMatrix.getEdgeValueFromMatrix(0, 1)+", " + distanceMatrix.getEdgeValueFromMatrix(1, 3));
		// System.out.println("distanceDataMatrix (0,1) and (1,3) : "+distanceDataMatrix.getValue(0, 1) +", " + distanceDataMatrix.getValue(1, 3));
		
		if(context.estimateClusterNumber && context.cNumber < 0){		
			int cEstimate = cEstimate();
			// System.out.println("Estimated number of Clusters: "+ cEstimate);
			context.cNumber = cEstimate;
		}
		
		int[] mostRelevantCluster = new int[network.getNodeList().size()];
		
		FuzzyNodeCluster.fuzzyClusterCount = 0;
		runFCM = new RunFCM(distanceMatrix, context.iterations, context.cNumber, 
									context.fIndex, context.beta, context.membershipThreshold.getValue(), context.maxThreads, this.monitor);
		
		runFCM.setDebug(debug);

		if (canceled) return;
		
		monitor.showMessage(TaskMonitor.Level.INFO,"Clustering...");
		
		List<FuzzyNodeCluster> clusters = runFCM.run(network, monitor,mostRelevantCluster);
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
		
		if (context.vizProperties.showUI) {
			monitor.showMessage(TaskMonitor.Level.INFO, 
		                      "Creating network");
			insertTasksAfterCurrentTask(new NewNetworkView(network, clusterManager, true,
			                                               context.vizProperties.restoreEdges));
		} else {
			monitor.showMessage(TaskMonitor.Level.INFO, "Done.  FCM results:\n"+results);
		}
		
		createFuzzyTable(clusters);		
	}
	
	public void cancel() {
		canceled = true;
		runFCM.cancel();
	}

	@Override
	public void setUIHelper(TunableUIHelper helper) {context.setUIHelper(helper); }
		
		/**
		 * Method calculates an estimated value for the number of clusters, based on Silhouette code
		 * 
		 * @return nClusters The estimated value of number of clusters
		 */
		private int cEstimate(){
			int nClusters = -1;
			TaskMonitor saveMonitor = monitor;
			silhouetteResults = new Silhouettes[context.cMax];

			int nThreads = Runtime.getRuntime().availableProcessors()-1;
			if (nThreads > 1)
				runThreadedSilhouette(context.cMax, context.iterations, nThreads, saveMonitor);
			else
				runLinearSilhouette(context.cMax, context.iterations, saveMonitor);

			// Now get the results and find our best k
			double maxSil = -Double.MAX_VALUE;
			for (int cEstimate = 2; cEstimate < context.cMax; cEstimate++) {
				double sil = silhouetteResults[cEstimate].getMean();
				//System.out.println("Average silhouette for "+cEstimate+" clusters is "+sil);
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
				int[] clusters = new int[distanceDataMatrix.nRows()];
				Runnable r = new RunCMeans(distanceDataMatrix, clusters, kEstimate, nIterations, saveMonitor);
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

		private void runLinearSilhouette(int cMax, int nIterations, TaskMonitor saveMonitor) {
			for (int cEstimate = 2; cEstimate < cMax; cEstimate++) {
				int[] clusters = new int[distanceDataMatrix.nRows()];
				if (cancelled()) return;
				if (saveMonitor != null) saveMonitor.setStatusMessage("Getting silhouette with a c estimate of "+cEstimate);
				//int ifound = kcluster(kEstimate, nIterations, dataMatrix, metric, clusters);
				// System.out.println("for cEstimate: "+cEstimate +", iterations= "+context.iterations+", Monitor: "+ saveMonitor.toString() );
				RunFCM silRunFCM = new RunFCM(distanceMatrix, context.iterations,cEstimate, 
						context.fIndex, context.beta, context.membershipThreshold.getValue(), context.maxThreads, saveMonitor);
				List<FuzzyNodeCluster> silClusters = silRunFCM.run(network, saveMonitor,clusters);
				silhouetteResults[cEstimate] = SilhouetteCalculator.calculate(distanceDataMatrix.getMatrix2DArray(), clusters);
			
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
				try {
					silhouetteResults[cEstimate] = SilhouetteCalculator.calculate(matrix.getMatrix2DArray(), clusters);
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
}
