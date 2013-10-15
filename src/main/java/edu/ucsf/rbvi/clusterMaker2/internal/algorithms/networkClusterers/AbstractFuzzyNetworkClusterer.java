package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableUtil;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.FuzzyNodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;

public abstract class AbstractFuzzyNetworkClusterer extends AbstractNetworkClusterer  {

	
	CyTableManager tableManager = null;
	private CyTableFactory tableFactory = null;
	
	public AbstractFuzzyNetworkClusterer(ClusterManager clusterManager) {
		super(clusterManager);
		tableManager = clusterManager.getTableManager();
		tableFactory = clusterManager.getTableFactory();
		// TODO Auto-generated constructor stub
	}
	

	/**
	 * Method creates a table to store the information about Fuzzy Clusters and adds it to the network
	 * 
	 * @param clusters List of FuzzyNodeCLusters, which have to be put in the table
	 * 
	 */
	
	protected void createFuzzyTable(List<FuzzyNodeCluster> clusters){
			
		CyTable networkTable = network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);
		CyTable FuzzyClusterTable = null;
		if(!CyTableUtil.getColumnNames(networkTable).contains(clusterAttributeName + "_Table.SUID")){
			
			network.getDefaultNetworkTable().createColumn(clusterAttributeName + "_Table.SUID", Long.class, false);
			FuzzyClusterTable = tableFactory.createTable(clusterAttributeName + "_Table", "Fuzzy_Node.SUID", Long.class, true, true);
			
		}
		else{
			long FuzzyClusterTableSUID = network.getRow(network).get(clusterAttributeName + "_Table.SUID", Long.class);
			 FuzzyClusterTable = tableManager.getTable(FuzzyClusterTableSUID);
		}
	
		for(FuzzyNodeCluster cluster : clusters){
			if(FuzzyClusterTable.getColumn("Cluster_"+cluster.getClusterNumber()) == null){
				FuzzyClusterTable.createColumn("Cluster_"+cluster.getClusterNumber(), Double.class, false);
			}
		}
		
		
		CyRow TableRow;
		for(CyNode node: network.getNodeList()){
			TableRow = FuzzyClusterTable.getRow(node.getSUID());
			for(FuzzyNodeCluster cluster : clusters){
				TableRow.set("Cluster_"+cluster.getClusterNumber(), cluster.getMembership(node));
			}
		}
		
		network.getRow(network).set(clusterAttributeName + "_Table.SUID", FuzzyClusterTable.getSUID());
		tableManager.addTable(FuzzyClusterTable);			
		
	}
	

}
