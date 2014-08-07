package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BiMine;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Abhiraj
 *	Node for Bicluster Enumeration Tree
 */
public class BETNode<T> {
	private T data;
	private List<BETNode<T>> children;
	private BETNode<T> parent;
	
	public BETNode(T data) {
		this.data = data;
		this.children = new ArrayList<BETNode<T>>();
	}

	public BETNode(BETNode<T> node) {
		this.data = (T) node.getData();
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
	
	public T getData() {
		return this.data;
	}

	public void setData(T data) {
		this.data = data;
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
}
