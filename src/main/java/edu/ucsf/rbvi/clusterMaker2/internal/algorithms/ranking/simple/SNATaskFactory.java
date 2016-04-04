package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.simple;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import org.cytoscape.work.TaskIterator;

public class SNATaskFactory implements RankFactory {

    public SNAContext context;
    public ClusterManager manager;

    public SNATaskFactory(ClusterManager manager) {
        this.manager = manager;
        this.context = new SNAContext(manager);
    }

    public String getShortName() {
        return SingleNodeAttribute.SHORTNAME;
    }

    public String getName() {
        return SingleNodeAttribute.NAME;
    }

    public Object getContext() {
        return context;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new SingleNodeAttribute(context, manager));
    }

    public boolean isReady() {
        return SingleNodeAttribute.isReady(this.manager.getNetwork(), this.manager);
    }
}
