/* BEGIN_HEADER                                              Java TreeView
 *
 * $Author: alokito $
 * $RCSfile: TVModel.java,v $f
 * $Revision: 1.36 $
 * $Date: 2007/02/03 04:58:37 $
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
package edu.ucsf.rbvi.clusterMaker2.internal.treeview.model;
import java.awt.Frame;
import java.util.*;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataModel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.FileSet;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.LoadException;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.PropertyConfig;

public class TVModel extends Observable implements DataModel {
  /*
   * This not-so-object-oriented hack is in those rare instances
   * where it is not enough to know that we've got a DataModel.
   */
	public String getType() {
		return "TVModel";
	}
	/** has model been successfully loaded? */
	boolean loaded = false;
	/*
	 * For cases where we are comparing two models (this needs to be changed).
	 */
	TVModel compareModel = null;
	int extraCompareExpr = 0;
	
  	public void setModelForCompare(DataModel m)
  	{
  		if(m == null)
  		{
  			compareModel = null;
  			extraCompareExpr = 0;
  		}
  		else
  		{
  			compareModel = (TVModel)m;
  			extraCompareExpr = compareModel.nExpr() + 2;
  		}
  		hasChanged();
  	}
    // accessor methods	
	public HeaderInfo getGeneHeaderInfo() {
	  return geneHeaderInfo;
	}
	public HeaderInfo getArrayHeaderInfo() {
	  return arrayHeaderInfo;
	}
	public DataMatrix getDataMatrix() {
		
		if(compareModel != null)
		{
			
		}
		return dataMatrix;
	}
	public HeaderInfo getAtrHeaderInfo() {
		return atrHeaderInfo;
	}
	public HeaderInfo getGtrHeaderInfo() {
		return gtrHeaderInfo;
	}
    public boolean gweightFound() {
	return gweightFound;
    }

    public int nGene() {return geneHeaderInfo.getNumHeaders();}
    public int nExpr() {return arrayHeaderInfo.getNumHeaders() + extraCompareExpr;}

	public void setExprData(double [] newData) {
		dataMatrix.setExprData(newData);
	}
    
	public double getValue(int x, int y) {
		int nexpr = nExpr();
		int ngene = nGene();
		if(x >= nexpr + 2)
		{
			if (compareModel != null)
				return compareModel.getValue(x - (nexpr + 2), y); // check offsets
		}
		else if(x >= nexpr && y < ngene)
		{
			return 0; // gray border
		}
		if ((x < nexpr && y < ngene) &&	(x >= 0    && y >= 0))
		{
			return dataMatrix.getValue(x, y);
		}
		return NODATA;
    }
    public boolean aidFound() {
	//	System.out.println("aid found called, value " + aidFound);
	return aidFound;
    };
	public void aidFound(boolean newVal) {aidFound = newVal;}
    public boolean gidFound() {return gidFound;};
	public void gidFound(boolean newVal) {gidFound = newVal;}

	public void setSource(FileSet source) {
		this.source = source;
		setChanged();
	}
	public String getSource() {
	  if (source == null) {
		return "No Data Loaded";
	  } else {
		return source.getCdt();
	  }
	}
	public String getName() {
		return getFileSet().getRoot();
	}
	public FileSet getFileSet() {
	  return source;
	}
    public ConfigNode getDocumentConfig() {return documentConfig.getRoot();}
    public void setDocumentConfig(PropertyConfig newVal) { documentConfig = newVal;}
    public TVModel() {
	  super();
	  /* build TVModel, initially empty... */	
	  geneHeaderInfo = new GeneHeaderInfo();
	  arrayHeaderInfo = new IntHeaderInfo();
		atrHeaderInfo = new IntHeaderInfo();
		gtrHeaderInfo = new IntHeaderInfo();
	  dataMatrix = new TVDataMatrix();
    }
    public void setFrame(Frame f) {
	frame = f;
    }
	public Frame getFrame() {
		return frame;
	}

	protected void hashAIDs() {
		arrayHeaderInfo.hashIDs("AID");
	}
	 
	protected void hashGIDs() {
		geneHeaderInfo.hashIDs("GID");
	}
	 
	protected void hashATRs() {
		atrHeaderInfo.hashIDs("NODEID");
	}
	
	protected void hashGTRs() {
		gtrHeaderInfo.hashIDs("NODEID");
	}
	
	protected Hashtable populateHash(HeaderInfo source, String headerName, Hashtable target) {
		int indexCol = source.getIndex(headerName);
		return populateHash(source, indexCol, target);
	}
	 protected Hashtable populateHash(HeaderInfo source, int indexCol, Hashtable target) {
		 if (target == null) {
			 target = new Hashtable((source.getNumHeaders() * 4) /3, .75f);
		 } else {
			 target.clear();
		 }

		 if (indexCol <0) indexCol = 0;
		 for( int i = 0; i < source.getNumHeaders(); i++) {
			 target.put(source.getHeader(i)[indexCol], new Integer(i));
		 }
		 
		 return target;
	 }	 /**
	  * Reorders all the arrays in the new ordering.
	  * @param ordering the new ordering of arrays, must have size equal to number of arrays
	  */
	public void reorderArrays(int [] ordering)
	{
		if(ordering == null || 
				ordering.length != dataMatrix.getNumUnappendedCol()) // make sure input to function makes sense
		{
			return;
		}
	
	
		DataMatrix data = getDataMatrix();
	
		for(int j = 0; j < data.getNumRow(); j++)
		{
			double [] temp = new double[data.getNumUnappendedCol()];
			for(int i = 0; i < ordering.length; i++)
			{
				temp[i] = data.getValue(ordering[i], j);
			}
			for(int i = 0; i < ordering.length; i++)
			{
				data.setValue(temp[i], i, j);
			}
		}
		String [][]aHeaders = arrayHeaderInfo.headerArray;
		String [][] temp2 = new String[aHeaders.length][];
	
		for(int i = 0; i < aHeaders.length; i++)
		{
			if(i < ordering.length)
			{
				temp2[i] = aHeaders[ordering[i]];
			}
			else
			{
				temp2[i] = aHeaders[i];
			}
		}
		setArrayHeaders(temp2);
		hashAIDs();
		
					
		setChanged();
	}
	
	/**
	 * Reset order in config file to the identity ordering.
	 */
	public void clearOrder()
	{
		ConfigNode order = documentConfig.getNode("ArrayOrder");

		for(int i = 0; i < arrayHeaderInfo.getNumHeaders(); i++)
		{
			order.setAttribute("Position" + i, i, i);
		}		
	}
	
	
	/**
	 * Save a reordering to the config node.
	 * @param ordering the ordering to save
	 */
	public void saveOrder(int [] ordering)
	{
		ConfigNode order = documentConfig.getNode("ArrayOrder");
			
		int prevVal = 0;
		int [] temp = new int[arrayHeaderInfo.getNumHeaders()];
		
		for(int i = 0; i < dataMatrix.getNumUnappendedCol(); i++)
		{
			prevVal = order.getAttribute("Position" + ordering[i], -1);
			if(prevVal == -1)
			{
				temp[i] = ordering[i];
			}
			else
			{
				temp[i] = prevVal;
			}
		}
		
		//System.out.print("Saved ordering:  ");
		for(int i = 0; i < dataMatrix.getNumUnappendedCol(); i++)
		{
			order.setAttribute("Position" + i, temp[i], -1);
		//	System.out.print(order.getAttribute("Position" + i, -1) + " ");
		}
		//System.out.println();
		
	}
	
	/**
	 * Load array order from the config node.
	 */
	public void loadOrder()
	{
		if(documentConfig == null)
		{
			return;
		}
		
		ConfigNode order = documentConfig.getNode("ArrayOrder");
		
		int [] ordering = new int[dataMatrix.getNumUnappendedCol()];
		
		int prevVal = 0;
		
		
		//System.out.print("Loaded ordering:  ");
		for(int i = 0; i < ordering.length; i++)
		{
			ordering[i] = order.getAttribute("Position" + i, i);
		//	System.out.print(ordering[i] + " ");
		}
		//System.out.println();
		
		reorderArrays(ordering);
	}
	 
	 public void resetState () {
		 // reset some state stuff.
		 //	if (documentConfig != null)
		 //          documentConfig.store();
		 documentConfig = null;
		 setLoaded(false);
		 aidFound = false;
		 gidFound = false;
		 source = null;
		 
		 eweightFound = false;
		 gweightFound = false;
		 
		 geneHeaderInfo.clear();
		 arrayHeaderInfo.clear();
		 atrHeaderInfo.clear();
		 gtrHeaderInfo.clear();
		 dataMatrix.clear();
	 }
	 
	 public String toString() {
		 String [] strings = toStrings();
		 String msg = "";
		 for (int i = 0; i < strings.length; i++) {
			 msg += strings[i] + "\n";
		 }
		 return msg;
	 }
    public String[] toStrings() {
	String[] msg = {"Selected TVModel Stats",
			"Source = " + getSource(),
			"Nexpr   = " + nExpr(),
			"NGeneHeader = " + getGeneHeaderInfo().getNumNames(),
			"Ngene   = " + nGene(),
			"eweight  = " + eweightFound,
			"gweight  = " + gweightFound,
			"aid  = " + aidFound,
			"gid  = " + gidFound};

	/*
	Enumeration e = genePrefix.elements();
	msg += "GPREFIX: " + e.nextElement();
	for (; e.hasMoreElements() ;) {
	    msg += " " + e.nextElement();
	}

	e = aHeaders.elements();
	msg += "\naHeaders: " + e.nextElement();
	for (; e.hasMoreElements() ;) {
	    msg += ":" + e.nextElement();
	}
	*/

	return msg;
    }
    /*
    // debug functions
    private String commonEscapes() {
	String err = "Common escapes\n";
	err += "ttype TT_EOL = " + FlatFileStreamTokenizer.TT_EOL;
	err += " ttype TT_EOF = " + FlatFileStreamTokenizer.TT_EOF;
	err += " ttype TT_NUMBER = " + FlatFileStreamTokenizer.TT_NUMBER;
	err += " ttype TT_WORD = " + FlatFileStreamTokenizer.TT_WORD;
	err += " '\t' = " + '\t';
	err += " '\n' = " + '\n';
	err += " '\r' = " + '\r';

	return err;
    }
    
    private void printStream(FlatFileStreamTokenizer st) throws IOException {
	int tt = st.nextToken();
	while (tt != st.TT_EOF) {
	    String msg;
	    switch(tt) {
	    case FlatFileStreamTokenizer.TT_WORD:
		msg = "Word: " + st.sval; break;
	    case FlatFileStreamTokenizer.TT_NUMBER:
		msg = "Number: " + st.nval; break;
	    case FlatFileStreamTokenizer.TT_EOL:
		msg = "EOL:"; break;
	    case FlatFileStreamTokenizer.TT_NULL:
		msg = "NULL:"; break;
	    default:
		msg = "INVALID TOKEN, tt=" + tt; break;
	    }
	    System.out.println(msg);
	    tt = st.nextToken();		    
	}
    }
    */
    
    public void removeAppended()
	{
		if(appendIndex == -1)
		{
			return;
		}
		int ngene = nGene();
		int nexpr = nExpr();
		double [] temp = new double[ngene*appendIndex];
		
		int i = 0;
		
		for(int g = 0; g < this.dataMatrix.getNumRow(); g++)
		{
			for(int e = 0; e < nexpr; e++)
			{
				if(e < appendIndex)
				{
					temp[i++] = getValue(e, g);
				}					
			}
		}
		dataMatrix.setExprData(temp);
		
		String [][] tempS = new String[appendIndex][];
		
		for(int j = 0; j < appendIndex; j++)
		{
			tempS[j] = arrayHeaderInfo.getHeader(j);
		}
		
		arrayHeaderInfo.setHeaderArray(tempS);
		nexpr = appendIndex;
		appendIndex = -1;
		setChanged();
	}
	
	/**
	 * Appends a second matrix to this one provided they have the same height. Used for comparison of two data sets where the data is displayed side by side.
	 * 
	 */
	public void append(DataModel m)
	{
		int ngene = nGene();
		int nexpr = nExpr();
		if(m == null || m.getDataMatrix().getNumRow() != ngene || appendIndex != -1)
		{
			System.out.println("Could not compare.");
			return;
		}
		
		double [] temp = new double[getDataMatrix().getNumRow()* getDataMatrix().getNumCol() + 
		                            m.getDataMatrix().getNumRow()*(m.getDataMatrix().getNumCol() + 1)];
		
		int i = 0;
				
		for(int g = 0; g < m.getDataMatrix().getNumRow(); g++)
		{
			for(int e = 0; e < nexpr + m.getDataMatrix().getNumCol() + 1; e++)
			{
				if(e < nexpr)
				{
					temp[i++] = getValue(e, g);
				}
				else if(e < nexpr + 1)
				{
					temp[i++] = DataModel.NODATA;
				}
				else
				{
					temp[i++] = m.getDataMatrix().getValue(e - nexpr - 1, g);
				}
				
			}
		}
		
		String [][] tempS = new String[getArrayHeaderInfo().getNumHeaders() + m.getArrayHeaderInfo().getNumHeaders() + 1][];
		
		i = 0;
		for(int j = 0; j < getArrayHeaderInfo().getNumHeaders(); j++)
		{
			tempS[i++] = getArrayHeaderInfo().getHeader(j);
		}
		
		tempS[i] = new String[getArrayHeaderInfo().getNumNames()];
		
		for(int j = 0; j < tempS[i].length; j++)
		{
			tempS[i][j] = "-----------------------";
		}
		i++;
		
		for(int j = 0; j < getArrayHeaderInfo().getNumHeaders(); j++)
		{
			tempS[i++] = getArrayHeaderInfo().getHeader(j);
		}
		
		
		arrayHeaderInfo.setHeaderArray(tempS);
		appendIndex = nexpr;
		nexpr += m.getDataMatrix().getNumCol() + 1;
		dataMatrix.setExprData(temp);
		setChanged();
	}

	int appendIndex = -1;
	
    protected Frame frame;
    protected FileSet source = null;
    protected String  dir = null;
    protected String  root;

	
	protected TVDataMatrix dataMatrix;
		
		
    protected IntHeaderInfo arrayHeaderInfo;
    protected GeneHeaderInfo geneHeaderInfo;
	protected IntHeaderInfo atrHeaderInfo;
	protected IntHeaderInfo gtrHeaderInfo;
	
	protected boolean aidFound = false;
	protected boolean gidFound = false;
		
    protected boolean eweightFound = false;
    protected boolean gweightFound = false;
    protected PropertyConfig documentConfig; // holds document config
	/**
	 * Really just a thin wrapper around exprData array.
	 * @author aloksaldanha
	 *
	 */
	class TVDataMatrix implements DataMatrix {
		
	    private double [] exprData = null;
		private double maxValue = Double.MIN_VALUE;
		private double minValue = Double.MAX_VALUE;

		public void clear() {
			exprData = null;
		}

	    public double getValue(int x, int y) {
			int nexpr = nExpr();
			int ngene = nGene();
			if ((x < nexpr) && (y < ngene) && (x >= 0) && (y >= 0)) {
				return exprData[x + y * nexpr];
			} else {
				return DataModel.NODATA;
			}
		}
		
		public void setExprData(double[] newData) {
			exprData = newData;
			for (int i = 0; i < newData.length; i++) {
				updateMinMax(newData[i]);
			}
		}

		public void setValue(double value, int x, int y)
		{
			updateMinMax(value);
			exprData[x + y*getNumCol()] = value;
			setChanged();
		}
		public int getNumRow() {
			return nGene();
		}
		public int getNumCol() {
			return nExpr();
		}
		
		public int getNumUnappendedCol()
		{
			return appendIndex == -1?getNumCol():appendIndex;
		}

		public double getMaxValue() {
			return maxValue;
		}

		public double getMinValue() {
			return minValue;
		}

		private void updateMinMax(double value) {
			if (value != DataModel.NODATA) {
				if (value < minValue) 
					minValue = value;
				else if (value > maxValue)
					maxValue = value;
			}
		}
	}

	/** holds actual node information for array tree */
	public void setAtrHeaders(String [][]atrHeaders) {
		atrHeaderInfo.setHeaderArray(atrHeaders);
	}
	/** holds header row from atr file */
	public void setAtrPrefix(String [] atrPrefix) {
		atrHeaderInfo.setPrefixArray(atrPrefix);
	}

	/** holds actual node information for gene tree */
	public void setGtrHeaders(String [][] gtrHeaders) {
		gtrHeaderInfo.setHeaderArray(gtrHeaders);
	}

	public void setGtrPrefix(String [] gtrPrefix) {
		gtrHeaderInfo.setPrefixArray(gtrPrefix);
	}

	public void setArrayHeaders(String [] [] newHeaders) {
		arrayHeaderInfo.setHeaderArray(newHeaders);
	}
	public void setArrayPrefix(String [] newPrefix) {
		arrayHeaderInfo.setPrefixArray(newPrefix);
	}
	
	
	
	class GeneHeaderInfo extends IntHeaderInfo {
		
	  public int getAIDIndex() {
		return 1;
	  }
	public int getGIDIndex() {
		return 0;
	  }
	  public int getYorfIndex() {
		if (getIndex("GID") == -1) {
		  return 0;
		} else {
		  return 1;
		}
	  }
	  public int getNameIndex() {
		if (getIndex("GID") == -1) {
		  return 1;
		} else {
		  return 2;
		}
	  }
	  
	  
	  /**
	  * There are two special indexes, YORF and NAME.
	  */
	  public int getIndex(String header) {
		  int retval = super.getIndex(header);
		  if (retval != -1) {
			  return retval;
		  }
		  if (header.equals("YORF")) {
			  return getYorfIndex();
		  }	

		  if(header.equals("NAME")) {
			  return getNameIndex();
		  }	
		  return -1;
	  }
	  
	}

	/**
	 * A generic headerinfo, backed by private arrays.
	 * 
	 * @author aloksaldanha
	 *
	 */
	class IntHeaderInfo extends Observable implements HeaderInfo {
		private String [] prefixArray = new String[0];
		private String [][] headerArray = new String[0][];
		private Hashtable id2row = new Hashtable();
		public void hashIDs(String header) {
			int index = getIndex(header);
			id2row = populateHash(this, index , id2row);
		}
		public void clear() {
			prefixArray = new String[0];
			headerArray = new String[0][];
			id2row.clear();
		}
		public void setPrefixArray(String[] newVal) {
			prefixArray = newVal;
		}
		public void setHeaderArray(String[][] newVal) {
			headerArray = newVal;
		}
		public String [] getNames() { 
			return prefixArray;
		  }
		  public int getNumNames() {
			  return prefixArray.length;
		  }
		  
		  public int getNumHeaders() {
			  return headerArray.length;
		  }
		  
		  /**
		  * Returns the header for a given gene and column heading.
		  */
		  public String [] getHeader(int gene) {
			  try{
				  if (headerArray[gene] == null) {
					  return new String[0];
				  } else {
					  return headerArray[gene];
				  }
			  } catch (java.lang.ArrayIndexOutOfBoundsException e) {
				  System.out.println("error: tried to retrieve header for  index " +
						  gene + " but max is "+ headerArray.length);
				  e.printStackTrace();
				  return new String[0];
			  }
		  }

		  /**
		  * Returns the header for a given gene and column heading,
		  * or null if not present.
		  */
		  public String getHeader(int gene, String col) {
			int index = getIndex(col);
			if (index == -1) {
				return null;
			}
			return getHeader(gene, index);
		  }
			public String getHeader(int rowIndex, int columnIndex) {
				  return (getHeader(rowIndex))[columnIndex];
			}
			  public int getIndex(String header) {
					for (int i = 0 ; i < prefixArray.length; i++) {
					  if (header.equalsIgnoreCase(prefixArray[i]))
						return i;
					}
					return -1;
				  }
		  
			public int getHeaderIndex(String id) {
				Object ind = id2row.get(id);
				if (ind == null) {
					return -1;
				} else {
					return ((Integer) ind).intValue();
				}
			}

			/**
			 * adds new header column of specified name at specified index.
			 * @param name
			 * @param index
			 * @return
			 */
			public boolean addName(String name, int index) {
				int existing = getIndex(name);
				//already have this header
				if (existing != -1) return false;
				int newNumNames = getNumNames()+1;
				for (int row = 0; row < getNumHeaders(); row++) {
					String [] from = headerArray[row];
					String [] to = new String[newNumNames];
					for (int col = 0; col < index; col++)
						to[col] = from[col];
					for (int col = index+1; col < newNumNames; col++)
						to[col] = from[col-1];
					headerArray[row] = to;
				}
				String [] newPrefix = new String[newNumNames];
				for (int col = 0; col < index; col++)
					newPrefix [col] = prefixArray[col];
				newPrefix[index] = name;
				for (int col = index+1; col < newNumNames; col++)
					newPrefix [col] = prefixArray[col-1];
				prefixArray = newPrefix;
				setModified(true);
				return true;
			}
			public boolean setHeader(int i, String name, String value) {
				if (headerArray.length < i) return false;
				int nameIndex = getIndex(name);
				if (nameIndex == -1) return false;
				if (headerArray[i][nameIndex] == value) return false;
				headerArray[i][nameIndex] = value;
				setModified(true);
				return true;
			}
			public boolean getModified() {return modified;}
			public void setModified(boolean mod) {
				setChanged();
				notifyObservers();
				modified = mod;
			}
			private boolean modified = false;
			/*
			public void printHashKeys() {
				Enumeration e = id2row.keys();
				while (e.hasMoreElements()) {
					System.err.println(e.nextElement());
				}
			}
			*/
		}
	

	  public void setGenePrefix(String [] newVal) {
		  geneHeaderInfo.setPrefixArray(newVal);
	  }
	  public void setGeneHeaders(String [][] newVal) {
		  geneHeaderInfo.setHeaderArray(newVal);
	  }
	// loading stuff follows...

    /**
     *
     *
     * @param fileSet fileset to load
     *
     */
	 public void loadNew(FileSet fileSet) throws LoadException {
	 }
	 /**
	 * Don't open a loading window...
	 */
	 public void loadNewNW(FileSet fileSet) throws LoadException {
	 }

	/**
	 * @param b
	 */
	public void setEweightFound(boolean b) {
		eweightFound = b;
	}
	/**
	 * @param b
	 */
	public void setGweightFound(boolean b) {
		gweightFound = b;
		
	}
	public boolean getModified() {
		return  getGtrHeaderInfo().getModified() ||
//		getGeneHeaderInfo().getModified() ||
//		getArrayHeaderInfo().getModified() ||
		getAtrHeaderInfo().getModified();
	}
	public boolean isLoaded() {
		return loaded;
	}
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	public boolean isSymmetrical() {
		return false;
	}
}
