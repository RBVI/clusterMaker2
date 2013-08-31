package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main.Config;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main.Creator;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main.Splitter;


public class MainFrame extends JFrame implements ActionListener {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel fcPanelBlast = new JPanel();
	JPanel fcPanelBlastText = new JPanel();
	JPanel fcPanelFasta = new JPanel();
	JPanel fcPanelFastaText = new JPanel();
	JPanel fcPanelSim = new JPanel();
	JPanel fcPanelSimText = new JPanel();
	JPanel fcPanelCmDir = new JPanel();
	JPanel fcPanelCmDirText = new JPanel();
	

	JButton exit = new JButton("EXIT");
	JButton run = new JButton("RUN");
	JButton blast = new JButton("open BLAST file");
	JFileChooser fcBlast = new JFileChooser();
	JButton fasta = new JButton("open Fasta file");
	JFileChooser fcFasta = new JFileChooser();
	JButton sim = new JButton("choose sim file");
	JFileChooser fcSim = new JFileChooser();
	JButton cmDir = new JButton("choose cmDir ");
	JFileChooser fcCmDir = new JFileChooser();
	JPanel mainPanel = new JPanel();
	
	JRadioButton createSimilartyFile = new JRadioButton("createSimilartyFile");
	JRadioButton createCostMatrices = new JRadioButton("createCostMatrices");

	
	public MainFrame() {
		
		super("CostMatrixCreator");
		
		this.setSize(800, 600);
		
		mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
		
	
		fcPanelBlast.setLayout(new BoxLayout(fcPanelBlast,BoxLayout.X_AXIS));
		blast.setActionCommand("blast");
		blast.addActionListener(this);
		fcPanelBlast.add(blast);

		fcPanelBlastText.setLayout(new BoxLayout(fcPanelBlastText,BoxLayout.X_AXIS));
		JLabel blastFile = new JLabel();
		if(Config.blastFile==null) blastFile.setText("not chosen yet");
		else blastFile.setText(Config.blastFile);
		fcPanelBlastText.add(blastFile);

		fcPanelFasta.setLayout(new BoxLayout(fcPanelFasta,BoxLayout.X_AXIS));
		fasta.setActionCommand("fasta");
		fasta.addActionListener(this);
		fcPanelFasta.add(fasta);

		fcPanelFastaText.setLayout(new BoxLayout(fcPanelFastaText,BoxLayout.X_AXIS));
		JLabel fastaFile = new JLabel();
		if(Config.fastaFile==null) fastaFile.setText("not choosen yet");
		else fastaFile.setText(Config.fastaFile);
		fcPanelFastaText.add(fastaFile);

		fcPanelSim.setLayout(new BoxLayout(fcPanelSim,BoxLayout.X_AXIS));
		sim.setActionCommand("sim");
		sim.addActionListener(this);
		fcPanelSim.add(sim);

		fcPanelSimText.setLayout(new BoxLayout(fcPanelSimText,BoxLayout.X_AXIS));
		JLabel simFile = new JLabel();
		if(Config.similarityFile==null) simFile.setText("not choosen yet");
		else simFile.setText(Config.similarityFile);
		fcPanelSimText.add(simFile);
		

		fcPanelCmDir.setLayout(new BoxLayout(fcPanelCmDir,BoxLayout.X_AXIS));
		cmDir.setActionCommand("cmDir");
		cmDir.addActionListener(this);
		fcPanelCmDir.add(cmDir);

		fcPanelCmDirText.setLayout(new BoxLayout(fcPanelCmDirText,BoxLayout.X_AXIS));
		JLabel cmDirFile = new JLabel();
		if(Config.costMatrixDirectory==null)	cmDirFile.setText("not choosen yet");
		else cmDirFile.setText(Config.costMatrixDirectory);
		fcPanelCmDirText.add(cmDirFile);



		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
		
		run.setActionCommand("run");
		run.addActionListener(this);
		buttonPanel.add(run);
		
		exit.setActionCommand("exit");
		exit.addActionListener(this);
		buttonPanel.add(exit);
		

		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel,BoxLayout.X_AXIS));
		
		if(Config.createSimilarityFile)	createSimilartyFile.setSelected(true);
		if(Config.splitAndWriteCostMatrices) createCostMatrices.setSelected(true);
		
		radioButtonPanel.add(new JLabel("Mode: "));
		radioButtonPanel.add(createSimilartyFile);

		radioButtonPanel.add(new JLabel("             "));
		radioButtonPanel.add(createCostMatrices);
		
		

		mainPanel.add(new JLabel("   "));
		
		mainPanel.add(radioButtonPanel);
		
		mainPanel.add(new JLabel("   "));
		mainPanel.add(new JSeparator());
		mainPanel.add(new JLabel("   "));
		
		mainPanel.add(fcPanelBlast);
		mainPanel.add(fcPanelBlastText);
		mainPanel.add(new JLabel("   "));
		mainPanel.add(new JSeparator());
		mainPanel.add(new JLabel("   "));
		mainPanel.add(fcPanelFasta);
		mainPanel.add(fcPanelFastaText);
		mainPanel.add(new JLabel("   "));
		mainPanel.add(new JSeparator());
		mainPanel.add(new JLabel("   "));
		mainPanel.add(fcPanelSim);
		mainPanel.add(fcPanelSimText);
		mainPanel.add(new JLabel("   "));
		mainPanel.add(new JSeparator());
		mainPanel.add(new JLabel("   "));
		mainPanel.add(fcPanelCmDir);
		mainPanel.add(fcPanelCmDirText);

		
		mainPanel.add(new JLabel("   "));
		mainPanel.add(new JSeparator());
		mainPanel.add(new JLabel("   "));
		
		
		
		mainPanel.add(buttonPanel);
		
		
		
		
		JPanel root = (JPanel) this.getContentPane();
		root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Main", mainPanel);
		
		JScrollPane optionsPanel = new JScrollPane(makeOptionsPanel());
		tabbedPane.add("Options", optionsPanel);
		
		
		root.add(tabbedPane);
		
	}
	
	
	
	
	
	private JTextField threshold = new JTextField(Float.toString(Config.threshold));
				
	private JTextField penaltyForMultipleHighScoringPairs = new JTextField(Double.toString(Config.penaltyForMultipleHighScoringPairs));
	
	private JTextField blastCutoff = new JTextField(Double.toString(Config.blastCutoff));
	
	private JTextField upperBound = new JTextField(Float.toString(Config.upperBound));
	
	private JTextField coverageFactor = new JTextField(Integer.toString(Config.coverageFactor));
	
	private JRadioButton BeH = new JRadioButton();
	
	private JRadioButton SoH = new JRadioButton();
	
	private JRadioButton BeHCoverage = new JRadioButton();
	
	private JRadioButton SoHCoverage = new JRadioButton();
	
	private JRadioButton reduceMatrices = new JRadioButton();
	

	private JPanel makeOptionsPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		
		addOptionSplit(p, "Similarity file creation");
		
		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel,BoxLayout.X_AXIS));
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(BeH);
		bg.add(SoH);
		bg.add(BeHCoverage);
		bg.add(SoHCoverage);
	
		if(Config.costModel==0) {
			BeH.setSelected(true);
			penaltyForMultipleHighScoringPairs.setEnabled(false);
			coverageFactor.setEnabled(false);
		} else if(Config.costModel==1) {
			SoH.setSelected(true);
			coverageFactor.setEnabled(false);
		} else if(Config.costModel==2){
			BeHCoverage.setSelected(true);
			penaltyForMultipleHighScoringPairs.setEnabled(false);
		}else if(Config.costModel==3){
			SoHCoverage.setSelected(true);
		}
		BeH.setActionCommand("costModel");
		BeH.addActionListener(this);
		SoH.setActionCommand("costModel");
		SoH.addActionListener(this);
		BeHCoverage.setActionCommand("costModel");
		BeHCoverage.addActionListener(this);
		SoHCoverage.setActionCommand("costModel");
		SoHCoverage.addActionListener(this);
		
		radioButtonPanel.add(new JLabel("\t BeH:"));
		radioButtonPanel.add(BeH);
		radioButtonPanel.add(new JLabel("\t SoH"));
		radioButtonPanel.add(SoH);
		radioButtonPanel.add(new JLabel("\t BeHCoverage"));
		radioButtonPanel.add(BeHCoverage);
		radioButtonPanel.add(new JLabel("\t SoHCoverage"));
		radioButtonPanel.add(SoHCoverage);
		
		
		addOption(p, "costModel:  ", radioButtonPanel, "");
		addOption(p, "penalty for multiple HSPs", penaltyForMultipleHighScoringPairs, "");
		addOption(p, "BLAST cutoff", blastCutoff, "");
		addOption(p, "coverage Factor", coverageFactor, "");
		
		
		addOptionSplit(p, "CostMatrixCreation");
		
		addOption(p, "threshold", threshold, "");
		
		if(Config.reducedMatrix) reduceMatrices.setSelected(true);
		else{
			reduceMatrices.setSelected(false);
			upperBound.setEnabled(false);
		}
		JPanel radioButtonPanel2 = new JPanel();
		radioButtonPanel2.setLayout(new BoxLayout(radioButtonPanel2,BoxLayout.X_AXIS));
		radioButtonPanel2.add(new JLabel("reduce Matrices "));
		radioButtonPanel2.add(reduceMatrices);
		reduceMatrices.setActionCommand("reduceMatrices");
		reduceMatrices.addActionListener(this);
		
		addOption(p, "", radioButtonPanel2, "");
		
		addOption(p, "upper Bound", upperBound,"");

		// to set the size properly
		JPanel dummy = new JPanel();
		dummy.setPreferredSize(new Dimension(200, 200));
		p.add(dummy);
		
		return p;
	}
	
	private void addOption(JPanel p, String before, JComponent c, String after) {
		
		JPanel d = new JPanel();
		d.setLayout(new BoxLayout(d, BoxLayout.X_AXIS));
		d.add(new JLabel(before + " "));
		d.add(c);
		d.add(new JLabel(" " + after));
		
		p.add(d);
		
	}
	
	private void addOptionSplit(JPanel p, String s) {
		p.add(new JSeparator());
		p.add(new JLabel(" "));
		p.add(new JLabel(s));
		p.add(new JLabel(" "));
		p.add(new JSeparator());
	}
	
	private void setOptions() {
		
		Config.blastCutoff = Double.parseDouble(blastCutoff.getText());
		Config.coverageFactor = Integer.parseInt(coverageFactor.getText());
		Config.threshold = Float.parseFloat(threshold.getText());
		Config.penaltyForMultipleHighScoringPairs = Float.parseFloat(penaltyForMultipleHighScoringPairs.getText());
		Config.upperBound = Float.parseFloat(upperBound.getText());
		
		if(BeH.isSelected()) Config.costModel =0;
		else if(SoH.isSelected()) Config.costModel =1;
		else if(BeHCoverage.isSelected()) Config.costModel =2;
		else if(SoHCoverage.isSelected()) Config.costModel =3;
		
	}
	
		
	@SuppressWarnings("deprecation")
	public void actionPerformed(ActionEvent e) {
		String s = e.getActionCommand();
		
		if (s.equalsIgnoreCase("run")) {
			
			setOptions();
			Config.splitAndWriteCostMatrices = createCostMatrices.isSelected();
			Config.createSimilarityFile = createSimilartyFile.isSelected();
			
			//check for not specified files
			boolean error = false;
			if(Config.createSimilarityFile){
				if(Config.blastFile==null){
					JLabel dum = (JLabel) fcPanelBlastText.getComponent(0);
					Color fg = new Color(255, 0, 0);
					dum.setForeground(fg);
					dum.setText("please specify Blast file");
					error = true;
				}
				if(Config.fastaFile==null){
					JLabel dum = (JLabel) fcPanelFastaText.getComponent(0);
					Color fg = new Color(255, 0, 0);
					dum.setForeground(fg);
					dum.setText("please specify Fasta file");
					error = true;
				}
				if(Config.similarityFile==null){
					JLabel dum = (JLabel) fcPanelSimText.getComponent(0);
					Color fg = new Color(255, 0, 0);
					dum.setForeground(fg);
					dum.setText("please specify similarity file");
					error = true;
				}
			}
			
			if(Config.splitAndWriteCostMatrices){
				if(Config.similarityFile==null){
					JLabel dum = (JLabel) fcPanelSimText.getComponent(0);
					Color fg = new Color(255, 0, 0);
					dum.setForeground(fg);
					dum.setText("please specify similarity file");
					error = true;
				}
				if(Config.costMatrixDirectory==null){
					JLabel dum = (JLabel) fcPanelCmDirText.getComponent(0);
					Color fg = new Color(255, 0, 0);
					dum.setForeground(fg);
					dum.setText("please specify costMatrix directory");
					error = true;
				}
			}
			
			
			if(error) return;
			t = new MainThread(run, exit, new Semaphore(1));
			t.start();
			
			
		} else if (s.equalsIgnoreCase("exit")) {
			System.exit(0);
		} else if (s.equalsIgnoreCase("stop")) {
			t.stop();
			Console.println();
			Console.println("STOPPED.");
			Console.println();
			
			exit.setText("EXIT");
			exit.setActionCommand("exit");
			run.setEnabled(true);
		} else if(s.equalsIgnoreCase("blast")){
			fcBlast.showOpenDialog(mainPanel);
			Config.blastFile = fcBlast.getSelectedFile().getAbsolutePath();
			JLabel dum = (JLabel) fcPanelBlastText.getComponent(0);
			Color fg = new Color(0, 0, 0);
			dum.setForeground(fg);
			dum.setText(Config.blastFile);

		}else if(s.equalsIgnoreCase("fasta")){
			fcFasta.showOpenDialog(mainPanel);
			Config.fastaFile = fcFasta.getSelectedFile().getAbsolutePath();
			JLabel dum = (JLabel) fcPanelFastaText.getComponent(0);
			Color fg = new Color(0, 0, 0);
			dum.setForeground(fg);
			dum.setText(Config.fastaFile);
		}else if(s.equalsIgnoreCase("sim")){
			if(createSimilartyFile.isSelected()){
				fcSim.showSaveDialog(mainPanel);
			}else{
				fcSim.showOpenDialog(mainPanel);
			}
			Config.similarityFile = fcSim.getSelectedFile().getAbsolutePath();
			JLabel dum = (JLabel) fcPanelSimText.getComponent(0);
			Color fg = new Color(0, 0, 0);
			dum.setForeground(fg);
			dum.setText(Config.similarityFile);
		}else if(s.equalsIgnoreCase("cmDir")){
			fcCmDir.setFileSelectionMode(1);
			fcCmDir.showSaveDialog(mainPanel);
			Config.costMatrixDirectory = fcCmDir.getSelectedFile().getAbsolutePath();
			JLabel dum = (JLabel) fcPanelCmDirText.getComponent(0);
			Color fg = new Color(0, 0, 0);
			dum.setForeground(fg);
			dum.setText(Config.costMatrixDirectory);
		} else if(s.equalsIgnoreCase("reduceMatrices")){
				Config.reducedMatrix = reduceMatrices.isSelected();
				if(Config.reducedMatrix){
					upperBound.setEnabled(true);
				}else{
					upperBound.setEnabled(false);
				}
		} else if(s.equalsIgnoreCase("costModel")){
			
			if(BeH.isSelected()) {
				blastCutoff.setEnabled(true);
				penaltyForMultipleHighScoringPairs.setEnabled(false);
				coverageFactor.setEnabled(false);
			} else if(SoH.isSelected()) {
				coverageFactor.setEnabled(false);
				blastCutoff.setEnabled(true);
				penaltyForMultipleHighScoringPairs.setEnabled(true);
			} else if(BeHCoverage.isSelected()){
				coverageFactor.setEnabled(true);
				blastCutoff.setEnabled(true);
				penaltyForMultipleHighScoringPairs.setEnabled(false);
			}else if(SoHCoverage.isSelected()){
				coverageFactor.setEnabled(true);
				blastCutoff.setEnabled(true);
				penaltyForMultipleHighScoringPairs.setEnabled(true);
			}
			
		}
			
	}
	
	public static MainThread t;

	
	public class MainThread extends Thread {
			
	//		Parameters p;
			JButton run, exit;
			Semaphore sem;
			
			public MainThread(JButton run, JButton exit, Semaphore s) {
				
				this.run = run;
				this.exit = exit;
				this.sem = s;
				exit.setActionCommand("stop");
			}
			
			public void run() {
				
				
				HashMap<Integer,String> proteins2integers = new HashMap<Integer, String>();
				
				HashMap<String,Integer> integers2proteins = new HashMap<String, Integer>();
				
				if(Config.createSimilarityFile){
					Creator c = new Creator();
					try {
						c.run(proteins2integers,integers2proteins);
					} catch (IOException e) {
					}
				} 
				if(Config.splitAndWriteCostMatrices){
					Splitter s = new Splitter();
					try {
						s.run(proteins2integers, integers2proteins);
					} catch (IOException e) {
					}
				}
				
				Console.println("FINISHED.");
				
				exit.setText("EXIT");
				exit.setActionCommand("exit");
				run.setEnabled(true);
				sem.release();
			}
	}

	
}


































