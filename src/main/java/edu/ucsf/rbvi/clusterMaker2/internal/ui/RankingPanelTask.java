package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ClusterUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ViewUtils;
import org.cytoscape.application.swing.*;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class RankingPanelTask extends AbstractTask implements ClusterViz, ClusterAlgorithm {

    private static String appName = "Ranklust Ranking Panel";
    public static String RANKLUSTNAME = "Show results from ranking clusters";
    public static String RANKLUSTSHORTNAME = "RanklustPanel";
    private boolean createFlag = false;
    private boolean checkAvailable = false;
    private List<NodeCluster> clusters = null;
    private ClusterManager manager;
    private CyNetworkView networkView;
    private RankingPanel rankingPanel;
    private final CyServiceRegistrar registrar;

    @Tunable(description="Network to look for cluster", context="nogui")
    private CyNetwork network;

    public RankingPanelTask(ClusterManager manager, boolean checkAvailable, boolean createFlag) {
        this.manager = manager;
        this.checkAvailable = checkAvailable;
        this.createFlag = createFlag;

        networkView = manager.getNetworkView();
        registrar = manager.getService(CyServiceRegistrar.class);

        if (network == null) {
            network = manager.getNetwork();
        }
    }

    @Override
    public String getShortName() {
        return RANKLUSTSHORTNAME;
    }

    @Override
    public String getName() {
        return RANKLUSTNAME;
    }

    @Override
    public Object getContext() {
        return null;
    }


    @Override
    public void run(TaskMonitor taskMonitor) {
        CySwingApplication swingApplication = manager.getService(CySwingApplication.class);
        CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);

        if (createFlag) {
            taskMonitor.setTitle("Creating new ranking panel with ranking results");
            taskMonitor.setStatusMessage("Calculating Ranking Results...");
            taskMonitor.setProgress(0.0);

            clusters = ClusterUtils.fetchRankingResults(network);
            rankingPanel = new RankingPanel(clusters, network, networkView, manager, taskMonitor);

            addAndRegisterPanel(cytoPanel);
            setNodeColors();
            taskMonitor.setProgress(1.0);
        } else {
            taskMonitor.setTitle("Deleting all ranking panels");
            removeAndUnregisterPanels();
            hideEmptyPanels(cytoPanel);
        }

        taskMonitor.setStatusMessage("Done!");
    }

    private void setNodeColors() {
        String rankingAlgorithmName = getRankingAlgorithmName();
        VisualStyle vs = ViewUtils.copyStyle(manager, ViewUtils.getCurrentVisualStyle(manager), "_removeThis");
        vs.setTitle("Ranking results colors");

        VisualMappingFunctionFactory vmff = manager.getService(VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
        ContinuousMapping mapping = (ContinuousMapping) vmff.createVisualMappingFunction(rankingAlgorithmName, Double.class,
                BasicVisualLexicon.NODE_FILL_COLOR);

        Color color1 = new Color(170, 221, 221);
        Color color2 = new Color(212, 229, 212);
        Color color3 = new Color(255, 238, 204);
        Color color4 = new Color(255, 195, 178);
        Color color5 = new Color(255, 153, 153);

        double belowAverage = NodeCluster.getAverageRankScore(clusters) / 2.0;
        double overAverage = NodeCluster.getMaxRankScore(clusters) / 2.0;
        BoundaryRangeValues<Paint> range1 = new BoundaryRangeValues<>(color1, color1, color2);
        BoundaryRangeValues<Paint> range2 = new BoundaryRangeValues<>(color3, color4, color5);


        mapping.addPoint(belowAverage, range1);
        mapping.addPoint(overAverage, range2);

        vs.addVisualMappingFunction(mapping);
        ViewUtils.setVisualStyle(manager, manager.getNetworkView(), vs);
    }

    private String getRankingAlgorithmName() {
        return network.getRow(network).get(ClusterManager.RANKING_ATTRIBUTE, String.class, "");
    }

    private void addAndRegisterPanel(CytoPanel cytoPanel) {
        registrar.registerService(rankingPanel, CytoPanelComponent.class, new Properties());
        manager.addRankingPanel(network, rankingPanel);
        cytoPanel.setState(CytoPanelState.DOCK);
    }

    private void hideEmptyPanels(CytoPanel cytoPanel) {
        if (cytoPanel.getCytoPanelComponentCount() == 0) {
            cytoPanel.setState(CytoPanelState.HIDE);
        }
    }

    private void removeAndUnregisterPanels() {
        List<RankingPanel> rankingResults = new ArrayList<>(manager.getRankingResults(network));
        for (RankingPanel panel : rankingResults) {
            registrar.unregisterService(panel, CytoPanelComponent.class);
            manager.removeRankingPanel(network, panel);
        }
    }

    @Override
    public boolean isAvailable() {
        return network != null;
    }

    @Override
    public ClusterResults getResults() {
        return null;
    }

    public static boolean isReady(final CyNetwork network) {
        if (network == null) {
            return false;
        }

        CyTable networkTable = network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);
        Set<String> columnNames = CyTableUtil.getColumnNames(networkTable);

        if (!columnNames.contains(ClusterManager.RANKING_ATTRIBUTE) &&
                !columnNames.contains(ClusterManager.CLUSTER_TYPE_ATTRIBUTE)) {
            return false;
        }

        return true;
    }
}
