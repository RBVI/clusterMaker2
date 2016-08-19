package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;


import java.awt.Color;

import javax.swing.JFrame;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
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
		tSNEContext.GetVisulaisation val=context.modeselection.getSelectedValue();
		mode=val.getValue();
		
		System.out.println("Is Symmetrical "+matrix.isSymmetrical());
		TSneInterface tsne=new tSNECalculation();
		Matrix Y=null;
		if(mode==1){//for edges
			Y=tsne.tsne(matrix, 2, initial_dimensions, perplexity, no_of_iterations, true);
		}else if(mode==0){//for nodes
			metric=context.metric.getSelectedValue();
			distances=matrix.getDistanceMatrix(metric);
			Y=tsne.tsne(distances, 2, initial_dimensions, perplexity, no_of_iterations, true);
		}
	
		
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

