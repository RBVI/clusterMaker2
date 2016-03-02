package edu.ucsf.rbvi.clusterMaker2.internal.utils;

import java.util.HashSet;

import org.cytoscape.event.CyEventHelper;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;

import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;

public class ViewUtils {

	public static CyNetworkView createView(ClusterManager manager, CyNetwork newNetwork, boolean register) {
		// Create the view
		CyNetworkView view = 
			manager.getService(CyNetworkViewFactory.class).createNetworkView(newNetwork);

		if (register)
			registerView(manager, view);

		// Make sure we flush our events before we try to do anything else
		CyEventHelper eventHelper = manager.getService(CyEventHelper.class);
		eventHelper.flushPayloadEvents();

		return view;
	}

	public static void registerView(ClusterManager manager, CyNetworkView view) {
		manager.getService(CyNetworkViewManager.class).addNetworkView(view);
	}

	public static void doLayout(ClusterManager manager, CyNetworkView view, 
	                            TaskMonitor monitor, String algName) {
		CyLayoutAlgorithm alg = manager.getService(CyLayoutAlgorithmManager.class).getLayout(algName);
		if (alg != null) {
			TaskIterator ti = alg.createTaskIterator(view, alg.getDefaultLayoutContext(), 
			                                         new HashSet<View<CyNode>>(), null);
			try {
				while (ti.hasNext())
					ti.next().run(monitor);
			} catch (Exception e) {
				monitor.showMessage(TaskMonitor.Level.ERROR, "Unable to layout network: "+e.getMessage());
			}
		}
	}


	public static VisualStyle getCurrentVisualStyle(ClusterManager manager) {
		return manager.getService(VisualMappingManager.class).getCurrentVisualStyle();
	}

	public static VisualStyle copyStyle(ClusterManager manager, VisualStyle style, String suffix) {
		VisualStyle newStyle = manager.getService(VisualStyleFactory.class).createVisualStyle(style);
		newStyle.setTitle(style.getTitle()+suffix);
		manager.getService(VisualMappingManager.class).addVisualStyle(newStyle);
		return newStyle;
	}

	public static void setVisualStyle(ClusterManager manager, CyNetworkView view, 
	                                  VisualStyle style) {
		manager.getService(VisualMappingManager.class).setVisualStyle(style, view);
		view.updateView();
	}
}
