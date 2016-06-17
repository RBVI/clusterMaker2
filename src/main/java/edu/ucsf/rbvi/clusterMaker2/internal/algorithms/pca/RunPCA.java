/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

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
	protected Matrix distanceMatrix;

	private static final int PCA_NODE_NODE = 1;
	private static final int PCA_NODE_ATTRIBUTE = 2;
	private static final int PCA_EDGE = 3;
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
	/*
	public ComputationMatrix[] runOnNodeToNodeDistanceMatrixSorted(){ 
		// Matrix matrix = new Matrix(network, weightAttributes, false, context.ignoreMissing, context.selectedOnly);
		// matrix.setUniformWeights();
		CyMatrix matrix = CyMatrixFactory.makeLargeMatrix(network, weightAttributes, context.selectedOnly, 
		                                                  context.ignoreMissing, false, false);
		distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());
		ComputationMatrix mat = new ComputationMatrix(distanceMatrix);

		ComputationMatrix[] components = this.computePCsSorted(mat, PCA_NODE_NODE);

		if(context.pcaPlot)
			ScatterPlotPCA.createAndShowGui(components, computeVariance(mat));

		return components;
	}

	// this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
	// are sorted in increasing order
	public ComputationMatrix[] runOnNodeToNodeDistanceMatrix(){ 
		// We can't do PCA on the distance matrix because the covariance of the
		// distance matrix is not positive semi-definite.  We need to create a matrix
		// that roughly corresponds to the covariance matrix.  We'll use Euclidean Similarity
		// (see the paper by Elmore & Richman, 2000)

		System.out.println("runOnNodeToNodeDistanceMatrix");

		// 1. Create the distance matrix
		Matrix matrix = new Matrix(network, weightAttributes, false, context.ignoreMissing, context.selectedOnly);
		matrix.setUniformWeights();
		Matrix distMat = matrix.makeDistanceMatrix(context.distanceMetric.getSelectedValue());
		distanceMatrix = distMat.toArray(ComputationMatrix.MISSING_DATA);
		ComputationMatrix dist = new ComputationMatrix(distanceMatrix);
		dist.writeMatrix("distance1.out");

		// This will divide all entries by the max distance
		normalizeMatrix(distanceMatrix);
		dist = new ComputationMatrix(distanceMatrix);
		dist.writeMatrix("distance2.out");

		// 2. Now calculate the similarity matrix
		double[][] similarityMatrix = convertToSimilarityMatrix(distanceMatrix);

		System.out.println("Creating computationMatrix");
		ComputationMatrix mat = new ComputationMatrix(similarityMatrix);
		mat.writeMatrix("similarity.out");

		System.out.println("Computing principle components");
		ComputationMatrix[] components = this.computePCsSorted(mat, PCA_NODE_NODE);

		if(context.pcaPlot)
			ScatterPlotPCA.createAndShowGui(components, computeVariance(mat));

		return components;
	}
	*/

	// this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
	// are not sorted in their order from maximum to minimum
	public void runOnNodeToAttributeMatrixSorted(){ 
		System.out.println("runOnNodeToAttributeDistanceMatrixSorted");
		CyMatrix matrix = CyMatrixFactory.makeLargeMatrix(network, weightAttributes, context.selectedOnly, 
		                                                  context.ignoreMissing, false, false);
		distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());

		System.out.println("Creating computationMatrix");
		ComputationMatrix mat = new ComputationMatrix(distanceMatrix);
		// double[][] matrixArray = matrix.toArray(ComputationMatrix.MISSING_DATA);

		System.out.println("Computing principle components(sorted)");
		ComputationMatrix[] components = this.computePCsSorted(mat, PCA_NODE_ATTRIBUTE);

		if(context.pcaPlot)
			ScatterPlotPCA.createAndShowGui(components, computeVariance(mat));

	}

	// this method assumes that eigen values returned by DenseDoubleEigenvalueDecomposition class
	// are sorted in increasing order
	public void runOnNodeToAttributeMatrix(){
		System.out.println("runOnNodeToAttributeDistanceMatrix");
		CyMatrix matrix = CyMatrixFactory.makeLargeMatrix(network, weightAttributes, context.selectedOnly, 
		                                                  context.ignoreMissing, false, false);
		distanceMatrix = matrix.getDistanceMatrix(context.distanceMetric.getSelectedValue());

		System.out.println("Creating computationMatrix");
		ComputationMatrix mat = new ComputationMatrix(distanceMatrix);

		System.out.println("Computing principle components");
		ComputationMatrix[] components = this.computePCs(mat, PCA_NODE_ATTRIBUTE);

		if(context.pcaResultPanel)
			ResultPanelPCA.createAndShowGui(components, network, networkView, 
			                                matrix.getRowNodes(), computeVariance(mat));

		if(context.pcaPlot)
			ScatterPlotPCA.createAndShowGui(components, computeVariance(mat));

	}

	/*
	public void runOnEdgeValues(){
		// We can't do PCA on the distance matrix because the covariance of the
		// distance matrix is not positive semi-definite.  We need to create a
		// weighted Laplacian first.

		// 1. Create a similarity matrix
		CyMatrix disMatrix = context.edgeAttributeHandler.getMatrix();
		distanceMatrix = disMatrix.toArray();
		// This will divide all entries by the max distance
		normalizeMatrix(distanceMatrix);

		// 2. Now calculate the similarity matrix
		double[][] similarityMatrix = convertToSimilarityMatrix(distanceMatrix);

		System.out.println("Creating computationMatrix");
		ComputationMatrix mat = new ComputationMatrix(similarityMatrix);

		System.out.println("Computing principle components");
		ComputationMatrix[] components = this.computePCs(mat, PCA_EDGE);
		mat.writeMatrix("output.txt");

		if(context.pcaPlot)
			ScatterPlotPCA.createAndShowGui(components, computeVariance(mat));
	}
	*/

	public ComputationMatrix[] computePCs(ComputationMatrix matrix, int type){
		matrix.writeMatrix("output.txt");

		ComputationMatrix mat;
		ComputationMatrix C;
		if (type == PCA_NODE_ATTRIBUTE) {
			System.out.println("centralizing columns");
			mat = matrix.centralizeColumns();
			mat.writeMatrix("centralized.txt");

			System.out.println("Creating covariance matrix");
			C = mat.covariance();
			mat.writeMatrix("covariance.txt");
		} else {
			// The matrix already has similarities, which are roughly equivalent to
			// covariances.
			mat = matrix.centralizeColumns();
			C = mat;
		}

		System.out.println("Finding eigenValues");
		double[] values = C.eigenValues();
		System.out.println("Finding eigenVectors");
		double[][] vectors = C.eigenVectors();

		monitor.showMessage(TaskMonitor.Level.INFO, "Found "+values.length+" EigenValues");

		System.out.println("EigenValues: ");
		for (double v: values) {
			System.out.println("     "+v);
		}

		ComputationMatrix[] components = new ComputationMatrix[values.length];

		// Create the thread pools
		final ExecutorService[] threadPools = new ExecutorService[nThreads];
		for (int pool = 0; pool < threadPools.length; pool++) {
			threadPools[pool] = Executors.newFixedThreadPool(1);
		}

		for(int j=values.length-1, k=0;j>=0;j--,k++){
			double[] w = new double[vectors.length];
			for(int i=0;i<vectors.length;i++){
				w[i] = vectors[i][j];
			}
			Runnable r = new CalculateComponent(components, k, mat, type, w);
			threadPools[k%nThreads].submit(r);
			// System.out.println("PC: " + k);
			// components[k].printMatrix();
		}
		for (int pool = 0; pool < threadPools.length; pool++) {
			threadPools[pool].shutdown();
			try {
				boolean result = threadPools[pool].awaitTermination(7, TimeUnit.DAYS);
			} catch (Exception e) {}
		}

		return components;
	}

	public ComputationMatrix[] computePCsSorted(ComputationMatrix matrix, int type){
		ComputationMatrix mat;
		ComputationMatrix C;
		matrix.writeMatrix("output.txt");
		if (type == PCA_NODE_ATTRIBUTE) {
			System.out.println("centralizing columns");
			mat = matrix.centralizeColumns();
			mat.writeMatrix("centralized.txt");

			System.out.println("Creating covariance matrix");
			C = mat.covariance();
			mat.writeMatrix("covariance.txt");
		} else {
			// The matrix already has similarities, which are roughly equivalent to
			// covariances.
			mat = matrix.centralizeColumns();
			C = mat;
		}

		double[] values = C.eigenValues();
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

		ComputationMatrix[] components = new ComputationMatrix[nEV];

		// Create the thread pools
		final ExecutorService[] threadPools = new ExecutorService[nThreads];
		for (int pool = 0; pool < threadPools.length; pool++) {
			threadPools[pool] = Executors.newFixedThreadPool(1);
		}

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

			Runnable r = new CalculateComponent(components, j, mat, type, w);
			threadPools[j%nThreads].submit(r);

			max = value;
		}

		for (int pool = 0; pool < threadPools.length; pool++) {
			threadPools[pool].shutdown();
			try {
				boolean result = threadPools[pool].awaitTermination(7, TimeUnit.DAYS);
			} catch (Exception e) {}
		}

		return components;
	}

	public double[] computeVariance(ComputationMatrix matrix){
		ComputationMatrix mat = matrix.centralizeColumns();

		ComputationMatrix C = mat.covariance();

		double[] values = C.eigenValues();
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
			if(type == PCA_NODE_NODE) {
				// System.out.println("k = "+k+": NODE_NODE -- mutiplying array");
				components[k] = mat.multiplyMatrix(ComputationMatrix.multiplyArray(w, w));
			} else if(type == PCA_NODE_ATTRIBUTE) {
				// System.out.println("k = "+k+": NODE_ATTRIBUTE -- mutiplying matrix with array");
				components[k] = ComputationMatrix.multiplyMatrixWithArray(mat, w);
			}
		}
	}
}
