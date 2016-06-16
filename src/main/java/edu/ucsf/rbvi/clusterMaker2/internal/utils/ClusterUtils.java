package edu.ucsf.rbvi.clusterMaker2.internal.utils;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import org.cytoscape.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterUtils {
    private static void createNewSingleColumn(CyTable table, String columnName, Class clzz, boolean isImmutable) {
        if (table.getColumn(columnName) != null) {
            table.deleteColumn(columnName);
        }

        table.createColumn(columnName, clzz, isImmutable);
    }

    private static void setNodeTableColumnValues(CyTable nodeTable, List<NodeCluster> clusters, String scoreCol) {
        for (NodeCluster cluster : clusters) {
            for (CyNode node : cluster) {
                nodeTable.getRow(node.getSUID()).set(scoreCol, cluster.getRankScore());
            }
        }
    }

    private static void setEdgeTableColumnValues(CyTable edgeTable, List<CyEdge> edges, List<NodeCluster> clusters, String scoreCol) {
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

    public static List<NodeCluster> setEdgeScoresInCluster(CyNetwork network, List<NodeCluster> clusters, List<String> edgeAttributes, String clusterColumnName, boolean multiplicative) {
        List<CyEdge> edges = network.getEdgeList();
        CyTable nodeTable = network.getDefaultNodeTable();
        CyTable edgeTable = network.getDefaultEdgeTable();

        for (String edgeAttr : edgeAttributes) {
            for (CyEdge edge : edges) {
                CyRow source = nodeTable.getRow(edge.getSource().getSUID());
                CyRow target = nodeTable.getRow(edge.getTarget().getSUID());
                CyRow edgeRow = edgeTable.getRow(edge.getSUID());
                int sourceClusterNumber = source.get(clusterColumnName, Integer.class, -1);
                int targetClusterNumber = target.get(clusterColumnName, Integer.class, -1);
                int sourceHighestClusterNumber = -1;
                int targetHighestClusterNumber = -1;

                for (NodeCluster cluster : clusters) {
                    int clusterNumber = cluster.getClusterNumber();

                    if (clusterNumber == sourceClusterNumber && (clusterNumber < sourceHighestClusterNumber || sourceHighestClusterNumber == -1)) {
                        if (multiplicative) {
                            setRankScoreMultiplicative(edgeAttr, edgeRow, cluster);
                        } else {
                            setRankScore(edgeAttr, edgeRow, cluster);
                        }
                        sourceHighestClusterNumber = clusterNumber;
                    } else if (clusterNumber == targetClusterNumber && (clusterNumber < targetHighestClusterNumber || targetHighestClusterNumber == -1)) {
                        if (multiplicative) {
                            setRankScoreMultiplicative(edgeAttr, edgeRow, cluster);
                        } else {
                            setRankScore(edgeAttr, edgeRow, cluster);
                        }
                        targetHighestClusterNumber = clusterNumber;
                    }
                }
            }
        }

        return clusters;
    }

    public static List<NodeCluster> setNodeScoresInCluster(CyNetwork network, List<NodeCluster> clusters, List<String> nodeAttributes, String clusterColumnName, boolean multiplicative) {
        List<CyNode> nodes = network.getNodeList();
        CyTable table = network.getDefaultNodeTable();

        for (String nodeAttr : nodeAttributes) {
            for (CyNode node : nodes) {
                CyRow row = table.getRow(node.getSUID());
                int nodeClusterNumber = row.get(clusterColumnName, Integer.class, -1);

                for (NodeCluster cluster : clusters) {
                    if (cluster.getClusterNumber() == nodeClusterNumber) {
                        if (multiplicative) {
                            setRankScoreMultiplicative(nodeAttr, row, cluster);
                        } else {
                            setRankScore(nodeAttr, row, cluster);
                        }
                    }
                }
            }
        }

        return clusters;
    }

    /*
     * Assumes ascending sorted clusters
     */
    public static void insertResultsInColumns(CyNetwork network, List<NodeCluster> clusters, String shortname) {
        CyTable nodeTable = network.getDefaultNodeTable();
        CyTable edgeTable = network.getDefaultEdgeTable();
        CyTable networkTable = network.getDefaultNetworkTable();
        List<CyEdge> edges = network.getEdgeList();

        // Create columns for the score
        ClusterUtils.createNewSingleColumn(networkTable, ClusterManager.RANKING_ATTRIBUTE, String.class, false);
        ClusterUtils.createNewSingleColumn(nodeTable, shortname, Double.class, false);
        ClusterUtils.createNewSingleColumn(edgeTable, shortname, Double.class, false);

        for (CyRow row : networkTable.getAllRows()) {
            row.set(ClusterManager.RANKING_ATTRIBUTE, shortname);
        }

        ClusterUtils.setNodeTableColumnValues(nodeTable, clusters, shortname);
        ClusterUtils.setEdgeTableColumnValues(edgeTable, edges, clusters, shortname);
    }

    private static void setRankScore(String attribute, CyRow row, NodeCluster cluster) {
        try {
            cluster.setRankScore(cluster.getRankScore() + row.get(attribute, Double.class, 0.0));
        } catch (ClassCastException cce) {
            try {
                cluster.setRankScore(cluster.getRankScore() + row.get(attribute, Integer.class, 0));
            } catch (Exception e) { // Not a number type!
                e.printStackTrace();
            }
        }
    }

    private static void setRankScoreMultiplicative(String edgeAttr, CyRow source, NodeCluster cluster) {
        try {
            if (cluster.getRankScore() == 0.0) {
                cluster.setRankScore(1.0);
            }
            cluster.setRankScore(cluster.getRankScore() * (source.get(edgeAttr, Double.class, 0.0) + 1.0)); // assumes values between 0.0 and 1.0
        } catch (ClassCastException cce) { //
            try {
                cluster.setRankScore(cluster.getRankScore() * (source.get(edgeAttr, Integer.class, 0) + 1));
            } catch (Exception e) { // Not a number type!
                e.printStackTrace();
            }
        }
    }

    public static List<NodeCluster> fetchClusters(CyNetwork network) {
        List<NodeCluster> clusters = new ArrayList<>();
        String clusterAttribute = getClusterAttribute(network);

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
        String clusterAttribute = getClusterAttribute(network);
        String rankingAttribute = getRankingAttribute(network);

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

    public static String getClusterAttribute(CyNetwork network) {
        return network.getRow(network, CyNetwork.LOCAL_ATTRS)
                .get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
    }

    private static String getRankingAttribute(CyNetwork network) {
        return network.getRow(network, CyNetwork.LOCAL_ATTRS)
                .get(ClusterManager.RANKING_ATTRIBUTE, String.class);
    }
}
