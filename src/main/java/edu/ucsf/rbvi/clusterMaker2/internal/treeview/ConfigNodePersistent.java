/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: ConfigNodePersistent.java,v $
 * $Revision: 1.5 $
 * $Date: 2004/12/21 03:28:13 $
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
 *  Defines an interface by which objects can be bound to ConfigNodes.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    $Revision: 1.5 $ $Date: 2004/12/21 03:28:13 $
 */
public interface ConfigNodePersistent {
	/**
	 *  Should bind implementing object to suppled confignode. As it is bound, the object
	 *  should change its state information to match that in the confignode. Furthermore,
	 *  once bound it should store all its state information in the confignode, so as
	 *  to maintain persistence across runs.
	 *
	 * @param  configNode  config node to bind to.
	 */
	public void bindConfig(ConfigNode configNode);
}

