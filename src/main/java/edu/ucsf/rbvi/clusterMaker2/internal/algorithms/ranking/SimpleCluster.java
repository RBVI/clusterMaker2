package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import edu.ucsf.rbvi.clusterMaker2.internal.commands.GetNetworkClusterTask;
import org.cytoscape.model.*;
import org.cytoscape.work.*;

import java.util.*;

public class SimpleCluster extends AbstractTask implements Rank {

    private GetNetworkClusterTask clusterMonitor;
    private List<List<CyNode>> clusters;
    private ClusterManager manager;
    private String attribute;
    private boolean canceled;
    public static String NAME = "Create rank from clusters";
    public static String SHORTNAME = "ranklust";
    public static String GROUP_ATTRIBUTE = SHORTNAME;

    @Tunable(description = "Network to look for cluster", context = "nogui")
    public static CyNetwork network;

    @ContainsTunables
    public SimpleClusterContext context;

    public SimpleCluster(SimpleClusterContext context, ClusterManager manager) {
        System.out.println("SimpleCluster constructor");
        this.canceled = false;
        this.manager = manager;
        this.context = context;
        network = this.manager.getNetwork();
        context.setNetwork(network);
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

        if (network == null) {
            this.manager.getNetwork();
        }

        /*
         * Update the GUI
         */
        this.context.setNetwork(network);

       /*
        * Get the cluster etc.
        */
        this.attribute = this.context.getSelectedAttribute();
        this.clusterMonitor = new GetNetworkClusterTask(manager);
        this.clusterMonitor.algorithm = this.context.getSelectedAlgorithm();

        if (this.clusterMonitor.algorithm.equals("None")) {
            return;
        }

        this.clusterMonitor.network = network;
        this.clusterMonitor.run(monitor);
        this.clusters = new ArrayList<>((Collection<List<CyNode>>)
                ((Map<String, Object>) this.clusterMonitor.getResults(Map.class)).get("networkclusters"));

        CyTable nodeTable = network.getDefaultNodeTable();
        if (!readyToGo(nodeTable, monitor)) {
            return;
        }

        // Start algorithm here
        monitor.showMessage(TaskMonitor.Level.INFO, "Getting scorelist for simpleCluster.");
        List<Integer> scoreList = createScoreList(nodeTable);
        debugPrintScoreList(scoreList);
        monitor.showMessage(TaskMonitor.Level.INFO, "Done.");
        System.out.println("SimpleCluster finished."); // Find another way to log
    }

    private List<Integer> createScoreList(CyTable nodeTable) {
        List<Integer> scoreList = new ArrayList<>(this.clusters.size());
        System.out.println("scoreList size: " + this.clusters.size());
        for (int i = 0; i < this.clusters.size(); i++) {
            int score = 0;
            scoreList.add(i, 0);
            for (CyNode node : this.clusters.get(i)) {
                score += nodeTable.getRow(node.getSUID()).get(this.attribute, Integer.class, 0);
            }
            scoreList.set(i, scoreList.get(i) + score);
        }
        System.out.println("SimpleCluster is running."); // Find another way to log
        System.out.println("RESULTS:");
        Arrays.sort(scoreList.toArray());
        return scoreList;
    }

    private void debugPrintScoreList(List<Integer> scoreList) {
        for (int i = 0; i < scoreList.size(); i++) {
            System.out.println("ClusterScore <" + i + ">: " + scoreList.get(i));
        }
    }

    private boolean readyToGo(CyTable nodeTable, TaskMonitor monitor) {
        if (this.clusters.size() == 0) {
            monitor.showMessage(TaskMonitor.Level.INFO, "No clusters to work with");
            return false;
        } else if (this.attribute == null || this.attribute.equals("--None--")) {
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
        return SimpleCluster.isReady();
    }

    public static boolean isReady() {
        return network != null;
    }

    public void cancel() {
        this.canceled = true;
    }
}
