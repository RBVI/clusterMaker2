package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types;

/**
 * Subsegregatable is a subsettable Segregatable.
 * @author djh.shih
 *
 */
public interface Subsegregatable extends Segregatable {
	public Subsegregatable subset(int[] idx);
}
