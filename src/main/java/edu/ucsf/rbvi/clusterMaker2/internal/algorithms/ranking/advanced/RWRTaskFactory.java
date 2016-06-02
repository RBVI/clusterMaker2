package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.advanced;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import org.cytoscape.work.TaskIterator;

public class RWRTaskFactory implements RankFactory {

    public RWRContext context;
    public ClusterManager manager;

    public RWRTaskFactory(ClusterManager manager) {
        this.manager = manager;
        context = new RWRContext(manager);
    }

    @Override
    public String getShortName() {
        return RandomWalkRanking.SHORTNAME;
    }

    @Override
    public String getName() {
        return RandomWalkRanking.NAME;
    }

    @Override
    public Object getContext() {
        return context;
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new RandomWalkRanking(context, manager));
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
