package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;

/**
 * The class adds membership edges from nodes in clusters to the cluster centroid.
 * The cluster centroid's location is calculated using the mean of coordinates of the nodes in the cluster. 
 * @author Abhiraj
 *
 */

public class MembershipEdges {
		
	public CyNetwork network = null;
	public CyNetworkView networkView = null;
	public CyTableManager tableManager = null;
	
	public MembershipEdges(ClusterManager clusterManager){
		this.network = clusterManager.getNetwork();
		this.networkView = clusterManager.getNetworkView();
		this.tableManager = clusterManager.getTableManager();
		
		createMembershipEdges();
	}
	
	private void createMembershipEdges(){
				
        List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>(); // List of node lists
		
		Long FuzzyClusterTableSUID = network.getRow(network).get("FuzzyClusterTable.SUID", long.class);
		CyTable FuzzyClusterTable = tableManager.getTable(FuzzyClusterTableSUID);
		
		int numC = FuzzyClusterTable.getColumns().size() - 1;
		for(int i = 0; i < numC; i++){
			clusterList.add(new ArrayList<CyNode>());
		}
		
		List<CyNode> nodeList = network.getNodeList();
		for (CyNode node : nodeList){
			CyRow nodeRow = FuzzyClusterTable.getRow(node);
			for(int i = 0; i < numC; i++){
				if(nodeRow.get("Cluster_"+ i, double.class) != null){
					clusterList.get(i).add(node);
				}
			}			
		}
		
		for(List<CyNode> cluster :clusterList){
			CyNode centroid = network.addNode();
			
	        network.getRow(centroid).set(CyNetwork.NAME, "Centroid" + clusterList.indexOf(cluster) );
	        View<CyNode> nodeView = networkView.getNodeView(centroid);
	        double x = 0;
	        double y = 0;
	        double count = 0;
	        
	        for (CyNode node : cluster) {
	        	nodeView = networkView.getNodeView(node);
	        	x += nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
	        	y += nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
	        	System.out.println(x);
	        	count += 1;
	        	
	        	network.addEdge(centroid, node, true);        	
	        }
	        
	        x = x/count;
	        y = y/count;
	        View<CyNode> centroidView = networkView.getNodeView(centroid);
	        
	        centroidView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
	        centroidView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
	                       
	        networkView.updateView();
	        
		}
		
	}

}
