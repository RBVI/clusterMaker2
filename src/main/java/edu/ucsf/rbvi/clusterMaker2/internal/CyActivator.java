package edu.ucsf.rbvi.clusterMaker2.internal;


// Java imports
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

// Cytoscape imports
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankingFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.AttributeClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.ChengChurch.ChengChurchTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.DBSCAN.DBSCANTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.AutoSOMETaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.featureVector.FeatureVectorTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.fft.FFTTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hierarchical.HierarchicalTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.hopach.HopachPAMTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.kmeans.KMeansTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.kmedoid.KMedoidTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.pam.PAMTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.FilterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.BestNeighbor.BestNeighborFilterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.CuttingEdge.CuttingEdgeFilterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.Density.DensityFilterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.HairCut.HairCutFilterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AP.APClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.ConnectedComponents.ConnectedComponentsTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM.FCMClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Fuzzifier.FuzzifierTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.GLay.GLayClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCODE.MCODEClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.SCPS.SCPSClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.TransClustClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.PCAMenuTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.PCATaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterVizFactory;
// Algorithms
import edu.ucsf.rbvi.clusterMaker2.internal.commands.CommandTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.BiclusterViewTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.CreateResultsPanelTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.DestroyResultsPanelTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.HeatMapViewTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.KnnViewTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.LinkSelectionTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.NewNetworkViewFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.TreeViewTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.UnlinkSelectionTaskFactory;
// import edu.ucsf.rbvi.clusterMaker2.internal.ui.UITaskFactory;
// clusterMaker imports

public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		// See if we have a graphics console or not
		boolean haveGUI = true;
		CySwingApplication swingAppRef = getService(bc, CySwingApplication.class);
		if (swingAppRef == null) {
			// if haveGUI is false, we don't want to provide any hooks to the treeview
			haveGUI = false;
		}
		CyApplicationManager appRef = getService(bc, CyApplicationManager.class);
		CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		CyGroupManager groupManager = getService(bc, CyGroupManager.class);
		CyGroupFactory groupFactory = getService(bc, CyGroupFactory.class);
		CyTableFactory tableFactory = getService(bc, CyTableFactory.class);
		CyTableManager tableManager = getService(bc, CyTableManager.class);

		// Create our context object.  This will probably keep track of all of the
		// registered clustering algorithms, settings, etc.
		ClusterManagerImpl clusterManager = new ClusterManagerImpl(appRef, serviceRegistrar, groupFactory, groupManager, tableFactory, tableManager );

		registerServiceListener(bc, clusterManager, "addClusterAlgorithm", "removeClusterAlgorithm", ClusterTaskFactory.class);
		registerServiceListener(bc, clusterManager, "addClusterVisualizer", "removeClusterVisualizer", ClusterVizFactory.class);
		registerServiceListener(bc, clusterManager, "addRankingAlgorithm", "removeRankingAlgorithm", RankingFactory.class);

		// Register each of our algorithms
		// Attribute clusterers
		registerService(bc, new AttributeClusterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new AutoSOMETaskFactory(clusterManager, true), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new FeatureVectorTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new HierarchicalTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new KMeansTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new KMedoidTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new HopachPAMTaskFactory(clusterManager), 
                ClusterTaskFactory.class, new Properties());
		registerService(bc, new PAMTaskFactory(clusterManager), 
                ClusterTaskFactory.class, new Properties());
		registerService(bc, new FFTTaskFactory(clusterManager), 
                ClusterTaskFactory.class, new Properties());
		registerService(bc, new DBSCANTaskFactory(clusterManager), 
               ClusterTaskFactory.class, new Properties());
		/* 
		 * Hold off on these until we get improve the performance sufficiently
		 * to allow them to be useful
		 */
		//registerService(bc, new BicFinderTaskFactory(clusterManager), 
    //           ClusterTaskFactory.class, new Properties());
		// registerService(bc, new BiMineTaskFactory(clusterManager), 
    //             ClusterTaskFactory.class, new Properties());
		registerService(bc, new ChengChurchTaskFactory(clusterManager), 
                ClusterTaskFactory.class, new Properties());

		// Network clusterers
		registerService(bc, new NetworkClusterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new APClusterTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new AutoSOMETaskFactory(clusterManager, false), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new FuzzifierTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new GLayClusterTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new ConnectedComponentsTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new FCMClusterTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new MCLClusterTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new MCODEClusterTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new SCPSClusterTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new TransClustClusterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());

		// Cluster ranking


		// Filters
		registerService(bc, new FilterTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new BestNeighborFilterTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new CuttingEdgeFilterTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new DensityFilterTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new HairCutFilterTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());

		// registerService(bc, new UITaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());

		// Visualizations
		registerService(bc, new NewNetworkViewFactory(clusterManager, false), ClusterVizFactory.class, 
		                new Properties());
		registerService(bc, new NewNetworkViewFactory(clusterManager, true), ClusterVizFactory.class, 
		                new Properties());
		registerService(bc, new HeatMapViewTaskFactory(clusterManager), ClusterVizFactory.class, 
		                new Properties());
		registerService(bc, new KnnViewTaskFactory(clusterManager), ClusterVizFactory.class, 
		                new Properties());
		registerService(bc, new BiclusterViewTaskFactory(clusterManager), ClusterVizFactory.class, 
		                new Properties());
		registerService(bc, new TreeViewTaskFactory(clusterManager), ClusterVizFactory.class, 
		                new Properties());
		registerService(bc, new CreateResultsPanelTaskFactory(clusterManager,true), ClusterVizFactory.class, 
                new Properties());
		registerService(bc, new DestroyResultsPanelTaskFactory(clusterManager,true), ClusterVizFactory.class, 
                new Properties());
                
                // Principal Component Analysis
                registerService(bc, new PCAMenuTaskFactory(), ClusterTaskFactory.class, new Properties());
                registerService(bc, new PCATaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());

		// Link Network Selections
		LinkSelectionTaskFactory linkTaskFactory = new LinkSelectionTaskFactory(clusterManager);
		Properties linkSelectionProps = new Properties();
    linkSelectionProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
    linkSelectionProps.setProperty(PREFERRED_MENU, "Apps.clusterMaker Visualizations");
    linkSelectionProps.setProperty(TITLE, "Link selection across networks");
    linkSelectionProps.setProperty(COMMAND, "linkSelection");
    linkSelectionProps.setProperty(COMMAND_NAMESPACE, "clusterviz");
    linkSelectionProps.setProperty(ENABLE_FOR, "networkAndView");
    linkSelectionProps.setProperty(IN_MENU_BAR, "true");
    linkSelectionProps.setProperty(MENU_GRAVITY, "100.0");
		registerService(bc, linkTaskFactory, NetworkTaskFactory.class, linkSelectionProps);

		// UnLink Network Selections
		UnlinkSelectionTaskFactory unlinkTaskFactory = new UnlinkSelectionTaskFactory(clusterManager);
		Properties unlinkSelectionProps = new Properties();
    unlinkSelectionProps.setProperty(PREFERRED_MENU, "Apps.clusterMaker Visualizations");
    unlinkSelectionProps.setProperty(TITLE, "Unlink selection across networks");
    unlinkSelectionProps.setProperty(COMMAND, "unlinkSelection");
    unlinkSelectionProps.setProperty(COMMAND_NAMESPACE, "clusterviz");
    unlinkSelectionProps.setProperty(ENABLE_FOR, "networkAndView");
    unlinkSelectionProps.setProperty(IN_MENU_BAR, "true");
    unlinkSelectionProps.setProperty(MENU_GRAVITY, "100.0");
		registerService(bc, unlinkTaskFactory, NetworkTaskFactory.class, unlinkSelectionProps);

		// Commands
		// These task factories provide useful commands that only make sense in the context of REST or 
		// the command interface
		{
			TaskFactory commandTaskFactory = new CommandTaskFactory(clusterManager, "hascluster");
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "cluster");
			props.setProperty(COMMAND, CommandTaskFactory.HASCLUSTER);
  		props.setProperty(TITLE, "Test to see if this network has a cluster of the requested type");
			registerService(bc, commandTaskFactory, TaskFactory.class, props);
		}
		{
			TaskFactory commandTaskFactory = new CommandTaskFactory(clusterManager, "getnetworkcluster");
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "cluster");
			props.setProperty(COMMAND, CommandTaskFactory.GETNETWORKCLUSTER);
  		props.setProperty(TITLE, 
				"Get a cluster of the requested type and the requested clustertype (node or attribute)");
			registerService(bc, commandTaskFactory, TaskFactory.class, props);
		}
		{
			TaskFactory commandTaskFactory = new CommandTaskFactory(clusterManager, "getcluster");
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "cluster");
			props.setProperty(COMMAND, CommandTaskFactory.GETCLUSTER);
  		props.setProperty(TITLE, "Get a cluster of the requested clustertype (node or attribute)");
			registerService(bc, commandTaskFactory, TaskFactory.class, props);
		}
	}
}
