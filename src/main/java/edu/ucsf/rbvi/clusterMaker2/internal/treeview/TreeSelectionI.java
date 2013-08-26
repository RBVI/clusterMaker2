package edu.ucsf.rbvi.clusterMaker2.internal.treeview;

import java.util.Observer;


public interface TreeSelectionI {

	/**
	 * Resizes the size of the TreeSelection to accomodate more elements. 
	 * @param nIndex - The new size.
	 */
	public abstract void resize(int nIndex);

	// index methods
	/**  calls deselectall on the <code>IntegerSelection</code> for indexes
	 *    Much faster than looping over indexes.
	 */
	public abstract void deselectAllIndexes();

	/**  calls selectall on the <code>IntegerSelection</code> for indexes.
	 *    Much faster than looping over genes.
	 */
	public abstract void selectAllIndexes();

	/**
	 *  sets the selection status for a particular index.
	 *
	 * @param  i  The gene index
	 * @param  b  The new selection status
	 */
	public abstract void setIndex(int i, boolean b);

	/**
	 *  gets the selection status for a particular index.
	 *
	 * @param  i  The gene index
	 * @return  The current selection status
	 */
	public abstract boolean isIndexSelected(int i);

	/**
	 *
	 * @return    The minimum selected index
	 */
	public abstract int getMinIndex();

	public abstract int[] getSelectedIndexes();

	/**
	 *
	 * @return    The maximum selected index.
	 */
	public abstract int getMaxIndex();

	/**
	 * Nice for find boxes which are curious.
	 *
	 * @return    The number of indexes which could be selected. 
	 */
	public abstract int getNumIndexes();

	/**
	 *  Selects a range of indexes.
	 *
	 * @param  min  the minimum gene to select
	 * @param  max  the maximum gene to select
	 */
	public abstract void selectIndexRange(int min, int max);

	/**
	 * @return    The number of selected indexes.
	 */
	public abstract int getNSelectedIndexes();

	// node methods
	/**
	 *  Selects a tree node
	 *
	 * @param  n  Id of node to select
	 */
	public abstract void setSelectedNode(String n);

	/**
	 *  Gets the selected tree node
	 *
	 * @return    Index of selected Node 
	 */
	public abstract String getSelectedNode();

	public abstract void addObserver(Observer view);

	public abstract void notifyObservers();

	public abstract void notifyObservers(Object arg);

	public abstract void deleteObserver(Observer view);

}
