/*
 * Created on 18. December 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Vector;


/**
 * This class compares two Vectors  according to their sizes.
 * 
 * @author Sita Lange
 *
 */
@SuppressWarnings("unchecked")
public class ClusterObjectComparator implements Comparator {

//	@SuppressWarnings("unchecked")
	public int compare(Object o1, Object o2) {
		
		int sizeV1 = ((ArrayList) o1).size();
		int sizeV2 = ((ArrayList) o2).size();
		
		if(sizeV1<sizeV2) return -1; 
		else if(sizeV1>sizeV2) return 1;
		else return 0;
	}
}
