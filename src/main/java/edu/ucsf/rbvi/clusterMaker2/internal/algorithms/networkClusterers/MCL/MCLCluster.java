package org.cytoscape.myapp.internal.algorithms.networkClusterers.MCL;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.swing.JPanel;


import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
//Cytoscape imports
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
//import cytoscape.Cytoscape;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import clusterMaker.ClusterMaker;
import clusterMaker.algorithms.networkClusterers.AbstractNetworkClusterer;
import clusterMaker.algorithms.ClusterAlgorithm;
import clusterMaker.algorithms.ClusterResults;
import org.cytoscape.myapp.internal.algorithms.DistanceMatrix;//import clusterMaker.algorithms.DistanceMatrix;
import org.cytoscape.myapp.internal.algorithms.NodeCluster;//import clusterMaker.algorithms.NodeCluster;
import clusterMaker.algorithms.edgeConverters.EdgeAttributeHandler;
import clusterMaker.ui.ClusterViz;
import clusterMaker.ui.NewNetworkView;


public class MCLCluster extends AbstractNetworkClusterer   {

	
	RunMCL runMCL = null;
	
	//Tunables
	
	@Tunable(description ="Basic MCL Tuning" ) 
	public int tunables_panel;
	
	@Tunable(description = "Granularity parameter (inflation value)")
	public double inflation_parameter;
	
	/*****/@Tunable(description =  "MCL Advanced Settings", params="displayState=collapsed")
	public int mclAdvancedGroup;

	@Tunable(description = "Weak edge weight pruning threshold")
	public double clusteringThresh;
	
	@Tunable(description = "Number of iterations")
	public int iterations;
	
	@Tunable(description = "Maximum residual value")
	public double maxResidual;
	
	@Tunable(description = "Maximum number of threads")
	public int maxThreads;
    
	
	
	public MCLCluster() {
		super();
		
		CyAppAdapter adapter;
		CyApplicationManager manager = adapter.getCyApplicationManager();
		CyNetwork network = manager.getCurrentNetwork();
		Long networkID = network.getSUID();
		
		clusterAttributeName = networkID + "_MCL_cluster" ;//Cytoscape.getCurrentNetwork().getIdentifier()+"_MCL_cluster";
		Logger logger = LoggerFactory.getLogger(MCLCluster.class);
		initializeProperties();
	}
	
	public String getShortName() {return "mcl";};
	public String getName() {return "MCL cluster";};

	public JPanel getSettingsPanel() {
		// Everytime we ask for the panel, we want to update our attributes
		edgeAttributeHandler.updateAttributeList();

		return clusterProperties.getTunablePanel();
	}
	
	public ClusterViz getVisualizer() {
		return new NewNetworkView(true);
	}
	
	public void initializeProperties() {
		super.initializeProperties();

		/**
		 * Tuning values
		 */
		tunables_panel = 1;
		inflation_parameter = 2.0;
		mclAdvancedGroup = 4;
		iterations = 16;
		clusteringThresh = 1e-15;
		maxResidual = 0.001;
		maxThreads = 0;

		// Use the standard edge attribute handling stuff....
		edgeAttributeHandler = new EdgeAttributeHandler(clusterProperties, true);

	/*****/	/*clusterProperties.add(new Tunable("mclAdvancedGroup", "MCL Advanced Settings",
		                                  Tunable.GROUP, new Integer(4),
		                                  new Boolean(true), null, Tunable.COLLAPSABLE));
		*/
		super.advancedProperties();
		clusterProperties.initializeProperties();
		updateSettings(true);
	}
	
	public void updateSettings() {
		updateSettings(false);
	}

	public void updateSettings(boolean force) {
		clusterProperties.updateValues();
		super.updateSettings(force);

		@Tunable(description="inflation parameter") 
		Double t = clusterProperties.get("inflation_parameter");
		if ((t != null) && (t.valueChanged() || force))
			inflation_parameter = ((Double) t.getValue()).doubleValue();

		t = clusterProperties.get("clusteringThresh");
		if ((t != null) && (t.valueChanged() || force))
			clusteringThresh = ((Double) t.getValue()).doubleValue();

		t = clusterProperties.get("maxResidual");
		if ((t != null) && (t.valueChanged() || force))
			maxResidual = ((Double) t.getValue()).doubleValue();

		t = clusterProperties.get("maxThreads");
		if ((t != null) && (t.valueChanged() || force))
			maxThreads = ((Integer) t.getValue()).intValue();

		t = clusterProperties.get("iterations");
		if ((t != null) && (t.valueChanged() || force))
			rNumber = ((Integer) t.getValue()).intValue();

		edgeAttributeHandler.updateSettings(force);
	}

	public void doCluster(TaskMonitor monitor) {
		this.monitor = monitor;
		
		CyAppAdapter adapter;
		CyApplicationManager manager = adapter.getCyApplicationManager();
		CyNetwork network = manager.getCurrentNetwork();
		Long networkID = network.getSUID();

		CyTable netAttributes = network.getDefaultNetworkTable();
		CyTable nodeAttributes = network.getDefaultNodeTable();
		//CyAttributes netAttributes = Cytoscape.getNetworkAttributes();
		//CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();

		Logger logger = LoggerFactory.getLogger("CyUserMessages");
		DistanceMatrix matrix = edgeAttributeHandler.getMatrix();
		if (matrix == null) {
			logger.error("Can't get distance matrix: no attribute value?");
			return;
		}

		if (canceled) return;

		//Cluster the nodes
		runMCL = new RunMCL(matrix, inflation_parameter, rNumber, 
		                    clusteringThresh, maxResidual, maxThreads, logger);

		runMCL.setDebug(debug);

		if (canceled) return;

		// results = runMCL.run(monitor);
		List<NodeCluster> clusters = runMCL.run(monitor);
		if (clusters == null) return; // Canceled?

		logger.info("Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(netAttributes, networkID);

		logger.info("Creating groups");
		monitor.setStatusMessage("Creating groups");/*monitor.setStatus("Creating groups");*/

		List<List<CyNode>> nodeClusters = 
		     createGroups(netAttributes, networkID, nodeAttributes, clusters);

		results = new ClusterResults(network, nodeClusters);
		monitor.setStatusMessage("Done.  MCL results:\n"+results);

		// Tell any listeners that we're done
		pcs.firePropertyChange(ClusterAlgorithm.CLUSTER_COMPUTED, null, this);
	}

	public void halt() {
		canceled = true;
		if (runMCL != null)
			runMCL.halt();
	}

	public void setParams(List<String>params) {
		params.add("inflation_parameter="+inflation_parameter);
		params.add("rNumber="+rNumber);
		params.add("clusteringThresh="+clusteringThresh);
		params.add("maxResidual="+maxResidual);
		super.setParams(params);
	}
}
	
	



