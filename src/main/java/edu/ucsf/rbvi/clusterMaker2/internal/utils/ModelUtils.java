package edu.ucsf.rbvi.clusterMaker2.internal.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;

import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.clusterMaker2.internal.api.ClusterManager;

public class ModelUtils {
	public static final String NONEATTRIBUTE = "--None--";

	public static List<CyEdge> getConnectingEdges(CyNetwork network, List<CyNode> nodes) {
		List<CyEdge> edgeList = new ArrayList<CyEdge>();
		for (int rowIndex = 0; rowIndex < nodes.size(); rowIndex++) {
			for (int colIndex = rowIndex; colIndex < nodes.size(); colIndex++) {
				List<CyEdge> connectingEdges = network.getConnectingEdgeList(nodes.get(rowIndex), nodes.get(colIndex), CyEdge.Type.ANY);
				if (connectingEdges != null) edgeList.addAll(connectingEdges);
			}
		}
		return edgeList;
	}

	public static String getNodeName(CyNetwork network, CyNode node) {
		return network.getRow(node).get(CyNetwork.NAME, String.class);
	}

	public static String getNetworkName(CyNetwork network) {
		return network.getRow(network).get(CyNetwork.NAME, String.class);
	}

	public static boolean hasAttribute(CyNetwork network, CyIdentifiable value, String column) {
		if (!CyTableUtil.getColumnNames(network.getRow(value).getTable()).contains(column))
			return false;

		if (network.getRow(value).getRaw(column) == null)
			return false;

		return true;
	}

	public static void deleteAttribute(CyNetwork network, CyIdentifiable value, String column) {
		if (!CyTableUtil.getColumnNames(network.getRow(value).getTable()).contains(column))
			return;
		network.getRow(value).getTable().deleteColumn(column);
	}

	public static CyNetwork createChildNetwork(ClusterManager manager, CyNetwork network, 
	                                           List<CyNode> nodeList, List<CyEdge> edgeList, String title) {

		String name = network.getRow(network).get(CyNetwork.NAME, String.class);
		// Create the network;
		CyNetwork newNetwork = ((CySubNetwork)network).getRootNetwork().addSubNetwork(nodeList,edgeList);

		// Register the network
		CyNetworkManager netManager = manager.getService(CyNetworkManager.class);
		netManager.addNetwork(newNetwork);

		// Set the title
		newNetwork.getRow(newNetwork).set(CyNetwork.NAME, name+title);

		return newNetwork;
	}

	public static void createAndSetLocal(CyNetwork net, CyIdentifiable obj, String column, 
	                                     Object value, Class type, Class elementType) {
		createAndSet(net, obj, column, value, type, elementType, CyNetwork.LOCAL_ATTRS);
	}

	public static void createAndSet(CyNetwork net, CyIdentifiable obj, String column, 
	                                Object value, Class type, Class elementType) {
		createAndSet(net, obj, column, value, type, elementType, CyNetwork.DEFAULT_ATTRS);
	}

	public static void createAndSet(CyNetwork net, CyIdentifiable obj, String column, 
	                                Object value, Class type, Class elementType, String namespace) {
		CyTable tab = net.getRow(obj, namespace).getTable();
		if (tab.getColumn(column) == null) {
			if (type.equals(List.class))
				tab.createListColumn(column, elementType, false);
			else
				tab.createColumn(column, type, false);
		}
		net.getRow(obj, namespace).set(column, value);
	}

	public static ListMultipleSelection<String> updateAttributeList(CyNetwork network, 
	                                                                ListMultipleSelection<String> attributes) {
		List<String> attributeArray = getAllAttributes(network, network.getDefaultNodeTable());
		attributeArray.addAll(getAllAttributes(network, network.getDefaultEdgeTable()));
		ListMultipleSelection<String> newAttribute = new ListMultipleSelection<String>(attributeArray);	
		if (attributeArray.size() > 0){
			if (attributes != null) {
				newAttribute.setSelectedValues(attributes.getSelectedValues());
			} else {
				newAttribute.setSelectedValues(Collections.singletonList(attributeArray.get(0)));
			}
			return newAttribute;
		}
		return new ListMultipleSelection<String>("--None--");
	}


	public static ListMultipleSelection<String> updateNodeAttributeList(CyNetwork network, 
	                                                                    ListMultipleSelection<String> attribute) {
		List<String> attributeArray = getAllAttributes(network, network.getDefaultNodeTable());
		if (attributeArray.size() > 0){
			ListMultipleSelection<String> newAttribute = new ListMultipleSelection<String>(attributeArray);	
			if (attribute != null) {
				newAttribute.setSelectedValues(attribute.getSelectedValues());
			} else
				newAttribute.setSelectedValues(Collections.singletonList(attributeArray.get(0)));

			return newAttribute;
		}
		return new ListMultipleSelection<String>("--None--");
	}

	public static ListSingleSelection<String> updateEdgeAttributeList(CyNetwork network, 
	                                                                  ListSingleSelection<String> attribute) {
		List<String> attributeArray = getAllAttributes(network, network.getDefaultEdgeTable());
		if (attributeArray.size() > 0){
			ListSingleSelection<String> newAttribute = new ListSingleSelection<String>(attributeArray);	
			if (attribute != null && attributeArray.contains(attribute.getSelectedValue())) {
				newAttribute.setSelectedValue(attribute.getSelectedValue());
			} else
				newAttribute.setSelectedValue(attributeArray.get(0));

			return newAttribute;
		}
		return new ListSingleSelection<String>("--None--");
	}

	public static String getName(CyNetwork network, CyIdentifiable obj) {
		CyRow row = network.getRow(obj);
		if (row == null) return null;
		return row.get(CyNetwork.NAME, String.class);
	}

	public static CyIdentifiable getNetworkObjectWithName(CyNetwork network, String name, Class <? extends CyIdentifiable> clazz) {
		CyTable table = network.getTable(clazz, CyNetwork.DEFAULT_ATTRS);
		Collection<CyRow> rows = table.getMatchingRows(CyNetwork.NAME, name);
		for (CyRow row: rows) {
			// Get take the first one
			Long suid = row.get(CyIdentifiable.SUID, Long.class);
			if (clazz.equals(CyNode.class))
				return network.getNode(suid);
			else if (clazz.equals(CyEdge.class))
				return network.getEdge(suid);
		}
		return null;
	}

	public static Double getNumericValue(CyNetwork network, CyIdentifiable obj, String column) {
		CyColumn col = network.getRow(obj).getTable().getColumn(column);
		Class type = col.getType();
		Number val = null;
		if (type == Double.class) {
			val = network.getRow(obj).get(column, Double.class);
		} else if (type == Float.class) {
			val = network.getRow(obj).get(column, Float.class);
		} else if (type == Long.class) {
			val = network.getRow(obj).get(column, Long.class);
		} else if (type == Integer.class) {
			val = network.getRow(obj).get(column, Integer.class);
		} else
			return null;

		if (val == null) return null;

		return Double.valueOf(val.doubleValue());
	}

	private static List<String> getAllAttributes(CyNetwork network, CyTable table) {
		String[] attributeArray = new String[1];
		// Create the list by combining node and edge attributes into a single list
		List<String> attributeList = new ArrayList<String>();
		attributeList.add(NONEATTRIBUTE);
		getAttributesList(attributeList, table);
		String[] attrArray = attributeList.toArray(attributeArray);
		if (attrArray.length > 1) 
			Arrays.sort(attrArray);
		return Arrays.asList(attrArray);
	}

	private static void getAttributesList(List<String>attributeList, CyTable attributes) {
		Collection<CyColumn> names = attributes.getColumns();
		java.util.Iterator<CyColumn> itr = names.iterator();
		for (CyColumn column: attributes.getColumns()) {
			if (column.getType() == Double.class ||
					column.getType() == Integer.class) {
					attributeList.add(column.getName());
			}
		}
	}
	
}
