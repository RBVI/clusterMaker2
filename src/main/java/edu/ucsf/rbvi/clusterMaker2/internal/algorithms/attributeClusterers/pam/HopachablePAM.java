package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam;


import org.cytoscape.model.CyNetwork;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BaseMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.Hopachable;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.SplitCost;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.Subsegregatable;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.DistanceCalculator;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.MSplitSilhouetteCalculator;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.silhouette.SilhouetteCalculator;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.MeanSummarizer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.Summarizer;

/**
 * A PAM partitioner that implements Hopachable.
 * @author djh.shih
 */
public class HopachablePAM extends PAM implements Hopachable, Subsegregatable {
	
	// maximum number of partitions to consider to splitting
	int maxK = 9;
	
	// maximum number of sub-partitions to consider for sub-splitting each partition
	int maxL = 9;
	
	SplitCost splitCost = SplitCost.AVERAGE_SPLIT_SILHOUETTE;
	
	Summarizer summarizer = new MeanSummarizer();
	
	public HopachablePAM(CyNetwork network, BaseMatrix data, DistanceMetric metric) {
		super(network, data, metric);
	}
	
	HopachablePAM(BaseMatrix data, DistanceMetric metric, DistanceMatrix distances, int[] idx) {
		super(data, metric, distances, idx);
	}
	
	public void setParameters(int maxK, int maxL, SplitCost splitCost, Summarizer summarizer) {
		this.maxK = maxK;
		this.maxL = maxL;
		this.splitCost = splitCost;
		this.summarizer = summarizer;
	}

	public Hopachable subset(int[] index) {
		// shallow copy super class's data and use supplied index
		return new HopachablePAM(super.data, super.metric, super.distances, index);
	}

	public double[][] segregations(Clusters clusters) {
		return DistanceCalculator.segregations(super.distances, clusters);
	}

	public double[][] separations(Clusters clusters) {
		return DistanceCalculator.separations(super.distances, clusters.getClusterLabels());
	}
	
	public Clusters split(boolean forceSplit) {
		switch (splitCost) {
		case AVERAGE_SILHOUETTE:
			return MSplitSilhouetteCalculator.splitByAverageSilhouette(this, maxK, forceSplit, summarizer);
		case AVERAGE_SPLIT_SILHOUETTE:
		default:
			return MSplitSilhouetteCalculator.splitByAverageSplitSilhouette(this, maxK, maxL, forceSplit, summarizer);
		}
	}

	public Clusters collapse(int i, int j, Clusters clusters) {
		// NB    In Pollard's implementation, the choice of the new medoid probably does not change downstream results...
		Clusters c = new Clusters(clusters);
		c.merge(i,  j);
		// set new cost
		switch (splitCost) {
		case AVERAGE_SILHOUETTE:
			c.setCost( 1 - SilhouetteCalculator.silhouettes(this.segregations(c), c).getAverage(summarizer) );
			break;
		case AVERAGE_SPLIT_SILHOUETTE:
		default:
			c.setCost( MSplitSilhouetteCalculator.averageSplitSilhouette(this, c, maxL, summarizer) );
			break;
		}
		return c;
	}

	public int[] order(Clusters clusters) {
		// put elements of same cluster together, and order the elements within each cluster based on neighbouring clusters
		return null;
	}

}
