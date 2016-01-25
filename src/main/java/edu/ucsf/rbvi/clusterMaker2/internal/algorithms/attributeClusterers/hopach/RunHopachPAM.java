package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractKClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Clusters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.types.SplitCost;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam.HopachablePAM;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.MeanSummarizer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.MedianSummarizer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.PrimitiveMeanSummarizer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.PrimitiveMedianSummarizer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.PrimitiveSummarizer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.Summarizer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.numeric.SummaryMethod;

public class RunHopachPAM extends AbstractKClusterAlgorithm {
	
	SplitCost splitCost;
	SummaryMethod summaryMethod;
	int maxLevel, K, L;
	double minCostReduction;
	boolean forceInitSplit;
	HopachPAMContext context = null;
	
	public RunHopachPAM(CyNetwork network, String weightAttributes[], DistanceMetric metric, 
			            TaskMonitor monitor, HopachPAMContext context, AbstractClusterAlgorithm parentTask) {
		super(network, weightAttributes, metric, monitor, parentTask);
		this.context = context;
	}
	
	void setParameters(SplitCost splitCost, SummaryMethod summaryMethod, int maxLevel, int K, int L, boolean forceInitSplit, double minCostReduction) {
		this.splitCost = splitCost;
		this.summaryMethod = summaryMethod;
		this.maxLevel = maxLevel;
		this.K = K;
		this.L = L;
		this.forceInitSplit = forceInitSplit;
		this.minCostReduction = minCostReduction;
	}
	
	@Override
	public int kcluster(int nClusters, int nIterations, CyMatrix matrix, DistanceMetric metric, int[] clusterId) {
		
		monitor.setProgress(0);
		
		Summarizer summarizer;
		PrimitiveSummarizer psummarizer;
		switch (summaryMethod) {
		case MEDIAN:
			summarizer = new MedianSummarizer();
			psummarizer = new PrimitiveMedianSummarizer();
			break;
		case MEAN:
		default:
			summarizer = new MeanSummarizer();
			psummarizer = new PrimitiveMeanSummarizer();
			break;
		}
		
		HopachablePAM partitioner = new HopachablePAM(network, matrix, metric);
		partitioner.setParameters(K, L, splitCost, summarizer);
		
		HopachPAM hopachPam = new HopachPAM(partitioner);
		hopachPam.setParameters(maxLevel,  minCostReduction,  forceInitSplit, psummarizer);
		
		Clusters c = hopachPam.run();
		
		// copy results into clusterId
		for (int i = 0; i < c.size(); ++i) {
			clusterId[i] = c.getClusterIndex(i);
		}
		
		return c.getNumberOfClusters();
	}
}
