package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.start;

import java.io.IOException;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.TransClustCluster;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.InvalidInputFileException;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.ArgsParseException;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.ArgsUtility;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.Console;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.ConsoleFormatter;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.UniqueFilter;

/**
 * This class is the main entry point to the program. It is the class that is started
 * from the console. It is possible to enter the parameters -gui to start the {@link TransClust},
 * or -help for usage information or normally with necessary parameters.
 * 
 * @author Sita Lange
 *
 */
public class TransClust {
	
	private static Logger log = Logger.getLogger(TransClust.class.getName());
	
	public static String[] args;

	/**
	 * The main method for the TransClust program. Starts the program
	 * appropriately.
	 * @param args The input variables.
	 */
	public static void main(String[] args) {
		TransClust.args = args;
		/* initialise logging */
		Logger logger = Logger.getLogger("");
		Handler[] handler = logger.getHandlers();
		for (Handler h : handler) {
			if(h instanceof ConsoleHandler){
				h.setLevel(Level.INFO);
				h.setFormatter(new ConsoleFormatter()); 
				h.setFilter(new UniqueFilter());
			}
		}
		
//		try { // add log file handler
//			int limit =  1000000; // 1MB
//			/* limit log file to the given 'limit' size */
//			FileHandler fileHandler = new FileHandler(TaskConfig.NAME+".log", limit, 1);
//			fileHandler.setLevel(Level.WARNING);
//			fileHandler.setFormatter(new SimpleFormatter());
//			logger.addHandler(fileHandler);
//		} catch (SecurityException e1) {
//			e1.printStackTrace();
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
		
		/* check if no input is given */
		if(args.length ==0){
			log.severe("ERROR: Please define at least an input file/directory and an " +
					"output file for the results. Use -help for more details or see the " +
					"respective documentation.\n\n");
			System.out.println(ArgsUtility.createUsage().toString());
			System.exit(-1);
		}
		
		/* print usage */
		if ((args.length == 1) && ((args[0].trim().equalsIgnoreCase("-help")) || 
				(args[0].trim().equalsIgnoreCase("--help")))) {
			System.out.println(ArgsUtility.createUsage().toString());
			System.exit(-1);
		}		
		
		/* start with parameters from console */
		else{
			
			try {
				new Console(args);
			} catch (InvalidInputFileException e) {
				log.severe("ERROR: An invalid file/path name was given.");
				e.printStackTrace();
				System.exit(-1);
			} catch (ArgsParseException e) {
				log.severe(e.getMessage());
				log.severe("ERROR: please see usage details!");
				System.out.println(ArgsUtility.createUsage().toString());
			} catch (IOException e) {
			}
		}
	}
}
