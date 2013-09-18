/* vim: set ts=2: */
/**
 * Copyright (c) 2013 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *      notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions, and the following
 *      disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *      originally developed by the UCSF Computer Graphics Laboratory
 *      under support by the NIH National Center for Research Resources,
 *      grant P41-RR01081.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * 
 * File last modified 06-25-13 by Aaron M. Newman, Ph.D.
 *
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.launch.Settings;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.KnnView;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;


public class AutoSOMECluster extends AbstractNetworkClusterer  {
	
	public final static String GROUP_ATTRIBUTE = "__AutoSOMEGroups.SUID";
	public final static String SHORTNAME = "autosome_heatmap";
	public final static String NAME = "AutoSOME Attribute Clustering";
	public final static String NET_SHORTNAME = "autosome_network";
	public final static String NET_NAME = "AutoSOME Network Clustering";

	private int cluster_output=0;
	private boolean finishedClustering = false;
	private Settings settings;

	private RunAutoSOME runAutoSOME = null;

	private List<NodeCluster> nodeCluster;

	private List<String>attrList;
	private List<String>attrOrderList;
	private List<String>nodeOrderList;

	private boolean heatmap = true;
	
	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public AutoSOMEContext context = null;
	
	public AutoSOMECluster(AutoSOMEContext context, ClusterManager clusterManager, boolean heatmap) {
		super(clusterManager);
		this.context = context;
		this.heatmap = heatmap;

		// Initialize our settings
		context.numThreads = Runtime.getRuntime().availableProcessors();

		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);

		if (heatmap)
			context.dataVisualization.setSelectedValue("Heatmap");
		else
			context.dataVisualization.setSelectedValue("Network");

		clusterAttributeName = context.getClusterAttribute();
	}

	public String getShortName() {
		return SHORTNAME;
	};

	@ProvidesTitle
	public String getName() {return "AutoSOME "+((context.getSettings().distMatrix) ? "Fuzzy " : "")+"Clustering";}

	public ClusterViz getVisualizer() {
		return null;
	}

	public void run(TaskMonitor monitor) {
		this.monitor = monitor;
		monitor.setTitle("Performing "+getName());

		String networkID = ModelUtils.getNetworkName(network);
		
		// Update settings from our context
		settings = context.getSettings();
               
		//got back to parent to cluster again
		if(networkID.contains("--AutoSOME")){
			String[] tokens = networkID.split("--AutoSOME");
			networkID = tokens[0];
			network = ModelUtils.getNetworkWithName(clusterManager, networkID);
		} 

		List<String> dataAttributes = context.attributeList.getNodeAttributeList();

		//Cluster the nodes
		runAutoSOME = new RunAutoSOME(clusterManager, dataAttributes, network, settings, monitor);

		runAutoSOME.setIgnoreMissing(context.ignoreMissing);
		runAutoSOME.setSelectedOnly(context.selectedOnly);

		runAutoSOME.setDebug(debug);

		monitor.setStatusMessage("Running AutoSOME"+((settings.distMatrix) ? " Fuzzy Clustering" : ""));

		nodeCluster = runAutoSOME.run(monitor);

		if(nodeCluster==null) {
			monitor.setStatusMessage("Clustering failed!");
			return;
		}

		if(nodeCluster.size()>0) finishedClustering=true;

		monitor.setStatusMessage("Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(network, getShortName());

		monitor.setStatusMessage("Creating groups");
		
		if(settings.distMatrix)
			runAutoSOME.getEdges(context.maxEdges);

		attrList = runAutoSOME.attrList;
		attrOrderList = runAutoSOME.attrOrderList;
		nodeOrderList = runAutoSOME.nodeOrderList;

		ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_NODE_ATTRIBUTE,
				                     attrList, List.class, String.class);
		ModelUtils.createAndSetLocal(network, network, ClusterManager.ARRAY_ORDER_ATTRIBUTE,
                attrOrderList, List.class, String.class);
		ModelUtils.createAndSetLocal(network, network, ClusterManager.NODE_ORDER_ATTRIBUTE,
                nodeOrderList, List.class, String.class);

		ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE,
                                     getShortName(), String.class, null);


		List<List<CyNode>> nodeClusters;

		if(!settings.distMatrix) {
			nodeClusters =
				createGroups(network, nodeCluster, GROUP_ATTRIBUTE);		   
			ClusterResults results = new AbstractClusterResults(network, nodeClusters);
			monitor.setStatusMessage("Done.  AutoSOME results:\n"+results);
			System.out.println("Done.  AutoSOME results:\n"+results);
		} else {
			nodeClusters = new ArrayList<List<CyNode>>();
			/*
			for (NodeCluster cluster: clusters) {
				List<CyNode>nodeList = new ArrayList();

				for (CyNode node: cluster) {
					nodeList.add(node);
				}
				nodeClusters.add(nodeList);
			}
	   */
			monitor.setStatusMessage("Done.  AutoSOME results:\n"+nodeCluster.size()+" clusters found.");
			System.out.println("Done.  AutoSOME results:\n"+nodeCluster.size()+" clusters found.");
		}

		// List<String> params = context.getParams(runAutoSOME.getMatrix());
		// updateParams(network, params);

		if (context.showViz) {
			if (heatmap)
				insertTasksAfterCurrentTask(new KnnView(clusterManager));
			else
				insertTasksAfterCurrentTask(new NewNetworkView(network, clusterManager, true, false));
		}


	}

	public void cancel() {
		runAutoSOME.cancel();
	}

}
