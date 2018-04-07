package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.MAA;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
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
        if (manager.getNetwork() == null) return false;
        return true;
    }

    @Override
    public String getLongDescription() {
        return "The Multiple Attribute Additive (MAA) Method goes "+
               "through all of the nodes in each cluster and sums "+
               "up the values of the selected attribute.  Each "+
               "cluster is then ranked based on the average sum "+
               "in the cluster";
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
