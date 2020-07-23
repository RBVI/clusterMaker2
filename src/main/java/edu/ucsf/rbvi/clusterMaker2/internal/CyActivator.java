package edu.ucsf.rbvi.clusterMaker2.internal;


import edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils.ClusterJobExecutionService;
// Java imports
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
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.BestNeighbor.BestNeighborFilterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.CuttingEdge.CuttingEdgeFilterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.Density.DensityFilterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.FilterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.clusterFilters.HairCut.HairCutFilterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AP.APClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.ConnectedComponents.ConnectedComponentsTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.FCM.FCMClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Fuzzifier.FuzzifierTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.GLay.GLayClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCL.MCLClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.MCODE.MCODEClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.NetworkClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.SCPS.SCPSClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.TransClustClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.Leiden.LeidenClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.PCAMenuTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca.PCATaskFactory;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.HITS.HITSTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.MAA.MAATaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.MAM.MAMTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.PR.PRTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.PRWP.PRWPTaskFactory;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.tSNEWrapper.tSNETaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pcoa.PCoATaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterVizFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.commands.CommandTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.*;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.jobs.CyJobExecutionService;
import org.cytoscape.jobs.CyJobManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

// Cytoscape imports
// Algorithms
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
		
		// clusterJob
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		CyJobManager cyJobManager = getService(bc, CyJobManager.class);

		// Create our context object.  This will probably keep track of all of the
		// registered clustering algorithms, settings, etc.
		ClusterManagerImpl clusterManager = 
						new ClusterManagerImpl(appRef, serviceRegistrar, groupFactory, 
		                               groupManager, tableFactory, tableManager );

		registerServiceListener(bc, clusterManager, 
		                        "addClusterAlgorithm", "removeClusterAlgorithm", ClusterTaskFactory.class);
		registerServiceListener(bc, clusterManager, 
		                        "addClusterVisualizer", "removeClusterVisualizer", ClusterVizFactory.class);
		registerServiceListener(bc, clusterManager, 
		                        "addRankingAlgorithm", "removeRankingAlgorithm", RankFactory.class);

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
		// FIXME: FFT is seriously broken!
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
		registerService(bc, new APClusterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new AutoSOMETaskFactory(clusterManager, false),
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new FuzzifierTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new GLayClusterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new ConnectedComponentsTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new FCMClusterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new MCLClusterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new MCODEClusterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new SCPSClusterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new TransClustClusterTaskFactory(clusterManager),
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new LeidenClusterTaskFactory(clusterManager, registrar),
						ClusterTaskFactory.class, new Properties());

		
		// Cluster ranking
		registerService(bc, new MAATaskFactory(clusterManager), RankFactory.class, new Properties());
		registerService(bc, new MAMTaskFactory(clusterManager), RankFactory.class, new Properties());
		registerService(bc, new PRWPTaskFactory(clusterManager), RankFactory.class, new Properties());
		registerService(bc, new PRTaskFactory(clusterManager), RankFactory.class, new Properties());
		registerService(bc, new HITSTaskFactory(clusterManager), RankFactory.class, new Properties());

		// Filters
		registerService(bc, new FilterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new BestNeighborFilterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new CuttingEdgeFilterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new DensityFilterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());
		registerService(bc, new HairCutFilterTaskFactory(clusterManager), 
		                ClusterTaskFactory.class, new Properties());

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
		registerService(bc, new CreateResultsPanelTaskFactory(clusterManager,true), 
		                ClusterVizFactory.class, new Properties());
		registerService(bc, new CreateRankingPanelTaskFactory(clusterManager, true), 
		                ClusterVizFactory.class, new Properties());
		registerService(bc, new DestroyResultsPanelTaskFactory(clusterManager,true), 
		                ClusterVizFactory.class, new Properties());
		registerService(bc, new DestroyRankingPanelTaskFactory(clusterManager, true), 
		                ClusterVizFactory.class, new Properties());

		// Dimensionality Reduction
		// registerService(bc, new PCAMenuTaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new PCATaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new PCoATaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());
		registerService(bc, new tSNETaskFactory(clusterManager), ClusterTaskFactory.class, new Properties());

		{
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
			linkSelectionProps.setProperty(COMMAND_EXAMPLE_JSON, "{}");
			linkSelectionProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
			linkSelectionProps.setProperty(COMMAND_LONG_DESCRIPTION, 
			                               "This command causes selection to be reflected across all open networks.  This "+
			                               "is particularly useful when examining various different clsuter results");
			registerService(bc, linkTaskFactory, NetworkTaskFactory.class, linkSelectionProps);
		}

		{
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
			unlinkSelectionProps.setProperty(COMMAND_EXAMPLE_JSON, "{}");
			unlinkSelectionProps.setProperty(COMMAND_SUPPORTS_JSON, "true");
			unlinkSelectionProps.setProperty(COMMAND_LONG_DESCRIPTION, 
			                                 "Disable the linking of selection across networks");
			registerService(bc, unlinkTaskFactory, NetworkTaskFactory.class, unlinkSelectionProps);
		}

		// Commands
		// These task factories provide useful commands that only make sense in the context of REST or
		// the command interface
		{
			CommandTaskFactory commandTaskFactory = new CommandTaskFactory(clusterManager, "hascluster");
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "cluster");
			props.setProperty(COMMAND, CommandTaskFactory.HASCLUSTER);
			props.setProperty(COMMAND_DESCRIPTION, "Test to see if this network has a cluster of the requested type");
			props.setProperty(COMMAND_EXAMPLE_JSON, commandTaskFactory.getExampleJSON());
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_LONG_DESCRIPTION, 
			                  "Test to see if the current network has a cluster of the requested type.");
			registerService(bc, commandTaskFactory, TaskFactory.class, props);
		}

		{
			CommandTaskFactory commandTaskFactory = new CommandTaskFactory(clusterManager, "getnetworkcluster");
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "cluster");
			props.setProperty(COMMAND, CommandTaskFactory.GETNETWORKCLUSTER);
			props.setProperty(COMMAND_DESCRIPTION, "Get a cluster network cluster result");
			props.setProperty(COMMAND_EXAMPLE_JSON, commandTaskFactory.getExampleJSON());
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_LONG_DESCRIPTION, 
			                                 "Disable the linking of selection across networks");
			registerService(bc, commandTaskFactory, TaskFactory.class, props);
		}

		{
			CommandTaskFactory commandTaskFactory = new CommandTaskFactory(clusterManager, "getcluster");
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "cluster");
			props.setProperty(COMMAND, CommandTaskFactory.GETCLUSTER);
			props.setProperty(COMMAND_DESCRIPTION, "Get an attribute cluster result");
			props.setProperty(COMMAND_EXAMPLE_JSON, commandTaskFactory.getExampleJSON());
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_LONG_DESCRIPTION, 
			                  "Return the attribute cluster results for the ```algorithm``` and ```type``` (node or attribute) provided as arguments. "+
			                  "Note that the JSON return syntax is highly dependent on the algorithm and type requested.  "+
			                  "In any case, to ease parsing, all of the JSON returns include the algorithm and type as top-level "+
			                  "keys.  The sample JSON return is for the hierarchical cluster of nodes, which is the most complicated "+
			                  "type.");
			registerService(bc, commandTaskFactory, TaskFactory.class, props);
		}
		
		
		{
			ClusterJobExecutionService clusterJobService = 
							new ClusterJobExecutionService(cyJobManager, registrar);
			Properties props = new Properties();
			props.setProperty(TITLE, "ClusterJobExecutor");
			registerService(bc, clusterJobService, CyJobExecutionService.class, props);
		}
		

	}
}
