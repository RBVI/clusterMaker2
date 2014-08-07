package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BiMine;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Abhiraj
 *	Node for Bicluster Enumeration Tree
 *
 *@param <T>
 */
public class BETNode<T> {
	private List<T> genes;
	private List<T> conditions;
	private List<BETNode<T>> children;
	private BETNode parent;
	
	public BETNode(List<T> genes,List<T> conditions) {
		this.genes = genes;
		this.conditions = conditions;
		this.children = new ArrayList<BETNode<T>>();
	}

	public BETNode(BETNode<T> node) {
		this.genes = node.getGenes();
		this.conditions = node.getConditions();
		children = new ArrayList<BETNode<T>>();
	}

	public void addChild(BETNode<T> child){
		child.setParent(this);
		children.add(child);
	}
	
	public void setChildren(List<BETNode<T>> children){
		for(BETNode<T> child: children){
			child.setParent(this);
		}
		this.children = children;
	}
	
	public void removeChildren() {
		this.children.clear();
	}
	
	public List<T> getGenes() {
		return this.genes;
	}
	
	public List<T> getConditions() {
		return this.conditions;
	}

	public void setData(List<T> genes,List<T> conditions) {
		this.genes = genes;
		this.conditions = conditions;
	}

	public BETNode<T> getParent() {
		return this.parent;
	}

	public void setParent(BETNode<T> parent) {
		this.parent = parent;
	}

	public List<BETNode<T>> getChildren() {
		return this.children;
	}
	
	public boolean isLeaf(){
		return (this.children.size()==0);
	}
	
	public BETNode<T> getUncle(int i){
		if (this.parent==null || this.parent.parent==null) return null;
		else{
			List<BETNode<T>> prevLevel = this.parent.getChildren();
			int parentIndex = prevLevel.indexOf(this.parent);
			return prevLevel.get(parentIndex + i + 1);
		}
	
	}
}


