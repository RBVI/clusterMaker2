/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: rqluk $
 * $RCSfile: NullMap.java,v $
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


/* Decompiled by Mocha from NullMap.class */
/* Originally compiled from NullMap.java */

public class NullMap extends IntegerMap
{
    public int getIndex(int i)
    {
        return 0;
    }

    public int getPixel(int i)
    {
        return 0;
    }

    public double getScale()
    {
        return 0.0;
    }

    public int getUsedPixels()
    {
        return 0;
    }

    public int getViewableIndexes()
    {
        return 0;
    }

    public String type()
    {
        return "Null";
    }
}
