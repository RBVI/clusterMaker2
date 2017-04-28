/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.pca;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.ui.ResultsPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import edu.ucsf.rbvi.clusterMaker2.internal.api.CyMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.api.Matrix;

/**
 *
 * @author root
 */
public class ResultPanelPCA extends JPanel implements ListSelectionListener, CytoPanelComponent {

	private final CyNetwork network;
	private CyNetworkView networkView;
	private final CyMatrix[] components;
	private final double[] varianceArray;

	// table size parameters
	private static final int graphPicSize = 80;
	private static final int defaultRowHeight = graphPicSize + 8;

	private List<Integer> nodeCount = new ArrayList<Integer>();

	private final JTable table;
	private final ResultPanelPCA.PCBrowserTableModel browserModel;

	public ResultPanelPCA(final CyMatrix[] components, 
		final double[] varianceArray,
		final CyNetwork network, 
		final CyNetworkView networkView){
		super();

		this.network = network;
		this.networkView = networkView;
		this.components = components;
		this.varianceArray = varianceArray;

		this.browserModel = new PCBrowserTableModel();
		this.table = new JTable(browserModel);

		setLayout(new BorderLayout());

		initComponents();
		this.setSize(this.getMinimumSize());
	}

	private void initComponents() {
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setAutoCreateRowSorter(true);
		table.setDefaultRenderer(StringBuffer.class, new ResultsPanel.JTextAreaRenderer(defaultRowHeight));
		table.setIntercellSpacing(new Dimension(0, 4)); // gives a little vertical room between clusters
		table.setFocusable(false); // removes an outline that appears when the user clicks on the images

		// Ask to be notified of selection changes.
		ListSelectionModel rowSM = table.getSelectionModel();
		rowSM.addListSelectionListener(this);

		JScrollPane tableScrollPane = new JScrollPane(table);
		//System.out.println("CBP: after creating JScrollPane");
		tableScrollPane.getViewport().setBackground(Color.WHITE);

		add(tableScrollPane, BorderLayout.CENTER);
		//System.out.println("CBP: after adding JScrollPane");
	}

	public int getSelectedRow() {
		return table.getSelectedRow();
	}

	public void update(final ImageIcon image, final int row) {
		table.setValueAt(image, row, 0);
	}

	public void update(final NodeCluster cluster, final int row) {
		final String score = "needs score here";
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
		double threshold = 0.02;
		// Get the clusters
		for (int index = 0; index < rowIndices.length; index++) {
			int row = rowIndices[index];
			CyMatrix matrix = components[row];
			for(int i=0;i<matrix.nRows();i++){
				for(int j=0;j<matrix.nColumns();j++){
					if(matrix.getValue(i, j) > threshold){
						CyNode node = matrix.getRowNode(i);
						selectedMap.put(node, node);
						break;
					}
				}
			}
			// System.out.println("PC is selected");
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

	//@Override
	public Component getComponent() {
		return this;
	}

	//@Override
	public String getTitle() {
		return "PCA for "+network;
	}

	//@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	//@Override
	public Icon getIcon() {
		return null;
	}

	private class PCBrowserTableModel extends AbstractTableModel {

		private final String[] columnNames = { "PC", "Description" };
		private final Object[][] data; // the actual table data

		public PCBrowserTableModel() {

			data = new Object[components.length][columnNames.length];

			for (int i = 0; i < components.length; i++) {

				final Image image = createPCImage(components[i], graphPicSize, graphPicSize);

				data[i][0] = image != null ? new ImageIcon(image) : new ImageIcon();

				String details = "Nodes: " + nodeCount.get(i) + "\n";
				details += "Variance: " + varianceArray[i] + "\n";
				data[i][1] = new StringBuffer(details);
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

	public Image createPCImage(CyMatrix pc, final int height, final int width){
		final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = (Graphics2D) image.getGraphics();

		double threshold = 0.02;
		double cx = networkView.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION);
		double cy = networkView.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION);
		List<Point> nodes = new ArrayList<Point>();
		// System.out.println("createPCImage: "+pc.printMatrixInfo());
		for(int i=0;i<pc.nRows();i++){
			for(int j=0;j<pc.nColumns();j++){
				// System.out.println("Value("+i+","+j+")="+pc.getValue(i,j));
				if(pc.getValue(i, j) > threshold){
					CyNode node = pc.getRowNode(i);
					View<CyNode> nodeView = networkView.getNodeView(node);
					Double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
					Double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
					//System.out.println("x: " + x + " y: " + y);
					double newx = x-cx;
					double newy = y-cy;
					nodes.add(new Point((int)newx, (int)newy));
					break;
				}
			}
		}

		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		for(Point p:nodes){
		if(maxX < p.x)
			maxX = p.x;
		if(maxY < p.y)
			maxY = p.y;
		if(minX > p.x)
			minX = p.x;
		if(minY > p.y)
			minY = p.y;
		}

		double xScale = (double) image.getWidth(this) / (maxX - minX);
		double yScale = (double) image.getHeight(this) / (maxY - minY);

		int newX = image.getWidth(this)/2;
		int newY = image.getHeight(this)/2;
		int count = 0;
		g.setColor(Color.BLACK);
		for(Point p:nodes){
		int x1 = (int) (p.x*xScale + newX);
		int y1 = (int) (-1*(p.y*yScale - newY));
		g.fillOval(x1, y1, 2, 2);
		count++;
		}
		nodeCount.add(count);
		return image;
	}
}
