package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import org.cytoscape.work.TaskIterator;

public class SimpleRankTaskFactory implements RankFactory {

    public SimpleClusterContext context;
    public ClusterManager manager;

    public SimpleRankTaskFactory(ClusterManager manager) {
        this.manager = manager;
        this.context = new SimpleClusterContext(manager);
    }

    public String getShortName() {
        return SimpleCluster.SHORTNAME;
    }

    public String getName() {
        return SimpleCluster.NAME;
    }

    public Object getContext() {
        return context;
    }

    public boolean isAvailable() {
        return SimpleCluster.isReady(this.manager.getNetwork(), this.manager);
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new SimpleCluster(context, manager));
    }

    public boolean isReady() {
        return true;
    }
}
