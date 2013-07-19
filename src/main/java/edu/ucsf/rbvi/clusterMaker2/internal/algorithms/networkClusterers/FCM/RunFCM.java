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
import org.cytoscape.myapp.internal.algorithms.attributeClusterers.Matrix;
import org.cytoscape.myapp.internal.algorithms.attributeClusterers.DistanceMetric;

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
	private DistanceMetric metric;
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
	
	public RunFCM (Matrix data,DistanceMatrix dMat, int num_iterations, int cClusters,DistanceMetric metric, double findex, double beta, Logger logger ){
		
		this.distanceMatrix = dMat;
		this.data = data;
		this.number_iterations = num_iterations;
		this.number_clusters = cClusters;
		this.findex = findex;
		this.beta = beta;
		this.metric = metric;
		this.logger = logger;
		nodes = distanceMatrix.getNodes();
		edges = distanceMatrix.getEdges();
		this.matrix = distanceMatrix.getDistanceMatrix();
		/*
		if (maxThreads > 0)
			nThreads = maxThreads;
		else
			nThreads = Runtime.getRuntime().availableProcessors()-1;
			*/
	}
	
	public void halt () { canceled = true; }

	public void setDebug(boolean debug) { this.debug = debug; }
	
	/*
	 * The method run has the actual implementation of the fuzzy c-means code
	 * @param monitor, Task monitor for the process
	 * @return method returns a 2D array of cluster membership values
	 */
	public double[][] run(TaskMonitor monitor){
		
		CyAppAdapter adapter;
		CyApplicationManager manager = adapter.getCyApplicationManager();
		CyNetwork network = manager.getCurrentNetwork();	
		Long networkID = network.getSUID();		

		CyTable netAttributes = network.getDefaultNetworkTable();	
		CyTable nodeAttributes = network.getDefaultNodeTable();			

		long startTime = System.currentTimeMillis();
		
		random = null;
		int nelements = data.nRows();
		
		//Matrix to store the temporary cluster membership values of elements 
		double [][] tClusterMemberships = new double[nelements][number_clusters];
		
		//Initializing all membership values to 0
		for (int i = 0; i < nelements; i++){
			for (int j = 0; j < number_clusters; j++){
				tClusterMemberships[i][j] = 0;
			}
		}
		
		// Matrix to store cluster memberships of the previous iteration
		double [][] prevClusterMemberships = new double[nelements][number_clusters];
		
		// This matrix will store the centroid data
		Matrix cData = new Matrix(number_clusters, data.nColumns());
		
		int iteration = 0;
		boolean end = false;
		do{
			
			if (monitor != null)
				monitor.setProgress(((double)iteration/(double)number_iterations));
			
			// Initializing the membership values by randomly assigning a cluster to each element
			if(iteration == 0 && number_iterations != 0){
				
				randomAssign(tClusterMemberships);
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
		
		return tClusterMemberships;
	}
	
	/*
	 * The method getFuzzyCenters calculates the fuzzy centers from the cluster memberships and node attributes.
	 * 
	 *  @param cData is a matrix to store the attribute values for the fuzzy cluster centers
	 *  @param tClusterMemberships has the fuzzy membership values of elements for the clusters 
	 */
	
	public void getFuzzyCenters(Matrix cData, double [][] tClusterMemberships){
		
		// To store the sum of memberships(raised to fuzziness index) corresponding to each cluster
		double[] totalMemberships = new double [number_clusters];
		int nelements = data.nRows();
		
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
			for (int i1 = 0; i1 < number_clusters; i1++) {
				for (int j = 0; j < data.nColumns(); j++) {
					if (totalMemberships[i1] > 0.0) {
						double temp = cData.getValue(i1,j).doubleValue() / totalMemberships[i1];
						cData.setValue(i1,j,Double.valueOf(temp));
					}
				}
			}
		}
		
	}
	
	/*
	 * The method getClusterMemberships calculates the new cluster memberships of elements
	 * 
	 * @param cData is a matrix has the attribute values for the fuzzy cluster centers
	 * @param the new fuzzy membership values of elements for the clusters will be stored in tClusterMemberships
	 */
	
	public void getClusterMemberships(Matrix cData, double [][]tClusterMemberships){
		
		int nelements = data.nRows();
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
	
	/*
	 * The method checkEndCriterion checks whether the maximum change in the cluster membership values is less than beta or not
	 * 
	 * @param tClusterMemberships has the fuzzy membership values of the current iteration
	 * @param prevClusterMemberships has the fuzzy membership values of the last iteration
	 * @return endCheck is true if the maximum change in membership values is less than beta, false otherwise.
	 */
	
	public boolean checkEndCriterion(double[][] tClusterMemberships,double[][] prevClusterMemberships){
		
		boolean endCheck = false;
		
		double[][] differences = new double [data.nRows()][number_clusters] ;
		double maxdiff = -1;
		for (int i = 0; i < data.nRows(); i++){
			
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
	
	/*
	 *  randomAssign assigns cluster memberships randomly for the purpose of initialization. 
	 *  each element is assigned one cluster and the other membership values are set to 0
	 */
	private void randomAssign(double[][] tClusterMemberships){
		
		int nelements = data.nRows();
		int n = nelements - number_clusters;
		int k = 0;
		int i = 0;
		int[] clusterID = new int[nelements];
		
		for (i = 0; i < number_clusters-1; i++) {
			double p = 1.0/(number_clusters-1);
			int j = binomial(n, p);
			n -= j;
			j += k+1; // Assign at least one element to cluster i
			for (;k<j; k++) clusterID[k] = i;
		}
		
		// Assign the remaining elements to the last cluster
		for (; k < nelements; k++) clusterID[k] = i;
		
		// Create a random permutation of the cluster assignments
		for (i = 0; i < nelements; i++) {
			int j = (int) (i + (nelements-i)*uniform());
			k = clusterID[j];
			clusterID[j] = clusterID[i];
			clusterID[i] = k;
		}
		
		for(i=0; i<nelements; i++){
			tClusterMemberships[i][clusterID[i]] = 1;
		}
		
	}

	/**
	 * This routine generates a random number between 0 and n inclusive, following
	 * the binomial distribution with probability p and n trials. The routine is
	 * based on the BTPE algorithm, described in:
	 * 
	 * Voratas Kachitvichyanukul and Bruce W. Schmeiser:
	 * Binomial Random Variate Generation
	 * Communications of the ACM, Volume 31, Number 2, February 1988, pages 216-222.
	 * 
	 * @param p The probability of a single event.  This should be less than or equal to 0.5.
	 * @param n The number of trials
	 * @return An integer drawn from a binomial distribution with parameters (p, n).
	 */

	private int binomial (int n, double p) {
		double q = 1 - p;
		if (n*p < 30.0) /* Algorithm BINV */
		{ 
			double s = p/q;
			double a = (n+1)*s;
			double r = Math.exp(n*Math.log(q)); /* pow() causes a crash on AIX */
			int x = 0;
			double u = uniform();
			while(true)
			{ 
				if (u < r) return x;
				u-=r;
				x++;
				r *= (a/x)-s;
			}
		}
		else /* Algorithm BTPE */
		{ /* Step 0 */
			double fm = n*p + p;
			int m = (int) fm;
			double p1 = Math.floor(2.195*Math.sqrt(n*p*q) -4.6*q) + 0.5;
			double xm = m + 0.5;
			double xl = xm - p1;
			double xr = xm + p1;
			double c = 0.134 + 20.5/(15.3+m);
			double a = (fm-xl)/(fm-xl*p);
			double b = (xr-fm)/(xr*q);
			double lambdal = a*(1.0+0.5*a);
			double lambdar = b*(1.0+0.5*b);
			double p2 = p1*(1+2*c);
			double p3 = p2 + c/lambdal;
			double p4 = p3 + c/lambdar;
			while (true)
			{ /* Step 1 */
				int y;
				int k;
				double u = uniform();
				double v = uniform();
				u *= p4;
				if (u <= p1) return (int)(xm-p1*v+u);
				/* Step 2 */
				if (u > p2)
				{ /* Step 3 */
					if (u > p3)
					{ /* Step 4 */
						y = (int)(xr-Math.log(v)/lambdar);
						if (y > n) continue;
						/* Go to step 5 */
						v = v*(u-p3)*lambdar;
					}
					else
					{
						y = (int)(xl+Math.log(v)/lambdal);
						if (y < 0) continue;
						/* Go to step 5 */
						v = v*(u-p2)*lambdal;
					}
				}
				else
				{
					double x = xl + (u-p1)/c;
					v = v*c + 1.0 - Math.abs(m-x+0.5)/p1;
					if (v > 1) continue;
					/* Go to step 5 */
					y = (int)x;
				}
				/* Step 5 */
				/* Step 5.0 */
				k = Math.abs(y-m);
				if (k > 20 && k < 0.5*n*p*q-1.0)
				{ /* Step 5.2 */
					double rho = (k/(n*p*q))*((k*(k/3.0 + 0.625) + 0.1666666666666)/(n*p*q)+0.5);
					double t = -k*k/(2*n*p*q);
					double A = Math.log(v);
					if (A < t-rho) return y;
					else if (A > t+rho) continue;
					else
					{ /* Step 5.3 */
						double x1 = y+1;
						double f1 = m+1;
						double z = n+1-m;
						double w = n-y+1;
						double x2 = x1*x1;
						double f2 = f1*f1;
						double z2 = z*z;
						double w2 = w*w;
						if (A > xm * Math.log(f1/x1) + (n-m+0.5)*Math.log(z/w)
						      + (y-m)*Math.log(w*p/(x1*q))
						      + (13860.-(462.-(132.-(99.-140./f2)/f2)/f2)/f2)/f1/166320.
						      + (13860.-(462.-(132.-(99.-140./z2)/z2)/z2)/z2)/z/166320.
						      + (13860.-(462.-(132.-(99.-140./x2)/x2)/x2)/x2)/x1/166320.
						      + (13860.-(462.-(132.-(99.-140./w2)/w2)/w2)/w2)/w/166320.)
							continue;
						return y;
					}
				}
				else
				{ /* Step 5.1 */
					int i;
					double s = p/q;
					double aa = s*(n+1);
					double f = 1.0;
					for (i = m; i < y; f *= (aa/(++i)-s));
					for (i = y; i < m; f /= (aa/(++i)-s));
					if (v > f) continue;
					return y;
				}
			}
		}
	}

	private double uniform() {
		if (random == null) {
			// Date date = new Date();
			// random = new Random(date.getTime());
			// Use an unseeded random so that our silhouette results are comparable
			random = new Random();
		}
		return random.nextDouble();
	}

}






