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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.featureVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Cytoscape imports
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

// clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;

import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ViewUtils;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractAttributeClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;

public class FeatureVectorCluster extends AbstractAttributeClusterer {
	public static String SHORTNAME = "featurevector";
	public static String NAME = "Create Correlation Network from Node Attributes";

	final static String interaction = "distance";

	@ContainsTunables
	public FeatureVectorContext context = null;

	public FeatureVectorCluster(FeatureVectorContext context, ClusterManager clusterManager) {
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
		return null;
	}

	public void run(TaskMonitor monitor) {
		this.monitor = monitor;
		monitor.setTitle("Performing "+getName());
		List<String> nodeAttributeList = context.nodeAttributeList.getSelectedValues();

		// Sanity check all of our settings
		if (nodeAttributeList == null || nodeAttributeList.size() == 0) {
			if (monitor != null) {
				monitor.showMessage(TaskMonitor.Level.ERROR, "Error: no attribute list selected");
			} 
			return;
		}

		String[] attributeArray = new String[nodeAttributeList.size()];
		int index = 0;
		for (String attr: nodeAttributeList) { attributeArray[index++] = "node."+attr; }

		// To make debugging easier, sort the attribute array
		Arrays.sort(attributeArray);

		if (monitor != null) {
			monitor.setProgress(0.0);
			monitor.setStatusMessage("Initializaing");
		}

		// Create the matrix
		// Matrix matrix = new Matrix(network, attributeArray, false, context.ignoreMissing, context.selectedOnly);
		CyMatrix matrix = CyMatrixFactory.makeSmallMatrix(network, attributeArray, false, context.ignoreMissing, context.selectedOnly, false);

		if (monitor != null) {
			monitor.setProgress(0.1);
			monitor.setStatusMessage("Calculating edge distances");
			if (canceled) return;
		}

		// Create a weight vector of all ones (we don't use individual weighting, yet)
		// matrix.setUniformWeights();

		// Handle special cases
		if (context.zeroMissing)
			matrix.setMissingToZero();

		int nNodes = matrix.nRows();

		// For each node, get the distance to all other nodes
		double maxdistance = Double.MIN_VALUE;
		double mindistance = Double.MAX_VALUE;

		double distanceMatrix[][] = new double[nNodes][nNodes];
		for (int i = 0; i < nNodes; i++) {
			for (int j = i+1; j < nNodes; j++) {
 				double distance = context.metric.getSelectedValue().getMetric(matrix, matrix, i, j);
				maxdistance = Math.max(maxdistance, distance);
				mindistance = Math.min(mindistance, distance);
				distanceMatrix[i][j] = distance;
			}
			if (canceled) return;
			monitor.setProgress((double)i/((double)nNodes*4));
		}

		monitor.setStatusMessage("Assigning values to edges");

		List<CyEdge> edgeList = new ArrayList<CyEdge>();
		double scale = maxdistance - mindistance;

		CyNetwork newNet = null;
		if (context.createNewNetwork) {
			newNet = ModelUtils.createChildNetwork(clusterManager, network, network.getNodeList(), null, "--clustered");
		}

		for (int i = 0; i < nNodes; i++) {
			for (int j = i+1; j < nNodes; j++) {
				// time = System.currentTimeMillis();
				double distance = (distanceMatrix[i][j]-mindistance)/scale;

				CyNode source = (CyNode)ModelUtils.getNetworkObjectWithName(network, matrix.getRowLabel(i), CyNode.class);
				CyNode target = (CyNode)ModelUtils.getNetworkObjectWithName(network, matrix.getRowLabel(j), CyNode.class);

				if (context.createNewNetwork == true && distance > context.edgeCutoff && context.edgeCutoff != 0.0)
					continue;

				// time = System.currentTimeMillis();
				if (context.createNewNetwork == true) {
					CyEdge edge = newNet.addEdge(source, target, false);
					ModelUtils.createAndSet(newNet, edge, context.edgeAttribute, distance, Double.class, null);
					edgeList.add(edge);
				} else {
					List<CyEdge> connectingEdges = network.getConnectingEdgeList(source, target, CyEdge.Type.ANY);
					// edgeFetchTime += System.currentTimeMillis()-time;
					// time = System.currentTimeMillis();
					if (connectingEdges == null || connectingEdges.size() == 0) 
						continue;

					CyEdge edge = connectingEdges.get(0);
					ModelUtils.createAndSet(network, edge, context.edgeAttribute, distance, Double.class, null);
				}
			}
			if (canceled) return;
			monitor.setProgress(25.0 + (75.0 * (double)i/(double)nNodes)/100.0);
		}

		// If we're supposed to, create the new network
		if (context.createNewNetwork) {
			VisualStyle style = ViewUtils.getCurrentVisualStyle(clusterManager);

			CyNetworkView view = ViewUtils.createView(clusterManager, newNet, false);

			ViewUtils.doLayout(clusterManager, view, monitor, "force-directed");
			ViewUtils.setVisualStyle(clusterManager, view, style);
			ViewUtils.registerView(clusterManager, view);
		}

		/*
		System.out.println("Created "+newEdges+" edges");
		System.out.println("Edge creation time: "+edgeCreateTime+"ms");
		System.out.println("Network add time: "+networkAddTime+"ms");
		System.out.println("Edge fetch time: "+edgeFetchTime+"ms");
		System.out.println("Node fetch time: "+nodeFetchTime+"ms");
		System.out.println("Set attribute time: "+setAttributeTime+"ms");
		*/

		monitor.setStatusMessage("Complete");

	}

}
