package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.algorithms;

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

public class PageRank extends AbstractTask implements Rank {
    private List<NodeCluster> clusters;
    private ClusterManager manager;
    private List<String> nodeAttributes;
    private List<String> edgeAttributes;
    private String clusterColumnName;
    final public static String NAME = "Create rank from the Page Rank algorithm with priors";
    final public static String SHORTNAME = "PRP";

    @Tunable(description = "Network", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public PRContext context;

    public PageRank(PRContext context, ClusterManager manager) {
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
        taskMonitor.setProgress(0.75);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting edge scores in clusters");
        taskMonitor.setProgress(0.80);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Sorting and ranking clusters");
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Insert cluster information in tables");
        taskMonitor.setProgress(1.0);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    public String getClusterColumnName() {
        return this.network.getRow(network).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class, "");
    }
}
