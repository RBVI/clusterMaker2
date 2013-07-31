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
	private boolean selectedOnly = false;
	private boolean ignoreMissing = true;
	private CyApplicationManager manager;
	int rNumber = 50;
	int c = -1;

	private String[] attributeArray = new String[1];
	
	@Tunable(description = "Number of iterations")
	public int iterations;
	
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
		Matrix dataMatrix = new Matrix(attributeArray, true, ignoreMissing, selectedOnly);
		dataMatrix.setUniformWeights();
		//Cluster the nodes
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

}







