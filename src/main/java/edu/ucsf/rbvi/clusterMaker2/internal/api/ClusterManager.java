package edu.ucsf.rbvi.clusterMaker2.internal.api;

import edu.ucsf.rbvi.clusterMaker2.internal.ui.RankingResults;
import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.model.CyNetworkView;

import edu.ucsf.rbvi.clusterMaker2.internal.ui.ResultsPanel;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

public interface ClusterManager {
	String MATRIX_ATTRIBUTE = "__distanceMatrix";
	String CLUSTER_NODE_ATTRIBUTE = "__nodeClusters";
	String CLUSTER_ATTR_ATTRIBUTE = "__attrClusters";
	String CLUSTER_EDGE_ATTRIBUTE = "__clusterEdgeWeight";
	String NODE_ORDER_ATTRIBUTE = "__nodeOrder";
	String ARRAY_ORDER_ATTRIBUTE = "__arrayOrder";
	String CLUSTER_TYPE_ATTRIBUTE = "__clusterType";
	String CLUSTER_ATTRIBUTE = "__clusterAttribute";
	String CLUSTER_PARAMS_ATTRIBUTE = "__clusterParams";
	String RANKING_ATTRIBUTE = "__rankingAttribute";


	// Returns the list of cluster algorithms
	Collection<ClusterTaskFactory> getAllAlgorithms();
	ClusterTaskFactory getAlgorithm(String name);
	Collection<ClusterVizFactory> getAllVisualizers();
	ClusterVizFactory getVisualizer(String name);

	// Add a new algorithm.  This is usually done through listening to
	// osgi registration, but could also be done manually
	void addAlgorithm(ClusterTaskFactory alg);
	void removeAlgorithm(ClusterTaskFactory alg);
	void addVisualizer(ClusterVizFactory alg);
	void removeVisualizer(ClusterVizFactory alg);

	CyNetwork getNetwork();
	CyNetworkView getNetworkView();
	CyTableFactory getTableFactory();
	CyTableManager getTableManager();
	CyGroup createGroup(CyNetwork network, String name, List<CyNode> nodeList, List<CyEdge> edgeList, boolean register);
	void removeGroup(CyNetwork network, Long suid);

	// getter and setter methods for RankingResults
	List<RankingResults> getRankingResults(CyNetwork network);
	void addRankingResults(CyNetwork network, RankingResults rankingResults);
	void removeRankingResults(CyNetwork network, RankingResults rankingResults);

	//getter and setter methods for ResultsPanel
	List<ResultsPanel> getResultsPanels(CyNetwork network);
	void addResultsPanel(CyNetwork network, ResultsPanel resultsPanel);
	void removeResultsPanel(CyNetwork network, ResultsPanel resultsPanel);

	<T> T getService(Class <? extends T> clazz);
	<T> T getService(Class<? extends T> clazz, String filter);

	// Use with caution.  If you register it, you need to unregister it!
	void registerService(Object service, Class<?> serviceClass, Properties props);
	void unregisterService(Object service, Class<?> serviceClass);
}

