package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric;

public enum SummaryMethod {
	MEAN("Mean"), MEDIAN("Median");

	private String name;

	SummaryMethod(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}
}
