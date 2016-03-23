package edu.ucsf.rbvi.clusterMaker2.internal.ui;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RankingResults extends JPanel implements CytoPanelComponent {

    public final List<NodeCluster> clusters;
    private final TaskMonitor monitor;
    private final CyNetwork network;

    public RankingResults(final List<NodeCluster> clusters, 
            TaskMonitor monitor, CyNetwork network) {
        this.clusters = clusters;
        this.monitor = monitor;
        this.network = network;

    }

    private void display() {
        JLabel resPanel = new JLabel("This is my Rankig results panel");
        this.add(resPanel);
        this.setVisible(true);
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public CytoPanelName getCytoPanelName() {
        return CytoPanelName.EAST;
    }

    @Override
    public String getTitle() {
        return "RankingResults";
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}
