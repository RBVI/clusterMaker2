package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types;

public enum SplitCost {
	AVERAGE_SPLIT_SILHOUETTE("Average split silhouette"),
	AVERAGE_SILHOUETTE("Average silhouette");

	private String name;

	SplitCost(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}

}
