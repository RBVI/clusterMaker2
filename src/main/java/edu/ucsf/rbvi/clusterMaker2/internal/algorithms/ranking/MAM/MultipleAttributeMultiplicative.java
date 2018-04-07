package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.MAM;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ClusterUtils;
import org.cytoscape.model.*;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import java.util.List;

public class MultipleAttributeMultiplicative extends AbstractTask implements Rank, ObservableTask {
    private ClusterManager manager;
    final public static String NAME = "Create rank from multiple nodes and edges (multiply sum)";
    final public static String SHORTNAME = "MAM";
    private AbstractClusterResults results;

    @Tunable(description = "Network", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public MAMContext context;

    public MultipleAttributeMultiplicative(MAMContext context, ClusterManager manager) {
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
        taskMonitor.setTitle("Multiple Node Edge Multiplum ranking of clusters");
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Fetching clusters...");
        taskMonitor.setProgress(0.1);
        List<NodeCluster> clusters = ClusterUtils.fetchClusters(network);
        taskMonitor.setProgress(0.5);

        String clusterColumnName = ClusterUtils.getClusterAttribute(network);
        List<String> nodeAttributes = context.getSelectedNodeAttributes();
        List<String> edgeAttributes = context.getSelectedEdgeAttributes();

        taskMonitor.setProgress(0.6);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting node scores in clusters");
        clusters = ClusterUtils.setNodeScoresInCluster(network, clusters, nodeAttributes, clusterColumnName, true);
        taskMonitor.setProgress(0.75);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Setting edge scores in clusters");
        clusters = ClusterUtils.setEdgeScoresInCluster(network, clusters, edgeAttributes, clusterColumnName, true);
        taskMonitor.setProgress(0.80);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Sorting and ranking clusters");
        ClusterUtils.ascendingSort(clusters);
        NodeCluster.setClusterRanks(clusters);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Insert cluster information in tables");
        ClusterUtils.insertResultsInColumns(network, clusters, SHORTNAME);
        results = new AbstractClusterResults(network, clusters);
        taskMonitor.setProgress(1.0);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    public static boolean isReady(CyNetwork network, ClusterManager manager) {
        return true;
    }

    @Override
    public List<Class<?>> getResultClasses() {
        return results.getResultClasses();
    }

    @Override
    public <R> R getResults(Class<? extends R> clzz) {
        return results.getResults(clzz);
    }
}
