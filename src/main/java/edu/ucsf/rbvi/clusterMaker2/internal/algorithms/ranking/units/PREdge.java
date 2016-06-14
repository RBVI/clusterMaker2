package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.units;

import org.cytoscape.model.CyEdge;

public class PREdge {
    private CyEdge edge;
    private Double score;

    public PREdge(CyEdge edge) {
        this.edge = edge;
        this.score = 0.0d;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public CyEdge getCyEdge() {
        return edge;
    }
}
