package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.PropertyConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeViewApp;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeViewFrame;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ViewFrame;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.TreeViewModel;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class BiclusterView extends TreeViewApp implements Observer,
								RowsSetListener,ClusterViz, ClusterAlgorithm {

	public static String SHORTNAME = "biclusterview";
	public static String NAME =  "JTree BiclusterView (unclustered)";
	
	private URL codeBase = null;
	protected ViewFrame viewFrame = null;
	protected TreeSelectionI geneSelection = null;
	protected TreeSelectionI arraySelection = null;
	protected TreeViewModel dataModel = null;
	protected CyNetworkView myView = null;
	protected TaskMonitor monitor = null;
	private	List<CyNode>selectedNodes;
	private	List<CyNode>selectedArrays;
	private boolean disableListeners = false;
	private boolean ignoreSelection = false;
	protected ClusterManager manager = null;
	protected CyNetworkTableManager networkTableManager = null;

	@Tunable(description="Network to view bicluster heatmap for", context="nogui")
	public CyNetwork myNetwork = null;

	@ContainsTunables
	public HeatMapContext context = null;

	private static String appName = "clusterMaker BiclusterView";
	
	public BiclusterView(HeatMapContext context, ClusterManager manager) {
		super();
		// setExitOnWindowsClosed(false);
		selectedNodes = new ArrayList<CyNode>();
		selectedArrays = new ArrayList<CyNode>();
		this.manager = manager;
		this.context = context;
		networkTableManager = manager.getService(CyNetworkTableManager.class);
		if (myNetwork == null) myNetwork = manager.getNetwork();
		context.setNetwork(myNetwork);
	}

	public BiclusterView(PropertyConfig propConfig) {
		super(propConfig);
		selectedNodes = new ArrayList<CyNode>();
		selectedArrays = new ArrayList<CyNode>();
		// setExitOnWindowsClosed(false);
	}
	
	public void setVisible(boolean visibility) {
		if (viewFrame != null)
			viewFrame.setVisible(visibility);
	}

	public String getAppName() {
		return appName;
	}

	public Object getContext() { return context; }

	// ClusterViz methods
	public String getShortName() { return SHORTNAME; }

	@ProvidesTitle
	public String getName() { return NAME; }

	public ClusterResults getResults() { return null; }
	
	public void run(TaskMonitor monitor) {
		monitor.setTitle("Creating heat map");
		myView = manager.getNetworkView();
		this.monitor = monitor;
		// Sanity check
		if (isAvailable()) {
			startup();
		} else {
			monitor.showMessage(TaskMonitor.Level.ERROR, "No compatible cluster results available");
		}
	}

	public boolean isAvailable() {
		return true;
	}

	public boolean isAvailable(CyNetwork network) {
		return true;
	}

	public void cancel() {}
	
	protected void startup(){
		CyProperty cyProperty = manager.getService(CyProperty.class, 
                "(cyPropertyName=cytoscape3.props)");

		List<String> nodeAttributeList = context.attributeList.getNodeAttributeList();
		String edgeAttribute = context.attributeList.getEdgeAttribute();
		
		if (nodeAttributeList == null && edgeAttribute == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "Must select either one edge column or two or more node columns");
			return;
		}
		
		if (nodeAttributeList != null && nodeAttributeList.size() > 0 && edgeAttribute != null) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Can't have both node and edge columns selected");
			return;
		}
		
		if (nodeAttributeList != null && nodeAttributeList.size() < 2) {
			monitor.showMessage(TaskMonitor.Level.ERROR,"Must have at least two node columns for cluster weighting");
			return;
		}
		
		HashMap<Integer, ArrayList<String>> clusterNodes = getBiclusterNodes();
		HashMap<Integer, ArrayList<String>> clusterAttrs = getBiclusterAttributes();
		
		
		ArrayList<String> nodeArrayL = new ArrayList<String>();
		for(Integer key:clusterNodes.keySet()){
			ArrayList<String> bicluster = clusterNodes.get(key);
			for(String node:bicluster){
				nodeArrayL.add(node);
			}			
		}
		
		ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.NODE_ORDER_ATTRIBUTE, 
                nodeArrayL, List.class, String.class);
		
		ArrayList<String> attrArrayL = new ArrayList<String>();
		for(Integer key:clusterAttrs.keySet()){
			ArrayList<String> bicluster = clusterAttrs.get(key);
			for(String attr:bicluster){
				attrArrayL.add(attr);
			}			
		}
		ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.ARRAY_ORDER_ATTRIBUTE, 
				attrArrayL, List.class, String.class);
		
		// Get our data model
		dataModel = new TreeViewModel(monitor, myNetwork, myView, manager);

		// Set up the global config
		setConfigDefaults(new PropertyConfig(cyProperty, globalConfigName(),"ProgramConfig"));

		// Set up our configuration
		PropertyConfig documentConfig = new PropertyConfig(cyProperty, getShortName(),"DocumentConfig");
		dataModel.setDocumentConfig(documentConfig);

		// Create our view frame
		TreeViewFrame frame = new TreeViewFrame(this, appName);

		// Set the data model
		frame.setDataModel(dataModel);
		frame.setLoaded(true);
		frame.addWindowListener(this);
		frame.setVisible(true);
		geneSelection = frame.getGeneSelection();
		geneSelection.addObserver(this);
		arraySelection = frame.getArraySelection();
		arraySelection.addObserver(this);

		manager.registerService(this, RowsSetListener.class, new Properties());
	
	}
	
	public HashMap<Integer, ArrayList<String>> getBiclusterNodes(){
		
		return null;
	}
	
	public HashMap<Integer, ArrayList<String>> getBiclusterAttributes(){
		
		return null;
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}


}
