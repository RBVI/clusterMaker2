package edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.main;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes.BlastFile;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.dataTypes.SourceHSPs;
import edu.ucsf.rbvi.clusterMaker2.internal.algorithms.networkClusterers.TransClust.de.costmatrixcreation.gui.Console;


public class Creator {

	
	public Creator(){
		
	}
	
	public void run(HashMap<Integer, String> proteins2integers, HashMap<String, Integer> integers2proteins) throws IOException{
		
		if(Config.source==Config.BLAST){
			if(Config.gui) Console.println("Read Fasta file ... ");
			int[] proteinLengths = InOut.readFastaFile(Config.fastaFile, proteins2integers, integers2proteins);
			if(Config.gui) Console.println();
			
			if(Config.gui) Console.println("Read Blast file ... ");
			BlastFile bf = InOut.readBlastFileWithArray(Config.blastFile, integers2proteins,proteinLengths);
			if(Config.gui) Console.println();
			
			
//			createHSPCluster(bf, proteins2integers, integers2proteins, proteinLengths);
			
			
			if(Config.gui) Console.println("Create similarity file ...");
			createSimilarityFileFromArray(Config.similarityFile, bf, proteins2integers, proteinLengths, Config.costModel);
			if(Config.gui) Console.println();
			
			if(Config.splitAndWriteCostMatrices){
				bf = null;
				System.gc();
			}	
		}else{
//			if(Config.gui) Console.println("Read Matrix ...");
//			createSimilarityFileFromExpressionMatrix(Config.similarityFile, Config.expressionMatrix, Config.withHeader, Config.withRowDescription, Config.costModel);
		}
		
	}
	
	public void runWithExpression() throws IOException{
		if(Config.gui) Console.println("Read Matrix ...");
		createSimilarityFileFromExpressionMatrix(Config.similarityFile, Config.expressionMatrix, Config.withHeader, Config.withRowDescription, Config.costModel);
	
	}
	
	private void createSimilarityFileFromExpressionMatrix(String simFile, String expressionMatrix, boolean withHeader, boolean withRowDescription, int costModel) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(expressionMatrix));
		HashMap<String, ArrayList<Double>> vectors = new HashMap<String, ArrayList<Double>>();
		String line;
		int count = 0;
		if(withHeader) br.readLine();
		while((line=br.readLine())!=null){
			if(line.trim().equals("")) continue;
			String tabs[] = line.split("\t");
			String elementName = Integer.toString(++count);
			int modifier = 0;
			if(withRowDescription){
				elementName = tabs[0];
				modifier = 1;
			}
			ArrayList<Double> al = new ArrayList<Double>();
			for (int i = modifier; i < tabs.length; i++) {
				al.add(Double.parseDouble(tabs[i]));
			}
			vectors.put(elementName, al);
		}
		br.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(simFile));
		
		for (String key : vectors.keySet()) {
			ArrayList<Double> al1 = vectors.get(key);
			for (String key2 : vectors.keySet()) {
//				if(key.equals(key2)) continue;
				ArrayList<Double> al2 = vectors.get(key2);
				if(costModel==Config.PEARSON){
					bw.write(key + "\t" + key2 + "\t" + calculatePearson(al1,al2));	
					bw.newLine();
				}else if(costModel==Config.NEGATIVEEUCLIDIAN){
					bw.write(key + "\t" + key2 + "\t" + calculateNegativeEuclidian(al1,al2));
					bw.newLine();
				}
			}
		}
		bw.flush();
		bw.close();
		
	}
	
	private double calculateNegativeEuclidian(ArrayList<Double> al1,
			ArrayList<Double> al2) {
		double negativeEuclidian =0;
		
		for (int i = 0; i < al1.size(); i++) {
			double d1 = al1.get(i);
			double d2 = al2.get(i);
			negativeEuclidian+=((d1-d2)*(d1-d2));
		}
		negativeEuclidian = Math.sqrt(negativeEuclidian);
		negativeEuclidian = -negativeEuclidian;
		return negativeEuclidian;
	}

	private double calculatePearson(ArrayList<Double> x, ArrayList<Double> y) {
		if(x.size()!=y.size()){
			System.err.println("The size of x and y have to be equal");
			return Double.NEGATIVE_INFINITY;
		}
		double correlation = 0;
		
		double meanX = mean(x);
		double meanY = mean(y);
		double standardDeviationX = standardDeviation(x,meanX);
		double standardDeviationY = standardDeviation(y,meanY);
		
//		for (int i = 0; i < y.size(); i++) {
//			y.set(i, (y.get(i)-meanY)/standardDeviationY);
//		}
//		for (int i = 0; i < x.size(); i++) {
//			x.set(i, (x.get(i)-meanX)/standardDeviationX);
//		}
		for (int i = 0; i < y.size(); i++) {
//			correlation+=x.get(i)*y.get(i);
			correlation+=(((x.get(i)-meanX))*((y.get(i)-meanY)));
//			System.out.println(x.get(i) + "\t" + meanX + "\t" + (x.get(i)-meanX) +"\t" +  + y.get(i) + "\t" + meanY+ "\t" + (y.get(i)-meanY) + "\t"+  correlation + "\t" + (((x.get(i)-meanX)/standardDeviationX)*((y.get(i)-meanY)/standardDeviationY)));
		}
//		correlation/=(x.size()-1);
		correlation/=((x.size())*standardDeviationX*standardDeviationY);
//		System.out.println(correlation);
//		correlation*=x.size();
		return correlation;
	}
	private static double standardDeviation(ArrayList<Double> x, double meanX) {
		double standardDeviation = 0;
		
		for (int i = 0; i < x.size(); i++) {
			standardDeviation+=((x.get(i)-meanX)*(x.get(i)-meanX));
		}
		standardDeviation/=x.size();
		standardDeviation = Math.sqrt(standardDeviation);
		return standardDeviation;
	}

	private static double mean(ArrayList<Double> x) {
		double mean = 0;
		for (int i = 0; i < x.size(); i++) {
			mean+=x.get(i);
		}
		mean/=x.size();
		return mean;
	}

	@SuppressWarnings("unchecked")
	private void createHSPCluster(BlastFile bf,HashMap<Integer, String> proteins2integers,HashMap<String, Integer> integers2proteins, int[] proteinLengths){
		
		HashMap<String, Integer> integers2proteinsClone = (HashMap<String, Integer>) integers2proteins.clone();
		HashMap<Integer, String> proteins2integersClone = (HashMap<Integer, String>) proteins2integers.clone();
		
//		integers2proteins.clear();
//		proteins2integers.clear();
		
		HashMap<String,SourceHSPs> sourceHSPs = new HashMap<String,SourceHSPs>();
		
		for (Iterator<String> iterator = integers2proteinsClone.keySet().iterator(); iterator.hasNext();) {
			String id = iterator.next();
			int idInt = integers2proteinsClone.get(id);
			SourceHSPs sHSP = new SourceHSPs(new ArrayList<boolean[]>(), proteinLengths[idInt], new ArrayList<Integer>(), id);
			sourceHSPs.put(id,sHSP);
		}
		
		for (int i = 0; i < bf.size; i++) {
			if(bf.getSource(i)==bf.getTarget(i)) continue;
			
			SourceHSPs sourceHSP = sourceHSPs.get(proteins2integersClone.get(bf.getSource(i)));
			SourceHSPs targetHSP = sourceHSPs.get(proteins2integersClone.get(bf.getTarget(i)));
			
			boolean[] sourceCoverage = new boolean[proteinLengths[bf.getSource(i)]];
			boolean[] targetCoverage = new boolean[proteinLengths[bf.getTarget(i)]];
			
			Arrays.fill(sourceCoverage, bf.getStartQuery(i), bf.getEndQuery(i), true);
			Arrays.fill(targetCoverage, bf.getStartSubject(i), bf.getEndSubject(i), true);
			
			
			ArrayList<boolean[]> coveragesSource = sourceHSP.getCoverages();
			sourceHSP.addLine(i);
			if(coveragesSource.isEmpty()){
				coveragesSource.add(sourceCoverage);
				sourceHSP.addCluster(0);
				sourceHSP.addClusterLine(0);
			}else{
				boolean match = false;
				for (int j = 0; j < coveragesSource.size(); j++) {
					
					boolean coverage[] = coveragesSource.get(j);
					
					float sim = calculateSimilarity(coverage, sourceCoverage);
					
					if(sim>0.8){
						match = true;
						sourceHSP.addCluster(j);
						break;
					}					
				}
				if(!match){
					coveragesSource.add(sourceCoverage);
					sourceHSP.setClusternr(sourceHSP.getClusternr()+1);
					sourceHSP.addClusterLine(i);
					sourceHSP.addCluster(sourceHSP.getClusternr());
				}
			}
			
			ArrayList<boolean[]> coveragesTarget = targetHSP.getCoverages();
			targetHSP.addLine(i);
			if(coveragesTarget.isEmpty()){
				coveragesTarget.add(targetCoverage);
				targetHSP.addCluster(0);
				targetHSP.addClusterLine(0);
			}else{
				boolean match = false;
				for (int j = 0; j < coveragesTarget.size(); j++) {
					
					boolean coverage[] = coveragesTarget.get(j);
					
					float sim = calculateSimilarity(coverage, targetCoverage);
					
					if(sim>0.8){
						match = true;
						targetHSP.addCluster(j);
						break;
					}					
				}
				if(!match){
					coveragesTarget.add(targetCoverage);
					targetHSP.setClusternr(targetHSP.getClusternr()+1);
					targetHSP.addClusterLine(i);
					targetHSP.addCluster( targetHSP.getClusternr());
				}
			}
		}
		
		for (Iterator iterator = sourceHSPs.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			SourceHSPs s = sourceHSPs.get(key);
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(Config.blastFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(Config.blastFile + "HSP"));
			String line;
			int k = 0;
			while((line=br.readLine())!=null){
				if(line.trim().equals("")) continue;
				String tabs[] = line.split("\t");
				if(tabs[0].equals(tabs[1])){
					k++;
					continue;
				}
				SourceHSPs source = sourceHSPs.get(tabs[0]);
				SourceHSPs target = sourceHSPs.get(tabs[1]);
				int sourceIndex = source.getLines().indexOf(k);
				int targetIndex = target.getLines().indexOf(k);
				bw.write(tabs[0]+"_HSP" + source.getCluster(sourceIndex) + "\t" + tabs[1]+"_HSP" + target.getCluster(targetIndex)+"\t");
				for (int j = 2; j < tabs.length; j++) {
					bw.write(tabs[j]);
					if(j<tabs.length-1){
						bw.write("\t");
					}else{
						bw.newLine();
					}
				}
				k++;
			}
			br.close();
			bw.flush();
			bw.close();
			
			br = new BufferedReader(new FileReader(Config.fastaFile));
			bw = new BufferedWriter(new FileWriter(Config.fastaFile + "HSP"));
			String sequence = "";
			String id = "";
			while((line=br.readLine())!=null){
				if(line.trim().equals("")) continue;
				if(line.startsWith(">")){
					if(!sequence.equals("")){
						SourceHSPs source = sourceHSPs.get(id);
						for (int j = 0; j <= source.getClusternr(); j++) {
							bw.write(">"+id+"_HSP" + j);
							bw.newLine();
							bw.write(sequence);
							bw.newLine();
						}
						sequence = "";
					}
					id = line.substring(1);
					
				}else{
					sequence += line;
				}
			}
			SourceHSPs source = sourceHSPs.get(id);
			for (int j = 0; j <= source.getClusternr(); j++) {
				bw.write(">"+id+"_HSP" + j);
				bw.newLine();
				bw.write(sequence);
				bw.newLine();
			}
			br.close();
			bw.flush();
			bw.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		
		
		
	}
	private float calculateSimilarity(boolean[] bs, boolean[] bs2) {
		float sim = 0;
		for (int i = 0; i < bs2.length; i++) {
			if((bs[i]&&bs2[i])||(!bs[i]&&!bs2[i])) sim++;
		}
		sim/=bs.length;
		return sim;
	}
	
	private void createSimilarityFileFromArray(String outputFile, BlastFile bf, HashMap<Integer,String> proteins2integers, int[] proteinLengths, int costModel) throws IOException{
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
		
		double percent = 0;
		double percentOld = 0;
		
		if(Config.gui){
			Console.println("start calculating similarities and writing file ...");
			Console.setBarValue(0);
			Console.restartBarTimer();
			Console.setBarText("calculating similarities and writing file");
                }
		for (int i = 0; i < bf.size; ) {
			
			if(i%100000==0&&i>0){
				percent  = Math.rint(((double) i / bf.size)*10000)/100;
				if(percent>percentOld+1){
					percentOld = percent;
					if(Config.gui){
						Console.setBarValue((int) Math.rint(percent));
						Console.setBarTextPlusRestTime("calculating similarities and writing file  " + percent + " %");
                                        }
				}
			}
						
			Vector<Integer> v = new Vector<Integer>();
			int source = bf.getSource(i);
			int target = bf.getTarget(i);
			
			if(source==target) {
				i++;
				continue;
			}
			
			v.add(i);
			
			int j = 1;
			try {
				
				while(true){
					
					int source2 = bf.getSource(i+j);
					int target2 = bf.getTarget(i+j);
					if(source==source2&&target==target2){
						v.add(i+j);
						j++;
					}else{
						i = i+j;
						break;
					}
					
				}
				
			}catch (Exception e) {
				i = i+j;
			}
			

			switch (costModel) {
			
			case 0:{//BeH
							
				
				double similarity = calculateBeH(v, bf);
				
				String sourceString = proteins2integers.get(source);

				String targetString  = proteins2integers.get(target);
				
				if(similarity==0) continue;
				
				bw.write(sourceString + InOut.TAB + targetString + InOut.TAB + Double.toString(similarity));
				bw.newLine();
				Config.linesInSimilarityFile++;
				
				break;
			}
			
			case 1:{//SoH
				
				double similarity = calculateSoH(v, bf);
				
				if(similarity==0) continue;
				
				String sourceString = proteins2integers.get(source);

				String targetString  = proteins2integers.get(target);
					
				bw.write(sourceString + InOut.TAB + targetString + InOut.TAB + Double.toString(similarity));
				bw.newLine();
				Config.linesInSimilarityFile++;
				
				break;
			}
			
			case 2:{//coverage BeH
				
				double similarity = calculateBeH(v, bf);
				
				double coverage = calculateCoverage(v,bf,proteinLengths, source, target);
				
				if(similarity==0) continue;
				
				String sourceString = proteins2integers.get(source);

				String targetString  = proteins2integers.get(target);
					
				bw.write(sourceString + InOut.TAB + targetString + InOut.TAB + Double.toString(similarity+coverage));
				bw.newLine();
				Config.linesInSimilarityFile++;

				break;
			}
			
			case 3:{//coverage SoH
				
				double similarity = calculateSoH(v, bf);
				
				double coverage = calculateCoverage(v,bf,proteinLengths, source, target);
				
				if(similarity==0) continue;
				
				String sourceString = proteins2integers.get(source);

				String targetString  = proteins2integers.get(target);
					
				bw.write(sourceString + InOut.TAB + targetString + InOut.TAB + Double.toString(similarity+coverage));
				bw.newLine();
				Config.linesInSimilarityFile++;
				
				break;
			}
			
			case 4:{//Score
							
				
				double similarity = calculateScore(v, bf);
				
				String sourceString = proteins2integers.get(source);

				String targetString  = proteins2integers.get(target);
				
				if(similarity==0) continue;
				
				bw.write(sourceString + InOut.TAB + targetString + InOut.TAB + Double.toString(similarity));
				bw.newLine();
				Config.linesInSimilarityFile++;
				
				break;
			}
			
			
			default:
				break;
			}//end switch
			
		}//end for	
		
		bw.close();
		
	}//end method

	private static double calculateBeH(Vector<Integer> v, BlastFile bf){
		
		double bestEvalue = 0;
		
		for (int k = 0; k < v.size(); k++) {
			int line = v.get(k);
			double evalue = bf.getEvalue(line);
//			double evalue = bf.getScore(line);
			if(evalue>bestEvalue){
				bestEvalue = evalue;
			}		
		}
		
		if(bestEvalue<0) Console.println("" + bestEvalue);
		
		return bestEvalue;
		
	}
	
	private static double calculateScore(Vector<Integer> v, BlastFile bf){
		
		double bestEvalue = 0;
		
		for (int k = 0; k < v.size(); k++) {
			int line = v.get(k);
//			double evalue = bf.getEvalue(line);
			double evalue = bf.getScore(line);
			if(evalue>bestEvalue){
				bestEvalue = evalue;
			}		
		}
		
		if(bestEvalue<0) Console.println("" + bestEvalue);
		
		return bestEvalue;
		
	}
	
	private static double calculateSoH(Vector<Integer> v, BlastFile bf){
		
		double similarity = 0;
		
		for (int k = 0; k < v.size(); k++) {
			int line = v.get(k);
			double evalue = bf.getEvalue(line);
			similarity +=evalue;	
		}
//		double penalty = Math.pow(Config.penaltyForMultipleHighScoringPairs,v.size()-1);
//		similarity *= (1/penalty);
//		similarity = -Math.log10(similarity);
		
		return similarity;
		
	}
	
	private static double calculateCoverage(Vector<Integer> v, BlastFile bf, int[] proteinLengths, int source, int target){
		
		double coverage = 0;
		
		int sourceLength = proteinLengths[source];
		int targetLength = proteinLengths[target];
		
		boolean query[] = new boolean[sourceLength];
		boolean subject[] = new boolean[targetLength];
		
		for (int k = 0; k < v.size(); k++) {
			int line = v.get(k);
			int startQuery = bf.getStartQuery(line);
			int endQuery = bf.getEndQuery(line);
			int startSubject = bf.getStartSubject(line);
			int endSubject = bf.getEndSubject(line);
			
		
			for (int i = startQuery; i < endQuery; i++) {
				query[i] = true;
			}
			for (int i = startSubject; i < endSubject; i++) {
				subject[i] = true;
			}
			
		}
		
		double queryCoverage = 0;
		for (int i = 0; i < query.length; i++) {
			if(query[i]) queryCoverage++;
		}
		
		double subjectCoverage = 0;
		for (int i = 0; i < subject.length; i++) {
			if(subject[i]) subjectCoverage++;
		}
		
		queryCoverage/=sourceLength;
		
		subjectCoverage/=targetLength;
		
		coverage = Math.min(queryCoverage, subjectCoverage)*Config.coverageFactor;
		
		return coverage;
		
	}
	
}
