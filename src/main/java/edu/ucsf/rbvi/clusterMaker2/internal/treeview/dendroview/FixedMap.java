/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: FixedMap.java,v $
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

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;

/**
 *  Maps integers (gene index) to pixels using a fixed scale
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version $Revision: 1.1 $ $Date: 2006/08/16 19:13:45 $
 */
public class FixedMap extends IntegerMap {
	private double default_scale;
	private double scale;


	/**
	 *  constructs new FixedMap
	 */
	public FixedMap() {
		default_scale = 10.0;
	}


	/**
	 *  For persistance of scale, bind to a ConfigNode
	 *
	 * @param  configNode  ConfigNode to bind to
	 */
	public void bindConfig(ConfigNode configNode) {
		super.bindConfig(configNode);
		scale = root.getAttribute("scale", default_scale);
	}


	/**
	 *  Gets the index for a particular pixel.
	 *
	 * @param  i  the pixel value
	 * @return    The index value
	 */
	public int getIndex(int i) {
		return (int) ((double) i / scale) + minindex;
	}


	/**
	 *  Gets the pixel for a particular index
	 *
	 * @param  i  The index value
	 * @return    The pixel value
	 */
	public int getPixel(int i) {
		return (int) ((double) (i - minindex) * scale);
	}


	/**
	 * @return    The effective scale for the current FillMap
	 */
	public double getScale() {
		return scale;
	}


	/**
	 * @return    The number of pixels currently being used
	 */
	public int getUsedPixels() {
		if (minindex == -1) {
			return 0;
		}
		int i  = (int) ((double) (maxindex - minindex + 1) * scale);
		int j  = (int) (scale * (int) ((double) availablepixels / scale));
		if (i > j) {
			return j;
		} else {
			return i;
		}
	}


	/**
	 * @return    The number of indexes currently visible
	 */
	public int getViewableIndexes() {
	int i  = (int) ((double) availablepixels / scale);
		return i;
	}


	/**
	 *  Sets the defaultScale attribute of the FixedMap object
	 *
	 * @param  d  The new defaultScale value
	 */
	public void setDefaultScale(double d) {
		default_scale = d;
	}


	/**
	 *  set scaling value
	 *
	 * @param  d  The new scale value
	 */
	public void setScale(double d) {
		scale = d;
		root.setAttribute("scale", scale, default_scale);
	}


	/**
	 * @return    A short word desribing this type of map
	 */
	public String type() {
		return "Fixed";
	}
}

