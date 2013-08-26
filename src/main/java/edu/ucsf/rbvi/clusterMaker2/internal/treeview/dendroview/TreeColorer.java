/* BEGIN_HEADER                                              Java TreeView
*
* $Author: alokito $
* $RCSfile: TreeColorer.java,v $
* $Revision: 1.2 $
* $Date: 2007/07/13 02:33:47 $
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


/**
* This class simply colors in trees.
* It's a pretty non-OO class.
*/
import java.awt.Color;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeDrawerNode;
public class TreeColorer {
	private static int colorInd;
	private static String [][] headers; // used when inferring node colors from gene colors
	private static HeaderInfo headerInfo; // used when coloring using column from GTR.
	/**
	 * 
	 * @param rootNode
	 * @param geneHeaderInfo
	 */
	public static void colorUsingHeader(TreeDrawerNode rootNode, HeaderInfo geneHeaderInfo) {
		int index = geneHeaderInfo.getIndex("FGCOLOR");
		if (index < 0) return;
		colorUsingHeader(rootNode, geneHeaderInfo, index);
	}
	/**
	 * colors using header stored in nodes of tree
	 * 
	 * @param root root node of tree
	 * @param h headerInfo of tree
	 * @param ci index into columns of tree's header info
	 */
	public static final synchronized void colorUsingHeader (TreeDrawerNode root, HeaderInfo h, int ci) {
		colorInd = ci;
		headerInfo =h;
		if (headerInfo == null) {
	    // CyLogger.getLogger(TreeColorer.class).warn("TreeColorer: headers null");
			return;
		}
		if (colorInd < 0) {
	    // CyLogger.getLogger(TreeColorer.class).warn("TreeColorer: colorInd < 0");
			return;
		}
		if (root == null) {
	    // CyLogger.getLogger(TreeColorer.class).warn("TreeColorer: root null");
			return;
		}
		recursiveColorUsingHeader(root);
	}
	private static final void recursiveColorUsingHeader(TreeDrawerNode node) {
		//wrong index			
		//String [] headers = headerInfo.getHeader((int) node.getIndex());
		if (node.isLeaf()) {
			return;
		} else {
			int index = headerInfo.getHeaderIndex(node.getId());
			if (index < 0) {
				// CyLogger.getLogger(TreeColorer.class).warn("Problem finding node " +node.getId());
			}
			String [] headers = headerInfo.getHeader(index);
			
			String color = headers[colorInd];
			node.setColor(parseColor(color));
			
			recursiveColorUsingHeader(node.getLeft());
			recursiveColorUsingHeader(node.getRight());
		}
	}

	/**
	 * colors using leaf nodes
	 * @param root
	 * @param h
	 * @param ci
	 */
	public static final synchronized void colorUsingLeaf(TreeDrawerNode root, HeaderInfo h, int ci) {
		colorInd = ci;
		headerInfo =h;
		if (headerInfo == null) {
			// CyLogger.getLogger(TreeColorer.class).warn("headers null");
			return;
		}
		if (colorInd < 0) {
			// LogPanel.println("colorInd < 0");
			return;
		}
		recursiveColorUsingLeaf(root);
	}
	public static final synchronized void colorize (TreeDrawerNode root, String [][] h, int ci) {
		colorInd = ci;
		headers =h;
		if (headers == null) {
			//	    System.out.println("headers null");
			return;
		}
		if (colorInd < 0) {
			//	    System.out.println("colorInd < 0");
			return;
		}
		recursiveColor(root);
	}
	private static final void recursiveColorUsingLeaf(TreeDrawerNode node) {
		if (node.isLeaf()) {
			//	    System.out.println("coloring leaf");
			node.setColor(parseColor(headerInfo.getHeader((int) node.getIndex(),colorInd)));
		} else {
			recursiveColorUsingLeaf(node.getLeft());
			recursiveColorUsingLeaf(node.getRight());
			majorityColor(node);
			//	    node.setColor(synthesizeColor(node.getLeft(), node.getRight()));
		}
	}
	
	private static final void recursiveColor(TreeDrawerNode node) {
		if (node.isLeaf()) {
			//	    System.out.println("coloring leaf");
			node.setColor(parseColor(headers[(int) node.getIndex()][colorInd]));
		} else {
			recursiveColor(node.getLeft());
			recursiveColor(node.getRight());
			majorityColor(node);
			//	    node.setColor(synthesizeColor(node.getLeft(), node.getRight()));
		}
	}
	
	private static String [] colornames = new String[100];
	private static Color [] colors = new Color[100];
	private static final void majorityColor(TreeDrawerNode node) {
		int [] count = new int[100];
		int min = (int) node.getMinIndex();
		int max = (int) node.getMaxIndex();
		for (int i = min; i < max; i++) {
			String color;
			if (headers == null) {
				color = headerInfo.getHeader(i, colorInd);
			} else {
				color = headers[i][colorInd];
			}
			int index = getIndex(color);
			count[index]++;
		}
		// now, get max 
		int maxI = 0;
		for (int i =0; colornames[i] != null; i++) {
			//	    System.out.println("colornames[" + i +"] = "+ colornames[i]);
			if (count[i] > count[maxI]) {
				maxI = i;
			}
		}
		node.setColor(colors[maxI]);
	}
	public static Color getColor(String color) {
		return colors[getIndex(color)];
	}
	private static int getIndex(String color) {
		int i;
		for (i = 0; i< colornames.length; i++) {
			if (colornames[i] == null) {
				break;
			} else if (colornames[i].equals(color)) {
				return i;
			}
		}
		// need to allocate new color
		colornames[i] = color;
		colors[i] = parseColor(colornames[i]);
		return i;
	}
	
	
	private static final Color synthesizeColor
	(TreeDrawerNode left, TreeDrawerNode right) {
		if (left.getColor().equals(right.getColor())) {
			return left.getColor();
		} else {
			return Color.black;
		}
	}
	
	private static final Color parseColor(String colorString) {
		try {
			return Color.decode(colorString); //will this work?
		} catch (Exception e) {
			return Color.gray;
		}
	}
	
}

