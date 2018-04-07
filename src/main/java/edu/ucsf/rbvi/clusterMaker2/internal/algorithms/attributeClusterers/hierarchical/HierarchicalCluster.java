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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hierarchical;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Cytoscape imports
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractAttributeClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.TreeView;


// clusterMaker imports

public class HierarchicalCluster extends AbstractAttributeClusterer implements ObservableTask {
	public static String SHORTNAME = "hierarchical";
	public static String NAME = "Hierarchical cluster";
	private List<CyNode> nodeOrder = null;
	private List<String> attributeOrder = null;
	private List<String> nodeTree = null;
	private List<String> attributeTree = null;

	/**
	 * Linkage types
	 */
	public static ClusterMethod[] linkageTypes = { ClusterMethod.AVERAGE_LINKAGE,
	                                               ClusterMethod.SINGLE_LINKAGE,
	                                               ClusterMethod.MAXIMUM_LINKAGE,
	                                               ClusterMethod.CENTROID_LINKAGE };

	ClusterMethod clusterMethod =  ClusterMethod.AVERAGE_LINKAGE;

	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public HierarchicalContext context = null;

	public HierarchicalCluster(HierarchicalContext context, ClusterManager clusterManager) {
		super(clusterManager);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
	}

	public String getShortName() {return SHORTNAME;}

	@ProvidesTitle
	public String getName() {return NAME;}

	public ClusterViz getVisualizer() {
		// return new TreeView();
		return null;
	}

	public void run(TaskMonitor monitor) {
		this.monitor = monitor;
		monitor.setTitle("Performing "+getName());

		List<String> nodeAttributeList = context.attributeList.getNodeAttributeList();
		String edgeAttribute = context.attributeList.getEdgeAttribute();

		if (nodeAttributeList == null && edgeAttribute == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Must select either one edge column or two or more node columns");
			return;
		}

		if (nodeAttributeList != null && nodeAttributeList.size() > 0 && edgeAttribute != null) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Can't have both node and edge columns selected");
			return;
		}

		if (context.selectedOnly && nodeAttributeList != null && nodeAttributeList.size() > 1 
				&& CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true).size() < 3) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Must have at least three nodes to cluster");
			return;
		}

		// Get our attributes we're going to use for the cluster
		String[] attributeArray;
		if (nodeAttributeList != null && nodeAttributeList.size() > 0) {
			Collections.sort(nodeAttributeList);
			attributeArray = new String[nodeAttributeList.size()];
			int i = 0;
			for (String attr: nodeAttributeList) { attributeArray[i++] = "node."+attr; }
		} else {
			attributeArray = new String[1];
			attributeArray[0] = "edge."+edgeAttribute;
		}

		monitor.showMessage(TaskMonitor.Level.INFO, "Initializing");
		// System.out.println("Initializing");

		resetAttributes(network, SHORTNAME);

		// Create a new clusterer
		DistanceMetric metric = context.metric.getSelectedValue();
		RunHierarchical algorithm = new RunHierarchical(network, attributeArray, metric, 
		                                                clusterMethod, monitor, context);

		// Cluster the attributes, if requested
		if (context.clusterAttributes && (attributeArray.length > 1 || context.isAssymetric())) {
			monitor.setStatusMessage("Clustering attributes");

			// System.out.println("Clustering attributes");
			Integer[] rowOrder = algorithm.cluster(true);
			attributeTree = algorithm.getAttributeList();
			CyMatrix matrix = algorithm.getMatrix();
			updateAttributes(network, SHORTNAME, rowOrder, attributeArray, 
			                 attributeTree, matrix);
			attributeOrder = new ArrayList<String>();
			for (int i = 0; i < rowOrder.length; i++) {
				attributeOrder.add(matrix.getRowLabel(rowOrder[i]));
			}
		}

		monitor.setStatusMessage("Clustering nodes");

		// Cluster the nodes
		// System.out.println("Clustering nodes");
		Integer[] rowOrder = algorithm.cluster(false);
		nodeTree = algorithm.getAttributeList();
		CyMatrix matrix = algorithm.getMatrix();
		updateAttributes(network, SHORTNAME, rowOrder, attributeArray, 
		                 nodeTree, matrix);

		nodeOrder = new ArrayList<CyNode>();
		for (int i = 0; i < rowOrder.length; i++) {
			nodeOrder.add(matrix.getRowNode(rowOrder[i]));
		}

		// TODO: Deal with params!
		List<String> params = context.getParams(algorithm.getMatrix());
		updateParams(network, params);

		if (context.showUI) {
			insertTasksAfterCurrentTask(new TreeView(clusterManager));
		}

		monitor.setStatusMessage("Done");

	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class, String.class);
	}

	@Override
  public <R> R getResults(Class<? extends R> requestedType) {
		if (requestedType.equals(String.class)) {
			return (R)toString();
		} else if (requestedType.equals(JSONResult.class)) {
			JSONResult res = () -> {
				String strRes = "{";
				strRes += "\"nodeOrder\": "+getNodeOrder()+",";
				strRes += "\"nodeTree\": "+getTree(nodeTree);
				if (attributeOrder != null) {
					strRes += ",\"attributeOrder\": "+getAttributeOrder()+",";
					strRes += "\"attributeTree\": "+getTree(attributeTree);
				}
				strRes += "}";
				return strRes;
			};
			System.out.println("Returning JSONResult");
			return (R)res;
		}
		return (R)toString();
	}

	private String getNodeOrder() {
		String res = "["+getNode(nodeOrder.get(0));
		for (int i = 1; i < nodeOrder.size(); i++) {
			res += ","+getNode(nodeOrder.get(i));
		}
		res += "]";
		return res;
	}

	private String getNode(CyNode node) {
		return "{\"nodeName\": \""+getName(node)+"\", \"suid\":"+node.getSUID()+"}";
	}

	private String getName(CyNode node) {
		return network.getRow(node).get(CyNetwork.NAME, String.class);
	}

	private String getTree(List<String> tree) {
		String res = "["+getTreeNode(tree.get(0));
		for (int i = 1; i < tree.size(); i++) {
			res += ","+getTreeNode(tree.get(i));
		}
		res += "]";
		return res;
	}

	private String getTreeNode(String treeNode) {
		String[] parts = treeNode.split("\t");
		String res = "{\"name\": \""+parts[0]+"\", \"left\": \""+parts[1]+
		             "\", \"right\": \""+parts[2]+"\", \"distance\": "+parts[3]+"}";
		return res;
	}

	private String getAttributeOrder() {
		String res = "[\""+attributeOrder.get(0)+"\"";
		for (int i = 1; i < attributeOrder.size(); i++) {
			res += ",\""+attributeOrder.get(i)+"\"";
		}
		res += "]";
		return res;
	}
}
