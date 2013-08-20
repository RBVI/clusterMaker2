package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes;

public class Edges {

	public int[] sources;
	
	public int[] targets;
	
	public float[] values;
	
	public int[] startPositions;
	
	public int[] endPositions;
	
	public int proteinNumber;
	
	public Edges(int size, int proteinNumber){
		this.proteinNumber = proteinNumber;
		this.sources = new int[size];
		this.targets = new int[size];
		this.values = new float[size];
		this.startPositions = new int[proteinNumber];
		this.endPositions = new int[proteinNumber];
	}
	
	public void setStartPosition(int i, int position){
		this.startPositions[i] = position;
	}
	
	public int getStartPosition(int i){
		return this.startPositions[i];
	}
	
	public int[] getStartPosition(){
		return this.startPositions;
	}
	
	public void setEndPosition(int i, int position){
		this.endPositions[i] = position;
	}
	
	public int getEndPosition(int i){
		return this.endPositions[i];
	}
	
	public int[] getEndPosition(){
		return this.endPositions;
	}
	
	public void setSource(int i, int source){
		this.sources[i] = source;
	}
	
	public int getSource(int i){
		return this.sources[i];
	}
	
	public void setTarget(int i, int target){
		this.targets[i] = target;
	}
	
	public int getTarget(int i){
		return this.targets[i];
	}
	
	public void setValue(int i, float value){
		this.values[i]=value;
	}
	
	public float getValue(int i){
		return this.values[i];
	}
	
	public int size(){
		return this.values.length;
	}
	
	public int size2(){
		return this.startPositions.length;
	}
	
	public float getValue(int source, int target){
		int position = calculateArrayPosition(source, target);
		return this.values[position];
	}

	private int calculateArrayPosition(int source, int target) {
		if(source !=0) return ((proteinNumber*(source))-((source*(source+1))/2))+(target-source)-1;
		return target-1;
	}

	public int[] getTargets() {
		return targets;
	}

	public void setTargets(int[] targets) {
		this.targets = targets;
	}
	
	
	
}
