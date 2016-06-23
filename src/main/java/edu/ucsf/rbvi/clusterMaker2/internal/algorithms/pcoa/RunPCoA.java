package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.TaskMonitor;
import org.netlib.util.doubleW;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ComputationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class RunPCoA {

	
	private boolean canceled = false;
	protected int clusterCount = 0;
	private CyMatrix distanceMatrix = null;
	
	
	private boolean debug = true;
	private Matrix dismatrix=null;
	private int nThreads = Runtime.getRuntime().availableProcessors()-1;
	
	
	
	public RunPCoA(CyMatrix dMat, boolean selectonly, boolean ignore_missing, 
             TaskMonitor monitor )
	{
			
		this.dismatrix=dMat.getDistanceMatrix(DistanceMetric.EUCLIDEAN);
		
		//nodes = distanceMatrix.getRowNodes();
		
		for(int i=0;i<dismatrix.nRows();i++){
			for(int j=0;j<dismatrix.nColumns();j++){
				System.out.print(dismatrix.doubleValue(i, j)+" ");
			}
			System.out.println("");
		}
		
		monitor.showMessage(TaskMonitor.Level.INFO,"Threads = "+nThreads);
		monitor.showMessage(TaskMonitor.Level.INFO,"Matrix info: = "+distanceMatrix.printMatrixInfo());
		
	}
	
	public void cancel () { canceled = true; }

	public void setDebug(boolean debug) { this.debug = debug; }
	
	
	
	
	/**
	 * Debugging routine to print out information about a matrix
	 *
	 * @param matrix the matrix we're going to print out information about
	 */
	private void printMatrixInfo(DoubleMatrix2D matrix) {
		debugln("Matrix("+matrix.rows()+", "+matrix.columns()+")");
		if (matrix.getClass().getName().indexOf("Sparse") >= 0)
			debugln(" matrix is sparse");
		else
			debugln(" matrix is dense");
		debugln(" cardinality is "+matrix.cardinality());
	}

	/**
	 * Debugging routine to print out information about a matrix
	 *
	 * @param matrix the matrix we're going to print out information about
	 */
	private void printMatrix(DoubleMatrix2D matrix) {
		for (int row = 0; row < matrix.rows(); row++) {
			debug(distanceMatrix.getRowLabel(row)+":\t"); //node.getIdentifier()
			for (int col = 0; col < matrix.columns(); col++) {
				debug(""+matrix.get(row,col)+"\t");
			}
			debugln();
		}
		debugln("Matrix("+matrix.rows()+", "+matrix.columns()+")");
		if (matrix.getClass().getName().indexOf("Sparse") >= 0)
			debugln(" matrix is sparse");
		else
			debugln(" matrix is dense");
		debugln(" cardinality is "+matrix.cardinality());
	}

	private void debugln(String message) {
		if (debug) System.out.println(message);
	}

	private void debugln() {
		if (debug) System.out.println();
	}

	private void debug(String message) {
		if (debug) System.out.print(message);
	}

	

	
	
	

	
	
	
	}

