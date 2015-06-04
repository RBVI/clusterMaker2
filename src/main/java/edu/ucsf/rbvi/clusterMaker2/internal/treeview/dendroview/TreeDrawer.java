/* BEGIN_HEADER                                              Java TreeView
*
* $Author: rqluk $
* $RCSfile: TreeDrawer.java,v $
* $Revision: 1.1 $
* $Date: 2006/08/16 19:13:45 $
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
package edu.ucsf.rbvi.clusterMaker2.internal.treeview.dendroview;

import java.awt.*;
import java.util.*;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.LinearTransformation;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeDrawerNode;

/**
*  Class for Drawing and Manipulating Trees
*
* @author Alok Saldanha <alok@genome.stanford.edu>
* @version Alpha

* Stores a representation of the tree in a normalized space. <p>

* The class will draw a tree on a graphics object, given some source
* rectangle in the normalized space, and a target rectangle in the
* caller's space. <p>

* The dimensions of the normalized space can be found by calling an
* accessor method. In general, it has a number of units in the major
* direction(the index direction) equal to the number of leaf
* nodes. For a sideways tree, this means that the height of the
* sideways tree is equal to the number of leafs. The minor dimension
* (or correlation direction) is determined by the range of the values
* stored in the nodes. If the nodes represent correlations, this is
* at most from -1 to 1. <p>

* The tree can also be queried to find the closest node given a (index,corr) pair. <p>

* I may later extend this class to support rotations about an arbitrary node.
*/
abstract class TreeDrawer extends Observable implements Observer {
	/**
	* Constructor does nothing but set defaults
	*/
	public TreeDrawer() {
		setDefaults();
	}
	
	/**
	 * used to keep track of the HeaderInfo we're observing, so we can stop
	 * observing if someone calls setData to a new HeaderInfo.
	 */
	private HeaderInfo nodeInfo = null;
	public void update(Observable o, Object arg) {
		if (o == nodeInfo) {
			setChanged();
			notifyObservers();
		} else {
			// CyLogger.getLogger(TreeDrawer.class).warn("TreeDrawer got update from unexpected observable " + o);
		}
	}
	/**
	* Accessor for the root node
	*
	* @return root node
	*/    
	public TreeDrawerNode getRootNode() {
		return rootNode;
	}
	
	private double corrMin;
	/**
	* this somewhat misnamed method returns the minimum branch value
	*/
	public double getCorrMin() {
		return corrMin;
	}
	public void setCorrMin(double corrMin) {
		this.corrMin = corrMin;
	}

	private double corrMax;
	/**
	* this somewhat misnamed method returns the maximum branch value
	*/
	public double getCorrMax() {
		return corrMax;
	}
	public void setCorrMax(double corrMax) {
		this.corrMax = corrMax;
	}

	public TreeDrawerNode getLeaf(int i) {
		if (leafList != null) {
			try {
				return leafList[i];
			} catch (Exception e) {
				System.out.println("Got exception " + e);
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	* Set the data from which to draw the tree
	*
	
	* @param nodeInfo The headers from the node file.
	* There should be one header row per node.
	* There should be a column named "NODEID", "RIGHT", "LEFT" and one of either
	* "CORRELATION" or "TIME".
	
	* @param rowInfo This is the header info for the rows which the ends of the tree are supposed to line up with.
	
	* 
	*/
	public void setData(HeaderInfo nodeInfo, HeaderInfo rowInfo) throws DendroException {
		if (nodeInfo == null) {
			setDefaults();
			return;
		}

		if (this.nodeInfo != null) this.nodeInfo.deleteObserver(this);
		this.nodeInfo = nodeInfo;
		nodeInfo.addObserver(this);
		
		leafList = new TreeDrawerNode[rowInfo.getNumHeaders()];
		id2node = new Hashtable(((nodeInfo.getNumHeaders() * 4) /3)/2, .75f);	
		int nodeIndex = nodeInfo.getIndex("NODEID");
		if (nodeIndex == -1)
			throw new DendroException("Could not find header NODEID in tree header info");
		for (int j = 0; j < nodeInfo.getNumHeaders() ;j++) {
			// extract the things we need from the enumeration
			String newId = nodeInfo.getHeader(j, nodeIndex);
			String leftId = nodeInfo.getHeader(j, "LEFT");
			String rightId = nodeInfo.getHeader(j, "RIGHT");
			// setup the kids
			TreeDrawerNode newn   = (TreeDrawerNode) id2node.get(newId);
			TreeDrawerNode leftn  = (TreeDrawerNode) id2node.get(leftId);
			TreeDrawerNode rightn = (TreeDrawerNode) id2node.get(rightId);
			if (newn != null) {
				System.out.println("Symbol '" + newn + 
				"' appeared twice, building weird tree");
			}
			if (leftn == null) { // this means that the identifier for leftn is a new leaf
				int val; // stores index (y location)
				val = rowInfo.getHeaderIndex(leftId);
				if (val == -1) {
					Thread.dumpStack();
					throw new DendroException("Identifier " + leftId + " from tree file not found in CDT.");
				}
				leftn = new TreeDrawerNode(leftId, 1.0, val);
				leafList[val] = leftn;
				id2node.put(leftId, leftn);
			}
			if (rightn == null) { // this means that the identifier for rightn is a new leaf
				//		System.out.println("Looking up " + rightId);
				int val; // stores index (y location)
				val = rowInfo.getHeaderIndex(rightId);
				if (val == -1) {
					Thread.dumpStack();
					throw new DendroException("Identifier " + rightId + " from tree file not found in CDT!");
				}
				rightn = new TreeDrawerNode(rightId, 1.0, val);
				leafList[val] = rightn;
				id2node.put(rightId, rightn);
			}
			
			
			if (leftn.getIndex() > rightn.getIndex()) {
				TreeDrawerNode swap = leftn;
				leftn = rightn;
				rightn = swap;
			}
			rootNode = new TreeDrawerNode(newId, 0.0, leftn, rightn);
			leftn.setParent (rootNode);
			rightn.setParent(rootNode);
			// finally, insert in tree
			id2node.put(newId, rootNode);
		}
		setBranchHeights(nodeInfo, rowInfo);
		setChanged();
	}
	
	/** type of header which can be used to set branch heights */
	public static final int CORRELATION = 0;
	/** type of header which can be used to set branch heights */
	public static final int TIME = 1;
	/** allow for a little arithmetic error */
	public static final double EPSILON = .00000001;
	public void setBranchHeights(HeaderInfo nodeInfo, HeaderInfo rowInfo) {
		if (rootNode == null) return;
		int nameIndex = nodeInfo.getIndex("TIME");
		int type = TIME;
		if (nameIndex == -1) {
			nameIndex = nodeInfo.getIndex("CORRELATION");
			type = CORRELATION;
		}
		setBranchHeightsIter(nodeInfo, nameIndex, type, rootNode);

		// set branch heights for leaf nodes...
		if (type == CORRELATION) {
			setCorrMin(rootNode.getMinCorr());
			setCorrMax(1.0);
			for (int i = 0; i < leafList.length; i++) {
				if (leafList[i] != null)
					leafList[i].setCorr(getCorrMax());
			}
		} else {
			for (int i = 0; i < leafList.length; i++) {
				double leaf = rootNode.getCorr();
				try {
					leaf = parseDouble(rowInfo.getHeader((int) leafList[i].getIndex(), "LEAF"));
				} catch (Exception e) {
				}
				leafList[i].setCorr(leaf);
			}
			setCorrMin(rootNode.getMinCorr());
			setCorrMax(rootNode.getMaxCorr());

			for (int i = 0; i < leafList.length; i++) {
				// similar to the correlation case, makes the leaves extend all the way to the end.
				double leaf = getCorrMax();
				try {
					leaf = parseDouble(rowInfo.getHeader((int) leafList[i].getIndex(), "LEAF"));
				} catch (Exception e) {
				}

				leafList[i].setCorr(leaf);

				// This would set the leaf's branch length to be the same as it's parent...
				// leafList[i].setCorr(leafList[i].getParent().getCorr());

				// this makes the leaf end at the midpoint of the previous two.
				//leafList[i].setCorr((getCorrMax() + leafList[i].getParent().getCorr()) / 2);

			}
		}
	}
	public void setBranchHeightsIter(HeaderInfo nodeInfo, int nameIndex, int type, TreeDrawerNode start) {
		Stack remaining = new Stack();
		remaining.push(start);
		while (remaining.empty() == false) {
			TreeDrawerNode current = (TreeDrawerNode) remaining.pop();
			if (current.isLeaf()) {
				// will get handled in a linear-time routine...
			} else {
				int j = nodeInfo.getHeaderIndex(current.getId());
				Double d = new Double(nodeInfo.getHeader(j)[nameIndex]);
				double corr = d.doubleValue();
				if (type == CORRELATION) {

					// Account for a litle arithmetic fudge
					if (corr > 1.0)
						corr = 1.0;
					else if (corr < -1.0)
						corr = -1.0;

					// if ((corr  < -1.0) || (corr > 1.0)) {
					// 	System.out.println("Got illegal correlation " + corr + " at line "+j);
					// }
					current.setCorr(corr);
				} else {
					current.setCorr(corr);
				}
				remaining.push(current.getLeft());
				remaining.push(current.getRight());
			}
		}
	}
	
	/** 
	* Draw the tree on a given graphics object, given a particular source
	* and destination.  Can specify a node to drawn as selected, or not.
	
	* @param graphics     The graphics object to draw on
	* @param xScaleEq      Equation describing mapping from pixels to index
	* @param yScaleEq      Equation describing mapping from pixels to index
	* @param dest         Specifies Rectangle of pixels to draw to
	* @param selected     A selected node
	* 
	* the actual implementation of this depends, of course, on the orientation of the tree.
	*/
	abstract public void paint(Graphics graphics, LinearTransformation xScaleEq, 
	LinearTransformation yScaleEq, Rectangle dest, 
	TreeDrawerNode selected);
	
	/**
	* Get the closest node to the given (index, correlation) pair.
	*
	*/
	public TreeDrawerNode getClosest(double index, double corr, double weight) {
		if (rootNode == null) return null;
		IterativeClosestFinder rcf = new IterativeClosestFinder(index, corr, weight);
		return rcf.find(rootNode);
	}
	/**
	 *  Get node by Id
	 *  returns null if no matching id
	 */
	public TreeDrawerNode getNodeById(String id) {
		if (id == null) return null;
		return (TreeDrawerNode)id2node.get(id);
 	}
	
	/**
	* this is an internal helper class which does a sort of recursive
	* search, but doesn't blow stack quite as much as a recursive
	* function
	
	* @author Alok Saldanha <alok@genome.stanford.edu>
	* @version Alpha
	*/
	class IterativeClosestFinder {
		
		/**
		* The constructor sets the variables
		*
		* @param ind        The index for which to search
		* @param corr  The correlation for which to search
		* @param wei        The relative weight to assign to correlation. The distance function will be sqrt((delta(corr) * weight) ^2 + (delta(index))^2)
		*/
		public IterativeClosestFinder(double ind, double corr, 
		double wei) {
			index = ind;
			correlation = corr;
			weight = wei;
		}
		
		/** 
		* the find method actually finds the node
		*/
		public TreeDrawerNode find(TreeDrawerNode startNode) {
			if (startNode.isLeaf())
				return startNode;
			TreeDrawerNode closest = startNode;
			
			// some stack allocation...
			Stack remaining = new Stack();
			remaining.push(startNode);
			while (remaining.empty() == false) {
				
				TreeDrawerNode testN = (TreeDrawerNode) remaining.pop();

				if (testN.getDist(index, correlation, weight) < closest.getDist(index,correlation,weight))
					closest = testN;
				
				// lots of stack allocation...
				TreeDrawerNode left = testN.getLeft();
				TreeDrawerNode right = testN.getRight();
				if (left.isLeaf() == false) remaining.push(left);
				if (right.isLeaf() == false) remaining.push(right);
			}
						
			return closest;
		}
		private double index, correlation, weight;
	}
	
	
	private void setDefaults() {
		id2node = null;
		rootNode = null;
		leafList = null;
		if (nodeInfo != null) nodeInfo.deleteObserver(this);
		nodeInfo = null;
		setChanged();
	}
	
	private TreeDrawerNode [] leafList;
	private TreeDrawerNode rootNode;
	private Hashtable id2node;
	
	public static double parseDouble (String string) {
			Double val = Double.valueOf(string);
	    return val.doubleValue();	
	}
}
