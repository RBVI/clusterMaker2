package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;


import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

import edu.ucsf.rbvi.clusterMaker2.internal.ui.ScatterPlotDialog;

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
	private final ClusterManager manager;

	public RuntSNE(final ClusterManager manager, final CyNetwork network, final CyNetworkView networkView, 
	               tSNEContext context, TaskMonitor monitor,CyMatrix matrix) {
		this.network = network;
		this.manager = manager;
		this.networkView = networkView;
		this.context = context;
		this.monitor = monitor;
		this.matrix=matrix;
	}

	public void run(){
		no_of_iterations=context.iterations;
		initial_dimensions=context.dimensions;
		perplexity=context.perplixity;

		// System.out.println("Is Symmetrical "+matrix.isSymmetrical());
		monitor.setTitle("Running t-Distributed Stochastic Neighbor (tSNE)");
		TSneInterface tsne=new tSNECalculation(monitor);

		CyMatrix Y;
		int dims = matrix.nColumns();
		if (initial_dimensions > 0 && initial_dimensions < dims)
			Y = tsne.tsne(matrix, 2, initial_dimensions, perplexity, no_of_iterations, true);
		else
			Y = tsne.tsne(matrix, 2, dims, perplexity, no_of_iterations, false);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ScatterPlotDialog dialog = new ScatterPlotDialog(manager, "tSNE Scatter Plot", monitor, Y);
			}
		});

	}
}

