package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking;


import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleClusterContext {
    private CyNetwork network;
    public ClusterManager manager;

    @Tunable(description = "Algorithm", groups = "List of Algorithms", gravity = 1.0)
    public ListSingleSelection<String> algorithms;

    public SimpleClusterContext(ClusterManager manager) {
        System.out.println("SimpleClusterContext constructor: " + this.manager.toString());
        this.manager = manager;
        this.network = manager.getNetwork();
        algorithms = new ListSingleSelection<>(this.getAlgorithms());
    }

    public void setAlgorithms(List<String> algs) {
        this.algorithms = new ListSingleSelection<>(algs);
    }

    public List<String> getAlgorithms() {
        return this.manager.getAllAlgorithms().stream()
                .filter(alg -> alg.getTypeList().contains(ClusterTaskFactory.ClusterType.NETWORK))
                .map(ClusterTaskFactory::getShortName).collect(Collectors.toList());
    }

    public void setNetwork(CyNetwork network) {
        this.network = network;
    }
}
