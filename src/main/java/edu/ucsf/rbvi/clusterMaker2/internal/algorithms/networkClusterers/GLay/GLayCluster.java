/* vim: set ts=2: */
/**
 * Copyright (c) 2008 The Regents of the University of California.
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
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.GLay;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

// Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

// clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;

public class GLayCluster extends AbstractNetworkClusterer  {
	public static String SHORTNAME = "glay";
	public static String NAME = "Community cluster (GLay)";
	public final static String GROUP_ATTRIBUTE = SHORTNAME;
	
	FastGreedyAlgorithm fa = null;
	boolean createNewNetwork = false;

	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public GLayContext context = null;

	public GLayCluster(GLayContext context, ClusterManager manager) {
		super(manager);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
	}

	public String getShortName() {return SHORTNAME;};

	@ProvidesTitle
	public String getName() {return NAME;};

	public ClusterViz getVisualizer() {
		// return new NewNetworkView(true);
		return null;
	}

	public void run(TaskMonitor monitor) {
		this.monitor = monitor;
		monitor.setTitle("Performing community clustering (GLay)");
		createGroups = context.advancedAttributes.createGroups;
		clusterAttributeName = context.getClusterAttribute();

		if (network == null)
			network = clusterManager.getNetwork();

		// Make sure to update the context
		context.setNetwork(network);

		NodeCluster.init();

    GSimpleGraphData simpleGraph = new GSimpleGraphData(network, context.selectedOnly, context.undirectedEdges);
		fa = new FastGreedyAlgorithm();
		//fa.partition(simpleGraph);
		fa.execute(simpleGraph, monitor);

		NumberFormat nf = NumberFormat.getInstance();
		String modularityString = nf.format(fa.getModularity());

		List<NodeCluster> clusterList = new ArrayList<NodeCluster>();
		for (int cluster = 0; cluster < fa.getClusterNumber(); cluster++) {
			clusterList.add(new NodeCluster());
		}

    int membership[] = fa.getMembership();
    for(int index=0; index < simpleGraph.graphIndices.length; index++){
      int cluster = membership[index];
      clusterList.get(cluster).add(simpleGraph.graphIndices[index]);
    }

		monitor.showMessage(TaskMonitor.Level.INFO,"Found "+clusterList.size()+" clusters");

		// Remove any leftover groups from previous runs
		removeGroups(network, GROUP_ATTRIBUTE);

		monitor.showMessage(TaskMonitor.Level.INFO,"Creating groups");

		List<List<CyNode>> nodeClusters = createGroups(network, clusterList, GROUP_ATTRIBUTE);

		results = new AbstractClusterResults(network, clusterList);
		monitor.showMessage(TaskMonitor.Level.INFO, "Done.  Community Clustering results:\n"+results);

		if (context.vizProperties.showUI) {
			monitor.showMessage(TaskMonitor.Level.INFO, 
		                      "Creating network");
			insertTasksAfterCurrentTask(new NewNetworkView(network, clusterManager, true,
			                                               context.vizProperties.restoreEdges,
																										 !context.selectedOnly));
		}

	}

	public void cancel() {
		if (fa != null)
			fa.cancel();
	}

}
