/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: TreeSelection.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/03/20 06:18:43 $
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

import java.util.Observable;

/**
 * A quasi-model independant selection model for leaf indexes as well as internal nodes of  trees.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    @version $Revision: 1.5 $ $Date: 2006/03/20 06:18:43 $
 */
public class TreeSelection extends Observable implements TreeSelectionI {
	private IntegerSelection integerSelection;
	private String selectedNode;


	/**
	 *  Constructor for the TreeSelection object
	 *
	 * @param  nIndex  number of indexes which can be selected
	 */
	public TreeSelection(int nIndex) {
		integerSelection = new IntegerSelection(nIndex);
	}
	
	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#resize(int)
	 */
	public void resize(int nIndex)  {
		IntegerSelection temp = new IntegerSelection(nIndex);
		
		for(int i = 0; i < nIndex; i++)
		{
			if(i < integerSelection.getNSelectable())
			{
				temp.set(i, integerSelection.isSelected(i));
			}
			else
			{
				temp.set(i , false);
			}
		}
		integerSelection = temp;
		setChanged();
	}

	// index methods
	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#deselectAllIndexes()
	 */
	public void deselectAllIndexes() {
		integerSelection.deselectAll();
	}


	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#selectAllIndexes()
	 */
	public void selectAllIndexes() {
		integerSelection.selectAll();
	}


	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#setIndex(int, boolean)
	 */
	public void setIndex(int i, boolean b) {
		integerSelection.set(i, b);
	}


	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#isIndexSelected(int)
	 */
	public boolean isIndexSelected(int i) {
		return integerSelection.isSelected(i);
	}


	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#getMinIndex()
	 */
	public int getMinIndex() {
		return integerSelection.getMin();
	}
	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#getSelectedIndexes()
	 */
	public int [] getSelectedIndexes() {
		return integerSelection.getSelectedIndexes();
	}

	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#getMaxIndex()
	 */
	public int getMaxIndex() {
		return integerSelection.getMax();
	}


	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#getNumIndexes()
	 */
	public int getNumIndexes() {
		return integerSelection.getNSelectable();
	}


	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#selectIndexRange(int, int)
	 */
	public void selectIndexRange(int min, int max) {
		if (min > max) {
			int swap  = min;
			min = max;
			max = swap;
		}
		for (int i = min; i <= max; i++) {
			integerSelection.set(i, true);
		}
	}


	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#getNSelectedIndexes()
	 */
	public int getNSelectedIndexes() {
		return integerSelection.getNSelected();
	}

	// node methods
	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#setSelectedNode(java.lang.String)
	 */
	public void setSelectedNode(String n) {
		if (selectedNode != n) {
			selectedNode = n;
			setChanged();
		}
	}


	/* (non-Javadoc)
	 * @see edu.stanford.genetics.treeview.TreeSelectionI#getSelectedNode()
	 */
	public String getSelectedNode() {
		return selectedNode;
	}


	/**
	* a class to efficiently model a range of integers which can be selected.
	*/
	class IntegerSelection {
		boolean[] isSelected;


		IntegerSelection(int size) {
			isSelected = new boolean[size];
			deselectAll();
		}


		public int getNSelectable() {
			return isSelected.length;
		}


		public int getNSelected() {
			int n  = 0;
			for (int i = 0; i < isSelected.length; i++) {
				if (isSelected[i]) {
					n++;
				}
			}
			return n;
		}
		public int [] getSelectedIndexes() {
			int nSelected = getNSelected();
			int [] indexes = new int [nSelected];
			int curr = 0;
			for (int i = 0; i < isSelected.length; i++) {
				if (isSelected[i]) {
					indexes[curr++] = i;
				}
			}
			return indexes;
		}

		public void deselectAll() {
			TreeSelection.this.setChanged();
			for (int i = 0; i < isSelected.length; i++) {
				isSelected[i] = false;
			}
		}


		public void selectAll() {
			TreeSelection.this.setChanged();
			for (int i = 0; i < isSelected.length; i++) {
				isSelected[i] = true;
			}
		}


		public void set(int i, boolean b) {
			if ((i >= 0) && (i < isSelected.length)) {
				TreeSelection.this.setChanged();
				isSelected[i] = b;
			}
		}


		public boolean isSelected(int i) {
			return isSelected[i];
		}


		public int getMin() {
			int min  = -1;
			for (int i = 0; i < isSelected.length; i++) {
				if (isSelected[i]) {
					return i;
				}
			}
			return min;
		}


		public int getMax() {
			int max  = -1;
			for (int i = 0; i < isSelected.length; i++) {
				if (isSelected[i]) {
					max = i;
				}
			}
			return max;
		}
	}
}

