package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.units;

import edu.uci.ics.jung.algorithms.scoring.HITS;
import org.cytoscape.model.CyEdge;

public class PREdge {
    private CyEdge edge;
    private Double score;
    private HITS.Scores scores;

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

    public Double getAuthScore() {
        return scores.authority;
    }

    public Double getHubScore() {
        return scores.hub;
    }

    public void setHitsScore(HITS.Scores scores) {
        this.scores = scores;
    }
}
