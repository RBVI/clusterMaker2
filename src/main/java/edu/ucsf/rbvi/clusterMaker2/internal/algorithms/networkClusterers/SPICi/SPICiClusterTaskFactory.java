package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.SPICi;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import org.cytoscape.work.TaskIterator;

import java.util.Collections;
import java.util.List;

public class SPICiClusterTaskFactory extends AbstractClusterTaskFactory {
    ClusterManager clusterManager;
    SPICiContext context = null;

    public SPICiClusterTaskFactory(ClusterManager clusterManager) {
        this.context = new SPICiContext();
        this.clusterManager = clusterManager;
    }

    public String getShortName() {
        return SPICiCluster.SHORTNAME;
    }

    public String getName() {
        return SPICiCluster.NAME;
    }

    public ClusterViz getVisualizer() {
        return null;
    }

    public List<ClusterType> getTypeList() {
        return Collections.singletonList(ClusterType.NETWORK);
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new SPICiCluster(context, clusterManager));
    }

    public boolean isReady() {
        return true;
    }
}
