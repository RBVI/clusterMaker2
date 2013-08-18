package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.gui.Console;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.gui.MainFrame;

public class CostMatrixCreator {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ArgsParseException 
	 */
	public static void main(String[] args) throws IOException, ArgsParseException {
		
		Args options = new Args(args, "-");
		if(options.options.containsKey("help")||options.options.containsKey("h")||options.options.containsKey("-help")) {
			printUsage();
			System.exit(0);
		}
	
		Config.init(options);

		if(Config.gui){

			MainFrame f = new MainFrame();
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setVisible(true);
			
			// f.pack();
			f.setSize(900, 900);
			
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); 
			int top = (screenSize.height - f.getHeight()) / 2;
			int left = (screenSize.width - f.getWidth()) / 2; 
			
			f.setLocation(left, top);
			
			JPanel fPanel = (JPanel) f.getContentPane();
			
			fPanel.add(new JSeparator());
			fPanel.add(new JLabel(" "));
			fPanel.add(new JLabel("Status:"));
			fPanel.add(new JLabel(" "));
			fPanel.add(Console.getConsolePanel());
			
			Console.println("Welcome...");
			
			fPanel.updateUI();
			
		}else{
			
			HashMap<Integer,String> proteins2integers = new HashMap<Integer, String>();
			
			HashMap<String,Integer> integers2proteins = new HashMap<String, Integer>();
			
			if(Config.createSimilarityFile){
				
				Creator c = new Creator();
				c.run(proteins2integers,integers2proteins);
				
			} 
			
			if(Config.splitAndWriteCostMatrices){
				
				Splitter s = new Splitter();
				s.run(proteins2integers, integers2proteins);
				
			}
			
		}
		
	}

	public static void printUsage() {
		
		// System.out.println("manual for deriving cost matrices from blast files or arbitrary similarity files");
		// System.out.println();
		// System.out.println("options:");
		// System.out.println();
		
		// System.out.println("-help \t prints this page");
		// System.out.println();
		
		// System.out.println("-gui  (Boolean) \t----starts the program with/without graphical user interface, default true");
		// System.out.println();
		
		// System.out.println("-b blastFile (String) \t---- location of the BLAST file");
		// System.out.println();
		
		// System.out.println("-f fastaFile (String) \t---- location of the according FASTA file");
		// System.out.println();
		
		// System.out.println("-s similarityFile (String) \t---- location of the similarity file (where to store or where to read)");
		// System.out.println();
		
		// System.out.println("-c costMatrixDirectory (String) \t---- directory, where the resulting cost matrices should be stored" );
		// System.out.println();
		
		// System.out.println("-t threshold (Float) \t---- threshold for the similarity; default 0");
		// System.out.println();
		
		// System.out.println("-m costModel (Integer) \t---- which costmodel should be used (0:BeH, 1:SoH, 2:coverage+BeH, 3:coverage+SoH; default 0");
		// System.out.println();
		
		// System.out.println("-cf coverageFactor (Integer) \t---- if costModel=2||3 the influence of the coverage to the similarity; default 0");
		// System.out.println();
		
		// System.out.println("-cs createSimilarityFile (Boolean) \t---- true: use BLAST file to create similarityFile; default true");
		// System.out.println();
		
		// System.out.println("-sp splitAndWriteCostMatrices (Boolean) \t---- true: use similarity file to create cost matrices; default true");
		// System.out.println();
		
		// System.out.println("-bc blastCutoff (Float) \t---- which BLAST cutoff was used to create BLAST file; default 0.001");
		// System.out.println();
		
		// System.out.println("-p penaltyForMultipleHighScoringPairs (Float) \t---- if costModel=1||3 penalty for every additional High scoring pair; default 0.001");
		// System.out.println();
		
		// System.out.println("-ub upperBound (Float) \t----threshold for merging nodes; default 100");
		// System.out.println();
		
		// System.out.println("-rm reducedMatrix (Boolean) \t----merges all nodes that exceeds upper bound to one node and calculates the resulting costs; default true");
		// System.out.println();
		

	}

	

}
