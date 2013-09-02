package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;



public class HopachPAMTest {

	@Test
	public void testRun() {
		Double[] data = {
			100.9, 100.9, 
			100.85, 100.85, 
			100.8, 100.8,
			.15, .15, 
			.2, .2, 
			.12, .12,
			.05, .05,
			.04, .04,
			.0, .0,
			.02, .02,
		};
		int[] ans = {0, 0, 0, 1, 1, 1, 2, 2, 2, 2};
		
		BaseMatrix mat = new BaseMatrix(0, 2, data);
		HopachPAM h = new HopachPAM(mat, DistanceMetric.CITYBLOCK);
	
		Clusters c = h.run();
		
		// check that the clustering results match
		for (int i = 0; i < c.size(); ++i) {
			assertEquals(c.getClusterIndex(i), ans[i]);
		}
	}

	@Test
	public void testRunDown() {
		fail("Not yet implemented");
	}

	@Test
	public void testInitLevel() {
		Double[] data = {
			100.9, 100.9, 
			100.85, 100.85, 
			100.8, 100.8,
			.15, .15, 
			.2, .2, 
			.12, .12,
			.05, .05,
			.04, .04,
			.0, .0,
			.02, .02,
		};
		// median
		//int[] ans = {0, 0, 0, 1, 1, 1, 1, 1, 1, 1};
		// mean
		int[] ans = {0, 0, 0, 1, 1, 1, 2, 2, 2, 2};
		
		BaseMatrix mat = new BaseMatrix(0, 2, data);
		HopachPAM h = new HopachPAM(mat, DistanceMetric.CITYBLOCK);
	
		Clusters c = h.initLevel();
		
		// check that the clustering results match
		for (int i = 0; i < c.size(); ++i) {
			assertEquals(c.getClusterIndex(i), ans[i]);
		}
	}

	@Test
	public void testCollapse() {
		
		// TODO Create a test case that results in a chain of collapses...
		
		Double[] data = {
			.15, .15, 
			.2, .2, 
			.12, .12,
			.05, .05,
			.0, .0,
			.06, .06,
			.015, .015,
			.01, .01,
			.03, .03,
			.04, .04,
			.02, .02,
		};
		int[] initMedoids = {0, 0, 0, 3, 3, 3, 3, 5, 5, 5, 5};
		int[] ans = {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1};
		
		BaseMatrix mat = new BaseMatrix(0, 2, data);
		HopachPAM h = new HopachPAM(mat, DistanceMetric.CITYBLOCK);
		
		Clusters b = new Clusters(initMedoids);
		//b.setCost( MSplitSilhouetteCalculator.medianSplitSilhouette(h.partitioner, b, 9) );
		b.setCost(1.0);
		h.splits.set(0, b);
		Clusters c = h.collapse(0);
		
		
		// check that the clustering results match
		for (int i = 0; i < c.size(); ++i) {
			assertEquals(c.getClusterIndex(i), ans[i]);
		}
	}

	@Test
	public void testNearestClusters() {
		fail("Not yet implemented");
	}

	@Test
	public void testNextLevel() {
		fail("Not yet implemented");
	}

	@Test
	public void testSortSplit() {
		fail("Not yet implemented");
	}

	@Test
	public void testSortInitLevel() {
		fail("Not yet implemented");
	}

	@Test
	public void testSplitIsFinal() {
		fail("Not yet implemented");
	}

	@Test
	public void testOptimizeOrderingCorrelation() {
		fail("Not yet implemented");
	}

	@Test
	public void testOrderingCorrelation() {
		fail("Not yet implemented");
	}

	@Test
	public void testDistanceMatrixToVector() {
		fail("Not yet implemented");
	}

	@Test
	public void testIndexLTMatrixToVector() {
		fail("Not yet implemented");
	}

	@Test
	public void testReorderDistanceVector() {
		fail("Not yet implemented");
	}

}
