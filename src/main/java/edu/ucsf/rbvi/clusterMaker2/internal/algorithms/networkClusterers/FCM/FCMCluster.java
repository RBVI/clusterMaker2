package org.cytoscape.myapp.internal.algorithms.networkClusterers.FCM;

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
import org.cytoscape.myapp.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import org.cytoscape.myapp.internal.algorithms.networkClusterers.MCL.MCLCluster;
import org.cytoscape.myapp.internal.algorithms.networkClusterers.MCL.RunMCL;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import clusterMaker.ClusterMaker;
import org.cytoscape.myapp.internal.algorithms.ClusterAlgorithm;
import org.cytoscape.myapp.internal.algorithms.ClusterResults;
import org.cytoscape.myapp.internal.algorithms.DistanceMatrix;//import clusterMaker.algorithms.DistanceMatrix;
import org.cytoscape.myapp.internal.algorithms.NodeCluster;//import clusterMaker.algorithms.NodeCluster;
import org.cytoscape.myapp.internal.algorithms.edgeConverters.EdgeAttributeHandler;
import clusterMaker.ui.ClusterViz;
import clusterMaker.ui.NewNetworkView;



public class FCMCluster extends AbstractNetworkClusterer {
	
	int rNumber = 50;
	int c = -1;
	
	@Tunable(description = "Number of iterations")
	public int iterations;
	
	@Tunable(description = "Number of clusters")
	public int cNumber;
	
	
	
	public FCMCluster() {
		super();
		
		CyAppAdapter adapter;
		CyApplicationManager manager = adapter.getCyApplicationManager();
		CyNetwork network = manager.getCurrentNetwork();
		Long networkID = network.getSUID();
		
		clusterAttributeName = networkID + "_FCM_cluster" ;
		Logger logger = LoggerFactory.getLogger(FCMCluster.class);
		initializeProperties();
	}

	public String getShortName() {return "fcm";};
	public String getName() {return "FCM cluster";};

		
		
	public void initializeProperties() {
		super.initializeProperties();

		/**
		 * Tuning values
		 */
		
		iterations = rNumber;
		cNumber = c;
		

	
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

		//Cluster the nodes
		runFCM = new RunFCM(matrix, inflation_parameter, rNumber, 
		                    clusteringThresh, maxResidual, maxThreads, logger);

		runFCM.setDebug(debug);

		if (canceled) return;

		// results = runMCL.run(monitor);
		List<NodeCluster> clusters = runFCM.run(monitor);
		if (clusters == null) return; // Canceled?

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
		params.add("inflation_parameter="+inflation_parameter);
		params.add("rNumber="+rNumber);
		params.add("clusteringThresh="+clusteringThresh);
		params.add("maxResidual="+maxResidual);
		super.setParams(params);
	}


}







