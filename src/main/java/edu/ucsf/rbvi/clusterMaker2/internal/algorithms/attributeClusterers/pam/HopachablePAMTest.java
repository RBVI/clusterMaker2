package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.Hopachable;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.SplitCost;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.MedianSummarizer;

public class HopachablePAMTest {

	@Test
	public void testSubset() {
		
		Double[] data = {
			.9, .9, 
			.8, .8, 
			.4, .4, 
			.5, .5, 
			.1, .1,
			.0, .0,
		};
		int k = 3;
		// new order
		int[] index = {0, 1, 5, 2, 4, 3};
		// expected results based on new order
		int[] ans = {0, 0, 1, 2, 1, 2};
		
		
		CyMatrix mat = CyMatrixFactory.makeSmallMatrix(6, 2, data);
		HopachablePAM pam = new HopachablePAM(null, mat, DistanceMetric.CITYBLOCK);
		// permute sample order
		Hopachable pamPermuted = pam.subset(index);
		
		Clusters c = pamPermuted.cluster(k);
		
		// the number of clusters should not change because it should always
		// return the specified number of clusters
		assertEquals(c.getNumberOfClusters(), k);
		
		// the clustering cluster cost, ideally should not change
		// but the PAM algorithm itself can be dependent on the order of the data points
		// (the initial medoids selected is influenced by data order, and a local minimum may be reached.)
		
		// check that the clustering results match
		for (int i = 0; i < c.size(); ++i) {
			assertEquals(c.getClusterIndex(i), ans[i]);
		}
		
		
		// minor test case
		
		// subset the last 4 elements
		int[] subsetIndex = {2, 3, 4, 5};
		int[] subsetAns = {0, 0, 1, 1};
		int subsetK = 2;
		
		Hopachable pamSubset = pam.subset(subsetIndex);
		
		Clusters c2 = pamSubset.cluster(subsetK);
	
		// check number of clusters
		assertEquals(c2.getNumberOfClusters(), subsetK);
		
		// check cluster assignments
		for (int i = 0; i < c2.size(); ++i) {
			assertEquals(c2.getClusterIndex(i), subsetAns[i]);
		}
			
	}

	@Test
	public void testSplit() {
		Double[] data = {
			.2, .2, 
			.8, .8, 
			.82, .82, 
			.4, .5, 
			.5, .4,
			.15, .15,
			.81, .81,
			.14, .14,
			.45, .45,
		};
		int k = 3;
		
		int[] ans = {0, 1, 1, 2, 2, 0, 1, 0, 2};
		
		CyMatrix mat = CyMatrixFactory.makeSmallMatrix(9, 2, data);
		HopachablePAM pam = new HopachablePAM(null, mat, DistanceMetric.CITYBLOCK);
		pam.setParameters(9, 9, SplitCost.AVERAGE_SPLIT_SILHOUETTE, new MedianSummarizer());
		
		Clusters c = pam.split(false);
		
		// check that data are split into expected number of clusters
		assertEquals(c.getNumberOfClusters(), k);
		
		// check cluster assignments
		for (int i = 0; i < c.size(); ++i) {
			assertEquals(c.getClusterIndex(i), ans[i]);
		}
	}

	@Test
	public void testCollapse() {
		Double[] data = {
			.9, .9, 
			.8, .8, 
			.4, .4, 
			.5, .5, 
			.1, .1,
			.0, .0,
		};
		int k = 3;
		
		CyMatrix mat = CyMatrixFactory.makeSmallMatrix(6, 2, data);
		HopachablePAM pam = new HopachablePAM(null, mat, DistanceMetric.CITYBLOCK);
		
		Clusters c1 = pam.cluster(k);
		
		Clusters c2 = pam.collapse(0, 1, c1);
		Clusters c3 = pam.collapse(1, 2, c1);
		Clusters c4 = pam.collapse(0, 2, c1);
		
		// check that the size has reduced
		--k;
		assertEquals(c2.getSizes().length, k);
		assertEquals(c3.getSizes().length, k);
		assertEquals(c4.getSizes().length, k);
		
		Clusters c5 = pam.collapse(0, 1, c2);
		--k;
		assertEquals(c5.getSizes().length, k);
		
	}

	@Test
	public void testOrder() {
		fail("Not yet implemented");
	}

}
