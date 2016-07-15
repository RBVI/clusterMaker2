package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.HITS;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import org.cytoscape.work.TaskIterator;

public class HITSTaskFactory implements RankFactory {

    public HITSContext context;
    public ClusterManager manager;

    public HITSTaskFactory(ClusterManager manager) {
        this.manager = manager;
        context = new HITSContext(manager);
    }

    @Override
    public String getShortName() {
        return HyperlinkInducedTopicSearch.SHORTNAME;
    }

    @Override
    public String getName() {
        return HyperlinkInducedTopicSearch.NAME;
    }

    @Override
    public Object getContext() {
        return context;
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new HyperlinkInducedTopicSearch(context, manager));
    }

    @Override
    public boolean isReady() {
        return true;
    }
}

