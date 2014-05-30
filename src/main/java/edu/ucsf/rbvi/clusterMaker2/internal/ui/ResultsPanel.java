package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

public class ResultsPanel extends JPanel implements CytoPanelComponent{

	private final Algorithm alg;
	private final int resultId;
	private final List<NodeCluster> clusters;
	private final CyNetwork network;
	private CyNetworkView networkView;
	private MCODEDiscardResultAction discardResultAction;
	
	private MCODECollapsiblePanel explorePanel;
	private JPanel[] exploreContent;
	private JButton closeButton;

	
	/**
	 * Constructor for the Results Panel which displays the clusters in a
	 * browswer table and explore panels for each cluster.
	 * 
	 * @param clusters Found clusters from the algorithm used
	 * @param alg A reference to the alg for this particular network
	 * @param network Network were these clusters were found
	 * @param clusterImages A list of images of the found clusters
	 * @param resultId Title of this result as determined by MCODESCoreAndFindAction
	 */
	public ResultsPanel(final List<NodeCluster> clusters,
							 final Algorithm alg,
							 final MCODEUtil mcodeUtil,
							 final CyNetwork network,
							 final CyNetworkView networkView,
							 final int resultId,
							 final MCODEDiscardResultAction discardResultAction) {
		setLayout(new BorderLayout());

		this.alg = alg;
		this.mcodeUtil = mcodeUtil;
		this.resultId = resultId;
		this.clusters = Collections.synchronizedList(clusters);
		this.network = network;
		// The view may not exist, but we only test for that when we need to (in the TableRowSelectionHandler below)
		this.networkView = networkView;
		this.discardResultAction = discardResultAction;
		this.currentParamsCopy = mcodeUtil.getCurrentParameters().getResultParams(resultId);

		this.clusterBrowserPanel = new MCODEClusterBrowserPanel();
		add(clusterBrowserPanel, BorderLayout.CENTER);
		add(createBottomPanel(), BorderLayout.SOUTH);

		this.setSize(this.getMinimumSize());
	}
	
	//@Override
	public Component getComponent() {
		return this;
	}

	//@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}
	
	//@Override
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
	
	public int getSelectedClusterRow() {
		return clusterBrowserPanel.getSelectedRow();
	}
	
	public void discard(final boolean requestUserConfirmation) {
		SwingUtilities.invokeLater(new Runnable() {

			//@Override
			public void run() {
				boolean oldRequestUserConfirmation = Boolean.valueOf(discardResultAction
						.getValue(MCODEDiscardResultAction.REQUEST_USER_CONFIRMATION_COMMAND).toString());

				discardResultAction.putValue(MCODEDiscardResultAction.REQUEST_USER_CONFIRMATION_COMMAND,
											 requestUserConfirmation);
				closeButton.doClick();
				discardResultAction.putValue(MCODEDiscardResultAction.REQUEST_USER_CONFIRMATION_COMMAND,
											 oldRequestUserConfirmation);
			}
		});
	}
	
	
	/**
	 * Creates a panel containing the explore collapsable panel and result set
	 * specific buttons
	 * 
	 * @return Panel containing the explore cluster collapsable panel and button
	 *         panel
	 */
	private JPanel createBottomPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		explorePanel = new MCODECollapsiblePanel("Explore");
		explorePanel.setCollapsed(false);
		explorePanel.setVisible(false);

		JPanel buttonPanel = new JPanel();

		// The Export button
		JButton exportButton = new JButton("Export");
		exportButton.addActionListener(new MCODEResultsPanel.ExportAction());
		exportButton.setToolTipText("Export result set to a text file");

		// The close button
		closeButton = new JButton(discardResultAction);
		discardResultAction.putValue(MCODEDiscardResultAction.REQUEST_USER_CONFIRMATION_COMMAND, true);

		buttonPanel.add(exportButton);
		buttonPanel.add(closeButton);

		panel.add(explorePanel, BorderLayout.NORTH);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

}
