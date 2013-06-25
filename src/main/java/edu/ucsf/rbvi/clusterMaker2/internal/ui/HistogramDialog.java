package org.cytoscape.myapp.internal.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.ScrollPane;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.cytoscape.myapp.internal.algorithms.edgeConverters.ThresholdHeuristic;

public class HistogramDialog extends JDialog implements ActionListener, ComponentListener, HistoChangeListener {
	
	double[] inputArray;
	int nBins;
	int currentBins;
	Histogram histo;
	JPanel mainPanel;
	JPanel buttonBox;
	JScrollPane scrollPanel;
	JButton zoomOutButton;
	boolean isZoomed = false;
	List<HistoChangeListener> changeListenerList = null;
	double cutOff = -1.0;

	ThresholdHeuristic thueristic = null;
	
	public HistogramDialog(String title, double[] inputArray, int nBins, ThresholdHeuristic thueristic) {
		super();
		this.inputArray = inputArray;
		this.nBins = nBins;
		this.currentBins = nBins;
		this.changeListenerList = new ArrayList();
		this.thueristic = thueristic;

		setTitle(title);

		initializeOnce();
	}
	
	public void updateData(double[] inputArray) {
		this.inputArray = inputArray;
		if (histo != null) {
			histo.updateData(inputArray);
		}
	}

	public void setLineValue(double cutOffValue) {
		cutOff = cutOffValue;
		if (histo != null) {
			histo.setLineValue(cutOffValue);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("set"))
			histo.setBoolShowLine(true);
		if(e.getActionCommand().equals("close"))
			this.dispose();

		if(e.getActionCommand().equals("C")){}
		if(e.getActionCommand().equals("zoom")){
			currentBins = currentBins * 2;
			isZoomed = true;
			zoom(inputArray, false);
			zoomOutButton.setEnabled(true);
		}

	 
		if(e.getActionCommand().equals("zoomOut")){
			currentBins = currentBins / 2;
			if (currentBins == nBins) {
				isZoomed = false;
				zoomOutButton.setEnabled(false);
			}
			zoom(inputArray, true);
		}

		if(e.getActionCommand().equals("cuttoffHeuristic")){

			cutOff = thueristic.run();
		
			//Found cuttoff. Update histogram
			if(cutOff > -1000) {
				histoValueChanged((double)cutOff);
				histo.setBoolShowLine(true);
				histo.setLineValue((double)cutOff);
			}
		}
		
	}
	
	public void componentHidden(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}

	public void componentResized(ComponentEvent e) {
		// Get our new size & update histogram
		Dimension dim = e.getComponent().getSize();
		if(!isZoomed){
			histo.setPreferredSize(new Dimension(dim.width-10, dim.height));
		}
		else{
			Dimension histoSize = histo.getSize();
			histo.setPreferredSize(new Dimension(histoSize.width, dim.height));
			histo.repaint();
			scrollPanel.setPreferredSize(new Dimension(dim.width, dim.height));
		}
	}
	
private void initializeOnce() {
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.addComponentListener(this);
		

		// Create and add the histogram component
		histo = new Histogram(inputArray, nBins);
		histo.addHistoChangeListener(this);
		scrollPanel = new JScrollPane(histo, JScrollPane.VERTICAL_SCROLLBAR_NEVER, 
		                              JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		mainPanel.add(scrollPanel);
			
		// TODO: Add box to set lower and upper bounds.  Look at JText and JLabel

		// Create our button box
		buttonBox = new JPanel();

		// Close button
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		closeButton.setActionCommand("close");

		// OK button
		JButton okButton = new JButton("Set Cutoff");
		okButton.addActionListener(this);
		okButton.setActionCommand("set");

		//Cuttoff Selection Heuristic
		JButton convertButton = new JButton("Select Cutoff Heuristically");
		convertButton.addActionListener(this);
		convertButton.setActionCommand("cuttoffHeuristic");
		
		JButton zoomButton = new JButton("Zoom In");
		zoomButton.addActionListener(this);
		zoomButton.setActionCommand("zoom");

		zoomOutButton = new JButton("Zoom Out");
		zoomOutButton.addActionListener(this);
		zoomOutButton.setActionCommand("zoomOut");
		zoomOutButton.setEnabled(false);

		buttonBox.add(okButton);
		buttonBox.add(closeButton);
		buttonBox.add(zoomButton);
		buttonBox.add(zoomOutButton);
		buttonBox.add(convertButton);
		buttonBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		Dimension buttonDim = buttonBox.getPreferredSize();
		buttonBox.setMinimumSize(buttonDim);
		buttonBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonDim.height));
		mainPanel.add(buttonBox);

		setContentPane(mainPanel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
	}

	public void addHistoChangeListener(HistoChangeListener h){
		if (changeListenerList.contains(h)) return;
		changeListenerList.add(h);
	}
	
	public void removeHistoChangeListener(HistoChangeListener h){
		changeListenerList.remove(h);
	}
	
	public void histoValueChanged(double bounds){
		if (changeListenerList.size() == 0) return;
		cutOff = bounds;
		for (HistoChangeListener listener: changeListenerList)
			listener.histoValueChanged(bounds);
	}
	
	private void zoom(double[] inputArray, boolean zoomOut){
		
		// Get the width of the current histogram
		Dimension histoDim = histo.getSize();
		int histoWidth;

		if (zoomOut)
			histoWidth = histoDim.width / 2;
		else
			histoWidth = histoDim.width * 2;
			
		// Get the size of the scrollPanel
		Dimension dim = scrollPanel.getSize();

		if (isZoomed) {
			histo.setPreferredSize(new Dimension(histoWidth, histoDim.height));
		} else {
			histo.setPreferredSize(new Dimension(dim.width, dim.height));
		}

		histo.revalidate();
		if (cutOff != -1.0)
			histo.setLineValue(cutOff);

	}

	

}
