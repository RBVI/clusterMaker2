/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: TreeViewFrame.java,v $w
 * $Revision: 1.69 $
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

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Observable;

import javax.swing.*;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.dendroview.KnnDendroView;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.model.KnnViewModel;

/**
 * This class is the main window of java treeview.
 * In practice, it serves as the base class for the LinkedViewFrame and
 * the AppletViewFrame.
 * 
 * @author aloksaldanha
 *
 */
public class KnnViewFrame extends TreeViewFrame {

	public KnnViewFrame(TreeViewApp treeview) {
		super(treeview);
	}

	public KnnViewFrame(TreeViewApp treeview, String appName) {
		super(treeview, appName);
	}

	protected void setupRunning() {
		KnnDendroView dv = new KnnDendroView(getDataModel(), this);
		running = dv;
	}
}
