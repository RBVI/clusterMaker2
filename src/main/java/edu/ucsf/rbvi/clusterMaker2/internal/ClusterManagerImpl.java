package edu.ucsf.rbvi.clusterMaker2.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskFactory;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class ClusterManagerImpl implements ClusterManager {
// public class ClusterManagerImpl {
	CyApplicationManager appMgr;
	CyServiceRegistrar serviceRegistrar;
	CyGroupManager groupMgr;
	Map<String, ClusterTaskFactory> algMap;
	Map<String, ClusterViz> vizMap;

	public ClusterManagerImpl(CyApplicationManager appMgr, CyServiceRegistrar serviceRegistrar,
 	                          CyGroupManager groupMgr) {
		this.appMgr = appMgr;
		this.serviceRegistrar = serviceRegistrar;
		this.groupMgr = groupMgr;
		this.algMap = new HashMap<String, ClusterTaskFactory>();
		this.vizMap = new HashMap<String, ClusterViz>();
	}

	public Collection<ClusterTaskFactory> getAllAlgorithms() {
		return algMap.values();
	}

	public ClusterTaskFactory getAlgorithm(String name) {
		if (algMap.containsKey(name))
		 	return algMap.get(name);
		return null;
	}

	public void addClusterAlgorithm(ClusterTaskFactory alg, Map props) {
		addAlgorithm(alg);
	}

	public void addAlgorithm(ClusterTaskFactory alg) {
		algMap.put(alg.getName(), alg);

		// Get the type of clusterer (Attribute, Network, Filter, Attribute+Network)
		List<ClusterTaskFactory.ClusterType> clusterTypes = alg.getTypeList();

		// Create our wrapper and register the algorithm
		for (ClusterTaskFactory.ClusterType type: clusterTypes) {
			Properties props = new Properties();
			props.setProperty(COMMAND, alg.getName());
			props.setProperty(COMMAND_NAMESPACE, "cluster");
			props.setProperty(IN_MENU_BAR, "true");
			props.setProperty(TITLE, alg.getName());
			switch(type) {
			case NETWORK:
				props.setProperty(PREFERRED_MENU, "Apps.Network Cluster Algorithms");
				break;

			case ATTRIBUTE:
				props.setProperty(PREFERRED_MENU, "Apps.Attribute Cluster Algorithms");
				break;

			case FILTER:
				props.setProperty(PREFERRED_MENU, "Apps.Network Filters");
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


	public Collection<ClusterViz> getAllVisualizers() {
		return vizMap.values();
	}

	public ClusterViz getVisualizer(String name) {
		if (vizMap.containsKey(name))
	 		return vizMap.get(name);
		return null;
	}

	public void addVisualizer(ClusterViz viz) {
		vizMap.put(viz.getName(), viz);

		// Create our wrapper and register the algorithm
		Properties props = new Properties();
		props.setProperty(COMMAND, viz.getName());
		props.setProperty(COMMAND_NAMESPACE, "clusterviz");
		props.setProperty(IN_MENU_BAR, "true");
		props.setProperty(PREFERRED_MENU, "Apps.Cluster Visualization");
		props.setProperty(TITLE, viz.getName());
		serviceRegistrar.registerService(viz, TaskFactory.class, props);
	}

	public void addClusterVisualizer(ClusterViz viz, Map props) {
		addVisualizer(viz);
	}

	public void removeVisualizer(ClusterViz viz) {
		if (vizMap.containsKey(viz.getName()))
	 		vizMap.remove(viz.getName());
	}

	public void removeClusterVisualizer(ClusterViz viz, Map props) {
		removeVisualizer(viz);
		serviceRegistrar.unregisterService(viz, TaskFactory.class);
	}

	public CyNetwork getNetwork() {
		return appMgr.getCurrentNetwork();
	}
}
