package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.util.Collections;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterVizFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory.ClusterType;

public class CreateResultsPanelTaskFactory implements ClusterVizFactory {
	
	ClusterManager clusterManager;
	boolean checkAvailable;
	public static String CLUSTERNAME = "Create Results Panel from Clusters";
	public static String CLUSTERSHORTNAME = "createResultsPanel";
	
	public CreateResultsPanelTaskFactory(ClusterManager clusterManager, boolean checkAvailable) {
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

		if (network == null)
			return false;

		return ResultsPanelTask.isReady(network, clusterManager);
	}

	public List<ClusterType> getTypeList() {
		return Collections.singletonList(ClusterType.UI);
		
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ResultsPanelTask(clusterManager, checkAvailable, true));

	}

	public boolean isReady() {
		if (!checkAvailable)
			return true;
		else
			return ResultsPanelTask.isReady(clusterManager.getNetwork(), clusterManager);
	}


}
