package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes.BlastFile;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes.CostMatrix;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes.Edges;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.gui.Console;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.layclust.taskmanaging.TaskConfig;


public class InOut {
	
	public static String TAB = "\t";
	
	public static String NL = "\n";
	
	public static String delimiter = "#";
	
	public static float min = Float.MAX_VALUE;
	
	
	public static int[] readFastaFile(String fastaFile, HashMap<Integer,String> proteins2integers, HashMap<String,Integer> integers2proteins) throws IOException{
		
		BufferedReader br = new BufferedReader(new FileReader(fastaFile));
			
		String protein = "";
		
		Vector<Integer> v = new Vector<Integer>();
		
		int i = -1;
		String sequence = "";
		String line;

		while((line = br.readLine())!=null){
			
			if(line.startsWith(">")){
				
				if(i!=-1) v.add(sequence.length());
				String tokens[] = line.split(" ");
				i++;
				protein =tokens[0].substring(1);
				proteins2integers.put(i, protein);
				integers2proteins.put(protein, i);
				sequence = "";
				
			}else{
				
				sequence+=line;
				
			}
		}
		
		v.add(sequence.length());
				
		int[] proteinlengths = new int[v.size()];
		
		for (int j = 0; j < v.size(); j++) 	proteinlengths[j] = v.get(j);
			
		br.close();
		System.gc();
		
		return proteinlengths;
	}
	
	public static BlastFile readBlastFileWithArray(String blastFile,HashMap<String ,Integer> integers2proteins,int[] proteinlengths) throws IOException{
		
		File f = new File(blastFile);
		BufferedReader br = new BufferedReader(new FileReader(blastFile));
	
		if(Config.gui){
			Console.restartBar(0, 100);
			Console.setBarText("start counting lines of BLAST file");
		}else{
		//	System.out.print("\t start counting lines of BLAST file ...");
		}
		
		int lineCount =countLines(f);

		if(Config.gui){
			Console.println("" +lineCount);
			Console.println();
		}else{
		}

		BlastFile bf = new BlastFile(lineCount);
		
		br.close();
		br =  new BufferedReader(new FileReader(blastFile));

		if(Config.gui){
			Console.println("\t start reading BLAST file ...");
			Console.restartBar(0, 100);
			Console.setBarText("reading blast file");
		}else{
		}
		
		int i = 0;
		double percent = 0;
		double percentOld = 0;
		
		double normalizeFactorFromBlastCutoff = Math.log10(Config.blastCutoff); 
		
		try {
			while(true){
				
				String line = br.readLine();
				
				if(i%10000==0&&i>0){
					percent  = Math.rint(((double) i / lineCount)*10000)/100;
					if(percent>percentOld+1){
						percentOld=percent;
						if(Config.gui){
							Console.setBarValue((int) Math.rint(percent));
							Console.setBarTextPlusRestTime("reading BLAST file  " + percent + " %");
						}//else System.out.print(percent + " %" + "\t");
					}				
				}
				
				String columns[] = line.split(TAB);
				String source = columns[0];
				int sourceInt = integers2proteins.get(source);
				
				String target = columns[1];
				int targetInt = integers2proteins.get(target);
				
				int startQuery = Integer.parseInt(columns[6]);

				int endQuery = Integer.parseInt(columns[7]);

				int startSubject = Integer.parseInt(columns[8]);
		
				int endSubject = Integer.parseInt(columns[9]);
			
				double evalue = Double.parseDouble(columns[10]);
				
//				double score = Double.parseDouble(columns[11])/proteinlengths[sourceInt];
				double score = Double.parseDouble(columns[11])/Double.parseDouble(columns[3]);
				
				if(evalue<Double.MIN_VALUE) evalue = Double.MIN_VALUE;
						
				if(evalue>Config.blastCutoff){
					score = 0;
					evalue = 0;
				}else{
					evalue = -Math.log10(evalue);
				}
				
				if(Config.blastCutoff>1)	evalue += normalizeFactorFromBlastCutoff ;

				bf.setAll(i, startQuery, endQuery, startSubject, endSubject, sourceInt, targetInt, evalue,score);
				
				i++;
				
				
			}
			
		} catch (Exception e) {
		}
		
		br.close();
		
		if(Config.gui)	Console.println();
		
		return bf;
	}

	public static Edges readSimilarityFile(String file, HashMap<Integer, String> proteins2integers, HashMap<String, Integer> integers2proteins) throws IOException{
		
		
		BufferedReader br = new BufferedReader(new FileReader(file));
				
		int lineCount = 0;
		int proteinNumber = 0;
		if(!Config.createSimilarityFile){
		
			//count lines and create proteins2integers HashMap
			if(Config.gui){
				Console.println("\t start counting lines of similarity file ...");
				Console.restartBar(0, 100);
				Console.setBarText("start counting lines of similarity file");
			}else{
			}
			
			if(Config.fastaFile!=null&&TaskConfig.mode!=TaskConfig.COMPARISON_MODE&&TaskConfig.mode!=TaskConfig.HIERARICHAL_MODE){
				File f = new File(Config.similarityFile);
				lineCount = countLines(f);
				InOut.readFastaFile(Config.fastaFile, proteins2integers, integers2proteins);
			}else{
				try {
					while(true){
														
						String line = br.readLine();
						String tokens[] = line.split(TAB);
						
					
						lineCount++;
						if(lineCount%10000==0&&Config.gui)  Console.setBarText("start counting lines of similarity file  " + lineCount);
							
					
						
						if(!integers2proteins.containsKey(tokens[0])){
							integers2proteins.put(tokens[0], proteinNumber);
							proteins2integers.put(proteinNumber,tokens[0]);
							proteinNumber++;
						}
						
						if(!integers2proteins.containsKey(tokens[1])){
							integers2proteins.put(tokens[1], proteinNumber);
							proteins2integers.put(proteinNumber,tokens[1]);
							proteinNumber++;
						}
						
					}
					
				} catch (Exception e) {
				}
			}
			
			
			
			
			if(Config.gui) Console.println();
			//else System.out.println();
			
		}else lineCount = Config.linesInSimilarityFile;
		
		Edges es = new Edges(lineCount,proteins2integers.size());
		
		// set startPosition of all elements that have no outgoing edge to -1
		for (int i = 0; i < proteinNumber; i++)	es.setStartPosition(i, -1);
		
		//reinitialise BufferedReader
		br.close();
		br = new BufferedReader(new FileReader(file));
	
		int i =0;
		int currentProtein=-1;
		double percent = 0;
		
		// get values
		if(Config.gui){
			Console.println( "\t start reading similarity file ...");
			Console.restartBar(0, 100);
			Console.setBarText("start reading similarity file");
		}//else System.out.println("\t start reading similarity file ...");
		
		double percentOld = 0;
		
//		char[] buffer = new char[4096];
//        
//        for (int charsRead = br.read(buffer); charsRead >= 0; charsRead = br.read(buffer)) {
//        	
//            for (int charIndex = 0; charIndex < charsRead ; charIndex++) {
//            	
//            	if(i%100000==0&&i>0){
//					percent  = Math.rint(((double) i / lineCount)*10000)/100;
//					if(percent>percentOld+1){
//						percentOld = percent;
//						if(Config.gui){
//							Console.setBarValue((int) Math.rint(percent));
//							Console.setBarTextPlusRestTime("reading similarity file  " + percent + " %");
//						}else System.out.print( percent + " %\t" );
//					}			
//				}
//            	
//            	 if(buffer[charIndex]=='\n') {
//            		 es.values[i] = Float.parseFloat(sb.toString());
////					 es.setValue(i, Float.parseFloat(sb.toString()));
////					 sb.delete(0, sb.length());
//					 sb = new StringBuffer();
//					
//					 if(sou!=currentProtein){
//							
//							currentProtein = sou;
//							es.setStartPosition(sou, i);	
//							
//					}
//					i++;
//						
//				 }else 	if(buffer[charIndex]=='\t'){
//					if(so){
//						sou = integers2proteins.get(sb.toString());
////						es.setSource(i, sou);
//						es.sources[i] = sou;
//						sb = new StringBuffer();
////						sb.delete(0, sb.length());
//						so=false;
//					}else{
//						tar = integers2proteins.get(sb.toString());
//						es.targets[i] = tar;
////						es.setTarget(i, tar);
//						sb = new StringBuffer();
////						sb.delete(0, sb.length());
//						so = true;
//					}
//				}else{
//					sb.append(buffer[charIndex]);
//				}
//            }
//        }
		
        String line = "";
        String[] tokens = new String[3];
        int s = -1;
        int t = 0;
        int indexOfFirstTAB;
        int indexOfSecondTAB;
        while ((line =br.readLine())!=null){
        	if(line.equals("")) continue;
        	if(i%100000==0&&i>0){
				percent  = Math.rint(((double) i / lineCount)*10000)/100;
				if(percent>percentOld+1){
					percentOld = percent;
					if(Config.gui){
						Console.setBarValue((int) Math.rint(percent));
						Console.setBarTextPlusRestTime("reading similarity file  " + percent + " %");
					}else System.out.print( percent + " %\t" );
				}			
			}
        	
        	indexOfFirstTAB = line.indexOf('\t');
        	indexOfSecondTAB = line.indexOf('\t', indexOfFirstTAB+1);
			tokens[0] = line.substring(0,indexOfFirstTAB);
			tokens[1] = line.substring(indexOfFirstTAB+1, indexOfSecondTAB);
			tokens[2] = line.substring(indexOfSecondTAB+1,line.length());
			s = integers2proteins.get(tokens[0]);
			t  = integers2proteins.get(tokens[1]);
			
			es.sources[i]= s;
			es.targets[i]=t;
			es.values[i]= Float.parseFloat(tokens[2]);
			if(es.values[i]<InOut.min) InOut.min = es.values[i];
			
			 if(s!=currentProtein){
					
					currentProtein = s;
					es.setStartPosition(s, i);	
					
			}
			
			
			i++;
        }
        
        
		
		if(Config.gui){
			Console.println();
			Console.println("\t start sorting");
			Console.restartBar(0, 100);
			Console.setBarText("start sorting");
		}else{
			//System.out.println();
			//System.out.println("\t start sorting");
		}
		int positions2[] = es.getStartPosition().clone();
		Arrays.sort(positions2);
		
		if(Config.gui){
			Console.println();
			Console.println("\t start finding endpositions");
			Console.restartBar(0, 100);
			Console.setBarText("finding endpositions");
		}else{
			//System.out.println();
		//	System.out.println("\t start finding endpositions");
		}
		
		percentOld=0;
		for (int j = 0; j < positions2.length; j++) {
			
			if(j%100000==0&&j>0){
				
				percent  = Math.rint(((double) j / positions2.length)*10000)/100;
				if(percent>percentOld+1){
					percentOld = percent;
					if(Config.gui){	
						Console.setBarValue((int) Math.rint(percent));
						Console.setBarTextPlusRestTime("finding endpositions  " + percent + " %");
					}else System.out.print(percent + " %\t");
				}
				
			}
			
			int dum = es.getStartPosition(j);
			int k = Arrays.binarySearch(positions2, dum);
			if(k+1<positions2.length){
				
				if(dum == -1) es.setEndPosition(j, -1);
				else	es.setEndPosition(j, positions2[k+1]);
				
			}else es.setEndPosition(j, es.size());
			
		}
		
		if(Config.gui){
			Console.println();
			Console.println("\t start normalizing");
			Console.restartBarTimer();
			Console.setBarValue(0);
			Console.setBarText("normalizing");
		}else{
			//System.out.println();
			//System.out.println("\t start normalizing");
		}
		
//		HashMap<Integer,Boolean> already = new HashMap<Integer, Boolean>(es.size()/2);
		boolean[] already = new boolean[es.size()];
		
		int countPairs = 0;
		int nonPairs = 0;
		percentOld = 0;
		//normalize (set all edges which have only one direction to -1 and otherwise choose the minimal value)
		for (int j = 0; j < es.size(); j++) {
			
			if(j%100000==0&&j>0){
				
				percent  = Math.rint(((double) j / es.size())*10000)/100;
				if(percent>percentOld+1){
					percentOld = percent;
					if(Config.gui){
						Console.setBarValue((int) Math.rint(percent));
						Console.setBarTextPlusRestTime("normalizing  " + percent + " %");
					}//else System.out.print( percent + " %\t");
				}
				
			}
			
			if(!already[j]){
				
				int source = es.sources[j];
//				int source = es.getSource(j);
				int target = es.targets[j];
//				int target = es.getTarget(j);
				float value = es.values[j];
//				float value = es.getValue(j);
				int startPosition = es.startPositions[target];
//				int startPosition = es.getStartPosition(target);
				int endPosition = es.endPositions[target];
//				int endPosition = es.getEndPosition(target);
				
					
				boolean hasPartner = false;

				for (int k = startPosition; k < endPosition; k++) {
					
					int target2 = es.targets[k];
//					int target2 = es.getTarget(k);
								
					if(target2==source){ // Partner found

						already[k] = true;
						hasPartner = true;
						
						float value2 = es.getValue(k);
						
						float minimalValue = Math.min(value, value2);

						es.setValue(j, minimalValue);
						es.setValue(k, minimalValue);
						countPairs++;
						break;
					}
					
				}// end for Partnersearch
				
				if(!hasPartner){
					
//					es.setValue(j,0);
					es.setValue(j,InOut.min);
					nonPairs++;
					
				}
					

				
			}//end test if already calculated
			
		}//end normalizing

		if(Config.gui) Console.println();
		//else System.out.println();
		
		return es;

	}
	


	public static void writeCostMatrices(Edges es, Vector<Vector<Integer>> clusters, HashMap<Integer, String> proteins2integers, HashMap<String, Integer> integers2proteins) throws IOException{
		
		Vector<Vector<Integer>> complete = new Vector<Vector<Integer>>();
		
		int countCostMatrices = 1;
		
		double percentOld=0;
		
		double numberOfProteins = proteins2integers.size();
		
		double alreadySolvedNumberOfProteins = 0;
		
		for (int i = 0; i < clusters.size(); i++) {
			
			Vector<Integer> cluster = clusters.get(i);
			
			int numberEdges = ((cluster.size()*(cluster.size()-1))/2);
			
			int numberProteins = cluster.size();
			
			alreadySolvedNumberOfProteins+=numberProteins;
			
			int countRealEdges = 0;
			
			
			
			if(Config.reducedMatrix){
				CostMatrix cm = new CostMatrix(numberProteins);
				
				HashMap<String,Integer> CmIntegers2proteins = new HashMap<String, Integer>(numberProteins);
				HashMap<Integer, String> CmProteins2integers  = new HashMap<Integer, String>(numberProteins);
				
				for (int j = 0; j < cluster.size(); j++) {
					
					int source = cluster.get(j);
					String protein = proteins2integers.get(source);
					CmProteins2integers.put(j, protein);
					CmIntegers2proteins.put(protein, j);
					
					for (int k = j+1; k < cluster.size(); k++) {
						
						int target = cluster.get(k);
						float value = getEdgeValue(source,target,es);
						cm.setEdgevalues(j, k, value);
						cm.setEdgevalues(k,j,value);
						
						if(value>Config.threshold)	countRealEdges++;

					}
					
				}
				
				cm.setIntegers2proteins(CmIntegers2proteins);
				cm.setProteins2integers(CmProteins2integers);
				
				if(countRealEdges==numberEdges){
					
					complete.add(cluster);
					
				}else{
					String costMatrixFile ="";
					if(TaskConfig.mode==TaskConfig.HIERARICHAL_MODE){
						costMatrixFile = "costMatrix_size_"+cluster.size()+"_nr_"+countCostMatrices + "_" + new Random().nextDouble()+  ".rcm";
					}else{
						costMatrixFile = "costMatrix_size_"+cluster.size()+"_nr_"+countCostMatrices + ".rcm";
					}
			
					
					CostMatrix mergedCM = cm.mergeNodes();
					
					mergedCM.writeCostMatrix(Config.costMatrixDirectory+ "/" +costMatrixFile);
					
					countCostMatrices++;

				}
				
				double percent = Math.rint((((double) alreadySolvedNumberOfProteins)/((double) numberOfProteins))*10000)/100;
				
				if(percent>percentOld+0.5||percent==100){
					
					percentOld = percent;
					
					if(Config.gui){
						Console.setBarValue((int) Math.rint(percent));
						Console.setBarTextPlusRestTime("Writing costmatrices  " + percent + "%");
					}//else  System.out.print( percent + "%\t");
					
				}
				
				for (int j = 0; j < cluster.size(); j++) { 
					String id = proteins2integers.get(cluster.get(j));
					integers2proteins.remove(id);
				}
				
			}else{
				int countPosition = 0;
				
				Edges edges = new Edges(numberEdges,numberProteins);
				
				// create edges for connected components
				for (int j = 0; j < cluster.size(); j++) {
					int source = cluster.get(j);
					edges.setStartPosition(j, countPosition);
				
					for (int k = j+1; k < cluster.size(); k++) {
						int target = cluster.get(k);
						float value = getEdgeValue(source,target,es);
//						if(value<0) Console.println(value);
						
						if(value>Config.threshold){
							countRealEdges++;
						}
						edges.setSource(countPosition, j);
						edges.setTarget(countPosition, k);
						edges.setValue(countPosition, value);
						countPosition++;
					}
					edges.setEndPosition(j, countPosition-1);
				}
				

				// divide between complete and incomplete connected components
				if(countRealEdges==numberEdges){
					complete.add(cluster);
				}else{
								
					writeCostMatrix(edges,cluster,countCostMatrices,proteins2integers,integers2proteins);
					countCostMatrices++;
					
				}
				
				double percent = Math.rint((((double) alreadySolvedNumberOfProteins)/((double) numberOfProteins))*10000)/100;
				
				if(percent>percentOld+0.5||percent==100){
					
					percentOld = percent;
					
					if(Config.gui){
						Console.setBarValue((int) Math.rint(percent));
						Console.setBarTextPlusRestTime("Writing costmatrices  " + percent + "%");
					}//else  System.out.print( percent + "%\t");
					
				}
				
				
				// remove proteins from list which are assigned to one cluster
				for (int j = 0; j < cluster.size(); j++) { 
					String id = proteins2integers.get(cluster.get(j));
					integers2proteins.remove(id);
				}
			}
			
		}
		
		for (Iterator<String> iter = integers2proteins.keySet().iterator(); iter.hasNext();) {
			
			String element = iter.next();
			int id = integers2proteins.get(element);
			Vector<Integer> cluster = new Vector<Integer>();
			cluster.add(id);
			complete.add(cluster);
			
		}

		writeCompleteTable(complete,proteins2integers);
		
	}
	
	public static float getEdgeValue(int source, int target, Edges es) {
		
		int startPosition = es.getStartPosition(source);
		int endPosition = es.getEndPosition(source);
		
		for (int i = startPosition; i < endPosition; i++) {
			
			int target2 = es.getTarget(i);
			if(target==target2) return es.getValue(i);
			
		}
		
		return InOut.min;
		
	}

	private static void writeCostMatrix(Edges es, Vector<Integer> cluster, int countCostMatrices, HashMap<Integer, String> proteins2integers, HashMap<String, Integer> integers2proteins) throws IOException{
		String costMatrixFile;
		if(TaskConfig.mode==TaskConfig.HIERARICHAL_MODE){
			if(Config.reducedMatrix){
				costMatrixFile = "costMatrix_size_"+cluster.size()+"_nr_"+countCostMatrices + "_" + new Random().nextDouble()+  ".rcm";
			} else {
				costMatrixFile = "costMatrix_size_"+cluster.size()+"_nr_"+countCostMatrices +  "_" + new Random().nextDouble()+".cm";
			}
		}else{
			if(Config.reducedMatrix){
				costMatrixFile = "costMatrix_size_"+cluster.size()+"_nr_"+countCostMatrices + ".rcm";
			} else {
				costMatrixFile = "costMatrix_size_"+cluster.size()+"_nr_"+countCostMatrices + ".cm";
			}
		}
		
			
		BufferedWriter bw = new BufferedWriter(new FileWriter(Config.costMatrixDirectory+ "/" +costMatrixFile));
		
		bw.write(Integer.toString(cluster.size()));
		bw.newLine();
		
		for (int i = 0; i < cluster.size(); i++) {
			String id = proteins2integers.get(cluster.get(i));
			bw.write(id);
			bw.newLine();
		}
		
		int k =0;
		for (int i = 0; i < cluster.size(); i++) {
			for (int j = i+1; j < cluster.size(); j++) {
				float value = es.getValue(k);
				if(value<Config.threshold){
					value = -Math.abs(value-Config.threshold);
				}else{
					value = Math.abs(value-Config.threshold);
				}
				
				bw.write(Float.toString(value));
				if(j<cluster.size()-1){
					bw.write("\t");
				}
				k++;
			}
			if(i<cluster.size()-1){
				bw.newLine();
			}
		}
		
		
		bw.close();	
	}
	
	private static void writeCompleteTable(Vector<Vector<Integer>> complete, HashMap<Integer, String> proteins2integers) throws IOException{
		
		String fileName = "transitive_connected_components_format_1.tcc";
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(Config.costMatrixDirectory+"/"+fileName));
		
		bw.write("Number of connected components: " + Integer.toString(complete.size()));
		bw.newLine();
		
		for (int i = 0; i < complete.size(); i++) {
			
			Vector<Integer> cluster = complete.get(i);
			
			bw.write(Integer.toString(cluster.size()) + TAB);
			
			for (int j = 0; j < cluster.size(); j++) {
				
				String id = proteins2integers.get(cluster.get(j));
				
				if(j<cluster.size()-1) bw.write(id + TAB);
				else bw.write(id);
				
			}
			
			bw.newLine();
			
		}
	
		bw.close();
		
	}
	
	public static int countLines(File file) throws IOException {
		
       BufferedReader br = new BufferedReader(new FileReader(file));
        
        if(Config.gui){
        	Console.setBarValue(0);
        	Console.setBarText("counting lines");
        }//else  System.out.println("counting lines");
        
        int lineCount = 0;
      
        char[] buffer = new char[4096];
        
        for (int charsRead = br.read(buffer); charsRead >= 0; charsRead = br.read(buffer)) {
        	
            for (int charIndex = 0; charIndex < charsRead ; charIndex++) {
            	
                if (buffer[charIndex] == '\n') {
                	
                	lineCount++;
                	if(lineCount%100000==0&&Config.gui) Console.setBarText("counting lines  " + lineCount);
                	
                }
                
            }
            
        }
        
        br.close();
        
        return lineCount;
    }
	
}
