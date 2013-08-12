package edu.ucsf.rbvi.clusterMaker2.internal.algorithms;

import org.cytoscape.work.Tunable;

public class AdvancedProperties {
	@Tunable(description="Cluster attribute name", groups={"Cytoscape Advanced Settings"}, 
	         params="displayState=collapsed", gravity=1.0)
	String clusterAttribute;

	@Tunable(description="Create groups (metanodes) with results", groups={"Cytoscape Advanced Settings"}, gravity=2.0)
	boolean createGroups;

	public AdvancedProperties(String clusterAttribute, boolean createGroups) {
		this.clusterAttribute = clusterAttribute;
		this.createGroups = createGroups;
	}

	public boolean createGroups() { return createGroups; }
	public String getClusterAttribute() { return clusterAttribute; }
}
