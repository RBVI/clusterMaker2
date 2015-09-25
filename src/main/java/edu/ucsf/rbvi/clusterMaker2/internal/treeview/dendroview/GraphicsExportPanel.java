/* BEGIN_HEADER                                              Java TreeView
*
* $Author: rqluk $
* $RCSfile: GraphicsExportPanel.java,v $
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;

import edu.ucsf.rbvi.clusterMaker2.internal.treeview.ConfigNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.DataModel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderInfo;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.HeaderSummary;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.LinearTransformation;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.SettingsPanel;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeDrawerNode;
import edu.ucsf.rbvi.clusterMaker2.internal.treeview.TreeSelectionI;

import org.freehep.graphicsio.ps.PSGraphics2D;
import org.freehep.graphicsio.svg.SVGGraphics2D;
import org.freehep.graphics2d.VectorGraphics;

/**
* This class is a superclass which implements a GUI for selection of options relating to output. 
* It makes most of the relevant variables accessible to subclasses through protected methods.
*/
public class GraphicsExportPanel extends JPanel implements SettingsPanel {
	private ConfigNode root;

	private String[] formats = {"png","jpg","bmp","pdf","svg","eps"};
	
	// external links
	private DendroView view;
	private DataModel model;

	private HeaderInfo arrayHeaderInfo; // allows access to array headers.
	private HeaderInfo geneHeaderInfo;  // allows access to gene headers.
	private TreeSelectionI geneSelection;
	private TreeSelectionI arraySelection;
	private InvertedTreeDrawer arrayTreeDrawer;
	private LeftTreeDrawer geneTreeDrawer;
	private ArrayDrawer arrayDrawer;
	private MapContainer geneMap;
	private MapContainer arrayMap;
	
	// components
	private FilePanel filePanel;
	private InclusionPanel inclusionPanel;
	private HeaderSelectionPanel headerSelectionPanel;
	private JComboBox formatPullDown;
	private JCheckBox appendExt;

	/**
	* the scale of the passed in gene map and array map define the initial size.
	* The export panel will not actually modify the map settings for now. 
	*
	* To Developers- if you want to simpify the code by changing the scale 
	* settings in the maps, make copies of them first. This might involve 
	* implementing copyStateFrom functions in the MapContainer class.
	*
	*/
	public GraphicsExportPanel(DendroView view) {
		this.view = view;

		// Get all of the necessary information from our view
		model = view.getDataModel();
		arrayHeaderInfo = model.getArrayHeaderInfo();
		geneHeaderInfo = model.getGeneHeaderInfo();

		geneSelection = view.getGeneSelection();
		arraySelection = view.getArraySelection();

		arrayTreeDrawer = view.getArrayTreeDrawer();
		geneTreeDrawer = view.getGeneTreeDrawer();
		arrayDrawer = view.getArrayDrawer();

		if ((geneSelection.getNSelectedIndexes() != 0) || (arraySelection.getNSelectedIndexes() != 0)){
			arrayMap = view.getZoomXmap();
			geneMap = view.getZoomYmap();
		} else {
			arrayMap = view.getGlobalXmap();
			geneMap = view.getGlobalYmap();
		}

		setupWidgets();
		inclusionPanel.synchSelected();
		inclusionPanel.synchEnabled();
	}
	
	// accessors
	protected HeaderInfo getArrayHeaderInfo() {
		return arrayHeaderInfo;
	}
	protected HeaderInfo getGeneHeaderInfo() {
		return geneHeaderInfo;
	}
	protected TreeSelectionI getGeneSelection() {
		return geneSelection;
	}
	protected TreeSelectionI getArraySelection() {
		return arraySelection;
	}
	protected ArrayDrawer getArrayDrawer() {
		return arrayDrawer;
	}

	public void synchronizeTo() {
		String format = (String) formatPullDown.getSelectedItem();
		if (format.equals("png") || format.equals("jpg") || format.equals("bmp"))
			bitmapSave(format);
		else if (format.equals("pdf"))
			pdfSave(format);
		else if (format.equals("eps"))
			epsSave(format);
		else if (format.equals("svg"))
			svgSave(format);
	}

	public void synchronizeFrom() {}
	
	// NOTE: border pixels appear on all sides.
	int borderPixels = 5;
	/** Setter for borderPixels */
	public void setBorderPixels(int border) {
		this.borderPixels = border;
	}
	/** Getter for borderPixels */
	public int getBorderPixels() {
		return borderPixels;
	}
	private static int textSpacing = 2; //pixels between boxes and text
	
	/**
	* for communication with subclass... (in this case PostscriptExport)
	*/
	protected boolean hasBbox() {
		return true;
	}
	
	/**
	* for communication with subclass... (in this case CharExport)
	* NOTE: better to have local, to avoid obligatory subclassing.
	*/
	boolean hasChar = false;
	protected boolean hasChar() {
		return hasChar;
	}
	
	// accessors for configuration information
	/**
	* returns the font for gene annotation information
	*/
	private Font geneFont = new Font("Lucida Sans Regular", 0, 12);
	protected Font getGeneFont() {
		return geneFont;
	}
	public void setGeneFont(Font f) {
		if (f != null) {
			geneFont =f;
		}
	}
	private Font arrayFont = new Font("Lucida Sans Regular", 0, 12);
	protected Font getArrayFont() {
		return arrayFont;
	}
	public void setArrayFont(Font f) {
		if (f != null) {
			arrayFont =f;
		}
	}
	
	/**
	* True if an explict bounding box should be included in the output.
	* Subclasses are to use this when creating output. The returned value reflects
	* what the user has selected in the GUI.
	* This is only meaningful for postscript.
	*/
	protected boolean includeBbox() {
		return inclusionPanel.useBbox();
	}
	
	/** 
	* This method returns the minimum correlation for the gene nodes which will be drawn.
	*/
	protected double getMinGeneCorr() {
		if (drawSelected()) {
			// if (geneTreeDrawer == null) logger.error("ExportPanel.getMinGeneCorr: geneTreeDrawer null");
			TreeSelectionI selection = getGeneSelection();
			// if (selection == null) logger.error("ExportPanel.getMinGeneCorr: selection null");
			String selectedId = selection.getSelectedNode();
			// if (selectedId == null) logger.error("ExportPanel.getMinGeneCorr: selectedId null");
			TreeDrawerNode selectedNode = geneTreeDrawer.getNodeById(selectedId);
			// if (selectedNode == null) logger.error("ExportPanel.getMinGeneCorr: selectedNode null , id " + selectedId);
			return  selectedNode.getCorr();
		} else {
			return geneTreeDrawer.getCorrMin();
		}
	}
	/** 
	* This method returns the minimum correlation for the gene nodes which will be drawn.
	*/
	protected double getMinArrayCorr() {
		if (drawSelected()) {
			return  arrayTreeDrawer.getNodeById(getArraySelection().getSelectedNode()).getCorr();
		} else {
			return arrayTreeDrawer.getCorrMin();
		}
	}
	
	/**
	* This method is for drawing the actual data. 
	*
	* It returns the offset of the first pixel of the block corresponding to the geneIndex
	* where the first block (index 0) always has an offset of zero.
	*/
	protected int getYmapPixel(double geneIndex) {
		double dp = geneMap.getPixel(geneIndex) - geneMap.getPixel(0);
		double ret = (int) (dp * getYscale() / geneMap.getScale());
		return (int) ret;
	}
	
	/**
	* This method is for drawing the actual data. 
	*
	* It returns the offset of the first pixel of the block corresponding to the arrayIndex
	* where the first block (index 0) always has an offset of zero.
	*/
	protected int getXmapPixel(double geneIndex) {
		double dp = arrayMap.getPixel(geneIndex) - arrayMap.getPixel(0);
		int ret = (int) (dp * getXscale() / arrayMap.getScale());
		return (int) ret;
	}
	protected boolean geneAnnoInside() {
		return headerSelectionPanel.geneAnnoInside();
	}
	protected boolean arrayAnnoInside() {
		return headerSelectionPanel.arrayAnnoInside();
	}
	protected String getGeneAnno(int i) {
		return headerSelectionPanel.getGeneAnno(i);
	}
	protected String getArrayAnno(int i) {
		return headerSelectionPanel.getArrayAnno(i);
	}
	private Color getFgColor(HeaderInfo headerInfo, int index) {
		int colorIndex       = headerInfo.getIndex("FGCOLOR");
		if (colorIndex > 0) {
			String[] headers  = headerInfo.getHeader(index);
			return TreeColorer.getColor(headers[colorIndex]);
		}
		return null;
	}
	private Color getBgColor(HeaderInfo headerInfo, int index) {
		int colorIndex       = headerInfo.getIndex("BGCOLOR");
		if (colorIndex > 0) {
			String[] headers  = headerInfo.getHeader(index);
			return TreeColorer.getColor(headers[colorIndex]);
		}
		return null;
	}
	
	protected Color getGeneFgColor(int i) {
		return getFgColor(geneHeaderInfo, i);
	}
	protected Color getArrayFgColor(int i) {
		return getFgColor(arrayHeaderInfo, i);
	}
	protected Color getGeneBgColor(int i) {
		return getBgColor(geneHeaderInfo, i);
	}
	protected Color getArrayBgColor(int i) {
		return getBgColor(arrayHeaderInfo, i);
	}
	// gene node to actually draw
	protected TreeDrawerNode getGeneNode() {
		if (inclusionPanel.drawSelected()) {
			return geneTreeDrawer.getNodeById(geneSelection.getSelectedNode());
		} else {
			return getGeneRootNode();
		}
	}
	
	// array node to actually draw
	protected TreeDrawerNode getArrayNode() {
		if (inclusionPanel.drawSelected()) {
			return arrayTreeDrawer.getNodeById(arraySelection.getSelectedNode()) ;
		} else {
			return getArrayRootNode();
		}
	}
	
	protected File getFile() {
		// See if we need to add the extension
		if (appendExt.isSelected())
			appendExtension();
		return filePanel.getFile();
	}
	public String getFilePath() {
		// See if we need to add the extension
		if (appendExt.isSelected())
			appendExtension();
		return filePanel.getFilePath();
	}
	public void setFilePath(String newFile) {
		filePanel.setFilePath(newFile);
	}
	protected TreeDrawerNode getGeneRootNode() {
		if (geneTreeDrawer == null) return null;
		return geneTreeDrawer.getRootNode();
	}
	protected TreeDrawerNode getArrayRootNode() {
		if (arrayTreeDrawer == null) return null;
		return arrayTreeDrawer.getRootNode();
	}
	protected String getInitialExtension() {
		return ".png";
	}
	protected String getInitialFilePath() {
		String defaultPath = System.getProperty("user.home");
		String fileSep = System.getProperty("file.separator");
		String file = model.getSource();
		if (root == null) {
			return defaultPath+fileSep+file;
		} else {
			return root.getAttribute("file", defaultPath)+fileSep+file;
		}
	}
	
	
	public void setIncludedGeneHeaders(int []newSelected) {
		headerSelectionPanel.geneList.setSelectedIndices(newSelected);
		headerSelectionPanel.setupSelected();
	}
	public void setIncludedArrayHeaders(int []newSelected) {
		headerSelectionPanel.arrayList.setSelectedIndices(newSelected);
		headerSelectionPanel.setupSelected();
	}
	
	public void bindConfig(ConfigNode configNode)
	{
		root = configNode;
	}
	public ConfigNode createSubNode()
	{
		return root.create("File");
	}
	
	private void setupWidgets() {
		Box upperPanel; // holds major widget panels
		upperPanel = new Box(BoxLayout.X_AXIS);
		headerSelectionPanel = new HeaderSelectionPanel();
		upperPanel.add(headerSelectionPanel);
		inclusionPanel = new InclusionPanel();
		upperPanel.add(inclusionPanel);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(upperPanel);
		filePanel = new FilePanel(getInitialFilePath());
		add(filePanel);
		
		JPanel formatPanel = new JPanel();
		formatPullDown = new JComboBox(formats);
		appendExt = new JCheckBox("Append Extension?", true);
		formatPanel.add(new JLabel("Graphics Format:"));
		formatPanel.add(formatPullDown);
		formatPanel.add(appendExt);
		add(formatPanel);
	}	

	private void appendExtension() {
    String fileName = filePanel.getFilePath();
    int extIndex = fileName.lastIndexOf('.');
    int dirIndex = fileName.lastIndexOf(File.separatorChar);
    if  (extIndex > dirIndex) {
      setFilePath(fileName.substring(0, extIndex) + "." + formatPullDown.getSelectedItem());
    } else {
      setFilePath(fileName + "." + formatPullDown.getSelectedItem());
    }
  }

	
	//drawing specific convenience methods...
	protected boolean includeAtr() {
		return inclusionPanel.includeAtr();
	}
	protected void includeAtr(boolean flag) {
		inclusionPanel.includeAtr(flag);
	}
	protected boolean includeGtr() {
		return inclusionPanel.includeGtr();
	}
	protected void includeGtr(boolean flag) {
		inclusionPanel.includeGtr(flag);
	}
	protected boolean includeData() {
		return inclusionPanel.includeData();
	}
	private boolean includeChar() {
		return inclusionPanel.includeChar();
	}
	protected void includeData(boolean flag) {
		inclusionPanel.includeData(flag);
	}
	protected boolean drawSelected() {
		return inclusionPanel.drawSelected();
	}
	protected boolean includeGeneMap() {
		return (includeGtr() || includeData() || (numGeneHeaders() > 0));
	}
	protected boolean includeArrayMap() {
		return (includeAtr() || includeData() || (numArrayHeaders() > 0));
	}
	
	public double getXmapWidth() {
		// HACK, doesn't account for discontinuous selection
		return (int) ((arrayMap.getPixel(maxArray() + 1) - arrayMap.getPixel(minArray())) * getXscale() / arrayMap.getScale());
	}
	public double getGtrWidth() {
		return 200 * getXscale() / arrayMap.getScale();
	}
	public double getXscale() {
		return inclusionPanel.getXscale();
	}
	
	public double getYmapHeight() {
		// HACK, doesn't account for discontinuous selection
		double ret = (geneMap.getPixel(maxGene() + 1) - geneMap.getPixel(minGene())) * getYscale() / geneMap.getScale();
		return ret;
	}
	
	public double getAtrHeight() {
		return 100 /* * getYscale() / geneMap.getScale()*/;
	}
	public double getYscale() {
		return inclusionPanel.getYscale();
	}
	
	public int getBboxWidth() {
		return inclusionPanel.getBboxWidth();
	}
	public int getBboxHeight() {
		return inclusionPanel.getBboxHeight();
	}
	
	public int minGene() {
		if (inclusionPanel.drawSelected()) {
			return geneSelection.getMinIndex();
		} else {
			return 0;
		}
	}
	
	public int minArray() {
		if (inclusionPanel.drawSelected()) {
			return arraySelection.getMinIndex();
		} else {
			return 0;
		}
	}
	
	public int maxGene() {
		if (inclusionPanel.drawSelected()) {
			return geneSelection.getMaxIndex();
		} else {
			return geneHeaderInfo.getNumHeaders() - 1;
		}
	}
	
	public int maxArray() {
		if (inclusionPanel.drawSelected()) {
			return arraySelection.getMaxIndex();
		} else {
			return arrayHeaderInfo.getNumHeaders() - 1;
		}
	}
	
	public int estimateHeight() {
		int height = 2*getBorderPixels();
		// do we need to include the height of the map?
		if (includeGeneMap()) {
			height += (int) getYmapHeight();
		} else {
		}
		// additional space for gene tree...
		if (includeAtr()) {
			height += (int) getAtrHeight();
		}
		height += getArrayAnnoLength();
		return height;
	}
	public int estimateWidth() {
		int width = 2*getBorderPixels();
		// do we need to include the width of the map?
		if (includeArrayMap()) {
			width += (int) getXmapWidth();
		} else {
		}
		// additional space for gene tree...
		if (includeGtr()) {
			width += (int) getGtrWidth();
		}
		width += getGeneAnnoLength(); 
		return width;
	}
	protected int getGeneAnnoLength() {
		// deal with text length...
		if ((inclusionPanel == null) || (inclusionPanel.useBbox() == false)) { 
			// no bounding box, have to wing it...
			return headerSelectionPanel.geneMaxLength() + textSpacing;
		} else {
			return getBboxWidth();
		}
	}
	protected int getArrayAnnoLength() {
		// deal with text length...
		if ((inclusionPanel == null) ||(inclusionPanel.useBbox() == false)) { 
			// no bounding box, have to wing it...
			return headerSelectionPanel.arrayMaxLength()+ textSpacing;
		} else {
			return getBboxHeight();
		}
	}
	
	public int numArrayHeaders() {
		return headerSelectionPanel.numArrayHeaders();
	}
	public int numGeneHeaders() {
		return headerSelectionPanel.numGeneHeaders();
	}
	
	public void deselectHeaders() {
		headerSelectionPanel.deselectHeaders();
	}
	
	/**
	* The following method gets the x coordiate of the data matrix, according to current settings.
	*/
	protected int getDataX() {
		int dataX = getBorderPixels();
		if (includeGtr()) dataX += getGtrWidth();
		return dataX;
	}
	
	/**
	* The following method gets the y coordiate of the data matrix, according to current settings.
	*/
	protected int getDataY() {
		int dataY = getBorderPixels();
		if (includeAtr()) dataY += getAtrHeight();
		dataY += getArrayAnnoLength();
		return dataY;
	}
	
	/**
	* does the dirty work by calling methods in the superclass.
	*/
	public void drawAll(Graphics g, double scale) {
		int width = estimateWidth();
		int height = estimateHeight();
		if ((width == 0) || (height == 0)) {
			return;
		}
		// 5 views to worry about... first, calculate datamatrix's origin...
		
		int dataX = (int) (scale * getDataX());
		int dataY = (int) (scale * getDataY());
		int scaleP = (int) (scale * getBorderPixels());
		drawGtr(g, scaleP, dataY, scale);
		
		if (includeAtr()) {
			if (arrayAnnoInside()) {
				drawAtr(g,dataX, scaleP, scale);
				drawArrayAnno(g,dataX, scaleP + (int) (scale * getAtrHeight()), scale);
			} else {
				drawArrayAnno(g,dataX, scaleP, scale);
				drawAtr(g,dataX, dataY - (int) (scale * getAtrHeight()), scale);
			}
		} else {
			drawArrayAnno(g,dataX, scaleP, scale);
		}
		drawData(g,dataX, dataY, scale);
		if (includeArrayMap()) {
			drawGeneAnno(g,dataX + (int) (getXmapWidth() * scale), dataY, scale);
		} else {
			drawGeneAnno(g,dataX , dataY, scale);
		}
	}
	/**
	* draws a scaled Gene Tree at the suggested x,y location
	*/
	protected void drawGtr(Graphics g, int x, int y, double scale) {
		if (includeGtr() == false) return;
		int width = (int) (getGtrWidth() * scale);
		int height = (int) (getYmapHeight() * scale);
		if ((height == 0) || (width == 0)) return;
		
		// clear the pallette...
		g.setColor(Color.black);
		
		//	calculate Scaling
		Rectangle destRect = new Rectangle();
		destRect.setBounds(x,y, width, height);
		
		double minCorr = getMinGeneCorr();
		LinearTransformation xScaleEq = new LinearTransformation
		(minCorr, destRect.x,
		geneTreeDrawer.getCorrMax(), destRect.x + destRect.width);
		
		LinearTransformation yScaleEq = new LinearTransformation (minGene(), destRect.y,
		                                                          maxGene()+ 1, 
		                                                          destRect.y + destRect.height);
		
		// draw
		geneTreeDrawer.paintSubtree(g, xScaleEq, yScaleEq, destRect, getGeneNode(), false);
		
	}
	/**
	* draws a scaled Array Tree at the suggested x,y location
	*/
	protected void drawAtr(Graphics g, int x, int y, double scale) {
		if (includeAtr() == false) return;
		int width = (int) (getXmapWidth() * scale);
		int height = (int) (getAtrHeight() * scale);
		if ((height == 0) || (width == 0)) return;
		// clear the pallette...
		g.setColor(Color.black);
		
		//	calculate Scaling
		Rectangle destRect = new Rectangle();
		destRect.setBounds(x, y, width, height);
		LinearTransformation xScaleEq = new LinearTransformation(minArray(), destRect.x,
		                                                         maxArray()+1, destRect.x + destRect.width);
		double minCorr = arrayTreeDrawer.getCorrMin();
		if (drawSelected()) {
			minCorr = arrayTreeDrawer.getNodeById(getArraySelection().getSelectedNode()).getCorr();
		}
		LinearTransformation yScaleEq = new LinearTransformation (minCorr, destRect.y,
		                                                          arrayTreeDrawer.getCorrMax(), 
		                                                          destRect.y + destRect.height);
		
		// draw
		arrayTreeDrawer.paintSubtree(g, xScaleEq, yScaleEq, destRect, getArrayNode(), false);
	}
	
	/**
	* draws an appropriately sized box for each annotation string at the specific location
	*/
	protected void drawGeneAnnoBox(Graphics g, int x, int y, double scale) {
		// HACK doesn't deal with discontinuous selection right.
		int width = (int) (getGeneAnnoLength() * scale);
		int height = (int) (getYmapHeight() * scale);
		g.setColor(Color.black);
		FontMetrics fontMetrics = getFontMetrics(getGeneFont());
		int geneHeight = (int) (fontMetrics.getAscent() * scale);
		int min = minGene();
		int max = maxGene();
		double spacing = (double) height/(max - min + 1);
		for (int i = min; i <= max;  i++) {
			/*
			int geneWidth = (int) (scale * headerSelectionPanel.getLength (headerSelectionPanel.getGeneAnno(i)));
			*/
			int geneWidth = width;
			g.fillRect(x, y + (int)((i - min) *spacing   + (spacing - geneHeight) /2),
			           geneWidth,
			           geneHeight);
		}
	}
	
	/**
	* draws an appropriately sized box for each annotation string at the specific location
	*/
	public void drawArrayAnnoBox(Graphics g, int x, int y, double scale) {
		// HACK doesn't deal with discontinuous selection right.
		int height = (int) (getArrayAnnoLength() * scale);
		int width = (int) (getXmapWidth() * scale);
		
		g.setColor(Color.black);
		FontMetrics fontMetrics = getFontMetrics(getArrayFont());
		int arrayWidth = (int) (fontMetrics.getAscent() * scale);
		int min = minArray();
		int max = maxArray();
		double spacing = (double) width/(max - min + 1);
		for (int i = min; i <= max;  i++) {
			//		int arrayHeight = (int) (scale * headerSelectionPanel.getLength(headerSelectionPanel.getArrayAnno(i)));
			int arrayHeight = height;
			int thisx = x+ (int)((i - min) *spacing   + (spacing - arrayWidth) /2);
			int thisy = y + height - arrayHeight;
			if (headerSelectionPanel.arrayAnnoInside()) {
				thisy = y;
			}
			g.fillRect(thisx, thisy, arrayWidth, arrayHeight);
		}
	}
	/**
	* draws an annotation strings at the specific location
	*/
	protected void drawGeneAnno(Graphics g, int x, int y, double scale) {
		// HACK doesn't deal with discontinuous selection right.
		int width = (int) (getGeneAnnoLength() * scale);
		int height = (int) (getYmapHeight() * scale);
		if ((height == 0) || (width == 0)) return;
		int min = minGene();
		int max = maxGene();
		double spacing = (double) height/(max - min + 1);
		
		MapContainer tempMap = new MapContainer("Fixed");
		tempMap.setScale(spacing);
		tempMap.setIndexRange(min,max);
		tempMap.setAvailablePixels(height + getBorderPixels());
		TextView anv = new TextView(geneHeaderInfo);
		anv.setMap(tempMap);
		anv.setHeaderSummary(headerSelectionPanel.getGeneSummary());
		// Image buf = createImage(width + getBorderPixels(), height + getBorderPixels());
		anv.setFace(getGeneFont().getName());
		anv.setStyle(getGeneFont().getStyle());
		anv.setPoints((int)spacing);
		anv.updateBuffer(g, new Rectangle(x+textSpacing, y, 
		                                  width+getBorderPixels(), height+getBorderPixels()));
		// g.drawImage(buf,x+textSpacing, y, null);
		
		/*
		g.setColor(Color.black);
		g.setFont(getGeneFont());
		FontMetrics fontMetrics = getFontMetrics(g.getFont());
		int geneHeight = (int) (fontMetrics.getAscent() * scale);
		int inset = (int) (scale * getBorderPixels());
		for (int i = min; i <= max;  i++) {
			g.drawString(getGeneAnno(i), x + inset,
			y + (int)((i - min + 1.0) *spacing   - (spacing - geneHeight) /2));
		}
		*/
	}
	
	/**
	* draws array annotation strings at the specific location
	*/
	public void drawArrayAnno(Graphics real, int x, int y, double scale) {
		int height = (int) (getArrayAnnoLength() * scale);
		int width = (int) (getXmapWidth() * scale);
		if ((height == 0) || (width == 0)) return;
		int min = minArray();
		int max = maxArray();
		double spacing = (double) width/(max - min + 1);
		
		MapContainer tempMap = new MapContainer("Fixed");
		tempMap.setScale(spacing);
		tempMap.setIndexRange(min,max);
		tempMap.setAvailablePixels(width + getBorderPixels());
		ArrayNameView anv = new ArrayNameView(arrayHeaderInfo);
		anv.setFace(getArrayFont().getName());
		anv.setStyle(getArrayFont().getStyle());
		anv.setPoints((int)spacing);
		anv.setHeaderSummary(headerSelectionPanel.getArraySummary());
		anv.setMapping(tempMap);
		// Image buf = createImage(width + getBorderPixels(), height + getBorderPixels());
		// buf.getGraphics().setFont(getArrayFont());

		anv.updateBuffer(real, new Rectangle(x, y-getBorderPixels()-textSpacing, 
		                                     width+getBorderPixels(),height+getBorderPixels()));
		// real.drawImage(buf,x, y-getBorderPixels()-textSpacing, null);
	}
	
	/**
	* draws the data matrix
	*/
	public void drawData(Graphics g, int x, int y, double scale) {
		if (includeData() == false) return;
		int height = (int) (getYmapHeight() * scale);
		int width = (int) (getXmapWidth() * scale);
		
		Rectangle sourceRect = new Rectangle();
		sourceRect.setBounds(minArray(),minGene(),
		(maxArray() + 1 - minArray()), 
		(maxGene() + 1 - minGene()));
		Rectangle destRect = new Rectangle();
		
		// HACK does not deal with discontinuous selection...
		/* old version, kinda slow... 
		destRect.setBounds(x,y, width, height);
		arrayDrawer.paint(g, sourceRect ,destRect);
		*/
		destRect.setBounds(0,0, width, height);
		int [] pixels = new int [width * height];
		arrayDrawer.paint(pixels, sourceRect, destRect, width);
		MemoryImageSource source = new MemoryImageSource(width, height, pixels, 0, width);
		Image image = createImage(source);
		g.drawImage(image, x, y, null);
		if (includeChar()) {
			try {
				Image cimage = createImage(width, height);
				//				destRect.x += x;
//				destRect.y += y;
				cimage.getGraphics().drawImage(image, 0, 0, null);
				((CharArrayDrawer) arrayDrawer).paintChars(cimage.getGraphics(), sourceRect, destRect);
				g.drawImage(cimage, x, y, null);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Problem drawing Sequence data:" + e);
				// logger.error("" + e);
				// e.printStackTrace();
				g.drawImage(image, x, y, null);
			}
		} else {
				g.drawImage(image, x, y, null);
		}
	}
	
	JCheckBox selectionBox;
	public boolean getDrawSelected() {
		return selectionBox.isSelected();
	}
	public void setDrawSelected(boolean bool) {
		selectionBox.setSelected(bool);
	}

	private void bitmapSave(String format) {
		try {
			OutputStream output = new BufferedOutputStream(new FileOutputStream(getFile()));

			int extraWidth = getBorderPixels();
			int extraHeight = getBorderPixels();
			Rectangle destRect = new Rectangle(0,0, estimateWidth(), estimateHeight());

			BufferedImage i = new BufferedImage(destRect.width + extraWidth, destRect.height + extraHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics g = i.getGraphics();
			g.setColor(Color.white);
			g.fillRect(0,0,destRect.width+1 + extraWidth,  destRect.height+1+extraHeight);
			g.setColor(Color.black);
			g.translate(extraHeight/2, extraWidth/2);
			drawAll(g, 1.0);

			ImageIO.write(i,format,output);
			// ignore success, could keep window open on failure if save could indicate success.
			output.close();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
				new JTextArea("Graphics export had problem " +  e ));
			// logger.error("Exception " + e);
			// e.printStackTrace();
		}
	}

	private void pdfSave(String format) {
		com.itextpdf.text.Rectangle pageSize = PageSize.LETTER;
		Document document = new Document(pageSize);
		try {
			OutputStream output = new BufferedOutputStream(new FileOutputStream(getFile()));
			PdfWriter writer = PdfWriter.getInstance(document, output);
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			Graphics2D g = cb.createGraphics(pageSize.getWidth(), pageSize.getHeight(), new DefaultFontMapper());
  
      double imageScale = Math.min(pageSize.getWidth()  / ((double) estimateWidth()+getBorderPixels()),
                                   pageSize.getHeight() / ((double) estimateHeight()+getBorderPixels()));
      g.scale(imageScale, imageScale);
			drawAll(g, 1.0);
      g.dispose();
    }
    catch (Exception e)
    {
			JOptionPane.showMessageDialog(this,
				new JTextArea("Dendrogram export had problem " +  e ));
			// logger.error("Exception " + e);
			// e.printStackTrace();
    }
  
    document.close();
	}

	private void epsSave(String format) {
		com.itextpdf.text.Rectangle pageSize = PageSize.LETTER;
		Properties p = new Properties();
		p.setProperty(PSGraphics2D.PAGE_SIZE,"Letter");
		p.setProperty("org.freehep.graphicsio.AbstractVectorGraphicsIO.TEXT_AS_SHAPES",
                  Boolean.toString(false));

		try {
			OutputStream output = new BufferedOutputStream(new FileOutputStream(getFile()));
			PSGraphics2D g = new PSGraphics2D(output, view);
      double imageScale = Math.min(pageSize.getWidth()  / ((double) estimateWidth()+getBorderPixels()),
                                   pageSize.getHeight() / ((double) estimateHeight()+getBorderPixels()));
			g.setMultiPage(false);
			g.setProperties(p);
			g.startExport();
      g.scale(imageScale, imageScale);
			drawAll(g, 1.0);
			g.endExport();
			output.close();
    }
    catch (Exception e)
    {
			JOptionPane.showMessageDialog(this,
				new JTextArea("Dendrogram export had problem " +  e ));
			// logger.error("Exception " + e);
			// e.printStackTrace();
    }
	}

	private void svgSave (String format) {
		com.itextpdf.text.Rectangle pageSize = PageSize.LETTER;
		Properties p = new Properties();
		p.setProperty(PSGraphics2D.PAGE_SIZE,"Letter");
		p.setProperty("org.freehep.graphicsio.AbstractVectorGraphicsIO.TEXT_AS_SHAPES",
                  Boolean.toString(false));

		try {
			OutputStream output = new BufferedOutputStream(new FileOutputStream(getFile()));
			SVGGraphics2D g = new SVGGraphics2D(output, view);
      double imageScale = Math.min(pageSize.getWidth()  / ((double) estimateWidth()+getBorderPixels()),
                                   pageSize.getHeight() / ((double) estimateHeight()+getBorderPixels()));
			g.setProperties(p);
			g.startExport();
      g.scale(imageScale, imageScale);
			drawAll(g, 1.0);
			g.endExport();
			output.close();
    }
    catch (Exception e)
    {
			JOptionPane.showMessageDialog(this,
				new JTextArea("Dendrogram export had problem " +  e ));
			// logger.error("Exception " + e);
			// e.printStackTrace();
    }
	}
	
	class InclusionPanel extends JPanel {
		JCheckBox gtrBox, atrBox, dataBox, bboxBox, charBox;
		JTextField xScaleField, yScaleField;
		JTextField borderField;
		BboxRow bboxRow;
		SizeRow sizeRow;
		public boolean useBbox() {
			return bboxBox.isSelected();
		}
		public boolean includeAtr() {
			return atrBox.isSelected();
		}
		public void includeAtr(boolean flag) {
			atrBox.setSelected(flag);
		}
		
		public boolean includeGtr() {
			return gtrBox.isSelected();
		}
		public void includeGtr(boolean flag) {
			gtrBox.setSelected(flag);
		}
		
		public boolean includeData() {
			return dataBox.isSelected();
		}
		public boolean includeChar() {
			if (charBox == null) return false;
			boolean isSelected = charBox.isSelected();
			return isSelected;
		}
		public void includeData(boolean flag) {
			dataBox.setSelected(flag);
		}
		
		public double getXscale() {
			return extractDouble(xScaleField.getText());
		}
		public double getYscale() {
			return extractDouble(yScaleField.getText());
		}
		public int getBorderPixels() {
			return (int) extractDouble(borderField.getText());
		}
		private double extractDouble(String text) {
			try {
				Double tmp = new Double(text);
				return tmp.doubleValue();
			} catch(java.lang.NumberFormatException e) {
				return 0;
			}
		}
		public int getBboxWidth() {
			return bboxRow.xSize();
		}
		public int getBboxHeight() {
			return bboxRow.ySize();
		}
		public boolean drawSelected() {
			return selectionBox.isSelected();
		}
		public void synchEnabled() {
			selectionBox.setEnabled((geneSelection.getNSelectedIndexes() != 0) ||
			(arraySelection.getNSelectedIndexes() != 0));
			bboxRow.setEnabled(bboxBox.isSelected());
			
			// deal with array tree...
			if (getArrayRootNode() == null) { // no array clustering...
				atrBox.setSelected(false);
				atrBox.setEnabled(false);
			} else {
				if (selectionBox.isSelected()) { // outputting selection...
					if (arraySelection.getSelectedNode() == null) { // no array node selected...
						atrBox.setSelected(false);
						atrBox.setEnabled(false);
					} else {
						atrBox.setEnabled(true);
					}
				} else { // outputting global, array tree exists...
					atrBox.setEnabled(true);
				}
			}
			
			// deal with gene tree...
			if (getGeneRootNode() == null) { // no gene clustering...
				gtrBox.setSelected(false);
				gtrBox.setEnabled(false);
			} else {
				if (selectionBox.isSelected()) { // outputting selection...
					if (geneSelection.getSelectedNode() == null) { // no gene node selected...
						gtrBox.setSelected(false);
						gtrBox.setEnabled(false);
					} else {
						gtrBox.setEnabled(true);
					}
				} else { // outputting global, gene tree exists...
					gtrBox.setEnabled(true);
				}
			}
			
			if (arrayDrawer == null) {
				dataBox.setSelected(false);
				dataBox.setEnabled(false);
			}
			updateSize();
		}
		
		
		/**
		* This routine selects options so that they make sense with respect to the current data
		* in the dendrogram. It should be called during initialization before synchEnabled()
		*/
		public void synchSelected() {
			// do we output selected or the whole thing?
			selectionBox.setSelected((geneSelection.getNSelectedIndexes() != 0) ||
			(arraySelection.getNSelectedIndexes() != 0));
			
			if (selectionBox.isSelected()) { 
				//outputting selected...
				atrBox.setSelected(arraySelection.getSelectedNode() != null);
				gtrBox.setSelected(geneSelection.getSelectedNode() != null);
			} else {
				// outputing everything
				atrBox.setSelected(getArrayRootNode() != null);
				gtrBox.setSelected(getGeneRootNode() != null);
			}
			// always inlcude the data by default... if you have the drawer, that is.
			dataBox.setSelected(arrayDrawer != null);
			
			// recalculateBbox();
			
			updateSize();
		}
		public void recalculateBbox() {
			if (headerSelectionPanel == null) {
				bboxRow.setXsize(2);
				bboxRow.setYsize(2);
			} else {
				bboxRow.setXsize(headerSelectionPanel.geneMaxLength());
				bboxRow.setYsize(headerSelectionPanel.arrayMaxLength());
			}
		}
		
		public void updateSize() {
			try {
				sizeRow.setXsize(estimateWidth());
				sizeRow.setYsize(estimateHeight());
				setBorderPixels(getBorderPixels());
			} catch (Exception e) {
				// ignore...
			}
		}
		
		InclusionPanel() {
			documentListener = new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					updateSize();
				}
				public void insertUpdate(DocumentEvent e) {
					updateSize();
				}
				public void removeUpdate(DocumentEvent e) {
					updateSize();
				}
			};
			setupWidgets();
			recalculateBbox();
		}

		DocumentListener documentListener = null;
		private void setupWidgets() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			ActionListener syncher = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					synchEnabled();
				}
			};
			
			add(new JLabel("Include"));
			selectionBox = new JCheckBox("Selection Only     ");
			selectionBox.addActionListener(syncher);
			JPanel outputPanel = new JPanel();
			outputPanel.add(selectionBox);
			add(outputPanel);
			
			if (!model.isSymmetrical())
				gtrBox = new JCheckBox("Node Tree     ");
			else
				gtrBox = new JCheckBox("Left Node Tree");
			gtrBox.addActionListener(syncher);
			outputPanel = new JPanel();
			outputPanel.add(gtrBox);
			add(outputPanel);
			if (!model.isSymmetrical())
				atrBox = new JCheckBox("Attribute Tree");
			else
				atrBox = new JCheckBox("Top Node Tree ");
			atrBox.addActionListener(syncher);
			outputPanel = new JPanel();
			outputPanel.add(atrBox);
			add(outputPanel);

			dataBox = new JCheckBox("Heat Map       ");
			dataBox.addActionListener(syncher);
			outputPanel = new JPanel();
			outputPanel.add(dataBox);
			add(outputPanel);

			if (hasChar) {
				charBox = new JCheckBox("Sequence");
				charBox.addActionListener(syncher);
				outputPanel = new JPanel();
				outputPanel.add(charBox);
				add(outputPanel);
			}
			
			JPanel scalePanel = new JPanel();
			scalePanel.setLayout(new BoxLayout(scalePanel, BoxLayout.Y_AXIS));
			JPanel Xsub = new JPanel();
			xScaleField = new JTextField(Double.toString(arrayMap.getScale()));
			Xsub.add(new JLabel("x scale"));
			Xsub.add(xScaleField);
			scalePanel.add(Xsub);
			
			double yScale = geneMap.getScale();
			// if (yScale < 15) yScale = 15;
			yScaleField = new JTextField(Double.toString(yScale));
			JPanel Ysub = new JPanel();
			Ysub.add(new JLabel("y scale"));
			Ysub.add(yScaleField);
			scalePanel.add(Ysub);
			
			borderField = new JTextField(Double.toString(GraphicsExportPanel.this.getBorderPixels()));
			JPanel Bsub = new JPanel();
			Bsub.add(new JLabel("Border "));
			Bsub.add(borderField);
			scalePanel.add(Bsub);
			
			scalePanel.add(new JLabel("Use apple key to select multiple headers"));
			
			add(scalePanel);
			
			xScaleField.getDocument().addDocumentListener(documentListener);
			yScaleField.getDocument().addDocumentListener(documentListener);
			borderField.getDocument().addDocumentListener(documentListener);
			
			bboxBox = new JCheckBox("Bounding Box?", hasBbox());
			
			bboxBox.addActionListener(syncher);
			
			outputPanel = new JPanel();
			outputPanel.add(bboxBox);
			bboxRow = new BboxRow();
			if (hasBbox()) {
				add(outputPanel);
				add(bboxRow);
			}
			sizeRow = new SizeRow();
			add(sizeRow);
		}


		class BboxRow extends SizeRow {
			protected void setupWidgets() {
				DocumentListener documentListener = new DocumentListener() {
					public void changedUpdate(DocumentEvent e) {
						updateSize();
					}
					public void insertUpdate(DocumentEvent e) {
						updateSize();
					}
					public void removeUpdate(DocumentEvent e) {
						updateSize();
					}
				};
				add(new JLabel("BBox size:"));
				xSize = new JTextField("2", 4);
				ySize = new JTextField("2", 4);
				add(xSize);
				add(new JLabel("x"));
				add(ySize);
				add(new JLabel("(pixels)"));
				xSize.getDocument().addDocumentListener(documentListener);
				ySize.getDocument().addDocumentListener(documentListener);
			}
		}
		class SizeRow extends JPanel {
			JTextField xSize, ySize;
			public SizeRow() {
				setupWidgets();
			}
			protected void setupWidgets() {
				add(new JLabel("Total Size:"));
				xSize = new JTextField("2", 5);
				ySize = new JTextField("2", 5);
				add(xSize);
				add(new JLabel("x"));
				add(ySize);
				add(new JLabel("(pixels)"));
			}
			double conversionFactor = 1;
			int xSize() {
				return (int) (extractDouble(xSize.getText()) * conversionFactor);
			}
			int ySize() {
				return (int) (extractDouble(ySize.getText()) * conversionFactor);
			}
			
			void setXsize(int points) {
				xSize.setText(convert(points));
			}
			void setYsize(int points) {
				ySize.setText(convert(points));
			}
			/*
			* makes an inch representation of the points, with 2 decimal places.
			*/
			private String convert(int points) {
				Double inch = new Double(Math.rint(((double) points * 100 )/ conversionFactor)/ 100.0);
				return inch.toString();
			}
			public void setEnabled(boolean flag) {
				super.setEnabled(flag);
				xSize.setEnabled(flag);
				ySize.setEnabled(flag);
			}
			
		}
	}
	
	class HeaderSelectionPanel extends JPanel {
		private JCheckBox geneAnnoInside, arrayAnnoInside;
		private HeaderSummary geneSummary = new HeaderSummary();
		public HeaderSummary getGeneSummary() {
			return geneSummary;
		}
		private HeaderSummary arraySummary = new HeaderSummary();
		public HeaderSummary getArraySummary() {
			return arraySummary;
		}
		
		public JList geneList, arrayList;
		public String getGeneAnno(int i) {
			return geneSummary.getSummary(geneHeaderInfo, i);
			//			return assembleAnno(i, geneHeaderInfo, geneList.getSelectedIndices());
		}
		public String getArrayAnno(int i) {
			return arraySummary.getSummary(arrayHeaderInfo, i);
			
			// return assembleAnno(i, arrayHeaderInfo, arrayList.getSelectedIndices());
		}
		public int arrayMaxLength() {
			if (inclusionPanel == null) return 100;
			FontMetrics fontMetrics = getFontMetrics(getArrayFont());
			int max = 0;
			boolean drawSelected = inclusionPanel.drawSelected();
			for (int i = minArray(); i < maxArray(); i++) {
				if (drawSelected &&(arraySelection.isIndexSelected(i) == false)) continue;
				String anno = getArrayAnno(i);
				if (anno == null) continue;
				int length = fontMetrics.stringWidth(anno);
				if (length > max) max = length;
			}
			return max;
		}
		
		public int geneMaxLength() {
			if (inclusionPanel == null) return 100;
			FontMetrics fontMetrics = getFontMetrics(getGeneFont());
			int max = 0;
			boolean drawSelected = inclusionPanel.drawSelected();
			for (int i = minGene(); i < maxGene(); i++) {
				//		if (drawSelected && (geneSelection.isIndexSelected(i) == false)) continue;

				// getGeneAnno returns null in the breaks between k-means clusters
				if (getGeneAnno(i) == null)
					continue;

				int length = fontMetrics.stringWidth(getGeneAnno(i));
				if (length > max) {
					max = length;
				}
			}
			return max;
		}
		public int getLength(String txt) {
			if (txt == null) return 0;
			//	  FontMetrics fontMetrics = getFontMetrics(getGraphics().getFont());
			FontMetrics fontMetrics = getFontMetrics(getGeneFont());
			return fontMetrics.stringWidth(txt);
		}
		
		public int numArrayHeaders() {
			return arrayList.getSelectedIndices().length;
		}
		public int numGeneHeaders() {
			return geneList.getSelectedIndices().length;
		}
		public void deselectHeaders() {
			arrayList.clearSelection();
			geneList.clearSelection();
		}
		
		public boolean geneAnnoInside() {
			return geneAnnoInside.isSelected();
		}
		public boolean arrayAnnoInside() {
			return arrayAnnoInside.isSelected();
		}
		
		public void addNotify() {
			super.addNotify();
			inclusionPanel.recalculateBbox();
		}
		
		HeaderSelectionPanel() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			
			add(new JLabel("Gene Headers"));
			String [] geneHeaders = geneHeaderInfo.getNames();
			if (geneHeaders == null) {
				geneList = new JList(new String [0]);
			} else {
				geneList = new JList(geneHeaders);
			}
			geneList.setVisibleRowCount(5);
			add(new JScrollPane(geneList));
			
			geneAnnoInside = new JCheckBox("Right of Tree?");
			//	  add(geneAnnoInside);
			add(new JLabel("Array Headers"));
			
			String [] arrayHeaders = arrayHeaderInfo.getNames();
			if (arrayHeaders == null) {
				arrayList = new JList(new String [0]);
			} else {
				arrayList = new JList(arrayHeaders);
			}
			arrayList.setVisibleRowCount(5);
			add(new JScrollPane(arrayList));
			
			arrayAnnoInside = new JCheckBox("Below Tree?");
			arrayAnnoInside.setSelected(true);
			arrayAnnoInside.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					inclusionPanel.updateSize();
				}
			});
			add(arrayAnnoInside);
			
			ListSelectionListener tmp = new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (inclusionPanel != null) {
						inclusionPanel.recalculateBbox();
						inclusionPanel.updateSize();
						geneSummary.setIncluded(geneList.getSelectedIndices());
						arraySummary.setIncluded(arrayList.getSelectedIndices());
					}
				}
			};
			geneList.addListSelectionListener(tmp);
			arrayList.addListSelectionListener(tmp);
			arrayList.setSelectedIndex(0);
			geneList.setSelectedIndex(1);
			setupSelected();
		}
		public void setupSelected() {
			geneSummary.setIncluded(geneList.getSelectedIndices());
			arraySummary.setIncluded(arrayList.getSelectedIndices());
			if (inclusionPanel != null)
				inclusionPanel.updateSize();
		}
	}
	
	class FilePanel extends JPanel {
		private JTextField fileField;
		String getFilePath() {
			return fileField.getText();
		}
		File getFile() {
			return new File(getFilePath());
		}
		void setFilePath(String fp) {
			fileField.setText(fp);
			fileField.invalidate();
			fileField.revalidate();
			fileField.repaint();
			
		}
		public FilePanel(String initial) {
			super();
			add(new JLabel("Export To: "));
			fileField = new JTextField(initial);
			add(fileField);
			JButton chooseButton = new JButton("Browse");
			chooseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						JFileChooser chooser = new JFileChooser();
						int returnVal = chooser.showSaveDialog(GraphicsExportPanel.this);
						if(returnVal == JFileChooser.APPROVE_OPTION) {
							fileField.setText(chooser.getSelectedFile().getCanonicalPath());
						}
					} catch (java.io.IOException ex) {
						// logger.error("Got exception " + ex);
					}
				}
			});
			add(chooseButton);
		}
	}
	
}
