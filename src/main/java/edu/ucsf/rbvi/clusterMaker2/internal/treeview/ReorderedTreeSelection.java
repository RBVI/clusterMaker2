package edu.ucsf.rbvi.clusterMaker2.internal.treeview;

import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

/**
 * represents a reordered version of the underlying TreeSelectionI
 * @author Alok@caltech.edu
 *
 */
public class ReorderedTreeSelection extends Observable implements TreeSelectionI, Observer {
	private TreeSelectionI parent;
	private int [] reorderedIndex;
	public ReorderedTreeSelection(TreeSelectionI p, int [] ri) {
		parent = p;
		reorderedIndex = ri;
		p.addObserver(this);
	}
	
	public void resize(int nIndex) {
		parent.resize(nIndex);
	}

	public void deselectAllIndexes() {
		parent.deselectAllIndexes();
	}

	public void selectAllIndexes() {
		parent.selectAllIndexes();
	}

	public void setIndex(int i, boolean b) {
		if (i >= reorderedIndex.length) i = reorderedIndex.length-1;
		if (i < 0) i = 0;
		int index = reorderedIndex[i];
		if (index != -1)
			parent.setIndex(index, b);
	}

	public boolean isIndexSelected(int i) {
		int index = reorderedIndex[i];
		if (index != -1)
			return parent.isIndexSelected(index);
		return false;
	}

	public int getMinIndex() {
		int start = parent.getMinIndex();
		if (start == -1) return -1;
		for (int i = 0; i < reorderedIndex.length; i++)
			if (reorderedIndex[i] == start)
				return i;
		return -1;
	}

	public int[] getSelectedIndexes() {
		return parent.getSelectedIndexes();
	}

	public int getMaxIndex() {
		int stop = parent.getMaxIndex();
		if (stop == -1) return -1;
		for (int i = reorderedIndex.length-1; i >= 0; i--)
			if (reorderedIndex[i] == stop)
				return i;
		return -1;
	}

	public int getNumIndexes() {
		return reorderedIndex.length;
	}

	public void selectIndexRange(int min, int max) {
		while ((reorderedIndex[min] == -1) && (min < reorderedIndex.length))
			min++;
		while ((reorderedIndex[max] == -1) && (max > 0))
			max--;
		if ((max >= 0) && (min <= reorderedIndex.length))
			parent.selectIndexRange(reorderedIndex[min], reorderedIndex[max]);
	}

	public int getNSelectedIndexes() {
		return parent.getNSelectedIndexes();
	}

	public void setSelectedNode(String n) {
		parent.setSelectedNode(n);
	}

	public String getSelectedNode() {
		return parent.getSelectedNode();
	}

	public void notifyObservers() {
		parent.notifyObservers();
	}
	
	private Vector observers = new Vector();

	public void update(Observable arg0, Object arg1) {
		Enumeration e = observers.elements();
		while (e.hasMoreElements())
			((Observer)e.nextElement()).update(this,arg1);
	}

	public void addObserver(Observer view) {
		observers.addElement(view);
	}

	public void deleteObserver(Observer view) {
		observers.remove(view);
	}

}
