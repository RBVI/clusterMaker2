package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.PR;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import java.util.List;
import java.util.stream.Collectors;

public class PRContext {
    private CyNetwork network;
    public ClusterManager manager;

    @Tunable(description = "Edge attributes", groups = "Biomarker information", gravity = 20.0)
    public ListMultipleSelection<String> edgeAttributes;

    @Tunable(description = "Alpha value", groups = "PR factors", gravity = 1.0)
    public double alpha = 0.1;

    @Tunable(description = "Iterations", groups = "PR factors", gravity = 10.0)
    public int iterations = 1000;

    public PRContext(ClusterManager manager) {
        this.manager = manager;
        network = this.manager.getNetwork();
        updateContext();
    }

    public void updateContext() {
        edgeAttributes = new ListMultipleSelection<>(updateEdgeAttributes());
    }

    private List<String> updateEdgeAttributes() {
        List<String> edgeAttrs;
        if (this.network != null) {
            edgeAttrs = ModelUtils.updateEdgeMultiAttributeList(network, null).getPossibleValues()
                    .stream()
                    .filter(attribute -> !attribute.equals(getClusterColumnName()))
                    .collect(Collectors.toList());
        } else {
            edgeAttrs = new ListMultipleSelection<>(ModelUtils.NONEATTRIBUTE).getPossibleValues();
        }

        return edgeAttrs;
    }

    public List<String> getSelectedEdgeAttributes() {
        return edgeAttributes.getSelectedValues();
    }

    public String getClusterColumnName() {
        return this.network.getRow(network).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class, "");
    }

    public void setNetwork(CyNetwork network) {
        this.network = network;
    }

    public CyNetwork getNetwork() {
        return network;
    }

    public double getAlpha() {
        return alpha;
    }
}
