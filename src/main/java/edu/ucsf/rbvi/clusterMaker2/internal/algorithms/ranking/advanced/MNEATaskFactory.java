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

    public String getShortName() {
        return MultipleNodeEdgeAdditive.SHORTNAME;
    }

    public String getName() {
        return MultipleNodeEdgeAdditive.NAME;
    }

    public Object getContext() {
        return context;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new MultipleNodeEdgeAdditive(context, manager));
    }

    public boolean isReady() {
        return true;
    }
}
