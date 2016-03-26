package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.util.Collections;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterVizFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;

public class DestroyResultsPanelTaskFactory implements ClusterVizFactory {

	ClusterManager clusterManager;
	boolean checkAvailable;
	public static String CLUSTERNAME = "Destroy All Cluster Results Panels";
	public static String CLUSTERSHORTNAME = "destroyResultsPanel";
	
	public DestroyResultsPanelTaskFactory(ClusterManager clusterManager, boolean checkAvailable) {
		this.clusterManager = clusterManager;
		this.checkAvailable = checkAvailable;
	}

	public String getShortName() {
		if (checkAvailable) {
			return CLUSTERSHORTNAME;
		} else {
			return null; 
		}
	}

	public String getName() {
		if (checkAvailable) {
			return CLUSTERNAME;
		} else {
			return null; 
		}
	}

	public ClusterViz getVisualizer() {
		return null;
	}

	public boolean isAvailable(CyNetwork network) {
		if (!checkAvailable)
			return true;

		return network != null && ResultsPanelTask.isReady(network, clusterManager);

	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.UI);
		
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ResultsPanelTask(clusterManager, checkAvailable, false));

	}

	public boolean isReady() {

		return clusterManager.getResultsPanels(clusterManager.getNetwork()) != null;
		/*
		if (!checkAvailable)
			return true;
		return ResultsPanelTask.isReady(clusterManager.getNetwork(), clusterManager);
		*/
	}

}
