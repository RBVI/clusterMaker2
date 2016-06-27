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
	protected CyMatrix distanceMatrix;

	private int nThreads = Runtime.getRuntime().availableProcessors()-1;

	public RunPCA(CyNetwork network, CyNetworkView networkView, 
	              PCAContext context, TaskMonitor monitor, String[] weightAttributes){
		this.network = network;
		this.networkView = networkView;
		this.context = context;
		this.monitor = monitor;
		this.weightAttributes = weightAttributes;
		System.out.println("WeightAttributes:");
		if (weightAttributes == null) {
			System.out.println("   -- none --");
		} else {
			for (String weight: weightAttributes) { System.out.println("    "+weight); }
		}
	}

	// this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
	// are not sorted in their order from maximum to minimum
	public void runOnNodeToAttributeMatrixSorted(){ 
		System.out.println("runOnNodeToAttributeDistanceMatrixSorted");
		CyMatrix matrix = CyMatrixFactory.makeLargeMatrix(network, weightAttributes, context.selectedOnly, 
		                                                  context.ignoreMissing, false, false);
		// distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());

		// System.out.println("Creating computationMatrix");
		// ComputationMatrix mat = new ComputationMatrix(distanceMatrix);
		// double[][] matrixArray = matrix.toArray(ComputationMatrix.MISSING_DATA);

		System.out.println("Computing principle components(sorted)");
		CyMatrix[] components = this.computePCsSorted(distanceMatrix);

		if(context.pcaPlot)
			ScatterPlotPCA.createAndShowGui(components, computeVariance(distanceMatrix));

	}

	// this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
	// are sorted in increasing order
	public void runOnNodeToAttributeMatrix(){
		System.out.println("runOnNodeToAttributeDistanceMatrix");
		CyMatrix matrix = CyMatrixFactory.makeLargeMatrix(network, weightAttributes, context.selectedOnly, 
		                                                  context.ignoreMissing, false, false);
		// distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());

		// System.out.println("Creating computationMatrix");
		// ComputationMatrix mat = new ComputationMatrix(distanceMatrix);

		System.out.println("Computing principle components");
		CyMatrix[] components = this.computePCs(matrix);

		double[] variance = computeVariance(matrix);

		if(context.pcaResultPanel)
			ResultPanelPCA.createAndShowGui(components, network, networkView, 
			                                matrix.getRowNodes(), variance);

		if(context.pcaPlot)
			ScatterPlotPCA.createAndShowGui(components, variance);

	}

	public CyMatrix[] computePCs(CyMatrix matrix){
		matrix.writeMatrix("output.txt");

		Matrix C;
		System.out.println("centralizing columns");
		matrix.centralizeColumns();
		matrix.writeMatrix("centralized.txt");

		System.out.println("Creating covariance matrix");
		C = matrix.covariance();
		C.writeMatrix("covariance.txt");

		System.out.println("Finding eigenValues");
		double[] values = C.eigenValues(true);
		System.out.println("Finding eigenVectors");
		double[][] vectors = C.eigenVectors();

		monitor.showMessage(TaskMonitor.Level.INFO, "Found "+values.length+" EigenValues");
		monitor.showMessage(TaskMonitor.Level.INFO, "Found "+vectors.length+" EigenVectors of length "+vectors[0].length);

		System.out.println("EigenValues: ");
		for (double v: values) {
			System.out.println("     "+v);
		}

		CyMatrix[] components = new CyMatrix[values.length];

		for(int j=values.length-1, k=0;j>=0;j--,k++){
			// double[] w = new double[vectors.length];
			CyMatrix result = CyMatrixFactory.makeLargeMatrix(matrix.getNetwork(), values.length, 1);
			for(int i=0;i<vectors.length;i++){
				result.setValue(i,0,vectors[i][j]);
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

	public CyMatrix[] computePCsSorted(CyMatrix matrix){
		matrix.writeMatrix("output.txt");

		Matrix C;
		System.out.println("centralizing columns");
		matrix.centralizeColumns();
		matrix.writeMatrix("centralized.txt");

		// Scale???

		System.out.println("Creating covariance matrix");
		C = matrix.covariance();
		C.writeMatrix("covariance.txt");

		double[] values = C.eigenValues(true);
		double[][] vectors = C.eigenVectors();
		monitor.showMessage(TaskMonitor.Level.INFO, "Found "+values.length+" EigenValues");

		System.out.println("EigenValues: ");
		for (int j = 0; j < values.length;j++)
			System.out.println("     "+values[j]);

		double max = Double.MAX_VALUE;
		double minPC = 0.0001;
		int nEV = 0;
		for (int j=0;j<values.length;j++) {
			if (values[j]>=minPC)
				nEV++;
		}

		CyMatrix[] components = new CyMatrix[nEV];

		for(int j=0;j<values.length;j++){
			double value = Double.MIN_VALUE;
			int pos = 0;
			for(int i=0; i<values.length; i++){
				// System.out.println("value = "+value+", values[i] = "+values[i]+", max = "+max);
				if(values[i] >= value && values[i] < max){
					System.out.println("value = "+value+", values[i] = "+values[i]+", max = "+max);
					System.out.println("Updating: value = "+values[i]+" pos = "+i);
					value = values[i];
					pos = i;
				}
			}
			if (values[pos] < minPC)
				break;

			System.out.println("pos = "+pos);
			double[] w = new double[vectors.length];
			for(int i=0;i<vectors.length;i++){
				w[i] = vectors[i][pos];
			}
			System.out.println("     "+values[pos]);

			CyMatrix result = CyMatrixFactory.makeLargeMatrix(matrix.getNetwork(), matrix.nRows(), 1);
			Matrix mat = matrix.multiplyMatrix(result);
			components[j] = matrix.copy(mat);

			max = value;
		}

		return components;
	}

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

	private void normalizeMatrix(double[][] distanceMatrix) {
		double dMax = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < distanceMatrix.length; i++) {
			for (int j = 0; j < i; j++) {
				if (distanceMatrix[i][j] > dMax) {
					dMax = distanceMatrix[i][j];
				}
			}
		}
		for (int i = 0; i < distanceMatrix.length; i++) {
			for (int j = 0; j < i; j++) {
				distanceMatrix[i][j] = distanceMatrix[i][j]/dMax;
				distanceMatrix[j][i] = distanceMatrix[i][j];
			}
		}
	}

	double[][] convertToSimilarityMatrix(double[][] distanceMatrix) {
		double[][] result = new double[distanceMatrix.length][distanceMatrix.length];
		for (int i = 0; i < distanceMatrix.length; i++) {
			for (int j = 0; j <= i; j++) {
				result[i][j] = 1-distanceMatrix[i][j];
				result[j][i] = result[i][j];
			}
		}
		return result;
	}

	double[] reverseArray(double[] arr) {
		double[] result = new double[arr.length];
		for (int i=0; i < arr.length; i++) {
			result[i] = arr[arr.length-1-i];
		}
		return result;
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
