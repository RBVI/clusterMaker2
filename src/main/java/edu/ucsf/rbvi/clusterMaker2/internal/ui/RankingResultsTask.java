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
import org.cytoscape.work.Tunable;

public class RankingResultsTask extends AbstractTask implements ClusterViz, ClusterAlgorithm {

    private static String appName = "Ranklust Ranking Results Panel";
    public static String RANKLUSTNAME = "Create Results Panel from Ranking Clusters";
    public static String RANKLUSTSHORTNAME = "ranklustRankingResultsPanel";
    private ClusterManager manager;
    private CyNetworkView networkView;
    private RankingResults rankingResults;
    private final CyServiceRegistrar registrar;

    @Tunable(description="Network to look for cluster", context="nogui")
    private CyNetwork network;

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

    public List<NodeCluster> getClusters() {
        List<NodeCluster> clusters = new ArrayList<NodeCluster>();

        String clusterAttribute = network.getRow(network, CyNetwork.LOCAL_ATTRS)
            .get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);

        String rankingAttribute = network.getRow(network, CyNetwork.LOCAL_ATTRS)
            .get(ClusterManager.RANKING_ATTRIBUTE, String.class);

        assert(clusterAttribute != null);
        assert(rankingAttribute != null);

        Map<Integer, ArrayList<List<CyNode>> clusterMap = new HashMap<Integer, 
                                                        ArrayList<CyNode>>();

        for (CyNode node : (List<CyNode>) network.getNodeList()) {
            if (ModelUtils.hasAttribute(network, node, clusterAttribute) {

                Integer cluster = network.getRow(node).get(clusterAttribute, Integer.class);

                if (!clusterMap.containsKey(cluster) {
                    clusterMap.put(cluster, new ArrayList<CyNode>());
                }

                clusterMap.get(cluster).add(node);
            }
        }

        List<Double> scores = null;
        if (network.getDefaultNetworkTable().getColumn(rankingAttribute) != null) {
            scores = network.getRow(network, CyNetwork.LOCAL_ATTRS).getList(rankingAttribute, Double.class);
        }

        for (int clustNum : clusterMap.keySet()) {
            NodeCluster cluster = new NodeCluster(clusterMap.get(clustNum));
            cluster.setClusterNumber(clustNum);
            cluster.setRank(1); // bogus - change!
            cluster.setRankScore(10); // bogus - change!
            clusters.add(cluster);
        }

        return clusters;
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
