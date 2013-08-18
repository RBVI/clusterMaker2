package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes;

public class BlastFile {
	
	public int size;

	private int[] startQuery;
	
	private int[] endQuery;
	
	private int[] startSubject;
	
	private int[] endSubject;
	
	private int[] source;
	
	private int[] target;
	
	private double[] evalue;
	
	private double[] scores;
	
	

	public BlastFile(int n){
		this.startQuery = new int[n];
		this.endQuery = new int[n];
		this.startSubject = new int[n];
		this.endSubject = new int[n];
		this.source = new int[n];
		this.target = new int[n];
		this.evalue = new double[n];
		this.scores = new double[n];
		this.size = n;
	}
	
	public double[] getScores() {
		return scores;
	}

	public void setScores(double[] scores) {
		this.scores = scores;
	}
	
	public void setStartQuery(int position,int startQuery){
		this.startQuery[position]=startQuery;
	}
	
	public void setEndQuery(int position,int endQuery){
		this.endQuery[position]=endQuery;
	}
	
	public void setStartSubject(int position,int startSubject){
		this.startSubject[position]=startSubject;
	}
	
	public void setEndSubject(int position,int endSubject){
		this.endSubject[position]=endSubject;
	}
	
	public void setSource(int position,int source){
		this.source[position]=source;
	}
	
	public void setTarget(int position,int target){
		this.target[position]=target;
	}
	
	public void setEvalue(int position, double evalue){
		this.evalue[position] = evalue;
	}
	    
	public void setAll(int position, int startQuery, int endQuery, int startSubject, int endSubject, int source, int target, double evalue, double score){
		this.startQuery[position]=startQuery;
		this.endQuery[position]=endQuery;
		this.startSubject[position]=startSubject;
		this.endSubject[position]=endSubject;
		this.source[position]=source;
		this.target[position]=target;
		this.evalue[position] = evalue;
		this.scores[position] = score;
	}
	            
	public int getStartQuery(int position){
		return this.startQuery[position];
	}
	
	public int getEndQuery(int position){
		return this.endQuery[position];
	}
	
	public int getStartSubject(int position){
		return this.startSubject[position];
	}
	
	public int getEndSubject(int position){
		return this.endSubject[position];
	}
	
	public int getSource(int position){
		return this.source[position];
	}
	
	public int getTarget(int position){
		return this.target[position];
	}
	
	public double getEvalue(int position){
		return this.evalue[position];
	}
	
	public double getScore(int position){
		return this.scores[position];
	}
	            
}
