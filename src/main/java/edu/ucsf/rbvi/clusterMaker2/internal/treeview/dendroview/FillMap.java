/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: FillMap.java,v $
 * $Revision: 1.1 $
 * $Date: 2006/08/16 19:13:46 $
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
 *  maps integers (gene index) to pixels, filling available pixels
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version $Revision: 1.1 $ $Date: 2006/08/16 19:13:46 $
 */
public class FillMap extends IntegerMap {
	/**
	 *  Gets the index for a particular pixel.
	 *
	 * @param  i  the pixel value
	 * @return    The index value
	 */
	public int getIndex(int i) {
	  	if (availablepixels == 0) return 0;
		return i * (maxindex - minindex + 1) / availablepixels + minindex;
	}


	/**
	 *  Gets the pixel for a particular index
	 *
	 * @param  i  The index value
	 * @return    The pixel value
	 */
	public int getPixel(int i) {
		return (i - minindex) * availablepixels / (maxindex - minindex + 1);
	}


	/**
	 * @return    The effective scale for the current FillMap
	 */
	public double getScale() {
		return (double) availablepixels / (maxindex - minindex + 1);
	}


	/**
	 * @return    The number of pixels currently being used
	 */
	public int getUsedPixels() {
		if (minindex == -1) {
			return 0;
		} else {
			return availablepixels;
		}
	}


	/**
	 * @return    The number of indexes currently visible
	 */
	public int getViewableIndexes() {
		return maxindex - minindex + 1;
	}


	/**
	 * @return    A short word desribing this type of map
	 */
	public String type() {
		return "Fill";
	}
}

