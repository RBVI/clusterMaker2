package edu.ucsf.rbvi.clusterMaker2.internal.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.model.CyNode;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster.LengthComparator;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.NodeCluster.ScoreComparator;

/*
 * The Fuzzy Node Clusters will be used to store the clusters for the fuzzy algorithms
 * These are similar to the Node Clusters, with an addition of the membership of each node in the cluster
 *
 */

public class FuzzyNodeCluster extends NodeCluster{
	
	private HashMap<CyNode, Double> membershipMap = null;
		
	public FuzzyNodeCluster() {
		super();
		
	}
	
	public FuzzyNodeCluster(Collection<CyNode> collection, HashMap<CyNode, Double> clusterMemberships) {
		super(collection);
		
		membershipMap = new HashMap<CyNode, Double>();
		
		for(CyNode element : clusterMemberships.keySet()){
			
			if(contains(element)){
				membershipMap.put(element,clusterMemberships.get(element));				
			}
		}
		
	}
	
	public boolean add(List<CyNode>nodeList, int index, double membershipValue) {
		
		boolean retval = add(nodeList.get(index));
		if(retval){
			membershipMap.put(nodeList.get(index), membershipValue);
		}
		return retval;
	}
	
	public Object getMembership(CyNode node){
		return membershipMap.get(node);	

		/*if(membershipMap.containsKey(node)){
			return membershipMap.get(node);	
		}
		else{
		return null;
		}*/
	}
	
	public void setMembership(CyNode node, double membership){
		
		membershipMap.put(node,membership);		
	}
	
	@Override
	public boolean isFuzzy(){
		return true;
	}
	
}

/*
public class FuzzyNodeCluster extends ArrayList<CyNode> {
	
	int clusterNumber = 0;
	static int clusterCount = 0;
	static boolean hasScore = false;
	protected double score = 0.0;
	
	private HashMap<CyNode, Double> membershipMap = null;
	
	public FuzzyNodeCluster() {
		super();
		clusterCount++;
		clusterNumber = clusterCount;
	}

	public FuzzyNodeCluster(Collection<CyNode> collection, HashMap<CyNode, double[]> clusterMemberships) {
		super(collection);
		clusterCount++;
		clusterNumber = clusterCount;
		membershipMap = new HashMap<CyNode, Double>();
		
		for(CyNode element : clusterMemberships.keySet()){
			
			if(contains(element)){
				membershipMap.put(element,clusterMemberships.get(element)[clusterNumber -1]);				
			}
		}
		
	}
	
	public boolean add(List<CyNode>nodeList, int index, double membershipValue) {
		
		boolean retval = add(nodeList.get(index));
		if(retval){
			membershipMap.put(nodeList.get(index), membershipValue);
		}
		return retval;
	}

	public static void init() { clusterCount = 0; hasScore = false; }
	public static boolean hasScore() { return hasScore; }

	public int getClusterNumber() { return clusterNumber; }

	public void setClusterNumber(int clusterNumber) { 
		this.clusterNumber = clusterNumber; 
	}

	public void setClusterScore(double score) { 
		this.score = score; 
		hasScore = true;
	}
	
	public double getClusterScore() { return score; }


	public String toString() {
		String str = "("+clusterNumber+": ";
		for (Object i: this) 
			str += i.toString();
		return str+")";
	}
	
	public static List<FuzzyNodeCluster> sortMap(Map<Integer, FuzzyNodeCluster> map) {
		FuzzyNodeCluster[] clusterArray = map.values().toArray(new FuzzyNodeCluster[1]);
		Arrays.sort(clusterArray, new LengthComparator());
		return Arrays.asList(clusterArray);
	}
	
	public static List<FuzzyNodeCluster> rankListByScore(List<FuzzyNodeCluster> list) {
		FuzzyNodeCluster[] clusterArray = list.toArray(new FuzzyNodeCluster[1]);
		Arrays.sort(clusterArray, new ScoreComparator());
		for (int rank = 0; rank < clusterArray.length; rank++) {
			clusterArray[rank].setClusterNumber(rank+1);
		}
		return Arrays.asList(clusterArray);
	}
	
	static class LengthComparator implements Comparator {
		public int compare (Object o1, Object o2) {
			List c1 = (List)o1;
			List c2 = (List)o2;
			if (c1.size() > c2.size()) return -1;
			if (c1.size() < c2.size()) return 1;
			return 0;
		}
	}

	static class ScoreComparator implements Comparator {
		public int compare (Object o1, Object o2) {
			NodeCluster c1 = (NodeCluster)o1;
			NodeCluster c2 = (NodeCluster)o2;
			if (c1.getClusterScore() > c2.getClusterScore()) return -1;
			if (c1.getClusterScore() < c2.getClusterScore()) return 1;
			return 0;
		}
	}
	
	public double getMembership(CyNode node){
		
		return membershipMap.get(node);		
	}
	
	public void setMembership(CyNode node, double membership){
		
		membershipMap.put(node,membership);		
	}

}
*/

