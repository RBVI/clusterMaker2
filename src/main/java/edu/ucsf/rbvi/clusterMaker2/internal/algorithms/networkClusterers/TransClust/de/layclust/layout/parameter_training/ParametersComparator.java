/*
 * Created on 15. November 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.parameter_training;

import java.util.Comparator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;

/**
 * This class implements the Comparator interface for the comparison of 
 * {@link IParameters} objects. The scores for the clustering carried out
 * using the parameters in the IParameters object.
 * @author Sita Lange
 */
public class ParametersComparator implements Comparator<IParameters> {

	/**
	 * Returns -1 if the score of o1 is smaller than of o2, 1 if it is greater and 0 if they
	 * are both equal.
	 * @param p1 The first IParameters object
	 * @param p2 The second IParameters object
	 */
	public int compare(IParameters p1, IParameters p2) {
		if(p1.getScore() < p2.getScore()){
			return -1;
		} else if (p1.getScore() > p2.getScore()){
			return 1;
		}
		return 0;
	}
}
