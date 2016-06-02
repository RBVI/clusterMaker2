package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.advanced;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import java.util.List;

public class RWRContext {
    private CyNetwork network;
    public ClusterManager manager;

    @Tunable(description = "Node attributes", groups = "Biomarker information", gravity = 1.0)
    public ListMultipleSelection<String> nodeAttributes;

    @Tunable(description = "Edge attributes", groups = "Biomarker information", gravity = 10.0)
    public ListMultipleSelection<String> edgeAttributes;

    public RWRContext(ClusterManager manager) {
        this.manager = manager;
        network = this.manager.getNetwork();
        updateContext();
    }

    public void updateContext() {
        nodeAttributes = new ListMultipleSelection<>(updateNodeAttributes());
        edgeAttributes = new ListMultipleSelection<>(updateEdgeAttributes());
    }

    public List<String> updateNodeAttributes() {
        if (network != null) {
            return ModelUtils.updateNodeAttributeList(network, null).getPossibleValues();
        }

        return new ListMultipleSelection<>(ModelUtils.NONEATTRIBUTE).getSelectedValues();
    }

    public List<String> updateEdgeAttributes() {
        if (network != null) {
            return ModelUtils.updateEdgeAttributeList(network, null).getPossibleValues();
        }

        return new ListMultipleSelection<>(ModelUtils.NONEATTRIBUTE).getSelectedValues();
    }

    public List<String> getSelectedNodeAttributes() {
        return nodeAttributes.getSelectedValues();
    }

    public List<String> getSelectedEdgeAttributes() {
        return edgeAttributes.getSelectedValues();
    }

    public void setNetwork(CyNetwork network) {
        this.network = network;
    }

    public CyNetwork getNetwork() {
        return network;
    }
}
