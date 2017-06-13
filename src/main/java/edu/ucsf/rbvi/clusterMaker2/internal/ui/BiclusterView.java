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

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.kmeans.KMeansCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.kmedoid.KMedoidCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterAlgorithm;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterResults;
import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterViz;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.KnnViewFrame;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.PropertyConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeViewApp;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeViewFrame;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ViewFrame;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.KnnViewModel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.TreeViewModel;
import edu.ucsf.rbvi.clusterMaker2.internal.utils.ModelUtils;

public class BiclusterView extends TreeView {

	public static String SHORTNAME = "biclusterview";
	public static String NAME =  "JTree BiclusterView";
	
	protected TaskMonitor monitor = null;
	protected CyTableManager tableManager = null;
	protected CyTableFactory tableFactory = null;
	protected String clusterAttribute = null;
	List<String> rowList;
	List<String> colList;

	@Tunable(description="Network to view bicluster heatmap for", context="nogui")
	public CyNetwork network = null;

	private static String appName = "clusterMaker BiclusterView";
	
	public BiclusterView(ClusterManager manager) {
		super(manager);		
		tableManager = manager.getTableManager();
		tableFactory = manager.getTableFactory();
				
		if (network == null) 
			myNetwork = manager.getNetwork();
		else
			myNetwork = network;
		
		try {
			clusterAttribute =
				myNetwork.getRow(myNetwork, CyNetwork.LOCAL_ATTRS).
					get(ClusterManager.CLUSTER_ATTRIBUTE, String.class);
		} catch (NullPointerException npe) {
			clusterAttribute = null;
		}
		//System.out.println("Cluster Attribute Name: "+clusterAttribute);
	}

	public BiclusterView(PropertyConfig propConfig) {
		super(propConfig);
	}
	
	public void setVisible(boolean visibility) {
		if (viewFrame != null)
			viewFrame.setVisible(visibility);
	}

	public String getAppName() {
		return appName;
	}

	// ClusterViz methods
	public String getShortName() { return SHORTNAME; }

	@ProvidesTitle
	public String getName() { return NAME; }

	public ClusterResults getResults() { return null; }
	
	public void run(TaskMonitor monitor) {
		if (clusterAttribute == null)
			return;
		monitor.setTitle("Creating heat map");
		myNetwork = manager.getNetwork();
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

		/*
		Map<Integer, List<String>> clusterNodes = getBiclusterNodes();
		Map<Integer, List<String>> clusterAttrs = getBiclusterAttributes();
		
		
		ArrayList<String> nodeArrayL = new ArrayList<String>();
		for(Integer key:clusterNodes.keySet()){
			List<String> bicluster =  clusterNodes.get(key);
			for(String node:bicluster){
				nodeArrayL.add(node);
			}			
		}
		
		// ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.NODE_ORDER_ATTRIBUTE, 
    //             nodeArrayL, List.class, String.class);
		
		ArrayList<String> attrArrayL = new ArrayList<String>();
		for(Integer key:clusterAttrs.keySet()){
			List<String> bicluster =  clusterAttrs.get(key);
			for(String attr:bicluster){
				attrArrayL.add(attr);
			}			
		}
		// ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.ARRAY_ORDER_ATTRIBUTE, 
		// 		attrArrayL, List.class, String.class);
		*/
		

		/*
		//Using the overlapping and reordering for node and array order attributes
		mergeBiclusters(clusterNodes,clusterAttrs);
		ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.NODE_ORDER_ATTRIBUTE, 
                rowList, List.class, String.class);
		ModelUtils.createAndSetLocal(myNetwork, myNetwork, ClusterManager.ARRAY_ORDER_ATTRIBUTE, 
				colList, List.class, String.class);
		*/

		// Get our data model
		// FIXME: Can't use the KnnViewModel because it's going to
		// fill in all of the data for each cell and we only want to
		// fill in the data for the cells within the bicluster
		dataModel = new KnnViewModel(monitor, myNetwork, myView, manager);

		// Set up the global config
		setConfigDefaults(new PropertyConfig(cyProperty, globalConfigName(),"ProgramConfig"));

		// Set up our configuration
		PropertyConfig documentConfig = new PropertyConfig(cyProperty, getShortName(),"DocumentConfig");
		dataModel.setDocumentConfig(documentConfig);

		// Create our view frame
		KnnViewFrame frame = new KnnViewFrame(this, appName);

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
			//System.out.println("Node : "+ node);
			CyRow nodeRow = BiClusterNodeTable.getRow(node.getSUID());
			List<Integer> temp = nodeRow.get("Bicluster List", List.class);
			if(temp==null)continue;

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
			clusterAttrs.put(row.get("BiCluster Number", Integer.class), row.get("Bicluster Attribute List", List.class));			
		}
		
		return clusterAttrs;
	}	


	public static boolean isReady(CyNetwork network) {
		if (network == null) return false;
		// System.out.println("network is not null");
		if (ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_TYPE_ATTRIBUTE)) {
			String type = network.getRow(network).get(ClusterManager.CLUSTER_TYPE_ATTRIBUTE, String.class);
			// System.out.println("CLUSTER_TYPE_ATTRIBUTE = "+type);
			if (!type.equals("BicFinder") &&
			    !type.equals("BiMine") &&
			    !type.equals("ccbicluster"))
				return false;
		}

		// System.out.println("Looking for = "+ClusterManager.CLUSTER_NODE_ATTRIBUTE+" or "+ClusterManager.CLUSTER_ATTR_ATTRIBUTE);
		if (ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_NODE_ATTRIBUTE) ||
		    ModelUtils.hasAttribute(network, network, ClusterManager.CLUSTER_ATTR_ATTRIBUTE)) {
			// System.out.println("Found 'em");
			return true;
		}

		return false;
	}
}
