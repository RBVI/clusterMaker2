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
	
	private CyMatrix distanceMatrix = null;
	private CyMatrix matrix = null;
	private List<CyNode> nodes = null;
	private boolean debug = true;
	private int nThreads = Runtime.getRuntime().availableProcessors()-1;
	
	public RunPCoA(CyMatrix dMat, 
             TaskMonitor monitor )
	{
			
		this.distanceMatrix = dMat;
		this.matrix = dMat.copy();
		
		nodes = distanceMatrix.getRowNodes();
	
		monitor.showMessage(TaskMonitor.Level.INFO,"Threads = "+nThreads);
		monitor.showMessage(TaskMonitor.Level.INFO,"Matrix info: = "+distanceMatrix.printMatrixInfo());
		
		
	}
	
	public void cancel () { canceled = true; }

	public void setDebug(boolean debug) { this.debug = debug; }
	
	public void run(CyMatrix matrix){
		System.out.println("Calculating values");
		double data[][]=matrix.toArray();
		System.out.println("Length "+ data.length);
		
		System.out.println("Checking CyMatrix symmetrical "+matrix.isSymmetrical());
		CalculationMatrix calc=new CalculationMatrix(matrix.nRows(), matrix.nColumns(), data, 0, 0, 0);
		System.out.println("Added data to the matrix ");
		calc.eigenAnalysis();
		System.out.println("Completed Eigen Analysis");
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

