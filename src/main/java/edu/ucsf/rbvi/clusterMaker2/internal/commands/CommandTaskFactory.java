package edu.ucsf.rbvi.clusterMaker2.internal.commands;

import java.util.Collections;
import java.util.List;

//Cytoscape imports
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

//clusterMaker imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.ClusterManagerImpl;


public class CommandTaskFactory implements TaskFactory   {
	public static final String HASCLUSTER = "hascluster";
	public static final String GETCLUSTER = "getcluster";
	public static final String GETNETWORKCLUSTER = "getnetworkcluster";

	private ClusterManager clusterManager;
	private String command;
	
	public CommandTaskFactory(ClusterManager clusterManager, String command) {
		this.clusterManager = clusterManager;
		this.command = command;
	}
	
	public TaskIterator createTaskIterator() {
		if (command.equals(GETCLUSTER)) {
			return new TaskIterator(new GetClusterTask(clusterManager));
		} else if (command.equals(HASCLUSTER)) {
			return new TaskIterator(new HasClusterTask(clusterManager));
		} else if (command.equals(GETNETWORKCLUSTER)) {
			return new TaskIterator(new GetNetworkClusterTask(clusterManager));
		}
		return null;
	}

	public boolean isReady() {
		return true;
	}
}
	
	



