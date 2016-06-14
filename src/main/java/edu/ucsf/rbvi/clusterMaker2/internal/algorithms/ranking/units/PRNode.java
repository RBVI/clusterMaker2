package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.units;

import org.cytoscape.model.CyNode;

public class PRNode {
    private CyNode node;
    private Double score;

    public PRNode(CyNode node) {
        this.node = node;
        this.score = 0.0d;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public CyNode getCyNode() {
        return node;
    }
}
