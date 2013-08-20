/* 
 * Created on 28. January 2008
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
// import java.util.logging.ConsoleHandler;
// import java.util.logging.Handler;
// import java.util.logging.Level;
// import java.util.logging.Logger;

import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.TransClustCluster;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main.Config;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering.GeometricClusteringConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering.GeometricClusteringFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.forcend.FORCEnDLayoutConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.parameter_training.ParameterTrainingFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PostProcessingFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.ClusteringManagerTask;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.InvalidInputFileException;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.InvalidTypeException;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

/**
 * Parses the input from the console and starts the appropriate mode with the
 * given input parameters.
 * 
 * @author Sita Lange
 * 
 */
public class Console {

	// private static Logger log = Logger.getLogger(Console.class.getName());

	private String[] args = null;

	public Console(String[] args) throws InvalidInputFileException,
			ArgsParseException, IOException {
		this.args = args;

		//log.debug("Available processors in system: "
		//		+ Runtime.getRuntime().availableProcessors());

		parseArgsAndInitProgram();
	}

	/**
	 * This method parses the input parameters from the console and starts the
	 * program with the correct parameters and in the correct mode. At this
	 * stage all input parameters are in this form: key value. Both key and
	 * value contain no spaces and if the value does, then it is bounded by
	 * apostrophes.
	 * 
	 * @throws InvalidInputFileException
	 * @throws ArgsParseException
	 * @throws IOException 
	 */
	private void parseArgsAndInitProgram() throws InvalidInputFileException,
			ArgsParseException, IOException {

		boolean inputAndOutputGiven = findAndReadConfigIfGivenAndSetMode();

		initGivenParameters();

		/* set logging */
		// Logger logger = Logger.getLogger("");
		// if (TaskConfig.setLogLevel){
		// 	logger.setLevel(TaskConfig.logLevel);
		// }
		// Handler[] handler = logger.getHandlers();
		// for (Handler h : handler) {
		// 	if (TaskConfig.setLogLevel){			
		// 		h.setLevel(TaskConfig.logLevel);
		// 	}
		// 	if (h instanceof ConsoleHandler) {
		// 		if (!TaskConfig.verbose) {
		// 			h.setLevel(Level.WARNING);
		// 		}
		// 	}
		// }

		if (TaskConfig.gui) {
			/* start gui with previous set parameters */

		} else if (inputAndOutputGiven && !TaskConfig.gui) {
			
			ClusteringManagerTask manageTask = new ClusteringManagerTask();
			manageTask.run(); //run without initialising new thread.
	
		} else {
			/* either input or output is missing */
			throw new ArgsParseException(
					"Either input file/directory (-i) or output file (-o) is missing!");
		}
	}

	/**
	 * Takes the input args string and divides it up into key and values and
	 * then passes it on. Also checks whether the input parameters are given
	 * correctly with both key and value. [-key value]
	 * 
	 * @throws ArgsParseException
	 *             Throws an exception if a value is missing.
	 * @throws IOException 
	 */
	private void initGivenParameters() throws ArgsParseException, IOException {

		int i = 0;
		String key = null;
		String value = null;
		while (i < args.length) {
			key = args[i].trim();
			// check all keys without values!
			if (key.equals("-gui")) {
				value = "true";
				setParameter(key, value);
				++i;
				continue;
			}
			if (key.equals("-verbose")) {
				value = "true";
				setParameter(key, value);
				++i;
				continue;
			}

			try {
				value = args[i + 1].trim();
			} catch (ArrayIndexOutOfBoundsException e) {
				throw new ArgsParseException(
						"One of the keys does not have a value!");
			}
			if (!key.startsWith("-")) {
				throw new ArgsParseException(
						"ERROR: This key does not begin with a '-', or the input is in the wrong format! Key="
								+ key);
			} else if (value.startsWith("-")) {
				throw new ArgsParseException(
						"ERROR: This value starts with a '-', which indicates that it is a key. Please look at your input again!");
			} else {
				setParameter(key, value);
			}
			i += 2;
			
			
		}
		
	    
	}

	/**
	 * This method tries to set the given parameter in the respective config. If
	 * this is not possible then an exception is thrown since this implies that
	 * either the given key is not valid, or the value is of an unfitting type.
	 * The mode is already set (or should be) when this method is called.
	 * 
	 * @param key
	 *            The key for the parameter.
	 * @param value
	 *            The value for the parameter.
	 * @throws ArgsParseException
	 * @throws ArgsParseException
	 *             If the given key does not exist, or if the given value has
	 *             the wrong type.
	 */
	private void setParameter(String key, String value)
			throws ArgsParseException {

		try {
			if (key.equals("-i")) {
				TaskConfig.cmPath = value;
			} else if (key.equals("-o")) {
				if (TaskConfig.mode == TaskConfig.GENERAL_TRAINING_MODE) {
					TaskConfig.outConfigPath = value;
				}else {
					TaskConfig.clustersPath = value;
				} 
			} else if (key.equals("-cf")) {
				TaskConfig.useConfigFile = Boolean.parseBoolean(value);
			} else if (key.equals("-log")) {
				TaskConfig.setLogLevel = true;
				// TaskConfig.logLevel = Level.parse(value.toUpperCase());
			} else if (key.equals("-verbose")) {
				TaskConfig.verbose = Boolean.parseBoolean(value);
			} else if (key.equals("-info")) {
				TaskConfig.info = true;
				if (value.endsWith(".info")) {
					TaskConfig.infoPath = value;
				} else {
					TaskConfig.infoPath = value + ".info";
				}
			} else if (key.equals("-gui")) {
				TaskConfig.gui = Boolean.parseBoolean(value);
			} else if (key.equals("-l")) {
				TaskConfig.layouterClasses = value;
				TaskConfig.layouterEnumTypes = LayoutFactory
						.getEnumArrayFromLayoutersString();
			} else if (key.equals("-g")) {
				TaskConfig.geometricClusteringClass = value;
				TaskConfig.geometricClusteringEnum = GeometricClusteringFactory
						.getClustererEnumByClass(value);
			} else if (key.equals("-p")) {
				if(value.equals("none")){
					TaskConfig.doPostProcessing = false;
				} else {
				TaskConfig.postProcessingClass = value;
				TaskConfig.doPostProcessing = true;
				TaskConfig.postProcessingEnum = PostProcessingFactory
						.getPostProcessorEnumByClass(value);
				}
			} else if (key.equals("-e")) {
				TaskConfig.ccEdgesClass = value;
				TaskConfig.ccEdgesEnum = LayoutFactory
						.getCCEdgesEnumByClass(value);
			} else if (key.equals("-t")) {
				TaskConfig.useThreads = true;
				int givenMax = Integer.parseInt(value);
				if (givenMax > TaskConfig.SYSTEM_NO_AVAILABLE_PROCESSORS) {
					givenMax = TaskConfig.SYSTEM_NO_AVAILABLE_PROCESSORS;
				}
				TaskConfig.maxNoThreads = givenMax;
			} else if (key.equals("-ld")) {
				int dim = Integer.parseInt(value);
				if (dim < 2) {
					throw new ArgsParseException(
							"The dimension given is too small: " + value);
				}
				if (dim > 3) {
					TaskConfig.monitor.showMessage(TaskMonitor.Level.WARN,
							"If using the ACCLayouter, then any dimension greater than 3 is very time expensive. Recommended are dimensions 2 or 3.");
				}
				TaskConfig.dimension = dim;
			} else if (key.equals("-lp")) {
				if(value.equals("none")){
					TaskConfig.doLayoutParameterTraining = false;
				} else {
				TaskConfig.parameterTrainingClass = value;
				TaskConfig.doLayoutParameterTraining = true;
				TaskConfig.parameterTrainingEnum = ParameterTrainingFactory
						.getParameterTrainingEnumByClass(value);
				}
			} else if (key.equals("-lps")) {
				int no = Integer.parseInt(value);
				if (no < 2) {
					throw new ArgsParseException(
							"The number of parameter configurations per generation need to be at least two! Number given="
									+ value);
				}
				TaskConfig.noOfParameterConfigurationsPerGeneration = no;
			} else if (key.equals("-lpn")) {
				int no = Integer.parseInt(value);
				if (no < 1) {
					throw new ArgsParseException(
							"The number of generations for the layout parameter training is too small! Number given="
									+ value);
				}
				TaskConfig.noOfGenerations = no;
			} else if (key.equals("-fa")) {
				FORCEnDLayoutConfig.attractionFactor = Double
						.parseDouble(value);
			} else if (key.equals("-fr")) {
				FORCEnDLayoutConfig.repulsionFactor = Double.parseDouble(value);
			} else if (key.equals("-fi")) {
				FORCEnDLayoutConfig.iterations = Integer.parseInt(value);
			} else if (key.equals("-ft")) {
				FORCEnDLayoutConfig.temperature = Float.parseFloat(value);
			} else if (key.equals("-sm")) {
				GeometricClusteringConfig.minDistance = Double
						.parseDouble(value);
			} else if (key.equals("-sx")) {
				GeometricClusteringConfig.maxDistance = Double
						.parseDouble(value);
			} else if (key.equals("-ss")) {
				GeometricClusteringConfig.stepsize = Double.parseDouble(value);
			} else if (key.equals("-sf")) {
				GeometricClusteringConfig.stepsizeFactor = Double
						.parseDouble(value);
			} else if (key.equals("-km")) {
				GeometricClusteringConfig.kLimit = Integer.parseInt(value);
			} else if (key.equals("-ki")) {
				GeometricClusteringConfig.maxInitStartConfigs = Integer
						.parseInt(value);
				/* do nothing for keys already read */
			} else if (key.equals("-mode")) {
				TaskConfig.mode = Integer.parseInt(value);
			} else if (key.equals("-sim")) {
				Config.similarityFile = value;
			} else if (key.equals("-gs")) {
				TaskConfig.goldstandardPath = value;
			} else if (key.equals("-greedy")) {
				TaskConfig.greedy = Boolean.parseBoolean(value);
			} else if (key.equals("-ub")) {
				TaskConfig.upperBound = Float.parseFloat(value);
			} else if (key.equals("-minT")) {
					TaskConfig.minThreshold =  Double.parseDouble(value);
			} else if (key.equals("-maxT")) {
				TaskConfig.maxThreshold =  Double.parseDouble(value);
			} else if (key.equals("-tss")) {
				TaskConfig.thresholdStepSize =  Double.parseDouble(value);
			} else if (key.equals("-chc")) {
				TaskConfig.clusterHierarchicalComplete =  Boolean.parseBoolean(value);
			} else if (key.equals("-fp")) {
				TaskConfig.fixedParameter =  Boolean.parseBoolean(value);
			} else if (key.equals("-fpt")) {
				TaskConfig.fpMaxTimeMillis =  (long) Math.rint(Double.parseDouble(value)*1000);
			} else if (key.equals("-fps")) {
				TaskConfig.fixedParameterMax =  Integer.parseInt(value);
				
				
				// ********************************************
				// TODO: Add extra input variables here !!
				// ********************************************

			} else {
				throw new ArgsParseException("This key does not exist: " + key);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ArgsParseException("The value is of the wrong type: "
					+ value);
		}
	}

	/**
	 * Looks through the input variables to see if a config file is defined. If
	 * so all parameters are read from this config file. If some do not exist, a
	 * warning is given, but the program continues. It may be the case that
	 * these parameters are unwanted or belong to an unused implementation. It
	 * also looks if a mode was given and sets this, otherwise the default is
	 * used.
	 * 
	 * It also checks whether input AND output files/directories are given,
	 * which is compulsory when starting with the console and not with the gui.
	 * 
	 * @return boolean value if an input and an output file (both) are given.
	 * @throws InvalidInputFileException
	 *             If the given config class does not end in .conf.
	 */
	private boolean findAndReadConfigIfGivenAndSetMode()
			throws InvalidInputFileException, ArgsParseException {

		boolean input = false;
		boolean output = false;

		String configPath = TaskConfig.DEFAULTCONFIG;
		for (int i = 0; i < args.length; i++) {

			/* check whether an input file is given */
			if (args[i].trim().equals("-i")) {
				input = true;
				++i;
			}
			if (args[i].trim().equals("-o")) {
				output = true;
				++i;
			}

			/* check for mode parameter */
			if (args[i].trim().equals("-mode")) {

				String value = args[i + 1].trim();
				try {
					int md = Integer.parseInt(value);
					if (md == TaskConfig.GENERAL_TRAINING_MODE) {
						TaskConfig.mode = TaskConfig.GENERAL_TRAINING_MODE;
					} else if (md == TaskConfig.CLUSTERING_MODE) {
						TaskConfig.mode = TaskConfig.CLUSTERING_MODE;
					} else if (md == TaskConfig.COMPARISON_MODE) {
						TaskConfig.mode = TaskConfig.COMPARISON_MODE;
					} else if (md == TaskConfig.HIERARICHAL_MODE) {
						TaskConfig.mode = TaskConfig.HIERARICHAL_MODE;
					}else {
						throw new ArgsParseException(
								"The given mode is incorrect - it does not exist! "
										+ md);
					}
				} catch (Exception e) {
					throw new ArgsParseException(
							"The given mode is not an interger value: " + value);
				}
				++i;
			}

			/* check for config parameter */
			if (args[i].trim().equals("-config")) {
				String value = args[i + 1].trim();
				if (value.endsWith(".conf")) {
					TaskConfig.useConfigFile = true;
					TaskConfig.inputConfigPath = value;
					configPath = value;
				} else {
					throw new InvalidInputFileException(
							"An invalid config file was entered. The file must end with '.conf'. Please try again! Given file="
									+ value);
				}
				++i;
			}

			/* check for if -cf parameter is set */
			if (args[i].trim().equals("-cf")) {

				TaskConfig.useConfigFile = Boolean.parseBoolean(args[i + 1]
						.trim());
				++i;
			}
		}

		/*
		 * read given config file - if it doesn't contain some resources,
		 * default values are taken
		 */

		if (TaskConfig.useConfigFile) {

			try {
				FileInputStream s = new FileInputStream(configPath);
				PropertyResourceBundle configrb = new PropertyResourceBundle(s);

				// log.debug("Using config file " + configPath);

				TaskConfig.initFromConfigFile(configrb);
				FORCEnDLayoutConfig.initFromConfigFile(configrb);
				GeometricClusteringConfig.initSLCFromConfigFile(configrb);
				GeometricClusteringConfig.initKmeansFromConfigFile(configrb);

			} catch (MissingResourceException ex) {
				TaskConfig.monitor.showMessage(TaskMonitor.Level.ERROR,
						"WARNING: Resources are missing in the given config file: "
								+ TaskConfig.DEFAULTCONFIG
								+ ", key="
								+ ex.getKey()
								+ ". Either you have defined these parameters in the input, or the default values are used from the "
								+ TaskConfig.DEFAULTCONFIG
								+ ". Or these parameters do not interest you, because they belong to an unused implemtation.");
			} catch (IOException ex) {
				TaskConfig.monitor.showMessage(TaskMonitor.Level.ERROR,
				    "ERROR: Unable to read the given config file: "
						+ configPath);
				System.exit(-1);
			} catch (InvalidTypeException ex) {
				TaskConfig.monitor.showMessage(TaskMonitor.Level.ERROR,
						    "ERROR: You have perhaps given an incorrect class name of an "
								+ "implemtation. Please note that this is case sensitive.");
				System.exit(-1);
			}

		}

		if (input && output)
			return true;
		else
			return false;
	}
}
