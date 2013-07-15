package org.cytoscape.myapp.internal.algorithms.networkClusterers.FCM;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.lang.Math;


import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.group.*;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import org.cytoscape.myapp.internal.algorithms.NodeCluster;
import org.cytoscape.myapp.internal.algorithms.DistanceMatrix;
import org.cytoscape.myapp.internal.algorithms.networkClusterers.MCL.Matrix;

import cern.colt.function.IntIntDoubleFunction;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

public class RunFCM {

	Random random = null;
	HashMap<String,List<CyNode>> groupMap = null;

	private int number_iterations; //number of inflation/expansion cycles
	private int number_clusters;
	private double findex; // fuzziness index
	private double beta; // Termination Criterion
	private List<CyNode> nodes;
	private List<CyEdge> edges;
	private boolean canceled = false;
	private Logger logger;
	public final static String GROUP_ATTRIBUTE = "__FCMGroups";
	protected int clusterCount = 0;
	private boolean createMetaNodes = false;
	private DistanceMatrix distanceMatrix = null;
	private DoubleMatrix2D matrix = null;
	private Matrix data = null;
	private boolean debug = false;
	private int nThreads = Runtime.getRuntime().availableProcessors()-1;
	
	public RunFCM (Matrix data,DistanceMatrix dMat, int num_iterations, int cClusters, double findex, double beta, Logger logger, int maxThreads){
		
		this.distanceMatrix = dMat;
		this.data = data;
		this.number_iterations = num_iterations;
		this.number_clusters = cClusters;
		this.findex = findex;
		this.beta = beta;
		this.logger = logger;
		nodes = distanceMatrix.getNodes();
		edges = distanceMatrix.getEdges();
		this.matrix = distanceMatrix.getDistanceMatrix();
		if (maxThreads > 0)
			nThreads = maxThreads;
		else
			nThreads = Runtime.getRuntime().availableProcessors()-1;
	}
	
	public void halt () { canceled = true; }

	public void setDebug(boolean debug) { this.debug = debug; }
	
	public List<NodeCluster> run(TaskMonitor monitor){
		
		CyAppAdapter adapter;
		CyApplicationManager manager = adapter.getCyApplicationManager();
		CyNetwork network = manager.getCurrentNetwork();	
		Long networkID = network.getSUID();		

		CyTable netAttributes = network.getDefaultNetworkTable();	
		CyTable nodeAttributes = network.getDefaultNodeTable();			

		long startTime = System.currentTimeMillis();
		
		random = null;
		int nelements = data.rows();
		
		//Matrix to store the temporary cluster membership values of elements 
		double [][] tClusterMemberships = new double[nelements][number_clusters];
		
		// Matrix to store cluster memberships of the previous iteration
		double [][] prevClusterMemberships = new double[nelements][number_clusters];
		
		// This matrix will store the centroid data
		Matrix cData = new Matrix(nClusters, data.nColumns());
		
		int iteration = 0;
		boolean end = false;
		do{
			
			if (monitor != null)
				monitor.setProgress(((double)iteration/(double)number_iterations));
			
			// Initializing the membership values by randomly assigning a cluster to each element
			if(iteration == 0 && number_iterations != 0){
				
				randomAssign(number_clusters, nelements, tClusterMemberships);
				prevClusterMemberships = tClusterMemberships;
				// Find the centers
				getFuzzyCenters(cData, tClusterMemberships);
			}
			
			//Calculate Fuzzy Memberships
			getClusterMemberships(cData,tClusterMemberships);
			
			// Now calculate the new fuzzy centers
			getFuzzyCenters(cData,tClusterMemberships);
			
			end = checkEndCriterion(tClusterMemberships,prevClusterMemberships);
			if (end){
				break;
			}
							
		}
		while (++iteration < number_iterations);
		
	}
	
	public void getFuzzyCenters(Matrix cData, double [][] tClusterMemberships){
		
		// To store the sum of memberships(raised to fuzziness index) corresponding to each cluster
		double[] totalMemberships = new double [number_clusters];
		int nelements = data.rows();
		
		//Calculating the total membership values
		for (int i = 0; i < number_clusters; i++){
			totalMemberships[i] = 0;
			for(int j = 0; j < nelements; j++ ){
				totalMemberships[i] += Math.pow(tClusterMemberships[j][i],findex);
			}
			
			for (int k = 0; k < nelements; k++) {
				//int i = clusterid[k];
				for (int j = 0; j < data.nColumns(); j++) {
					if (data.hasValue(k,j)) {
						double cValue = 0.0;
						double dataValue = data.getValue(k,j).doubleValue();						
						for (int i1 = 0; i1 < number_clusters; i1++){
							if (cData.hasValue(i1,j)){
								cValue = cData.getValue(i1,j).doubleValue();
							}
							
							// Multiplying data value with the element's membership in the cluster(raised to fuzziness index)
							double temp = Math.pow(tClusterMemberships[k][i1],findex) * dataValue;
							
							cData.setValue(i1,j, Double.valueOf(cValue+temp));
						}
						
					}
				}
			}
			for (int i = 0; i < number_clusters; i++) {
				for (int j = 0; j < data.nColumns(); j++) {
					if (totalMemberships[i] > 0.0) {
						double temp = cData.getValue(i,j).doubleValue() / totalMemberships[i];
						cData.setValue(i,j,Double.valueOf(temp));
					}
				}
			}
		}
		
	}
	
	public void getClusterMemberships(Matrix cData, double [][]tClusterMemberships){
		
		int nelements = data.rows();
		double fpower = 2/(findex - 1);
		for (int i = 0; i < nelements; i++) {
			
			double distance_ic;
			for(int c = 0; c < number_clusters; c++){
				double sumDistanceRatios = 0;
				double distance_ik;
				distance_ic = metric.getMetric(data, cData, data.getWeights(), i, c);
				
				for(int k = 0; k < number_clusters; k++){
					
					distance_ik = metric.getMetric(data, cData, data.getWeights(), i, k);
					sumDistanceRatios += Math.pow((distance_ic/distance_ik), fpower);
					
				}
				
				tClusterMemberships[i][c] = 1/sumDistanceRatios;
				
			}
			
		}
			
	}
	
	public boolean checkEndCriterion(double[][] tClusterMemberships,double[][] prevClusterMemberships){
		
		boolean endCheck = false;
		
		double[][] differences = new double [data.rows()][number_clusters] ;
		double maxdiff = -1;
		for (int i = 0; i < data.rows(); i++){
			
			for (int j = 0; j < number_clusters; j++){
				
				differences[i][j] = Math.abs( tClusterMemberships[i][j] - prevClusterMemberships[i][j]);
				
				if (differences[i][j] > maxdiff){
					maxdiff = differences[i][j];
				}
			}
		}
		
		if( maxdiff != -1 && maxdiff < beta){
			endCheck = true;
		}
		
		return endCheck;
	}


}






