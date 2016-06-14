package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.algorithms;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import org.cytoscape.work.TaskIterator;

public class PRTaskFactory implements RankFactory{

    public PRContext context;
    public ClusterManager manager;

    public PRTaskFactory(ClusterManager manager) {
        this.manager = manager;
        context = new PRContext(manager);
    }

    @Override
    public String getShortName() {
        return PageRank.SHORTNAME;
    }

    @Override
    public String getName() {
        return PageRank.NAME;
    }

    @Override
    public Object getContext() {
        return context;
    }

    @Override
    public TaskIterator createTaskIterator() {
        System.out.println("YOLOBAGGINS HERE WE GO AGAIN");
        return new TaskIterator(new PageRank(context, manager));
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
