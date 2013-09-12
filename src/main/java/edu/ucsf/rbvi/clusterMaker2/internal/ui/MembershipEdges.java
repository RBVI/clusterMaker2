package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ViewUtils;

/**
 * The class adds membership edges from nodes in clusters to the cluster centroid.
 * The cluster centroid's location is calculated using the mean of coordinates of the nodes in the cluster. 
 * @author Abhiraj
 *
 */

public class MembershipEdges {
		
	private CyNetwork network = null;
	private CyNetworkView networkView = null;
	private CyTableManager tableManager = null;
	private ClusterManager manager;
	private CyTable FuzzyClusterTable = null;
	
	public MembershipEdges(CyNetwork network, CyNetworkView view, ClusterManager manager,CyTable FuzzyClusterTable){
		this.network = network;
		this.networkView = view;
		this.tableManager = manager.getTableManager();
		this.manager = manager;
		this.FuzzyClusterTable =  FuzzyClusterTable;
		
		createMembershipEdges();
	}
	
	private void createMembershipEdges(){
				
        List<List<CyNode>> clusterList = new ArrayList<List<CyNode>>(); // List of node lists
		//CyNetwork parentNetwork = manager.getNetwork();
		//Long FuzzyClusterTableSUID = parentNetwork.getRow(parentNetwork).get("FuzzyClusterTable.SUID", long.class);
		//CyTable FuzzyClusterTable = tableManager.getTable(FuzzyClusterTableSUID);
		
		int numC = FuzzyClusterTable.getColumns().size() - 1;
		for(int i = 0; i < numC; i++){
			clusterList.add(new ArrayList<CyNode>());
		}
		
		List<CyNode> nodeList = network.getNodeList();
		for (CyNode node : nodeList){
			CyRow nodeRow = FuzzyClusterTable.getRow(node.getSUID());
			for(int i = 1; i <= numC; i++){
				if(nodeRow.get("Cluster_"+ i, Double.class) != null){
					clusterList.get(i-1).add(node);
				}
			}			
		}
		
		for(List<CyNode> cluster :clusterList){
			CyNode centroid = network.addNode();
			
	        network.getRow(centroid).set(CyNetwork.NAME, "Centroid" + clusterList.indexOf(cluster) );
	        //System.out.println("Centroid SUID: " + centroid.getSUID());
	        //View<CyNode> nodeView = networkView.getNodeView(centroid);
	        double x = 0;
	        double y = 0;
	        double count = 0;
	        
	        for (CyNode node : cluster) {
	        	View<CyNode> nodeView = networkView.getNodeView(node);
	        	//System.out.println("NodeView SUID: " + nodeView.getSUID());
	        	x += nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
	        	y += nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
	        	//System.out.println(x);
	        	count += 1;
	        	//System.out.println("Read x = "+ x +", y = "+ y);
	        	network.addEdge(centroid, node, false);
	        	networkView.updateView();
	        }
	        
	        x = x/count;
	        y = y/count;
	        View<CyNode> centroidView = networkView.getNodeView(centroid);
	        //System.out.println("CentroidView SUID: " + centroidView.getSUID());
	        //System.out.println("x = "+ x +", y = "+ y);
	        centroidView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
	        centroidView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
	        
	        List<CyEdge> edgeList = network.getAdjacentEdgeList(centroid, CyEdge.Type.ANY); 
	        	        
	        membershipEdgeStyle(clusterList.indexOf(cluster)+1, edgeList, FuzzyClusterTable);
	        
	        networkView.updateView();
	        
		}
		
	}
	
	private void membershipEdgeStyle(int cNum, List<CyEdge> edgeList, CyTable FuzzyClusterTable){
		for (CyEdge edge : edgeList){
			View<CyEdge> edgeView = networkView.getEdgeView(edge);
			CyNode node = edge.getTarget();
			//edgeView.setVisualProperty(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.DASH_DOT);
			edgeView.setLockedValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.DASH_DOT);

			edgeView.setLockedValue(BasicVisualLexicon.EDGE_TRANSPARENCY, (int)(FuzzyClusterTable.getRow(node.getSUID()).get("Cluster_"+ cNum, Double.class)*255));
			network.getDefaultEdgeTable().getRow(edge).set("Membership_%", 100*(FuzzyClusterTable.getRow(node.getSUID()).get("Cluster_"+ cNum, Double.class)));
		}
		
	}

}
