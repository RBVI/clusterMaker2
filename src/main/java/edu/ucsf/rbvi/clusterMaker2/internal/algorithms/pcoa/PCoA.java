package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.RunMCL;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;

public class PCoA extends AbstractNetworkClusterer{
	RunPCoA runpcoa;
	public static String SHORTNAME = "pcoa";
	public static String NAME = "Principal Coordinate Analysis";
	public final static String GROUP_ATTRIBUTE = "__PCoA.SUID";

	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public PCoAContext context = null;
	
	public PCoA(PCoAContext context, ClusterManager manager) {
		super(manager);
		this.context = context;
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
		

		if (canceled) return;

		PCoAContext.NegEigenHandling neg = context.neg.getSelectedValue();

		//Cluster the nodes
		runpcoa = new RunPCoA(matrix,context.scale, neg.getValue(),monitor);
		runpcoa.run();
		runpcoa.setDebug(false);

		if (canceled) return;

		monitor.showMessage(TaskMonitor.Level.INFO,"Clustering...");


		monitor.showMessage(TaskMonitor.Level.INFO, 
		                    "PCoA results:\n"+results);

		if (context.vizProperties.showUI) {
			monitor.showMessage(TaskMonitor.Level.INFO, 
		                      "Creating network");
			insertTasksAfterCurrentTask(new NewNetworkView(network, clusterManager, true,
			                                               context.vizProperties.restoreEdges));
		}
	}

	public void cancel() {
		canceled = true;
		runpcoa.cancel();
	}

	@Override
	public void setUIHelper(TunableUIHelper helper) {context.setUIHelper(helper); }
	
}
