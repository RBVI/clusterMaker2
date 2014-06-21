package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DBSCAN;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AbstractAttributeClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.fft.FFTContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;

public class DBSCAN extends AbstractAttributeClusterer {

	public static String SHORTNAME = "dbscan";
	public static String NAME = "DBSCAN cluster";
	public static String GROUP_ATTRIBUTE = SHORTNAME;
	
	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;

	@ContainsTunables
	public DBSCANContext context = null;
	
	public DBSCAN(DBSCANContext context, ClusterManager clusterManager) {
		super(clusterManager);
		this.context = context;
		if (network == null)
			network = clusterManager.getNetwork();
		context.setNetwork(network);
	}

	public String getShortName() {return SHORTNAME;}

	@ProvidesTitle
	public String getName() {return NAME;}
	
	public ClusterViz getVisualizer() {
		return null;
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

	
}
