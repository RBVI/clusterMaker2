package edu.ucsf.rbvi.clusterMaker2.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.ucsf.rbvi.clusterMaker2.internal.api.*;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;

import edu.ucsf.rbvi.clusterMaker2.internal.ui.NetworkSelectionLinker;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.ResultsPanel;

public class ClusterManagerImpl implements ClusterManager {
// public class ClusterManagerImpl {
	CyApplicationManager appMgr;
	CyServiceRegistrar serviceRegistrar;
	CyGroupFactory groupFactory;
	CyGroupManager groupMgr;
	Map<String, ClusterTaskFactory> algMap;
	Map<String, ClusterVizFactory> vizMap;
	Map<String, RankFactory> rankMap;
	CyTableFactory tableFactory;
	CyTableManager tableManager;
	Map<CyRootNetwork, NetworkSelectionLinker> linkedNetworks;
	double networkClusterIndex = 50.0;
	double attributeClusterIndex = 1.0;
	double filterIndex = 100.0;
        double pcaIndex = 150.0;
	double vizClusterIndex = 1.0;
	double rankingIndex = 1.0;
	Map<CyNetwork, List<ResultsPanel>> resultsPanelMap;

	public ClusterManagerImpl(CyApplicationManager appMgr, CyServiceRegistrar serviceRegistrar,
 	                          CyGroupFactory groupFactory, CyGroupManager groupMgr, 
	                          CyTableFactory tableFactory, CyTableManager tableManager) {
		this.appMgr = appMgr;
		this.serviceRegistrar = serviceRegistrar;
		this.groupFactory = groupFactory;
		this.groupMgr = groupMgr;
		this.algMap = new HashMap<String, ClusterTaskFactory>();
		this.vizMap = new HashMap<String, ClusterVizFactory>();
		this.rankMap = new HashMap<String, RankFactory>();
		this.resultsPanelMap = new HashMap<CyNetwork, List<ResultsPanel>>();
		this.tableFactory = tableFactory;
		this.tableManager = tableManager;
		this.linkedNetworks = new HashMap<CyRootNetwork, NetworkSelectionLinker>();
	}

	public Collection<ClusterTaskFactory> getAllAlgorithms() {
		return algMap.values();
	}

	public ClusterTaskFactory getAlgorithm(String name) {
        return algMap.get(name);
	}

	public void addClusterAlgorithm(ClusterTaskFactory alg, Map props) {
		addAlgorithm(alg);
	}

	public void addAlgorithm(ClusterTaskFactory alg) {
		algMap.put(alg.getShortName(), alg);

		// Get the type of clusterer (Attribute, Network, Filter, Attribute+Network)
		List<ClusterTaskFactory.ClusterType> clusterTypes = alg.getTypeList();

		// Create our wrapper and register the algorithm
		for (ClusterTaskFactory.ClusterType type: clusterTypes) {
			Properties props = new Properties();
			props.setProperty(COMMAND, alg.getShortName());
			props.setProperty(COMMAND_NAMESPACE, "cluster");
			props.setProperty(IN_MENU_BAR, "true");
			props.setProperty(TITLE, alg.getName());
			props.setProperty(PREFERRED_MENU, "Apps.clusterMaker");
			switch(type) {
			case NETWORK:
				networkClusterIndex += 1.0;
				props.setProperty(MENU_GRAVITY, ""+networkClusterIndex);
				break;

			case ATTRIBUTE:
				attributeClusterIndex += 1.0;
				props.setProperty(MENU_GRAVITY, ""+attributeClusterIndex);
				break;

			case FILTER:
				filterIndex += 1.0;
				props.setProperty(MENU_GRAVITY, ""+filterIndex);
				break;
                            
                        case PCA:
                                pcaIndex += 1.0;
                                props.setProperty(MENU_GRAVITY, ""+pcaIndex);
				break;
			}
			serviceRegistrar.registerService(alg, TaskFactory.class, props);
		}
	}

	public void removeClusterAlgorithm(ClusterTaskFactory alg, Map props) {
		removeAlgorithm(alg);
	}

	public void removeAlgorithm(ClusterTaskFactory alg) {
		if (algMap.containsKey(alg.getName()))
		 	algMap.remove(alg.getName());

		serviceRegistrar.unregisterService(alg, TaskFactory.class);
	}


	public Collection<ClusterVizFactory> getAllVisualizers() {
		return vizMap.values();
	}

	public ClusterVizFactory getVisualizer(String name) {
		if (vizMap.containsKey(name))
	 		return vizMap.get(name);
		return null;
	}

	// Find out a way to avoid this duplication??? ref method addRanking
	public void addVisualizer(ClusterVizFactory viz) {
		vizMap.put(viz.getName(), viz);

		// Create our wrapper and register the algorithm
		Properties props = new Properties();
		props.setProperty(COMMAND, viz.getName());
		props.setProperty(COMMAND_NAMESPACE, "clusterviz");
		props.setProperty(IN_MENU_BAR, "true");
		props.setProperty(PREFERRED_MENU, "Apps.clusterMaker Visualizations");
		props.setProperty(TITLE, viz.getName());
		vizClusterIndex += 1.0;
		props.setProperty(MENU_GRAVITY, ""+vizClusterIndex);
		serviceRegistrar.registerService(viz, TaskFactory.class, props);
	}

	public void addClusterVisualizer(ClusterVizFactory viz, Map props) {
		addVisualizer(viz);
	}

	public void removeVisualizer(ClusterVizFactory viz) {
		if (vizMap.containsKey(viz.getName()))
	 		vizMap.remove(viz.getName());
	}

	public void removeClusterVisualizer(ClusterVizFactory viz, Map props) {
		removeVisualizer(viz);
		serviceRegistrar.unregisterService(viz, TaskFactory.class);
	}

	// Check why we take this road
	public void addRankingAlgorithm(RankFactory ranking, Map props) {
		addRanking(ranking);
	}

	// Check why we take this road
	public void removeRankingAlgorithm(RankFactory ranking, Map props) {
		removeRanking(ranking);
		serviceRegistrar.unregisterService(ranking, TaskFactory.class);
	}

	// Find out a way to avoid this duplication???
	public void addRanking(RankFactory rankFactory) {
		rankMap.put(rankFactory.getName(), rankFactory);

		Properties props = new Properties();
		props.setProperty(COMMAND, rankFactory.getName());
		props.setProperty(COMMAND_NAMESPACE, "rankingcluster");
		props.setProperty(IN_MENU_BAR, "true");
		props.setProperty(PREFERRED_MENU, "Apps.clusterMaker Ranking");
		props.setProperty(TITLE, rankFactory.getName());
		rankingIndex += 1.0;
		props.setProperty(MENU_GRAVITY, ""+rankingIndex);
		serviceRegistrar.registerService(rankFactory, TaskFactory.class, props);
	}

	public void removeRanking(RankFactory rankFactory) {
		rankMap.remove(rankFactory.getName());
        serviceRegistrar.unregisterService(rankFactory, TaskFactory.class);
	}

	public CyNetwork getNetwork() {
		return appMgr.getCurrentNetwork();
	}

	public CyNetworkView getNetworkView() {
		return appMgr.getCurrentNetworkView();
	}
	
	public CyTableFactory getTableFactory(){
		return tableFactory;
	}
	
	public CyTableManager getTableManager(){
		return tableManager;
	}

	public CyGroup createGroup(CyNetwork network, String name, List<CyNode> nodeList, 
	                           List<CyEdge> edgeList, boolean registerGroup) {
		CyGroup group =  groupFactory.createGroup(network, nodeList, edgeList, registerGroup);
		if (group != null) {
			CyRootNetwork rootNetwork = ((CySubNetwork)network).getRootNetwork();
			// The name of the group node is the name of the group
			rootNetwork.getRow(group.getGroupNode()).set(CyNetwork.NAME, name);
			rootNetwork.getRow(group.getGroupNode(), CyRootNetwork.SHARED_ATTRS).set(CyRootNetwork.SHARED_NAME, name);
		}
		return group;
	}

	public void removeGroup(CyNetwork network, Long suid) {
		CyNode node = ((CySubNetwork)network).getRootNetwork().getNode(suid); // Make sure to get the node in the root network
		if (node == null)
			return;

		CyGroup group = groupMgr.getGroup(node, network);
		if (group == null) 
			return;

		// Remove the group from this network
		if (group.getNetworkSet() != null && group.getNetworkSet().size() > 1) {
			group.removeGroupFromNetwork(network);
			return;
		}

		groupMgr.destroyGroup(group);
	}

	public List<ResultsPanel> getResultsPanels(CyNetwork network){
		if (resultsPanelMap.containsKey(network))
			return resultsPanelMap.get(network);
		return null;
		
	}
	public void addResultsPanel(CyNetwork network, ResultsPanel resultsPanel){
		if (!resultsPanelMap.containsKey(network))
			resultsPanelMap.put(network, new ArrayList<ResultsPanel>());
		resultsPanelMap.get(network).add(resultsPanel);
	}

	public void removeResultsPanel(CyNetwork network, ResultsPanel resultsPanel){
		if (!resultsPanelMap.containsKey(network))
			return;

		List<ResultsPanel> panels = resultsPanelMap.get(network);
		panels.remove(resultsPanel);
		if (panels.size() == 0)
			resultsPanelMap.remove(network);
	}
	
	public <T> T getService(Class<? extends T> clazz) {
		return serviceRegistrar.getService(clazz);
	}

	public <T> T getService(Class<? extends T> clazz, String filter) {
		return serviceRegistrar.getService(clazz, filter);
	}

	public void registerService(Object service, Class serviceClass, Properties props) {
		serviceRegistrar.registerService(service, serviceClass, props);
	}

	public void unregisterService(Object service, Class serviceClass) {
		serviceRegistrar.unregisterService(service, serviceClass);
	}

	public boolean isLinked(CyNetwork network) {
		CyRootNetwork rootNetwork = ((CySubNetwork)network).getRootNetwork();
		if (linkedNetworks.containsKey(rootNetwork))
			return true;
		return false;
	}

	public void linkNetworkSelection(CyNetwork network) {
		if (isLinked(network))
			return;
		CyRootNetwork rootNetwork = ((CySubNetwork)network).getRootNetwork();
		CyEventHelper helper = serviceRegistrar.getService(CyEventHelper.class);
		NetworkSelectionLinker linker = new NetworkSelectionLinker(rootNetwork, helper, this);
		registerService(linker, RowsSetListener.class, new Properties());
		linkedNetworks.put(rootNetwork, linker);
	}

	public void unlinkNetworkSelection(CyNetwork network) {
		if (!isLinked(network))
			return;
		CyRootNetwork rootNetwork = ((CySubNetwork)network).getRootNetwork();
		unregisterService(linkedNetworks.get(rootNetwork), RowsSetListener.class);
		linkedNetworks.remove(rootNetwork);
	}
}
