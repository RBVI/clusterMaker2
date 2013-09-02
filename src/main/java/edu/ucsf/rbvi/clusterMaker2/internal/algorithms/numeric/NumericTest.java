package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric;

import static org.junit.Assert.*;

import org.junit.Test;

public class NumericTest {
	
	private static double epsilon = 0.001;
	
	@Test
	public void testMean() {
		Double[][] tests = {
				{2., 1., 4., 3.},
				{1.5, 2.5, 3.6, 1.4, 1.0},
				{-3., null, 2., null, null},
		};
		double[] ans = {2.5, 2.0, -0.5};
		
		for (int i = 0; i < tests.length; ++i) {
			assertEquals("mean", ans[i], Numeric.mean(tests[i]).doubleValue(), epsilon);
		}
	}

	@Test
	public void testMedian() {
		Double[][] tests = {
				{3., 5., 1., 9., 7.},
				{4., 3., 1., 2.},
		};
		double[] ans = {5., 2.5};
		
		for (int i = 0; i < tests.length; ++i) {
			assertEquals("median", ans[i], Numeric.median(tests[i]).doubleValue(), epsilon);
		}
	}

	@Test
	public void testSelect() {
		Double[] test = {9., 4., 3., 2., 2., 6., 1., 4., 4., 1.};
		int[] order = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		double[] ans = {1., 1., 2., 2., 3., 4., 4., 4., 6., 9.};
		
		for (int i = 0; i < order.length; ++i) {
			assertEquals("select", ans[i], Numeric.select(test, order[i]).doubleValue(), epsilon);
		}
	}

}
