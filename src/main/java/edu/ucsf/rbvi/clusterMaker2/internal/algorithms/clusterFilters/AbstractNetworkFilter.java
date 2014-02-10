/* vim: set ts=2: */
/**
 * Copyright (c) 2011 The Regents of the University of California.
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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.FuzzyNodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

/**
 * This abstract class is the base class for all of the network filters provided by
 * clusterMaker.  Fundamentally, a network filters is an algorithm which functions to
 * modify the results of a previous cluster algorithm by filtering the results.
 */
public abstract class AbstractNetworkFilter extends AbstractNetworkClusterer {
	protected String groupAttribute;

	public AbstractNetworkFilter(ClusterManager clusterManager, String groupAttribute) { 
		super(clusterManager); 
		this.groupAttribute = groupAttribute;
	}


	public void run (TaskMonitor monitor) {
		monitor.setTitle("Filtering using "+getName());
		this.monitor = monitor;
		if (network == null)
			network = clusterManager.getNetwork();

		clusterAttributeName = getClusterAttributeName();

		// get the cluster list
		List<List<CyNode>> clusterList = getNodeClusters(getClusterAttribute());
		List<NodeCluster> newClusterList = new ArrayList<NodeCluster>();

		System.out.println("ClusterList has "+clusterList.size()+" clusters");

		Map<NodeCluster, List<CyNode>> addedNodeMap = new HashMap<NodeCluster, List<CyNode>>();
		// Iterate over clusters and build a new clusterList
		for (List<CyNode>nodeList: clusterList) {
			NodeCluster newCluster = doFilter(nodeList, addedNodeMap);
			if (newCluster != null && newCluster.size() > 0)
				newClusterList.add(newCluster);
		}

		// Fixup our clusters
		if (addedNodeMap.size() > 0) {
			// We grabbed some new nodes.  We need to determine if
			// those nodes were taken from any other cluster and remove
			// them if they were.  Ideally, we would continue to iterate
			// until this stabelized...
			for (NodeCluster addedCluster: addedNodeMap.keySet()) {
				for (NodeCluster cluster: newClusterList) {
					if (cluster.equals(addedCluster)) continue;
					removeNodes(cluster, addedNodeMap.get(addedCluster));
				}
			}
		}

		monitor.showMessage(TaskMonitor.Level.INFO,"Removing groups");

		// Remove any leftover groups from previous runs
		removeGroups(network, groupAttribute);

		monitor.showMessage(TaskMonitor.Level.INFO,"Creating groups");

		List<List<CyNode>> nodeClusters = createGroups(network, newClusterList, groupAttribute);
		System.out.println("nodeClusters has "+nodeClusters.size()+" clusters");

		results = new AbstractClusterResults(network, clusterList);
		ClusterResults results2 = new AbstractClusterResults(network, nodeClusters);
		monitor.showMessage(TaskMonitor.Level.INFO, "Done.  Results:\n\nBefore Filter:\n"+results+"\n\nAfter Filter:\n"+results2);

		if (showUI()) {
			monitor.showMessage(TaskMonitor.Level.INFO, 
		                      "Creating network");
			insertTasksAfterCurrentTask(new NewNetworkView(network, clusterManager, true,
			                                               restoreEdges()));
		}

	}

	abstract public NodeCluster doFilter(List<CyNode>nodeList, Map<NodeCluster, List<CyNode>>addedNodeMap);

	abstract public String getClusterAttribute();
	abstract public String getClusterAttributeName();

	abstract public boolean restoreEdges();
	abstract public boolean showUI();

	private void removeNodes(NodeCluster cluster, List<CyNode> nodesToRemove) {
		for (CyNode node: nodesToRemove) {
			if (cluster.contains(node))
				cluster.remove(node);
		}
	}

}
