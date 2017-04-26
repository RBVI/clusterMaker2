package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEWrapper;


import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

import edu.ucsf.rbvi.clusterMaker2.internal.ui.ScatterPlotDialog;

import com.jujutsu.tsne.FastTSne;
import com.jujutsu.tsne.TSne;
import com.jujutsu.tsne.barneshut.BHTSne;

public class RuntSNE {

	protected CyNetwork network;
	protected CyNetworkView networkView;
	protected tSNEContext context;
	protected TaskMonitor monitor;
	protected CyMatrix matrix;

	private final ClusterManager manager;

	private CyMatrix Y;

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
		context.cancelled = false;
		context.setXin(matrix.toArray());
		TSne tsne;

		// System.out.println("Is Symmetrical "+matrix.isSymmetrical());
		if (context.useBarnesHut) {
			monitor.setTitle("Running t-Distributed Stochastic Neighbor (tSNE) using Barnes-Hut approximation");
			tsne = new BHTSne();

		} else {
			monitor.setTitle("Running t-Distributed Stochastic Neighbor (tSNE)");
			tsne = new FastTSne();
		}

		double[][] result = tsne.tsne(context, monitor);
		if (result == null && context.cancelled) {
			monitor.setStatusMessage("Cancelled by user");
			return;
		}

		Y = matrix.copy();
		Y.initialize(result.length, result[0].length, result);

		if (context.showScatterPlot) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					ScatterPlotDialog dialog = new ScatterPlotDialog(manager, "tSNE", monitor, Y);
				}
			});
		}

	}

	public void cancel() { 
		context.cancelled = true; 
	}

	CyMatrix getResult() { return Y; }
}

