package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking;


import org.cytoscape.work.Tunable;

public class SimpleClusterContext {

    @Tunable(description = "Testing", groups = "testgroup", gravity = 1.0)
    public String welcomeString = "WelcomeTest";

    public SimpleClusterContext() {
        System.out.println("Welcome string: " + this.welcomeString);
    }
}
