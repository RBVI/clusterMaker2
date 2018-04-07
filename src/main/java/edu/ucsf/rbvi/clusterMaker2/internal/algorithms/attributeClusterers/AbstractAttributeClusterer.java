/* vim: set ts=2: */
/**
 * Copyright (c) 20118 The Regents of the University of California.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *   1. Redistributions of source code must retain the above copyright
 *	  notice, this list of conditions, and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above
 *	  copyright notice, this list of conditions, and the following
 *	  disclaimer in the documentation and/or other materials provided
 *	  with the distribution.
 *   3. Redistributions must acknowledge that this software was
 *	  originally developed by the UCSF Computer Graphics Laboratory
 *	  under support by the NIH National Center for Research Resources,
 *	  grant P41-RR01081.
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
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.swing.RequestsUIHelper;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.Silhouettes;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

/**
 * This abstract class is the base class for all of the attribute clusterers provided by
 * clusterMaker.  Fundamentally, an attribute clusterer is an algorithm which functions to
 * partition nodes or node attributes based on properties of the attributes.
 */
public abstract class AbstractAttributeClusterer extends AbstractClusterAlgorithm
                                                 implements RequestsUIHelper {
	// Common instance variables
	protected DistanceMetric distanceMetric = DistanceMetric.EUCLIDEAN;
	protected List<String>attrList;

	public AbstractAttributeClusterer(ClusterManager clusterManager) {
		super(clusterManager);
	}

	/**
	 * Default methods for JSON returns from attribute cluster algorithms.
	 */
	public static String getExampleJSON() {
		String strRes = "{\"nodeCluster\":";
		strRes += "{\"silhouette\":2.2,\"order\":[\"EGFR\",\"BRCA1\"],"+
		          "\"clusters\": ["+
							"{\"clusterNumber\": 1, \"members\": [\"EGFR\", \"BRCA1\"]}"+
							"{\"clusterNumber\": 2, \"members\": [\"EGFR\", \"BRCA1\"]}]}";
		strRes += "{\"attributeCluster\":";
		strRes += "{\"silhouette\":2.2,\"order\":[\"Column 1\",\"Column 2\"],"+
		          "\"clusters\": ["+
							"{\"clusterNumber\": 1, \"members\": [\"Column 1\", \"Column 2\"]}"+
							"{\"clusterNumber\": 2, \"members\": [\"Column 1\", \"Column 2\"]}]}}";
		return strRes;
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class, Map.class, String.class);
	}

	public static JSONResult getJSONResults(List<String> nodeOrder, 
	                                        List<String> nodeList,
	                                        Silhouettes nodeSil,
	                                        List<String> attributeOrder, 
	                                        List<String> attributeList,
	                                        Silhouettes attributeSil) {
		JSONResult res = () -> {
			String strRes = "{";
			strRes += "\"nodeCluster\":"+jsonCluster(nodeOrder, nodeList, nodeSil);
			if (attributeOrder != null && attributeList != null) {
				strRes += ",\"attributeCluster\":"+jsonCluster(attributeOrder, attributeList, attributeSil);
			}
			strRes += "}";
			return strRes;
		};
		return res;
	}

	public static String getStringResults(List<String> nodeOrder, 
	                                      List<String> nodeList,
	                                      Silhouettes nodeSil,
	                                      List<String> attributeOrder, 
	                                      List<String> attributeList,
	                                      Silhouettes attributeSil) {
		String strRes = "Node Clusters: \n";
		strRes += getStringResults(nodeOrder, nodeList, nodeSil);
		if (attributeOrder != null && attributeList != null) {
			strRes = "Attribute Clusters: \n";
			strRes += getStringResults(attributeOrder, attributeList, attributeSil);
		}
		return strRes;
	}

	protected static List<String> getOrder(Integer[] rowOrder, CyMatrix matrix) {
		List<String> orderList = new ArrayList<String>();
		for (int i = 0; i < rowOrder.length; i++) {
			orderList.add(matrix.getRowLabel(rowOrder[i]));
		}
		return orderList;
	}
	
	private static String getStringResults(List<String> order, List<String> list, Silhouettes sil) {
		String strRes = "";
		if (sil != null)
			strRes += "   silhouette: "+sil.getMean()+"\n";
		strRes += "   order: ";
		strRes += order.get(0);
		for (int i = 1; i < order.size(); i++) {
			strRes += ","+order.get(i);
		}
		strRes += "\n";
		if (list != null) {
			strRes += "   clusters:\n";
			Map<Integer, List<String>> clusterMap = makeClusterMap(list);
			for (Integer cluster: clusterMap.keySet()) {
				strRes += "        "+cluster+": ";
				List<String> members = clusterMap.get(cluster);
				strRes += members.get(0);
				for (int i = 1; i < members.size(); i++)
					strRes += ","+members.get(i);
				strRes += "\n";
			}
		}
		return strRes;
	}

	public static Map<String, List<String>> getMapResults(List<String> nodeOrder, 
	                                                      List<String> nodeList,
	                                                      Silhouettes nodeSil,
	                                                      List<String> attributeOrder, 
	                                                      List<String> attributeList,
	                                                      Silhouettes attributeSil) {
		Map<String, List<String>> map = new HashMap<>();
		map.put("nodeOrder", nodeOrder);
		map.put("nodeList", nodeList);
		if (nodeSil != null)
			map.put("nodeSil", Collections.singletonList(Double.toString(nodeSil.getMean())));
		if (attributeOrder != null)
			map.put("attributeOrder", attributeOrder);
		if (attributeList != null)
			map.put("attributeList", attributeList);
		if (attributeSil != null)
			map.put("attributeSil", Collections.singletonList(Double.toString(attributeSil.getMean())));
		return map;
	}

	protected void updateKEstimates(CyNetwork network) {
	}

	private static String jsonCluster(List<String> order, List<String> attrList, Silhouettes sil) {
		Map<Integer, List<String>> clusterMap = makeClusterMap(attrList);

		String strRes = "{";
		if (sil != null)
			strRes += "\"silhouette\": "+sil.getMean()+",";
		strRes += "\"order\":";
		strRes += "[";
		strRes += "\""+order.get(0)+"\"";
		for (int i = 1; i < order.size(); i++) {
			strRes += ",\""+order.get(i)+"\"";
		}
		strRes += "]";
		if (clusterMap.size() > 0) {
			strRes += ",\"clusters\":";
			strRes += "[";
			strRes += getCluster(1, clusterMap.get(1));
			for (int i = 2; i < clusterMap.size(); i++) {
				strRes += ","+getCluster(i, clusterMap.get(i));
			}
			strRes += "]";
		}
		strRes += "}";
		return strRes;
	}

	private static Map<Integer, List<String>> makeClusterMap(List<String> attrList) {
		Map<Integer, List<String>> clusterMap = new HashMap<>();
		for (String str: attrList) {
			String[] parts = str.split("\t");
			Integer cluster = Integer.parseInt(parts[1]);
			if (!clusterMap.containsKey(cluster))
				clusterMap.put(cluster, new ArrayList<String>());
			clusterMap.get(cluster).add(parts[0]);
		}
		return clusterMap;
	}

	private static String getCluster(int cluster, List<String> members) {
		if (members == null) 
			System.out.println("Cluster: "+cluster+" has no members!");
		System.out.println("Cluster: "+cluster+" has "+members.size()+" members");
		String strRes = "{ \"clusterNumber\": "+cluster+",";
		strRes += "\"members\": [";
		System.out.println("members.get(0) = "+members.get(0));
		strRes += "\""+members.get(0)+"\"";
		for (int i = 1; i < members.size(); i++) {
			strRes += ",\""+members.get(i)+"\"";
		}
		strRes += "]}";
		return strRes;
	}

	/**
	 * This method resets (clears) all of the existing network attributes.
	 */
	@SuppressWarnings("unchecked")
	protected void resetAttributes(CyNetwork network, String group_attr) {

		// Remove the attributes that are lingering
		if (ModelUtils.hasAttributeLocal(network, network, ClusterManager.ARRAY_ORDER_ATTRIBUTE))
			ModelUtils.deleteAttributeLocal(network, network, ClusterManager.ARRAY_ORDER_ATTRIBUTE);
		if (ModelUtils.hasAttributeLocal(network, network, ClusterManager.NODE_ORDER_ATTRIBUTE))
			ModelUtils.deleteAttributeLocal(network, network, ClusterManager.NODE_ORDER_ATTRIBUTE);
		if (ModelUtils.hasAttributeLocal(network, network, ClusterManager.CLUSTER_ATTR_ATTRIBUTE))
			ModelUtils.deleteAttributeLocal(network, network, ClusterManager.CLUSTER_ATTR_ATTRIBUTE);
		if (ModelUtils.hasAttributeLocal(network, network, ClusterManager.CLUSTER_NODE_ATTRIBUTE))
			ModelUtils.deleteAttributeLocal(network, network, ClusterManager.CLUSTER_NODE_ATTRIBUTE);
		if (ModelUtils.hasAttributeLocal(network, network, ClusterManager.CLUSTER_EDGE_ATTRIBUTE))
			ModelUtils.deleteAttributeLocal(network, network, ClusterManager.CLUSTER_EDGE_ATTRIBUTE);
		if (ModelUtils.hasAttributeLocal(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE))
			ModelUtils.deleteAttributeLocal(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE);
		if (ModelUtils.hasAttributeLocal(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE))
			ModelUtils.deleteAttributeLocal(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE);

		// See if we have any old groups in this network
		if (ModelUtils.hasAttributeLocal(network, network, group_attr)) {
			List<String>clList = network.getRow(network).getList(group_attr, String.class);
			ModelUtils.deleteAttributeLocal(network, network, group_attr);
		}
	}

	protected void updateAttributes(CyNetwork network, String cluster_type, Integer[] rowOrder,
	                                String weightAttributes[], List<String> attrList,
	                                CyMatrix matrix) {

		if (cancelled) return;

		ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE,
				cluster_type, String.class, null);

				// System.out.println("Matrix: "+matrix.printMatrixInfo());
		if (matrix.isTransposed()) {
			ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_ATTR_ATTRIBUTE,
					attrList, List.class, String.class);
		} else {
			// System.out.println("attrList's size: " + attrList.size());
			ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_NODE_ATTRIBUTE,
					attrList, List.class, String.class);
			if (matrix.isSymmetrical() || matrix.isAssymetricalEdge()) {
				ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_EDGE_ATTRIBUTE,
						weightAttributes[0], String.class, null);
			}
			if (matrix.isSymmetrical()) {
				ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_ATTR_ATTRIBUTE,
			 		   attrList, List.class, String.class);
						}
		}

		String[] rowArray = matrix.getRowLabels();
		ArrayList<String> orderList = new ArrayList<String>();

		String[] columnArray = matrix.getColumnLabels();
		ArrayList<String>columnList = new ArrayList<String>(columnArray.length);

		for (int i = 0; i < rowOrder.length; i++) {
			orderList.add(rowArray[rowOrder[i]]);
			if (matrix.isSymmetrical())
				columnList.add(rowArray[rowOrder[i]]);
		}

		if (!matrix.isSymmetrical()) {
			for (int col = 0; col < columnArray.length; col++) {
				columnList.add(columnArray[col]);
			}
		}

		if (matrix.isTransposed()) {
			// We did an Array cluster -- output the calculated array order
			// and the actual node order
			// netAttr.setListAttribute(netID, ClusterManager.ARRAY_ORDER_ATTRIBUTE, orderList);
			ModelUtils.createAndSetLocal(network, network, ClusterManager.ARRAY_ORDER_ATTRIBUTE,
					orderList, List.class, String.class);

			// Don't override the columnlist if a node order already exists
			if (!ModelUtils.hasAttributeLocal(network, network, ClusterManager.NODE_ORDER_ATTRIBUTE))
				ModelUtils.createAndSetLocal(network, network, ClusterManager.NODE_ORDER_ATTRIBUTE,
						columnList, List.class, String.class);
		} else {
			ModelUtils.createAndSetLocal(network, network, ClusterManager.NODE_ORDER_ATTRIBUTE,
					orderList, List.class, String.class);
						// System.out.println("orderList.size() = "+orderList.size());
			// Don't override the columnlist if a node order already exists
			if (!ModelUtils.hasAttributeLocal(network, network, ClusterManager.ARRAY_ORDER_ATTRIBUTE))
				ModelUtils.createAndSetLocal(network, network, ClusterManager.ARRAY_ORDER_ATTRIBUTE,
						columnList, List.class, String.class);
		}

	}

	protected void updateParams(CyNetwork network, List<String> params) {
		if (cancelled) return;

		ModelUtils.createAndSetLocal(network, network, ClusterManager.CLUSTER_PARAMS_ATTRIBUTE,
				params, List.class, String.class);
	}

	/**
	 * This protected method is called to create all of our groups (if desired).
	 * It is used by all of the k-clustering algorithms.
	 *
	 * @param nClusters the number of clusters we created
	 * @param cluster the list of values and the assigned clusters
	 */

	protected void createGroups(CyNetwork net,CyMatrix matrix,int nClusters, int[] clusters, String algorithm) {
		if (matrix.isTransposed()) {
			return;
		}

		network = net;
		if (monitor != null)
			monitor.setStatusMessage("Creating groups");

		attrList = new ArrayList<String>(matrix.nRows());
		// Create the attribute list
		for (int cluster = 0; cluster < nClusters; cluster++) {
			List<CyNode> memberList = new ArrayList<CyNode>();
			for (int i = 0; i < matrix.nRows(); i++) {
				if (clusters[i] == cluster) {
					// System.out.println("Setting cluster # for node "+matrix.getRowLabel(i)+"("+i+") to "+cluster);
					attrList.add(matrix.getRowLabel(i)+"\t"+cluster);
					memberList.add(matrix.getRowNode(i));
					ModelUtils.createAndSetLocal(network, matrix.getRowNode(i), algorithm+" Cluster", cluster, Integer.class, null);
				}
			}
			if (createGroups) {
				// System.out.println("Creating group: Cluster_"+cluster+" with "+memberList.size()+" nodes");
				CyGroup group = clusterManager.createGroup(network, "Cluster_"+cluster, memberList, null, true);
			}
		}
	}

	public List<String> getAttributeList() { return attrList; }
}
