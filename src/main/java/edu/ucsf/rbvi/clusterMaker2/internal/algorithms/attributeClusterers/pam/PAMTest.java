package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;


public class PAMTest {
	
	@Test
	public void testSingletonCluster() {
		Double[] data = {
			.9, .9
		};
		int k = 1;
		
		int[] ans = {0};
		
		BaseMatrix mat = new BaseMatrix(0, 2, data);
		PAM pam = new PAM(null, mat, DistanceMetric.CITYBLOCK);
		
		Clusters c = pam.cluster(k);
		
		assertEquals(c.getNumberOfClusters(), k);
	}

	@Test
	public void testCluster() {
		Double[] data = {
			.9, .9, 
			.8, .8, 
			.5, .5, 
			.4, .4, 
			.0, .0,
			.1, .1,
		};
		int k = 3;
		
		int[] ans = {0, 0, 1, 1, 2, 2};
		
		BaseMatrix mat = new BaseMatrix(0, 2, data);
		PAM pam = new PAM(null, mat, DistanceMetric.CITYBLOCK);
		
		Clusters c = pam.cluster(k);
		
		assertEquals(c.getNumberOfClusters(), k);
		
		for (int i = 0; i < c.size(); ++i) {
			assertEquals(c.getClusterIndex(i), ans[i]);
		}
		
		// NB    The current implementation fails the below test case:
		//       data = [.9, .9; .8, .8; .4, .4,; .5, .5; .1, .1]
		//       ans  = [     0,      0,      1,       1,      2]
		//       
		//       Instead, PAM.cluster(...) yields:
		//       res  = [     0,      0,      1,       2,      2]
		//       
		//       This discrepancy is due to existence of singleton clusters.
		//       During the build phase, element 2 is selected as a medoid, which precludes
		//       element 4 from becoming a medoid.
		//       During the swap phase, the current implementation failed to register
		//       the (2, 4) swap as a worthwhile swap, because the contribution to a potential
		//       swap is only calculated based on nonmedoids other than the candidate itself.
		//       There is no way to justify merging element 2 to cluster 1 and creating
		//       a new singleton cluster headed by element 4.
		//       
		//       In contrast, R's cluster::pam passes this test case.
		//       The algorithm therein likely differs from the one upon which the current
		//       implementation is based.
	}

}
