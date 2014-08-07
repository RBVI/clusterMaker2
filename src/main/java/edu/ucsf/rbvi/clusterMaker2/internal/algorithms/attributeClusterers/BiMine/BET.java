package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.attributeClusterers.BiMine;


/**
 * 
 * @author Abhiraj
 *	Bicluster Enumeration Tree
 *	Data Structure for the use in BiMine algorithm
 *
 * @param <T>
 */
public class BET<T> {

	private BETNode<T> root;
	
	public BET(BETNode<T> root){
		this.root = root;
	}
	
	public boolean isEmpty() {
		return (root == null) ? true : false;
	}
	
	public BETNode<T> getRoot() {
		return root;
	}
	
	public void setRoot(BETNode<T> root) {
		this.root = root;
	}

}
