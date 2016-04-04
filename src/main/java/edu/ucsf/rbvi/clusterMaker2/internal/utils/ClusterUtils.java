package edu.ucsf.rbvi.clusterMaker2.internal.utils;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterUtils {
    public static List<NodeCluster> createClusters(CyNetwork network, boolean needRankingAttribute) {
        List<NodeCluster> clusters = new ArrayList<>();
        String clusterAttribute = getClusterAttribute(network, ClusterManager.CLUSTER_ATTRIBUTE);
        String rankingAttribute = getClusterAttribute(network, ClusterManager.RANKING_ATTRIBUTE);

        Map<Integer, ArrayList<CyNode>> clusterMap = new HashMap<>();
        Map<Integer, Double> clusterScoreMap = new HashMap<>();

        insertNodeClusters(network, needRankingAttribute, clusterAttribute, rankingAttribute, clusterMap, clusterScoreMap);
        setNodeClusterInfo(clusters, clusterMap, clusterScoreMap);
        ascendingSort(clusters);
        return clusters;
    }

    private static String getClusterAttribute(CyNetwork network, String clusterAttribute) {
        return network.getRow(network, CyNetwork.LOCAL_ATTRS)
                .get(clusterAttribute, String.class);
    }

    private static void ascendingSort(List<NodeCluster> clusters) {
        // Ascending sort
        clusters.sort((a, b) -> {
            if (a.getRankScore() == b.getRankScore()) {
                return 0;
            } else if (a.getRankScore() > b.getRankScore()) {
                return -1;
            } else {
                return 1;
            }
        });
    }

    private static void insertNodeClusters(CyNetwork network, boolean needRankingAttribute, String clusterAttribute,
                                           String rankingAttribute,
                                           Map<Integer, ArrayList<CyNode>> clusterMap,
                                           Map<Integer, Double> clusterScoreMap) {

        for (CyNode node : network.getNodeList()) {
            if (needRankingAttribute) {
                if (!ModelUtils.hasAttribute(network, node, rankingAttribute)) {
                    return;
                }
            }

            if (ModelUtils.hasAttribute(network, node, clusterAttribute)) {
                Integer cluster = network.getRow(node).get(clusterAttribute, Integer.class);
                Double clusterScore = network.getRow(node).get(rankingAttribute, Double.class, 0.0);

                if (!clusterMap.containsKey(cluster)) {
                    clusterMap.put(cluster, new ArrayList<>());
                    clusterScoreMap.put(cluster, clusterScore);
                }

                clusterMap.get(cluster).add(node);
            }
        }
    }

    private static void setNodeClusterInfo(List<NodeCluster> clusters,
                                           Map<Integer, ArrayList<CyNode>> clusterMap,
                                           Map<Integer, Double> clusterScoreMap) {

        for (int clusterNum : clusterMap.keySet()) {
            NodeCluster cluster = new NodeCluster(clusterMap.get(clusterNum));
            cluster.setClusterNumber(clusterNum);
            cluster.setRankScore(clusterScoreMap.get(clusterNum));
            clusters.add(cluster);
        }
    }
}
