/* 
 * Created on 28. January 2008
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io;

import java.util.HashMap;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering.GeometricClusteringConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.geometric_clustering.GeometricClusteringFactory.EnumGeometricClusteringClass;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory.EnumCCEdgesStructure;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.LayoutFactory.EnumLayouterClass;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.forcend.FORCEnDLayoutConfig;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.parameter_training.ParameterTrainingFactory.EnumParameterTrainingClass;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.postprocessing.PostProcessingFactory.EnumPostProcessingClass;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;

/**
 * A collection of static methods for parsing the arguments that are the input from
 * the console.
 * 
 * @author Sita Lange
 *
 */
public class ArgsUtility {
	
	private static final String NL = TaskConfig.NL;
	private static final String TAB = "\t";
	
	/**
	 * Takes the input String array from the main method (console) and creates
	 * a HashMap from this. It depends on the structure "key value". This means
	 * each option has a key which is indicated by some symbol, such as "-" and then
	 * the key to this option follows separated by a space. Any keys that include a
	 * space must be given as a string using quotation marks.
	 * @param args The input args String array.
	 * @param optionIndicator The symbol that indicates an options key.
	 * @return The HashMap including all given keys with their values.
	 * @throws ArgsParseException
	 */
	protected static HashMap<String, String> createOptionsHash(String[] args, 
			String optionIndicator) throws ArgsParseException{
		if(args.length != 0 && args != null){
			
			// options hash with key = key and value = value for this programme
			HashMap<String, String> options = new HashMap<String, String>();
			
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith(optionIndicator)) {
					if (args.length <= i+1) {
						throw new ArgsParseException("No value found for parameter "
								+args[i] +".");
					} else {
						String key = args[i].substring(optionIndicator.length());
						String value = args[i+1];
						if (value.startsWith(optionIndicator)) {
							throw new ArgsParseException("No value found for parameter"
									+key + ".");
						}
						options.put(key, args[i+1]);
						i++;
					}
				}
			}	
		}
		return null;
	}
	
	/**
	 * Get the value for the given key.
	 * 
	 * @param key Key
	 * @param options All options
	 * @return The String object for the value.
	 * @throws ArgsParseException Occurs when no value exists.
	 */
	protected static String getStringValue(String key, 
			HashMap<String, String> options) throws ArgsParseException {
		
		String value = options.get(key);
		if (value == null) {
			throw new ArgsParseException("Key " + key + " unknown.");
		} else {
			return value;
		}
		
	}
	
	/**
	 * Gets the int value for the respective value for the given key.
	 * @param key Key.
	 * @param options All options in a hash.
	 * @return The int value for the given key.
	 * @throws ArgsParseException If the string of the value is not an int.
	 */
	protected static int getIntValue(String key, 
			HashMap<String, String> options) throws ArgsParseException {
		
		String value = options.get(key);
		if (value == null) {
			throw new ArgsParseException("Key " + key + " unknown.");
		} else {
			return Integer.parseInt(value);
		}
		
	}
	
	/**
	 * Gets double value for the respective key, if this is a double in String format.
	 * @param key Key.
	 * @param options All options in a hash.
	 * @return Double value for the given key.
	 * @throws ArgsParseException If the value-string is not a double.
	 */
	protected static double getDoubleValue(String key, 
			HashMap<String, String> options) throws ArgsParseException {
		
		String value = options.get(key);
		if (value == null) {
			throw new ArgsParseException("Key " + key + " unknown.");
		} else {
			return Double.parseDouble(value);
		}
		
	}
	
	/**
	 * Gets float value for the respective key.
	 * @param key Key.
	 * @param options All options hash.
	 * @return The float value for the given key.
	 * @throws ArgsParseException If the string-value is not a float.
	 */
	protected static float getFloatValue(String key, 
			HashMap<String, String> options) throws ArgsParseException {
		
		String value = options.get(key);
		if (value == null) {
			throw new ArgsParseException("Key " + key + " unknown.");
		} else {
			return Float.parseFloat(value);
		}
		
	}
	
	/**
	 * Gets the boolean value for the respective key.
	 * @param key Key.
	 * @param options All options hash.
	 * @return The boolean value for the given key.
	 * @throws ArgsParseException If the string-value is not a boolean.
	 */
	protected static boolean getBoolValue(String key, 
			HashMap<String, String> options) throws ArgsParseException {
		
		String value = options.get(key);
		if (value == null) {
			throw new ArgsParseException("Key " + key + " unknown.");
		} else {
			return Boolean.parseBoolean(value);
		}
		
	}
	
	/**
	 * Creates the usage information and writes it into a StringBuffer and returns this.
	 * @return The StringBuffer with the usage information.
	 */
	public static StringBuffer createUsage() {

		StringBuffer sb = new StringBuffer(500);
		

		
		header("SUMMARY", 
				"This program clusters objects into transitive groups according to the weighted graph cluster editing problem using the given cost matrices.", 
				sb);
		
		header("ABOUT", TaskConfig.NAME+" version "+TaskConfig.VERSION, sb);
		tabLine("Copyright 2008 by", sb);
		for (int i = 0; i < TaskConfig.AUTHORS.length; i++) {
			tabLine(TaskConfig.AUTHORS[i], sb);
	}
		
		header("USAGE", "java -jar [java virtual machine options] "+TaskConfig.JAR+" [-key value]", sb);		
		sb.append(NL);
		tabLine("e.g. java -jar -Xmx2G -Xss100M"+TaskConfig.JAR+" -i cost_matrix_dir -o clusters.cls", sb);
		sb.append(NL);
		tabLine("Note: If the input is large and/or complex then the virtual machine options must be set.", sb);
		tabLine("Any values that inlude spaces must be surrounded by quotation marks '\"'.", sb);
		tabLine("{ } denotes the value choices, [ ] means that the value is a list, and ' ' surrounds a description of the value. ", sb);
		tabLine("Further note that the keys are not case sensitive, but the class names of the respective implementations are!", sb);
		sb.append(NL);

		header("COMPULSORY OPTIONS", "One of the following must be entered.", sb);
		sb.append(NL);
		tabLine("-key  value", sb);
		optionsLine("-i", "{'inputdir', 'costmatrixfile.cm'}", "", "Input file or directory.", sb);
		optionsLine("-o", "{'output.file', 'output.conf'}", "","Output file for the clustering results or the generated config file.", sb);
		tabLine("OR", sb);
		optionsLine("-gui", "{[OTHER OPTIONS]}", "","Start the program with the graphical user interface. It is also possible to initialise the gui with the OTHER OPTIONS defined below!", sb);
		tabLine("OR", sb);
		optionsLine("-help", "{}", "", "Show this help manual.", sb);			
		sb.append(NL);
		
		header("OTHER OPTIONS", "These are optional. "+
				"All parameters that are not specified here are first taken from the input config file if stated,"+
				"otherwise from the default config file that comes with this program. "+
				"IMPORTANT: The given input parameter values override any values written in the config files.", 
				sb);
		sb.append(NL);
		tabLine("-key  value  (default value)", sb);
		
		subheader("EXTRA (not defined in the config file)", sb);
		optionsLine("-verbose", "{}", "()", "Write a short summary of the program results to the standard output (console).", sb);
		optionsLine("-cf", "{true, false}", "("+TaskConfig.useConfigFile+")", "Use config file (true) or hard coded standard options (false).", sb);
		optionsLine("-config", "{'config.conf'}", "", "A config file with the program parameters in the correct format (see documentation for details).", sb);
		optionsLine("-mode", "{0,1}", "("+TaskConfig.mode+")", "Determines the mode in which the program should be started", sb);
		optionsNote("0" ,"Default clustering mode: clustering of given input and writing the clusters to the output file.", sb);
		optionsNote("1", "General training mode: trains a set of data (cost matrices) and writes the generated parameters in the output file.", sb);
		optionsLine("-info", "{'file.info'}", "", "A summary of what functions the program carried out.", sb);
		optionsNote("","This file includes information such the date, the input and output files, which mode the program was carried out in, and which processes were done using which implementations.", sb);
		optionsLine("-log", "{ALL,FINEST,FINER,FINE,CONFIG,INFO,WARNING,SEVERE,OFF}", "()", "Defines the level of logging from the most sensitive level to completely off.", sb);
		subheader("GENERAL", sb);
		optionsLine("-l", "['layouterClass']", "("+TaskConfig.layouterClasses+")", "A List of class names of layouter implementations. These implementations are then used for the layouting phase in the order they are given. Each name should be separated by a \",\" (comma)."+
				" E.g. FORCEnDLayouter,ACCLayouter  or for just one layouter, then only e.g. FORCEnDLayouter.", sb);
		optionsNote("Implemented Classes", printArray(EnumLayouterClass.getClassnames()), sb);
		optionsLine("-g", "{'geometricClustererClass'}", "("+TaskConfig.geometricClusteringClass+")", "The class name of the geometric clustering implementation.", sb);
		optionsNote("Implemented Classes", printArray(EnumGeometricClusteringClass.getClassnames()), sb);
		optionsLine("-p", "{'postProcessorClass'}", "("+TaskConfig.postProcessingClass+")", "The class name of the post processing implementation. Write 'none' if post-processing should NOT be carried out.", sb);
		optionsNote("Implemented Classes", printArray(EnumPostProcessingClass.getClassnames()), sb);
//		optionsNote("", "'none' is also an option to turn of post processing.", sb);
		optionsLine("-e", "{ICCEdgesImplementation}", "("+TaskConfig.ccEdgesClass+")", "The class name of the implementation of the ICCEdges interface describing the datastructure for the costs between objects.", sb);
		optionsNote("Implemented Classes", printArray(EnumCCEdgesStructure.getClassnames()), sb);
		optionsLine("-t", "{1,...,max no. CPUs}", "("+TaskConfig.maxNoThreads+")", "Turn the use of multiple threads on and give the maximum no. of parallel threads (do not give a number greater than the number of CPUs your system has).", sb);
		
		subheader("GENERAL LAYOUT", sb);
		optionsLine("-ld", "{2,...,n}", "("+TaskConfig.dimension+")", "The dimension in which the layouters should run in. NOTE: Because of runtime reasons, ACCLayouter only makes sense for dimensions 2 and 3.", sb);
		optionsLine("-lp", "{'parameterTrainingClass'}", "("+TaskConfig.parameterTrainingClass+")", "The class name of the parameter training implementation. Write 'none' if parameter training should NOT be carried out.", sb);
		optionsNote("Implemented Classes", printArray(EnumParameterTrainingClass.getClassnames()), sb);
//		optionsNote("", "Turn off parameter training 'none' is also an option.", sb);
		optionsLine("-lps", "{2,...,n}", "("+TaskConfig.noOfParameterConfigurationsPerGeneration+")", "Number of parameter configurations for each generation in the parameter training.", sb);
		optionsLine("-lpn", "{1,...,n}", "("+TaskConfig.noOfGenerations+")", "The number of generations that should be used for parameter training.", sb);
		
		subheader("FORCEnDLayouter", sb);
		optionsLine("-fa", "{'double'}", "("+FORCEnDLayoutConfig.attractionFactor+")", "The value for the attraction factor.", sb);
		optionsLine("-fr", "{'double'}", "("+FORCEnDLayoutConfig.repulsionFactor+")", "The value for the repulsion factor.", sb);
		optionsLine("-fi", "{'integer'}", "("+FORCEnDLayoutConfig.iterations+")","Number of iterations.", sb);
		optionsLine("-ft", "{'float'}", "("+FORCEnDLayoutConfig.temperature+")", "The cooling temperature value for the convergence of the layout.", sb);
		
		
		subheader("GEOMETRIC CLUSTERING", sb);
		subheader("SingleLinkageClusterer", sb);
		optionsLine("-sm", "{'double'}", "("+GeometricClusteringConfig.minDistance+")", "The minimum distance.", sb);
		optionsLine("-sx", "{'double'}", "("+GeometricClusteringConfig.maxDistance+")", "The maximum distance to look at.", sb);
		optionsLine("-ss", "{'double'}", "("+GeometricClusteringConfig.stepsize+")", "The step size.", sb);
		optionsLine("-sf", "{'double'}", "("+GeometricClusteringConfig.stepsizeFactor+")", "The step size factor.", sb);
		subheader("KmeansClusterer", sb);
		optionsLine("-km", "{'integer'}", "("+GeometricClusteringConfig.kLimit+")", "The maximum k value that is allowed. This means the maximum number of clusters that the input can be divided into.", sb);
		optionsLine("-ki", "{'integer'}", "("+GeometricClusteringConfig.maxInitStartConfigs+")", "Maximum number of different initial starting point combinations (for one k) that k-means uses.", sb);

		// ********************************************
		// TODO: Add extra input variables here !!
		// ********************************************
		
		sb.append(NL);
		return sb;
	}
	
	/**
	 * USAGE:
	 * Appends an options line to the StringBuffer in a readable format.  
	 * If the description is too long it is cut into sizeable pieces.
	 * @param key The key for the option.
	 * @param values The value description for the option.
	 * @param description The description for the option.
	 * @param sb The StringBuffer.
	 */
	private static void optionsLine(String key, String values, String defaultValue, String description, StringBuffer sb){

		sb.append(NL);
		sb.append(TAB);
		sb.append(key);
		sb.append("  ");
		sb.append(values);
		sb.append("  ");
		sb.append(defaultValue);
		sb.append(NL);
		sb.append(TAB);
		sb.append(TAB);
		int step = 60;
		int length = description.length();
		int i = 0;
		int space;
		while(i<length){
			if(i+step-1 >= length){
				sb.append(description.substring(i));
				sb.append(NL);
				break;
			}
			else {
				space = findClosestSpace(description, i+step-1, length);
				sb.append(description.substring(i, i+step+space));
				i += step+space;
				sb.append(NL+TAB+TAB);
			}
		}
		
	}
	
	/**
	 * USAGE: 
	 * Append a header to the StringBuffer.  If the description is too long it is cut into sizeable pieces.
	 * @param header The name of the header.
	 * @param description The description for the header.
	 * @param sb The StringBuffer.
	 */
	private static void header(String header, String description, StringBuffer sb){

		sb.append(NL);
		sb.append(NL);
		sb.append(header);
		sb.append(NL);
		sb.append(TAB);
		int step = 65;
		int length = description.length();
		int i = 0;
		int space;
		while(i<length){
			if(i+step-1 >= length){
				sb.append(description.substring(i));
				sb.append(NL);
				break;
			}
			else {
				space = findClosestSpace(description, i+step-1, length);
				sb.append(description.substring(i, i+step+space));
				i += step+space;
				sb.append(NL+TAB);
			}
		}
	}
	
	/**
	 * USAGE: 
	 * Append a string to the StringBuffer and start with a tab.  
	 * If the string is too long it is cut into sizeable pieces.
	 * @param string The String to append.
	 * @param sb The StringBuffer.
	 */
	private static void tabLine(String string, StringBuffer sb){

		sb.append(TAB);
		int i = 0;
		int space;
		int step = 65;
		int length = string.length();
		while(i<length){
			if(i+step-1 >= length){
				sb.append(string.substring(i));
				sb.append(NL);
				break;
			}
			else {
				space = findClosestSpace(string, i+step-1, length);
				sb.append(string.substring(i, i+step+space));
				i += step+space;
				sb.append(NL+TAB);
			}
		}
	}
	
	/**
	 * USAGE:
	 * Append a note to an options entry to the given StringBuffer. Usually to describe
	 * meanings of option values. If the description is too long it is cut into sizeable pieces.
	 * @param key The key for the note.
	 * @param string The The description for the key.
	 * @param sb The StringBuffer.
	 */
	private static void optionsNote(String key, String string, StringBuffer sb){
		sb.append(TAB);
		sb.append(TAB);
		sb.append(key);
		sb.append(TAB);
		int i = 0;
		int space;
		int step = 50;
		int length = string.length();
		while(i<length){
			if(i+step-1 >= length){
				sb.append(string.substring(i));
				sb.append(NL);
				break;
			}
			else {
				space = findClosestSpace(string, i+step-1, length);
				sb.append(string.substring(i, i+step+space));
				i += step+space;
				sb.append(NL+TAB+TAB+TAB);
			}
		}
	}
	
	/**
	 * USAGE:
	 * Append a subheader to the StringBuffer.
	 * @param header The name of the sub-header.
	 * @param sb The StringBuffer.
	 */
	private static void subheader(String header, StringBuffer sb){
		sb.append(NL);
		sb.append(TAB);
		sb.append(header);
		sb.append(NL);
	}
	
	
	/**
	 * Finds the closest space in the given string starting from the given starting position.
	 * Returns the distance that the space is from the start position. To the left a negative distance and
	 * to the right a positive distance.
	 * @param string The string to be searched.
	 * @param pos The position to start the search.
	 * @param length The length of the given string.
	 * @return The distance of the space to the starting position.
	 */
	private static int findClosestSpace(String string, int pos, int length){
		int i=0;
		
		while(i<10){
			if(pos+i<length && pos-i>=0){
				if(string.charAt(pos+i)==' '){

					return i;
				}
				else if(string.charAt(pos-i)==' '){
					return -i;
				} else{
					i++;
				}
			} else { return (length-1-pos);}
			
		}
		return 0;
	}
	
	private static String printArray(String[] array){
		StringBuffer stringbuff = new StringBuffer();
		stringbuff.append("{");
		for (int i = 0; i < array.length-1; i++) {
			stringbuff.append(array[i]);
			stringbuff.append(", ");
		}
		stringbuff.append(array[array.length-1]);
		stringbuff.append("}");
	
		return stringbuff.toString();
	}
}
