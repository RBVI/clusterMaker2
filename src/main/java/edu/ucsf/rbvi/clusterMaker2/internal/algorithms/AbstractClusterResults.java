package edu.ucsf.rbvi.clusterMaker2.internal.algorithms;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTableUtil;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

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

public class AbstractClusterResults implements ClusterResults {
	
	private List<List<CyNode>> clusters;
	private CyNetwork network;
	private int clusterCount;
	private double averageSize;
	private int maxSize;
	private int minSize;
	private double clusterCoefficient;
	private double modularity;
	private String extraText = null;

	public AbstractClusterResults(CyNetwork network, List<List<CyNode>> cl, String extraInformation) { 
		this.network = network;
		clusters = cl; 
		extraText = extraInformation;
		calculate();
	}

	public AbstractClusterResults(CyNetwork network, List<List<CyNode>> cl) { 
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

	public double getScore() { return modularity; }

	public List<List<CyNode>> getClusters() {
		return clusters;
	}

	public Object getResults(Class requestedType) {
		if (requestedType.equals(String.class))
			return toString();
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
			double innerEdges = (double)getInnerEdgeCount(cluster);
			double outerEdges = (double)getOuterEdgeCount(cluster);
			clusterCoefficient += (innerEdges / (innerEdges+outerEdges)) / (double)(clusterCount);

			double percentEdgesInCluster = innerEdges/edgeCount;
			double percentEdgesTouchingCluster = (innerEdges+outerEdges)/edgeCount;
			modularity += percentEdgesInCluster - percentEdgesTouchingCluster*percentEdgesTouchingCluster;
			clusterNumber++;
		}
	}

	private int getInnerEdgeCount(List<CyNode> cluster) {
		return ModelUtils.getConnectingEdges(network, cluster).size();
	}

	private int getOuterEdgeCount(List<CyNode> cluster) {
		Set<CyEdge> innerEdgeSet = new HashSet<CyEdge>(ModelUtils.getConnectingEdges(network, cluster));
		List<CyEdge> outerEdges = new ArrayList<CyEdge>();
		for (CyNode node: cluster) {
			for (CyEdge edge: network.getAdjacentEdgeList(node, CyEdge.Type.ANY)) {
				if (!innerEdgeSet.contains(edge))
					outerEdges.add(edge);
			}
		}
		return outerEdges.size();
	}

}
