package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DBSCAN;

import java.util.ArrayList;
import java.util.HashSet;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractKClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.fft.FFTContext;

public class RunDBSCAN extends AbstractKClusterAlgorithm {
	
	DBSCANContext context;
	double eps;
	int minPts;
	ArrayList<Integer> unvisited;

	public RunDBSCAN(CyNetwork network, String weightAttributes[], DistanceMetric metric, 
            TaskMonitor monitor, DBSCANContext context) {
		super(network, weightAttributes, metric, monitor);
		this.context = context;
	}

	@Override
	public int kcluster(int nClusters, int nIterations, Matrix matrix,
			DistanceMetric metric, int[] clusters) {
		
		int nelements = matrix.nRows();
		int ifound = 1;
		int currentC = -1;
		
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
		return ifound;
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
		
		return neighborPts;
	}
	

}
