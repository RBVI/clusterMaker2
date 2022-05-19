package edu.ucsf.rbvi.clusterMaker2.internal.utils;

import static org.cytoscape.view.presentation.property.ArrowShapeVisualProperty.NONE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.util.HashSet;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Paint;

import javax.swing.SwingUtilities;

import org.cytoscape.event.CyEventHelper;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;

import org.cytoscape.util.color.Palette;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.NetworkImageFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
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

    public static void copyLayout(ClusterManager manager, CyNetworkView sourceView, CyNetworkView targetView) {
      for (View<CyNode> sourceNodeView: sourceView.getNodeViews()) {
        double x = sourceNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
        double y = sourceNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
        moveNode(manager, targetView, sourceNodeView.getModel(), x, y);
      }
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

    // NOTE: we need at least two pivot points
    public static VisualStyle createFillStyle(ClusterManager manager, VisualStyle source, String suffix, String column, Palette palette, double[] pivots) {
      VisualStyle newStyle;
      if (source != null) {
        newStyle = copyStyle(manager, source, suffix);
      } else {
        VisualStyleFactory visualStyleFactory = manager.getService(VisualStyleFactory.class);
        newStyle = visualStyleFactory.createVisualStyle(palette.getName());
      }

      VisualMappingFunctionFactory vmff = manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
      ContinuousMapping mapping = (ContinuousMapping) vmff.createVisualMappingFunction(column, Double.class,
              BasicVisualLexicon.NODE_FILL_COLOR);

      int nPivots = pivots.length;
      Color[] paletteColors = palette.getColors(nPivots);

      // First point
      BoundaryRangeValues<Paint> firstRange = new BoundaryRangeValues<>(paletteColors[0], paletteColors[0], paletteColors[1]);
      mapping.addPoint(pivots[0], firstRange);

      for (int i=1; i < nPivots-1; i++) {
        BoundaryRangeValues<Paint> range = new BoundaryRangeValues<>(paletteColors[i-1], paletteColors[i], paletteColors[i+1]);
        mapping.addPoint(pivots[i], range);
      }

      // Handle the last point
      BoundaryRangeValues<Paint> lastRange = new BoundaryRangeValues<>(paletteColors[nPivots-2], paletteColors[nPivots-1], paletteColors[nPivots-1]);
      mapping.addPoint(pivots[nPivots-1], lastRange);

      newStyle.addVisualMappingFunction(mapping);

      return newStyle;
    }

    public static void setVisualStyle(ClusterManager manager, CyNetworkView view, 
                                      VisualStyle style) {
        manager.getService(VisualMappingManager.class).setVisualStyle(style, view);
        view.updateView();
    }

    public static Color getColor(ClusterManager manager, CyNetwork network, CyNode node) {
        CyNetworkView view = manager.getNetworkView(network);
        View<CyNode> nodeView = view.getNodeView(node);
        return (Color)nodeView.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR);
    }

    public static String getLabel(ClusterManager manager, CyNetwork network, CyNode node) {
        CyNetworkView view = manager.getNetworkView(network);
        View<CyNode> nodeView = view.getNodeView(node);
        return nodeView.getVisualProperty(BasicVisualLexicon.NODE_LABEL);
    }

    public static void moveNode(ClusterManager manager, CyNetwork network, 
                                CyNode node, double x, double y) {
        CyNetworkView view = manager.getNetworkView(network);
        moveNode(manager, view, node, x, y);
    }

    public static void moveNode(ClusterManager manager, CyNetworkView view, 
                                CyNode node, double x, double y) {
        View<CyNode> nodeView = view.getNodeView(node);
        if (nodeView == null) return;
        nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x);
        nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
    }

    /**
     * Convert a network to an image.  This is used by the MCODEResultsPanel.
     *
     * @param clusterManager the clusterManager (used to get services)
     * @param vs the VisualStyle to use
     * @param network the network
     * @param cluster Input network to convert to an image
     * @param height  Height that the resulting image should be
     * @param width   Width that the resulting image should be
     * @param layouter Reference to the layout algorithm
     * @param layoutNecessary Determinant of cluster size growth or shrinkage, the former requires layout
     * @return The resulting image
     */
    public static Image createClusterImage(final ClusterManager clusterManager,
                                           final VisualStyle vs,
                                           final CyNetwork network,
                                           final NodeCluster cluster,
                                           final int height,
                                           final int width,
                                           SpringEmbeddedLayouter layouter,
                                           boolean layoutNecessary) {
        //System.out.println("CCI: inside method");
        final CyRootNetwork root =  clusterManager.getService(CyRootNetworkManager.class).getRootNetwork(network);
        final NetworkImageFactory networkImageFactory = clusterManager.getService(NetworkImageFactory.class);
        //need to create a method get the subnetwork for a cluster
        final CyNetwork net = cluster.getSubNetwork(network, root, SavePolicy.DO_NOT_SAVE);
 
        //System.out.println("CCI: after getting root and network ");
        // Progress reporters.
        // There are three basic tasks, the progress of each is calculated and then combined
        // using the respective weighting to get an overall progress global progress
        int weightSetupNodes = 20; // setting up the nodes and edges is deemed as 25% of the whole task
        int weightSetupEdges = 5;
        double weightLayout = 75.0; // layout it is 70%
        double goalTotal = weightSetupNodes + weightSetupEdges;

        if (layoutNecessary) {
            goalTotal += weightLayout;
        }

        // keeps track of progress as a percent of the totalGoal
        double progress = 0;

        //System.out.println("CCI: after getClusterStyle");
        final CyNetworkView clusterView = createNetworkView(clusterManager, net, vs);
        //System.out.println("CCI: after createNetworkView");

        clusterView.setVisualProperty(NETWORK_WIDTH, new Double(width));
        clusterView.setVisualProperty(NETWORK_HEIGHT, new Double(height));

        for (View<CyNode> nv : clusterView.getNodeViews()) {
          /*
            if (interrupted) {
                //logger.debug("Interrupted: Node Setup");
                // before we short-circuit the method we reset the interruption so that the method can run without
                // problems the next time around
                if (layouter != null) layouter.resetDoLayout();
                resetLoading();

                return null;
            }
            */

            // Node position
            final double x;
            final double y;

            // First we check if the MCODECluster already has a node view of this node (posing the more generic condition
            // first prevents the program from throwing a null pointer exception in the second condition)
            if (cluster.getView() != null && cluster.getView().getNodeView(nv.getModel()) != null) {
                //If it does, then we take the layout position that was already generated for it
                x = cluster.getView().getNodeView(nv.getModel()).getVisualProperty(NODE_X_LOCATION);
                y = cluster.getView().getNodeView(nv.getModel()).getVisualProperty(NODE_Y_LOCATION);
            } else {
                // Otherwise, randomize node positions before layout so that they don't all layout in a line
                // (so they don't fall into a local minimum for the SpringEmbedder)
                // If the SpringEmbedder implementation changes, this code may need to be removed
                // size is small for many default drawn graphs, thus +100
                x = (clusterView.getVisualProperty(NETWORK_WIDTH) + 100) * Math.random();
                y = (clusterView.getVisualProperty(NETWORK_HEIGHT) + 100) * Math.random();

                if (!layoutNecessary) {
                    goalTotal += weightLayout;
                    progress /= (goalTotal / (goalTotal - weightLayout));
                    layoutNecessary = true;
                }
            }

            nv.setVisualProperty(NODE_X_LOCATION, x);
            nv.setVisualProperty(NODE_Y_LOCATION, y);
        }

        if (layoutNecessary) {
            if (layouter == null) {
                layouter = new SpringEmbeddedLayouter();
            }

            layouter.setGraphView(clusterView);

            // The doLayout method should return true if the process completes without interruption
            if (!layouter.doLayout(weightLayout, goalTotal, progress)) {
                // Otherwise, if layout is not completed, set the interruption to false, and return null, not an image
                return null;
            }
        }

        // final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Image image = networkImageFactory.createImage(clusterView, width, height);


        return image;
    }

	public static VisualStyle getClusterStyle(final ClusterManager clusterManager, VisualStyle clusterStyle) {
		if (clusterStyle == null) {
      VisualStyleFactory visualStyleFactory = clusterManager.getService(VisualStyleFactory.class);
			clusterStyle = visualStyleFactory.createVisualStyle("Cluster");

			clusterStyle.setDefaultValue(NODE_SIZE, 40.0);
			clusterStyle.setDefaultValue(NODE_WIDTH, 40.0);
			clusterStyle.setDefaultValue(NODE_HEIGHT, 40.0);
			clusterStyle.setDefaultValue(NODE_PAINT, Color.RED);
			clusterStyle.setDefaultValue(NODE_FILL_COLOR, Color.RED);
			clusterStyle.setDefaultValue(NODE_BORDER_WIDTH, 0.0);

			clusterStyle.setDefaultValue(EDGE_WIDTH, 5.0);
			clusterStyle.setDefaultValue(EDGE_PAINT, Color.BLUE);
			clusterStyle.setDefaultValue(EDGE_UNSELECTED_PAINT, Color.BLUE);
			clusterStyle.setDefaultValue(EDGE_STROKE_UNSELECTED_PAINT, Color.BLUE);
			clusterStyle.setDefaultValue(EDGE_SELECTED_PAINT, Color.BLUE);
			clusterStyle.setDefaultValue(EDGE_STROKE_SELECTED_PAINT, Color.BLUE);
			clusterStyle.setDefaultValue(EDGE_TARGET_ARROW_SHAPE, NONE);
			clusterStyle.setDefaultValue(EDGE_SOURCE_ARROW_SHAPE, NONE);

			/*
			//System.out.println("GCS: before getVisual Lexicon");
			VisualLexicon lexicon = applicationMgr.getCurrentRenderingEngine().getVisualLexicon();
			VisualProperty vp = lexicon.lookup(CyEdge.class, "edgeTargetArrowShape");
			//System.out.println("CCI: after setting visual property");

			if (vp != null) {
				Object arrowValue = vp.parseSerializableString("ARROW");
				System.out.println("Edge target arrow value = "+arrowValue.toString());
				if (arrowValue != null) clusterStyle.setDefaultValue(vp, arrowValue);
			}
			*/
		}

		return clusterStyle;
	}

	public static CyNetworkView createNetworkView(final ClusterManager clusterManager, final CyNetwork net, VisualStyle vs) {
    CyNetworkViewFactory networkViewFactory = clusterManager.getService(CyNetworkViewFactory.class);
    VisualMappingManager visualMappingMgr = clusterManager.getService(VisualMappingManager.class);
		final CyNetworkView view = networkViewFactory.createNetworkView(net);
		//System.out.println("inside createNetworkView");
		if (vs == null) vs = visualMappingMgr.getDefaultVisualStyle();
		visualMappingMgr.setVisualStyle(vs, view);
		vs.apply(view);
		view.updateView();

		return view;
	}
}
