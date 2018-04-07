package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.MAM;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.RankFactory;
import org.cytoscape.work.TaskIterator;

public class MAMTaskFactory implements RankFactory {

    public MAMContext context;
    public ClusterManager manager;

    public MAMTaskFactory(ClusterManager manager) {
        this.manager = manager;
        context = new MAMContext(manager);
    }

    public String getShortName() {
        return MultipleAttributeMultiplicative.SHORTNAME;
    }

    public String getName() {
        return MultipleAttributeMultiplicative.NAME;
    }

    public Object getContext() {
        return context;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new MultipleAttributeMultiplicative(context, manager));
    }

    public boolean isReady() {
        if (manager.getNetwork() == null) return false;
        return true;
    }

    @Override
    public String getExampleJSON() {
        return AbstractClusterResults.getRankExampleJSON();
    }

    @Override
    public String getSupportsJSON() {
        return "true";
    };

    @Override
    public String getLongDescription() {
        return "The Multiple Attribute Multiplication (MAM) Method goes "+
               "through all of the nodes in each cluster and multiplies "+
               "the values of the selected attribute.  To avoid the problem of "+
               "multiplying by 0, all values have 1 added to them before "+
               "the multiplication. Each "+
               "cluster is then ranked based on the average sum "+
               "in the cluster";
    }
}
