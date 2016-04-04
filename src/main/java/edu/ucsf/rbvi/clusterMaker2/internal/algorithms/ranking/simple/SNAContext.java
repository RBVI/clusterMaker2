package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.simple;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import java.util.List;
import java.util.stream.Collectors;

public class SNAContext {
    private CyNetwork network;
    public ClusterManager manager;

    @Tunable(description = "Attributes", groups = "List of attributes to use", gravity = 10.0)
    public ListSingleSelection<String> attributes;

    @ContainsTunables
    public AdvancedProperties advancedAttributes;

    public SNAContext(ClusterManager manager) {
        System.out.println("SNAContext constructor");
        this.manager = manager;
        this.network = this.manager.getNetwork();
        this.advancedAttributes = new AdvancedProperties("__SCRank", false);
        this.attributes = new ListSingleSelection<>(getAttributes());
    }

    public List<String> getAttributes() {
        if (this.network != null) {
            return ModelUtils.updateNodeAttributeList(this.network, null).getPossibleValues();
        }
        return new ListSingleSelection<>(ModelUtils.NONEATTRIBUTE).getPossibleValues();
    }

    public List<String> getAlgorithms() {
        return this.manager.getAllAlgorithms().stream()
                .map(ClusterTaskFactory::getShortName).collect(Collectors.toList());
    }

    public String getSelectedAttribute() {
        return this.attributes.getSelectedValue();
    }

    public void setNetwork(CyNetwork network) {
        if (this.network != null && this.network.equals(network)) {
            return;
        }
        this.network = network;
    }

    public void updateContext() {
        this.attributes = new ListSingleSelection<>(getAttributes());
    }

    public CyNetwork getNetwork() {
        return this.network;
    }

    public String getClusterAttribute() {
        return advancedAttributes.getClusterAttribute();
    }
}
