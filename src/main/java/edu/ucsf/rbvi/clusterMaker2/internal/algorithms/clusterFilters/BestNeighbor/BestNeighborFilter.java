/* vim: set ts=2: */
/**
 * Copyright (c) 201 The Regents of the University of California.
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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.BestNeighbor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.AbstractNetworkFilter;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class BestNeighborFilter extends AbstractNetworkFilter {
	public static String SHORTNAME = "bestneighbor";
	public static String NAME = "Best Neighbor Filter";
	public final static String GROUP_ATTRIBUTE = "__BestNeighborGroups.SUID";

	@Tunable(description="Network to filter", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public BestNeighborContext context = null;

	public BestNeighborFilter(BestNeighborContext context, ClusterManager clusterManager) {
		super(clusterManager, GROUP_ATTRIBUTE);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
		super.network = network;
	}

	public String getShortName() { return SHORTNAME; }

	@ProvidesTitle
	public String getName() { return NAME; }

	public String getClusterAttribute() {
		return context.getClusterAttribute();
	}

	public String getClusterAttributeName() {
		return context.getClusterAttributeName();
	}

	public boolean restoreEdges() {return context.vizProperties.restoreEdges;}
	public boolean showUI() {return context.vizProperties.showUI;}

	/**
	 * Add any nodes that are above the neighbor threshold to
	 * our cluster
	 */
	public NodeCluster doFilter(List<CyNode>nodeList, Map<NodeCluster, List<CyNode>> addedNodeMap) {
		Set<CyNode> clusterNodes = new HashSet<CyNode>(nodeList);
		List<CyNode> newNodeList = new ArrayList<CyNode>();
		newNodeList.addAll(nodeList);
		List<CyNode> nodesToAdd = new ArrayList<CyNode>();
		for (CyNode node: nodeList) {
			for (CyNode neighbor: network.getNeighborList(node, CyEdge.Type.ANY)) {
				if (clusterNodes.contains(neighbor)) continue;
				double adjacency = getAdjacency(neighbor, clusterNodes);
				if (adjacency > context.threshold) {
					// Add this node to our list
					nodesToAdd.add(neighbor);
				}
			}
		}

		newNodeList.addAll(nodesToAdd);

		if (newNodeList.size() > 1) {
			NodeCluster newNodeCluster = new NodeCluster(newNodeList);
			if (nodesToAdd.size() > 0)
				addedNodeMap.put(newNodeCluster,nodesToAdd);
			return newNodeCluster;
		}
		return null;
	}

	private double getAdjacency(CyNode neighbor, Set<CyNode> clusterNodes) {
		List<CyEdge> edgeList = network.getAdjacentEdgeList(neighbor, CyEdge.Type.ANY);
		int totalEdges = edgeList.size();
		if (totalEdges == 0) return 0;

		int adjacentEdges = 0;
		for (CyEdge edge: edgeList) {
			if (clusterNodes.contains(edge.getSource()) || clusterNodes.contains(edge.getTarget())) {
				adjacentEdges += 1;
			}
		}
		return (double)adjacentEdges/(double)totalEdges;
	}

}
