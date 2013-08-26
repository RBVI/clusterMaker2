/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: LoadException.java,v $
 * $Revision: 1.6 $
 * $Date: 2007/02/03 04:58:36 $
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


public class LoadException extends Exception {// errors when loading model
    public static final int EXT = 0; // Wrong extension, not cdt
    public static final int CDTPARSE = 1; // cdt parse error
    public static final int ATRPARSE = 2; // atr parse error
    public static final int GTRPARSE = 3; // gtr parse error
    public static final int INTPARSE = 4; // parse interrupted
    public static final int KAGPARSE = 5; // kag parse error
    public static final int KGGPARSE = 6; // kgg parse error
    public static final int NOFILE = 7;   // no file selected
    int type;
    public LoadException(String message, int t) {
	super(message);
	type = t;
    }
    public int getType() {
	return type;
    }
	public String getMessage() {
		return "LoadException " + type + ": " + super.getMessage();
	}
}
