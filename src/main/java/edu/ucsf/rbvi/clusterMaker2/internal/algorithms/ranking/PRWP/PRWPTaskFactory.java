package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.PRWP;

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
}
