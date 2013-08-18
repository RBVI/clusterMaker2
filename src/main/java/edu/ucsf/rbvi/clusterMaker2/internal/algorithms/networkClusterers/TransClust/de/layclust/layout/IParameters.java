/*
 * Created on 7. November 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout;

/**
 * This is the interface for any parameter objects for the layout algorithms. The classes 
 * implementing this should contain a set of global variables for all the parameters that
 * are variable in the algorithms and should implement the defined methods below.
 * These are used to create new values with different methods and for saving, reading
 * and writing the parameter values within the program.
 * 
 * @author Sita Lange
 *
 */
public interface IParameters{
	
	/**
	 * Sets the parameters of this instance with values from the config.
	 */
	public void readParametersFromConfig();
	
	/**
	 * Changes the parameters in the config class to the ones in this instance.
	 */
	public void saveParametersToConfig();

	/**
	 * Here this instance of IParameters is set with totally new and random values that
	 * lie within certain boundaries.
	 */
	public void createRandomConfiguration();
	
	/**
	 * Sets all parameter values to zero.
	 */
	public void initialiseToZero();
	
	/**
	 * Sets the parameters of this instance to the mean value of the parameters given 
	 * in the array.
	 * 
	 * @param configurations The array of all parameters for which the mean should be calculated.
	 */
	public void combineConfigurationsMean(IParameters[] configurations);
	
	/**
	 * Takes the given IParameters array and  for each parameter of this instance the configurations
	 * of the given objects are combined randomly, so that a new set of configurations is created
	 * from the old ones in the given array.
	 * 
	 * @param configurations An array containing a set of IParameters objects, which already
	 * have been initialised with values.
	 */
	public void combineConfigurationsRandomly(IParameters[] configurations);
	
	/**
	 * This method randomly decides whether to create a new random parameter value, or
	 * to take one of the old ones from the given array. If taken from the existing IParameters object
	 * one of the respective values is also chosen randomly.
	 * The parameters of this instance of IParameters is then set with the chosen values.
	 * 
	 * @param configurations An array containing a set of IParameters objects, which already
	 * 					have been initialised with values.
	 */
	public void combineParametersRandomlyAndGetNewRandom(IParameters[] configurations);
	
	/**
	 * Creates the string representation of the parameters.
	 * @return Returns The String representation of the object.
	 */
	public String toString();
	
	/**
	 * Gets the score for the clustering with these parameter values.
	 * @return the score The clustering cost for these set of parameters.
	 */
	public double getScore();
	
	/**
	 * Sets the score for the clustering with these parameter values.
	 * @param score The clustering cost for these set of parameters.
	 */
	public void setScore(double score);
	
}
