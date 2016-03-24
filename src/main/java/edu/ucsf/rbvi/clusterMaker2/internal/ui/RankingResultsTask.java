package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
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
    private ClusterManager manager;
    private CyNetworkView networkView;
    private RankingResults rankingResults;
    private final CyServiceRegistrar registrar;

    @Tunable(description="Network to look for cluster", context="nogui")
    private CyNetwork network;

    public RankingResultsTask(ClusterManager manager, CyNetworkView networkView) {
        this.manager = manager;
        this.networkView = networkView;

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
        taskMonitor.setProgress(0.0);
        taskMonitor.setStatusMessage("Calculating Ranking Results...");
        rankingResults = new RankingResults(null, taskMonitor, network);
        manager.addRankingResults(network, rankingResults);
    }

    @Override
    public boolean isAvailable() {
        return network != null;
    }

    @Override
    public ClusterResults getResults() {
        return null;
    }
}
