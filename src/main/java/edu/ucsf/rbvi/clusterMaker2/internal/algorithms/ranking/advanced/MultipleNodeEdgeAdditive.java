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

public class MultipleNodeEdgeAdditive extends AbstractTask implements Rank {

    private List<NodeCluster> clusters;
    private ClusterManager manager;
    private boolean canceled;
    public static String NAME = "Create rank from multiple nodes and edges";
    public static String SHORTNAME = "MNEArank";
    public static String GROUP_ATTRIBUTE = "ranklust";

    @Tunable(description = "Network", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public MNEAContext context;

    public MultipleNodeEdgeAdditive(MNEAContext context, ClusterManager manager) {
        this.context = context;
        this.manager = manager;
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
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        taskMonitor.setTitle("Multiple Node Edge Additive ranking of clusters");
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Creating clusters...");
        clusters = ClusterUtils.createClusters(network);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    public void cancel() {
        canceled = true;
    }

    public static boolean isReady(CyNetwork network, ClusterManager manager) {
        return true;
    }
}
