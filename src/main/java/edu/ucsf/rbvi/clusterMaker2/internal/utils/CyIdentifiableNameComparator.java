package edu.ucsf.rbvi.clusterMaker2.internal.utils;

import java.util.Comparator;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;

public class CyIdentifiableNameComparator implements Comparator<CyIdentifiable> {
	CyNetwork network;

	public CyIdentifiableNameComparator(CyNetwork network) {
		this.network = network;
	}

	public int compare(CyIdentifiable o1, CyIdentifiable o2) {
		String name1 = network.getRow(o1).get(CyNetwork.NAME, String.class);
		String name2 = network.getRow(o2).get(CyNetwork.NAME, String.class);
		return name1.compareTo(name2);
	}
}
