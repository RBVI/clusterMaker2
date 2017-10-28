package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL;

import java.text.DecimalFormat;
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
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class RunMCL {

	private double inflationParameter; //density parameter 
	private int number_iterations; //number of inflation/expansion cycles
	private double clusteringThresh; //Threshold used to remove weak edges between distinct clusters
	private double maxResidual; //The maximum residual to look for
	private boolean canceled = false;
	protected int clusterCount = 0;
	private boolean createMetaNodes = false;
	private CyMatrix distanceMatrix = null;
	private CyMatrix matrix = null;
	private List<CyNode> nodes = null;
	private boolean debug = true;
	private int nThreads = Runtime.getRuntime().availableProcessors()-1;
	
	public RunMCL(CyMatrix dMat, double inflationParameter, int num_iterations, 
            double clusteringThresh, double maxResidual, int maxThreads, TaskMonitor monitor )
	{
			
		this.distanceMatrix = dMat;
		this.matrix = dMat.copy();
		this.inflationParameter = inflationParameter;
		this.number_iterations = num_iterations;
		this.clusteringThresh = clusteringThresh;
		this.maxResidual = maxResidual;
		nodes = distanceMatrix.getRowNodes();
		if (maxThreads > 0)
			nThreads = maxThreads;
		else
			nThreads = Runtime.getRuntime().availableProcessors()-1;

		monitor.showMessage(TaskMonitor.Level.INFO,"InflationParameter = "+inflationParameter);
		monitor.showMessage(TaskMonitor.Level.INFO,"Iterations = "+num_iterations);
		monitor.showMessage(TaskMonitor.Level.INFO,"Clustering Threshold = "+clusteringThresh);
		monitor.showMessage(TaskMonitor.Level.INFO,"Threads = "+nThreads);
		monitor.showMessage(TaskMonitor.Level.INFO,"Matrix info: = "+distanceMatrix.printMatrixInfo());
		
		
	}
	
	public void cancel () { canceled = true; }

	public void setDebug(boolean debug) { this.debug = debug; }
	
	public List<NodeCluster> run(CyNetwork network, TaskMonitor monitor)
	{
		Long networkID = network.getSUID();		//String networkID = network.getIdentifier();

		long startTime = System.currentTimeMillis();

		// Matrix matrix;
		double numClusters;

		debugln("Initial matrix:",matrix);

		// Normalize
		normalize(matrix, clusteringThresh, false);


		debugln("Normalized matrix:",matrix);

		double residual = 1.0;
		double progress = 1.0;
		// IntIntDoubleFunction myPow = new MatrixPow(inflationParameter);
		debugln("residual = "+residual+" maxResidual = "+maxResidual);
		for (int i=0; (i<number_iterations)&&(residual>maxResidual); i++)
		{

			progress = (double)(i*3)/(double)(number_iterations*3);
			monitor.setProgress(progress);

			// Expand
			{
				long t = System.currentTimeMillis();
				monitor.setStatusMessage("Iteration: "+(i+1)+" expanding "); //monitor.setStatus();
				debugln("Iteration: "+(i+1)+" expanding ");
				debugln("matrix: ",matrix);
				Matrix multiMatrix = matrix.ops().multiplyMatrix(matrix);
				matrix = matrix.copy(multiMatrix);

				// Normalize
				normalize(matrix, clusteringThresh, false);
				monitor.showMessage(TaskMonitor.Level.INFO,"Expansion "+(i+1)+" took "+(System.currentTimeMillis()-t)+"ms");
			}

			debugln("^ "+(i+1)+" after expansion");

			progress = (double)(i*3+1)/(double)(number_iterations*3);
			monitor.setProgress(progress);

			// Inflate
			// DoubleMatrix2D m = matrix.getColtMatrix();
			{
				long t = System.currentTimeMillis();
				monitor.setStatusMessage("Iteration: "+(i+1)+" inflating");	//monitor.setStatusMessage
				debugln("Iteration: "+(i+1)+" inflating");

				matrix.ops().powScalar(inflationParameter);

				// Normalize
				normalize(matrix, clusteringThresh, true);
			}

			debugln("^ "+(i+1)+" after inflation");

			progress = (double)(i*3+2)/(double)(number_iterations*3);
			monitor.setProgress(progress);

			/*
			double newResidual  = calculateResiduals(matrix);
			if (newResidual >= residual) break;
			residual = newResidual;
			*/

			residual = calculateResiduals(matrix);

			debugln("Iteration: "+(i+1)+" residual: "+residual);
			monitor.showMessage(TaskMonitor.Level.INFO,"Iteration "+(i+1)+" complete.  Residual="+residual);
			// System.out.println("Iteration: "+(i+1)+" residual: "+residual);

			if (canceled) {
				monitor.setStatusMessage("canceled"); 	//monitor.setStatusMessage
				return null;
			}
		}

		// If we're in debug mode, output the matrix
		debugln("Matrix: ", matrix);

		monitor.setStatusMessage("Assigning nodes to clusters");	//monitor.setStatusMessage

		clusterCount = 0;
		HashMap<Integer, NodeCluster> clusterMap = new HashMap<Integer, NodeCluster>();
		ClusterMatrix clusterMat = new ClusterMatrix(clusterMap);
		for (int row = 0; row < matrix.nRows(); row++) {
			for (int col = 0; col < matrix.nColumns(); col++) {
				clusterMat.apply(row, col, matrix.doubleValue(row, col));
			}
		}

		//Update node attributes in network to include clusters. Create cygroups from clustered nodes
		monitor.setStatusMessage("Created "+clusterCount+" clusters");
		monitor.setStatusMessage("Cluster map has "+clusterMap.keySet().size()+" clusters");
		// debugln("Created "+clusterCount+" clusters:");
		//
		if (clusterCount == 0) {
			monitor.setStatusMessage("Created 0 clusters!!!!");
			monitor.setStatusMessage("Cluster map has "+clusterMap.keySet().size()+" clusters");
			return null;
		}

		int clusterNumber = 1;
		HashMap<NodeCluster,NodeCluster> cMap = new HashMap<NodeCluster, NodeCluster>();
		for (NodeCluster cluster: NodeCluster.sortMap(clusterMap)) {

			if (cMap.containsKey(cluster))
				continue;

			cMap.put(cluster,cluster);

			cluster.setClusterNumber(clusterNumber);
			clusterNumber++;
		}

		monitor.setStatusMessage("Total runtime = "+(System.currentTimeMillis()-startTime)+"ms");

		Set<NodeCluster>clusters = cMap.keySet();
		return new ArrayList<NodeCluster>(clusters);
	}
	
	
	/**
	 * This method does threshold and normalization.  First, we get rid of
	 * any cells that have a value beneath our threshold and in the same pass
	 * calculate all of the column sums.  Then we use the column sums to normalize
	 * each column such that all of the cells in the column sum to 1.
	 *
	 * @param matrix the (sparse) data matrix we're operating on
	 * @param clusteringThresh the maximum value that we will take as a "zero" value
	 * @param prune if 'false', don't prune this pass
	 */
	private void normalize(Matrix matrix, double clusteringThresh, boolean prune)
	{
		// long startTime = System.currentTimeMillis();
		// Remove any really low values and create the sums array
		double [] sums = new double[matrix.nColumns()];
		if (prune)
			matrix.ops().threshold(clusteringThresh);

		for (int col = 0; col < matrix.nColumns(); col++) {
			sums[col] = matrix.ops().columnSum(col);
			if (sums[col] == 0.0) {
				matrix.setValue(col,col,1.0);
			} else {
				matrix.ops().divideScalarColumn(col, sums[col]);
			}
		}
		// System.out.println("Normalization took "+(System.currentTimeMillis()-startTime)+"ms");
	}
	
	/**
	 * This method calculates the residuals.  Calculate the sum and
	 * sum of squares for each row, then return the maximum residual.
	 *
	 * @param matrix the (sparse) data matrix we're operating on
	 * @return residual value
	 */
	private double calculateResiduals(Matrix matrix) {
		// Calculate and return the residuals
		double sums = 0.0;
		double sumSquares = 0.0;
		double residual = 0.0;
		for (int column = 0; column < matrix.nColumns(); column++) {
			sums = matrix.ops().columnSum(column);
			sumSquares = matrix.ops().columnSum2(column);
			residual = Math.max(residual, sums - sumSquares);
		}
		return residual;
	}

	private static DecimalFormat scFormat = new DecimalFormat("0.###E0");
	private static DecimalFormat format = new DecimalFormat("0.###");
	
	private void debugln(String message) {
		if (debug) System.out.println(message);
	}

	private void debugln(String message, CyMatrix matrix) {
		if (!debug) 
			return;
		System.out.println(message+matrix.printMatrixInfo());
	}

	private void debugln() {
		if (debug) System.out.println();
	}

	private void debug(String message) {
		if (debug) System.out.print(message);
	}

	
	private class ClusterMatrix {
		Map<Integer, NodeCluster> clusterMap;

		public ClusterMatrix(Map<Integer,NodeCluster> clusterMap) {
			this.clusterMap = clusterMap;
		}

		public double apply(int row, int column, double value) {
			if (canceled || value == 0.0 || Double.isNaN(value)) { return 0.0; }

			if (row == column) 
				return value;

			if (clusterMap.containsKey(column)) {
				// Already seen "column" -- get the cluster and add column
				NodeCluster columnCluster = clusterMap.get(column);
				if (clusterMap.containsKey(row)) {
					// We've already seen row also -- join them
					NodeCluster rowCluster = clusterMap.get(row);
					if (rowCluster == columnCluster) 
						return value;
					clusterCount--;
					// logger.debug("Joining cluster "+columnCluster.getClusterNumber()+" and "+rowCluster.getClusterNumber());
					// logger.debug("clusterCount = "+clusterCount);
					columnCluster.addAll(rowCluster);
				} else {
					// logger.debug("Adding "+row+" to "+columnCluster.getClusterNumber());
					// logger.debug("clusterCount = "+clusterCount);
					columnCluster.add(distanceMatrix.getRowNode(row));
				}
				updateClusters(columnCluster);
			} else {
				NodeCluster rowCluster;
				// First time we've seen "column" -- have we already seen "row"
				if (clusterMap.containsKey(row)) {
					// Yes, just add column to row's cluster
					rowCluster = clusterMap.get(row);
					// logger.debug("Adding "+column+" to "+rowCluster.getClusterNumber());
					// logger.debug("clusterCount = "+clusterCount);
					rowCluster.add(distanceMatrix.getColumnNode(column));
				} else {
					clusterCount++;
					rowCluster = new NodeCluster();
					// logger.debug("Created new cluster "+rowCluster.getClusterNumber()+" with "+row+" and "+column);
					// logger.debug("clusterCount = "+clusterCount);
					rowCluster.add(distanceMatrix.getColumnNode(column));
					rowCluster.add(distanceMatrix.getRowNode(row));
				}
				updateClusters(rowCluster);
			}
			return value;
		}

		private void updateClusters(NodeCluster cl) {
			for (CyNode node: cl) {
				clusterMap.put(nodes.indexOf(node), cl);
			}
		}
	}
}
