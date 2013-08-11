package edu.ucsf.rbvi.clusterMaker2.internal;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;

import edu.ucsf.rbvi.clusterMaker2.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.ClusterManager;

public class ClusterManagerImpl implements ClusterManager {
	CyApplicationManager appMgr;
	CyServiceRegistrar serviceRegistrar;
	Map<String, ClusterAlgorithm> algMap;
	Map<String, ClusterViz> vizMap;

	public ClusterManagerImpl(CyApplicationManager appMgr, CyServiceRegistrar serviceRegistrar) {
		this.appMgr = appMgr;
		this.serviceRegistrar = serviceRegistrar;
		this.algMap = new HashMap<String, ClusterAlgorithm>();
		this.vizMap = new HashMap<String, ClusterViz>();

	}

	public Collection<ClusterAlgorithm> getAllAlgorithms() {
		return algMap.values();
	}

	public ClusterAlgorithm getAlgorithm(String name) {
		if (algMap.containsKey(name))
			return algMap.get(name);
		return null;
	}

	public void addAlgorithm(ClusterAlgorithm alg, Map props) {
		addAlgorithm(alg);
	}

	public void addAlgorithm(ClusterAlgorithm alg) {
		algMap.put(alg.getName(), alg);

		// Create our wrapper and register the algorithm
	}

	public void removeAlgorithm(ClusterAlgorithm alg) {
		if (algMap.containsKey(alg.getName()))
			algMap.remove(alg.getName());

		// unregister the algorithm
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
	}

	public void addVisualizer(ClusterViz viz, Map props) {
		addVisualizer(viz);
	}

	public void removeVisualizer(ClusterViz viz) {
		if (vizMap.containsKey(viz.getName()))
			vizMap.remove(viz.getName());
	}

	public CyNetwork getNetwork() {
		return appMgr.getCurrentNetwork();
	}

}
