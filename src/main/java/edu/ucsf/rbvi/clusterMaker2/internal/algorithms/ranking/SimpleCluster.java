package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class SimpleCluster extends AbstractTask implements Rank {

    private ClusterManager manager;
    public static String NAME = "Create rank from clusters";
    public static String SHORTNAME = "ranklust";

    @Tunable(description = "Network to look for cluster", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public SimpleClusterContext gui;

    public SimpleCluster(ClusterManager manager) {
        this.manager = manager;
        this.network = this.manager.getNetwork();

    }

    public String getShortName() {
        return SHORTNAME;
    }

    public String getName() {
        return NAME;
    }

    public Object getContext() {
        return this.gui;
    }

    public void run(TaskMonitor monitor) {

    }

    public boolean isAvailable() {
        return SimpleCluster.isReady(network);
    }

    private static boolean isReady(CyNetwork network) {
        return network != null;
    }
}
