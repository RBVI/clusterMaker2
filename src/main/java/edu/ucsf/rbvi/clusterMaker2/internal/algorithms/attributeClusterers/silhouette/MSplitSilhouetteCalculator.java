package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette;

import java.util.ArrayList;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.Segregatable;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.Subsegregatable;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.MeanSummarizer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.MedianSummarizer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.Summarizer;



/**
 * MSplitSilhouetteCalculator calculates the median/mean split silhouette.
 * @author djh.shih
 *
 */
public class MSplitSilhouetteCalculator {
	
	public static Clusters segregateByAverageSilhouette(Segregatable seg, int K, Summarizer summarizer) {
		Clusters split = null;
		
		int m = seg.size();
		
		// silhouette can only be calculated for 2 <= k <= m-1
		// bound K
		if ( K > m-1) {
			K = m - 1;
		}
		
		// maximize average silhouette
		double avgSil = Double.NEGATIVE_INFINITY;
		for (int k = 2; k <= K; ++k) {
			Clusters clusters = seg.cluster(k);
			Silhouettes sils = SilhouetteCalculator.silhouettes(seg.segregations(clusters), clusters);
			double t = sils.getAverage(summarizer);
			if (t > avgSil) {
				avgSil = t;
				split = clusters;
			}
		}
		
		if (split != null) {
			// replace classification cost by (1 - average silhouette)
			split.setCost(1 - avgSil);
		}
		
		return split;
	}
	
	public static Clusters segregateByMeanSilhouette(Segregatable seg, int K) {
		return segregateByAverageSilhouette(seg, K, new MeanSummarizer());
	}
	
	public static Clusters segregateByMedianSilhouette(Segregatable seg, int K) {
		return segregateByAverageSilhouette(seg, K, new MedianSummarizer());
	}
	
	
	public static ArrayList<Double> averageSilhouettes(Subsegregatable sseg, Clusters clusters, int L, Summarizer summarizer) {
		int K = clusters.getNumberOfClusters();
		ArrayList<Double> splitSilhouettes = new ArrayList<Double>();
		int[][] partitions = clusters.getPartitions();
		// calculate the split silhouette of each cluster
		for (int kk = 0; kk < K; ++kk) {
			Clusters subclusters = segregateByAverageSilhouette(sseg.subset(partitions[kk]), L, summarizer);
			if (subclusters != null) {
				// cluster could be split further into subclusters
				splitSilhouettes.add(1 - subclusters.getCost());
			}
		}
		return splitSilhouettes;
	}
	
	public static ArrayList<Double> meanSilhouettes(Subsegregatable sseg, Clusters clusters, int L) {
		return averageSilhouettes(sseg, clusters, L, new MeanSummarizer());
	}
	
	public static ArrayList<Double> medianSilhouettes(Subsegregatable sseg, Clusters clusters, int L) {
		return averageSilhouettes(sseg, clusters, L, new MedianSummarizer());
	}
	
	public static double averageSplitSilhouette(Subsegregatable sseg, Clusters clusters, int L, Summarizer summarizer) {
		ArrayList<Double> splitSilhouettes = averageSilhouettes(sseg, clusters, L, summarizer);
		if (splitSilhouettes.size() == 0) {
			// no cluster has a valid silhouette value (e.g. when all clusters have size < 3)
			return Double.POSITIVE_INFINITY;
		}
		return summarizer.summarize(splitSilhouettes.toArray(new Double[splitSilhouettes.size()]));
		
	}
	
	public static double meanSplitSilhouette(Subsegregatable sseg, Clusters clusters, int L) {
		return averageSplitSilhouette(sseg, clusters, L, new MeanSummarizer());
	}
	
	public static double medianSplitSilhouette(Subsegregatable sseg, Clusters clusters, int L) {
		return averageSplitSilhouette(sseg, clusters, L, new MedianSummarizer());
	}
	
	public static Clusters splitByAverageSplitSilhouette(Subsegregatable sseg, int K, int L, boolean forceSplit, Summarizer summarizer) {
		Clusters split = null;
		int m = sseg.size();
		
		// mean split silhouette can only be calculated for 2 <= k <= m-1
		// bound K
		if ( K > m / 3) {
			K = m / 3;
		}
		
		int minK = (forceSplit ? 2 : 1);
		
		// minimize the mean split silhouette
		double avgSplitSil = Double.POSITIVE_INFINITY;
		for (int k = minK; k <= K; k++) {
			Clusters clusters = sseg.cluster(k);
			double t = averageSplitSilhouette(sseg, clusters, L, summarizer);
			if (t < avgSplitSil) {
				avgSplitSil = t;
				split = clusters;
			}
		}
		
		if (split == null) {
			split = sseg.cluster(minK);
		} 
		split.setCost(avgSplitSil);
		
		return split;
	}
	
	public static Clusters splitByMeanSplitSilhouette(Subsegregatable sseg, int K, int L) {
		return splitByMeanSplitSilhouette(sseg, K, L, false);
	}
	
	public static Clusters splitByMeanSplitSilhouette(Subsegregatable sseg, int K, int L, boolean forceSplit) {
		return splitByAverageSplitSilhouette(sseg, K, L, forceSplit, new MeanSummarizer());
	}
	
	public static Clusters splitByMedianSplitSilhouette(Subsegregatable sseg, int K, int L) {
		return splitByMedianSplitSilhouette(sseg, K, L, false);
	}
	
	public static Clusters splitByMedianSplitSilhouette(Subsegregatable sseg, int K, int L, boolean forceSplit) {
		return splitByAverageSplitSilhouette(sseg, K, L, forceSplit, new MedianSummarizer());
	}
	
	public static Clusters splitByAverageSilhouette(Segregatable seg, int K, boolean forceSplit, Summarizer summarizer) {
		Clusters split = segregateByAverageSilhouette(seg, K, summarizer);
		if (!forceSplit) {
			// consider no split (k = 1)
			if (split.getCost() >= 1) {
				// cost >= 1  =>  average silhouette < 0  =>  no splitting is warranted
				split = seg.cluster(1);
				split.setCost(1.0);
			}
		}
		return split;
	}
	
	public static Clusters splitByMeanSilhouette(Segregatable seg, int K, boolean forceSplit) {
		return splitByAverageSilhouette(seg, K, forceSplit, new MeanSummarizer());
	}
	
	public static Clusters splitByMedianSilhouette(Segregatable seg, int K, boolean forceSplit) {
		return splitByAverageSilhouette(seg, K, forceSplit, new MedianSummarizer());
	}
	
}
