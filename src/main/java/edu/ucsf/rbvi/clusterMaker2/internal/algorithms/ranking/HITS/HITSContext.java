package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.HITS;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import java.util.List;
import java.util.stream.Collectors;

public class HITSContext {
    private CyNetwork network;
    public ClusterManager manager;

    @Tunable(description = "Node attributes", groups = "Biomarker information", gravity = 10.0)
    public ListMultipleSelection<String> nodeAttributes;

    @Tunable(description = "Edge attributes", groups = "Biomarker information", gravity = 20.0)
    public ListMultipleSelection<String> edgeAttributes;

    @Tunable(description = "Alpha value", groups = "PageRank factors", gravity = 1.0)
    public double alpha = 0.1;

    public HITSContext(ClusterManager manager) {
        this.manager = manager;
        network = this.manager.getNetwork();
        updateContext();
    }

    public void updateContext() {
        nodeAttributes = new ListMultipleSelection<>(updateNodeAttributes());
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

    private List<String> updateNodeAttributes() {
        List<String> nodeAttrs;
        if (this.network != null) {
            nodeAttrs = ModelUtils.updateNodeAttributeList(network, null).getPossibleValues()
                    .stream()
                    .filter(attribute -> !attribute.equals(getClusterColumnName()))
                    .collect(Collectors.toList());
        } else {
            nodeAttrs = new ListMultipleSelection<>(ModelUtils.NONEATTRIBUTE).getPossibleValues();
        }

        return nodeAttrs;
    }

    public List<String> getSelectedNodeAttributes() {
        return nodeAttributes.getSelectedValues();
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
