/*
 * Created on 7. April 2008
 * 
 */
package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
// import java.util.logging.Logger;
//

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.TransClustCluster;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.ConnectedComponent;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.datastructure.CostMatrixReader;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.ILayoutInitialiser;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.IParameters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.forcend.FORCEnDParameters;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.layout.parameter_training.ParameterTraining_SE;

/**
 * Implements a simple evolutionary parameter training on the complete input, which is either
 * one file or a directory. The actual training works the same way as the training for each
 * {@link ConnectedComponent} separately as in {@link ParameterTraining_SE}.
 * 
 * Simple description:
 * The number of generations and the number of configurations per generations are
 * taken from the config class. The initial generation consists of only random parameter
 * configurations and 10 more than the given number of configurations per generation.
 * The 10 best configurations are saved in a list of 'best parameter configurations'.
 * Then for each generation that should be trained the following make-up of parameters 
 * is used:
 * 1. First parameters object is created by using the mean value of each of the previously collected
 * best parameter configurations.
 * 2. The rest is divided into thirds.
 * 		a) A random combination of the previously found best parameter configurations
 * 		b) A random combination of the previously found best parameter configurations and
 * 			additionally new random parameter values.
 * 		c) A complete random set of new parameter configurations within given bounds.
 * 
 * Then each generation is started. For each parameter configuration in the generation,
 * clustering occurs with the given input configurations and for all input cost matrices. The
 * clustering score is added to a total and this is set as the score in the parameters object.
 * Then, according to this score, the set of parameter objects for one generation is sorted.
 * 
 * After each generation has been trained, the best half of the parameters are added
 * to the list of 'best parameter configurations'. Also it is checked if the training should be terminated.
 * This is done by looking at the scores. The training terminates when all scores are the
 * highest, allowing for only two configurations to be below this top score.
 * 
 * @author Sita Lange
 *
 */
public class GeneralParameterTraining {
	
	// private static Logger log = Logger.getLogger(GeneralParameterTraining.class.getName());
	
	/* to do the actual clustering in training mode */
	private ClusteringManager clusteringManager;

	/* size of each generationg */
	private int generationSize = TaskConfig.noOfParameterConfigurationsPerGeneration;

	/* number of generations to carry out */
	private int noOfGenerations = TaskConfig.noOfGenerations;

	private IParametersArrayComparator paramComparator = null;

	/*
	 * previous best parameter configuration this means only one instance should
	 * be created for the parameter training of one dataset.
	 */
	private IParameters[] bestPreviousIParameters = null;

	/* collection of all best configurations found */
	private Vector<IParameters[]> bestConfigs = null;

	
	public GeneralParameterTraining(ClusteringManager clusteringManager){
		this.clusteringManager = clusteringManager;
		this.paramComparator = new IParametersArrayComparator();
		this.bestConfigs = new Vector<IParameters[]>();
	}

	/**
	 * Starts the general parameter training using a simple evolutionary method
	 * for the whole input directory (or just one file if
	 * this was given). Saves the best found parameters of each layout algorithm to the
	 * respective config classes. But does not create a config file.
	 * 
	 * @return The best set of IParameters as an Array.
	 * @throws InvalidInputFileException If the input file/directory is somehow 
	 * 					incorrect or cannot be read.
	 * @throws InvalidTypeException If an method type does not exist or hasn't 
	 * 					been included properly.
	 */
	public IParameters[] runGeneralTraining() throws InvalidInputFileException, 
		InvalidTypeException{
		
		/* initialise connected components and parameters */
		this.clusteringManager.initParametersAndCCs();
		ArrayList<File> connectedComponents = clusteringManager.
			getConnectedComponents();
		
		/*
		 * initialise positions of the cc with respect to the FIRST layouter - the same 
		 * initial positions are used for all training rounds
		 */
		for (File cc : connectedComponents) {
			ILayoutInitialiser li = TaskConfig.layouterEnumTypes[0].createLayoutInitialiser();
			li.initLayoutInitialiser(new CostMatrixReader(cc).getConnectedComponent());
			li.run();
		}

		boolean terminateTraining = false;

		/* run initial generation */
		IParameters[][] initialGeneration = createInitialParameterGeneration();
		runOneGeneration(initialGeneration, connectedComponents, 0);
		terminateTraining = terminateTraining(initialGeneration);
		/* add the best 10 random configs to the bestConfigs collection Vector */
		for (int i = 0; i < initialGeneration.length; i++) {
			this.bestConfigs.add(initialGeneration[i]);
		}

		/* run all following generations */
		IParameters[][] generation;
		for (int i = 1; i <= this.noOfGenerations; i++) {
			if (terminateTraining) {
				break;
			}

			generation = createParameterGeneration();
			runOneGeneration(generation, connectedComponents, i);
			terminateTraining = terminateTraining(generation);
			for (int j = 0; j < this.generationSize / 2; j++) {
				this.bestConfigs.add(initialGeneration[j]);
			}
		}

		/* convert best configurations vector to array */
		IParameters[][] bestConfigsArray = new IParameters[bestConfigs.size()]
		                                                   [TaskConfig.layouterEnumTypes.length];
		for (int i = 0; i < bestConfigs.size(); i++) {
			bestConfigsArray[i] = bestConfigs.get(i);
		}
		/*
		 * sort the IParameters array according to their score and return the
		 * best parameter set.
		 */
		Arrays.sort(bestConfigsArray, this.paramComparator);
		this.bestPreviousIParameters = bestConfigsArray[0];
		
		for (int i = 0; i < bestConfigsArray[0].length; i++) {
			bestConfigsArray[0][i].saveParametersToConfig();
		}
		

		for (int i = 0; i < 30; i++) {
			FORCEnDParameters ip = (FORCEnDParameters) bestConfigsArray[i][0];
			ip.printParamters();
		}
		
		return bestConfigsArray[0];
	}

	private IParameters[][] createParameterGeneration() {
		IParameters[][] paramsGen = new IParameters[this.generationSize][TaskConfig.
		                                                                 layouterEnumTypes.length];

		/* add mean of best configs */
		IParameters param = null;
		for(int i=0;i<TaskConfig.layouterEnumTypes.length;i++){
			/* get array of best configurations for one layouter */
			IParameters[] bestConfigsArray = new IParameters[bestConfigs.size()];
			for (int j = 0; j < bestConfigs.size(); j++) {
				bestConfigsArray[j] = bestConfigs.get(j)[i];
			}
			
			param = TaskConfig.layouterEnumTypes[i].createIParameters();
			param.combineConfigurationsRandomly(bestConfigsArray);
			paramsGen[0][i] = param;
		}

		int currentPosition = 0;
		int third = (this.generationSize - 1) / 3;

		/* add combinations of the best configurations for first third */
		for (int i = currentPosition; i < currentPosition + third; i++) {
			for(int j=0;j<TaskConfig.layouterEnumTypes.length;j++){
				/* get array of best configurations for one layouter */
				IParameters[] bestConfigsArray = new IParameters[bestConfigs.size()];
				for (int k = 0; k < bestConfigs.size(); k++) {
					bestConfigsArray[k] = bestConfigs.get(k)[j];
				}
				param = TaskConfig.layouterEnumTypes[j].createIParameters();
				param.combineConfigurationsRandomly(bestConfigsArray);
				paramsGen[i][j] = param;
			}
		}
		currentPosition += third;

		/* add combinations of best half plus new random parameters */
		for (int i = currentPosition; i < currentPosition + third; i++) {
			for(int j=0;j<TaskConfig.layouterEnumTypes.length;j++){
				/* get array of best configurations for one layouter */
				IParameters[] bestConfigsArray = new IParameters[bestConfigs.size()];
				for (int k = 0; k < bestConfigs.size(); k++) {
					bestConfigsArray[k] = bestConfigs.get(k)[j];
				}
				param = TaskConfig.layouterEnumTypes[j].createIParameters();
				param.combineParametersRandomlyAndGetNewRandom(bestConfigsArray);
				paramsGen[i][j] = param;
			}
		}
		currentPosition += third;

		/* fill the rest with random configuratons */
		for (int i = currentPosition; i < this.generationSize; i++) {
			paramsGen[i] = this.createRandomParametersForLayouters();
		}
		return paramsGen;
	}

	/**
	 * Check to see if the training should be terminated.
	 * This happens when all except at most two have the
	 * best score. If the number of parameter configurations is below
	 * 10, then the training is never terminated early.
	 * 
	 * @param parameters The parameters with the clustering score for one generation.
	 * @return If the training should be terminated after this training generation.
	 */
	private boolean terminateTraining(IParameters[][] parameters) {
		boolean terminate = true;
		Arrays.sort(parameters, this.paramComparator);
		double bestScore = parameters[0][0].getScore();
		if (bestScore == 0||parameters.length<10) {
			terminate =  false;
		} else {
			for (int i = 1; i < parameters.length - 2; i++) {
				if (parameters[i][0].getScore() > bestScore) {
					terminate =  false;
				}
			}
		}
		return terminate;
	}

	private void runOneGeneration(IParameters[][] parameters, 
			ArrayList<File> connectedComponents, int gen) throws InvalidInputFileException, InvalidTypeException {
		TaskConfig.monitor.setStatusMessage("Start training generation: "+gen);
	
		int size = parameters.length;

		/* do clustering for each set of parameter configurations */
		for (int i = 0; i < size; i++) {

			/* copy the cc (ConnectedComponent) ArrayList - shares same resources! */
			ArrayList<ConnectedComponent> newConnectedComponents =
				new ArrayList<ConnectedComponent>(connectedComponents.size());
//			for (int j = 0; j < connectedComponents.size(); j++) {
//				ConnectedComponent cc = new CostMatrixReader(connectedComponents.get(j)).getConnectedComponent();
//				ConnectedComponent newCC = cc.copy();
				
				/* sets a new positions array for the copy */
//				newCC.setCCPositions(cc.copyCCPositions());
				
//				newConnectedComponents.add(newCC);
//			}

			/* start clustering */
			clusteringManager.setConnectedComponents(connectedComponents);
			clusteringManager.setLayouterParameters(parameters[i]);
			clusteringManager.runClustering();
			FORCEnDParameters fndparam = (FORCEnDParameters)parameters[i][0];
			fndparam.printParamters();
			newConnectedComponents.clear();
		}
		//log.debug("Finished training generation: "+gen);
	}

	private IParameters[][] createInitialParameterGeneration() {
		// create enough random configurations to start with
		
		ArrayList<FORCEnDParameters> parameters = new ArrayList<FORCEnDParameters>();
		
		for (double attraction = 1; attraction <= 100; attraction+=5) {
//			for (double repulsion = 1; repulsion <= 100; repulsion+=5) {
//				if((attraction*10)<=repulsion||(repulsion*10)<=attraction) continue;
				for (float temperature = 100; temperature <= 1000; temperature+=100) {
					for (int iterations = 50; iterations < 500; iterations+=50) {
						FORCEnDParameters fndparam = new FORCEnDParameters();
						fndparam.setAttractionFactor(attraction);
						fndparam.setRepulsionFactor(attraction);
						fndparam.setTemperature(temperature);
						fndparam.setIterations(iterations);
						parameters.add(fndparam);
//						System.out.println(" iterations " + iterations);
					}
//					System.out.println(" temperature " + temperature);
				}
//				System.out.println(" repulsion " + repulsion);
//			}
//			System.out.println(" attraction " + attraction);
		}
		
		
		
//		int initialSize = this.generationSize + 10;
		IParameters[][] paramsGen = new IParameters[parameters.size()][TaskConfig.
		                                                         layouterEnumTypes.length];
//		paramsGen[0] = clusteringManager.getLayouterParameters();
//
//		/*
//		 * get the best parameter configuration for the previous training round
//		 * if it exists - otherwise use a random configuration.
//		 */
//		if (this.bestPreviousIParameters != null) {
//			paramsGen[1] = bestPreviousIParameters;
//		} else {
//			paramsGen[1] = this.createRandomParametersForLayouters();
//		}
//		for (int i = 2; i < initialSize; i++) {
//			paramsGen[i] = this.createRandomParametersForLayouters();
//		}
		for (int i = 0; i < paramsGen.length; i++) {
			paramsGen[i][0] = parameters.get(i);
		}
		return paramsGen;
	}
	
	/**
	 * Creates a random configuration of parameters as an IParameters object
	 * for each layouter used and in the correct order.
	 * @return The array of IParameters with random configuration.
	 */
	private IParameters[] createRandomParametersForLayouters(){
		IParameters[] params = new IParameters[TaskConfig.layouterEnumTypes.length];
		for (int i = 0; i < TaskConfig.layouterEnumTypes.length; i++) {
			params[i] = TaskConfig.layouterEnumTypes[i].createIParameters();
			params[i].createRandomConfiguration();
		}
		return params;
	}
}
