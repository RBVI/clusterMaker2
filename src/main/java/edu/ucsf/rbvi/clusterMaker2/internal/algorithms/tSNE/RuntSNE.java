package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;


import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.ScatterPlotDialog;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE.tSNEContext.GetVisulaisation;
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
		
		//System.out.println("Is Symmetrical "+matrix.isSymmetrical());
		monitor.setTitle("Running t-Distributed Stochastic Neighbor (tSNE)");
		TSneInterface tsne=new tSNECalculation(monitor);
		CyMatrix Y=null;
		
		if(mode==1){//for edges
			Y=tsne.tsne(matrix, 2, initial_dimensions, perplexity, no_of_iterations, true,network);
		}else if(mode==0){//for nodes
			//metric=context.metric.getSelectedValue();
			//distances=matrix.getDistanceMatrix(metric);
			//Y=tsne.tsne(distances, 2, initial_dimensions, perplexity, no_of_iterations, true);
			Y=tsne.tsne(matrix, 2, initial_dimensions, perplexity, no_of_iterations, false,network);
		}
	
		/*CyMatrix[] comps=getCooridinates(Y);
		double variance[]=computeVariance(Y.toArray()[0]);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				System.out.println("Scatter plot dialog call");
				ScatterPlotDialog dialog = new ScatterPlotDialog(comps, null, variance);
				dialog.setTitle("T-SNE ScatterPlot");
			}
		});*/
		
		
		/*Plot2DPanel plot = new Plot2DPanel();
	     
		 ScatterPlot setosaPlot = new ScatterPlot("setosa", Color.BLUE, Y.toArray());
	     plot.plotCanvas.setNotable(true);
	     plot.plotCanvas.setNoteCoords(true);
	     plot.plotCanvas.addPlot(setosaPlot);
	             
	     FrameView plotframe = new FrameView(plot);
	     plotframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	     plotframe.setVisible(true);*/
	

	//ScatterPlotDialog dialog = new ScatterPlotDialog(Y, null, variance);
		
	}

	public static double[] computeVariance(double[] values){
		double[] explainedVariance = new double[values.length];
		double total = 0.0;
		for (int i = 0; i < values.length; i++)
			total += values[i];

		for (int i = 0, j=values.length-1; j >= 0; j--,i++) {
			explainedVariance[i] = (values[j] / total) * 100;
		}

		return explainedVariance;
	}

	public CyMatrix[] getCooridinates(CyMatrix matrix){
		int length=matrix.nRows();
		CyMatrix[] components = new CyMatrix[length];

		for(int j=length-1, k=0;j>=0;j--,k++){
			// double[] w = new double[vectors.length];
			CyMatrix result = CyMatrixFactory.makeLargeMatrix(matrix.getNetwork(), matrix.nRows(),1);
			for(int i=0;i<length;i++){
				result.setValue(i,j,matrix.getValue(i, j));
			}
			// System.out.println("matrix: "+matrix.printMatrixInfo());
			// System.out.println("vector: "+result.printMatrixInfo());
			System.out.println("Matrix rows "+matrix.printMatrixInfo());
			System.out.println("Result rows "+result.printMatrixInfo());
			Matrix mat = matrix.multiplyMatrix(result);
			// System.out.println("After vector multiply: "+mat.printMatrixInfo());
			components[k] = matrix.copy(mat);
			components[k].printMatrixInfo();
			components[k].writeMatrix("component_"+k+".txt");
			// System.out.println("Component matrix "+k+" has "+components[k].getRowNodes().size()+" nodes");
		}

		return components;
	}

	}

