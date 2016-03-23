package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class RankingResultsTask extends AbstractTask implements ClusterViz, ClusterAlgorithm {

    private static String appName = "Ranklust Ranking Results Panel";
    public static String RANKLUSTNAME = "Create Results Panel from Ranking Clusters";
    public static String RANKLUSTSHORTNAME = "ranklustRankingResultsPanel";
    private ClusterManager manager;
    private CyNetworkView networkView;
    private CyNetwork network;
    private RankingResults rankingResults;
    private final CyServiceRegistrar registrar;

    public RankingResultsTask(ClusterManager manager, CyNetworkView networkView) {
        this.manager = manager;
        this.networkView = networkView;

        registrar = manager.getService(CyServiceRegistrar.class);

        if (network == null) {
            network = manager.getNetwork();
        }
    }

    @Override
    public String getShortName() {
        return RANKLUSTSHORTNAME;
    }

    @Override
    public String getName() {
        return RANKLUSTNAME;
    }

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        taskMonitor.setProgress(0.0);
        taskMonitor.setStatusMessage("Calculating Ranking Results...");
        rankingResults = new RankingResults(null, taskMonitor, network);
        manager.addRankingResults(network, rankingResults);
    }

    @Override
    public boolean isAvailable() {
        return network != null;
    }

    @Override
    public ClusterResults getResults() {
        return null;
    }
}
