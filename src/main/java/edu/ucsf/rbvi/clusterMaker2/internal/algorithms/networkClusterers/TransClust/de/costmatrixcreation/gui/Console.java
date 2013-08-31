package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.gui;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class Console {
	
	public static JTextArea area;
	public static JProgressBar progressBar;
	public static JProgressBar progressBar2;
	
	private static JPanel root = new JPanel();
	
	static {
		
		area = new JTextArea();
		
		root.setLayout(new BoxLayout(root,BoxLayout.Y_AXIS));
				
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setString("Adjust variables and click RUN to start program.");
		
		
		progressBar2 = new JProgressBar(0, 100);
		progressBar2.setValue(0);
		progressBar2.setStringPainted(true);
		progressBar2.setString("test");
		
		root.add(new JScrollPane(area));
		root.add(progressBar);
				
		root.setPreferredSize(new Dimension(900, 500));
		
	}
	
	public static JPanel getConsolePanel() {
		return root;
	}
	
	public static void println(String s) {
		area.setText(area.getText() + s + "\n");
		area.setCaretPosition(area.getText().length());
	}
	
	public static void println() {
		println("");
	}
	
	public static void print(String s) {
		area.setText(area.getText() + s);
		area.setCaretPosition(area.getText().length());
	}
	
	public static void setBarMin(int x) {
		progressBar.setMinimum(x);
	}
	
	public static void setBarMax(int x) {
		progressBar.setMaximum(x);
	}
	
	public static void setBarText(String x) {
		progressBar.setString(x);
	}
	
	static long stopTime = 0;
	static long timePerRun = 0;
	static long restTime = 0;
	static long startTime = 0;
	
	public static void restartBarTimer() {
		startTime = System.currentTimeMillis();
	}
	
	public static void restartBar(int min, int max) {
		progressBar.setMinimum(min);
		progressBar.setMaximum(max);
		restartBarTimer();
	}
	
	public static void setBarTextPlusRestTime(String x) {
		progressBar.setString(x + " - Remaining: " + getTimeString(restTime));
	}
	
	public static void setBarValue(int x) {
		progressBar.setValue(x);
		
		stopTime = System.currentTimeMillis();
		timePerRun = (stopTime - startTime)/(x+1);
		restTime = timePerRun * (progressBar.getMaximum()-x);
	}
	
	public static String getTimeString(long diff) {
		
		int h = 0;
		int m = 0;
		int s = 0;
		
		s = (int) Math.rint(diff/1000);
		m = (int) Math.rint(s/60);
		h = (int) Math.rint(m/60);
		
		String str = new String();
		
		if ((h == 0) && (m == 0)) {
			str = s + " s";
		} else if (h == 0) {
			s = s - (m*60);
			str = m + " min " + s + " s";
		} else {
			m = m - (h*60);
			s = s - (m*60);
			str = h + " h " + m + " min " + s + " s";
		}
		
		return str;
	}
	
}












