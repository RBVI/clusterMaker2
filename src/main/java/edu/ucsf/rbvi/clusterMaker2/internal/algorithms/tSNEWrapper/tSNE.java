package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEWrapper;

import java.awt.geom.Point2D;
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

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;


public class tSNE extends AbstractNetworkClusterer implements ObservableTask {

	RuntSNE runtsne;
	public static String SHORTNAME = "tsne";
	public static String NAME = "t-Distributed Stochastic Neighbor";
	public final static String GROUP_ATTRIBUTE = "__tSNE.SUID";
	public final ClusterManager manager;
	private CyNetworkView networkView;

	@Tunable(description="Network to cluster", context="nogui")
	public CyNetwork network = null;
	
	@ContainsTunables
	public tSNEContext context = null;
	
	public tSNE(tSNEContext context, ClusterManager manager) {
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
		monitor.setTitle("t-Distributed Stochastic Neighbour");
		monitor.setStatusMessage("Running t-Distributed Stochastic Neighbour");
		this.monitor = monitor;
		if (network == null)
			network = clusterManager.getNetwork();

		context.setNetwork(network);

		List<String> dataAttributes = context.attributeList.getNodeAttributeList();

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

		// CyMatrix matrix = context.edgeAttributeHandler.getMatrix();
		CyMatrix matrix = CyMatrixFactory.makeLargeMatrix(network, attrArray, context.selectedOnly, 
		                                                  context.ignoreMissing, false, false);
		
		if (matrix == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Can't get distance matrix: no attribute value?");
			return;
		}
		

		
		runtsne = new RuntSNE(manager, network, networkView, context, monitor, matrix);
		runtsne.run();
		
	}

	public void cancel() {
		canceled = true;
		
	}

	@Override
	public <R>R getResults(Class<? extends R> type) {
		CyMatrix results = runtsne.getResult();
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

	/*
	@Override
	public void setUIHelper(TunableUIHelper helper) {context.setUIHelper(helper); }
	*/
	
}
