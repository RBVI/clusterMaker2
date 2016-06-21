package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.ranking.units;

import edu.uci.ics.jung.algorithms.scoring.HITS;
import org.cytoscape.model.CyNode;

public class PRNode {
    private CyNode node;
    private Double score;
    private Double prScore;
    private HITS.Scores scores;

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

    public void setPRScore(Double score) {
        this.prScore = score;
    }

    public Double getPRScore() {
        return prScore;
    }

    public CyNode getCyNode() {
        return node;
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
