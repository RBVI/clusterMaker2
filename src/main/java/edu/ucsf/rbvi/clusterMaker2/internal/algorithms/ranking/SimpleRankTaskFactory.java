package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class SimpleRankTaskFactory implements RankFactory {

    public SimpleClusterContext gui;
    public ClusterManager manager;

    public SimpleRankTaskFactory(ClusterManager manager) {
        this.gui = new SimpleClusterContext();
        this.manager = manager;
    }

    public String getShortName() {
        return SimpleCluster.SHORTNAME;
    }

    public String getName() {
        return SimpleCluster.NAME;
    }

    public Object getContext() {
        return gui;
    }

    public void run(TaskMonitor monitor) {

    }

    public boolean isAvailable() {
        return false;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new SimpleCluster(gui, manager));
    }

    public boolean isReady() {
        return false;
    }
}
