package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.PRWP;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import org.cytoscape.work.TaskIterator;

public class PRWPTaskFactory implements RankFactory{

    public PRWPContext context;
    public ClusterManager manager;

    public PRWPTaskFactory(ClusterManager manager) {
        this.manager = manager;
        context = new PRWPContext(manager);
    }

    @Override
    public String getShortName() {
        return PRWP.SHORTNAME;
    }

    @Override
    public String getName() {
        return PRWP.NAME;
    }

    @Override
    public Object getContext() {
        return context;
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new PRWP(context, manager));
    }

    public boolean isReady() {
        if (manager.getNetwork() == null) return false;
        return true;
    }

    @Override
    public String getLongDescription() {
        return "A generalization of PageRank that permits "+
               "non-uniformly-distributed random jumps. The "+
               "'node_priors' (that is, prior probabilities for "+
               "each node) may be thought of as the fraction "+
               "of the total 'potential' that is assigned to that "+
               "node at each step out of the portion that is "+
               "assigned according to random jumps (this portion "+
               "is specified by 'alpha').";
    }

    @Override
    public String getExampleJSON() {
        return AbstractClusterResults.getRankExampleJSON();
    }

    @Override
    public String getSupportsJSON() {
        return "true";
    }
}
