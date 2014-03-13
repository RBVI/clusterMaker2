package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCODE;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * * Copyright (c) 2004 Memorial Sloan-Kettering Cancer Center
 * *
 * * Code written by: Gary Bader
 * * Authors: Gary Bader, Ethan Cerami, Chris Sander
 * *
 * * This library is free software; you can redistribute it and/or modify it
 * * under the terms of the GNU Lesser General Public License as published
 * * by the Free Software Foundation; either version 2.1 of the License, or
 * * any later version.
 * *
 * * This library is distributed in the hope that it will be useful, but
 * * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * * documentation provided hereunder is on an "as is" basis, and
 * * Memorial Sloan-Kettering Cancer Center
 * * has no obligations to provide maintenance, support,
 * * updates, enhancements or modifications.  In no event shall the
 * * Memorial Sloan-Kettering Cancer Center
 * * be liable to any party for direct, indirect, special,
 * * incidental or consequential damages, including lost profits, arising
 * * out of the use of this software and its documentation, even if
 * * Memorial Sloan-Kettering Cancer Center
 * * has been advised of the possibility of such damage.  See
 * * the GNU Lesser General Public License for more details.
 * *
 * * You should have received a copy of the GNU Lesser General Public License
 * * along with this library; if not, write to the Free Software Foundation,
 * * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * User: Vuk Pavlovic
 * Date: Nov 29, 2006
 * Time: 5:34:46 PM
 * Description: Stores various cluster information for simple get/set purposes
 */

/**
 * Stores various cluster information for simple get/set purposes.
 */
public class MCODEClusterObj {
	private List<CyNode> alCluster = null;
	private CyNetwork gpCluster = null;
	private CyNode seedNode;
	private Map<CyNode, Boolean> nodeSeenHashMap; //stores the nodes that have already been included in higher ranking clusters
	private double clusterScore;
	private String clusterName; //Pretty much unsed so far, but could store name by user's input
	private int rank;
	private String resultTitle;

	public MCODEClusterObj() {}

	public String getResultTitle() {
		return resultTitle;
	}

	public void setResultTitle(String resultTitle) {
		this.resultTitle = resultTitle;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public double getClusterScore() {
		return clusterScore;
	}

	public void setClusterScore(double clusterScore) {
		this.clusterScore = clusterScore;
	}

	public CyNetwork getGPCluster() {
		return gpCluster;
	}

	public void setGPCluster(CyNetwork gpCluster) {
		this.gpCluster = gpCluster;
	}

	public List<CyNode> getALCluster() {
		return alCluster;
	}

	public void setALCluster(List<CyNode> alCluster) {
		this.alCluster = alCluster;
	}

	public CyNode getSeedNode() {
		return seedNode;
	}

	public void setSeedNode(CyNode seedNode) {
		this.seedNode = seedNode;
	}

	public Map<CyNode, Boolean> getNodeSeenHashMap() {
		return nodeSeenHashMap;
	}

	public void setNodeSeenHashMap(Map<CyNode, Boolean> nodeSeenHashMap) {
		this.nodeSeenHashMap = nodeSeenHashMap;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
		this.clusterName = "Cluster " + (rank + 1);
	}

	public NodeCluster getNodeCluster() {
		NodeCluster result = new NodeCluster();
		List nodes = gpCluster.getNodeList();
		result.addAll(nodes);
		result.setClusterScore(clusterScore);
		return result;
	}

	
}
