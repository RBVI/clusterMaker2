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
			
    String tableSuidColumn = clusterAttributeName + "_Table.SUID";
    String fuzzyClusterTableName = clusterAttributeName + "_Table";
		CyTable networkTable = network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);
		CyTable fuzzyClusterTable = null;
		if(!CyTableUtil.getColumnNames(networkTable).contains(tableSuidColumn)) {
			networkTable.createColumn(tableSuidColumn, Long.class, false);
			fuzzyClusterTable = tableFactory.createTable(fuzzyClusterTableName, "Fuzzy_Node.SUID", Long.class, true, true);
		} else {
			Long fuzzyClusterTableSUID = networkTable.getRow(network.getSUID()).get(tableSuidColumn, Long.class);
      if (fuzzyClusterTableSUID == null) {
        fuzzyClusterTable = tableFactory.createTable(fuzzyClusterTableName, "Fuzzy_Node.SUID", Long.class, true, true);
      } else {
        fuzzyClusterTable = tableManager.getTable(fuzzyClusterTableSUID);
      }
		}

		for(FuzzyNodeCluster cluster : clusters){
			if(fuzzyClusterTable.getColumn("Cluster_"+cluster.getClusterNumber()) == null){
				fuzzyClusterTable.createColumn("Cluster_"+cluster.getClusterNumber(), Double.class, false);
			}
		}
		
		CyRow tableRow;
		for(CyNode node: network.getNodeList()){
			tableRow = fuzzyClusterTable.getRow(node.getSUID());
			for(FuzzyNodeCluster cluster : clusters){
				tableRow.set("Cluster_"+cluster.getClusterNumber(), cluster.getMembership(node));
			}
		}
		
		networkTable.getRow(network.getSUID()).set(tableSuidColumn, fuzzyClusterTable.getSUID());
		tableManager.addTable(fuzzyClusterTable);
	}
}
