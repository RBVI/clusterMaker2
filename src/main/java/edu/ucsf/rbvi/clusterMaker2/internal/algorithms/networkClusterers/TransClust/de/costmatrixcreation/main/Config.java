package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main;

public class Config {
	
	public static boolean gui;
	
	public static float threshold;
	
	public static int costModel;
	
	public static String blastFile;
	
	public static String fastaFile;
	
	public static String similarityFile;
	
	public static String costMatrixDirectory;
	
	public static boolean createSimilarityFile;
	
	public static final String BLAST = "blast";
	public static final String EXPRESSION = "expression";

	public static final int PEARSON = 1;

	public static final int NEGATIVEEUCLIDIAN = 2;
	
	public static String source = BLAST;
	
	public static boolean splitAndWriteCostMatrices;
	
	public static double penaltyForMultipleHighScoringPairs;
	
	public static double blastCutoff;
	
	public static int coverageFactor;
	
	public static int linesInSimilarityFile;
	
	public static float upperBound;
	
	public static boolean reducedMatrix;
	
	public static boolean useMinSimilarityOfBothDirections;
	
	public static float defaultCostsForMissingEdges;

	public static boolean withHeader;

	public static boolean withRowDescription;

	public static String expressionMatrix;
	
	
	public static void init(Args options){
		
		createSimilarityFile = true;
		
		splitAndWriteCostMatrices = true;
		
//		blastCutoff = 0.01;
		blastCutoff = 1e-5;
		
		penaltyForMultipleHighScoringPairs = blastCutoff; 
		
		coverageFactor = 0;
		
		linesInSimilarityFile = 0;
		
		upperBound = 100;
		
		threshold = 10;
		
		costModel = 0;
		
		gui = true;
		
		reducedMatrix = false;
		
		useMinSimilarityOfBothDirections = true;
		
		defaultCostsForMissingEdges = 0;
		
		setOptionalConfigurationVariables(options);
		
		if(!gui) testAndPrintErrors();
		
	}
	
	private static void setOptionalConfigurationVariables(Args options) {
		
			try {
				reducedMatrix = options.getBoolValue("rm");
			} catch (Exception e) {
			}
		
			try {
				gui = options.getBoolValue("gui");
			} catch (Exception e) {
			}
		
		
			try{
				createSimilarityFile = options.getBoolValue("cs");
			} catch (Exception e) {
			}
			
			try{
				splitAndWriteCostMatrices = options.getBoolValue("sp");
			} catch (Exception e) {
			}
			
			try {
				blastFile = options.getStringValue("b");
			} catch (Exception e) {
			}
			
			try {
				similarityFile = options.getStringValue("s");
			} catch (Exception e) {
			}
			
			try {
				fastaFile = options.getStringValue("f");
			} catch (Exception e) {
			}
			  
			try {
				costMatrixDirectory = options.getStringValue("c");
			} catch (Exception e) {
			}
			
			try {
				threshold = options.getFloatValue("t");
			} catch (Exception e) {
			}
			
			try {
				costModel = options.getIntValue("m");
			} catch (Exception e) {
			}
			
			try {
				blastCutoff = options.getDoubleValue("bc");
			} catch (Exception e) {
			}
			
			try {
				penaltyForMultipleHighScoringPairs = options.getDoubleValue("p");
			} catch (Exception e) {
			}
			
			try{
				coverageFactor = options.getIntValue("cf");
			} catch (Exception e) {
			}
			
			try{
				upperBound = options.getFloatValue("ub");
			} catch (Exception e) {
			}
			
	}
	
	private static void testAndPrintErrors(){
		
		boolean error = false;
		
		if(createSimilarityFile){
			
			if(blastFile==null||similarityFile==null||fastaFile==null){
				System.out.println("You have at least to specify: \n -blastFile (-b)  \n -similarityFile (-s) \n -fastaFile (-f)  \n -costModel (-m)");
				error = true;
			}
			
		}
		
		if(splitAndWriteCostMatrices){
			
			if(similarityFile==null||costMatrixDirectory==null){
				if(error){
					System.out.println(" -costMatrixDirectory (-c)  \n -threshold (-t)");
				}else{
					System.out.println("You have at least to specify: \n -similarityFile (-s) \n -costMatrixDirectory (-c)  \n -threshold (-t)");
					error = true;
				}
			}
			
		}
		
		if(error){
			printVariables();
			CostMatrixCreator.printUsage();
			System.exit(1);
		}
		
	}

	private static void printVariables() {
		
		System.out.println("threshold = " + threshold);
		
		System.out.println("costModel = " + costModel);
		
		System.out.println("blastFile = " + blastFile);
		
		System.out.println("fastaFile = " + fastaFile);
		
		System.out.println("similarityFile = " + similarityFile);
		
		System.out.println("costMatrixDirectory = " + costMatrixDirectory);
		
		System.out.println("createSimilarityFile = " + createSimilarityFile);
		
		System.out.println("splitAndWriteCostMatrices = " + splitAndWriteCostMatrices);
		
		System.out.println("penaltyForMultipleHighScoringPairs = " + penaltyForMultipleHighScoringPairs);
		
		System.out.println("blastCutoff = " + blastCutoff);
		
		System.out.println("coverageFactor = " + coverageFactor);
		
		System.out.println("upperBound = " + upperBound);

			
	}

}
