package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AdvancedProperties;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.Tunable;

public class SimpleClusterContext {
    // Is this needed???
    CyNetwork network;

    @Tunable(description = "Testing", groups = "testgroup", gravity = 1.0)
    public String welcomeString = "WelcomeTest";

    @ContainsTunables
    public AdvancedProperties advancedProperties;

    public SimpleClusterContext() {
        System.out.println("Welcome string: " + this.welcomeString);
    }

    public SimpleClusterContext(SimpleClusterContext oldContext) {
        System.out.println("Welcome string: " + this.welcomeString);

        if (oldContext != null) {
            this.advancedProperties = new AdvancedProperties(oldContext.advancedProperties);
        } else {
            this.advancedProperties = new AdvancedProperties("__Ranking", false);
        }
    }

    // Is this needed???
    public void setNetwork(CyNetwork network) {
        this.network = network;
    }
}
