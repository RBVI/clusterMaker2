package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.text.DecimalFormat;
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
	
	private TaskMonitor monitor;
	private CyMatrix distanceMatrix = null;
	private CyMatrix matrix = null;
	private List<CyNode> nodes = null;
	private boolean debug = true;
	private int nThreads = Runtime.getRuntime().availableProcessors()-1;
	private boolean scale;
	private int neg;
	
	public RunPCoA(CyMatrix dMat, boolean scale, int neg, TaskMonitor monitor )
	{
			
		this.distanceMatrix = dMat;
		this.monitor = monitor;
		this.scale = scale;
		this.neg = neg;
		
		monitor.showMessage(TaskMonitor.Level.INFO,"Threads = "+nThreads);
		monitor.showMessage(TaskMonitor.Level.INFO,"Matrix info: = "+distanceMatrix.printMatrixInfo());
	}
	
	public void cancel () { canceled = true; }

	public void setDebug(boolean debug) { this.debug = debug; }
	
	public void run(){
		System.out.println("Calculating values");
		// double data[][]=matrix.toArray();
		System.out.println("Length "+ distanceMatrix.nRows());
		
		System.out.println("Checking CyMatrix symmetrical "+distanceMatrix.isSymmetrical());
		// TODO: make scale and neg tunables in PCoAContext
		CalculationMatrix calc=new CalculationMatrix(distanceMatrix, 0, scale, neg);
		System.out.println("Added data to the matrix ");
		calc.eigenAnalysis();
		System.out.println("Completed Eigen Analysis");
		calc.getVarianceExplained();
		System.out.println("Completed Variance Explained");
		calc.negativeEigenAnalysis();
		System.out.println("Completed Negative Eigen Analysis");
		calc.scaleEigenVectors();
		System.out.println("Completed Scale Eigen Vetors");
		
		System.out.println("Final Result");
		double eigen_vals[]=calc.getEigen_values();
		double scores[][]=calc.getScores();
		double combne[][]=calc.getCombine_array();
		
	}
	

	private static DecimalFormat scFormat = new DecimalFormat("0.###E0");
	private static DecimalFormat format = new DecimalFormat("0.###");
	
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

