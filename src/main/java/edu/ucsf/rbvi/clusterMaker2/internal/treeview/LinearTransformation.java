/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: LinearTransformation.java,v $
 * $Revision: 1.3 $
 * $Date: 2004/12/21 03:28:14 $
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
 * this class encapuslates a linear equation.
 * I probably could have named it line equation or something...
 *
 */
public class LinearTransformation {
	// y = mx + b
	private double m = 0.0;
	private double b = 0.0;    
	// for the inverse transformation
	private double mi = 0.0;
	private double bi = 0.0;    
	public double getSlope() {
		return m;
	}

	public LinearTransformation (double fromX, double fromY, double toX, double toY) {
		setMapping(fromX, fromY, toX, toY);
		/*	
		System.out.println("New line y = " + m + " x + " + b);
		System.out.println("from (" + fromX + ", " + fromY + "), (" + toX+ ", " + toY + ")");
		*/
	}

	public LinearTransformation () { /* default, map everything to 0.0 */ }

	public void setMapping(double fromX, double fromY, double toX, double toY) {
		m = (toY - fromY)/ (toX - fromX);	
		b = (fromY*toX - toY*fromX) / (toX - fromX);
		mi = (toX - fromX) / (toY - fromY);
		bi = (fromX*toY - toX*fromY) / (toY - fromY);
	}

	public double transform(double y) {
		return (m * y + b);
	}

	public double inverseTransform(double y) {
		return (mi * y + bi);
	}
}
