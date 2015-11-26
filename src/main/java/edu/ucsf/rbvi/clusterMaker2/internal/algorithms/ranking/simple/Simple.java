package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.simple;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.RankingContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class Simple implements Task {
    ClusterManager manager;
    public static final String NAME = "Simple NAME";
    public static final String SHORTNAME = "RSN";

    private boolean canceled;

    @Tunable(description = "Cluster to rank")
    public NodeCluster cluster;

    @ContainsTunables
    public RankingContext guiContext;

    public Simple(RankingContext guiContext, ClusterManager manager) {
        this.manager = manager;
        this.guiContext = guiContext;
        this.canceled = false;
    }

    public void run(TaskMonitor monitor) {
        monitor.setTitle("yolobaggins");

    }

    public void cancel() {
        this.canceled = true;
    }
}
