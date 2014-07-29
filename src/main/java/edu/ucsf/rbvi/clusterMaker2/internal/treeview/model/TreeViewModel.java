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
package edu.ucsf.rbvi.clusterMaker2.internal.treeview.model;

// System imports
import java.util.Iterator;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;


// Cytoscape imports
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

// ClusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

// TreeView imports
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataModel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.TVModel;

/**
 * The ClusterVizModel provides the data that links the results of a cluster run
 * in Cytoscape with the Java TreeView code
 *
 */
public class TreeViewModel extends TVModel {
	// Keep track of gene to node references
	boolean isSymmetrical = false;
	boolean zeroMissing = false;
	Double diagonalValue = null;
	List<String> clusterParams = null;

	CyNetwork network;
	CyNetworkView networkView;
	TaskMonitor monitor;
	ClusterManager clusterManager;

	private String [] clusterHeaders = {"NODEID", "LEFT", "RIGHT", "CORRELATION"};

	public TreeViewModel(TaskMonitor monitor, CyNetwork network, 
	                     CyNetworkView networkView, ClusterManager clusterManager) {
		super();
		this.monitor = monitor;
		this.network = network;
		this.networkView = networkView;
		this.clusterManager = clusterManager;

		// Get the type of cluster
		String clusterType = null;
		if (ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE))
			clusterType = network.getRow(network).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);

		if (ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE))
			clusterParams = network.getRow(network).getList(ClusterManager.CLUSTER_PARAMS_ATTRIBUTE, String.class);

		if (clusterParams != null) {
			for (String param: clusterParams) {
				String[] pair = param.split("=");
				if (pair[0].equals("zeroMissing"))
					zeroMissing = Boolean.valueOf(pair[1]);
				else if (pair[0].equals("diagonals")) {
					diagonalValue = Double.valueOf(pair[1]);
				}
			}
		}
		
		// Gene annotations are just the list of node names
		List<String>geneList = network.getRow(network).getList(ClusterManager.NODE_ORDER_ATTRIBUTE, String.class);
		// System.out.println("geneList: "+geneList);
		String [][] gHeaders = new String[geneList.size()][4];
		int headerNumber = 0;
		for (String nodeName: geneList) {
			gHeaders[headerNumber][0] = nodeName;
			gHeaders[headerNumber][1] = nodeName;
			gHeaders[headerNumber][2] = nodeName;
			gHeaders[headerNumber++][3] = "1.0";
		}
		setGenePrefix(new String[] {"GID", "NODE","ORF","GWEIGHT"});
		setGeneHeaders(gHeaders);


		// Array annotations are the list of attributes we used (note: order matters)
		List<String>arrayList = network.getRow(network).getList(ClusterManager.ARRAY_ORDER_ATTRIBUTE, String.class);
		String [][] aHeaders = new String[arrayList.size()][2];
		headerNumber = 0;
		for (String attribute: arrayList) {
			aHeaders[headerNumber][0] = attribute;
			aHeaders[headerNumber++][1] = "1.0";
		}
		setArrayPrefix(new String[] {"AID", "EWEIGHT"});
		setArrayHeaders(aHeaders);

		int nGene = geneList.size();
		int nExpr = arrayList.size();
		// The CDT is the Gene x Array matrix
		double[] exprData = new double[nGene * nExpr];

		// Check for a symmetrical matrix
		if (geneList.get(0).equals(arrayList.get(0))) {

			// Matrix is symmetrical.  Get the edge attribute
			String attribute = network.getRow(network).get(ClusterManager.CLUSTER_EDGE_ATTRIBUTE, String.class);
			attribute = attribute.substring(5);
			// System.out.println("Edge attribute is "+attribute);

			// Initialize the data
			for (int row = 0; row < nGene; row++) {
				for (int col = 0; col < nExpr; col++) {
					int cell = row * nExpr + col;
					if (diagonalValue != null && row == col)
						exprData[cell] = diagonalValue;
					else if (zeroMissing)
						exprData[cell] = 0.0f;
					else
						exprData[cell] = DataModel.NODATA;
				}
			}

			Class attributeType = network.getDefaultEdgeTable().getColumn(attribute).getType();
			for (CyEdge edge: network.getEdgeList()) {
				CyNode source = edge.getSource();
				CyNode target = edge.getTarget();
				if (!geneList.contains(ModelUtils.getName(network, source)) || 
				    !geneList.contains(ModelUtils.getName(network, target)))
					continue;

				Double val = ModelUtils.getNumericValue(network, edge, attribute);

				int gene = geneList.indexOf(ModelUtils.getName(network, source));
				int expr = geneList.indexOf(ModelUtils.getName(network, target));
				// System.out.println("Edge "+source.getIdentifier()+"("+gene+") "+
				//                    target.getIdentifier()+"("+expr+") = "+val);
				if (val != null) {
					exprData[gene*nExpr + expr] = val;
					exprData[expr*nExpr + gene] = val;
				}
			}
			isSymmetrical = true;
		} else {
			// Get the data
			int gene = 0;
			for (String nodeName: geneList) {
				CyNode node = (CyNode)ModelUtils.getNetworkObjectWithName(network, nodeName, CyNode.class);
				int expr = 0;
				for (String attribute: arrayList) {
					Double val = ModelUtils.getNumericValue(network, node, attribute);

					if (val == null) {
						exprData[gene*nExpr + expr] = DataModel.NODATA;
					} else {
						exprData[gene*nExpr + expr] = val.doubleValue();
					}
					expr++;
				}
				gene++;
			}
		}

		setExprData(exprData);
		hashGIDs();
		hashAIDs();

		// Now, get the gene tree results (GTR) or array tree results (ATR) from Cytoscape, depending on
		// what we clustered.  Note that the way we put this together really depends on whether we've done
		// a som, kmeans, or hierarchical cluster

		if (ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_NODE_ATTRIBUTE)) {
			System.out.println("ClusterNodeAttribute: "+ClusterManager.CLUSTER_NODE_ATTRIBUTE);
			List<String>groupList = network.getRow(network).getList(ClusterManager.CLUSTER_NODE_ATTRIBUTE, String.class);
			System.out.println("Size of grouplist: "+groupList.size());
			setGtrPrefix(getClusterHeaders());
			String [][] gtrHeaders = new String[groupList.size()][getClusterHeaders().length];

			parseGroupHeaders(groupList, gtrHeaders);

			setGtrHeaders(gtrHeaders);
			hashGTRs();
			gidFound(true);
		}

		// If we're not a gene cluster, we need to transpose the matrix
		// when we save it
		if (ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_ATTR_ATTRIBUTE)) {
			List<String>groupList = network.getRow(network).getList(ClusterManager.CLUSTER_ATTR_ATTRIBUTE, String.class);
			setAtrPrefix(getClusterHeaders());
			String [][] atrHeaders = new String[groupList.size()][getClusterHeaders().length];

			parseGroupHeaders(groupList, atrHeaders);

			setAtrHeaders(atrHeaders);
			hashATRs();
			aidFound(true);
		}


		// We don't use weights
		setEweightFound(false);
		setGweightFound(false);
	}

	public String getName() {
		return network.getRow(network).get(CyNetwork.NAME, String.class)+" Clusters";
	}

	public String getSource() {
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}

	public boolean isSymmetrical() {
		return isSymmetrical;
	}

	public ClusterManager getClusterManager() {
		return clusterManager;
	}

	protected String[] getClusterHeaders() {
		return new String [] {"NODEID", "LEFT", "RIGHT", "CORRELATION"};
	}

	private void parseGroupHeaders (List<String>groupList, String [][] headers) {

		// Parse the group data: format is NAME\tID1\tID2\tdistance
		int headerNumber = 0;
		int headerLength = headers[0].length;
		for (String group: groupList) {
			String[] tokens = group.split("[\t]");
			for (int t = 0; t < headerLength; t++) {
				headers[headerNumber][t] = tokens[t];
			}
			headerNumber++;
		}
	}
}
