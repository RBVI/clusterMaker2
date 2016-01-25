package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DBSCAN;

import java.util.ArrayList;
import java.util.HashSet;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractKClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.fft.FFTContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;

public class RunDBSCAN  {

	protected CyNetwork network;
	protected String[] weightAttributes;
	protected DistanceMetric metric;
	protected CyMatrix matrix;
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

	public CyMatrix getMatrix() { return matrix; }
	public int getNClusters() {return nClusters;}

	public int[] cluster(boolean transpose) {

		// Create the matrix
		CyMatrix cData = CyMatrixFactory.makeSmallMatrix(network, weightAttributes, selectedOnly, ignoreMissing, transpose, false);
		monitor.showMessage(TaskMonitor.Level.INFO,"cluster matrix has "+matrix.nRows()+" rows");
		DistanceMetric metric = context.metric.getSelectedValue();

		// Create a weight vector of all ones (we don't use individual weighting, yet)
		// matrix.setUniformWeights();

		if (monitor != null) 
			monitor.setStatusMessage("Clustering...");

		int nelements = matrix.nRows();
		int ifound = 1;
		int currentC = -1;
		int[] clusters = new int[nelements];

		// calculate the distances and store in distance matrix
		distanceMatrix = matrix.getDistanceMatrix(metric).toArray();

		unvisited = new ArrayList<Integer>();

		//Initializing all nodes as unvisited and clusters to -1
		for(int i = 0; i < nelements; i++){
			unvisited.add(i);
			clusters[i] = -1;
		}

		while(unvisited.size() > 0){
			int p = unvisited.get(0);
			unvisited.remove(0);

			ArrayList<Integer> neighborPts = regionQuery(p);
			// System.out.println("Node "+p+" has "+neighborPts.size()+" neighbors");

			if(neighborPts.size() < minPts){
				clusters[p] = -1;
			}
			else{
				currentC += 1;
				expandCluster(p,neighborPts,currentC,clusters);
				// System.out.println("Node "+p+" has "+neighborPts.size()+" neighbors after expansion");
			}
		}
		nClusters = currentC+1;
		// System.out.println("nClusters = "+nClusters);
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
				unvisited.remove(unvisited.indexOf(np));

				//Now fetch new neighboring points
				ArrayList<Integer> newNeighborPts = regionQuery(np);
				// System.out.println("Adding "+newNeighborPts.size()+" neighbors of "+np+" to cluster "+currentC);

				if(newNeighborPts.size() >= minPts){
					//Merge neighboring points
					for(Integer newNp: newNeighborPts){
						if (!neighborPts.contains(newNp))
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

		if (!neighborPts.contains(p))
			neighborPts.add(p);
		for(int i = 0; i < nelements; i++){
			if (i == p) continue;

			if(distanceMatrix[p][i] <= eps){
				// System.out.println("distanceMatrix["+p+"]["+i+"] = "+distanceMatrix[p][i]+"<="+eps);
				if (!neighborPts.contains(i))
					neighborPts.add(i);
			}
		}

		return neighborPts;
	}
}
