package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.Vector;

// import java.util.logging.Level;
// import java.util.logging.Logger;

import javax.swing.JFileChooser;

import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.TransClustCluster;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering.GeometricClusteringFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.forcend.FORCEnDLayoutConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.parameter_training.ParameterTrainingFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PostProcessingFactory;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.ConfigFile;

public class TaskConfig {
	
	// private static Logger log = Logger.getLogger(TaskConfig.class.getName());
	
	// --------------------- FIXED VARIABLES ---------------------- //	
	/* program details */
	public final static String NAME = "TransClust";	
	public final static String NAME_EXTENDED = "Clustering by Weighted Transitive Graph Projection";
	public final static String VERSION = "1.0";
//	public final static String[] AUTHORS = {"Sita Lange: sita.lange@cebitec.uni-bielefeld.de",
//		"Nils Kleinboelting: nils.kleinboelting@cebitec.uni-bielefeld.de",
//		"Tobias Wittkop: tobias.wittkop@cebitec.uni-bielefeld.de",
//		"and Jan Baumbach: jan.baumbach@cebitec.uni-bielefeld.de"};	
	public final static String[] AUTHORS = {"Tobias Wittkop: tobias.wittkop@cebitec.uni-bielefeld.de",
		" and Jan Baumbach: jan.baumbach@icsi.berkeley.edu"};
	public final static String[] DEVELOPERS = {"Tobias Wittkop: tobias.wittkop@cebitec.uni-bielefeld.de",
		"Jan Baumbach: jan.baumbach@icsi.berkeley.edu","Sita Lange: sita.lange@cebitec.uni-bielefeld.de","Nils Kleinboelting: nils.kleinboelting@cebitec.uni-bielefeld.de", "and Dorothea Emig: demig@mpi-inf.mpg.de"};
	public final static String JAR = "TransClust.jar";
	
	public final static String NL = System.getProperty("line.separator"); //newline
	public final static String TAB = "\t";
	public final static String FS = System.getProperty("file.separator"); //slash
	
	public final static String DEFAULTCONFIG = "Default.conf";
	public final static int CLUSTERING_MODE = 0;
	public final static int GENERAL_TRAINING_MODE = 1;
	public final static int COMPARISON_MODE = 2;
	public final static int HIERARICHAL_MODE = 3;
	public final static int SYSTEM_NO_AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
	
	
	

	// ------------------------------------------------------------------- //
	
	
	//	 ----------------------- INTERNAL VARIABLES ----------------------- //
	
	public static LayoutFactory.EnumCCEdgesStructure ccEdgesEnum = 
		LayoutFactory.EnumCCEdgesStructure.CC2DARRAY;
	public static LayoutFactory.EnumLayouterClass[] layouterEnumTypes =
		{LayoutFactory.EnumLayouterClass.FORCEND};
	public static ParameterTrainingFactory.EnumParameterTrainingClass 
		parameterTrainingEnum = ParameterTrainingFactory.EnumParameterTrainingClass.
		PARAMETERTRAINER_SE;
	public static GeometricClusteringFactory.EnumGeometricClusteringClass 
	    geometricClusteringEnum = GeometricClusteringFactory.
	    EnumGeometricClusteringClass.SINGLE_LINKAGE_CLUSTERING;
	public static PostProcessingFactory.EnumPostProcessingClass postProcessingEnum = 
		PostProcessingFactory.EnumPostProcessingClass.PP_DIVIDE_AND_RECLUSTER_RECURSIVELY;
	public static String currentDirectory = System.getProperty("user.dir");

	public static JFileChooser jfc = new JFileChooser();
	
	//	 --------------------------------------------------------------------------- //

	
	// --------------------- INPUT/CONFIG VARIABLES ---------------------- //	
	
	/* ---- file paths ---- */
	public static String outConfigPath; // for training mode
	public static String inputConfigPath = DEFAULTCONFIG;
	public static String clustersPath;
	public static String transitiveConnectedComponents;
	public static String cmPath;
	public static String goldstandardPath;
	public static String infoPath;
	public static File tempDir;
	
	
	/* ---- general ---- */
	public static boolean useThreads = true;
//	public static boolean useThreadsForCCs= useThreads;
	public static int maxNoThreads = Runtime.getRuntime().availableProcessors()-1;
	public static String ccEdgesClass = "CC2DArray";
	public static boolean verbose = false;
	public static int mode = CLUSTERING_MODE;
	public static boolean info = false; //default is that no info file is created
	
	public static boolean gui = false; //if the program is started with the gui or not.
	public static boolean useConfigFile = false;
	
	/* ---- layouting ----*/
	public static String layouterClasses = "FORCEnDLayouter"; //use correct class names
	public static int dimension = 3;
	
	
	/* ---- parameter training for the layouters ---- */
	public static String parameterTrainingClass = "ParameterTraining_SE";
	public static boolean doLayoutParameterTraining = false;
	public static int noOfParameterConfigurationsPerGeneration = 15; //minimum = 2!!
	public static int noOfGenerations = 3; //min number of generations = 1;
//	public static boolean useThreadsForParameterTraining = false;
	
	
	/* ---- geometric clustering ---- */
	public static String geometricClusteringClass = "SingleLinkageClusterer";
//	public static String geometricClusteringClass = "KmeansClusterer";
	
	
	/* ---- post-processing ---- */
	public static boolean doPostProcessing = true;
	public static String postProcessingClass = "PP_DivideAndReclusterRecursively";
	
	
//	/* ---- logging ---- */
	public static boolean setLogLevel = false;
	// public static LogLevel logLevel = LogLevel.LOG_FATAL;
	public static TaskMonitor monitor = null;

	
//	/* ---- additional ---- */
	public static double minThreshold = 0;

	public static double thresholdStepSize = 1;

	public static double maxThreshold = 100;

	public static boolean clusterHierarchicalComplete = false;

	public static boolean greedy = false;

	public static boolean fixedParameter  = true;

	public static int fixedParameterMax = 20;
	
	public static long fpMaxTimeMillis = 1000;
	
	public static boolean fpStopped = false;

	public static float upperBound = Float.MAX_VALUE;

	public static boolean reducedMatrix = false;

	private static boolean debug = false;

	public static boolean developerMode = true;
	
	public static boolean fuzzy = false;
	
	public static boolean overlap = false;
	
	public static double fuzzyThreshold = 0.5;
	
	public static double lowerBound = 0;
	
	public static boolean UseLimitK = false;
	
	public static int limitK = 7;

	public static String knownAssignmentsFile;

//	public static Hashtable<Integer, Vector<Integer>> dummy;
	

	// ------------------------------------------------------------------- //

	
	// ----------------------------- OTHER ----------------------------- //

	
	/**
	 * This methods loads every necessary parameters from the given ConfigFile
	 * 
	 * @param rb
	 *            The PropertyResourceBundle object for the config file.
	 * @throws IOException 
	 */
	public static void initFromConfigFile(PropertyResourceBundle rb)
			throws MissingResourceException, InvalidTypeException, IOException {
		
		useThreads = Boolean.parseBoolean(rb.getString(
				"general.useThreads").trim());
		
		int configMax = Integer.parseInt(rb.getString("general.maxNoThreads").trim());
		/* don't allow maxNoThreads to become greater than the number of available processors */
		if(configMax >TaskConfig.SYSTEM_NO_AVAILABLE_PROCESSORS){
			configMax= TaskConfig.SYSTEM_NO_AVAILABLE_PROCESSORS;
		}
		maxNoThreads = configMax;

		ccEdgesClass = rb.getString("general.ccEdgesDataStructure").trim();
		
		ccEdgesEnum = LayoutFactory.getCCEdgesEnumByClass(ccEdgesClass);
		
		verbose = Boolean.parseBoolean(rb.getString("general.verbose"));
		
		dimension = Integer.parseInt(rb.getString("layout.dimension").trim());
		
		layouterClasses = (rb.getString("general.layouters").trim());
		
		layouterEnumTypes = LayoutFactory.getEnumArrayFromLayoutersString();
		
		geometricClusteringClass = rb.getString("general.geometricClusterer").trim();
		
		geometricClusteringEnum = GeometricClusteringFactory.
				getClustererEnumByClass(geometricClusteringClass);
		
		postProcessingClass = rb.getString("general.postProcessor").trim();
		
		postProcessingEnum = PostProcessingFactory.getPostProcessorEnumByClass(
				postProcessingClass); 
		
		parameterTrainingClass = rb.getString("layout.parameterTraining").trim();
		
		parameterTrainingEnum = ParameterTrainingFactory.getParameterTrainingEnumByClass(
				parameterTrainingClass);
		
		doLayoutParameterTraining = Boolean.parseBoolean(rb.getString(
				"layout.doParameterTraining").trim());
		
		noOfParameterConfigurationsPerGeneration = Integer.parseInt(rb.getString(
				"layout.generationSize").trim());
		
		noOfGenerations = Integer.parseInt(rb.getString("layout.noOfGenerations").trim());
		
//		useThreadsForParameterTraining = Boolean.parseBoolean(rb.getString(
//				"layout.useThreadsForParameterTraining").trim());

		doPostProcessing = Boolean.parseBoolean(rb.getString(
				"general.doPostProcessing").trim());
		
		postProcessingClass = rb.getString("general.postProcessor").trim();
		
		postProcessingEnum = PostProcessingFactory.getPostProcessorEnumByClass(
				postProcessingClass);

		
		minThreshold = Double.parseDouble(rb.getString("ic.minThreshold").trim());
		maxThreshold = Double.parseDouble(rb.getString("ic.maxThreshold").trim());
		thresholdStepSize = Double.parseDouble(rb.getString("ic.thresholdStepSize").trim());
		clusterHierarchicalComplete= Boolean.parseBoolean(rb.getString("ic.clusterHierarchicalComplete").trim());
		
		
		
	}
	
	
	public static void saveConfigurationsToConfigFile(String filepath){
		/* create config file instance and print header */
		ConfigFile confile = new ConfigFile();
		confile.instantiateFile(filepath);
		confile.printHeader();
		confile.printnewln();
		confile.printnewln();
		confile.printnewln();
		
		/* print general parameters */
		confile.printSubHeader("GENERAL");
		confile.printParameter("general.useThreads", Boolean.toString(TaskConfig.useThreads));
		confile.printParameter("general.maxNoThreads", ""+TaskConfig.maxNoThreads);
//		confile.printParameter("general.useThreadsForCCs", Boolean.toString(TaskConfig.useThreadsForCCs));
		confile.printParameter("general.ccEdgesDataStructure", TaskConfig.ccEdgesClass);
		confile.printParameter("general.layouters", TaskConfig.layouterClasses);
		confile.printParameter("general.geometricClusterer", TaskConfig.geometricClusteringClass);
		confile.printParameter("general.doPostProcessing", Boolean.toString(TaskConfig.doPostProcessing));
		confile.printParameter("general.postProcessor", TaskConfig.postProcessingClass);
		confile.printParameter("general.verbose", Boolean.toString(TaskConfig.verbose));
		confile.printnewln();
		confile.printnewln();
		confile.printnewln();
		
		/* print general layout parameters */
		confile.printSubHeader("LAYOUT");
		confile.printParameter("layout.dimension", ""+TaskConfig.dimension);
		confile.printParameter("layout.doParameterTraining", Boolean.toString(TaskConfig.doLayoutParameterTraining));
//		confile.printParameter("layout.useThreadsForParameterTraining", Boolean.toString(TaskConfig.useThreadsForParameterTraining));
		confile.printParameter("layout.parameterTraining", TaskConfig.parameterTrainingClass);
		confile.printParameter("layout.generationSize", ""+TaskConfig.noOfParameterConfigurationsPerGeneration);
		confile.printParameter("layout.noOfGenerations", ""+TaskConfig.noOfGenerations);
		confile.printnewln();
		confile.printnewln();
		confile.printnewln();
		
		/* print FORCEnD parameters */
		FORCEnDLayoutConfig.printParametersToConfig(confile);
		confile.printnewln();
		confile.printnewln();
		confile.printnewln();
		
		/* print geometric clustering parameters */
		GeometricClusteringFactory.printParametersToConfig(confile);
		
		/* print iterative parameters */
		confile.printSubHeader("Iterative Clustering");
		confile.printParameter("ic.minThreshold", TaskConfig.minThreshold+"");
		confile.printParameter("ic.thresholdStepSize", ""+TaskConfig.thresholdStepSize);
		confile.printParameter("ic.maxThreshold", ""+TaskConfig.maxThreshold);
		confile.printParameter("ic.clusterHierarchicalComplete", ""+TaskConfig.clusterHierarchicalComplete);
		
		
		
		// ========================================//
		// ADD ADDITIONAL IMPLEMENTED METHODS HERE!!						   //
		// ========================================//
		
		
		
		confile.flushbw();
	}
	
	/**
	 * Prints the present configuration (at the time this method is called. 
	 * This means prints the main information
	 * in all the config classes - which configuration is used.
	 * 
	 * @return The present configuration of the config classes.
	 */
	public static StringBuffer printConfiguration(){
		StringBuffer sb = new StringBuffer();
		
		sb.append("CONFIGURATION");
		sb.append(NL);
		sb.append("-------------------------");
		sb.append(NL);
		sb.append(NL);
		
		if(TaskConfig.mode == TaskConfig.CLUSTERING_MODE){
			sb.append("MODE: Clustering mode (");
		} else if (TaskConfig.mode == TaskConfig.GENERAL_TRAINING_MODE){
			sb.append("MODE: General training mode (");
		} else if (TaskConfig.mode == TaskConfig.COMPARISON_MODE){
			sb.append("MODE: iterative clustering (");
		} else if (TaskConfig.mode == TaskConfig.HIERARICHAL_MODE){
			sb.append("MODE: hierarchical clustering (");
			// ==============================================//
			// ADD ADDITIONAL IMPLEMENTED MODES HERE!!	AND CHECK CODE BELOW  //
			// ==============================================//
			
		} else {
			monitor.showMessage(TaskMonitor.Level.ERROR, "This mode does not exist or needs to be added here: "+TaskConfig.mode);
			System.exit(-1);
		}
		sb.append(TaskConfig.mode);
		sb.append(")");
		sb.append(NL+NL);
		
		sb.append("FILE PATHS");
		sb.append(NL);
		addConfigToSB(sb, "Cost matrices", TaskConfig.cmPath);
		if(TaskConfig.mode == TaskConfig.CLUSTERING_MODE){
			addConfigToSB(sb, "Clustering results", TaskConfig.clustersPath);
		} else if (TaskConfig.mode == TaskConfig.GENERAL_TRAINING_MODE){
			addConfigToSB(sb, "Output config file", TaskConfig.outConfigPath);
		}
		if(TaskConfig.info){
			addConfigToSB(sb, "Info file", TaskConfig.infoPath);
		}
		if(TaskConfig.useConfigFile){
			addConfigToSB(sb, "Config file", TaskConfig.inputConfigPath);
		} else {
			//TODO
//			sb.append("Default configurations will be used.");
//			sb.append(NL);
		}
		sb.append(NL);
		
		if(TaskConfig.gui){
//			addConfigToSB(sb, "WARNING", "Could have used the gui to change the parameters for the different methods. \nThis is not kept track of here! \nOtherwise the parameters are as given in the config file.");
//			sb.append(NL);
		}
		if(TaskConfig.debug){
			sb.append("CLUSTERING");
			sb.append(NL);
		
		
			if(!TaskConfig.greedy){
				addConfigToSB(sb, "Layouter classes in order of execution", TaskConfig.layouterClasses);
				addConfigToSB(sb, "Layout dimension", ""+TaskConfig.dimension);
				if(TaskConfig.mode == TaskConfig.CLUSTERING_MODE){
					if(doLayoutParameterTraining){
						addConfigToSB(sb, "ParameterTraining class", TaskConfig.parameterTrainingClass);
						addConfigToSB(sb, "Number of parameter configurations per generation", ""+TaskConfig.noOfParameterConfigurationsPerGeneration);
						addConfigToSB(sb, "Number of generations", ""+TaskConfig.noOfGenerations);
					} else {
						sb.append("Parameter training is turned off!");
						sb.append(NL);
					}
				} else if(TaskConfig.mode == TaskConfig.GENERAL_TRAINING_MODE){
					sb.append(NL);
					sb.append("Options For General Training Mode "+NL);
					addConfigToSB(sb, "Number of parameter configurations per generation", ""+TaskConfig.noOfParameterConfigurationsPerGeneration);
					addConfigToSB(sb, "Number of generations", ""+TaskConfig.noOfGenerations);
					sb.append(NL);
				}
				addConfigToSB(sb, "Geometric clustering class", TaskConfig.geometricClusteringClass);	
			}else{
				addConfigToSB(sb, "using greedy approximation", "");
			}
		
		
		//TODO
			if(TaskConfig.fixedParameter){
				
			}
			
			if(TaskConfig.doPostProcessing){
				addConfigToSB(sb, "Post-processing class", TaskConfig.postProcessingClass);
			} else{
				sb.append("Post-processing is turned off!");
		}
		}
		return sb;
	}
	
	/**
	 * Just adds a line to the info StringBuffer in two parts (description and value);
	 * @param sb StringBuffer to add to.
	 * @param descr The description string.
	 * @param value The value string.
	 */
	private static void addConfigToSB(StringBuffer sb, String descr, String value){
		sb.append(descr);
		sb.append(":  ");
		sb.append(value);
		sb.append(NL);
	}
	
	//	 ------------------------------------------------------------------- //
	
	
	

}
