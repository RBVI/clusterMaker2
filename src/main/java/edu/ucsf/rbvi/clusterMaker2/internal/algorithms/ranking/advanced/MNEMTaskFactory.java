package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.advanced;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import org.cytoscape.work.TaskIterator;

public class MNEMTaskFactory implements RankFactory {

    public MNEMContext context;
    public ClusterManager manager;

    public MNEMTaskFactory(ClusterManager manager) {
        this.manager = manager;
        context = new MNEMContext(manager);
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
        return new TaskIterator(new MultipleNodeEdgeMultiplum(context, manager));
    }

    public boolean isReady() {
        return MultipleNodeEdgeMultiplum.isReady(manager.getNetwork(), manager);
    }
}
