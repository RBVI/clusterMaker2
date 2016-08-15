package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.addColumnVector;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.addRowVector;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.assignValuesToRow;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.concatenate;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.exp;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.fillMatrix;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.getValuesFromRow;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.mean;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.range;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.scalarDivide;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.scalarInverse;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.scalarMult;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.sqrt;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.square;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.sum;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix.times;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.JFrame;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.CalculationMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ScatterPlotDialog;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.TSneInterface.R;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

import org.math.plot.FrameView;
import org.math.plot.Plot2DPanel;
import org.math.plot.plots.ScatterPlot;
import org.math.plot.PlotPanel;

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
	
	private int nThreads = Runtime.getRuntime().availableProcessors()-1;

	
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
	
	if(context.tsnePlot){
		
		Plot2DPanel plot = new Plot2DPanel();
	     
		 ScatterPlot setosaPlot = new ScatterPlot("setosa", Color.BLUE, Y.toArray());
	     plot.plotCanvas.setNotable(true);
	     plot.plotCanvas.setNoteCoords(true);
	     plot.plotCanvas.addPlot(setosaPlot);
	             
	     FrameView plotframe = new FrameView(plot);
	     plotframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	     plotframe.setVisible(true);
	}

	//ScatterPlotDialog dialog = new ScatterPlotDialog(Y, null, variance);
		
	}

	

	}

