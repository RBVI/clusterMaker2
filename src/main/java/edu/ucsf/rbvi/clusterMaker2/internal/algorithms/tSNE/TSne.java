package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNE;

import java.util.List;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.TunableUIHelper;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.matrix.CyMatrixFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa.PCoAContext;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa.RunPCoA;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.DistanceMetric;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;


public class tSNE extends AbstractNetworkClusterer{

	RuntSNE runtsne;
	public static String SHORTNAME = "tsne";
	public static String NAME = "t-Distributed Stochastic Neighbor";
	public final static String GROUP_ATTRIBUTE = "__tSNE.SUID";
	private CyNetworkView networkView;
	
	
	protected Matrix distances;

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
		monitor.setTitle("t-Distributed Stochastic Neighbour");
		monitor.setStatusMessage("Running t-Distributed Stochastic Neighbour");
		this.monitor = monitor;
		if (network == null)
			network = clusterManager.getNetwork();

		context.setNetwork(network);

		//CyMatrix edgematrix = context.edgeAttributeHandler.getMatrix();
		
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
		
	//	tSNEContext.GetVisulaisation modeselection = context.modeselection.getSelectedValue();
		
		

		
		runtsne = new RuntSNE(network, networkView, context, monitor,matrix);
		runtsne.run();
		//runpcoa.setDebug(false);

		
	}

	public void cancel() {
		canceled = true;
		
	}

	
	
}
