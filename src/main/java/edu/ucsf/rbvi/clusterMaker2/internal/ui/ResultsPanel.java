package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class ResultsPanel extends JPanel implements CytoPanelComponent{

	private static final long serialVersionUID = 868213052692609076L;
	
	public final List<NodeCluster> clusters;
	private final CyNetwork network;
	private CyNetworkView networkView;
	private final int resultId;
	
	// table size parameters
	private static final int graphPicSize = 80;
	private static final int defaultRowHeight = graphPicSize + 8;
	
	private JPanel[] exploreContent;
	
	public Component getComponent() {
		return this;
	}

	//@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	//@Override
	public Icon getIcon() {
		URL iconURL = MCODEResources.getUrl(ImageName.LOGO_SMALL);
		return new ImageIcon(iconURL);
	}
	
	
	/**
	 * Constructor for the Results Panel which displays the clusters in a
	 * browswer table and explore panels for each cluster.
	 * 
	 * @param clusters Found clusters from the algorithm used
	 * @param network Network were these clusters were found
	 * @param resultId Title of this result as determined by MCODESCoreAndFindAction
	 */
	public ResultsPanel(final List<NodeCluster> clusters,							 
							 final CyNetwork network,
							 final CyNetworkView networkView,
							 final int resultId) {
		setLayout(new BorderLayout());
		
		this.clusters = clusters;
		this.network = network;
		this.networkView = networkView;
		this.resultId = resultId;
	}
	
	
	public String getTitle() {
		return "Result " + getResultId();
	}
	
	public int getResultId() {
		return this.resultId;
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
	

	private static StringBuffer getClusterScore(final NodeCluster cluster) {
		StringBuffer details = new StringBuffer();

		details.append("Score: ");
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(3);
		details.append(nf.format(cluster.getClusterScore()));
		
		return details;
	}
	
	
	/**
	 * Panel that contains the browser table with a scroll bar.
	 */
	private class ClusterBrowserPanel extends JPanel {

		private final ResultsPanel.ClusterBrowserTableModel browserModel;
		private final JTable table;

		public ClusterBrowserPanel() {
			super();
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createTitledBorder("Cluster Browser"));

			// main data table
			browserModel = new ResultsPanel.ClusterBrowserTableModel();

			table = new JTable(browserModel);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setDefaultRenderer(StringBuffer.class, new ResultsPanel.JTextAreaRenderer(defaultRowHeight));
			table.setIntercellSpacing(new Dimension(0, 4)); // gives a little vertical room between clusters
			table.setFocusable(false); // removes an outline that appears when the user clicks on the images

			
			/*
			// Ask to be notified of selection changes.
			ListSelectionModel rowSM = table.getSelectionModel();
			rowSM.addListSelectionListener(new ResultsPanel.TableRowSelectionHandler());
			*/
			
			JScrollPane tableScrollPane = new JScrollPane(table);
			tableScrollPane.getViewport().setBackground(Color.WHITE);

			add(tableScrollPane, BorderLayout.CENTER);
		}

		public int getSelectedRow() {
			return table.getSelectedRow();
		}

		public void update(final ImageIcon image, final int row) {
			table.setValueAt(image, row, 0);
		}

		public void update(final NodeCluster cluster, final int row) {
			final StringBuffer score = getClusterScore(cluster);
			table.setValueAt(score, row, 1);
		}

		JTable getTable() { 
			return table;
		}
	}
	
	/**
	 * Handles the data to be displayed in the cluster browser table
	 */
	private class ClusterBrowserTableModel extends AbstractTableModel {

		private final String[] columnNames = { "Network", "Score" };
		private final Object[][] data; // the actual table data

		public ClusterBrowserTableModel() {
			exploreContent = new JPanel[clusters.size()];
			data = new Object[clusters.size()][columnNames.length];

			for (int i = 0; i < clusters.size(); i++) {
				final NodeCluster c = clusters.get(i);
				//c.setRank(i);
				StringBuffer details = getClusterScore(c);
				data[i][1] = new StringBuffer(details);

				// get an image for each cluster - make it a nice layout of the cluster
				final Image image = c.getImage();
				data[i][0] = image != null ? new ImageIcon(image) : new ImageIcon();
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
	 * A text area renderer that creates a line wrapped, non-editable text area
	 */
	private static class JTextAreaRenderer extends JTextArea implements TableCellRenderer {

		int minHeight;

		/**
		 * Constructor
		 * 
		 * @param minHeight
		 *            The minimum height of the row, either the size of the
		 *            graph picture or zero
		 */
		public JTextAreaRenderer(int minHeight) {
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
	
}
