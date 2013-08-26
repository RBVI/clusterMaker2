/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: TreeDrawerNode.java,v $
 * $Revision: 1.8 $
 * $Date: 2005/03/07 22:20:41 $
 * $Name:  $
 *
 * This file is part of Java TreeView
 * Copyright (C) 2001-2003 Alok Saldanha, All Rights Reserved. Modified by Alex Segal 2004/08/13. Modifications Copyright (C) Lawrence Berkeley Lab.
 *
 * This software is provided under the GNU GPL Version 2. In particular, 
 *
 * 1) If you modify a source file, make a comment in it containing your name and the date.
 * 2) If you distribute a modified version, you must do it under the GPL 2.
 * 3) Developers are encouraged but not required to notify the Java TreeView maintainers at alok@genome.stanford.edu when they make a useful addition. It would be nice if significant contributions could be merged into the main distribution.
 *
 * A full copy of the license can be found in gpl.txt or online at
 * http://www.gnu.org/licenses/gpl.txt
 *
 * END_HEADER 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.treeview;

/**
 * Represents nodes in ATRView and GTRView.
 *
 * HACK Should really retrofit this so that it's a subclass of DefaultMutableTreeNode. 
 *
 * @author Alok Saldanha <alok@genome.stanford.edu>
 * @version Alpha
 */

import java.awt.Color;
import java.util.Stack;

public class TreeDrawerNode  {
    //accessors
	/**
	 * @return index of node, i.e. where to draw node across width of tree
	 */
    public double getIndex() {return ind;}
	/**
	 * @return correlation of node, i.e. where to draw node across height of tree
	 */
    public double getCorr() {return corr;}
    public String getId() { return id;}
    public TreeDrawerNode getParent() { return parent;}
    public TreeDrawerNode getLeft() { return left;}
    public TreeDrawerNode getRight() { return right;}
    
    // settors
    public void setParent(TreeDrawerNode n) { parent = n;}
		public void setCorr(double newCorr) {corr = newCorr;}
   //    public void setLeft  (TreeDrawerNode n) { left = n;}
    //    public void setRight (TreeDrawerNode n) { right = n;}
		/**
		* returns maximum correlation value (really branch height) for this subtree 
		*/
		public double getMaxCorr() {
			double curCorr = getCorr();
			if (isLeaf())	return curCorr;
			
			Stack remaining = new Stack();
			remaining.push(this);
			while (remaining.empty() == false) {
				TreeDrawerNode node = (TreeDrawerNode) remaining.pop();
				if (node.getCorr() > curCorr)
					curCorr = node.getCorr();

				TreeDrawerNode leftNode = node.getLeft();
				if (leftNode != null) remaining.push(leftNode);

				TreeDrawerNode rightNode = node.getRight();
				if (rightNode != null) remaining.push(rightNode);
			}
			
			return curCorr;
		}

		/**
		 * returns minimum correlation value (really branch height) for this subtree 
		 */
		public double getMinCorr() {
			double curCorr = getCorr();
			if (isLeaf())	return curCorr;
			
			Stack remaining = new Stack();
			remaining.push(this);
			while (remaining.empty() == false) {
				TreeDrawerNode node = (TreeDrawerNode) remaining.pop();
				if (node.getCorr() < curCorr)
					curCorr = node.getCorr();

				TreeDrawerNode leftNode = node.getLeft();
				if (leftNode != null) remaining.push(leftNode);

				TreeDrawerNode rightNode = node.getRight();
				if (rightNode != null) remaining.push(rightNode);
			}
			return curCorr;
		}
		
		/**
		 * This method interatively finds the node with the given id.
		 * @param nodeid ID of the node to be found.
		 * @return the node found, or null if no such node exists
		 */
		public TreeDrawerNode findNode(String nodeid)
		{
			Stack remaining = new Stack();
			remaining.push(this);
			while (remaining.empty() == false) {
				TreeDrawerNode node = (TreeDrawerNode) remaining.pop();
			
				if(node.getId().equals(nodeid)) return node;

				TreeDrawerNode leftNode = node.getLeft();
				if (leftNode != null) remaining.push(leftNode);

				TreeDrawerNode rightNode = node.getRight();
				if (rightNode != null) remaining.push(rightNode);
			}
			
			return null;
			
		}	

    public double getDist(double index, double correlation, double weight) {
	double dx = ind - index;
	double dy = corr - correlation;
	dy *= weight;
	return dx * dx + dy * dy;
    }

    // constructor for leafs
    public TreeDrawerNode (String i, double correlation, double index) {
	id = i;
	corr = correlation;
	ind = index;
	minInd = index;
	maxInd = index;
    }
    // constructor for internal nodes
    public TreeDrawerNode (String i, double correlation, TreeDrawerNode l, TreeDrawerNode r) {
	id = i;
	corr = correlation;
	right = r;
	left = l;
	ind = (right.getIndex() + left.getIndex()) / 2;
	minInd = Math.min(right.getMinIndex(), left.getMinIndex());
	maxInd = Math.max(right.getMaxIndex(), left.getMaxIndex());
	if (minInd > maxInd) {
	    throw new RuntimeException
		("min was less than max! this should not happen.");
	}
    }
    

    public double getMaxIndex() {
	return maxInd;
    }

    public double getMinIndex() {
	return minInd;
    }
    
    public boolean isLeaf() {
	return ((left == null) && (right == null));
    }
        
    public TreeDrawerNode getLeftLeaf() {
    		TreeDrawerNode cand = this;
    		while (cand.isLeaf() == false)
    			cand = cand.getLeft();
    		return cand;
    }
    public TreeDrawerNode getRightLeaf() {
		TreeDrawerNode cand = this;
		while (cand.isLeaf() == false)
			cand = cand.getRight();
		return cand;
    }
    
    public double getRange() {
	return maxInd - minInd;
    }

    public Color getColor() {
	return color;
    }

    public void setColor(Color c) {
	color = c;
    }
		
	public void printSubtree() {
		printRecursive("");
	}
	private void printRecursive(String pre) {
		if (getLeft() != null) {
			System.out.println(pre + getId() + ", corr " + getCorr() +", index " + getIndex());
			System.out.println(pre + "Left:");
			getLeft().printRecursive(pre + " ");
			System.out.println(pre + "Right:");
			getRight().printRecursive(pre + " ");
		} else {
			System.out.println(pre + getId() + " LEAF, corr " + getCorr() +", index " + getIndex());
		}
		
	}
	

    // instance variables
    private double corr = 0.0;
    private double ind = -1;
    private double minInd, maxInd; // store max and min ind from this one.
    private TreeDrawerNode parent = null;
    private TreeDrawerNode left = null;
    private TreeDrawerNode right = null;
    private String id = null;
    private Color color = Color.black;
}
