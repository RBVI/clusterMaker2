package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;


import java.awt.Color;

import javax.swing.JFrame;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import org.math.plot.plots.ScatterPlot;


public class RuntSNE {

	protected CyNetwork network;
	protected CyNetworkView networkView;
	protected tSNEContext context;
	protected TaskMonitor monitor;
	protected double perplexity;
	protected int initial_dimensions;
	protected int no_of_iterations;
	protected String mode;
	protected CyMatrix edgematrix;
	protected double eigenValues[];
	protected double eigenVectors[][];
	
	

	
	public RuntSNE(CyNetwork network, CyNetworkView networkView, 
	              tSNEContext context, TaskMonitor monitor,CyMatrix matrix
								 ){
		this.network = network;
		this.networkView = networkView;
		this.context = context;
		this.monitor = monitor;
		this.edgematrix=matrix;
		
		
	}
	
	public void run(){
		no_of_iterations=context.num_of_iterations;
		initial_dimensions=context.int_dims;
		perplexity=context.perplixity;
		
	TSneInterface tsne=new tSNECalculation();
	
	Matrix Y=tsne.tsne(edgematrix, 2, initial_dimensions, perplexity, no_of_iterations, true);
	
	
		
		Plot2DPanel plot = new Plot2DPanel();
	     
		 ScatterPlot setosaPlot = new ScatterPlot("setosa", Color.BLUE, Y.toArray());
	     plot.plotCanvas.setNotable(true);
	     plot.plotCanvas.setNoteCoords(true);
	     plot.plotCanvas.addPlot(setosaPlot);
	             
	     FrameView plotframe = new FrameView(plot);
	     plotframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	     plotframe.setVisible(true);
	

	//ScatterPlotDialog dialog = new ScatterPlotDialog(Y, null, variance);
		
	}

	

	}

