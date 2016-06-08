package edu.ucsf.rbvi.clusterMaker2.internal.utils;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import org.cytoscape.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterUtils {
    public static void createNewSingleColumn(CyTable table, String columnName, Class clzz, boolean isImmutable) {
        if (table.getColumn(columnName) != null) {
            table.deleteColumn(columnName);
        }

        table.createColumn(columnName, clzz, isImmutable);
    }

    public static void setNodeTableColumnValues(CyTable nodeTable, List<NodeCluster> clusters, String scoreCol) {
        for (NodeCluster cluster : clusters) {
            for (CyNode node : cluster) {
                nodeTable.getRow(node.getSUID()).set(scoreCol, cluster.getRankScore());
            }
        }
    }

    public static void setEdgeTableColumnValues(CyTable edgeTable, List<CyEdge> edges, List<NodeCluster> clusters, String scoreCol) {
        for (CyEdge edge : edges) {
            for (NodeCluster cluster : clusters) {
                for (CyNode node : cluster) {
                    if (edge.getSource().getSUID().equals(node.getSUID())) {
                        edgeTable.getRow(edge.getSUID()).set(scoreCol, cluster.getRankScore());
                    }
                    if (edge.getTarget().getSUID().equals(node.getSUID())) {
                        edgeTable.getRow(edge.getSUID()).set(scoreCol, cluster.getRankScore());
                    }
                }
            }
        }
    }

    public static List<NodeCluster> fetchClusters(CyNetwork network) {
        List<NodeCluster> clusters = new ArrayList<>();
        String clusterAttribute = getClusterAttribute(network, ClusterManager.CLUSTER_ATTRIBUTE);

        Map<Integer, ArrayList<CyNode>> clusterMap = new HashMap<>();

        for (CyNode node : network.getNodeList()) {
            if (ModelUtils.hasAttribute(network, node, clusterAttribute)) {
                Integer cluster = network.getRow(node).get(clusterAttribute, Integer.class);

                if (!clusterMap.containsKey(cluster)) {
                    clusterMap.put(cluster, new ArrayList<>());
                }

                clusterMap.get(cluster).add(node);
            }
        }

        for (int clusterNum : clusterMap.keySet()) {
            NodeCluster cluster = new NodeCluster(clusterMap.get(clusterNum));
            cluster.setClusterNumber(clusterNum);
            clusters.add(cluster);
        }

        return clusters;
    }

    public static List<NodeCluster> fetchRankingResults(CyNetwork network) {
        List<NodeCluster> clusters = new ArrayList<>();
        String clusterAttribute = getClusterAttribute(network, ClusterManager.CLUSTER_ATTRIBUTE);
        String rankingAttribute = getClusterAttribute(network, ClusterManager.RANKING_ATTRIBUTE);

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

        // Ascending sort
        ascendingSort(clusters);
        return clusters;
    }

    public static void ascendingSort(List<NodeCluster> clusters) {
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

    private static String getClusterAttribute(CyNetwork network, String clusterAttribute) {
        return network.getRow(network, CyNetwork.LOCAL_ATTRS)
                .get(clusterAttribute, String.class);
    }
}
