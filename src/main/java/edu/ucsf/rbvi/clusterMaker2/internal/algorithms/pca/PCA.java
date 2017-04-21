/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;

/**
 *
 * @author root
 */
public class PCA extends AbstractTask implements ObservableTask {
	final ClusterManager manager;
	public static String SHORTNAME = "pca";
	public static String NAME = "Principal Component Analysis";
	private List<String>attrList;	
	private CyNetworkView networkView;
	private CyMatrix resultsMatrix;

	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public PCAContext context = null;

	public PCA(PCAContext context, ClusterManager clusterManager){
		this.context = context;
		this.manager = clusterManager;
		this.networkView = clusterManager.getNetworkView();
		if (network == null)
				network = clusterManager.getNetwork();
		context.setNetwork(network);
	}

	public String getShortName() {return SHORTNAME;}

	@ProvidesTitle
	public String getName() {return NAME;}

	public void run(TaskMonitor monitor){
		monitor.setTitle("Principal Component Analysis");
		monitor.setStatusMessage("Running Principal Component Analysis");

		List<String> dataAttributes = context.getNodeAttributeList();

		if (dataAttributes == null || dataAttributes.isEmpty() ) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Error: no attribute list selected");
			return;
		}

		if (context.selectedOnly &&
			network.getDefaultNodeTable().countMatchingRows(CyNetwork.SELECTED, true) == 0) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Error: no nodes selected from network");
			return;
		}

		String[] attrArray = new String[dataAttributes.size()];
		int att = 0;
		for (String attribute: dataAttributes) {
				attrArray[att++] = "node."+attribute;
		}

		String matrixType = context.matrixType.getSelectedValue();

		RunPCA runPCA = new RunPCA(manager, network, networkView, context, monitor, attrArray, 
		                           matrixType, context.standardize);
		runPCA.runOnNodeToAttributeMatrix();
		final CyMatrix[] components = runPCA.getComponents();
		final double[] variance = runPCA.getVariance();
		resultsMatrix = CyMatrixFactory.makeLargeMatrix(components[0].getNetwork(), 
		                                                components[0].nRows(), variance.length);
		resultsMatrix.setRowNodes(components[0].getRowNodes());
		resultsMatrix.setRowLabels(Arrays.asList(components[0].getRowLabels()));
		for (int col=0; col < variance.length; col++) {
			resultsMatrix.setColumnLabel(col, String.format("%3.2f%%",variance[col]));
			for (int row=0; row < resultsMatrix.nRows(); row++) {
				resultsMatrix.setValue(row, col, components[col].doubleValue(row, 0));
			}
		}
	}

	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)) {
			return (R)resultsMatrix.printMatrix();
		}
		return (R)resultsMatrix;
	}
}
