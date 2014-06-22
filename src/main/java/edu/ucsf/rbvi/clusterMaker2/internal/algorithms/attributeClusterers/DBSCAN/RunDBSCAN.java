package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DBSCAN;

import java.util.ArrayList;
import java.util.HashSet;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractKClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.fft.FFTContext;

public class RunDBSCAN  {
	
	protected CyNetwork network;
	protected String[] weightAttributes;
	protected DistanceMetric metric;
	protected Matrix matrix;
	protected TaskMonitor monitor;
	protected boolean ignoreMissing = true;
	protected boolean selectedOnly = false;
	DBSCANContext context;
	protected int nClusters;
	double eps;
	int minPts;
	ArrayList<Integer> unvisited;
	double distanceMatrix[][];

	public RunDBSCAN(CyNetwork network, String weightAttributes[], DistanceMetric metric, 
            TaskMonitor monitor, DBSCANContext context) {
		//super(network, weightAttributes, metric, monitor);
		this.network = network;
		this.weightAttributes = weightAttributes;
		this.metric = metric;
		this.monitor = monitor;
		this.context = context;
		this.eps = context.eps;
		this.minPts = context.minPts;
		this.nClusters = 0;		
	}

	public Matrix getMatrix() { return matrix; }
	public int getNClusters() {return nClusters;}
	
	public int[] cluster(boolean transpose) {
		
		// Create the matrix
		matrix = new Matrix(network, weightAttributes, transpose, ignoreMissing, selectedOnly);
		monitor.showMessage(TaskMonitor.Level.INFO,"cluster matrix has "+matrix.nRows()+" rows");
		
		// Create a weight vector of all ones (we don't use individual weighting, yet)
		matrix.setUniformWeights();
				
		if (monitor != null) 
			monitor.setStatusMessage("Clustering...");
		
		int nelements = matrix.nRows();
		int ifound = 1;
		int currentC = -1;
		int[] clusters = new int[nelements];
		
		// calculate the distances and store in distance matrix
		distanceMatrix = new double[nelements][nelements];
		for (int i = 0; i < nelements; i++) {
			for (int j = i+1; j < nelements; j++) {
 				double distance = context.metric.getSelectedValue().getMetric(matrix, matrix, matrix.getWeights(), i, j);				
				distanceMatrix[i][j] = distance;
			}			
		}
		
		unvisited = new ArrayList<Integer>();
		
		//Initializing all nodes as unvisited and clusters to -1
		for(int i = 0; i < nelements; i++){
			unvisited.add(i);
			clusters[i] = -1;
		}
		
		while(unvisited.size() > 0){
			int p = unvisited.get(0);
			unvisited.remove(p);
			
			ArrayList<Integer> neighborPts = regionQuery(p);
			
			if(neighborPts.size() < minPts){
				clusters[p] = -1;
			}
			else{
				currentC += 1;
				expandCluster(p,neighborPts,currentC,clusters);
			}			
		}		
		nClusters = currentC+1;
		return clusters;
	}

	private void expandCluster(int p, ArrayList<Integer> neighborPts,
			int currentC, int[] clusters) {
		
		//Add p to current cluster
		clusters[p] = currentC;
		
		//Now expand for each neighbor
		for(int i = 0; i < neighborPts.size(); i++ ){
			
			int np = neighborPts.get(i);
			
			if(unvisited.contains(np)){
				
				//make neighboring point visited
				unvisited.remove(np);
				
				//Now fetch new neighboring points
				ArrayList<Integer> newNeighborPts = regionQuery(np);
				
				if(newNeighborPts.size() >= minPts){
					//Merge neighboring points
					for(Integer newNp: newNeighborPts){
						neighborPts.add(newNp);
					}
				}				
			}
						
			//Check if neighboring point is not assigned to any cluster
			if (clusters[np] == -1){
				clusters[np] = currentC;
			}					
		}		
	}

	private ArrayList<Integer> regionQuery(int p) {
		
		ArrayList<Integer> neighborPts = new ArrayList<Integer>();
		int nelements = distanceMatrix[p].length;
		
		for(int i = 0; i < nelements; i++){
			if (i == p) continue;
			
			if(distanceMatrix[p][i] <= eps){
				neighborPts.add(i);
			}
		}
		
		return neighborPts;
	}
}
