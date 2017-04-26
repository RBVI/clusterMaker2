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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.ConnectedComponents;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;

// Cytoscape imports
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.TunableUIHelper;

// clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;

import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;

public class ConnectedComponentsCluster extends AbstractNetworkClusterer  {
	public static String SHORTNAME = "connectedcomponents";
	public static String NAME = "Connected components cluster";
	public final static String GROUP_ATTRIBUTE = "__CCGroups.SUID";

	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public ConnectedComponentsContext context = null;

	public ConnectedComponentsCluster(ConnectedComponentsContext context, ClusterManager manager) {
		super(manager);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
	}

	public String getShortName() { return SHORTNAME; }

	@ProvidesTitle
	public String getName() { return NAME; }

	public void run(TaskMonitor monitor) {
		monitor.setTitle("Performing ConnectedComponents cluster");
		this.monitor = monitor;
		if (network == null)
			network = clusterManager.getNetwork();

		// Make sure to update the context
		context.setNetwork(network);

		NodeCluster.init();

		CyMatrix matrix = context.edgeAttributeHandler.getMatrix();
		if (matrix == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Can't get distance matrix: no attribute value?");
			return;
		}

		// Update our tunable results
		clusterAttributeName = context.getClusterAttribute();
		createGroups = context.advancedAttributes.createGroups;

		if (canceled) return;

		Map<Integer, List<CyNode>> components = MatrixUtils.findConnectedComponents(matrix);

		// Create the NodeClusters
		Map<Integer, NodeCluster> clusterMap = new HashMap<Integer, NodeCluster>();
		for (Integer cluster: components.keySet()) {
			clusterMap.put(cluster, new NodeCluster(components.get(cluster)));
		}

		// Now get the sorted cluster map
		int clusterNumber = 1;
		HashMap<NodeCluster,NodeCluster> cMap = new HashMap<NodeCluster, NodeCluster>();
		for (NodeCluster cluster: NodeCluster.sortMap(clusterMap)) {

			if (cMap.containsKey(cluster))
				continue;

			cMap.put(cluster,cluster);

			cluster.setClusterNumber(clusterNumber);
			clusterNumber++;
		}

		List<NodeCluster> clusters = new ArrayList<NodeCluster> (cMap.keySet());

		monitor.showMessage(TaskMonitor.Level.INFO,"Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(network, GROUP_ATTRIBUTE);

		monitor.showMessage(TaskMonitor.Level.INFO,"Creating groups");

		params = new ArrayList<String>();
		context.edgeAttributeHandler.setParams(params);

		List<List<CyNode>> nodeClusters = createGroups(network, clusters, GROUP_ATTRIBUTE);

		results = new AbstractClusterResults(network, nodeClusters);

		monitor.showMessage(TaskMonitor.Level.INFO, 
		                    "ConnectedComponent results:\n"+results);

		if (context.vizProperties.showUI) {
			monitor.showMessage(TaskMonitor.Level.INFO, 
		                      "Creating network");
			insertTasksAfterCurrentTask(new NewNetworkView(network, clusterManager, true,
			                                               context.vizProperties.restoreEdges, 
																										 !context.edgeAttributeHandler.selectedOnly));
		}
	}

	public void cancel() {
	}

	@Override
	public void setUIHelper(TunableUIHelper helper) {context.setUIHelper(helper); }
}
