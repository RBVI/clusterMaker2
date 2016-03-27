package edu.ucsf.rbvi.clusterMaker2.internal.ui;


import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import org.cytoscape.application.swing.*;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.List;

public class RankingResults extends JPanel implements CytoPanelComponent {

    public final List<NodeCluster> clusters;
    private final TaskMonitor monitor;
    private final CyNetwork network;
    private final RankingBrowserPanel rankingBrowserPanel;
    private ClusterManager manager;

    public RankingResults(final List<NodeCluster> clusters, TaskMonitor monitor, CyNetwork network,
                          ClusterManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout());

        this.clusters = clusters;
        this.monitor = monitor;
        this.network = network;

        this.rankingBrowserPanel = new RankingBrowserPanel(this);
        add(rankingBrowserPanel, BorderLayout.CENTER);
        setSize(getMinimumSize());
    }

    private void display() {
        JLabel resPanel = new JLabel("This is my Ranking results panel");
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

    private static StringBuffer getRankScore(final NodeCluster cluster) {
        StringBuffer rankScore = new StringBuffer();

        rankScore.append("Score: ");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        rankScore.append(nf.format(cluster.getRankScore()));

        return rankScore;
    }

    private class RankingBrowserPanel extends JPanel implements ListSelectionListener {

        private final JTable table;
        private final RankingResults rankingPanel;
        private final TableModel tableModel;

        private RankingBrowserPanel(RankingResults resultsPanel) {
            super();
            this.rankingPanel = resultsPanel;

            setLayout(new BorderLayout());

            String title = "Cluster ranking summary for " + ModelUtils.getNetworkName(network);
            TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title);
            border.setTitlePosition(TitledBorder.TOP);
            border.setTitleJustification(TitledBorder.LEFT);
            border.setTitleColor(Color.BLUE);

            tableModel = null;
            table = new JTable(null);
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.setAutoCreateRowSorter(true);
            table.setIntercellSpacing(new Dimension(0, 4));
            table.setFocusable(false); // cosmetic reasons...

            ListSelectionModel rowSelectionModel = table.getSelectionModel();
            rowSelectionModel.addListSelectionListener(this);

            JScrollPane tableScrollPanel = new JScrollPane(table);
            tableScrollPanel.getViewport().setBackground(Color.WHITE);

            add(tableScrollPanel, BorderLayout.CENTER);

            JButton removeResultsButton = new JButton("Remove results");
            removeResultsButton.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    CySwingApplication application = manager.getService(CySwingApplication.class);
                    CytoPanel cytoPanel = application.getCytoPanel(CytoPanelName.EAST);

                    manager.unregisterService(rankingPanel, CytoPanelComponent.class);
                    manager.removeRankingResults(network, rankingPanel);

                    if (cytoPanel.getCytoPanelComponentCount() == 0) {
                        cytoPanel.setState(CytoPanelState.HIDE);
                    }
                }
            });

            JPanel bottomButtonPanel = new JPanel();
            bottomButtonPanel.add(removeResultsButton);
            add(bottomButtonPanel, BorderLayout.SOUTH);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {

        }
    }
}
