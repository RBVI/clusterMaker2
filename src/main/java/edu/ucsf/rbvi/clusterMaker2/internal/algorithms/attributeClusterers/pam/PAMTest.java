package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
		System.out.println("testCluster begin");
		Double[] data = {.9, .9, .8, .8, .4, .4, .5, .5, .1, .1}/* = {
			.9, .9, 
			.8, .8, 
			.5, .5, 
			.4, .4, 
			.0, .0,
			.1, .1,
		}*/;
		int k = 3;
		
		int[] ans = {0, 0, 1, 1, 2};//{0, 0, 1, 1, 2, 2};
		
		BaseMatrix mat = new BaseMatrix(0, 2, data);
		PAM pam = new PAM(null, mat, DistanceMetric.CITYBLOCK);
		
		Clusters c = pam.cluster(k);
		System.out.println("testCluster end");
		assertEquals(c.getNumberOfClusters(), k);
		for (int i = 0; i < c.size(); ++i) {
			System.out.println("c[" + i + "] = " + c.getClusterIndex(i));
		}
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

	@Test
	public void testLarge() {
		System.out.println("testLarge begin");
		Double[] data;
		int k = 8;
		
		int[] ans = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7};

		BufferedReader reader = new BufferedReader(new InputStreamReader(PAMTest.class.getResourceAsStream("/pam_data.txt")));
		String line;
		ArrayList<Double> vectors = new ArrayList<Double>();
		int vectorWidth = 0;
		try {
			while ((line = reader.readLine()) != null) {
				String [] vector = line.split("\t");
				vectorWidth = vector.length;
				for (String v: vector)
				vectors.add(Double.parseDouble(v));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		data = new Double[vectors.size()];
		data = vectors.toArray(data);

		BaseMatrix mat = new BaseMatrix(0, vectorWidth, data);
		PAM pam = new PAM(null, mat, DistanceMetric.EUCLIDEAN);
		
		Clusters c = pam.cluster(k);
		System.out.println("testLarge end");
		assertEquals(c.getNumberOfClusters(), k);
		
		for (int i = 0; i < c.size(); ++i) {
			assertEquals(c.getClusterIndex(i), ans[i]);
		}		
	}

}
