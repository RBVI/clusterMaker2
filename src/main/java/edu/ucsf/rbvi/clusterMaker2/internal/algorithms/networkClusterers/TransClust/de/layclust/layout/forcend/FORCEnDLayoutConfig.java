/* 
* Created on 4. October 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.forcend;

import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.io.ConfigFile;

/**
 * This class contains the input static variables that apply only to the force layouting phase.
 * Also includes methods for reading and writing the parameters from a config file. 
 * 
 * @author sita
 */
public class FORCEnDLayoutConfig {
	
	//	 --------------------- FIXED VARIABLES ---------------------- //
	
	/* set nodes on same position to a minimal distance, so it is not zero */
	public static final double MIN_DISTANCE = 0.0001;
	/* the minimal distance a node can move in one iteration, saves unnecessary steps */
	// TODO check which value is suitable! In which range do the values move in?
	public static final double MIN_MOVEMENT = 1e-7;
	
	//	 ------------------------------------------------------------------- //
	
	// -------------------- CONFIG VARIABLES --------------------- //
	public static double attractionFactor = 100;
//	public static double attractionFactor = 1.2448524402942829;
//	public static double attractionFactor = 0.9432118232029466;
	public static double repulsionFactor = 100;
//	public static double repulsionFactor = 1.6866447301914302;
//	public static double repulsionFactor = 1.2779532734686379218393975097234;
//	public static double maximalDisplacement = 1000;
	public static int iterations = 100;
	public static float temperature = 100;
//	public static double influenceOfGraphSizeToForces = 1.3198015648987826 ;
	// ------------------------------------------------------------------- //
	
	
	/**
	 * This methods loads every necessary parameters from the given ConfigFile for
	 * FORCEnD
	 * 
	 * @param rb
	 *            The PropertyResourceBundle object for the config file.
	 */
	public static void initFromConfigFile(PropertyResourceBundle rb)
			throws MissingResourceException {

		attractionFactor = Double.parseDouble(rb.getString("forcend.attractionFactor").
				trim());
		repulsionFactor = Double.parseDouble(rb.getString("forcend.repulsionFactor").
				trim());
//		maximalDisplacement = Double.parseDouble(rb.getString(
//				"forcend.maximalDisplacement").trim());
		iterations = Integer.parseInt(rb.getString("forcend.iterations").trim());
		temperature = Float.parseFloat(rb.getString("forcend.temperature").trim());
//		influenceOfGraphSizeToForces = Double.parseDouble(rb.getString(
//				"forcend.influenceOfGraphSizeToForces").trim());
	}
	
	/**
	 * Print FORCEnD parameters to given ConfigFile.
	 * 
	 * @param confile The ConfigFile to be written to.
	 */
	public static void printParametersToConfig(ConfigFile confile){
		
		confile.printSubHeader("FORCEnD");
		confile.printParameter("forcend.attractionFactor", ""+FORCEnDLayoutConfig.attractionFactor);
		confile.printParameter("forcend.repulsionFactor", ""+FORCEnDLayoutConfig.repulsionFactor);
//		confile.printParameter("forcend.maximalDisplacement", ""+FORCEnDLayoutConfig.maximalDisplacement);
		confile.printParameter("forcend.iterations", ""+FORCEnDLayoutConfig.iterations);
		confile.printParameter("forcend.temperature", ""+FORCEnDLayoutConfig.temperature);
//		confile.printParameter("forcend.influenceOfGraphSizeToForces", ""+FORCEnDLayoutConfig.influenceOfGraphSizeToForces);
	}
	
}
