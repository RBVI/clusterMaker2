package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.SPICi;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithmContext;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.swing.TunableUIHelper;

public class SPICiContext implements ClusterAlgorithmContext {
    CyNetwork network;

    public CyNetwork getNetwork() {
        return null;
    }

    public void setNetwork(CyNetwork network) {
        if (this.network != null && this.network.equals(network)) {
            return;
        }

        this.network = network;
    }

    public void setUIHelper(TunableUIHelper helper) {
    }
}
