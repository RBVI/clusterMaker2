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
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import org.netlib.util.doubleW;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.matrix.tdouble.DoubleFactory2D;
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ComputationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ResultPanelPCA;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class RunPCoA {

	
	private boolean canceled = false;
	protected int clusterCount = 0;
	
	private TaskMonitor monitor;
	private PCoAContext context;
	private CyMatrix distanceMatrix = null;
	private CyMatrix matrix = null;
	private CyNetwork network;
	private CyNetworkView networkView; 
	private List<CyNode> nodes = null;
	private boolean debug = true;
	private int nThreads = Runtime.getRuntime().availableProcessors()-1;
	private boolean scale;
	private int neg;
	
	public RunPCoA(CyMatrix dMat,CyNetwork network, CyNetworkView networkView ,PCoAContext context,boolean scale, int neg, TaskMonitor monitor )
	{
			
		this.distanceMatrix = dMat;
		this.monitor = monitor;
		this.scale = scale;
		this.neg = neg;
		this.context=context;
		this.network=network;
		this.networkView=networkView;
		
		monitor.showMessage(TaskMonitor.Level.INFO,"Threads = "+nThreads);
		//monitor.showMessage(TaskMonitor.Level.INFO,"Matrix info: = "+distanceMatrix.printMatrixInfo());
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
		double eigenValues[]=calc.eigenAnalysis();
		System.out.println("Completed Eigen Analysis");
		double variance[]=calc.computeVariance(eigenValues);
		System.out.println("Completed Variance Calculation");
		CyMatrix components[]=calc.getCooridinates(distanceMatrix);
		System.out.println("Completed Coordinates Calculation");
		if(context.pcoaResultPanel)
			ResultPanelPCA.createAndShowGui(components, network, networkView, 
			                                matrix.getRowNodes(), variance);
		//System.out.println("Completed Variance Explained");
		//calc.negativeEigenAnalysis();
		//System.out.println("Completed Negative Eigen Analysis");
		//calc.scaleEigenVectors();
		//System.out.println("Completed Scale Eigen Vetors");

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

