package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.advanced;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ClusterUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import java.util.List;

public class RandomWalkRanking extends AbstractTask implements Rank {
    private List<NodeCluster> clusters;
    private ClusterManager manager;
    private List<String> nodeAttributes;
    private List<String> edgeAttributes;
    private String clusterColumnName;
    private int iterations = 5;

    public static final String SHORTNAME = "RWR";
    public static final String NAME = "RandomWalkRanking";

    @Tunable(description = "Network", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public RWRContext context;

    public RandomWalkRanking(RWRContext context, ClusterManager manager) {
        this.context = context;
        this.manager = manager;

        if (network == null) {
            network = this.manager.getNetwork();
        }

        this.context.setNetwork(network);
        this.context.updateContext();;
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
        return null;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        taskMonitor.setProgress(0.0);
        taskMonitor.setTitle("Random walking Ranking of clusters");
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Fetching clusters...");
        taskMonitor.setProgress(0.1);
        clusters = ClusterUtils.fetchClusters(network);
        taskMonitor.setProgress(0.5);

        clusterColumnName = getClusterColumnName();
        nodeAttributes = context.getSelectedNodeAttributes();
        edgeAttributes = context.getSelectedEdgeAttributes();

        taskMonitor.setProgress(0.6);

        assignStartValues(clusters);
        for (int i = 0; i < iterations; i++) {
            iterate(clusters);
        }
        summarize(clusters);

    }

    private void assignStartValues(List<NodeCluster> clusters) {
        for (NodeCluster cluster : clusters) {
            cluster.initSWRWRScores();
        }
    }

    private void iterate(List<NodeCluster> clusters) {
    }

    private void summarize(List<NodeCluster> clusters) {
        for (NodeCluster cluster : clusters) {
            cluster.setClusterScore(cluster.getNodeScores().stream().reduce(0.0, Double::sum));
        }
    }

    public String getClusterColumnName() {
        return this.network.getRow(network).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class, "");
    }
}
