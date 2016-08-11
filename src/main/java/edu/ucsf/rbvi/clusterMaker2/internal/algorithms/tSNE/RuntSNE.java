package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.addColumnVector;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.addRowVector;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.assignValuesToRow;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.concatenate;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.exp;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.fillMatrix;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.getValuesFromRow;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.mean;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.range;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.scalarDivide;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.scalarInverse;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.scalarMult;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.sqrt;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.square;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.sum;
import static edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE.times;

import java.awt.Color;
import java.util.Arrays;

import javax.swing.JFrame;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.MatrixtSNE;
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
	 Plot2DPanel plot = new Plot2DPanel();
     
	 ScatterPlot setosaPlot = new ScatterPlot("setosa", Color.BLUE, Y.toArray());
     plot.plotCanvas.setNotable(true);
     plot.plotCanvas.setNoteCoords(true);
     plot.plotCanvas.addPlot(setosaPlot);
             
     FrameView plotframe = new FrameView(plot);
     plotframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
     plotframe.setVisible(true);
		
	}
	//Calculate Principal Components for TSNE
	public CyMatrix[] computePCs(CyMatrix matrix, Matrix loadingMatrix){
		// matrix.writeMatrix("output.txt");

		Matrix C;
		
		// System.out.println("centralizing columns");
		matrix.centralizeColumns();
		
			C = matrix.covariance();
		

		// System.out.println("Finding eigenValues");
		eigenValues = C.eigenValues(true);
		// System.out.println("Finding eigenVectors");
		eigenVectors = C.eigenVectors();

		monitor.showMessage(TaskMonitor.Level.INFO, "Found "+eigenValues.length+" EigenValues");
		monitor.showMessage(TaskMonitor.Level.INFO, "Found "+eigenVectors.length+" EigenVectors of length "+eigenVectors[0].length);

		// Calculate the loading matrix
		calculateLoadingMatrix(matrix, loadingMatrix, eigenVectors, eigenValues);

		/*
		loadingMatrix.writeMatrix("loadingMatrix.txt");

		System.out.println("EigenValues: ");
		for (double v: eigenValues) {
			System.out.println("     "+v);
		}
		*/

		CyMatrix[] components = new CyMatrix[eigenValues.length];

		for(int j=eigenValues.length-1, k=0;j>=0;j--,k++){
			// double[] w = new double[vectors.length];
			CyMatrix result = CyMatrixFactory.makeLargeMatrix(matrix.getNetwork(), eigenValues.length, 1);
			for(int i=0;i<eigenVectors.length;i++){
				result.setValue(i,0,eigenVectors[i][j]);
			}
			// System.out.println("matrix: "+matrix.printMatrixInfo());
			// System.out.println("vector: "+result.printMatrixInfo());

			Matrix mat = matrix.multiplyMatrix(result);
			// System.out.println("After vector multiply: "+mat.printMatrixInfo());
			components[k] = matrix.copy(mat);
			components[k].printMatrixInfo();
			components[k].writeMatrix("component_"+k+".txt");
			// System.out.println("Component matrix "+k+" has "+components[k].getRowNodes().size()+" nodes");
		}

		return components;
	}
	
	private void calculateLoadingMatrix(CyMatrix matrix, Matrix loading, 
            double[][] eigenVectors, double[] eigenValues) {
		int rows = eigenVectors.length;
		int columns = eigenVectors[0].length;
		loading.initialize(rows, columns, new double[rows][columns]);

		// 	System.out.print("Eigenvectors:");
		for (int row = 0; row < rows; row++) {
			// System.out.print("\n"+matrix.getColumnLabel(row)+"\t");
			for (int column = columns-1, newCol=0; column >= 0; column--,newCol++) {
				// System.out.print(""+eigenVectors[row][column]+"\t");
				loading.setValue(row, newCol, 
						eigenVectors[row][column]*Math.sqrt(Math.abs(eigenValues[column])));
				// 	loading.setValue(row, newCol, eigenVectors[row][column]*eigenValues[column]);
			}
		}
		// System.out.println("\n");

		loading.setRowLabels(Arrays.asList(matrix.getColumnLabels()));
			for (int column = 0; column < columns; column++) {
				loading.setColumnLabel(column, "PC "+(column+1));
			}
	}

	

	}

