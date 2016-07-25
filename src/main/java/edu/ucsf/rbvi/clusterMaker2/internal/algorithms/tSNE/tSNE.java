package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa.RunPCoA;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;


public class tSNE extends AbstractNetworkClusterer{

	
	public static String SHORTNAME = "tsne";
	public static String NAME = "t-Distributed Stochastic Neighbor";
	public final static String GROUP_ATTRIBUTE = "__tSNE.SUID";
	private CyNetworkView networkView;

	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public tSNEContext context = null;
	
	public tSNE(tSNEContext context, ClusterManager manager) {
		super(manager);
		this.context = context;
		this.networkView = clusterManager.getNetworkView();
		if (network == null)
				network = clusterManager.getNetwork();
		context.setNetwork(network);
	}

	public String getShortName() { return SHORTNAME; }

	@ProvidesTitle
	public String getName() { return NAME; }
	
	public void run(TaskMonitor monitor) {
		monitor.setTitle("Running Principal Coordinate Analysis");
		this.monitor = monitor;
		if (network == null)
			network = clusterManager.getNetwork();

		context.setNetwork(network);

		CyMatrix matrix = context.edgeAttributeHandler.getMatrix();
		
		if (matrix == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Can't get distance matrix: no attribute value?");
			return;
		}
		

		//Cluster the nodes
	//	runpcoa = new RunPCoA(matrix,network,networkView,context, neg.getValue(),monitor);
		//runpcoa.run();
		//runpcoa.setDebug(false);

		
	}

	public void cancel() {
		canceled = true;
		
	}

	@Override
	public void setUIHelper(TunableUIHelper helper) {context.setUIHelper(helper); }
	
}
