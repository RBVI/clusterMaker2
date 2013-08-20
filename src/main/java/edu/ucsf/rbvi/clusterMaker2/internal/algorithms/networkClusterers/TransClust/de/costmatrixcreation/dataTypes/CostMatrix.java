package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main.Config;


public class CostMatrix {

	private int size;
	
	private HashMap<String,Integer> integers2proteins;
	
	private HashMap<Integer, String> proteins2integers;
	
	private float[][] edgevalues;
	
	private float costs;
	
	public CostMatrix(int size){
		
		this.size = size;
		
//		System.out.println(size);
		
		this.edgevalues = new float[size][size];
		
		this.proteins2integers = new HashMap<Integer, String>(size);
		
		this.integers2proteins = new HashMap<String, Integer>(size);
		
	}

	public float[][] getEdgevalues() {
		return edgevalues;
	}

	public void setEdgevalues(float[][] edgevalues) {
		this.edgevalues = edgevalues;
	}
	
	public void setEdgevalues(int x, int y, float value){
		this.edgevalues[x][y] = value;
		this.edgevalues[y][x] = value;
	}
	
	public float getEdgevalue(int x, int y){
		return this.edgevalues[x][y];
	}

	
	public HashMap<String, Integer> getIntegers2proteins() {
		return integers2proteins;
	}

	public void setIntegers2proteins(HashMap<String, Integer> integers2proteins) {
		this.integers2proteins = integers2proteins;
	}

	public HashMap<Integer, String> getProteins2integers() {
		return proteins2integers;
	}

	public void setProteins2integers(HashMap<Integer, String> proteins2integers) {
		this.proteins2integers = proteins2integers;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public void writeCostMatrix(String fileName) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		bw.write(Float.toString(this.costs));
		bw.newLine();
		bw.write(Integer.toString(this.size));
		bw.newLine();
		
//		for (Iterator<String> iter = this.integers2proteins.keySet().iterator(); iter.hasNext();) {
		for (int i = 0; i < this.integers2proteins.size(); i++) {
			String protein = this.proteins2integers.get(i);
			bw.write(protein);
			bw.newLine();
		}	
			
//		}
		
		
		for (int i = 0; i < this.size-1; i++) {
			
			for (int j = i+1; j < this.size-1; j++) bw.write(Float.toString(this.edgevalues[i][j]) + "\t");
			
			bw.write(Float.toString(this.edgevalues[i][this.size-1]));
			bw.newLine();
			
		}
		
		bw.close();
	}

	public CostMatrix mergeNodes() {
		
		Vector<Vector<Integer>> groupsOfMergedNodes = detectGroupsOfMergedNodes();
		
		CostMatrix cm = new CostMatrix(groupsOfMergedNodes.size());
		cm.costs =0;
		HashMap<Integer, String> proteins2integers = new HashMap<Integer, String>();
		HashMap<String,Integer> integers2proteins = new HashMap<String, Integer>();

		for (int i = 0; i < groupsOfMergedNodes.size(); i++) {
			
			Vector<Integer> sourceVector = groupsOfMergedNodes.get(i);
			
			cm.costs += calculateGroupCosts(sourceVector);
			String proteins = "";
			
			for (int j = 0; j < sourceVector.size(); j++) {
				
				proteins+= this.proteins2integers.get(sourceVector.get(j));
				if(j<sourceVector.size()-1) proteins+="\t";
				
			}
			
			proteins2integers.put(i, proteins);
			integers2proteins.put(proteins, i);
			
			for (int j = i+1; j < groupsOfMergedNodes.size(); j++) {
				
				Vector<Integer> targetVector = groupsOfMergedNodes.get(j);
				
				Vector<Float> costs2 = new Vector<Float>();
				float value = calculateValue(sourceVector,targetVector,costs2);
				cm.setEdgevalues(i, j, value);
				cm.costs+=costs2.get(0);
				
			}
			
		}
		
		cm.setIntegers2proteins(integers2proteins);
		cm.setProteins2integers(proteins2integers);
		
		return cm;
	}
	
	private float calculateValue(Vector<Integer> sourceVector, Vector<Integer> targetVector, Vector<Float> costs) {
		
		float value = 0;
		float positiveCosts = 0;
		float negativeCosts = 0;
		
		for (int i = 0; i < sourceVector.size(); i++) {
			
			int source =sourceVector.get(i);
			
			for (int j = 0; j < targetVector.size(); j++) {
				
				int target = targetVector.get(j);
				
				value+=(this.edgevalues[source][target]-Config.threshold);
				
				if(this.edgevalues[source][target]<Config.threshold) positiveCosts += Config.threshold-this.edgevalues[source][target];
				else negativeCosts += this.edgevalues[source][target] - Config.threshold;
				
			}
			
		}
		
		if(value>0) costs.add(positiveCosts);
		else costs.add(negativeCosts);
		
		return value;
	}

	private float calculateGroupCosts(Vector<Integer> v){
		
		float costs = 0;
		
		for (int i = 0; i < v.size(); i++) {
			
			int source = v.get(i);
			
			for (int j = i+1; j < v.size(); j++) {
				
				int target = v.get(j);
				
				if(this.edgevalues[source][target]<Config.threshold) costs+=Math.abs(Config.threshold-this.edgevalues[source][target]);
				
			}
		}
		
		return costs;
		
	}

	private Vector<Vector<Integer>> detectGroupsOfMergedNodes(){
		
		Vector<Vector<Integer>> neighbours = new Vector<Vector<Integer>>();
		
		boolean[] already = new boolean[this.size];
		
		for (int i = 0; i < this.size; i++) {
			
			if(!already[i]){
				
				Vector<Integer> v = new Vector<Integer>();
				findNeighbour(i,already,v);
				neighbours.add(v);
				
			}
			
		}
		
		return neighbours;
	}
	
	private void findNeighbour(int i, boolean[] already, Vector<Integer> v){
		
		if(!already[i]){
			
			already[i]=true;
			v.add(i);
			for (int j = 0; j < this.size; j++) if(this.edgevalues[i][j]>Config.upperBound) findNeighbour(j,already,v);
				
		}
		
	}	
	
}
