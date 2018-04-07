package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.HITS;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
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

		public boolean isReady() {
			if (manager.getNetwork() == null) return false;
			return true;
		}

		@Override
		public String getLongDescription() {
			return "Hyperlink-Induced Topic Search (HITS; also known "+
			       "as hubs and authorities) is a link analysis algorithm "+
			       "that rates Web pages, developed by Jon Kleinberg. The "+
			       "idea behind Hubs and Authorities stemmed from a particular "+
			       "insight into the creation of web pages when the Internet "+
			       "was originally forming; that is, certain web pages, known "+
			       "as hubs, served as large directories that were not actually "+
			       "authoritative in the information that they held, but were "+
			       "used as compilations of a broad catalog of information that "+
			       "led users direct to other authoritative pages. In other words, "+
			       "a good hub represented a page that pointed to many other "+
			       "pages, and a good authority represented a page that was linked "+
			       "by many different hubs.";
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

