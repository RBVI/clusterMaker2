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
        List<Double> scoreList = new ArrayList<>();

        for (String nodeAttr : nodeAttributes) {
            addNodeScoreToColumn(nodeAttr, taskMonitor, scoreList);
        }

        taskMonitor.setProgress(75.0);

        for (String edgeAttr : edgeAttributes) {
            addEdgeScoreToColumn(taskMonitor, scoreList);
        }

        taskMonitor.setProgress(100.0);

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    private String getClusterColumnName() {
        return this.network.getRow(network).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class, "");
    }

    private void addNodeScoreToColumn(String nodeAttr, TaskMonitor taskMonitor, List<Double> scoreList) {

    }

    private void addEdgeScoreToColumn(TaskMonitor taskMonitor, List<Double> scoreList) {
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
                            cluster.setRankScore(cluster.getRankScore() + source.get(edgeAttr, Double.class, 0.0));
                        } catch (Exception e) { // Probably not a double class in the edgeAttr column
                            e.printStackTrace(); // just print the trace and continue
                        }

                        sourceHighestClusterNumber = clusterNumber;
                    }

                    if (clusterNumber == targetClusterNumber && (clusterNumber < targetHighestClusterNumber || sourceHighestClusterNumber == -1)) {

                        try {
                            cluster.setRankScore(cluster.getRankScore() + target.get(edgeAttr, Double.class, 0.0));
                        } catch (Exception e) { // Probably not a double class in the edgeAttr column
                            e.printStackTrace(); // just print the trace and continue
                        }

                        targetHighestClusterNumber = clusterNumber;
                    }
                }
            }
        }
    }

    public static boolean isReady(CyNetwork network, ClusterManager manager) {
        return true;
    }
}
