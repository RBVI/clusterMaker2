package edu.ucsf.rbvi.clusterMaker2.internal.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTableUtil;

public class ModelUtils {

	public static List<CyEdge> getConnectingEdges(CyNetwork network, List<CyNode> nodes) {
		List<CyEdge> edgeList = new ArrayList<CyEdge>();
		for (int rowIndex = 0; rowIndex < nodes.size(); rowIndex++) {
			for (int colIndex = rowIndex; colIndex < nodes.size(); colIndex++) {
				List<CyEdge> connectingEdges = network.getConnectingEdgeList(nodes.get(rowIndex), nodes.get(colIndex), CyEdge.Type.ANY);
				if (connectingEdges != null) edgeList.addAll(connectingEdges);
			}
		}
		return edgeList;
	}

	public static String getNodeName(CyNetwork network, CyNode node) {
		return network.getRow(node).get(CyNetwork.NAME, String.class);
	}

	public static String getNetworkName(CyNetwork network) {
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}

}
