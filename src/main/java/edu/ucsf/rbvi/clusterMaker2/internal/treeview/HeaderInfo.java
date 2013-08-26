/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: HeaderInfo.java,v $
 * $Revision: 1.12 $
 * $Date: 2005/11/25 07:24:08 $
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

import java.util.Observer;


/**
 * Interface to access header info about genes or arrays or treenodes
 * This interface is used many ways. The basic idea is that the "Header" refers
 * to which gene, array, or node you want information about, whereas the "Name"
 * is which header you want. Thus, getNumHeaders() is the number of genes,
 * whereas getNumNames() is the number of headers for each gene.
 *
 * Conceptually, the objects that are annotated (genes, arrays, nodes) can be
 * thought of as rows, and the various names as the headers of columns of information about them.
 * For historical reasons, the actual annotations are called the headers, and the column headers
 * are called names (i.e. names of the annotation). This is because the first HeaderInfo
 * objects represented subtables of the CDT file.
 *
 * @author     Alok Saldanha <alok@genome.stanford.edu>
 * @version $Revision: 1.12 $ $Date: 2005/11/25 07:24:08 $
 */
public interface HeaderInfo {
	/**
	 *  Gets the header info for gene/array/node i
	 *
	 * @param  i  index of the gene/array/node for which to get headers
	 * @return    The array of header values
	 */
	public String[] getHeader(int i);

	/**
	 *  Gets the header info for gene/array/node i, col name
	 *
	 * @param  i  index of the gene/array/node for which to get headers
	 * @param  name  name of the header to get
	 * @return    header value
	 */
	public String getHeader(int i, String name);


	/**
	 *  Gets the names of the headers
	 *
	 * @return    The array of header names
	 */
	public String[] getNames();

	/**
	* The number of headers.
	*/
	public int getNumNames();
	/**
	 * Gets the number of sets of headers. This will generally be the number things which have headers, i.e. number of genes/arrays/nodes.
	 */
	public int getNumHeaders();

	/**
	 *  Gets the index associated with a particular header name.
	 *
	 * usually, getIndex(getNames() [i]) == i.
	 *
	 * Note that some header info classes may have special ways of mapping
	 * names to indexes, so that the getNames() array at the returned index
	 * may not actually match the name argument. This is particularly true for
	 * fields like YORF, which may also be UID, etc...
	 *
	 * Should have been called "getNameIndex".
	 *
	 * @param  name  A name to find the index of
	 * @return       The index value
	 */
	public int getIndex(String name);

	/**
	* gets the index of a gene/array/node given a value from the first column (the id column). Should have been called "getIndexById" or something.
	*
	* @param  id	a particular id for a gene or array or node
	* @return       The index value, for use with getHeader() or similar thing. Returns -1 if no header matching "id" can be found.
	*/
	public int getHeaderIndex(String id);
	
	/**
	 * This is used by HeaderInfo objects that may change over time.
	 * If your HeaderInfo is static, you can just make this a noop.
	 * 
	 * @param o
	 */
	public void addObserver(Observer o);
	
	/**
	 * This is used by HeaderInfo objects that may change over time.
	 * If your HeaderInfo is static, you can just make this a noop.
	 * 
	 * @param o
	 */
	public void deleteObserver(Observer o);

	/**
	 * Adds a new named "column" of headers to this object
	 * Just return false if your header info is read only.
	 * 
	 * @param name name of column to add
	 * @param location 0 means make it first, getNumNames() means make it last
	 * @return true if successfully added, false if not.
	 */
	public boolean addName(String name, int location);
	
	/**
	 * Sets indicated header to specified value
	 * Just return false if your header info is read only.
	 * 
	 * @param name name of column to change
	 * @param value new value for header.
	 * 
	 * @return true if successfully modified, false if not.
	 */
	public boolean setHeader(int i, String name, String value);
	
	/**
	 * @return true if the HeaderInfo has been modified since last save
	 */
	public boolean getModified();
	
	/**
	 * should only be called externally after HeaderInfo has been saved to disk.
	 * @param mod false if no longer out of synch with disk.
	 */
	public void setModified(boolean mod);

	/**
	 * lookup by row and column, which should correspond to position in the names array.
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	public String getHeader(int rowIndex, int columnIndex);
	
}

