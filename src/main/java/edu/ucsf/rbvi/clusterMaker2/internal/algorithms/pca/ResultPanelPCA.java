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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
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
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

/**
 *
 * @author root
 */
public class ResultPanelPCA extends JPanel{
    
        private final List<CyNode> nodeList;
        private final CyNetwork network;
        private final CyNetworkView networkView;
        private final ComputationMatrix[] components;

        // table size parameters
        private static final int graphPicSize = 80;
        private static final int defaultRowHeight = graphPicSize + 8;
        
        private final ResultPanelPCA.PCBrowserPanel pcBrowserPanel;
        private final List<Integer> nodeCount = new ArrayList<Integer>();
        private static double[] varianceArray;
        private final List<List<CyNode>> nodeListArray;
        private int lastSelectedPC = -1;
        
        private static JFrame frame;

        public ResultPanelPCA(final ComputationMatrix[] components,
                final List<CyNode> nodeList,
                final CyNetwork network, 
                final CyNetworkView networkView){

                this.nodeList = nodeList;
                this.network = network;
                this.networkView = networkView;
                this.components = components;
                nodeListArray = new ArrayList<List<CyNode>>();
                this.pcBrowserPanel = new PCBrowserPanel();
                add(pcBrowserPanel, BorderLayout.CENTER);
		this.setSize(this.getMinimumSize());
        }
    
        /**
	 * Panel that contains the browser table with a scroll bar.
	 */
	private class PCBrowserPanel extends JPanel implements ListSelectionListener {
		private final ResultPanelPCA.PCBrowserTableModel browserModel;
		private final JTable table;

		public PCBrowserPanel() {
			super();

			setLayout(new BorderLayout());

			// Create the summary panel
			String title = "Title Here";
			TitledBorder border = 
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title);
			border.setTitlePosition(TitledBorder.TOP);
			border.setTitleJustification(TitledBorder.LEFT);
			border.setTitleColor(Color.BLUE);

			JLabel summary = new JLabel("<html>"+"here is the summery"+"</html>");
			summary.setBorder(border);
			//add(summary, BorderLayout.NORTH);

			// main data table
			browserModel = new ResultPanelPCA.PCBrowserTableModel();

			table = new JTable(browserModel);
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			table.setAutoCreateRowSorter(true);
			table.setDefaultRenderer(StringBuffer.class, new ResultsPanel.JTextAreaRenderer(defaultRowHeight));
			table.setIntercellSpacing(new Dimension(0, 4)); // gives a little vertical room between clusters
			table.setFocusable(false); // removes an outline that appears when the user clicks on the images

			// Ask to be notified of selection changes.
			ListSelectionModel rowSM = table.getSelectionModel();
			rowSM.addListSelectionListener(this);

			JScrollPane tableScrollPane = new JScrollPane(table);
			tableScrollPane.getViewport().setBackground(Color.WHITE);

			add(tableScrollPane, BorderLayout.CENTER);

			JButton dispose = new JButton("Close");
			dispose.addActionListener(new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
                                    ResultPanelPCA.closeGui();
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
			final String score = "needs score here";
			table.setValueAt(score, row, 1);
		}

		JTable getTable() { 
			return table;
		}

		public void valueChanged(ListSelectionEvent e) {
                        if(lastSelectedPC != -1){
                            for(CyNode node: nodeListArray.get(lastSelectedPC)){
                                network.getRow(node).set(CyNetwork.SELECTED, false);
                            }
                        }
                        for(CyNode node: nodeListArray.get(e.getFirstIndex())){
                            network.getRow(node).set(CyNetwork.SELECTED, true);
                        }
                        lastSelectedPC = e.getFirstIndex();
                        networkView.updateView();
		}
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
    
        public Image createPCImage(ComputationMatrix pc, final int height, final int width){
            final Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g = (Graphics2D) image.getGraphics();
            
            double threshold = 0.02;
            double cx = networkView.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION);
            double cy = networkView.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION);
            List<Point> nodes = new ArrayList<Point>();
            List<CyNode> nodesList = new ArrayList<CyNode>();
            for(int i=0;i<pc.nRow();i++){
                for(int j=0;j<pc.nColumn();j++){
                    if(pc.getCell(i, j) > threshold){
                        Double x = networkView.getNodeView(network.getNodeList().get(i)).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
                        Double y = networkView.getNodeView(network.getNodeList().get(i)).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
                        //System.out.println("x: " + x + " y: " + y);
                        double newx = x-cx;
                        double newy = y-cy;
                        nodes.add(new Point((int)newx, (int)newy));
                        nodesList.add(this.nodeList.get(i));
                    }
                }
            }
            nodeListArray.add(nodesList);
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
    
        public static void createAndShowGui(final ComputationMatrix[] components,
                final List<CyNode> nodeList,
                final CyNetwork network, 
                final CyNetworkView networkView,
                final double[] varianceArray){
            ResultPanelPCA.varianceArray = varianceArray;
            ResultPanelPCA resultPanelPCA = new ResultPanelPCA(components, nodeList,network, networkView);
            frame = new JFrame("Result Panel");
            
            frame.getContentPane().add(resultPanelPCA);
            frame.pack();
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        }
        
        public static void closeGui(){
            if(frame != null)
                frame.dispose();
        }
}
