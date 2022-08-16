package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import static org.cytoscape.view.presentation.property.ArrowShapeVisualProperty.NONE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.UIManager;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.SpringEmbeddedLayouter;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ViewUtils;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.swing.CyColorPaletteChooser;
import org.cytoscape.util.swing.CyColorPaletteChooserFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.work.TaskMonitor;

public class RankingPanel extends JPanel implements CytoPanelComponent{

    private static final long serialVersionUID = 868213052692609076L;

    protected ClusterManager clusterManager;
    public final List<NodeCluster> clusters;
    private final CyNetwork network;
    private CyNetworkView networkView;
    private final CyApplicationManager applicationMgr;
    private final TaskMonitor monitor;

    // table size parameters
    private static final int graphPicSize = 80;
    private static final int defaultRowHeight = graphPicSize + 8;
    private final VisualStyleFactory visualStyleFactory;
    private final CyNetworkViewFactory networkViewFactory;
    private final VisualMappingManager visualMappingMgr;

    private final RenderingEngineFactory<CyNetwork> renderingEngineFactory;

    private JPanel[] exploreContent;

    private VisualStyle clusterStyle;

    private boolean interrupted;

    private RankingBrowserPanel rankingBrowserPanel;

    private String clusterType = null;
    private String rankingType = null;

    public Component getComponent() {
        return this;
    }

    //@Override
    public CytoPanelName getCytoPanelName() {
        return CytoPanelName.EAST;
    }


    //@Override
    public Icon getIcon() {
        return null;
    }

    public RankingPanel(final List<NodeCluster> clusters,
                        final CyNetwork network,
                        final CyNetworkView networkView,
                        ClusterManager clusterManager,
                        TaskMonitor monitor) {
        setLayout(new BorderLayout());

        this.clusterManager = clusterManager;
        this.clusters = clusters;
        this.network = network;
        this.networkView = networkView;
        this.monitor = monitor;
        visualStyleFactory = clusterManager.getService(VisualStyleFactory.class);
        applicationMgr = clusterManager.getService(CyApplicationManager.class);
        networkViewFactory = clusterManager.getService(CyNetworkViewFactory.class);
        visualMappingMgr = clusterManager.getService(VisualMappingManager.class);
        renderingEngineFactory = clusterManager.getService(RenderingEngineFactory.class);
        clusterType = network.getRow(network).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);
        rankingType = network.getRow(network).get(ClusterManager.RANKING_ATTRIBUTE, String.class);

        this.rankingBrowserPanel = new RankingBrowserPanel(this);
        add(rankingBrowserPanel, BorderLayout.CENTER);
        this.setSize(this.getMinimumSize());
    }


    public String getTitle() {
        return "[" + clusterType + "]{" + rankingType + "}("+network+")";
    }


    public CyNetworkView getNetworkView() {
        return networkView;
    }

    public List<NodeCluster> getClusters() {
        return clusters;
    }

    public CyNetwork getNetwork() {
        return network;
    }


    private static Integer getClusterNumber(final NodeCluster cluster) {
      int clusterNumber = cluster.getClusterNumber();
      return clusterNumber;
    }

    private static StringBuilder getRankScore(final NodeCluster cluster) {
        StringBuilder details = new StringBuilder();

        // details.append("Score: ");
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        details.append(nf.format(cluster.getRankScore()));

        return details;
    }


    /**
     * Panel that contains the browser table with a scroll bar.
     */
    private class RankingBrowserPanel extends JPanel implements ListSelectionListener {

        private final RankingPanel.RankingBrowserPanelModel browserModel;
        private final JTable table;
        private final RankingPanel rankingPanel;

        public RankingBrowserPanel(RankingPanel component) {

            super();
            rankingPanel = component;

            //System.out.println("CBP: inside constructor, after super()");
            setLayout(new BorderLayout());
            // setBorder(BorderFactory.createTitledBorder("Cluster Browser"));

            // Create the summary panel
            String title = clusterType+" cluster summary for "+ModelUtils.getNetworkName(network);
            TitledBorder border =
                    BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title);
            border.setTitlePosition(TitledBorder.TOP);
            border.setTitleJustification(TitledBorder.LEFT);
            border.setTitleColor(Color.BLUE);

            browserModel = new RankingPanel.RankingBrowserPanelModel();

            table = new JTable(browserModel);
            table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            table.setAutoCreateRowSorter(true);
            // table.setDefaultRenderer(StringBuffer.class, new ResultsPanel.JTextAreaRenderer(defaultRowHeight));
            table.setDefaultRenderer(ImageIcon.class, new ImageRenderer(defaultRowHeight));
            table.setIntercellSpacing(new Dimension(0, 4)); // gives a little vertical room between clusters
            table.setFocusable(false); // removes an outline that appears when the user clicks on the images
                                       //
            // DefaultTableCellRenderer columnRenderer = new DefaultTableCellRenderer();
            // columnRenderer.setHorizontalAlignment(JLabel.CENTER);
            table.getTableHeader().setDefaultRenderer(new HeaderRenderer());
            // DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer();
            // headerRenderer.setFont(new Font("sans-serif", Font.BOLD, 10));
            // headerRenderer.setHorizontalAlignment(JLabel.CENTER);
            

            table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
            table.getColumnModel().getColumn(0).setPreferredWidth(80);

            //System.out.println("CBP: after setting table params");

            // Ask to be notified of selection changes.
            ListSelectionModel rowSM = table.getSelectionModel();
            rowSM.addListSelectionListener(this);

            JScrollPane tableScrollPane = new JScrollPane(table);
            //System.out.println("CBP: after creating JScrollPane");
            tableScrollPane.getViewport().setBackground(Color.WHITE);

            add(tableScrollPane, BorderLayout.CENTER);
            //System.out.println("CBP: after adding JScrollPane");

            Font buttonFont = new Font("sans-serif", Font.BOLD, 10);
            JButton dispose = new JButton("Remove Results");
            dispose.setFont(buttonFont);
            dispose.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    CySwingApplication swingApplication = clusterManager.getService(CySwingApplication.class);
                    CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);
                    clusterManager.unregisterService(rankingPanel, CytoPanelComponent.class);
                    clusterManager.removeRankingPanel(network, rankingPanel);
                    if (cytoPanel.getCytoPanelComponentCount() == 0)
                        cytoPanel.setState(CytoPanelState.HIDE);
                }
            });

            JButton color = new JButton("Color nodes by rank");
            color.setFont(buttonFont);
            color.addActionListener(new AbstractAction() {
                boolean undo = false;
                VisualStyle sourceStyle = null;
                VisualStyle rankingStyle = null;
                @Override
                public void actionPerformed(ActionEvent e) {
                  if (!undo) {
                    CyColorPaletteChooserFactory chooserFactory = clusterManager.getService(CyColorPaletteChooserFactory.class);
                    CyColorPaletteChooser paletteChooser = chooserFactory.getColorPaletteChooser(BrewerType.SEQUENTIAL, true);
                    Palette colorPalette = paletteChooser.showDialog(null, "Choose color palette for node fill", null, 9);

                    double[] pivots = new double[]{0.0,0.5,1.0};

                    sourceStyle = ViewUtils.getCurrentVisualStyle(clusterManager);
                    rankingStyle = ViewUtils.createFillStyle(clusterManager, sourceStyle, "_ranking", rankingType, colorPalette,pivots);
                    ViewUtils.setVisualStyle(clusterManager, clusterManager.getNetworkView(), rankingStyle);
                    color.setText("Remove ranking color");
                    undo = true;
                  } else {
                    ViewUtils.setVisualStyle(clusterManager, clusterManager.getNetworkView(), sourceStyle);
                    clusterManager.getService(VisualMappingManager.class).removeVisualStyle(rankingStyle);
                    rankingStyle = null;

                    color.setText("Color nodes by rank");
                    undo = false;
                  }
                }
            });

            JPanel buttonPanel = new JPanel(new GridLayout(2,1));
            buttonPanel.add(dispose);
            buttonPanel.add(color);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        public int getSelectedRow() {
            return table.getSelectedRow();
        }

        public void update(final ImageIcon image, final int row) {
            table.setValueAt(image, row, 0);
        }

        public void update(final NodeCluster cluster, final int row) {
            final StringBuilder score = getRankScore(cluster);
            table.setValueAt(score, row, 1);
        }

        JTable getTable() {
            return table;
        }

        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            // Get the rows
            int[] rowIndices = table.getSelectedRows();
            Map<CyNode, CyNode> selectedMap = new HashMap<CyNode, CyNode>();
            // Get the clusters
            for (int i = 0; i < rowIndices.length; i++) {
                ClusterImageIcon cii = (ClusterImageIcon)table.getValueAt(rowIndices[i], 0);
                if (cii.getNodeCluster() != null) {
                    for (CyNode node: cii.getNodeCluster()) {
                        selectedMap.put(node, node);
                    }
                }
            }
            // Select the nodes
            for (CyNode node: network.getNodeList()) {
                if (selectedMap.containsKey(node))
                    network.getRow(node).set(CyNetwork.SELECTED, true);
                else
                    network.getRow(node).set(CyNetwork.SELECTED, false);
            }

            // I wish we didn't need to do this, but if we don't, the selection
            // doesn't update
            networkView.updateView();
        }
    }

    /**
     * Handles the data to be displayed in the cluster browser table
     */
    private class RankingBrowserPanelModel extends AbstractTableModel {

        private final String[] columnNames = { "Network", "Cluster", "Score" };
        private final Object[][] data; // the actual table data
        private int rowCount = 0;

        public RankingBrowserPanelModel() {
            //System.out.println("CBTM: inside constructor");
            exploreContent = new JPanel[clusters.size()];
            data = new Object[clusters.size()][columnNames.length];
            //System.out.println("CBTM: after initialising exploreContent and data");

            VisualStyle vs = ViewUtils.getClusterStyle(clusterManager, null);

            SpringEmbeddedLayouter layouter = new SpringEmbeddedLayouter();

            for (int i = 0; i < clusters.size(); i++) {
                // System.out.println("CBTM: cluster num: "+ i);
                final NodeCluster c = clusters.get(i);
                if (c.getRankScore() < 0.1)
                  continue;
                //c.setRank(i);
                rowCount++;
                data[i][1] = getClusterNumber(c);
                // StringBuilder details = getRankScore(c);
                // data[i][2] = new StringBuffer(details);
                data[i][2] = c.getRankScore();

                // System.out.println("CBTM: after invoking SpringEmbeddedLayouter");
                // get an image for each cluster - make it a nice layout of the cluster
                final Image image = ViewUtils.createClusterImage(clusterManager, vs, network, c, graphPicSize, graphPicSize, layouter, false);
                // System.out.println("CBTM: after createClusterImage");
                data[i][0] = image != null ? new ClusterImageIcon(image, c) : new ClusterImageIcon();
            }
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return rowCount;
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        @Override
        public void setValueAt(Object object, int row, int col) {
            data[row][col] = object;
            fireTableCellUpdated(row, col);
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
    }

    /**
     * Header renderer
     */
    public static class HeaderRenderer extends DefaultTableCellRenderer {
      public HeaderRenderer() {
        super();
        setHorizontalAlignment(JLabel.CENTER);
        setFont(new Font("sans-serif", Font.BOLD, 10));
      }

      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
        c.setFont(new Font("sans-serif", Font.BOLD, 10));
        return c;
      }
    }

    /**
     * An image renderer that creates a reasonable row height
     */
    public static class ImageRenderer extends JLabel implements TableCellRenderer {

        int minHeight;

        public ImageRenderer(int minHeight) {
          super();
          this.minHeight = minHeight;
        }

        /**
         * Used to render a table cell. Handles selection color and cell height
         * and width. Note: Be careful changing this code as there could easily
         * be infinite loops created when calculating preferred cell size as the
         * user changes the dialog box size.
         *
         * @param table Parent table of cell
         * @param value Value of cell
         * @param isSelected True if cell is selected
         * @param hasFocus True if cell has focus
         * @param row The row of this cell
         * @param column The column of this cell
         * @return The cell to render by the calling code
         */
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            this.setIcon((Icon)value);

            if (isSelected) {
                this.setBackground(table.getSelectionBackground());
                this.setForeground(table.getSelectionForeground());
            } else {
                this.setBackground(table.getBackground());
                this.setForeground(table.getForeground());
            }

            // Row height calculations
            int iconPreferredHeight = (int) this.getPreferredSize().getHeight();
            int iconPreferredWidth = (int) this.getPreferredSize().getWidth();
            int currentRowHeight = table.getRowHeight(row);
            int rowMargin = table.getRowMargin();
            this.setSize(iconPreferredWidth, currentRowHeight - (2 * rowMargin));

            // JTextArea can grow and shrink here
            if (currentRowHeight != Math.max(iconPreferredHeight + (2 * rowMargin), minHeight + (2 * rowMargin))) {
                table.setRowHeight(row, Math
                        .max(iconPreferredHeight + (2 * rowMargin), minHeight + (2 * rowMargin)));
            }

            return this;
        }
    }

    private class ClusterImageIcon extends ImageIcon implements Comparable<ClusterImageIcon> {
        protected NodeCluster nodeCluster;
        static final long serialVersionUID = 1L;


        public ClusterImageIcon() {
            super();
            nodeCluster = null;
        }

        public ClusterImageIcon(Image image, NodeCluster c) {
            super(image);
            nodeCluster = c;
        }

        public int compareTo(ClusterImageIcon cii2) {
            if ((nodeCluster == null && cii2.nodeCluster == null) ||
                    (nodeCluster.size() == cii2.nodeCluster.size()))
                return 0;
            else if (nodeCluster == null || nodeCluster.size() < cii2.nodeCluster.size())
                return -1;
            return 1;
        }

        public NodeCluster getNodeCluster() {
            return nodeCluster;
        }
    }

    public void interruptLoading() {
        interrupted = true;
    }

    public void resetLoading() {
        interrupted = false;
    }

}

