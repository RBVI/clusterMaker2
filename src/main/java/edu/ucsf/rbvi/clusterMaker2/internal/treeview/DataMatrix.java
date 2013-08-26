/*
 *  BEGIN_HEADER                                              Java TreeView
 *
 *  $Author: alokito $
 *  $RCSfile: DataMatrix.java,v $
 *  $Revision: 1.6 $
 *  $Date: 2005/03/06 01:51:41 $
 *  $Name:  $
 *
 *  This file is part of Java TreeView
 *  Copyright (C) 2001-2003 Alok Saldanha, All Rights Reserved. Modified by Alex Segal 2004/08/13. Modifications Copyright (C) Lawrence Berkeley Lab.
 *
 *  This software is provided under the GNU GPL Version 2. In particular,
 *
 *  1) If you modify a source file, make a comment in it containing your name and the date.
 *  2) If you distribute a modified version, you must do it under the GPL 2.
 *  3) Developers are encouraged but not required to notify the Java TreeView maintainers at alok@genome.stanford.edu when they make a useful addition. It would be nice if significant contributions could be merged into the main distribution.
 *
 *  A full copy of the license can be found in gpl.txt or online at
 *  http://www.gnu.org/licenses/gpl.txt
 *
 *  END_HEADER
 */
package edu.ucsf.rbvi.clusterMaker2.internal.treeview;

/**
 *  Description of the Interface
 *
 * Provides a simple interface to the actual gene expression data, using the same indexes as the corresponding HeaderInfo objects.
 *
 
 * @author     aloksaldanha
 */
public interface DataMatrix {
	/**
	 *  Gets the value attribute of the DataMatrix object
	 *
	 * @param  row  row (gene) of interest
	 * @param  col  column (array) of interest
	 * @return      The value at the row/col, or possibly some special "missing data" value, as defined by the constants in DataModel.
	 */
	double getValue(int col, int row);

	/**
	 *  Sets the value attribute of an element in the DataMatrix object
	 *
	 * @param  value  value to be set
	 * @param  row  row (gene) of interest
	 * @param  col  column (array) of interest
	 */
	void setValue(double value, int col, int row);

	/**
	 *  Gets the numRow attribute of the DataMatrix object
	 *
	 * @return    The number of rows (genes) in this data matrix.
	 */
	int getNumRow();
	
	/**
	 * Appends a data matrix to the right of this one. Used for comparison of two data sets.
	 * @param m The DataMatrix being appended.
	 */
	
	/**
	 *  Gets the numCol attribute of the DataMatrix object
	 *
	 * @return    The number of columns (arrays) in this data matrix.
	 */
	int getNumCol();
	
	/**
	 *  Gets the numCol attribute of the DataMatrix object before anything was appended to it.
	 *
	 * @return    The number of columns (arrays) in this data matrix before anything was appended.
	 */
	int getNumUnappendedCol();

	/**
	 * Return the maximum, non-empty value in this matrix. 
	 *
	 * @return    The maximum value
	 */
	double getMaxValue();

	/**
	 * Return the minimum, non-empty value in this matrix. 
	 *
	 * @return    The minimum value
	 */
	double getMinValue();
}

