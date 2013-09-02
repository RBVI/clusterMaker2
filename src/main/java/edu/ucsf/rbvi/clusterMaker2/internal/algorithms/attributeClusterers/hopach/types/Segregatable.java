package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;


/**
 * Segregatable is an interface for constructing segregation matrices.
 * @author djh.shih
 *
 */
public interface Segregatable extends KClusterable {
	public double[][] segregations(Clusters clusters);
}