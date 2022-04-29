package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.HITS;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.units.NormalizationContext;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;


public class HITSContext {
    private CyNetwork network;
    public ClusterManager manager;

    @Tunable(description = "Alpha value", groups = "HITS teleport probability", gravity = 1.0)
    public double alpha = 0.1;

    @ContainsTunables
    public NormalizationContext normalizationContext;

    public HITSContext(ClusterManager manager) {
        this.manager = manager;
        network = this.manager.getNetwork();
        normalizationContext = new NormalizationContext(manager, network);
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
