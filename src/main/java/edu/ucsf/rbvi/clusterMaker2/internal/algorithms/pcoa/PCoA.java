package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkView;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class PCoA extends AbstractNetworkClusterer implements ObservableTask {
	RunPCoA runpcoa;
	public static String SHORTNAME = "pcoa";
	public static String NAME = "Principal Coordinate Analysis";
	public final static String GROUP_ATTRIBUTE = "__PCoA.SUID";
	private CyNetworkView networkView;
	private final ClusterManager manager;

	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public PCoAContext context = null;
	
	public PCoA(PCoAContext context, ClusterManager manager) {
		super(manager);
		this.manager = manager;
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
		long start = System.currentTimeMillis();
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
		runpcoa = new RunPCoA(manager,matrix,network,networkView,context, neg.getValue(),monitor);
		runpcoa.run();
		

		if (canceled) return;

		long duration = System.currentTimeMillis()-start;
		monitor.showMessage(TaskMonitor.Level.INFO, 
		                    "PCoA completed in "+duration+"ms");

	}

	public void cancel() {
		canceled = true;
		runpcoa.cancel();
	}

	@Override
	public <R>R getResults(Class<? extends R> type) {
		CyMatrix results = runpcoa.getResult();
		if (type.equals(String.class)) {
			results.setColumnLabel(0, "X");
			results.setColumnLabel(1, "Y");
			CyNetwork net = results.getNetwork();
			for (int row = 0; row < results.nRows(); row++) {
				CyNode node = results.getRowNode(row);
				results.setRowLabel(row, ModelUtils.getName(net, node));
			}
			return (R)results.printMatrix();
		} else if (type.equals(Map.class)) {
			Map<CyNode, Point2D> resultsMap = new HashMap<>();
			for (int row = 0; row < results.nRows(); row++) {
				CyNode node = results.getRowNode(row);
				resultsMap.put(node,  
						new Point2D.Double(results.doubleValue(row, 0), results.doubleValue(row,1)));
			}
			return (R) resultsMap;
		}
		return (R) results;
	}
	@Override
	public void setUIHelper(TunableUIHelper helper) {context.setUIHelper(helper); }
	
}
