package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.advanced;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import org.cytoscape.work.TaskIterator;

public class MAATaskFactory implements RankFactory {

    public MAAContext context;
    public ClusterManager manager;

    public MAATaskFactory(ClusterManager manager) {
        this.manager = manager;
        context = new MAAContext(manager);
    }

    public String getShortName() {
        return MultipleAttributeAddition.SHORTNAME;
    }

    public String getName() {
        return MultipleAttributeAddition.NAME;
    }

    public Object getContext() {
        return context;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new MultipleAttributeAddition(context, manager));
    }

    public boolean isReady() {
        return true;
    }
}
