/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.util.HashSet;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ViewUtils;

/**
 *
 * @author root
 */
@SuppressWarnings("serial")
public class ScatterPlotLayoutTask extends AbstractLayoutTask {
	
	CyMatrix coordinates;
	ClusterManager manager;
	CyNetworkView networkView;
	
	public ScatterPlotLayoutTask(ClusterManager manager,
	                             String displayName, CyNetworkView view,
	                             CyMatrix coordinates, UndoSupport undo) {
		super(displayName, view, new HashSet(view.getNodeViews()), null, undo);
		this.coordinates = coordinates;
		this.networkView = view;
		this.manager = manager;
	}

	public void execute() {
		TaskManager mgr = manager.getService(TaskManager.class);
		mgr.execute(new TaskIterator(this));
	}

	public void doLayout(TaskMonitor monitor) {
		CyNetwork net = coordinates.getNetwork();
		double scale = 1.0;

		// Get the min and max so we can see if we need to scale
		double maxValue = coordinates.getMaxValue();
		double minValue = coordinates.getMinValue();
		double range = maxValue-minValue;
		if (range < 2500.0)
			scale = 2500.0/range;

		for (int row = 0; row < coordinates.nRows(); row++) {
			CyNode node = coordinates.getRowNode(row);
			double x = coordinates.doubleValue(row, 0);
			double y = coordinates.doubleValue(row, 1);
			ViewUtils.moveNode(manager, networkView, node, x*scale, -y*scale);
		}


	}
}
