package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import edu.ucsf.rbvi.clusterMaker2.internal.commands.GetNetworkClusterTask;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleCluster extends AbstractTask implements Rank {

    private GetNetworkClusterTask clusterMonitor;
    private List<List<CyNode>> clusters;
    private ClusterManager manager;
    private boolean canceled;
    public static String NAME = "Create rank from clusters";
    public static String SHORTNAME = "ranklust";
    public static String GROUP_ATTRIBUTE = SHORTNAME;

    @Tunable(description = "Network to look for cluster", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public SimpleClusterContext context;

    public SimpleCluster(SimpleClusterContext context, ClusterManager manager) {
        this.canceled = false;
        this.manager = manager;
        this.context = context;
        this.network = this.manager.getNetwork();
        this.clusterMonitor = new GetNetworkClusterTask(manager);

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

    public void run(TaskMonitor monitor) {
        monitor.setTitle("Running SimpleCluster...");

       /*
        * Ensure here that we actually have a cluster to work with
        */

        if (network == null) {
            this.manager.getNetwork();
        }

        // update the GUI
        this.context.setNetwork(network);

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

    public static boolean isReady(CyNetwork network) {
        return network != null;
    }

    public void cancel() {
        this.canceled = true;
    }
}
