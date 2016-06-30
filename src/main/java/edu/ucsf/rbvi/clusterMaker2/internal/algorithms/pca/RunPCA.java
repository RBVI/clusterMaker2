/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;


// import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.ColtMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

/**
 *
 * @author root
 */
public class RunPCA {
	protected CyNetwork network;
	protected CyNetworkView networkView;
	protected PCAContext context;
	protected TaskMonitor monitor;
	protected String[] weightAttributes;
	protected boolean ignoreMissing;
	protected boolean selectedOnly;
	protected String matrixType;
	protected boolean standardize;
	double[] eigenValues;
	double[][] eigenVectors;

	private int nThreads = Runtime.getRuntime().availableProcessors()-1;

	public RunPCA(CyNetwork network, CyNetworkView networkView, 
	              PCAContext context, TaskMonitor monitor, String[] weightAttributes,
								String matrixType, boolean standardize){
		this.network = network;
		this.networkView = networkView;
		this.context = context;
		this.monitor = monitor;
		this.weightAttributes = weightAttributes;
		this.matrixType = matrixType;
		this.standardize = standardize;
		System.out.println("WeightAttributes:");
		if (weightAttributes == null) {
			System.out.println("   -- none --");
		} else {
			for (String weight: weightAttributes) { System.out.println("    "+weight); }
		}
		this.eigenValues = null;
		this.eigenVectors = null;
	}

	// this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
	// are sorted in increasing order
	public void runOnNodeToAttributeMatrix(){
		System.out.println("runOnNodeToAttributeMatrix");
		CyMatrix matrix = CyMatrixFactory.makeLargeMatrix(network, weightAttributes, context.selectedOnly, 
		                                                  context.ignoreMissing, false, false);
		// distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());

		// System.out.println("Creating computationMatrix");
		// ComputationMatrix mat = new ComputationMatrix(distanceMatrix);

		System.out.println("Computing principle components");
		Matrix loadingMatrix = new ColtMatrix();
		CyMatrix[] components = computePCs(matrix, loadingMatrix);

		double[] variance = computeVariance(eigenValues);

		if(context.pcaResultPanel)
			ResultPanelPCA.createAndShowGui(components, network, networkView, 
			                                matrix.getRowNodes(), variance);

		if(context.pcaPlot)
			ScatterPlotPCA.createAndShowGui(components, loadingMatrix, variance);

	}

	public CyMatrix[] computePCs(CyMatrix matrix, Matrix loadingMatrix){
		matrix.writeMatrix("output.txt");

		Matrix C;
		if (standardize) {
			for (int column = 0; column < matrix.nColumns(); column++) {
				matrix.standardizeColumn(column);
			}
		}
		System.out.println("centralizing columns");
		matrix.centralizeColumns();
		matrix.writeMatrix("centralized.txt");

		if (matrixType.equals("correlation")) {
			System.out.println("Creating correlation matrix");
			C = matrix.correlation();
			C.writeMatrix("correlation.txt");
		} else {
			// Covariance
			System.out.println("Creating covariance matrix");
			C = matrix.covariance();
			C.writeMatrix("covariance.txt");
		}

		System.out.println("Finding eigenValues");
		eigenValues = C.eigenValues(true);
		System.out.println("Finding eigenVectors");
		eigenVectors = C.eigenVectors();

		monitor.showMessage(TaskMonitor.Level.INFO, "Found "+eigenValues.length+" EigenValues");
		monitor.showMessage(TaskMonitor.Level.INFO, "Found "+eigenVectors.length+" EigenVectors of length "+eigenVectors[0].length);

		// Calculate the loading matrix
		calculateLoadingMatrix(matrix, loadingMatrix, eigenVectors, eigenValues);

		loadingMatrix.writeMatrix("loadingMatrix.txt");

		System.out.println("EigenValues: ");
		for (double v: eigenValues) {
			System.out.println("     "+v);
		}

		CyMatrix[] components = new CyMatrix[eigenValues.length];

		for(int j=eigenValues.length-1, k=0;j>=0;j--,k++){
			// double[] w = new double[vectors.length];
			CyMatrix result = CyMatrixFactory.makeLargeMatrix(matrix.getNetwork(), eigenValues.length, 1);
			for(int i=0;i<eigenVectors.length;i++){
				result.setValue(i,0,eigenVectors[i][j]);
			}
			System.out.println("matrix: "+matrix.printMatrixInfo());
			System.out.println("vector: "+result.printMatrixInfo());

			Matrix mat = matrix.multiplyMatrix(result);
			System.out.println("After vector multiply: "+mat.printMatrixInfo());
			components[k] = matrix.copy(mat);
			components[k].printMatrixInfo();
			components[k].writeMatrix("component_"+k+".txt");
			System.out.println("Component matrix "+k+" has "+components[k].getRowNodes().size()+" nodes");
		}

		return components;
	}

	public double[] computeVariance(double[] values){
		double[] explainedVariance = new double[values.length];
		double total = 0.0;
		for (int i = 0; i < values.length; i++)
			total += values[i];

		for (int i = 0, j=values.length-1; j >= 0; j--,i++) {
			explainedVariance[i] = (values[j] / total) * 100;
		}

		return explainedVariance;
	}

	/*
	public double[] computeVariance(CyMatrix matrix){
		Matrix C = matrix.covariance();

		double[] values = C.eigenValues(true);
		double[] variances = new double[values.length];

		double sum = 0;
		for(int i=0;i<values.length;i++)
			sum += values[i];

		for(int i=0,j=values.length-1; j>=0; j--,i++){
			variances[i] = (double) Math.round((values[j]*100/sum) * 100) / 100;
		}
		return variances;
	}
	*/

	private void calculateLoadingMatrix(CyMatrix matrix, Matrix loading, 
	                                    double[][] eigenVectors, double[] eigenValues) {
		int rows = eigenVectors.length;
		int columns = eigenVectors[0].length;
		loading.initialize(rows, columns, new double[rows][columns]);

		System.out.print("Eigenvectors:");
		for (int row = 0; row < rows; row++) {
			System.out.print("\n"+matrix.getColumnLabel(row)+"\t");
			for (int column = columns-1, newCol=0; column >= 0; column--,newCol++) {
				System.out.print(""+eigenVectors[row][column]+"\t");
				loading.setValue(row, newCol, 
				                 eigenVectors[row][column]*Math.sqrt(Math.abs(eigenValues[column])));
			}
		}
		System.out.println("\n");

		loading.setRowLabels(Arrays.asList(matrix.getColumnLabels()));
		for (int column = 0; column < columns; column++) {
			loading.setColumnLabel(column, "PC "+(column+1));
		}
	}

	private class CalculateComponent implements Runnable {
		ComputationMatrix[] components;
		ComputationMatrix mat;
		double[] w;
		int k;
		int type;
		
		public CalculateComponent(ComputationMatrix[] components, int k, 
		                          ComputationMatrix mat, int type, double[] w) {
			this.components = components;
			this.k = k;
			this.mat = mat;
			this.w = w;
			this.type = type;
		}

		public void run() {
			// System.out.println("k = "+k+": NODE_ATTRIBUTE -- mutiplying matrix with array");
			components[k] = ComputationMatrix.multiplyMatrixWithArray(mat, w);
		}
	}
}
