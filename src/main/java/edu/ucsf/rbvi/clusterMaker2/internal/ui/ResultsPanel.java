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
import java.awt.Image;
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
import javax.swing.table.TableCellRenderer;

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
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.AbstractClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;


public class ResultsPanel extends JPanel implements CytoPanelComponent{

	private static final long serialVersionUID = 868213052692609076L;
	
	protected ClusterManager clusterManager;
	public final List<NodeCluster> clusters;
	public final AbstractClusterResults clusterResults;
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
	
	private ClusterBrowserPanel clusterBrowserPanel;

	private String clusterType = null;
	
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
	
	
	/**
	 * Constructor for the Results Panel which displays the clusters in a
	 * browswer table and explore panels for each cluster.
	 * 
	 * @param clusters Found clusters from the algorithm used
	 * @param network Network were these clusters were found
	 */
	public ResultsPanel(final List<NodeCluster> clusters,
							 final AbstractClusterResults clusterResults,
							 final CyNetwork network,
							 final CyNetworkView networkView,
							 ClusterManager clusterManager,
							 TaskMonitor monitor) {
		//System.out.println("RP: Inside constructor");
		setLayout(new BorderLayout());
		
		this.clusterManager = clusterManager;
		this.clusters = clusters;
		this.clusterResults = clusterResults;
		this.network = network;
		this.networkView = networkView;
		this.monitor = monitor;
		visualStyleFactory = clusterManager.getService(VisualStyleFactory.class);
		applicationMgr = clusterManager.getService(CyApplicationManager.class);
		networkViewFactory = clusterManager.getService(CyNetworkViewFactory.class);
		visualMappingMgr = clusterManager.getService(VisualMappingManager.class);
		renderingEngineFactory = clusterManager.getService(RenderingEngineFactory.class);
		clusterType = network.getRow(network).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);
		//System.out.println("RP: after setting variables and fields");
		
		this.clusterBrowserPanel = new ClusterBrowserPanel(this);
		//System.out.println("RP: after creating new cluster browser panel");
		add(clusterBrowserPanel, BorderLayout.CENTER);
		//System.out.println("RP: after adding clusterBrowserPanel");
		this.setSize(this.getMinimumSize());
		//System.out.println("RP: after this.setSize");
		//
	}
	
	
	public String getTitle() {
		return clusterType+"("+network+")";
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
	

	private static StringBuilder getClusterScore(final NodeCluster cluster) {
		StringBuilder details = new StringBuilder();

		details.append("Score: ");
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		details.append(nf.format(cluster.getClusterScore()));
		
		return details;
	}
	
	
	/**
	 * Panel that contains the browser table with a scroll bar.
	 */
	private class ClusterBrowserPanel extends JPanel implements ListSelectionListener {

		private final RankingBrowserPanelModel browserModel;
		private final JTable table;
		private final ResultsPanel resultsPanel;

		public ClusterBrowserPanel(ResultsPanel component) {
			
			super();
			resultsPanel = component;

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

			JLabel summary = new JLabel("<html>"+clusterResults.toHTML()+"</html>");
			summary.setBorder(border);
			add(summary, BorderLayout.NORTH);
			
			//System.out.println("CBP: after setLayout n setBorder");
			// main data table
			browserModel = new RankingBrowserPanelModel();
			//System.out.println("CBP: after creating browser model");

			table = new JTable(browserModel);
			//System.out.println("CBP: after creating new JTable");
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			table.setAutoCreateRowSorter(true);
			table.setDefaultRenderer(StringBuffer.class, new ResultsPanel.JTextAreaRenderer(defaultRowHeight));
			table.setIntercellSpacing(new Dimension(0, 4)); // gives a little vertical room between clusters
			table.setFocusable(false); // removes an outline that appears when the user clicks on the images

			//System.out.println("CBP: after setting table params");

			// Ask to be notified of selection changes.
			ListSelectionModel rowSM = table.getSelectionModel();
			rowSM.addListSelectionListener(this);

			JScrollPane tableScrollPane = new JScrollPane(table);
			//System.out.println("CBP: after creating JScrollPane");
			tableScrollPane.getViewport().setBackground(Color.WHITE);

			add(tableScrollPane, BorderLayout.CENTER);
			//System.out.println("CBP: after adding JScrollPane");

			JButton dispose = new JButton("Remove Results");
			dispose.addActionListener(new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					CySwingApplication swingApplication = clusterManager.getService(CySwingApplication.class);
					CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);
					clusterManager.unregisterService(resultsPanel, CytoPanelComponent.class);
					clusterManager.removeResultsPanel(network, resultsPanel);
					if (cytoPanel.getCytoPanelComponentCount() == 0)
						cytoPanel.setState(CytoPanelState.HIDE);
				}
			});

			JPanel buttonPanel = new JPanel();
			buttonPanel.add(dispose);
			add(buttonPanel, BorderLayout.SOUTH);
		}

		public int getSelectedRow() {
			return table.getSelectedRow();
		}

		public void update(final ImageIcon image, final int row) {
			table.setValueAt(image, row, 0);
		}

		public void update(final NodeCluster cluster, final int row) {
			final StringBuilder score = getClusterScore(cluster);
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

		private final String[] columnNames = { "Network", "Score" };
		private final Object[][] data; // the actual table data

		public RankingBrowserPanelModel() {
			//System.out.println("CBTM: inside constructor");
			exploreContent = new JPanel[clusters.size()];
			data = new Object[clusters.size()][columnNames.length];
			//System.out.println("CBTM: after initialising exploreContent and data");

			for (int i = 0; i < clusters.size(); i++) {
				//System.out.println("CBTM: cluster num: "+ i);
				final NodeCluster c = clusters.get(i);
				//c.setRank(i);
				StringBuilder details = getClusterScore(c);
				data[i][1] = new StringBuffer(details);

				SpringEmbeddedLayouter layouter = new SpringEmbeddedLayouter();
				//System.out.println("CBTM: after invoking SpringEmbeddedLayouter");
				// get an image for each cluster - make it a nice layout of the cluster
				final Image image = createClusterImage(c, graphPicSize, graphPicSize, layouter, false);
				//System.out.println("CBTM: after createClusterImage");
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
			return data.length;
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
	 * Convert a network to an image.  This is used by the MCODEResultsPanel.
	 * 
	 * @param cluster Input network to convert to an image
	 * @param height  Height that the resulting image should be
	 * @param width   Width that the resulting image should be
	 * @param layouter Reference to the layout algorithm
	 * @param layoutNecessary Determinant of cluster size growth or shrinkage, the former requires layout
	 * @return The resulting image
	 */
	public Image createClusterImage(final NodeCluster cluster,
									final int height,
									final int width,
									SpringEmbeddedLayouter layouter,
									boolean layoutNecessary) {
		//System.out.println("CCI: inside method");
		final CyRootNetwork root =  clusterManager.getService(CyRootNetworkManager.class).getRootNetwork(network);
		//need to create a method get the subnetwork for a cluster
		final CyNetwork net = cluster.getSubNetwork(network, root, SavePolicy.DO_NOT_SAVE);
		
		//System.out.println("CCI: after getting root and network ");
		// Progress reporters.
		// There are three basic tasks, the progress of each is calculated and then combined
		// using the respective weighting to get an overall progress global progress
		int weightSetupNodes = 20; // setting up the nodes and edges is deemed as 25% of the whole task
		int weightSetupEdges = 5;
		double weightLayout = 75.0; // layout it is 70%
		double goalTotal = weightSetupNodes + weightSetupEdges;

		if (layoutNecessary) {
			goalTotal += weightLayout;
		}

		// keeps track of progress as a percent of the totalGoal
		double progress = 0;

		final VisualStyle vs = getClusterStyle();
		//System.out.println("CCI: after getClusterStyle");
		final CyNetworkView clusterView = createNetworkView(net, vs);
		//System.out.println("CCI: after createNetworkView");

		clusterView.setVisualProperty(NETWORK_WIDTH, new Double(width));
		clusterView.setVisualProperty(NETWORK_HEIGHT, new Double(height));

		for (View<CyNode> nv : clusterView.getNodeViews()) {
			if (interrupted) {
				//logger.debug("Interrupted: Node Setup");
				// before we short-circuit the method we reset the interruption so that the method can run without
				// problems the next time around
				if (layouter != null) layouter.resetDoLayout();
				resetLoading();

				return null;
			}

			// Node position
			final double x;
			final double y;

			// First we check if the MCODECluster already has a node view of this node (posing the more generic condition
			// first prevents the program from throwing a null pointer exception in the second condition)
			if (cluster.getView() != null && cluster.getView().getNodeView(nv.getModel()) != null) {
				//If it does, then we take the layout position that was already generated for it
				x = cluster.getView().getNodeView(nv.getModel()).getVisualProperty(NODE_X_LOCATION);
				y = cluster.getView().getNodeView(nv.getModel()).getVisualProperty(NODE_Y_LOCATION);
			} else {
				// Otherwise, randomize node positions before layout so that they don't all layout in a line
				// (so they don't fall into a local minimum for the SpringEmbedder)
				// If the SpringEmbedder implementation changes, this code may need to be removed
				// size is small for many default drawn graphs, thus +100
				x = (clusterView.getVisualProperty(NETWORK_WIDTH) + 100) * Math.random();
				y = (clusterView.getVisualProperty(NETWORK_HEIGHT) + 100) * Math.random();

				if (!layoutNecessary) {
					goalTotal += weightLayout;
					progress /= (goalTotal / (goalTotal - weightLayout));
					layoutNecessary = true;
				}
			}

			nv.setVisualProperty(NODE_X_LOCATION, x);
			nv.setVisualProperty(NODE_Y_LOCATION, y);

			//Might be specific to MCODE
			/*
			// Node shape
			if (cluster.getSeedNode() == nv.getModel().getSUID()) {
				nv.setLockedValue(NODE_SHAPE, NodeShapeVisualProperty.RECTANGLE);
			} else {
				nv.setLockedValue(NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
			}
			 */
			
			/*
			// Update loader
			if (loader != null) {
				progress += 100.0 * (1.0 / (double) clusterView.getNodeViews().size()) *
							((double) weightSetupNodes / (double) goalTotal);
				loader.setProgress((int) progress, "Setup: nodes");
			}
			
			*/
		}

		if (clusterView.getEdgeViews() != null) {
			for (int i = 0; i < clusterView.getEdgeViews().size(); i++) {
				if (interrupted) {
					//logger.error("Interrupted: Edge Setup");
					if (layouter != null) layouter.resetDoLayout();
					resetLoading();

					return null;
				}
				/*
				if (loader != null) {
					progress += 100.0 * (1.0 / (double) clusterView.getEdgeViews().size()) *
								((double) weightSetupEdges / (double) goalTotal);
					loader.setProgress((int) progress, "Setup: edges");
				}
				*/
			}
		}

		if (layoutNecessary) {
			if (layouter == null) {
				layouter = new SpringEmbeddedLayouter();
			}

			layouter.setGraphView(clusterView);

			// The doLayout method should return true if the process completes without interruption
			if (!layouter.doLayout(weightLayout, goalTotal, progress)) {
				// Otherwise, if layout is not completed, set the interruption to false, and return null, not an image
				resetLoading();

				return null;
			}
		}

		final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		SwingUtilities.invokeLater(new Runnable() {
			//@Override
			public void run() {
				try {
					final Dimension size = new Dimension(width, height);

					JPanel panel = new JPanel();
					panel.setPreferredSize(size);
					panel.setSize(size);
					panel.setMinimumSize(size);
					panel.setMaximumSize(size);
					panel.setBackground((Color) vs.getDefaultValue(NETWORK_BACKGROUND_PAINT));

					JWindow window = new JWindow();
					window.getContentPane().add(panel, BorderLayout.CENTER);

					RenderingEngine<CyNetwork> re = renderingEngineFactory.createRenderingEngine(panel, clusterView);

					vs.apply(clusterView);
					clusterView.fitContent();
					clusterView.updateView();
					window.pack();
					window.repaint();

					final Image tmpImage = re.createImage(width, height);
					final Graphics2D g = (Graphics2D) image.getGraphics();
					g.drawImage(tmpImage, 0, 0, null);

					if (clusterView.getNodeViews().size() > 0) {
						cluster.setView(clusterView);
					}
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		});

		layouter.resetDoLayout();
		resetLoading();

		return image;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public VisualStyle getClusterStyle() {
		if (clusterStyle == null) {
			clusterStyle = visualStyleFactory.createVisualStyle("Cluster");

			clusterStyle.setDefaultValue(NODE_SIZE, 40.0);
			clusterStyle.setDefaultValue(NODE_WIDTH, 40.0);
			clusterStyle.setDefaultValue(NODE_HEIGHT, 40.0);
			clusterStyle.setDefaultValue(NODE_PAINT, Color.RED);
			clusterStyle.setDefaultValue(NODE_FILL_COLOR, Color.RED);
			clusterStyle.setDefaultValue(NODE_BORDER_WIDTH, 0.0);

			clusterStyle.setDefaultValue(EDGE_WIDTH, 5.0);
			clusterStyle.setDefaultValue(EDGE_PAINT, Color.BLUE);
			clusterStyle.setDefaultValue(EDGE_UNSELECTED_PAINT, Color.BLUE);
			clusterStyle.setDefaultValue(EDGE_STROKE_UNSELECTED_PAINT, Color.BLUE);
			clusterStyle.setDefaultValue(EDGE_SELECTED_PAINT, Color.BLUE);
			clusterStyle.setDefaultValue(EDGE_STROKE_SELECTED_PAINT, Color.BLUE);
			clusterStyle.setDefaultValue(EDGE_TARGET_ARROW_SHAPE, NONE);
			clusterStyle.setDefaultValue(EDGE_SOURCE_ARROW_SHAPE, NONE);

			/*
			//System.out.println("GCS: before getVisual Lexicon");
			VisualLexicon lexicon = applicationMgr.getCurrentRenderingEngine().getVisualLexicon();
			VisualProperty vp = lexicon.lookup(CyEdge.class, "edgeTargetArrowShape");
			//System.out.println("CCI: after setting visual property");

			if (vp != null) {
				Object arrowValue = vp.parseSerializableString("ARROW");
				System.out.println("Edge target arrow value = "+arrowValue.toString());
				if (arrowValue != null) clusterStyle.setDefaultValue(vp, arrowValue);
			}
			*/
		}

		return clusterStyle;
	}

	public CyNetworkView createNetworkView(final CyNetwork net, VisualStyle vs) {
		final CyNetworkView view = networkViewFactory.createNetworkView(net);
		//System.out.println("inside createNetworkView");
		if (vs == null) vs = visualMappingMgr.getDefaultVisualStyle();
		visualMappingMgr.setVisualStyle(vs, view);
		vs.apply(view);
		view.updateView();

		return view;
	}
	
	/**
	 * A text area renderer that creates a line wrapped, non-editable text area
	 */
	public static class JTextAreaRenderer extends JTextArea implements TableCellRenderer {
		
		int minHeight;

		/**
		 * Constructor
		 * 
		 * @param minHeight
		 *            The minimum height of the row, either the size of the
		 *            graph picture or zero
		 */
		public JTextAreaRenderer(int minHeight) {
			//System.out.println("JTAR: inside constructor ");
			this.setLineWrap(true);
			this.setWrapStyleWord(true);
			this.setEditable(false);
			this.setFont(new Font(this.getFont().getFontName(), Font.PLAIN, 11));
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
			//System.out.println("JTAR: inside getTableCellRendererComponent");
			StringBuffer sb = (StringBuffer) value;
			this.setText(sb.toString());

			if (isSelected) {
				this.setBackground(table.getSelectionBackground());
				this.setForeground(table.getSelectionForeground());
			} else {
				this.setBackground(table.getBackground());
				this.setForeground(table.getForeground());
			}

			// Row height calculations
			int currentRowHeight = table.getRowHeight(row);
			int rowMargin = table.getRowMargin();
			this.setSize(table.getColumnModel().getColumn(column).getWidth(), currentRowHeight - (2 * rowMargin));
			int textAreaPreferredHeight = (int) this.getPreferredSize().getHeight();

			// JTextArea can grow and shrink here
			if (currentRowHeight != Math.max(textAreaPreferredHeight + (2 * rowMargin), minHeight + (2 * rowMargin))) {
				table.setRowHeight(row, Math
						.max(textAreaPreferredHeight + (2 * rowMargin), minHeight + (2 * rowMargin)));
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
