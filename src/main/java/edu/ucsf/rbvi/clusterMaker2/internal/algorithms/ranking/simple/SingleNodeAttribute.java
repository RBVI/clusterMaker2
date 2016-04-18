package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.simple;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import edu.ucsf.rbvi.clusterMaker2.internal.commands.GetNetworkClusterTask;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ClusterUtils;
import org.cytoscape.model.*;
import org.cytoscape.work.*;

import java.util.*;

public class SingleNodeAttribute extends AbstractTask implements Rank {

    private List<NodeCluster> clusters;
    private ClusterManager manager;
    private String attribute;
    public static String NAME = "Create rank from clusters";
    public static String SHORTNAME = "SNArank";

    @Tunable(description = "Network to look for cluster", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public SNAContext context;

    public SingleNodeAttribute(SNAContext context, ClusterManager manager) {
        this.manager = manager;
        this.context = context;

        if (network == null) {
            network = this.manager.getNetwork();
        }

        this.context.setNetwork(network);
        this.context.updateContext();
    }

    public String getShortName() {
        return SHORTNAME;
    }

    public String getName() {
        return NAME;
    }

    public Object getContext() {
        return this.context;
    }

    @SuppressWarnings("unchecked")
    public void run(TaskMonitor monitor) {
        monitor.setTitle("SingleNodeAttribute.run()");


        if (network == null) {
            this.manager.getNetwork();
            monitor.showMessage(TaskMonitor.Level.INFO, "");
        }

        context.setNetwork(network);
        clusters = ClusterUtils.fetchClusters(network);
        attribute = context.getSelectedAttribute();

        if (nullValuesOrCancel(monitor)) {
            monitor.showMessage(TaskMonitor.Level.INFO, "There exists null values or process was canceled");
            return;
        }

        monitor.showMessage(TaskMonitor.Level.INFO, "Getting scorelist for SingleNodeAttribute.");
        monitor.showMessage(TaskMonitor.Level.INFO, "Creating scoring list...");
        List<Double> scoreList = createScoreList();
        monitor.showMessage(TaskMonitor.Level.INFO, "Adding score to columns...");
        addScoreToColumns(createScoreList(), monitor);
        monitor.showMessage(TaskMonitor.Level.INFO, "Done.");
    }

    /*
     * For each row in the table, index on the clustering group attribute and add a column with the score
     */
    private void addScoreToColumns(List<Double> scoreList, TaskMonitor monitor) {
        CyTable nodeTable = network.getDefaultNodeTable();
        CyTable networkTable = network.getDefaultNetworkTable();
        List<CyRow> rows = nodeTable.getAllRows();
        String clusterColumnName = this.getClusterColumn();
        String rankColumnName = this.context.getClusterAttribute();
        System.out.println("Number of rows in the table: " + rows.size());

        if (clusterColumnName.equals("")) {
            monitor.showMessage(TaskMonitor.Level.INFO, "Could not find cluster column name to work with");
            return;
        }

        if (nodeTable.getColumn(rankColumnName) != null) {
            nodeTable.deleteColumn(rankColumnName);
        }

        if (networkTable.getColumn(ClusterManager.RANKING_ATTRIBUTE) != null) {
            networkTable.deleteColumn(ClusterManager.RANKING_ATTRIBUTE);
        }

        nodeTable.createColumn(rankColumnName, Double.class, false);
        networkTable.createColumn(ClusterManager.RANKING_ATTRIBUTE, String.class, false);

        for (CyRow rankRow : networkTable.getAllRows()) {
            rankRow.set(ClusterManager.RANKING_ATTRIBUTE, "__SCRank");
        }

        for (CyRow row : rows) {
            int cluster = row.get(clusterColumnName, Integer.class, 0);
            if (cluster != 0) {
                row.set(rankColumnName, scoreList.get(cluster - 1));
            }
        }
    }

    private String getClusterColumn() {
        return this.network.getRow(network).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class, "");
    }

    private List<Double> createScoreList() {
        CyTable nodeTable = this.network.getDefaultNodeTable();
        List<Double> scoreList = new ArrayList<>(this.clusters.size());

        System.out.println("SingleNodeAttribute is running.");

        for (int i = 0; i < this.clusters.size(); i++) {
            double score = 0;

            for (CyNode node : this.clusters.get(i)) {
                try {
                    score += nodeTable.getRow(node.getSUID()).get(this.attribute, Double.class, 0.0);
                } catch (ClassCastException cce) {
                    score += (double) nodeTable.getRow(node.getSUID()).get(this.attribute, Integer.class, 0);
                }
            }

            scoreList.add(i, score);
        }

        return scoreList;
    }

    private boolean nullValuesOrCancel(TaskMonitor monitor) {
        CyTable nodeTable = network.getDefaultNodeTable();

        if (clusters.size() == 0) {
            monitor.showMessage(TaskMonitor.Level.INFO, "No clusters to work with");
            return true;
        } else if (attribute == null || attribute.equals("None")) {
            monitor.showMessage(TaskMonitor.Level.INFO, "No attribute(s) to work with");
            return true;
        } else if (nodeTable.getColumn(attribute) == null) {
            monitor.showMessage(TaskMonitor.Level.INFO, "No column with '" + attribute + "' as an attribute");
            return true;
        } else if (cancelled) {
            monitor.showMessage(TaskMonitor.Level.INFO, "Canceled");
            return true;
        }

        return false;
    }

    // This should go through the clustering algorithms and check if one of them have some results.
    // NB! Only the algorithm run last will have results to work with!
    public static boolean isReady(CyNetwork network, ClusterManager manager) {
        return true;
    }
}
