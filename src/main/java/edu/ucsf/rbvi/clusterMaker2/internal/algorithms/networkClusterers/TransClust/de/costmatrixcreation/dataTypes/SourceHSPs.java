package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class SourceHSPs {

	private ArrayList<Integer> lines;

	private ArrayList<boolean[]> coverages;

	private int lenght;
	
	private String id;
	
	private ArrayList<Integer> cluster;
	
	private int clusternr;
	
	private ArrayList<Integer> clusterline;

	public SourceHSPs(ArrayList<boolean[]> coverages, int lenght,
			ArrayList<Integer> lines,String id) {
		super();
		this.coverages = coverages;
		this.lenght = lenght;
		this.lines = lines;
		this.id = id;
		this.cluster = new ArrayList<Integer>();
		this.clusterline= new ArrayList<Integer>();
		this.clusternr = 0;
	}

	public SourceHSPs(int size) {
		this.lenght = size;
		this.lines = new ArrayList<Integer>();
		this.coverages = new ArrayList<boolean[]>();
	}

	public boolean addAllCoverages(Collection<? extends boolean[]> c) {
		return coverages.addAll(c);
	}

	public boolean addAllLines(int index, Collection<? extends Integer> c) {
		return lines.addAll(index, c);
	}

	public void addCluster(Integer element) {
		cluster.add(element);
	}

	public boolean addClusterLine(Integer e) {
		return clusterline.add(e);
	}

	public boolean addCoverage(boolean[] e) {
		return coverages.add(e);
	}

	public boolean addLine(Integer e) {
		return lines.add(e);
	}

	private void assignRecursivly(float[][] similarity,int clusterNr,
			boolean[] already, float distance, int seed, int[] clusters) {
		

		
		for (int i = 0; i < already.length; i++) {
			if(already[i]) continue;
			if(similarity[i][seed]>=distance){
				clusters[i] = clusterNr;
				already[i] = true;
				assignRecursivly(similarity, clusterNr, already, distance, i,clusters);
			}
		}
	}

	private int calculateClusters(float[][] similarity, float distance, int[] clusters,
			boolean[] already) {
		
		int clusterNr = 0;
		for (int i = 0; i < clusters.length; i++) {
			if(already[i]) continue;
			clusters[i] = clusterNr;
			already[i] = true;
			assignRecursivly(similarity, clusterNr,already,distance,i,clusters);
			clusterNr++;
		}
		
		return clusterNr;
	}

	private float calculateSimilarity(boolean[] bs, boolean[] bs2) {
		float sim = 0;
		for (int i = 0; i < bs2.length; i++) {
			if((bs[i]&&bs2[i])||(!bs[i]&&!bs2[i])) sim++;
		}
		sim/=bs.length;
		return sim;
	}

	public void cluster(){
		
		float similarity[][] = new float[this.lines.size()][this.lines.size()];
		
		for (int i = 0; i < this.lines.size(); i++) {
			for (int j = i+1; j < this.lines.size(); j++) {
				float sim = calculateSimilarity(this.coverages.get(i),this.coverages.get(j));
				similarity[i][j] = similarity[j][i] = sim;
			}
		}
		
		int clusters[] = new int[this.lines.size()];
		boolean already[] = new boolean[this.lines.size()];
		int clusternr = calculateClusters(similarity,(float) 0.9, clusters, already);
		
		System.out.println(this.id + "\t" + clusternr + "\t" + this.lines.size());
		System.out.println(Arrays.toString(clusters));
		for (int i = 0; i < clusters.length; i++) {
			this.cluster.add(clusters[i]);
		}
		
	}
	
	public ArrayList<Integer> getCluster() {
		return cluster;
	}

	
	public Integer getCluster(int index) {
		return cluster.get(index);
	}


	public ArrayList<Integer> getClusterline() {
		return clusterline;
	}
	
	public Integer getClusterLine(int index) {
		return clusterline.get(index);
	}

	public int getClusternr() {
		return clusternr;
	}

	public boolean[] getCoverage(int index) {
		return coverages.get(index);
	}

	public ArrayList<boolean[]> getCoverages() {
		return coverages;
	}

	public int getLenght() {
		return lenght;
	}

	public Integer getLine(int index) {
		return lines.get(index);
	}

	public ArrayList<Integer> getLines() {
		return lines;
	}

	public void setCluster(ArrayList<Integer> cluster) {
		this.cluster = cluster;
	}

	public void setClusterline(ArrayList<Integer> clusterline) {
		this.clusterline = clusterline;
	}

	public void setClusternr(int clusternr) {
		this.clusternr = clusternr;
	}

	public void setCoverages(ArrayList<boolean[]> coverages) {
		this.coverages = coverages;
	}

	public void setLenght(int lenght) {
		this.lenght = lenght;
	}

	public void setLines(ArrayList<Integer> lines) {
		this.lines = lines;
	}
}
