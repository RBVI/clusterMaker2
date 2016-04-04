package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.advanced;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import org.cytoscape.work.TaskIterator;

public class MNEATaskFactory implements RankFactory {

    public MNEAContext context;
    public ClusterManager manager;

    public MNEATaskFactory(ClusterManager manager) {
        this.manager = manager;
        context = new MNEAContext(manager);
    }

    @Override
    public String getShortName() {
        return MultipleNodeEdgeAdditive.SHORTNAME;
    }

    @Override
    public String getName() {
        return MultipleNodeEdgeAdditive.NAME;
    }

    @Override
    public Object getContext() {
        return context;
    }

    @Override
    public boolean isAvailable() {
        return MultipleNodeEdgeAdditive.isReady(manager.getNetwork(), manager);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new MultipleNodeEdgeAdditive(context, manager));
    }

    @Override
    public boolean isReady() {
        return false;
    }
}
