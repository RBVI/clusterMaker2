package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.PR;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
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
        return PR.SHORTNAME;
    }

    @Override
    public String getName() {
        return PR.NAME;
    }

    @Override
    public Object getContext() {
        return context;
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new PR(context, manager));
    }

    public boolean isReady() {
        if (manager.getNetwork() == null) return false;
        return true;
    }

    @Override
    public String getLongDescription() {
        return "The Page Rank (PR) algorithm is a random walk algorithm where "+
               "the score for a given node may be thought of as the fraction "+
               "of time spent 'visiting' that node (measured over all time) "+
               "in a random walk over the vertices (following outgoing edges "+
               "from each node). PageRank modifies this random walk by adding "+
               "to the model a probability (specified as 'alpha' in the constructor) "+
               "of jumping to any node. If alpha is 0, this is equivalent to "+
               "the eigenvector centrality algorithm; if alpha is 1, all vertices "+
               "will receive the same score (1/|V|). Thus, alpha acts as a sort "+
               "of score smoothing parameter.";
    }

    @Override
    public String getExampleJSON() {
        return AbstractClusterResults.getRankExampleJSON();
    }

    @Override
    public String getSupportsJSON() {
        return "true";
    };
}
