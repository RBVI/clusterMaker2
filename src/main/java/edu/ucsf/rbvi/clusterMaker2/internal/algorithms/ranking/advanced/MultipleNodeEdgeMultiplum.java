package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.advanced;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Rank;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ClusterUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import java.util.ArrayList;
import java.util.List;

public class MultipleNodeEdgeMultiplum extends AbstractTask implements Rank {
    private List<NodeCluster> clusters;
    private ClusterManager manager;
    private List<String> nodeAttributes;
    private List<String> edgeAttributes;
    final public static String NAME = "Create rank from multiple nodes and edges (multiply sum)";
    final public static String SHORTNAME = "MNEMrank";

    @Tunable(description = "Network", context = "nogui")
    public CyNetwork network;

    @ContainsTunables
    public MNEMContext context;

    public MultipleNodeEdgeMultiplum(MNEMContext context, ClusterManager manager) {
        this.context = context;
        this.manager = manager;

        if (network == null) {
            network = this.manager.getNetwork();
        }

        this.context.setNetwork(network);
        this.context.updateContext();
    }

    @Override
    public String getShortName() {
        return SHORTNAME;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object getContext() {
        return context;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        taskMonitor.setProgress(0.0);
        taskMonitor.setTitle("Multiple Node Edge Multiplum ranking of clusters");
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Creating clusters...");
        taskMonitor.setProgress(10.0);
        clusters = ClusterUtils.createClusters(network);
        taskMonitor.setProgress(50.0);

        nodeAttributes = context.getSelectedNodeAttributes();
        edgeAttributes = context.getSelectedEdgeAttributes();
        List<Double> scoreList = new ArrayList<>();

        for (String nodeAttr : nodeAttributes) {
            addNodeScoreToColumn(nodeAttr, taskMonitor, scoreList);
        }

        taskMonitor.setProgress(75.0);

        for (String edgeAttr : edgeAttributes) {
            addEdgeScoreToColumn(edgeAttr, taskMonitor, scoreList);
        }

        taskMonitor.setProgress(100.0);

        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Done...");
    }

    private void addNodeScoreToColumn(String nodeAttr, TaskMonitor taskMonitor, List<Double> scoreList) {

    }

    private void addEdgeScoreToColumn(String edgeAttr, TaskMonitor taskMonitor, List<Double> scoreList) {

    }

    public static boolean isReady(CyNetwork network, ClusterManager manager) {
        return true;
    }
}
