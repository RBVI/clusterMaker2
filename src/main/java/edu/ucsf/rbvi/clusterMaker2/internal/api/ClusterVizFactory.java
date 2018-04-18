package edu.ucsf.rbvi.clusterMaker2.internal.api;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskFactory;

public interface ClusterVizFactory extends ClusterTaskFactory {
	@Override
	default public String getExampleJSON() { return "{}"; }
}


