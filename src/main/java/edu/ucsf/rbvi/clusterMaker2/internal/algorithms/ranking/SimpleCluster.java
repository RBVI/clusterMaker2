package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import edu.ucsf.rbvi.clusterMaker2.internal.commands.GetNetworkClusterTask;
import org.cytoscape.model.*;
import org.cytoscape.work.*;

import java.util.*;

public class SimpleCluster extends AbstractTask implements Rank {

    private List<List<CyNode>> clusters;
    private ClusterManager manager;
    private String attribute;
    private boolean canceled;
    public static String NAME = "Create rank from clusters";
    public static String SHORTNAME = "ranklust";
    public static String GROUP_ATTRIBUTE = SHORTNAME;

    @Tunable(description = "Network to look for cluster", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public SimpleClusterContext context;

    public SimpleCluster(SimpleClusterContext context, ClusterManager manager) {
        System.out.println("SimpleCluster constructor");
        this.canceled = false;
        this.manager = manager;
        this.context = context;
        this.network = this.manager.getNetwork();
        context.setNetwork(this.network);
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
        monitor.setTitle("SimpleCluster.run()");

        GetNetworkClusterTask clusterMonitor = new GetNetworkClusterTask(manager);

        if (!clusterIsReady(clusterMonitor)) {
            return;
        }

        clusterMonitor.run(monitor);

        this.clusters = new ArrayList<>((Collection<List<CyNode>>)
                ((Map<String, Object>) clusterMonitor.getResults(Map.class)).get("networkclusters"));

        CyTable nodeTable = network.getDefaultNodeTable();

        if (!noNullValuesOrCancel(nodeTable, monitor)) {
            return;
        }

        monitor.showMessage(TaskMonitor.Level.INFO, "Getting scorelist for simpleCluster.");
        List<Integer> scoreList = createScoreList(nodeTable);
        addScoreToColumn(nodeTable, scoreList, monitor); // This can be abstract for ALL of ranking cluster algorithms
        monitor.showMessage(TaskMonitor.Level.INFO, "Done.");
        System.out.println("SimpleCluster finished.");
    }

    /*
     * For each row in the table, index on the clustering group attribute and add a column with the score
     */
    private void addScoreToColumn(CyTable table, List<Integer> scoreList, TaskMonitor monitor) {
        //String clusterColumnName = this.context.getSelectedAlgorithm();
        List<CyRow> rows = table.getAllRows();
        String clusterColumnName = this.getClusterColumn(table.getColumns());
        String rankColumnName = this.context.getClusterAttribute();
        System.out.println("Number of rows in the table: " + rows.size());

        table.createColumn(rankColumnName, String.class, false);

        if (clusterColumnName.equals("")) {
            monitor.showMessage(TaskMonitor.Level.INFO, "Could not find cluster column name to work with");
            return;
        }

        for (CyRow row : rows) {
            int index = row.get(clusterColumnName, Integer.class, 0);
            if (index == 0) {// node is node clustered!
                System.out.println("No column found with <" + clusterColumnName + "> as columnName");
                continue;
            }
            row.set(rankColumnName, scoreList.get(index - 1));
        }
    }

    private String getClusterColumn(Collection<CyColumn> columns) {
        for (CyColumn column : columns) {
            String columnName = column.getName().toLowerCase();
            if (columnName.contains("cluster")) {
                return columnName;
            }
        }
        System.out.println("ERROR: Could not find clustering column");
        return "";
    }

    private boolean clusterIsReady(GetNetworkClusterTask clusterMonitor) {
        if (network == null) {
            this.manager.getNetwork();
        }

        this.context.setNetwork(network);
        this.attribute = this.context.getSelectedAttribute();
        clusterMonitor.algorithm = this.context.getSelectedAlgorithm();

        // This should be removed in the future
        if (clusterMonitor.algorithm.equals("None")) {
            return false;
        }

        clusterMonitor.network = network;
        return true;
    }

    private List<Integer> createScoreList(CyTable nodeTable) {
        List<Integer> scoreList = new ArrayList<>(this.clusters.size());

        System.out.println("SimpleCluster is running.");

        for (int i = 0; i < this.clusters.size(); i++) {
            int score = 0;
            scoreList.add(i, score);
            for (CyNode node : this.clusters.get(i)) {
                score += nodeTable.getRow(node.getSUID()).get(this.attribute, Integer.class, 0);
            }
            scoreList.set(i, score);
        }

        return scoreList;
    }

    private boolean noNullValuesOrCancel(CyTable nodeTable, TaskMonitor monitor) {
        if (this.clusters.size() == 0) {
            monitor.showMessage(TaskMonitor.Level.INFO, "No clusters to work with");
            return false;
        } else if (this.attribute == null || this.attribute.equals("None")) {
            monitor.showMessage(TaskMonitor.Level.INFO, "No attribute(s) to work with");
            return false;
        } else if (nodeTable.getColumn(this.attribute) == null) {
            monitor.showMessage(TaskMonitor.Level.INFO, "No column with '" + this.attribute + "' as an attribute");
            return false;
        } else if (this.canceled) {
            monitor.showMessage(TaskMonitor.Level.INFO, "Canceled");
            return false;
        }

        return true;
    }

    public boolean isAvailable() {
        return SimpleCluster.isReady(this.network, this.manager);
    }

    // This should go through the clustering algorithms and check if one of them have some results.
    // NB! Only the algorithm run last will have results to work with!
    public static boolean isReady(CyNetwork network, ClusterManager manager) {
        GetNetworkClusterTask clusterMonitor;

        if (network == null) {
            return false;
        }

        clusterMonitor = new GetNetworkClusterTask(manager);
        clusterMonitor.algorithm = "shit"; // Temporary

        if (clusterMonitor.algorithm.equals("None")) {
            return false;
        }

        return true;
    }

    public void cancel() {
        this.canceled = true;
    }
}
