/*
 * Created on 7. April 2008
 * 
 */

package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging;

import java.util.Comparator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;

/**
 * This class implements the Comparator interface for the comparison of an array of
 * {@link IParameters} objects. The scores for the clustering carried out
 * using the parameters in the IParameters object. It is assumed that the score is
 * the same for each IParameters object in the array.
 * @author Sita Lange
 */
public class IParametersArrayComparator implements Comparator<IParameters[]> {

	/**
	 * Returns -1 if the score of o1 is smaller than of o2, 1 if it is greater and 0 if they
	 * are both equal.
	 * @param p1 The first IParameters[] object
	 * @param p2 The second IParameters[] object
	 */
	public int compare(IParameters[] p1, IParameters[] p2) {
		if(p1==null || p2==null||p1.length==0||p2.length==0){
			return 0;
		}
		if(p1[0].getScore() < p2[0].getScore()){
			return -1;
		} else if (p1[0].getScore() > p2[0].getScore()){
			return 1;
		}
		return 0;
	}
}
