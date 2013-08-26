	/* BEGIN_HEADER											  Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: MapContainer.java,v $
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

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JScrollBar;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNodePersistent;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeDrawerNode;

/**
* MapContainers tell the views which pixel offset to draw each array or gene index at.
* the scrollbars "scroll" by communicating with the maps.
* 
* This is distinct from which genes are selected (see the TreeSelection object)
*/
public class MapContainer extends Observable implements Observer, AdjustmentListener,
						 ConfigNodePersistent {
	private String default_map = "Fixed";
	private double default_scale = 10.0;
	private IntegerMap current = null;

	private FixedMap fixedMap = null;
	private FillMap fillMap = null;
	private NullMap nullMap = null;

	private JScrollBar scrollbar = null;
	private TreeDrawerNode selected = null;
	private ConfigNode root = null;
	
	public MapContainer() {
	  fixedMap = new FixedMap();
	  fillMap = new FillMap();
	  nullMap = new NullMap();
	  current = nullMap;
	}
	public MapContainer(String type) {
		this();
		setMap(type);
	}
	private ConfigNode fetchOrCreateNode(String name) {
	  ConfigNode ret = root.fetchFirst(name);
	  if (ret == null)
		ret = root.create(name);
	  return ret;
	}
	// confignode persistent
	public void bindConfig(ConfigNode configNode)
	{
		root = configNode;
	  // first bind subordinate maps...
	  fixedMap.bindConfig(fetchOrCreateNode("FixedMap"));
	  fillMap.bindConfig(fetchOrCreateNode("FillMap"));
	  nullMap.bindConfig(fetchOrCreateNode("NullMap"));
	  
	  // then, fix self up...
		setMap(root.getAttribute("current", default_map));
	}
	
	public void setDefaultScale(double d) {
		default_scale = d;
		fixedMap.setDefaultScale(d);
	}

	public void recalculateScale() {
	  if (root.fetchFirst("FixedMap").hasAttribute("scale")) {
		if (getScale() < getAvailablePixels())
			return;
	  }
	  int range = getMaxIndex() - getMinIndex() + 1;
	  double requiredScale = getAvailablePixels() /range;
	  if (requiredScale > default_scale) {
		setScale(requiredScale);
	  }  else {
		setScale(default_scale);
	  }
	}

	public void setScrollbar(JScrollBar scrollbar) {
		if (this.scrollbar != null) {
			this.scrollbar.removeAdjustmentListener(this);
		}
		this.scrollbar = scrollbar;
		if (this.scrollbar != null) {
			this.scrollbar.addAdjustmentListener(this);
			setupScrollbar();
		}
	}

	public IntegerMap setMap(String string)
	{
		if (current.type().equals(string))
		  return current;
		 
		IntegerMap newMap = null;
		if (nullMap.type().equals(string)) {
//		  System.out.println("type " + string + " is nullMap");
		  newMap = nullMap;
		}
		if (fillMap.type().equals(string)) {
//		  System.out.println("type " + string + " is fillMap");
		  newMap = fillMap;
		}
		if (fixedMap.type().equals(string)) {
//		  System.out.println("type " + string + " is fixedMap");
		  newMap = fixedMap;
		}
		if (newMap == null) {
			/*
		  CyLogger.getLogger(MapContainer.class).warn("Couldn't find map matching " + string + " in MapContainer.java");
		  CyLogger.getLogger(MapContainer.class).warn("Choices include");
		  CyLogger.getLogger(MapContainer.class).warn(nullMap.type());
		  CyLogger.getLogger(MapContainer.class).warn(fixedMap.type());
		  CyLogger.getLogger(MapContainer.class).warn(fillMap.type());
		  */
		  newMap = fixedMap;
		}

		switchMap(newMap);
		return current;
	}

	/*					  Scrollbar Functions				   */
	public void scrollToIndex(int i) {
		int j = scrollbar.getValue();
		scrollbar.setValue(i - scrollbar.getVisibleAmount() / 2);
		if (j != scrollbar.getValue())
			setChanged();
	}

	public void adjustmentValueChanged(AdjustmentEvent adjustmentEvent) {
		setChanged();
		notifyObservers(scrollbar);
	}

	private void setupScrollbar() {
	  if (scrollbar != null) {
		  int value = scrollbar.getValue();
		  int extent = current.getViewableIndexes();
		  int max = current.getMaxIndex() - current.getMinIndex() + 1;
		  if (value + extent > max) value = max - extent;
		  if (value < 0) value = 0;
		  scrollbar.setValues(value, extent, 0, max);
		  scrollbar.setBlockIncrement(current.getViewableIndexes());
	  }
	 }

	/** 
	 * expect to get updates from selection only
	 */
	public void update(Observable observable, Object object)
	{
		System.out.println(new StringBuffer("MapContainer Got an update from unknown ").append(observable).toString());
		notifyObservers(object);
	}

	public void underlyingChanged()
	{
	setupScrollbar();
		setChanged();
	}

	public boolean contains(int i)
	{
	return current.contains(i);
	}

	/*					  Mapping Functions					  */	
	
	// forward all map operations...
	public double getScale() {
		return current.getScale();
	}	
	public int getPixel(double d) {
	  int offset = 0;
	  if (scrollbar != null) offset = scrollbar.getValue();
	  return current.getPixel(d - offset);
	}
	public int getPixel(int i) {
	  int offset = 0;
	  if (scrollbar != null) offset = scrollbar.getValue();
	  return current.getPixel(i - offset);
	}
	public int getIndex(int pix) {
	  int index =0;
	  if (current != null)
		index = current.getIndex(pix);
	  if (scrollbar != null)
		index += scrollbar.getValue();
		return  index;
	}
	public boolean isVisible(int i) {
			int min = getIndex(0);
			int max = getIndex(getAvailablePixels());
			if (i < min ) return false;
			if (i > max) return false;
			return true;
	}
	// {return current.getPixel(intval);}

	public int getRequiredPixels() {
		return current.getRequiredPixels();
	}

	public int getUsedPixels()
	{
		return current.getUsedPixels();
	}

	public void setAvailablePixels(int i) {
		int j = current.getUsedPixels();
		current.setAvailablePixels(i);
		setupScrollbar();
		if (j != current.getUsedPixels())
			setChanged();
	}

	public void setIndexRange(int i, int j) {
		if (i > j) {
			int k = i;
			i = j;
			j = k;
		}
		if (current.getMinIndex() != i || current.getMaxIndex() != j) {
			current.setIndexRange(i, j);
			setupScrollbar();
			setChanged();
		}
	}

	public void setScale(double d)
	{
			if (fixedMap.getScale() != d) {
			fixedMap.setScale(d);
			setupScrollbar();
			setChanged();
		}
	}


	public int getMiddlePixel(int i) {
		return (getPixel(i) + getPixel(i + 1)) / 2;
	}

	public int getMaxIndex() {
		return current.getMaxIndex();
	}
	public int getMinIndex() {
		return current.getMinIndex();
	}

	public TreeDrawerNode getSelectedNode()  {
		return selected;
	}

	public void setSelectedNode(TreeDrawerNode treeDrawerNode) {
		if (selected != treeDrawerNode) {
		/*
		  System.out.println("setindexrange called, start = " + selected);
		  Throwable t = new Throwable();
		  t.printStackTrace();
		*/
			selected = treeDrawerNode;
			setChanged();
		}
	}

	public IntegerMap getCurrent() {
		return current;
	}
	public int getAvailablePixels() {
	return current.getAvailablePixels();
	}

	private void switchMap(IntegerMap integerMap) {
	  if (current != integerMap) {
		if (root != null) {
		  root.setAttribute("current", integerMap.type(), default_map);
		}
		integerMap.setAvailablePixels(current.getAvailablePixels());
		integerMap.setIndexRange(current.getMinIndex(), current.getMaxIndex());
		current = integerMap;
		setupScrollbar();
		setChanged();
	  }
	}

}
