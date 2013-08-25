package edu.ucsf.rbvi.clusterMaker2.internal.algorithms;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;

public abstract class AbstractClusterAlgorithm extends AbstractTask 
                                               implements RequestsUIHelper, ClusterAlgorithm {
	
	// Common class values
	protected boolean debug = false;
	protected boolean createGroups = false;
	protected String clusterAttributeName = null;
	protected boolean canceled = false;
	protected ClusterResults results;
	protected ClusterManager clusterManager;
	protected CyNetwork network = null;
	protected TaskMonitor monitor = null;
	protected List<String>params = null;
	
	public AbstractClusterAlgorithm(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}
	
	/************************************************************************
	 * Convenience routines                                                 *
	 ***********************************************************************/

	public void cancel() { canceled = true; }

	public boolean cancelled() { return canceled; }

	public ClusterResults getResults() { return results; }

	public static double mean(Double[] vector) {
	double result = 0.0;
		for (int i = 0; i < vector.length; i++) {
			result += vector[i].doubleValue();
		}
		return (result/(double)vector.length);
	}
	
	// Inefficient, but simple approach to finding the median
	public static double median(Double[] vector) {
		// Clone the input vector
		Double[] vectorCopy = new Double[vector.length];
		for (int i = 0; i < vector.length; i++) {
			vectorCopy[i] = new Double(vector[i].doubleValue());
		}
	
		// sort it
		Arrays.sort(vectorCopy);
	
		// Get the median
		int mid = vector.length/2;
		if (vector.length%2 == 1) {
			return (vectorCopy[mid].doubleValue());
		}
		return ((vectorCopy[mid-1].doubleValue()+vectorCopy[mid].doubleValue()) / 2);
	}

  public void setUIHelper(TunableUIHelper helper) { }
}
