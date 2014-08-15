package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BicFinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BiMine.BETNode;

public class RunBicFinder {

	protected CyNetwork network;
	protected String[] weightAttributes;
	protected Matrix matrix;
	protected Matrix biclusterMatrix;
	protected Double arr[][];
	protected Double geneRho[][];
	protected Double conditionRho[][];
	protected int[] clusters;
	protected TaskMonitor monitor;
	protected boolean ignoreMissing = true;
	protected boolean selectedOnly = false;
	BicFinderContext context;	
	double alpha;
	double delta;
	
	protected Map<Integer,List<Long>> clusterNodes;
	protected Map<Integer,List<String>> clusterAttrs;
	
	public Matrix getMatrix() { return matrix; }
	public Matrix getBiclusterMatrix() { return biclusterMatrix; }
	public int[] getClustersArray() {return clusters;}
	
	public RunBicFinder(CyNetwork network, String weightAttributes[],
            TaskMonitor monitor, BicFinderContext context) {
		//super(network, weightAttributes, metric, monitor);
		this.network = network;
		this.weightAttributes = weightAttributes;
		this.monitor = monitor;
		this.context = context;
		this.alpha = context.alpha.getValue();
		this.delta = context.delta.getValue();
	}

	public Integer[] cluster(boolean transpose) {
		// Create the matrix
		matrix = new Matrix(network, weightAttributes, transpose, ignoreMissing, selectedOnly);
		monitor.showMessage(TaskMonitor.Level.INFO,"cluster matrix has "+matrix.nRows()+" rows");
		
		// Create a weight vector of all ones (we don't use individual weighting, yet)
		matrix.setUniformWeights();
				
		if (monitor != null) 
			monitor.setStatusMessage("Clustering...");
		
		int nelements = matrix.nRows();
		int nattrs = matrix.nColumns();
				
				
		Integer[] rowOrder;
		rowOrder = biclusterMatrix.indexSort(clusters, clusters.length);
		return rowOrder;
	}
}
