package org.cytoscape.myapp.internal.algorithms;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTableUtil;
import org.omg.CORBA.Any;

import giny.model.Edge;

/**
 * This class calculates a number of cluster statistics on a set of
 * of node clusters.  The node clusters passed as a list of lists of
 * CyNodes, where each list of CyNodes represents a cluster.  Currently
 * the calculated statistics include:
 *	Number of clusters
 *	Average cluster size
 *	Maximum cluster size
 *	Minimum cluster size
 *	Cluster coefficient (intra-cluster edges / total edges)
 */

public class ClusterResults {
	
	private List<List<CyNode>> clusters;
	private CyNetwork network;
	private int clusterCount;
	private double averageSize;
	private int maxSize;
	private int minSize;
	private double clusterCoefficient;
	private double modularity;
	private String extraText = null;

	public ClusterResults(CyNetwork network, List<List<CyNode>> cl, String extraInformation) { 
		this.network = network;
		clusters = cl; 
		extraText = extraInformation;
		calculate();
	}

	public ClusterResults(CyNetwork network, List<List<CyNode>> cl) { 
		this(network,cl,null);
	}

	public String toString() {
		NumberFormat nf = NumberFormat.getInstance();
		String result = "  Clusters: "+clusterCount+"\n";
		result += "  Average size: "+nf.format(averageSize)+"\n";
		result += "  Maximum size: "+maxSize+"\n";
		result += "  Minimum size: "+minSize+"\n";
		result += "  Modularity: "+nf.format(modularity);
		if (extraText != null)
			result += "  "+extraText;
		return result;
	}

	public List<List<CyNode>> getClusters() {
		return clusters;
	}

	private void calculate() {
		clusterCount = clusters.size();
		averageSize = 0.0;
		maxSize = -1;
		minSize = Integer.MAX_VALUE;
		clusterCoefficient = 0.0;
		modularity = 0.0;
		double edgeCount = (double)network.getEdgeCount();

		int clusterNumber = 0;
		for (List<CyNode> cluster: clusters) {
			averageSize += (double)cluster.size() / (double)clusterCount;
			maxSize = Math.max(maxSize, cluster.size());
			minSize = Math.min(minSize, cluster.size());
			double innerEdges = getInnerEdgeCount(cluster);
			double outerEdges = getOuterEdgeCount(cluster);
			clusterCoefficient += (innerEdges / (innerEdges+outerEdges)) / (double)(clusterCount);

			double percentEdgesInCluster = innerEdges/edgeCount;
			double percentEdgesTouchingCluster = (innerEdges+outerEdges)/edgeCount;
			modularity += percentEdgesInCluster - percentEdgesTouchingCluster*percentEdgesTouchingCluster;
			clusterNumber++;
		}
	}

	private double getInnerEdgeCount(List<CyNode> cluster) {
		
		return (double) CyTableUtil.getEdgesInState(nodes,"connected",true).size(); //network.getConnectingEdges(cluster).size();
	}

	private double getOuterEdgeCount(List<CyNode> cluster) {
		// Get all of the inner edges
		List<Edge> innerEdges = network.getConnectingEdges(cluster);

		// Make a map out of the inner edges
		Map<Edge,Edge> edgeMap = new HashMap<Edge,Edge>();
		for (Edge edge: innerEdges) {
			edgeMap.put(edge, edge);
		}

		int outerCount = 0;
		for (CyNode node: cluster) {
			List<Edge> edges = network.getAdjacentEdgesList(node,"ANY");
			for (Edge edge: edges) {
				if (!edgeMap.containsKey(edge))
					outerCount++;
			}
		}
		return (double) outerCount;
	}


}
