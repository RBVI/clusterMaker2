/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.swing.SwingUtilities;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.ColtMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.ScatterPlotDialog;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

/**
 *
 * @author root
 */
public class RunPCA {
	private final ClusterManager manager;
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
	double[] variance;
	CyMatrix[] components;

	private int nThreads = Runtime.getRuntime().availableProcessors()-1;

	public RunPCA(ClusterManager manager, CyNetwork network, CyNetworkView networkView, 
	              PCAContext context, TaskMonitor monitor, String[] weightAttributes,
								String matrixType, boolean standardize){
		this.manager = manager;
		this.network = network;
		this.networkView = networkView;
		this.context = context;
		this.monitor = monitor;
		this.weightAttributes = weightAttributes;
		this.matrixType = matrixType;
		this.standardize = standardize;
		/*
		System.out.println("WeightAttributes:");
		if (weightAttributes == null) {
			System.out.println("   -- none --");
		} else {
			for (String weight: weightAttributes) { System.out.println("    "+weight); }
		}
		*/
		this.eigenValues = null;
		this.eigenVectors = null;
	}

	// this method assumes that eigen values 
	// are sorted in increasing order
	public void runOnNodeToAttributeMatrix(){
		// System.out.println("runOnNodeToAttributeMatrix");
		CyMatrix matrix = CyMatrixFactory.makeLargeMatrix(network, weightAttributes, context.selectedOnly, 
		                                                  context.ignoreMissing, false, false);

		// System.out.println("Computing principle components");
		components = computePCs(matrix);

		final Matrix loadingMatrix = calculateLoadingMatrix(matrix);

		if(context.pcaResultPanel) {
			CyServiceRegistrar registrar = manager.getService(CyServiceRegistrar.class);
			CySwingApplication swingApplication = manager.getService(CySwingApplication.class);
			ResultPanelPCA panel = new ResultPanelPCA(components, variance, network, networkView);
			CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);
			registrar.registerService(panel, CytoPanelComponent.class, new Properties());
			if (cytoPanel.getState() == CytoPanelState.HIDE)
				cytoPanel.setState(CytoPanelState.DOCK);
		}

		if(context.pcaPlot) {
			if (components.length < 2) {
				monitor.showMessage(TaskMonitor.Level.ERROR, 
				                    "Only found "+components.length+" components. Need 2 for scatterplot. "+
														"Perhaps minimum variance is set too high?");
				return;
			}
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// System.out.println("Scatter plot dialog call");
					ScatterPlotDialog dialog = 
									new ScatterPlotDialog(manager, "PCA", monitor, components, loadingMatrix, variance);
				}
			});
		}
	}

	public CyMatrix[] computePCs(CyMatrix matrix/*, Matrix loadingMatrix*/) {
		// matrix.writeMatrix("output.txt");

		Matrix C;
		if (standardize) {
			for (int column = 0; column < matrix.nColumns(); column++) {
				matrix.ops().standardizeColumn(column);
			}
		}

		// System.out.println("centralizing columns");
		matrix.ops().centralizeColumns();

		if (matrixType.equals("correlation")) {
			// System.out.println("Creating correlation matrix");
			C = matrix.ops().correlation();
		} else {
			// Covariance
			// System.out.println("Creating covariance matrix");
			C = matrix.ops().covariance();
		}

		C.ops().eigenInit();
		// System.out.println("Finding eigenValues");
		eigenValues = C.ops().eigenValues(true);
		// System.out.println("Finding eigenVectors");
		eigenVectors = C.ops().eigenVectors();

		monitor.showMessage(TaskMonitor.Level.INFO, "Found "+eigenValues.length+" EigenValues");
		monitor.showMessage(TaskMonitor.Level.INFO, "Found "+eigenVectors.length+" EigenVectors of length "+eigenVectors[0].length);

		variance = computeVariance(eigenValues);

		CyMatrix[] components = new CyMatrix[variance.length];

		for(int j=eigenValues.length-1, k=0;j>=0&&k<variance.length;j--,k++){
			// double[] w = new double[vectors.length];
			CyMatrix result = CyMatrixFactory.makeLargeMatrix(matrix.getNetwork(), eigenValues.length, 1);//vector
			for(int i=0;i<eigenVectors.length;i++){
				result.setValue(i,0,eigenVectors[i][j]);
			}

			Matrix mat = matrix.ops().multiplyMatrix(result);
			// System.out.println("After vector multiply: "+mat.printMatrixInfo());
			components[k] = matrix.copy(mat);
		}

		return components;
	}

	public double[] computeVariance(double[] values){
		double[] explainedVariance = new double[values.length];
		double total = 0.0;
		for (int i = 0; i < values.length; i++)
			total += values[i];

		int component = 0;
		for (int j=values.length-1; j >= 0; j--,component++) {
			explainedVariance[component] = (values[j] / total) * 100;
			if (explainedVariance[component] < context.minVariance)
				break;
		}
		if (component < values.length-1) {
			return Arrays.copyOf(explainedVariance, component);
		}

		return explainedVariance;
	}

	private Matrix calculateLoadingMatrix(CyMatrix matrix) {
		int rows = eigenVectors.length;
		int columns = eigenVectors[0].length;
		Matrix loading = CyMatrixFactory.makeSmallMatrix(matrix.getNetwork(), rows, columns);
		// loading.initialize(rows, columns, new double[rows][columns]);

		IntStream.range(0, rows).parallel()
			.forEach(row -> {
					for (int column = columns-1, newCol=0; column >=0; column--,newCol++) {
						loading.setValue(row, newCol, 
				   		               eigenVectors[row][column]*Math.sqrt(Math.abs(eigenValues[column])));
					}
				});

		loading.setRowLabels(Arrays.asList(matrix.getColumnLabels()));
		for (int column = 0; column < columns; column++) {
			loading.setColumnLabel(column, "PC "+(column+1));
		}
		return loading;
	}

	CyMatrix[] getComponents() { return components; }
	double[] getVariance() { return variance; }
}
