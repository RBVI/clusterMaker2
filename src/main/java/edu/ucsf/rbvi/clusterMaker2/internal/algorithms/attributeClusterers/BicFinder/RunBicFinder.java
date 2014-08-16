package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BicFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
	protected int[][] discrete_matrix;
	protected List<Map<Integer,Integer>> dag;
	protected List<Map<Integer,List<Boolean>>> csl;
	
	protected Double geneRho[][];
	protected Double conditionRho[][];
	
	protected int[] clusters;
	protected TaskMonitor monitor;
	protected boolean ignoreMissing = true;
	protected boolean selectedOnly = false;
	BicFinderContext context;	
	double alpha;
	double delta;
	
	int nelements;
	int nattrs;
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
		
		nelements = matrix.nRows();
		nattrs = matrix.nColumns();
		
		discrete_matrix = getDiscreteMatrix();
		generateCSL();
		dag = generateDag();
		
		Integer[] rowOrder;
		rowOrder = biclusterMatrix.indexSort(clusters, clusters.length);
		return rowOrder;
	}
	
	private void generateCSL() {
		csl = new ArrayList<Map<Integer,List<Boolean>>>(nelements);
		for(int i = 0; i < nelements; i++){
			Map<Integer,List<Boolean>> simlistMap = new HashMap<Integer,List<Boolean>>();
			
			for(int j = i+1; j < nelements ; j++){
				
				List<Boolean> simlist = new ArrayList<Boolean>(nattrs-1);
				for(int k = 0; k < nattrs-1; k++){
					if(discrete_matrix[i][k] == discrete_matrix[j][k])simlist.add(true);
					else simlist.add(false);						
				}
				
				simlistMap.put(j, simlist);
			}
			csl.add(simlistMap);
		}		
	}
	
	private List<Map<Integer, Integer>> generateDag() {
		List<Map<Integer,Integer>> graph = new ArrayList<Map<Integer,Integer>>();
		
		for(int i = 0; i < nelements; i++){
			Map<Integer,Integer> edges = new HashMap<Integer,Integer>();
			for(int j = i+1; j < nelements ; j++){
				edges.put(j, Collections.frequency(csl.get(i).get(j), true));
			}
			graph.add(edges);
		}
		return graph;
	}
	private int[][] getDiscreteMatrix() {
		int M[][] = new int[nelements][nattrs-1];
		for(int i = 0 ;i < nelements; i++){
			for(int j = 0; j < nattrs-1; j++){
				Double current = matrix.getValue(i, j);
				if (current==null) current = 0.0;
				Double next = matrix.getValue(i, j+1);
				if (next==null) next = 0.0;
				
				if (current > next)M[i][j] = -1;
				else if(current < next)M[i][j] = 1;
				else M[i][j] = 0; 
			}			
		}		
		return M;
	}
}
