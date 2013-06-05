package edu.ucsf.rbvi.clusterMaker2.internal;

// Java imports

// Cytoscape imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// clusterMaker imports

public class ClusterManager {
	private static Logger logger = LoggerFactory
			.getLogger(edu.ucsf.rbvi.clusterMaker2.internal.ClusterManager.class);

	public ClusterManager() {
		// Use OSGi to find all of the registered cluster algorithms?
		// Alternatively, we may want to provide "hooks" to register algorithms
	}

}
