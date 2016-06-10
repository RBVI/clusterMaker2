package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.algorithms;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ClusterUtils;
import org.cytoscape.model.*;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import java.util.ArrayList;
import java.util.List;

public class MultipleAttributeAddition extends AbstractTask implements Rank {
    private List<NodeCluster> clusters;
    private ClusterManager manager;
    private List<String> nodeAttributes;
    private List<String> edgeAttributes;
    private String clusterColumnName;
    final public static String NAME = "Create rank from multiple nodes and edges (additive sum)";
    final public static String SHORTNAME = "MAA";

    @Tunable(description = "Network", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public MAAContext context;

    public MultipleAttributeAddition(MAAContext context, ClusterManager manager) {
        this.context = context;
        this.manager = manager;

        if (network == null) {
            network = this.manager.getNetwork();
        }

        this.context.setNetwork(network);
        this.context.updateContext();
    }

    @Override
    public String getShortName() {
        return SHORTNAME;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object getContext() {
        return context;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        taskMonitor.setProgress(0.0);
        taskMonitor.setTitle("Multiple Node Edge Additive ranking of clusters");
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Fetching clusters...");
        taskMonitor.setProgress(0.1);
        clusters = ClusterUtils.fetchClusters(network);
        taskMonitor.setProgress(0.5);

        clusterColumnName = getClusterColumnName();
        nodeAttributes = context.getSelectedNodeAttributes();
        edgeAttributes = context.getSelectedEdgeAttributes();

        taskMonitor.setProgress(0.6);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting node scores in clusters");
        clusters = setNodeScoresInCluster();
        taskMonitor.setProgress(0.75);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting edge scores in clusters");
        clusters = setEdgeScoresInCluster();
        taskMonitor.setProgress(0.80);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Sorting and ranking clusters");
        ClusterUtils.ascendingSort(clusters);
        NodeCluster.setClusterRanks(clusters);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Insert cluster information in tables");
        insertResultsInColumns();
        taskMonitor.setProgress(1.0);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    /*
     * Assumes ascending sorted clusters
     */
    private void insertResultsInColumns() {
        CyTable nodeTable = network.getDefaultNodeTable();
        CyTable edgeTable = network.getDefaultEdgeTable();
        CyTable networkTable = network.getDefaultNetworkTable();
        List<CyEdge> edges = network.getEdgeList();

        // Create columns for the score
        ClusterUtils.createNewSingleColumn(networkTable, ClusterManager.RANKING_ATTRIBUTE, String.class, false);
        ClusterUtils.createNewSingleColumn(nodeTable, SHORTNAME, Double.class, false);
        ClusterUtils.createNewSingleColumn(edgeTable, SHORTNAME, Double.class, false);

        for (CyRow row : networkTable.getAllRows()) {
            row.set(ClusterManager.RANKING_ATTRIBUTE, SHORTNAME);
        }

        ClusterUtils.setNodeTableColumnValues(nodeTable, clusters, SHORTNAME);
        ClusterUtils.setEdgeTableColumnValues(edgeTable, edges, clusters, SHORTNAME);
    }

    private String getClusterColumnName() {
        return this.network.getRow(network).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class, "");
    }

    private List<NodeCluster> setNodeScoresInCluster() {
        List<NodeCluster> clusters = new ArrayList<>(this.clusters);
        List<CyNode> nodes = network.getNodeList();
        CyTable table = network.getDefaultNodeTable();

        for (String nodeAttr : nodeAttributes) {
            for (CyNode node : nodes) {
                CyRow row = table.getRow(node.getSUID());
                int nodeClusterNumber = row.get(clusterColumnName, Integer.class, -1);

                for (NodeCluster cluster : clusters) {
                    if (cluster.getClusterNumber() == nodeClusterNumber) {
                        setRankScore(nodeAttr, row, cluster);
                    }
                }
            }
        }

        return clusters;
    }

    private void setRankScore(String attribute, CyRow row, NodeCluster cluster) {
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

    private List<NodeCluster> setEdgeScoresInCluster() {
        List<NodeCluster> clusters = new ArrayList<>(this.clusters);
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
                        setRankScore(edgeAttr, edgeRow, cluster);
                        sourceHighestClusterNumber = clusterNumber;
                    } else if (clusterNumber == targetClusterNumber && (clusterNumber < targetHighestClusterNumber || targetHighestClusterNumber == -1)) {
                        setRankScore(edgeAttr, edgeRow, cluster);
                        targetHighestClusterNumber = clusterNumber;
                    }
                }
            }
        }

        return clusters;
    }

    public static boolean isReady(CyNetwork network, ClusterManager manager) {
        return true;
    }
}
