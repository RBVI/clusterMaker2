package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;


import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

// import org.math.plot.FrameView;
// import org.math.plot.Plot2DPanel;
// import org.math.plot.plots.ScatterPlot;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ScatterPlotDialog;


public class RuntSNE {

	protected CyNetwork network;
	protected CyNetworkView networkView;
	protected tSNEContext context;
	protected TaskMonitor monitor;
	protected double perplexity;
	protected int initial_dimensions;
	protected int no_of_iterations;
	protected int mode;
	protected CyMatrix matrix;

	protected double eigenValues[];
	protected double eigenVectors[][];
	protected DistanceMetric metric;
	protected Matrix distances;
	
	

	
	public RuntSNE(CyNetwork network, CyNetworkView networkView, 
	              tSNEContext context, TaskMonitor monitor,CyMatrix matrix
								 ){
		this.network = network;
		this.networkView = networkView;
		this.context = context;
		this.monitor = monitor;
		this.matrix=matrix;
		
	}
	
	public void run(){
		no_of_iterations=context.num_of_iterations;
		initial_dimensions=context.int_dims;
		perplexity=context.perplixity;
		
		// System.out.println("Is Symmetrical "+matrix.isSymmetrical());
		monitor.setTitle("Running t-Distributed Stochastic Neighbor (tSNE)");
		TSneInterface tsne=new tSNECalculation(monitor);
		CyMatrix Y=tsne.tsne(matrix, 2, initial_dimensions, perplexity, no_of_iterations, false);
	
		
	
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ScatterPlotDialog dialog = new ScatterPlotDialog("tSNE Scatter Plot", monitor, Y);
				}
			});

		
	}

	

	}

