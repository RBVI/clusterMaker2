/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: DataModel.java,v $
 * $Revision: 1.17 $
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

import java.awt.MenuItem;
import java.awt.event.WindowListener;
/**
 *  This file defines the bare bones of what needs to implemented by a data model
 *  which wants to be used with a ViewFrame and some ModelViews.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version    @version $Revision: 1.17 $ $Date: 2007/02/03 04:58:36 $
 */

public interface DataModel {
    public final static double NODATA = -10000000;
	public final static double EMPTY = -20000000;

	/**
	 *  Gets the documentConfig attribute of the DataModel object
	 *
	 * values stored in the <code>ConfigNode</code>s of this <code>XmlConfig</code> should be persistent across multiple openings of this DataModel.
	 *
	 *<p>
	 *  Of course, if you don't care about persistence you could subclass XmlConfig to create one which doesn't store things to file.
	 *
	 * @return    The documentConfig value
	 */
	public ConfigNode getDocumentConfig();


	/**
	 *  Gets the file path or url which this <code>DataModel</code> was built from.
	 *
	 * @return    String representation of file path or url
	 */
	public String getSource();

	/**
	 *  Gets a short name, unique for this <code>DataModel</code>, suitable for putting in a windo menu.
	 *
	 * @return    Short name of data model.
	 */
	public String getName();

	
	/**
	 * Sets an data model to be compare to this model.
	 * @param dm The data model.
	 */
	public void setModelForCompare(DataModel dm);


	/**
	 *  Gets the HeaderInfo associated with genes for this DataModel.
	 *
	  * There are two special indexes, YORF and NAME, which mean the unique id column and the description column, respectively. See TVModel.TVModelHeaderInfo for details.
	  */
	public HeaderInfo getGeneHeaderInfo();


	/**
	 *  Gets the HeaderInfo associated with arrays for this DataModel.
	 */
	public HeaderInfo getArrayHeaderInfo();

	/**
	 *  Gets the HeaderInfo associated with gene tree for this DataModel.
	 *
	  * There are two special indexes, YORF and NAME, which mean the unique id column and the description column, respectively. See TVModel.TVModelHeaderInfo for details.
	  */
	public HeaderInfo getGtrHeaderInfo();


	/**
	 *  Gets the HeaderInfo associated with array tree for this DataModel.
	 */
	public HeaderInfo getAtrHeaderInfo();


	/**
	 *  This not-so-object-oriented hack is in those rare instances where it is not
	 *  enough to know that we've got a DataModel.
	 *
	 * @return    a string representation of the type of this <code>DataModel</code>
	 */
	public String getType();


	/**
	* returns the datamatrix which underlies this data model,
	* typically the matrix of measured intensity ratios.
	*/
	public DataMatrix getDataMatrix();
	
	
	void append(DataModel m);

	/**
	 * Removes the previously appended DataMatrix.
	 *
	 */
	void removeAppended();


	/**
	 * @return
	 */
	public boolean aidFound();


	/**
	 * @return
	 */
	public boolean gidFound();


	/**
	 * @return true if data model has been modified since last save to source.
	 * always returning false is generally a safe thing, if you have an
	 * immutable data model.
	 */
	public boolean getModified();

	/**
	 * 
	 * @return true if data model has been sucessfully loaded.
	 */
	public boolean isLoaded();

	/**
	 * 
	 * @return true if data model represents a symmetrical array
	 */
	public boolean isSymmetrical();
}

