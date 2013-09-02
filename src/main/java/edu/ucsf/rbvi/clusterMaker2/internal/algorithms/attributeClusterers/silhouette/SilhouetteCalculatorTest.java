package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;


public class SilhouetteCalculatorTest {
	
	private static double epsilon = 0.001;

	@Test
	public void testCalculateBaseMatrix() {
		BaseMatrix test = new BaseMatrix(
			12, 3,
			new Double[]{
				101., 102., 103.,
				102., 103., 104.,
				103., 104., 105.,
				111., 112., 113.,
				112., 113., 114.,
				113., 114., 115.,
				 21.,  22.,  23.,
				 22.,  23.,  24.,
				 23.,  24.,  25.,
				 29.,  30.,  31.,
				 32.,  33.,  34.,
				 33.,  34.,  35.,
			}
		);
		
		int[] labels = {0, 0, 0, 1, 1, 1, 2, 2, 2, 3, 3, 3};
		
		// silhouettes calculated by cluster::silhouette in R
		double[] ans = {
			0.8636364, 0.9000000, 0.8333333, 
			0.8333333, 0.9000000, 0.8636364,
			0.8548387, 0.8928571, 0.8200000,
			0.5000000, 0.8000000, 0.7727273,
		};
		
		Silhouettes out = SilhouetteCalculator.calculate(test, DistanceMetric.CITYBLOCK, labels);
		
		assertEquals("length", ans.length, out.size());
		
		for (int i = 0; i < ans.length; i++) {
			assertEquals("silhouette[" + i + "]", ans[i], out.getSilhouette(i), epsilon);
		}
		
	}

}
