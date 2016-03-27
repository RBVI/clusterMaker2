package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import org.cytoscape.application.swing.*;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import java.util.*;

public class RankingResultsTask extends AbstractTask implements ClusterViz, ClusterAlgorithm {

    private static String appName = "Ranklust Ranking Results Panel";
    public static String RANKLUSTNAME = "Create Results Panel from Ranking Clusters";
    public static String RANKLUSTSHORTNAME = "ranklustRankingResultsPanel";
    private boolean createFlag = false;
    private boolean checkAvailable = false;
    private ClusterManager manager;
    private CyNetworkView networkView;
    private RankingResults rankingResults;
    private final CyServiceRegistrar registrar;

    @Tunable(description="Network to look for cluster", context="nogui")
    private CyNetwork network;

    public RankingResultsTask(ClusterManager manager, boolean checkAvailable, boolean createFlag) {
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

    public List<NodeCluster> createClusters() {
        List<NodeCluster> clusters = new ArrayList<>();

        String clusterAttribute = network.getRow(network, CyNetwork.LOCAL_ATTRS)
            .get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);

        String rankingAttribute = network.getRow(network, CyNetwork.LOCAL_ATTRS)
            .get(ClusterManager.RANKING_ATTRIBUTE, String.class);

        assert clusterAttribute != null; // just for dev, remove when in prod
        assert rankingAttribute != null; // just for dev, remove when in prod

        Map<Integer, ArrayList<CyNode>> clusterMap = new HashMap<>();
        Map<Integer, Double> clusterScoreMap = new HashMap<>();

        for (CyNode node : network.getNodeList()) {
            if (ModelUtils.hasAttribute(network, node, clusterAttribute) &&
                    ModelUtils.hasAttribute(network, node, rankingAttribute)) {

                Integer cluster = network.getRow(node).get(clusterAttribute, Integer.class);
                Double clusterScore = network.getRow(node).get(rankingAttribute, Double.class, 0.0);

                if (!clusterMap.containsKey(cluster)) {
                    clusterMap.put(cluster, new ArrayList<>());
                    clusterScoreMap.put(cluster, clusterScore);
                }

                clusterMap.get(cluster).add(node);
            }
        }

        for (int clusterNum : clusterMap.keySet()) {
            NodeCluster cluster = new NodeCluster(clusterMap.get(clusterNum));
            cluster.setClusterNumber(clusterNum);
            cluster.setRankScore(clusterScoreMap.get(clusterNum));
            clusters.add(cluster);
        }

        clusters.sort((a, b) -> {
            if (a.getRankScore() == b.getRankScore()) {
                return 0;
            } else if (a.getRankScore() > b.getRankScore()) {
                return 1;
            } else {
                return -1;
            }
        });

        return clusters;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        CySwingApplication swingApplication = manager.getService(CySwingApplication.class);
        CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);

        if (createFlag) {
            taskMonitor.setTitle("Creating new results panel with ranking results");
            taskMonitor.setStatusMessage("Calculating Ranking Results...");
            taskMonitor.setProgress(0.0);

            rankingResults = new RankingResults(createClusters(), taskMonitor, network);

            addAndRegisterPanel(cytoPanel);
            taskMonitor.setProgress(100.0);
        } else {
            taskMonitor.setTitle("Deleting all ranking panels");
            removeAndUnregisterPanels();
            hideEmptyPanels(cytoPanel);
        }

        taskMonitor.setStatusMessage("Done!");
    }

    private void addAndRegisterPanel(CytoPanel cytoPanel) {
        registrar.registerService(rankingResults, CytoPanelComponent.class, new Properties());
        manager.addRankingResults(network, rankingResults);
        cytoPanel.setState(CytoPanelState.DOCK);
    }

    private void hideEmptyPanels(CytoPanel cytoPanel) {
        if (cytoPanel.getCytoPanelComponentCount() == 0) {
            cytoPanel.setState(CytoPanelState.HIDE);
        }
    }

    private void removeAndUnregisterPanels() {
        List<RankingResults> rankingResults = new ArrayList<>(manager.getRankingResults(network));
        for (RankingResults panel : rankingResults) {
            registrar.unregisterService(panel, CytoPanelComponent.class);
            manager.removeRankingResults(network, panel);
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

    public static boolean isReady(CyNetwork network, ClusterManager clusterManager) {
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
