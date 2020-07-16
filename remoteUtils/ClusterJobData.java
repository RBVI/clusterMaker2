package edu.ucsf.rbvi.clusterMaker2.internal.utils.remoteUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.jobs.CyJobData;

public class ClusterJobData implements CyJobData {
	Map<String, Object> dataStore;
	
	public ClusterJobData() {
		dataStore = new HashMap<>();
	}

	@Override
	public Map<String, Object> getAllValues() { return dataStore; }

	@Override
	public Object get(String key) {
		if (dataStore.containsKey(key))
			return dataStore.get(key);
		return null;
	}

	@Override
	public void put(String key, Object value) {
		dataStore.put(key, value);
	}

	@Override
	public Object remove(String key) {
		if (dataStore.containsKey(key))
			return dataStore.remove(key);
		return null;
	}

	@Override
	public boolean containsKey(String key) {
		return dataStore.containsKey(key);
	}

	@Override
	public Set<String> keySet() {
		return dataStore.keySet();
	}

	@Override
	public void clear() {
		dataStore.clear();
	}
}
