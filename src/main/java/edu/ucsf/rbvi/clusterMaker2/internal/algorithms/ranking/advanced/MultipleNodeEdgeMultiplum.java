package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.advanced;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.autosome.mapping.som.Node;
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

public class MultipleNodeEdgeMultiplum extends AbstractTask implements Rank {
    private List<NodeCluster> clusters;
    private ClusterManager manager;
    private List<String> nodeAttributes;
    private List<String> edgeAttributes;
    private String clusterColumnName;
    final public static String NAME = "Create rank from multiple nodes and edges (multiply sum)";
    final public static String SHORTNAME = "MNEMrank";

    @Tunable(description = "Network", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public MNEMContext context;

    public MultipleNodeEdgeMultiplum(MNEMContext context, ClusterManager manager) {
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
        taskMonitor.setTitle("Multiple Node Edge Multiplum ranking of clusters");
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Fetching clusters...");
        taskMonitor.setProgress(10.0);
        clusters = ClusterUtils.fetchClusters(network);
        taskMonitor.setProgress(50.0);

        nodeAttributes = context.getSelectedNodeAttributes();
        edgeAttributes = context.getSelectedEdgeAttributes();

        clusters = setNodeScoreInCluster();
        taskMonitor.setProgress(75.0);
        clusters = setEdgeScoreInCluster();
        createColumns();
        taskMonitor.setProgress(100.0);

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    private void createColumns() {
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

    private List<NodeCluster> setNodeScoreInCluster() {
        List<NodeCluster> clusters = new ArrayList<>(this.clusters);
        List<CyNode> nodes = network.getNodeList();
        CyTable table = network.getDefaultNodeTable();

        for (String nodeAttr : nodeAttributes) {
            for (CyNode node : nodes) {
                CyRow row = table.getRow(node.getSUID());
                int nodeClusterNumber = row.get(clusterColumnName, Integer.class, -1);

                for (NodeCluster cluster : clusters) {
                    if (cluster.getClusterNumber() == nodeClusterNumber) {
                        double score = cluster.getRankScore();

                        try {
                            cluster.setRankScore(cluster.getRankScore() * (row.get(nodeAttr, Double.class, 0.0) + 1.0)); // assumes scores to be between 0 and 1
                        } catch (Exception e) {
                            e.printStackTrace(); // Probably a type mismatch - something not a Double.class
                        }
                    }
                }
            }
        }

        return clusters;
    }

    private List<NodeCluster> setEdgeScoreInCluster() {
        List<NodeCluster> clusters = new ArrayList<>(this.clusters);
        List<CyEdge> edges = network.getEdgeList();
        CyTable table = network.getDefaultNodeTable();

        for (String edgeAttr : edgeAttributes) {
            for (CyEdge edge : edges) {
                CyRow source = table.getRow(edge.getSource().getSUID());
                CyRow target = table.getRow(edge.getTarget().getSUID());
                int sourceClusterNumber = source.get(clusterColumnName, Integer.class, -1);
                int targetClusterNumber = target.get(clusterColumnName, Integer.class, -1);
                int sourceHighestClusterNumber = -1;
                int targetHighestClusterNumber = -1;

                for (NodeCluster cluster : clusters) {
                    int clusterNumber = cluster.getClusterNumber();

                    if (clusterNumber == sourceClusterNumber && (clusterNumber < sourceHighestClusterNumber || sourceHighestClusterNumber == -1)) {

                        try {
                            cluster.setRankScore(cluster.getRankScore() * (source.get(edgeAttr, Double.class, 0.0) + 1.0)); // assumes values between 0 and 1
                        } catch (Exception e) { // Probably not a double class in the edgeAttr column
                            e.printStackTrace(); // just print the trace and continue
                        }

                        sourceHighestClusterNumber = clusterNumber;
                    }

                    if (clusterNumber == targetClusterNumber && (clusterNumber < targetHighestClusterNumber || sourceHighestClusterNumber == -1)) {

                        try {
                            cluster.setRankScore(cluster.getRankScore() * (target.get(edgeAttr, Double.class, 0.0) + 1.0)); // assumes values between 0 and 1
                        } catch (Exception e) { // Probably not a double class in the edgeAttr column
                            e.printStackTrace(); // just print the trace and continue
                        }

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
