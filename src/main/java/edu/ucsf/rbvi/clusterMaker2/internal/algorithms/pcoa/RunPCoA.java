package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.List;


import javax.swing.SwingUtilities;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.ui.ScatterPlotDialog;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

public class RunPCoA {

	
	private boolean canceled = false;
	protected int clusterCount = 0;
	
	private final TaskMonitor monitor;
	private final PCoAContext context;
	private final CyMatrix distanceMatrix;
	private final CyNetwork network;
	private final CyNetworkView networkView; 
	private final ClusterManager manager; 
	
	
	private int nThreads = Runtime.getRuntime().availableProcessors()-1;
	private int neg;
	
	public RunPCoA(final ClusterManager manager, final CyMatrix dMat, final CyNetwork network, 
	               final CyNetworkView networkView,
	               final PCoAContext context, int neg, TaskMonitor monitor )
	{
			
		this.distanceMatrix = dMat;
		this.manager = manager;
		this.monitor = monitor;
		this.neg = neg;
		this.context=context;
		this.network=network;
		this.networkView=networkView;
		
		monitor.showMessage(TaskMonitor.Level.INFO,"Threads = "+nThreads);
		//monitor.showMessage(TaskMonitor.Level.INFO,"Matrix info: = "+distanceMatrix.printMatrixInfo());
	}
	
	public void cancel () { canceled = true; }

	
	public void run(){
		long startTime = System.currentTimeMillis();
		long time = startTime;

		System.out.println("Calculating values");
		// double data[][]=matrix.toArray();
		System.out.println("Length "+ distanceMatrix.nRows());
		
		//System.out.println("Checking CyMatrix symmetrical "+distanceMatrix.isSymmetrical());

		CalculationMatrix calc = new CalculationMatrix();

		// Get the GOwer's Matrix
		Matrix G = GowersMatrix.getGowersMatrix(distanceMatrix);
		long delta = System.currentTimeMillis()-time; time = System.currentTimeMillis();
		System.out.println("Got GowersMatrix in "+delta+"ms");
		System.out.println("Added data to the matrix ");
		double eigenValues[]=calc.eigenAnalysis(G);
		System.out.println("Completed Eigen Analysis, found "+eigenValues.length+" eigenvalues");
		double variance[]=calc.computeVariance(eigenValues);
		delta = System.currentTimeMillis()-time; time = System.currentTimeMillis();
		System.out.println("Completed Variance Calculation in "+delta+"ms");
		if(neg==2){//corect negative eigen values
			calc.correctEigenValues();
		}
		CyMatrix components[]=calc.getCoordinates(distanceMatrix);
		System.out.println("Completed Coordinates Calculation in "+(System.currentTimeMillis()-startTime)+"ms");
		System.out.println("Found "+components.length+" components");
		if(context.pcoaResultPanel){
			ResultPanelPCoA.createAndShowGui(components, network, networkView, distanceMatrix.getRowNodes(), variance);

		}			
		if(context.pcoaPlot) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					System.out.println("Scatter plot dialog call");
					ScatterPlotDialog dialog = new ScatterPlotDialog(manager, "PCoA Scatter Plot", monitor, components, variance);
				}
			});
		}
	}
}

