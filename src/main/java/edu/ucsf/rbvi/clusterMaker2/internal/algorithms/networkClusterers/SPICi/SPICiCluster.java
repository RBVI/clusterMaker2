package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.SPICi;


// Cytoscape imports
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.AbstractNetworkClusterer;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;

public class SPICiCluster extends AbstractNetworkClusterer {

    ClusterManager clusterManager;
    SPICiContext context = null;

    public static String SHORTNAME = "SPICi";
    public static String NAME = "SPICi cluster";

    public SPICiCluster(SPICiContext context, ClusterManager clusterManager) {
        super(clusterManager);
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {

    }

    public String getShortName() {
        return null;
    }

    public String getName() {
        return null;
    }
}
