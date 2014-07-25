package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableUtil;
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
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
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
	protected CyTableManager tableManager = null;
	protected CyTableFactory tableFactory = null;
	protected String clusterAttribute = null;
	List<String> rowList;
	List<String> colList;

	@Tunable(description="Network to view bicluster heatmap for", context="nogui")
	public CyNetwork myNetwork = null;

	private static String appName = "clusterMaker BiclusterView";
	
	public BiclusterView(ClusterManager manager) {
		super();
		// setExitOnWindowsClosed(false);
		selectedNodes = new ArrayList<CyNode>();
		selectedArrays = new ArrayList<CyNode>();
		this.manager = manager;
		networkTableManager = manager.getService(CyNetworkTableManager.class);
		tableManager = manager.getTableManager();
		tableFactory = manager.getTableFactory();
				
		if (myNetwork == null) myNetwork = manager.getNetwork();
		
		clusterAttribute =
				myNetwork.getRow(myNetwork, CyNetwork.LOCAL_ATTRS).get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
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

	public Object getContext() { return null; }

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

		Map<Integer, List<String>> clusterNodes = getBiclusterNodes();
		Map<Integer, List<String>> clusterAttrs = getBiclusterAttributes();
		
		/*
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
		*/

		//Using the overlapping and reordering for node and array order attributes
		mergeBiclusters(clusterNodes,clusterAttrs);
		ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.NODE_ORDER_ATTRIBUTE, 
                rowList, List.class, String.class);
		ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.ARRAY_ORDER_ATTRIBUTE, 
				colList, List.class, String.class);


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
	
	public void mergeBiclusters(Map<Integer, List<String>> clusterNodes, Map<Integer, List<String>> clusterAttrs){
		Map<Integer,Integer> biclusterSizes = new HashMap<Integer,Integer>();
		
		
		for(Integer key: clusterNodes.keySet()){
			biclusterSizes.put(key, clusterNodes.get(key).size());
			
		}
		//sort the Biclusters by size
		//ArrayList<Integer> list = new ArrayList<Integer>(biclusterSizes.entrySet());
		List<Map.Entry<Integer,Integer>> templist = new LinkedList<Map.Entry<Integer,Integer>>(biclusterSizes.entrySet());
		Collections.sort(templist, new Comparator<Map.Entry<Integer,Integer>>() {
			public int compare(Map.Entry<Integer,Integer> o1, Map.Entry<Integer,Integer> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		
		/*
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = templist.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		*/
		
		rowList = new ArrayList<String>();
		colList = new ArrayList<String>();
		
		List<String> rowPrev = new ArrayList<String>();
		List<String> colPrev = new ArrayList<String>();
		List<String> rowNew;
		List<String> colNew;
		
		boolean initial = true;
		//now checking biclusters in decreasing order of size
		for (Map.Entry<Integer,Integer> entry: templist) {
			int clust = entry.getKey().intValue();
			if(initial == true){
				rowPrev.addAll(clusterNodes.get(clust));
				colPrev.addAll(clusterAttrs.get(clust));
				initial = false;
			}
			else{
				rowNew = new ArrayList<String>();
				colNew = new ArrayList<String>();
				
				rowNew.addAll(clusterNodes.get(clust));
				colNew.addAll(clusterAttrs.get(clust));
				
				//Find the common rows
				List<String> rowOverlap = new ArrayList<String>(rowPrev);					
				rowOverlap.retainAll(rowNew);
				//Separate out other rows
				rowPrev.removeAll(rowOverlap);
				//Insert the other rows first 
				Collections.sort(rowPrev);
				rowList.addAll(rowPrev);
				//Then insert the common rows
				Collections.sort(rowOverlap);
				rowList.addAll(rowOverlap);
				
				//Find the common columns
				List<String> colOverlap = new ArrayList<String>(colPrev);
				colOverlap.retainAll(colNew);
				//Separate out other columns
				colPrev.removeAll(colOverlap);
				//Insert the other columns first
				Collections.sort(colPrev);
				colList.addAll(colPrev);
				//Then insert the common columns
				Collections.sort(colOverlap);
				colList.addAll(colOverlap);
				
				//The above puts the overlapping region in the bottom right corner as we build up
				//The ones which got added above are locked
				//The ones remaining from  2nd bicluster are not locked and will be used in next iteration
				rowNew.removeAll(rowOverlap);
				colNew.removeAll(colOverlap);
				rowPrev = rowNew;
				colPrev = colNew;
			}
		}
		//Now insert the remaining rows and columns for the last bicluster
		Collections.sort(rowPrev);
		rowList.addAll(rowPrev);
		Collections.sort(colPrev);
		colList.addAll(colPrev);
	}

	public Map<Integer, List<String>> getBiclusterNodes(){
		long BiClusterTableSUID = myNetwork.getRow(myNetwork).get(clusterAttribute + "_NodeTable.SUID", Long.class);
		CyTable BiClusterNodeTable = tableManager.getTable(BiClusterTableSUID);

		Map<Integer, List<String>> clusterNodes = new HashMap<Integer, List<String>>();
		int numNodes = BiClusterNodeTable.getRowCount();
		List<CyNode> nodeList = myNetwork.getNodeList();

		for (CyNode node : nodeList){
			CyRow nodeRow = BiClusterNodeTable.getRow(node.getSUID());
			List<Integer> temp = (ArrayList<Integer>) nodeRow.get("Bicluster List", List.class);

			for(Integer biclust : temp){
				if(clusterNodes.containsKey(biclust)){
					clusterNodes.get(biclust).add(ModelUtils.getName(myNetwork, node));
				}
				else{
					List<String> newlist = new ArrayList<String>();
					newlist.add(ModelUtils.getName(myNetwork, node));
					clusterNodes.put(biclust, newlist);
				}
			}
		}

		return clusterNodes;
	}
	
	public Map<Integer, List<String>> getBiclusterAttributes(){
		long BiClusterTableSUID = myNetwork.getRow(myNetwork).get(clusterAttribute + "_AttrTable.SUID", Long.class);
		CyTable BiClusterAttrTable = tableManager.getTable(BiClusterTableSUID);
		
		Map<Integer, List<String>> clusterAttrs = new HashMap<Integer, List<String>>();
		int numClust = BiClusterAttrTable.getRowCount();
		
		List<CyRow> tableRows = BiClusterAttrTable.getAllRows();
		for(CyRow row: tableRows){
			clusterAttrs.put(row.get("BiCluster Number", Integer.class), (ArrayList<String>) row.get("Bicluster Attribute List", List.class));			
		}
		
		return clusterAttrs;
	}
	
	public void handleEvent(RowsSetEvent e) {
		if (!e.containsColumn(CyNetwork.SELECTED))
			return;

		if (ignoreSelection)
			return;

		CyTable table = e.getSource();
		CyNetwork net = networkTableManager.getNetworkForTable(table);
		Class type = networkTableManager.getTableType(table);

		if (type.equals(CyNode.class)) {
			if (dataModel.isSymmetrical()) return;

			List<CyNode> selectedNodes = CyTableUtil.getNodesInState(net, CyNetwork.SELECTED, true);
			setNodeSelection(selectedNodes, true);
		} else if (type.equals(CyEdge.class) && dataModel.isSymmetrical()) {
			List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(net, CyNetwork.SELECTED, true);
			setEdgeSelection(selectedEdges, true);
		}

	}

	public void update(Observable o, Object arg) {
		CyNetworkView currentView = manager.getNetworkView();
		CyNetwork currentNetwork = manager.getNetwork();

		// See if we're supposed to disable our listeners
		if ((o == arraySelection) && (arg instanceof Boolean)) {
			// System.out.println("Changing disable listeners to: "+arg.toString());
			disableListeners = ((Boolean)arg).booleanValue();
		}

		if (disableListeners) return;

		if (o == geneSelection) {
			// System.out.println("gene selection");
			selectedNodes.clear();
			int[] selections = geneSelection.getSelectedIndexes();
			HeaderInfo geneInfo = dataModel.getGeneHeaderInfo();
			String [] names = geneInfo.getNames();
			for (int i = 0; i < selections.length; i++) {
				String nodeName = geneInfo.getHeader(selections[i])[0];
				CyNode node = (CyNode)ModelUtils.getNetworkObjectWithName(currentNetwork, nodeName, CyNode.class);
				// Now see if this network has this node
				if (node != null && !currentNetwork.containsNode(node)) {
					// No, try dropping any suffixes from the node name
					String[] tokens = nodeName.split(" ");
					node = (CyNode)ModelUtils.getNetworkObjectWithName(currentNetwork, tokens[0], CyNode.class);
				}
				if (node != null)
					selectedNodes.add(node);
			}
			// System.out.println("Selecting "+selectedNodes.size()+" nodes");
			if (!dataModel.isSymmetrical() || selectedArrays.size() == 0) {
				List<CyNode> nodesToClear = CyTableUtil.getNodesInState(currentNetwork, CyNetwork.SELECTED, true);
				ignoreSelection = true;
				for (CyNode node: nodesToClear) {
					myNetwork.getRow(node).set(CyNetwork.SELECTED, Boolean.FALSE);
					if (currentNetwork.containsNode(node))
						currentNetwork.getRow(node).set(CyNetwork.SELECTED, Boolean.FALSE);
				}

				for (CyNode node: selectedNodes) {
					if (currentNetwork.containsNode(node))
						currentNetwork.getRow(node).set(CyNetwork.SELECTED, Boolean.TRUE);
					myNetwork.getRow(node).set(CyNetwork.SELECTED, Boolean.TRUE);
				}
				manager.getService(CyEventHelper.class).flushPayloadEvents();
				ignoreSelection = false;

				if (currentView != null)
					currentView.updateView();
			}
			return;
		} else if (o == arraySelection) {
			// System.out.println("array selection");
			// We only care about array selection for symmetrical models
			if (!dataModel.isSymmetrical())
				return;

			selectedArrays.clear();
			int[] selections = arraySelection.getSelectedIndexes();
			if (selections.length == dataModel.nExpr())
				return;
			HeaderInfo arrayInfo = dataModel.getArrayHeaderInfo();
			String [] names = arrayInfo.getNames();
			for (int i = 0; i < selections.length; i++) {
				String nodeName = arrayInfo.getHeader(selections[i])[0];
				CyNode node = (CyNode)ModelUtils.getNetworkObjectWithName(currentNetwork, nodeName, CyNode.class);
				if (node != null && !currentNetwork.containsNode(node)) {
					// No, try dropping any suffixes from the node name
					String[] tokens = nodeName.split(" ");
					node = (CyNode)ModelUtils.getNetworkObjectWithName(currentNetwork, tokens[0], CyNode.class);
				}
				if (node != null)
					selectedArrays.add(node);
			}
		}

		Map<CyEdge,CyEdge>edgesToSelect = new HashMap<CyEdge,CyEdge>();
		ignoreSelection = true;
		List<CyEdge> edgesToClear = CyTableUtil.getEdgesInState(currentNetwork, CyNetwork.SELECTED, true);
		for (CyEdge edge: edgesToClear) {
			myNetwork.getRow(edge).set(CyNetwork.SELECTED, Boolean.FALSE);
			if (currentNetwork.containsEdge(edge))
				currentNetwork.getRow(edge).set(CyNetwork.SELECTED, Boolean.FALSE);
		}

		for (CyNode node1: selectedNodes) {
			for (CyNode node2: selectedArrays) {
				List<CyEdge> edges = currentNetwork.getConnectingEdgeList(node1, node2, CyEdge.Type.ANY);
				if (edges == null) {
					continue;
				}
				for (CyEdge edge: edges) {
					myNetwork.getRow(edge).set(CyNetwork.SELECTED, Boolean.TRUE);
					if (currentNetwork.containsEdge(edge))
						currentNetwork.getRow(edge).set(CyNetwork.SELECTED, Boolean.TRUE);
				}
			}
		}
		manager.getService(CyEventHelper.class).flushPayloadEvents();
		ignoreSelection = false;

		if (currentView != null)
			currentView.updateView();
		selectedNodes.clear();
		selectedArrays.clear();
	}
	
	private void setEdgeSelection(List<CyEdge> edgeArray, boolean select) {
		HeaderInfo geneInfo = dataModel.getGeneHeaderInfo();
		HeaderInfo arrayInfo = dataModel.getArrayHeaderInfo();

		// Avoid loops -- delete ourselves as observers
		geneSelection.deleteObserver(this);
		arraySelection.deleteObserver(this);

		// Clear everything that's currently selected
		geneSelection.setSelectedNode(null);
		geneSelection.deselectAllIndexes();
		arraySelection.setSelectedNode(null);
		arraySelection.deselectAllIndexes();

		// Do the actual selection
		for (CyEdge cyEdge: edgeArray) {
			if (!myNetwork.containsEdge(cyEdge))
				continue;
			CyNode source = (CyNode)cyEdge.getSource();
			CyNode target = (CyNode)cyEdge.getTarget();
			int geneIndex = geneInfo.getHeaderIndex(ModelUtils.getName(myNetwork, source));
			int arrayIndex = arrayInfo.getHeaderIndex(ModelUtils.getName(myNetwork, target));
			geneSelection.setIndex(geneIndex, select);
			arraySelection.setIndex(arrayIndex, select);
		}

		// Notify all of the observers
		geneSelection.notifyObservers();
		arraySelection.notifyObservers();

		// OK, now we can listen again
		geneSelection.addObserver(this);
		arraySelection.addObserver(this);
	}

	private void setNodeSelection(List<CyNode> nodeArray, boolean select) {
		HeaderInfo geneInfo = dataModel.getGeneHeaderInfo();
		geneSelection.deleteObserver(this);
		geneSelection.setSelectedNode(null);
		geneSelection.deselectAllIndexes();
		for (CyNode cyNode: nodeArray) {
			if (!myNetwork.containsNode(cyNode))
				continue;
			int geneIndex = geneInfo.getHeaderIndex(ModelUtils.getName(myNetwork, cyNode));
			// System.out.println("setting "+cyNode.getIdentifier()+"("+geneIndex+") to "+select);
			geneSelection.setIndex(geneIndex, select);
		}
		geneSelection.deleteObserver(this);
		geneSelection.notifyObservers();
		geneSelection.addObserver(this);
	}


}
