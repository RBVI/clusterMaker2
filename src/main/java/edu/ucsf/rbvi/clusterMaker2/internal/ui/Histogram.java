package edu.ucsf.rbvi.clusterMaker2.internal.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.text.AttributedCharacterIterator;

import java.lang.Math;

import java.util.ArrayList;
import java.util.List;

import java.text.DecimalFormat;

import javax.swing.JComponent;

public class Histogram extends JComponent implements MouseMotionListener, MouseListener {
	
	// The histogram
		private int[] histoArray;

		// Original data
		private double[] graphData;

		// Y scale values
		private int histoMax = Integer.MIN_VALUE;
		private int histoMin = 0;
		private int histoMaxUp = 0;

		// X scale values
		private double minValue = Double.MAX_VALUE;
		private double maxValue = Double.MIN_VALUE;
		private double low;
		private double high;

		private final int XSTART = 100;
		private final int YEND = 50;
		private final int NBINS;
		private int mouseX;
		private boolean boolShowLine = false;
		private List<HistoChangeListener> listeners = null;
		private double xInterval;
		private double xIncrement;

		private int height;
		private int width;

		private int yTicks = 10;
		private int xTicks = 10;

		private static final String FONT_FAMILY = "SansSerif";

		private Font adjFont;

		DecimalFormat form = new DecimalFormat("0.0E0"); //rounds values for drawString
			
		Histogram(double[] inputData, int nBins) {
			super();
			NBINS = nBins;
			height = 400;
			width = 1000;
			setPreferredSize(new Dimension(width,height));
			histoArray = new int[NBINS];
			this.graphData = inputData;
			listeners = new ArrayList<HistoChangeListener>();

			adjFont = new Font(FONT_FAMILY, Font.PLAIN, 14);

			createHistogram(graphData);

			addMouseMotionListener(this);
			addMouseListener(this);
		}

		public void updateData(double[] graphData) {
			// Trigger redraw
			histoArray = new int[NBINS];
			this.graphData = graphData;

			minValue = Double.MAX_VALUE;
			maxValue = Double.MIN_VALUE;
			histoMax = Integer.MIN_VALUE;
			histoMaxUp = 0;
			createHistogram(graphData);
			this.repaint();
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Dimension dim = getSize();
			if (dim.width == 0 || dim.height == 0) {
				dim = getPreferredSize();
			}

			width = dim.width;
			height = dim.height;
			xIncrement = (double)(width-200)/(double)NBINS;

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				                  RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				                  RenderingHints.VALUE_RENDER_QUALITY);

			drawGraph(g2);
			if(boolShowLine)
				mouseLine(mouseX, g);
		}

		public void mouseMoved(MouseEvent e) {}
		public void mouseDragged(MouseEvent e) { rePaintMouseLine(e.getX()); }
		public void mouseClicked(MouseEvent e){}
		public void mouseEntered(MouseEvent e){}
		public void mouseExited(MouseEvent e){}
		public void mousePressed(MouseEvent e){ rePaintMouseLine(e.getX()); }
		
		public void mouseReleased(MouseEvent e){
			int histoMousePos = (int)(((double)(e.getX()-XSTART))/xIncrement);
			if(e.getX()>XSTART && e.getX()<(XSTART+xIncrement*histoArray.length) && boolShowLine){
				double binValue = xInterval*histoMousePos;
				// System.out.println("histoArray["+histoMousePos+"] = "+ histoArray[histoMousePos]+", "+Double.parseDouble(form.format((binValue))));
				if (listeners.size() == 0) return;
				for (HistoChangeListener listener: listeners)
					listener.histoValueChanged(Double.parseDouble(form.format(binValue)));
			}

		}

		/**
		 * Shows the selection line if true
		 *
		 * @param inShowLine if true, show the manual selection line
		 */
		public void setBoolShowLine(boolean inShowLine){boolShowLine = inShowLine;}

		/**
		 * Sets the value of the selection line
		 *
		 * @param cutOffValue the value of the selection (for use by the heuristic selection)
		 */
		public void setLineValue(double cutOffValue) {
			// System.out.println("Setting line value to: "+cutOffValue);
			// mouseX = ((int)((cutOffValue-low)/xIncrement))+XSTART;
			mouseX = (int)((cutOffValue/xInterval) * xIncrement) + XSTART;
			int histoMousePos = (int)(((double)(mouseX-XSTART))/xIncrement);
			double binValue = xInterval*histoMousePos;
			// System.out.println("histoArray["+histoMousePos+"] = "+ histoArray[histoMousePos]+", "+Double.parseDouble(form.format((binValue))));
			if (boolShowLine)
				rePaintMouseLine(mouseX);
		}

		/**
		 * Add a new change listener to this histogram
		 *
		 * @param listener the HistoChangeListener to call when the cutoff value changes
		 */
		public void addHistoChangeListener(HistoChangeListener listener) {
			if (listeners.contains(listener)) return;
			listeners.add(listener);
		}

		/**
		 * Remove a change listener from this histogram
		 *
		 * @param listener the HistoChangeListener to remove
		 */
		public void removeHistoChangeListener(HistoChangeListener listener) {
			listeners.remove(listener);
		}

				
		private void mouseLine(int mX, Graphics g){
			int histoMousePos = (int)((double)(mX-XSTART)/xIncrement);
			if(histoMousePos >= histoArray.length)
				histoMousePos = histoArray.length-1;

			g.setColor(Color.red);
			g.drawLine(mX, YEND, mX, height);
			g.setColor(Color.black);
			g.setFont(adjFont);
			g.drawString(toSciNotation(form.format(xInterval*histoMousePos).toString()," ("+histoArray[histoMousePos]+" values)"),mX-50,YEND-5);
		}

		private void rePaintMouseLine(int xPos) {
			repaint(mouseX-1, YEND, 2, height-YEND);
			repaint(mouseX-50, YEND-30, 150, 30);
			if(xPos>XSTART && boolShowLine){
				mouseX = xPos;
				repaint(mouseX-1, YEND, 2, height-YEND);
				repaint(mouseX-50, YEND-30, 150, 30);
			}
		}

		private void createHistogram(double[] inputData){
			calculateXScale();

			// System.out.println("Creating histogram: low = "+low);
			
			// Bin the data
			for(double dataItr : inputData){
				for(int nI=0; nI < NBINS; nI++){
					if(dataItr==low){
						histoArray[0]+=1;
						break;
					}
					if(dataItr>low+xInterval*nI && dataItr<=low+xInterval*(nI+1) ){
						histoArray[nI]+=1;
						break;
					}
				}
			}
			calculateYScale();
		}

		private void calculateXScale() {

			// Calculate our minimum and maximum X values
			for (int i=0; i < graphData.length; i++) {
				minValue = Math.min(minValue, graphData[i]);
				maxValue = Math.max(maxValue, graphData[i]);
			}

			// Calculate our X scale
			double range = maxValue - minValue;
			double oomRange = Math.log10(range); //order of magnitude
			// System.out.println("oomRange = "+oomRange+", range = "+range+", minValue = "+minValue+", maxValue = "+maxValue);
			oomRange = oomRange + (.5*oomRange/Math.abs(oomRange)); // Increase our oom by .5
			oomRange = (int)(oomRange); //make it an integer

			high = (Math.rint((maxValue/Math.pow(10, oomRange))+.5)) * (Math.pow(10, oomRange)); // This is our initial high value

			if (maxValue <= high/2) 
				high = high/2; // A little fine-tuning

			low = (Math.rint((minValue/Math.pow(10, oomRange))-.5)) * Math.pow(10,oomRange);

			if (minValue >= low/2) 
				low = low/2;

			xInterval = (high - low) / NBINS;
		}
		
		private void calculateYScale() {
			histoMin = 0;

			// First, determine the max value
			for(int nI=0; nI<histoArray.length; nI++){ 
				histoMax = Math.max(histoMax, histoArray[nI]);
			}

			while(histoMax > histoMaxUp)
				histoMaxUp += (int)(Math.pow(10,(int)(Math.log10(histoMax))));

			if(histoMaxUp<10)
				histoMaxUp = 10;
		}
		
		private void drawGraph(Graphics2D g){
			Dimension dim = getSize();
			if (dim.height == 0 || dim.width == 0) {
				dim = getPreferredSize();
			}
			int height = dim.height-100;
			int width = dim.width;

			// Since we allow scaling in the X dimension, we want to
			// check to see if we should increase the number of
			// ticks on the X axis
			if (NBINS > 1000)
				xTicks = 20;
			if (NBINS > 5000)
				xTicks = 50;
			if (NBINS > 10000)
				xTicks = 100;

			drawAxes(g, height, width);
			drawLabels(g, height, width);
			drawData(g, height, width);
		}
		
		private void drawAxes(Graphics2D g, int height, int width) {
			int maxX = (int)(xIncrement*NBINS+XSTART);

			// Draw the Y axis
			g.setColor(Color.black);
			g.drawLine(XSTART,YEND,XSTART,height);

			// Draw the X axis
			g.drawLine(XSTART,height,maxX,height);
			
			// Draw the Y incremental lines
			double yIncrement = (height-YEND)/(double)histoMaxUp;
			for(int nI=1;nI<=histoMaxUp;nI++){
				if(((double)nI%((double)histoMaxUp/yTicks)) == 0.0){
					g.setColor(Color.red);
					g.drawLine(XSTART-5,(int)(height-(yIncrement*nI)),maxX,(int)(height-(yIncrement*nI)));
				}
				else if(((double)nI%((double)histoMaxUp/(yTicks*2))) == 0.0){
					g.setColor(Color.gray);
					g.drawLine(XSTART,(int)(height-(yIncrement*nI)),maxX,(int)(height-(yIncrement*nI)));
				}
			}

			g.setColor(Color.black);
			for(int nI=0; nI<NBINS; nI++){
				int x = XSTART + (int)(xIncrement * nI);
				if(nI%(NBINS/xTicks)==0){
					g.drawLine(x,height,x,height+10);
				} else if (nI%(NBINS/(xTicks*5)) == 0) {
					g.drawLine(x,height,x,height+5);
				}
			}
		}
		
		private void drawLabels(Graphics2D g, int height, int width) {
			g.setColor(Color.black);
			g.setFont(adjFont);
			FontMetrics metrics = g.getFontMetrics();

			// Draw the Y labels
			double yIncrement = (height-YEND)/(double)histoMaxUp;
			for(int nI=1;nI<=histoMaxUp;nI++){
				String str = ""+nI;
				int offset = 90-metrics.stringWidth(str);

				if(nI%(histoMaxUp/yTicks)==0)
					g.drawString(str, offset, height-(int)(yIncrement*nI)+5);
			}

			// Now draw the X labels
			for(int nI=0; nI<=NBINS; nI++){
				double value = low+(xInterval*nI);
				String str = form.format(value);
				int offset = XSTART+metrics.stringWidth(str)/2 - 50;
				if (value == 0 || (value > 1 && value < 10))
					offset += 20;

				int x = (int)(xIncrement*nI)+offset;
				if(nI%(NBINS/xTicks)==0)
					g.drawString(toSciNotation(str, ""),x,height+25);
			}
		}
		
		// TODO: Change this method to use height and width.  You may need to scale the
		// the font also.
		private void drawData(Graphics2D g, int height, int width){
			int nBlueChange = 100;
			int barWidth = 0;

			double yIncrement = (height-50)/(double)(histoMaxUp);
			//System.out.println("yIncrement = "+yIncrement);
			double xValue = low;

			if (xIncrement < 1)
				barWidth = 1;
			else
				barWidth = (int) xIncrement;
			
			for(int nI=0; nI<NBINS; nI++){
				double barHeight = histoArray[nI]*yIncrement;

				if (barHeight > 0) {
					g.setColor(new Color(0,0,nBlueChange));
					g.fillRect(XSTART+(int)(xIncrement*nI), (int)(height-barHeight), barWidth, (int)barHeight);
					g.setColor(Color.black);
					g.drawRect(XSTART+(int)(xIncrement*nI), (int)(height-barHeight), barWidth, (int)barHeight);
				}

				nBlueChange+=15;
				if(nBlueChange >= 250)
					nBlueChange = 100;
				xValue += xInterval;
			}
		}
		
		private AttributedCharacterIterator toSciNotation(String d, String suffix){
			String returnString = "";
			for(int i=0; i<d.length(); i++){
				if(d.charAt(i)== 'E')
					break;
				returnString+=d.charAt(i);
			}

			String exponent = "";
			for(int i=d.length()-1; i>0; i--){
				if(d.charAt(i)== 'E')
					exponent+=d.substring(i+1,d.length());
			}

			AttributedString str;
			if (exponent.length() == 0 || Integer.parseInt(exponent) == 0) {
				str = new AttributedString(returnString+suffix);
				str.addAttribute(TextAttribute.FONT, adjFont);
			} else {
				returnString += "x10";
				int superOffset = returnString.length();
				returnString += exponent;
				int superEnd = returnString.length();

				str = new AttributedString(returnString+suffix);
				str.addAttribute(TextAttribute.FAMILY, FONT_FAMILY);
				str.addAttribute(TextAttribute.SIZE, new Float(14));
				str.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, superOffset, superEnd);
			}

			return str.getIterator();
		}

}
