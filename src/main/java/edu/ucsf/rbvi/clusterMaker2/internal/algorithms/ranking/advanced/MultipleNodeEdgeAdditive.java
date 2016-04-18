package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.advanced;

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

public class MultipleNodeEdgeAdditive extends AbstractTask implements Rank {
    private List<NodeCluster> clusters;
    private ClusterManager manager;
    private List<String> nodeAttributes;
    private List<String> edgeAttributes;
    private String clusterColumnName;
    final public static String NAME = "Create rank from multiple nodes and edges (additive sum)";
    final public static String SHORTNAME = "MNEArank";

    @Tunable(description = "Network", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public MNEAContext context;

    public MultipleNodeEdgeAdditive(MNEAContext context, ClusterManager manager) {
        this.context = context;
        this.manager = manager;

        if (network == null) {
            network = this.manager.getNetwork();
        }

        clusterColumnName = getClusterColumnName();
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
        taskMonitor.setProgress(10.0);
        clusters = ClusterUtils.fetchClusters(network);
        taskMonitor.setProgress(50.0);

        nodeAttributes = context.getSelectedNodeAttributes();
        edgeAttributes = context.getSelectedEdgeAttributes();

        clusters = setNodeScoresInCluster();
        taskMonitor.setProgress(75.0);
        clusters = setEdgeScoresInCluster();
        insertResultsInColumns();
        taskMonitor.setProgress(100.0);

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    private void insertResultsInColumns() {
        CyTable nodeTable = network.getDefaultNodeTable();
        CyTable edgeTable = network.getDefaultEdgeTable();
        CyTable networkTable = network.getDefaultNetworkTable();
        List<CyEdge> edges = network.getEdgeList();

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
        } catch (Exception e) {
            e.printStackTrace(); // Probably a type mismatch - something not a Double.class
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
