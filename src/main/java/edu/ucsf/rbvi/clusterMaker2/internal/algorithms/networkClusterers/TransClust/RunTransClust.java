package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes.Edges;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.iterativeclustering.IteratorThread;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

public class RunTransClust {

	private List<CyNode> nodes;
	private boolean canceled = false;
	protected int clusterCount = 0;
	private DistanceMatrix distanceMatrix = null;
	private double threshold;

	public RunTransClust( DistanceMatrix dMat,double threshold, TaskMonitor monitor)
	{
		this.distanceMatrix = dMat;
		this.threshold = threshold;
	}
	
	public void cancel () { canceled = true; }

	public List<NodeCluster> run(TaskMonitor monitor, CyNetwork network)
	{
		DoubleMatrix2D matrix = this.distanceMatrix.getDistanceMatrix(threshold, true);

		nodes = distanceMatrix.getNodes();
		HashMap<String, CyNode> nodeHash = new HashMap<String, CyNode>();
		for (CyNode node : nodes) {
			nodeHash.put(ModelUtils.getNodeName(network, node), node);
		}

		HashMap<String,Integer> integers2proteins = new HashMap<String, Integer>();
		HashMap<Integer,String>  proteins2integers = new HashMap<Integer, String>();
		int count = 0;
		for (CyNode node : this.nodes) {
			integers2proteins.put(ModelUtils.getNodeName(network, node), count);
			proteins2integers.put(count, ModelUtils.getNodeName(network, node));
			count++;
		}
		
		Edges es = new Edges(this.nodes.size()*this.nodes.size(), this.nodes.size());
		count = 0;
		for (int i = 0; i < this.nodes.size(); i++) {
			CyNode cyNodeI = this.nodes.get(i);
			es.startPositions[integers2proteins.get(cyNodeI.getSUID())] = count;
			for (int j = 0; j < this.nodes.size(); j++) {
				CyNode cyNodeJ = this.nodes.get(j);
					es.sources[count] = i;
					es.targets[count] = j;
					es.values[count] = (float) distanceMatrix.getEdgeValueFromMatrix(i, j);
					count++;
			}
			es.endPositions[integers2proteins.get(cyNodeI.getSUID())] = count-1;
		}
		
		Semaphore s = new Semaphore(1);
		TaskConfig.mode = TaskConfig.COMPARISON_MODE;
		TaskConfig.monitor = monitor;
		IteratorThread it = new IteratorThread(es,integers2proteins,proteins2integers,s);
		TaskConfig.minThreshold = threshold;
		TaskConfig.maxThreshold = threshold;
		try {
			s.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		it.start();
		monitor.showMessage(TaskMonitor.Level.INFO,"Executing TransClust Clustering...");
		
		try {
			s.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		monitor.showMessage(TaskMonitor.Level.INFO,"Assigning nodes to clusters");

		String result = it.resultsStringBuffer.toString();
		String clusters[] = result.split("\t")[2].split(";");
		
		
		Map<Integer, NodeCluster> clusterMap = getClusterMap(clusters,nodeHash);

		
		//Update node attributes in network to include clusters. Create cygroups from clustered nodes
		monitor.showMessage(TaskMonitor.Level.INFO,"Created "+clusterMap.size()+" clusters");
	       
		if (clusterCount == 0) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Created 0 clusters!!!!");
			return null;
		}

		int clusterNumber = 1;
		Map<NodeCluster,NodeCluster> cMap = new HashMap();
		for (NodeCluster cluster: NodeCluster.sortMap(clusterMap)) {
			if (cMap.containsKey(cluster))
				continue;

			cMap.put(cluster,cluster);

			cluster.setClusterNumber(clusterNumber);
			clusterNumber++;
		}

		Set<NodeCluster>clusters2 = cMap.keySet();
		return new ArrayList<NodeCluster>(clusters2);
	}
	
private Map<Integer, NodeCluster> getClusterMap(String[] clusters, HashMap<String, CyNode> nodeHash){
	    
		HashMap<Integer, NodeCluster> clusterMap = new HashMap<Integer, NodeCluster>();
		
		for (int i = 0; i < clusters.length; i++) {
			String elements[] = clusters[i].split(",");
			NodeCluster nc = new NodeCluster();
			for (int j = 0; j < elements.length; j++) {
				if(nodeHash.containsKey(elements[j].trim())){
					nc.add(nodeHash.get(elements[j].trim()));	
				}
			}
			clusterCount++;
			updateClusters(nc, clusterMap);
		}
		return clusterMap;
	}

	private void updateClusters(NodeCluster cluster, Map<Integer, NodeCluster> clusterMap) {
		for (CyNode node: cluster) {
			clusterMap.put(nodes.indexOf(node), cluster);
		}
	}
	
}
