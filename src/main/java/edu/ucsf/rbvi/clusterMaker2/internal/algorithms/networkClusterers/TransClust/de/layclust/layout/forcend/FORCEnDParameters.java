/*
 * Created on 7. November 2007
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.forcend;

import java.util.Random;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;

/**
 * This is an implementation of the IParameters interface for the FORCEnD
 * algorithm. It contains all the variable and user defined parameters that exist.
 * It has methods to generate new values for the parameters in different ways and
 * also to read, write, and save the values.
 * 
 * @author Sita Lange
 *
 */
public class FORCEnDParameters implements IParameters {
	
	private double attractionFactor = 0;
	private double repulsionFactor = 0;
//	private double maximalDisplacement = 0;
	private int iterations = 0;
	private float temperature = 0;
//	private double influenceOfGraphSizeToForces = 0;

	private double score = 0;
	

	/**
	 * Sets the parameters of this instance to the mean value of the parameters given 
	 * in the array.
	 * 
	 * @param configurations The array of all parameters for which the mean should be calculated.
	 */
	public void combineConfigurationsMean(IParameters[] configurations) {
		initialiseToZero();
		/* add all values */
		for(int i=0; i<configurations.length;i++){
			this.attractionFactor +=((FORCEnDParameters) configurations[i]).
					getAttractionFactor();
			this.iterations += ((FORCEnDParameters) configurations[i]).getIterations();
//			this.maximalDisplacement += ((FORCEnDParameters) 
//					configurations[i]).getMaximalDisplacement();
			this.repulsionFactor += ((FORCEnDParameters) configurations[i]).getRepulsionFactor();
			this.temperature += ((FORCEnDParameters) configurations[i]).getTemperature();
			// TODO add extra param here
		}
		/* divide by the number of configurations */
		this.attractionFactor /= configurations.length;
//		this.influenceOfGraphSizeToForces /= configurations.length;
		this.iterations /= configurations.length;
//		this.maximalDisplacement /= configurations.length;
		this.repulsionFactor /= configurations.length;
		this.temperature /= configurations.length;
		//TODO add extra param here
	}

	/**
	 * Takes the given IParameters array and  for each parameter of this instance the configurations
	 * of the given objects are combined randomly, so that a new set of configurations is created
	 * from the old ones in the given array.
	 * 
	 * @param configurations An array containing a set of IParameters objects, which already
	 * have been initialised with values.
	 */
	public void combineConfigurationsRandomly(IParameters[] configurations) {

		initialiseToZero();
		
		int pos;
		
		Random generator = new Random();
		
		pos = generator.nextInt(configurations.length);
		this.attractionFactor = ((FORCEnDParameters) configurations[pos]).
				getAttractionFactor();
				
		pos = generator.nextInt(configurations.length);
//		this.influenceOfGraphSizeToForces = ((FORCEnDParameters) 
//				configurations[pos]).getInfluenceOfGraphSizeToForces();
		
		pos = generator.nextInt(configurations.length);
		this.iterations = ((FORCEnDParameters) configurations[pos]).getIterations();
		
//		pos = generator.nextInt(configurations.length);
//		this.maximalDisplacement = ((FORCEnDParameters) configurations[pos]).
//				getMaximalDisplacement();
		
		pos = generator.nextInt(configurations.length);
		this.repulsionFactor = ((FORCEnDParameters) configurations[pos]).
				getRepulsionFactor();
		
		pos = generator.nextInt(configurations.length);
		this.temperature = ((FORCEnDParameters) configurations[pos]).
				getTemperature();
		// TODO add extra param here
	}
	
	/**
	 * This method randomly decides whether to create a new random parameter value, or
	 * to take one of the old ones from the given array. If taken from the existing IParameters object
	 * one of the respective values is also chosen randomly.
	 * The parameters of this instance of IParameters is then set with the chosen values.
	 * 
	 * @param configurations An array containing a set of IParameters objects, which already
	 * have been initialised with values.
	 */
	public void combineParametersRandomlyAndGetNewRandom(IParameters[] configurations){
		
		Random generator = new Random();		
		int pos;
		
		int randomOrOld = generator.nextInt(2);		
		if(randomOrOld == 0){ // get an old parameter
			pos = generator.nextInt(configurations.length);
			this.attractionFactor = ((FORCEnDParameters) configurations[pos]).
					getAttractionFactor();			
		} else { // get a new random parameter
			this.attractionFactor = generator.nextInt(100);
		}
		
		randomOrOld = generator.nextInt(2);
		if(randomOrOld == 0){ // get an old parameter
			pos = generator.nextInt(configurations.length);
			this.iterations = ((FORCEnDParameters) configurations[pos]).getIterations();
		} else { // get a new random parameter
			this.iterations = 10 + generator.nextInt(90);
		}
		
//		randomOrOld = generator.nextInt(2);
//		if(randomOrOld == 0){ // get an old parameter
//			pos = generator.nextInt(configurations.length);
//			this.maximalDisplacement = ((FORCEnDParameters) configurations[pos]).
//					getMaximalDisplacement();
//		} else { // get a new random parameter
//			this.maximalDisplacement = 50 + generator.nextInt(1000);
//		}
		
		randomOrOld = generator.nextInt(2);
		if(randomOrOld == 0){ // get an old parameter
			pos = generator.nextInt(configurations.length);
			this.repulsionFactor = ((FORCEnDParameters) configurations[pos]).
					getRepulsionFactor();
		} else { // get a new random parameter
			this.repulsionFactor = generator.nextInt(100);
		}
		
		randomOrOld = generator.nextInt(2);
		if(randomOrOld == 0){ // get an old parameter
			pos = generator.nextInt(configurations.length);
			this.temperature = ((FORCEnDParameters) configurations[pos]).
					getTemperature();
		} else { // get a new random parameter
			this.temperature = 50 + generator.nextInt(100);
		}
		
		// TODO add extra param here and check ranges for random generation
		
	}

	/**
	 * Here this instance of IParameters is set with totally new and random values that
	 * lie within certain boundaries.
	 */
	public void createRandomConfiguration() {
		
		Random generator = new Random();

		this.attractionFactor = 10 * generator.nextDouble();
		this.repulsionFactor = 10 * generator.nextDouble();
		this.iterations = 10 + generator.nextInt(90);
		this.temperature = 50 + generator.nextInt(2500);
//		this.maximalDisplacement = 50 + generator.nextInt(1000);
	}

	/**
	 * Sets all parameter values to zero.
	 */
	public void initialiseToZero() {
		this.attractionFactor = 0;
//		this.maximalDisplacement = 0;
		this.repulsionFactor = 0;
		this.iterations = 0;
		this.temperature = 0;
		// TODO add extra param here
	}

	/**
	 * Sets the parameters of this instance with values from the config.
	 */
	public void readParametersFromConfig() {	
		this.attractionFactor = FORCEnDLayoutConfig.attractionFactor;
		this.repulsionFactor = FORCEnDLayoutConfig.repulsionFactor;
//		this.maximalDisplacement = FORCEnDLayoutConfig.maximalDisplacement;
		this.iterations = FORCEnDLayoutConfig.iterations;
		this.temperature = FORCEnDLayoutConfig.temperature;
		//TODO add extra param here
	}


	/**
	 * Changes the parameters in the config class to the ones in this instance.
	 */
	public void saveParametersToConfig() {
		FORCEnDLayoutConfig.attractionFactor = this.attractionFactor;
//		FORCEnDLayoutConfig.maximalDisplacement = this.maximalDisplacement;
		FORCEnDLayoutConfig.repulsionFactor = this.repulsionFactor;
		FORCEnDLayoutConfig.iterations = this.iterations;
		FORCEnDLayoutConfig.temperature = this.temperature;
		//TODO add extra param here
	}
	
	
	public void printParamters(){
		
		System.out.println("attraction = " + this.attractionFactor);
		System.out.println("repulsion = " + this.repulsionFactor);
		System.out.println("temperature = " + this.temperature );
		System.out.println("iterations = " + this.iterations);
		System.out.println("score = " + this.score);
		System.out.println();
		
		
	}

	/**
	 * @return the attractionFactor
	 */
	public double getAttractionFactor() {
		return attractionFactor;
	}

	/**
	 * @param attractionFactor the attractionFactor to set
	 */
	public void setAttractionFactor(double attractionFactor) {
		this.attractionFactor = attractionFactor;
	}

	/**
	 * @return the influenceOfGraphSizeToForces
	 */
//	public double getInfluenceOfGraphSizeToForces() {
//		return influenceOfGraphSizeToForces;
//	}

	/**
	 * @param influenceOfGraphSizeToForces the influenceOfGraphSizeToForces to set
	 */
//	public void setInfluenceOfGraphSizeToForces(double influenceOfGraphSizeToForces) {
//		this.influenceOfGraphSizeToForces = influenceOfGraphSizeToForces;
//	}

	/**
	 * @return the maximalDisplacement
	 */
//	public double getMaximalDisplacement() {
//		return maximalDisplacement;
//	}

	/**
	 * @param maximalDisplacement the maximalDisplacement to set
	 */
//	public void setMaximalDisplacement(double maximalDisplacement) {
//		this.maximalDisplacement = maximalDisplacement;
//	}

	/**
	 * @return the repulsionFactor
	 */
	public double getRepulsionFactor() {
		return repulsionFactor;
	}

	/**
	 * @param repulsionFactor the repulsionFactor to set
	 */
	public void setRepulsionFactor(double repulsionFactor) {
		this.repulsionFactor = repulsionFactor;
	}

	/**
	 * @return the rounds
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * @param iterations the number of iterations to set
	 */
	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	/**
	 * @return the temperature
	 */
	public float getTemperature() {
		return temperature;
	}

	/**
	 * @param temperature the temperature to set
	 */
	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	/**
	 * Gets the score for the clustering with these parameter values.
	 * @return the score The clustering cost for these set of parameters.
	 */
	public double getScore() {
		return score;
	}

	/**
	 * Sets the score for the clustering with these parameter values.
	 * @param score The clustering cost for these set of parameters.
	 */
	public void setScore(double score) {
		this.score = score;
	}
	
	/**
	 * Creates the string representation of the parameters.
	 * @return Returns The String representation of the object.
	 */
	public String toString(){
		StringBuffer paramString = new StringBuffer();
		
		paramString.append("FORCEnD paramter Configuration:");
		paramString.append("\n score - ");
		paramString.append(this.score);
		paramString.append("\n iterations - ");
		paramString.append(this.iterations);
		paramString.append("\n attractionFactor - ");
		paramString.append(this.attractionFactor);
		paramString.append("\n repulsionFactor - ");
		paramString.append(this.repulsionFactor);
//		paramString.append("\n influenceOfGraphSizeToForces - ");
//		paramString.append(this.influenceOfGraphSizeToForces);
//		paramString.append("\n maximalDisplacement - ");
//		paramString.append(this.maximalDisplacement);
		paramString.append("\n temperature - ");
		paramString.append(this.temperature);
				
		return paramString.toString();
	}
}
