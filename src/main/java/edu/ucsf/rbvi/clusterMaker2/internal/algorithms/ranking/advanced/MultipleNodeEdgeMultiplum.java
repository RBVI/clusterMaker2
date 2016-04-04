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

public class MultipleNodeEdgeMultiplum extends AbstractTask implements Rank{
    private List<NodeCluster> clusters;
    private ClusterManager manager;
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

        this.context.setNetwork(network);
        this.context.updateContext();
    }

    @Override
    public String getShortName() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        taskMonitor.setTitle("Multiple Node Edge Multiplum ranking of clusters");
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Creating clusters...");
        clusters = ClusterUtils.createClusters(network);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    public static boolean isReady(CyNetwork network, ClusterManager manager) {
        return true;
    }
}
