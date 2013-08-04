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
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

import edu.ucsf.rbvi.clusterMaker2.internal.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.RunMCL;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.FuzzyNodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.edgeConverters.EdgeWeightConverter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.SilhouetteCalculator;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.Silhouettes;
import clusterMaker.ui.ClusterViz;
import clusterMaker.ui.NewNetworkView;

/**
 * The FCMCluster class is for implementing the Fuzzy C-Means algorithm for clustering. 
 * The fuzzy clusters are created based on node properties and each node gets assigned a 
 * degree of membership to each cluster.
 */

public class FCMCluster extends AbstractNetworkClusterer {
	
	RunFCM runFCM = null;
	public static final String NONEATTRIBUTE = "--None--";
	protected Matrix dataMatrix;
	private boolean selectedOnly = false;
	private boolean ignoreMissing = true;
	private Silhouettes[] silhouetteResults = null;
	private CyApplicationManager manager;
	int rNumber = 50;
	int c = -1;

	private String[] attributeArray = new String[1];
	
	@Tunable(description = "Number of iterations")
	public int iterations;
	
	@Tunable(description = "Maximum Number of clusters")
	public int cMax;
	
	@Tunable(description = "Number of clusters")
	public int cNumber;
	
	@Tunable(description = "Fuzziness Index")
	public double fIndex;
	
	@Tunable(description = " Margin allowed for change in fuzzy memberships, to act as end criterion ")
	public double beta;
	
	@Tunable(description = "Distance Metric")
	public ListSingleSelection<DistanceMetric> metric;
	
	@Tunable(description = "Distance Metric")
	public DistanceMetric getMetric(){
		return metric.getSelectedValue();
	}
	
	public void setMetric(DistanceMetric newMetric){
		
		metric.setSelectedValue(newMetric);
		System.out.println("Setting the value of Distance Metric to: " + metric.getSelectedValue()  );
	}
	
	@Tunable (description = "The attribute to use to get the weights")
	public ListMultipleSelection<String> attributeList;
	
	@Tunable(description = "The attribute to use to get the weights")
	public List<String> getAttributeList(){
		return attributeList.getSelectedValues();
	}
	
	public void setAttributeList(List<String> newAttributeList){
		
		attributeList.setSelectedValues(newAttributeList);
		System.out.println("Setting the Attribute List to: " + attributeList.getSelectedValues() );
	}
	
	
	
	public FCMCluster(ClusterManager clusterManager) {
		super();
		
		
		this.manager = clusterManager.manager;
		CyNetwork network = manager.getCurrentNetwork();
		Long networkID = network.getSUID();
		
		clusterAttributeName = networkID + "_FCM_cluster" ;
		Logger logger = LoggerFactory.getLogger(FCMCluster.class);
		initializeProperties();
	}

	public String getShortName() {return "fcm";};
	public String getName() {return "FCM cluster";};

		
	/**
	 * initializeProperties initializes the values of tunables	
	 */
	public void initializeProperties() {
		super.initializeProperties();

		/**
		 * Tuning values
		 */
		
		iterations = rNumber;
		cNumber = c;
		cMax = 10;
		fIndex = 1.5;
		beta = 0.01;
		
		attributeArray = getAllAttributes();
		if (attributeArray.length > 0){
			attributeList = new ListMultipleSelection<String>(attributeArray);	
			List<String> temp = new ArrayList<String>();
			temp.add(attributeArray[0]);
			attributeList.setSelectedValues(temp);
		}
		else{
			attributeList = new ListMultipleSelection<String>("None");
		}
		
		DistanceMetric[] distanceMetricArray = Matrix.distanceTypes;
		metric = new ListSingleSelection<DistanceMetric>(distanceMetricArray);
		
			
		super.advancedProperties();
		clusterProperties.initializeProperties();
		updateSettings(true);
	}
	
	public void doCluster(TaskMonitor monitor) {
		this.monitor = monitor;
		
		CyAppAdapter adapter;
		CyApplicationManager manager = adapter.getCyApplicationManager();
		CyNetwork network = manager.getCurrentNetwork();
		Long networkID = network.getSUID();

		CyTable netAttributes = network.getDefaultNetworkTable();
		CyTable nodeAttributes = network.getDefaultNodeTable();
		
		Logger logger = LoggerFactory.getLogger("CyUserMessages");
		DistanceMatrix matrix = edgeAttributeHandler.getMatrix();
		if (matrix == null) {
			logger.error("Can't get distance matrix: no attribute value?");
			return;
		}

		if (canceled) return;
		
		List <String> dataAttributes = getAttributeList();
		String[] attributeArray = new String[dataAttributes.size()];
		for (int i = 0; i < dataAttributes.size(); i++){
			
			attributeArray[i] = dataAttributes.get(i);
		}
		
		Arrays.sort(attributeArray);
		
		// Create the matrix of data with the attributes for calculating distances
		dataMatrix = new Matrix(attributeArray, true, ignoreMissing, selectedOnly);
		dataMatrix.setUniformWeights();
		//Cluster the nodes
		
		this.cNumber = cEstimate();
		DistanceMetric distMetric = getMetric();
		runFCM = new RunFCM(dataMatrix, matrix, iterations, cNumber, distMetric, fIndex, beta, logger);

		
		//RunFCM (Matrix data,DistanceMatrix dMat, int num_iterations, int cClusters,DistanceMetric metric, double findex, double beta, int maxThreads, Logger logger)
		runFCM.setDebug(debug);

		if (canceled) return;

		// results = runMCL.run(monitor);
		List<FuzzyNodeCluster> clusters = runFCM.run(monitor);
		if (clusters == null) return; // Canceled?
		
		addToNetwork(nodeAttributes, dataMatrix, runFCM.clusterMemberships);

		logger.info("Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(netAttributes, networkID);

		logger.info("Creating groups");
		monitor.setStatusMessage("Creating groups");/*monitor.setStatus("Creating groups");*/

		List<List<CyNode>> nodeClusters = 
		     createGroups(netAttributes, networkID, nodeAttributes, clusters);

		results = new ClusterResults(network, nodeClusters);
		monitor.setStatusMessage("Done.  FCM results:\n"+results);

		// Tell any listeners that we're done
		pcs.firePropertyChange(ClusterAlgorithm.CLUSTER_COMPUTED, null, this);
	}
	
	public void halt() {
		canceled = true;
		if (runFCM != null)
			runFCM.halt();
	}

	public void setParams(List<String>params) {
		
	//	params.add("rNumber="+rNumber);
		//params.add("clusteringThresh="+clusteringThresh);
		//params.add("maxResidual="+maxResidual);
		super.setParams(params);
	}

				
		private String[] getAllAttributes() {
			attributeArray = new String[1];
			// Create the list by combining node and edge attributes into a single list
			List<String> attributeList = new ArrayList<String>();
			attributeList.add(NONEATTRIBUTE);
			getAttributesList(attributeList, Cytoscape.getEdgeAttributes());
			String[] attrArray = attributeList.toArray(attributeArray);
			if (attrArray.length > 1) 
				Arrays.sort(attrArray);
			return attrArray;
		}
		
		/**
		 * This method adds the membership value array of each CyNode to the node attributes table 
		 * 
		 * @param nodeAttributes :Attribute Table for nodes
		 * @param data : data matrix for the current set of nodes
		 * @param clusterMemberships : 2D array of membership values
		 */
		
		private void addToNetwork(CyTable nodeAttributes, Matrix data, double[][] clusterMemberships){
			
			CyNode node; 
			for(int i = 0; i < data.nRows(); i++ ){
				node = data.getRowNode(i);
				nodeAttributes.getRow(node).set(clusterAttributeName + "_MembershipValues", clusterMemberships[i]);
			}
									
		}
		
		private int cEstimate(){
			int nClusters;
			TaskMonitor saveMonitor = monitor;
			monitor = null;
			silhouetteResults = new Silhouettes[cMax];

			int nThreads = Runtime.getRuntime().availableProcessors()-1;
			if (nThreads > 1)
				runThreadedSilhouette(cMax, iterations, nThreads, saveMonitor);
			else
				runLinearSilhouette(cMax, iterations, saveMonitor);

			// Now get the results and find our best k
			double maxSil = Double.MIN_VALUE;
			for (int cEstimate = 2; cEstimate < cMax; cEstimate++) {
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
				if (halted()) return;
				if (saveMonitor != null) saveMonitor.setStatusMessage("Getting silhouette with a k estimate of "+kEstimate);
				//int ifound = kcluster(kEstimate, nIterations, dataMatrix, metric, clusters);
				silhouetteResults[kEstimate] = SilhouetteCalculator.calculate(dataMatrix, metric.getSelectedValue(), clusters);
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
				if (halted()) return;
				if (saveMonitor != null) saveMonitor.setStatusMessage("Getting silhouette with a c estimate of "+cEstimate);
				//int ifound = kcluster(kEstimate, nIterations, matrix, metric, clusters);
				try {
					silhouetteResults[cEstimate] = SilhouetteCalculator.calculate(matrix, metric.getSelectedValue(), clusters);
				} catch (Exception e) { e.printStackTrace(); }
			}
		}
		

}







