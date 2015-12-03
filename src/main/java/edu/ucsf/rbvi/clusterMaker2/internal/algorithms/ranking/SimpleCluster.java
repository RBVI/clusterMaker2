package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import java.util.List;

public class SimpleCluster extends AbstractTask implements Rank {

    private List<List<CyNode>> clusters;
    private ClusterManager manager;
    private boolean canceled;
    public static String NAME = "Create rank from clusters";
    public static String SHORTNAME = "ranklust";
    public static String GROUP_ATTRIBUTE = SHORTNAME;

    @Tunable(description = "Network to look for cluster", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public SimpleClusterContext gui;

    public SimpleCluster(SimpleClusterContext gui, ClusterManager manager) {
        this.canceled = false;
        this.manager = manager;
        this.gui = gui;
        this.network = this.manager.getNetwork();

    }

    public String getShortName() {
        return SHORTNAME;
    }

    public String getName() {
        return NAME;
    }

    public Object getContext() {
        return this.gui;
    }

    public void run(TaskMonitor monitor) {
        monitor.setTitle("Running SimpleCluster...");

       /*
        Ensure here that we actually have a cluster to work with
         */

        if (network == null) {
            this.manager.getNetwork();
        }

        // Is this needed???
        this.gui.setNetwork(network);

        // Ugly abort
        if (this.canceled) {
            monitor.showMessage(TaskMonitor.Level.INFO, "Canceled");
            return;
        }

        // Start algorithm here
        monitor.showMessage(TaskMonitor.Level.INFO, "Running.");
        System.out.println("SimpleCluster is running."); // Find another way to log
        monitor.showMessage(TaskMonitor.Level.INFO, "Done.");
        System.out.println("SimpleCluster finished."); // Find another way to log
    }

    public boolean isAvailable() {
        return SimpleCluster.isReady(network);
    }

    private static boolean isReady(CyNetwork network) {
        return network != null;
    }

    public void cancel() {
        this.canceled = true;
    }
}
